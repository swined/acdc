package peer;

import logger.ILogger;

public class LoggingHandler implements IPeerHandler {

    private ILogger logger;

    public LoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handlePeerData(byte[] data) {
        logger.debug("got " + data.length + " bytes from peer");
    }

    public void handlePeerCommand(byte[] data) throws Exception {
        logger.debug("got command bytes from peer: " + new String(data));
    }


}
