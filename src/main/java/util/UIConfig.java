package util;

import java.awt.*;

public class UIConfig {
    public static final String DEF_FONT_FAMILY = "Consolas";
    public static final int DEF_FONT_SIZE_LABEL = 16;
    public static final int DEF_FONT_SIZE_NORMAL = 14;
    public static final Color COLOR_SUCCESS = new Color(24, 132, 10);
    public static void STATUS_SCHEDULED(Component c) {
        c.setBackground(new Color(255, 239, 150));
    }
    public static void STATUS_COMPLETED(Component c) {
        c.setBackground(new Color(175, 238, 175));
    }
    public static void STATUS_CANCELLED(Component c) {
        c.setBackground(new Color(255, 182, 193));
    }
}