package iquldev.iqantiafk;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String process(String message) {
        if (message == null) return "";
        
        // Match &#RRGGBB
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);
        
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String gradient(String text, java.awt.Color start, java.awt.Color end) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (float) (length - 1);
            if (length == 1) ratio = 0;
            
            int r = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
            int g = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
            int b = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);
            
            sb.append(ChatColor.of(new java.awt.Color(r, g, b)));
            sb.append(text.charAt(i));
        }
        
        return sb.toString();
    }
}
