package com.petstore.shop;

import com.petstore.animal.Animal;
import com.petstore.animal.AnimalNameGenerator;
import com.petstore.animal.Cat;
import com.petstore.animal.ChineseRuralDog;
import com.petstore.animal.Gender;
import com.petstore.animal.GoldenHamster;
import com.petstore.animal.MiniPig;
import com.petstore.animal.Parrot;
import com.petstore.customer.Customer;
import com.petstore.exception.AnimalNotFountException;
import com.petstore.exception.InsufficientBalanceException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 我的宠物店（{@link AnimalShop} 的实现）。
 *
 * @author pet-store
 */
public class MyAnimalShop implements AnimalShop {

    /** 初始默认余额 */
    public static final double DEFAULT_BALANCE = 200.0;

    /** 每天最多接待顾客数 */
    public static final int MAX_CUSTOMERS_PER_DAY = 8;

    /** 开业时间（小时） */
    public static final int OPEN_HOUR = 14;

    /** 歇业时间（小时） */
    public static final int CLOSE_HOUR = 21;

    /** 每位顾客推进的分钟数 */
    public static final int MINUTES_PER_CUSTOMER = 52;

    /** 进价占售价的比例 */
    public static final double PURCHASE_RATIO = 0.2;

    private double balance;
    private final List<Animal> animalList = new ArrayList<>();
    private final List<Customer> customerList = new ArrayList<>();
    private boolean isOpen;

    private final ShopDate shopDate;
    private final AnimalNameGenerator nameGenerator = new AnimalNameGenerator();

    /** 全部动物品种 */
    public static final List<Class<? extends Animal>> SPECIES_CLASSES = List.of(
            ChineseRuralDog.class, Cat.class, MiniPig.class, Parrot.class, GoldenHamster.class);

    /** 每类动物的进货候选货位（每类 5 个） */
    private final Map<Class<? extends Animal>, List<Animal>> candidateSlots = new HashMap<>();

    /** 今日光顾顾客（含未成交） */
    private final List<Customer> todayVisitors = new ArrayList<>();
    /** 今日成交金额 */
    private double todaySales;
    /** 今日进货花销 */
    private double todayPurchaseCost;

    public MyAnimalShop(double balance) {
        this.balance = balance;
        // 自定营业日起始日期（不使用真实时间）
        this.shopDate = new ShopDate(LocalDate.of(2026, 1, 1));
    }

    public MyAnimalShop() {
        this(DEFAULT_BALANCE);
    }

    // ==================== 接口方法 ====================

    @Override
    public void buyAnimal(Animal animal) throws InsufficientBalanceException {
        double cost = animal.getPrice() * PURCHASE_RATIO;
        if (balance < cost) {
            throw new InsufficientBalanceException(
                    String.format("余额不足：需要 ¥%.2f，当前余额 ¥%.2f", cost, balance));
        }
        balance -= cost;
        animalList.add(animal);
        todayPurchaseCost += cost;
    }

    @Override
    public void entertainCustomer(Customer customer) throws AnimalNotFountException {
        if (animalList.isEmpty()) {
            throw new AnimalNotFountException("店内没有动物可卖！");
        }
        // 题目要求：出售店内第一只动物
        Animal sold = animalList.remove(0);
        balance += sold.getPrice();
        todaySales += sold.getPrice();
        recordCustomerVisit(customer);
    }

    @Override
    public DailyReport close() {
        DailyReport report = new DailyReport(
                shopDate.getDate().toString(),
                new ArrayList<>(todayVisitors),
                todaySales,
                todayPurchaseCost);
        // 控制台输出原题要求
        System.out.println(report);
        // 重置当日统计，进入下一营业日
        todayVisitors.clear();
        todaySales = 0;
        todayPurchaseCost = 0;
        isOpen = false;
        shopDate.nextDay();
        // 次日进货货位补货
        refillCandidateSlots();
        return report;
    }

    // ==================== GUI 辅助方法 ====================

    /**
     * 开业：重置当日统计。
     */
    public void open() {
        todayVisitors.clear();
        todaySales = 0;
        todayPurchaseCost = 0;
        isOpen = true;
    }

    /**
     * 生成一只候选动物（用于进货界面货位）。
     *
     * @param speciesClass 品种类
     * @return 候选动物；编号耗尽返回 {@code null}
     */
    public Animal createCandidate(Class<? extends Animal> speciesClass) {
        String name = nameGenerator.nextName(speciesClass);
        if (name == null) {
            return null;
        }
        Gender gender = randomGender();
        int age = ThreadLocalRandom.current().nextInt(1, 5); // 1~4
        try {
            return speciesClass.getConstructor(String.class, int.class, Gender.class)
                    .newInstance(name, age, gender);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("无法创建动物实例: " + speciesClass.getSimpleName(), e);
        }
    }

    /**
     * 获取指定品种的 5 个进货货位（首次调用时初始化）。
     *
     * @param speciesClass 品种类
     * @return 长度为 5 的货位列表（元素可能为 null，表示空位）
     */
    public List<Animal> getCandidateSlots(Class<? extends Animal> speciesClass) {
        return candidateSlots.computeIfAbsent(speciesClass, k -> {
            List<Animal> slots = new ArrayList<>(5);
            for (int i = 0; i < 5; i++) {
                slots.add(createCandidate(k));
            }
            return slots;
        });
    }

    /**
     * 从货位取出一只候选动物（货位置空）。
     *
     * @param speciesClass 品种类
     * @param index        货位索引 0~4
     * @return 取出的动物；若货位已空返回 {@code null}
     */
    public Animal takeCandidate(Class<? extends Animal> speciesClass, int index) {
        List<Animal> slots = getCandidateSlots(speciesClass);
        Animal animal = slots.get(index);
        slots.set(index, null);
        return animal;
    }

    /**
     * 将动物放回货位（买入失败时回滚）。
     *
     * @param speciesClass 品种类
     * @param index        货位索引
     * @param animal       要放回的动物
     */
    public void returnCandidate(Class<? extends Animal> speciesClass, int index, Animal animal) {
        getCandidateSlots(speciesClass).set(index, animal);
    }

    /**
     * 补货：将所有空货位用新候选动物填满（歇业后次日调用）。
     */
    public void refillCandidateSlots() {
        for (Class<? extends Animal> speciesClass : SPECIES_CLASSES) {
            List<Animal> slots = getCandidateSlots(speciesClass);
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i) == null) {
                    slots.set(i, createCandidate(speciesClass));
                }
            }
        }
    }

    /**
     * 查找店内符合顾客要求的动物。
     *
     * @param species 品种名（中文）
     * @param gender  性别
     * @param age     年龄
     * @return 符合条件的动物列表
     */
    public List<Animal> findMatchingAnimals(String species, Gender gender, int age) {
        List<Animal> result = new ArrayList<>();
        for (Animal a : animalList) {
            if (a.getSpecies().equals(species)
                    && a.getGender() == gender
                    && a.getAge() == age) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * 将指定动物出售给顾客（GUI 选动物成交）。
     *
     * @param customer 顾客
     * @param animal   要出售的动物
     * @return 是否成功
     */
    public boolean sellToCustomer(Customer customer, Animal animal) {
        if (!animalList.remove(animal)) {
            return false;
        }
        balance += animal.getPrice();
        todaySales += animal.getPrice();
        recordCustomerVisit(customer);
        return true;
    }

    /**
     * 记录顾客到店（同名顾客累加次数，否则新增）。
     *
     * @param customer 顾客
     */
    public void recordCustomerVisit(Customer customer) {
        Optional<Customer> existing = customerList.stream()
                .filter(c -> c.getName().equals(customer.getName()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().visit(shopDate.getDate());
            // 若今日已记录过该顾客则不重复加入今日列表
            if (!todayVisitors.contains(existing.get())) {
                todayVisitors.add(existing.get());
            }
        } else {
            customer.visit(shopDate.getDate());
            customerList.add(customer);
            todayVisitors.add(customer);
        }
    }

    /**
     * 仅记录顾客到店（未成交也调用）。
     *
     * @param customer 顾客
     */
    public void visitOnly(Customer customer) {
        recordCustomerVisit(customer);
    }

    // ==================== 工具方法 ====================

    private Gender randomGender() {
        Gender[] values = Gender.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    // ==================== Getter ====================

    public double getBalance() {
        return balance;
    }

    public List<Animal> getAnimalList() {
        return animalList;
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public ShopDate getShopDate() {
        return shopDate;
    }

    public AnimalNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public List<Customer> getTodayVisitors() {
        return todayVisitors;
    }

    public double getTodaySales() {
        return todaySales;
    }

    public double getTodayPurchaseCost() {
        return todayPurchaseCost;
    }
}
