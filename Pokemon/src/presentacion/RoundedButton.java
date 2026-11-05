package presentacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Botón redondeado de los menús. Respecto al anterior pinta estados distintos para
 * normal, sobre-el-botón, presionado, deshabilitado y con foco de teclado, y se puede
 * operar con teclado.
 */
class RoundedButton extends JButton {

    private static final int ARC = 15;

    private final Color baseFill;
    private boolean hasFocusRing;

    RoundedButton(String text, Rectangle bounds, Color baseFill) {
        super(text);
        this.baseFill = baseFill;
        setBounds(bounds);
        setFont(Assets.font(18));
        setForeground(Theme.TEXT);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hasFocusRing = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                hasFocusRing = false;
                repaint();
            }
        });
    }

    private Color currentFill() {
        if (!isEnabled()) {
            return Theme.BUTTON_DISABLED_FILL;
        }
        if (getModel().isPressed()) {
            return Theme.BUTTON_PRESSED_FILL;
        }
        if (getModel().isRollover()) {
            return Theme.BUTTON_HOVER_FILL;
        }
        return baseFill;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(currentFill());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

        g2.setStroke(new BasicStroke(hasFocusRing ? 3f : 2f));
        g2.setColor(hasFocusRing ? Theme.FOCUS_RING : Theme.BUTTON_BORDER);
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}
