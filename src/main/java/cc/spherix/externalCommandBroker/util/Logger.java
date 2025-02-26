package cc.spherix.externalCommandBroker.util;

import org.bukkit.plugin.java.JavaPlugin;

public class Logger {

    private static JavaPlugin plugin;

    private Logger() {
    }

    public static void init(JavaPlugin plugin) {
        if (Logger.plugin != null) {
            return;
        }
        Logger.plugin = plugin;
    }

    public static org.slf4j.Logger logger() {
        validate();
        return plugin.getSLF4JLogger();
    }

    public static String getName() {
        return logger().getName();
    }

    public static void trace(String msg) {
        logger().info(tracePrefix() + msg);
    }

    public static void trace(String format, Object arg) {
        logger().info(tracePrefix() + format, arg);
    }

    public static void trace(String format, Object arg1, Object arg2) {
        logger().info(tracePrefix() + format, arg1, arg2);
    }

    public static void trace(String format, Object... arguments) {
        logger().info(tracePrefix() + format, arguments);
    }

    public static void trace(String msg, Throwable t) {
        logger().info(tracePrefix() + msg, t);
    }

    public static void debug(String msg) {
        logger().info(debugPrefix() + msg);
    }

    public static void debug(String format, Object arg) {
        logger().info(debugPrefix() + format, arg);
    }

    public static void debug(String format, Object arg1, Object arg2) {
        logger().info(debugPrefix() + format, arg1, arg2);
    }

    public static void debug(String format, Object... arguments) {
        logger().info(debugPrefix() + format, arguments);
    }

    public static void debug(String msg, Throwable t) {
        logger().info(debugPrefix() + msg, t);
    }

    public static void info(String msg) {
        logger().info(msg);
    }

    public static void info(String format, Object arg) {
        logger().info(format, arg);
    }

    public static void info(String format, Object arg1, Object arg2) {
        logger().info(format, arg1, arg2);
    }

    public static void info(String format, Object... arguments) {
        logger().info(format, arguments);
    }

    public static void info(String msg, Throwable t) {
        logger().info(msg, t);
    }

    public static boolean isWarnEnabled() {
        return logger().isWarnEnabled();
    }

    public static void warn(String msg) {
        logger().warn(msg);
    }

    public static void warn(String format, Object arg) {
        logger().warn(format, arg);
    }

    public static void warn(String format, Object... arguments) {
        logger().warn(format, arguments);
    }

    public static void warn(String format, Object arg1, Object arg2) {
        logger().warn(format, arg1, arg2);
    }

    public static void warn(String msg, Throwable t) {
        logger().warn(msg, t);
    }

    public static void error(String msg) {
        logger().error(msg);
    }

    public static void error(String format, Object arg) {
        logger().error(format, arg);
    }

    public static void error(String format, Object arg1, Object arg2) {
        logger().error(format, arg1, arg2);
    }

    public static void error(String format, Object... arguments) {
        logger().error(format, arguments);
    }

    public static void error(String msg, Throwable t) {
        logger().error(msg, t);
    }

    private static String debugPrefix() {
        return ConsoleColors.BLUE_BRIGHT + "[DEBUG] ";
    }

    private static String tracePrefix() {
        return ConsoleColors.PURPLE_BRIGHT + "[TRACE] ";
    }

    private static void validate() {
        if (plugin == null) {
            throw new RuntimeException();
        }
    }

    public static class ConsoleColors {

        // Reset
        public static final String RESET = "\033[0m";  // Text Reset

        // Regular Colors
        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String RED = "\033[0;31m";     // RED
        public static final String GREEN = "\033[0;32m";   // GREEN
        public static final String YELLOW = "\033[0;33m";  // YELLOW
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String PURPLE = "\033[0;35m";  // PURPLE
        public static final String CYAN = "\033[0;36m";    // CYAN
        public static final String WHITE = "\033[0;37m";   // WHITE

        // Bold
        public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
        public static final String RED_BOLD = "\033[1;31m";    // RED
        public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
        public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
        public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
        public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
        public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
        public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

        // Underline
        public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
        public static final String RED_UNDERLINED = "\033[4;31m";    // RED
        public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
        public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
        public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
        public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
        public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
        public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

        // Background
        public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
        public static final String RED_BACKGROUND = "\033[41m";    // RED
        public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
        public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
        public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
        public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
        public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
        public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

        // High Intensity
        public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
        public static final String RED_BRIGHT = "\033[0;91m";    // RED
        public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
        public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
        public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
        public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
        public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
        public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

        // Bold High Intensity
        public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
        public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
        public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
        public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m"; // YELLOW
        public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
        public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m"; // PURPLE
        public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
        public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

        // High Intensity backgrounds
        public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m"; // BLACK
        public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m"; // RED
        public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m"; // GREEN
        public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m"; // YELLOW
        public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m"; // BLUE
        public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
        public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
        public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
    }

    public enum SwitchableLevel {
        DEBUG,
        TRACE,
    }
}
