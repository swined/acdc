package logger;

public class ConsoleLogger implements ILogger{

    private final boolean debug;

    public ConsoleLogger(boolean debug) {
        this.debug = debug;
    }

    public void debug(String m) {
        if (debug)
            System.err.println("debug: " + m);
    }

    public void error(String m) {
        System.err.println("error: " + m);
    }

    public void info(String m) {
        System.err.println("info: " + m);
    }

    public boolean supportsDebug() {
        return debug;
    }

    public void warn(String m) {
        System.err.println("warn: " + m);
    }


}
