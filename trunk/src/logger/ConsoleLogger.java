package logger;

public class ConsoleLogger implements ILogger{

    private boolean debug = true;

    public ConsoleLogger(boolean debug) {
        this.debug = debug;
    }

    public void info(String m) {
        System.out.println("info: " + m);
    }

    public void debug(String m) {
        if (debug)
            System.out.println("debug: " + m);
    }

    public void warn(String m) {
        System.err.println("warn: " + m);
    }

    public void error(String m) {
        System.err.println("error: " + m);
    }


}
