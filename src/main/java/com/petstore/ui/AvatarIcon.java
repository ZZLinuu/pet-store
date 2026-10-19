package com.petstore.ui;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * 程序绘制的顾客头像（彩色圆形 + 姓名首字）。
 *
 * @author pet-store
 */
public class AvatarIcon implements Icon {

    private static final Color[] COLORS = {
            new Color(255, 154, 158),
            new Color(255, 196, 132),
            new Color(250, 234, 145),
            new Color(170, 230, 166),
            new Color(133, 210, 230),
            new Color(180, 170, 255),
            new Color(240, 160, 230),
            new Color(160, 210, 200)
    };

    private final int size;
    private final Color color;
    private final String text;

    public AvatarIcon(String name, int size) {
        this.size = size;
        this.color = COLORS[Math.abs(name.hashCode()) % COLORS.length];
        this.text = name.isEmpty() ? "?" : name.substring(0, 1);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont((float) size * 0.5f));
        int textX = x + size / 2 - g2.getFontMetrics().stringWidth(text) / 2;
        int textY = y + size / 2 + g2.getFontMetrics().getAscent() / 3;
        g2.drawString(text, textX, textY);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
