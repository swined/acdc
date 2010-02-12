package peer;

import logger.ILogger;

class PeerLogger implements ILogger {

    private ILogger logger;
    private String name;

    public PeerLogger(ILogger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public void debug(String msg) {
        logger.debug("["+ name + "]: " + msg);
    }

    public void error(String msg) {
        logger.error("["+ name + "]: " + msg);
    }

    public void info(String msg) {
        logger.info("["+ name + "]: " + msg);
    }
    public boolean supportsDebug() {
        return logger.supportsDebug();
    }
    public void warn(String msg) {
        logger.warn("["+ name + "]: " + msg);
    }


}
