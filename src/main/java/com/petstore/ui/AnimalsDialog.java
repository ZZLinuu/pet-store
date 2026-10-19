package com.petstore.ui;

import com.petstore.animal.Animal;
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
 * 所拥有动物列表弹窗。
 *
 * @author pet-store
 */
public class AnimalsDialog extends JDialog {

    public AnimalsDialog(JFrame owner, MyAnimalShop shop) {
        super(owner, "所拥有动物", true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("店内动物（共 " + shop.getAnimalList().size() + " 只）", JLabel.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (Animal a : shop.getAnimalList()) {
            model.addElement(a.toString());
        }
        JList<String> list = new JList<>(model);
        list.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        add(new JScrollPane(list), BorderLayout.CENTER);

        setSize(560, 400);
        setLocationRelativeTo(owner);
    }
}
