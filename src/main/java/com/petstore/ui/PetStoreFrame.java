package com.petstore.ui;

import com.petstore.customer.CustomerNamePool;
import com.petstore.shop.MyAnimalShop;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * 宠物店主窗口。
 * <p>
 * 上半区始终可见（余额 + [所拥有动物][顾客纪念册]）；
 * 下半区为推进按钮（[开业][购买动物]），开业后被营业界面叠加层覆盖。
 *
 * @author pet-store
 */
public class PetStoreFrame extends JFrame {

    private static final int FRAME_WIDTH = 900;
    private static final int FRAME_HEIGHT = 600;
    private static final Color BG_COLOR = new Color(252, 248, 240);

    private final MyAnimalShop shop;
    private final JLayeredPane layeredPane;
    private final JPanel mainPanel;
    private final JPanel lowerPanel;
    private final JLabel balanceLabel;
    private final BusinessPanel businessPanel;

    public PetStoreFrame(MyAnimalShop shop) {
        super("My Pet Store");
        this.shop = shop;

        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);

        // ---- 主面板（默认层）----
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);

        // 顶部：余额 + 两个按钮
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        balanceLabel.setAlignmentX(CENTER_ALIGNMENT);
        refreshBalance();
        topPanel.add(balanceLabel);
        topPanel.add(javax.swing.Box.createVerticalStrut(15));

        JPanel topButtons = new JPanel();
        topButtons.setOpaque(false);
        JButton animalsBtn = createButton("所拥有动物", 130, 40);
        JButton albumBtn = createButton("顾客纪念册", 130, 40);
        animalsBtn.addActionListener(e -> showAnimals());
        albumBtn.addActionListener(e -> showCustomerAlbum());
        topButtons.add(animalsBtn);
        topButtons.add(albumBtn);
        topPanel.add(topButtons);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 下半区：开业 / 购买动物
        lowerPanel = new JPanel();
        lowerPanel.setLayout(new javax.swing.BoxLayout(lowerPanel, javax.swing.BoxLayout.Y_AXIS));
        lowerPanel.setBackground(BG_COLOR);
        lowerPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        JButton openBtn = createButton("开业", 220, 70);
        openBtn.setFont(new Font("微软雅黑", Font.BOLD, 24));
        openBtn.addActionListener(e -> openBusiness());
        lowerPanel.add(openBtn);
        lowerPanel.add(javax.swing.Box.createVerticalStrut(30));

        JButton purchaseBtn = createButton("购买动物", 220, 70);
        purchaseBtn.setFont(new Font("微软雅黑", Font.BOLD, 24));
        purchaseBtn.addActionListener(e -> showPurchase());
        lowerPanel.add(purchaseBtn);

        mainPanel.add(lowerPanel, BorderLayout.CENTER);

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        // ---- 营业界面叠加层（高层，初始不可见）----
        businessPanel = new BusinessPanel(shop, this);
        businessPanel.setVisible(false);
        layeredPane.add(businessPanel, JLayeredPane.PALETTE_LAYER);

        // 布局
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);

        // 窗口大小变化时重新定位叠加层
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutOverlay();
            }
        });
        // 首次显示后定位
        SwingUtilities.invokeLater(this::layoutOverlay);
    }

    /**
     * 刷新余额显示。
     */
    public void refreshBalance() {
        balanceLabel.setText(String.format("余额：¥%.2f", shop.getBalance()));
    }

    private void layoutOverlay() {
        mainPanel.setBounds(0, 0, getContentPane().getWidth(), getContentPane().getHeight());
        // 让 BorderLayout 重新布局
        mainPanel.revalidate();
        // 将叠加层定位到下半区（lowerPanel 的区域）
        Rectangle lowerBounds = SwingUtilities.convertRectangle(
                lowerPanel.getParent(), lowerPanel.getBounds(), layeredPane);
        businessPanel.setBounds(lowerBounds);
    }

    private JButton createButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setMaximumSize(new Dimension(w, h));
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        btn.setFocusPainted(false);
        return btn;
    }

    // ==================== 业务动作 ====================

    private void openBusiness() {
        if (shop.isOpen()) {
            UiUtils.info(this, "已经在营业中啦～");
            return;
        }
        List<String> names = CustomerNamePool.pickRandom(MyAnimalShop.MAX_CUSTOMERS_PER_DAY);
        shop.open();
        businessPanel.startDay(names);
        businessPanel.setVisible(true);
        layeredPane.moveToFront(businessPanel);
        layoutOverlay();
    }

    /**
     * 歇业：由营业界面调用。
     */
    public void closeBusiness() {
        businessPanel.setVisible(false);
        com.petstore.shop.DailyReport report = shop.close();
        refreshBalance();
        new SettlementDialog(this, report).setVisible(true);
    }

    private void showAnimals() {
        new AnimalsDialog(this, shop).setVisible(true);
    }

    private void showCustomerAlbum() {
        new CustomerAlbumDialog(this, shop).setVisible(true);
    }

    private void showPurchase() {
        new PurchaseDialog(this, shop).setVisible(true);
        refreshBalance();
    }
}
