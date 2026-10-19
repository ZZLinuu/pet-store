package com.petstore.ui;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * UI 工具类。
 *
 * @author pet-store
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * 显示信息提示框。
     */
    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示成功提示框。
     */
    public static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "成功", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 显示错误提示框。
     */
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示确认对话框。
     *
     * @return true 表示用户选择"是"
     */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "确认",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
