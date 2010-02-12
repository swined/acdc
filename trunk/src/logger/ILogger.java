package logger;

public interface ILogger {

    public void debug(String m);
    public void error(String m);
    public void info(String m);
    public boolean supportsDebug();
    public void warn(String m);

}
