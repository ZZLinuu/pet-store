package com.petstore.ui;

import com.petstore.customer.Customer;
import com.petstore.shop.MyAnimalShop;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * 顾客纪念册弹窗（全部历史顾客）。
 *
 * @author pet-store
 */
public class CustomerAlbumDialog extends JDialog {

    public CustomerAlbumDialog(JFrame owner, MyAnimalShop shop) {
        super(owner, "顾客纪念册", true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("纪念册（共 " + shop.getCustomerList().size() + " 位顾客）", JLabel.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (Customer c : shop.getCustomerList()) {
            model.addElement(c.toString());
        }
        JList<String> list = new JList<>(model);
        list.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        add(new JScrollPane(list), BorderLayout.CENTER);

        setSize(560, 420);
        setLocationRelativeTo(owner);
    }
}
