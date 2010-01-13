package peer;

import logger.ILogger;
import util.DCReader.IDCEventHandler;

public class LoggingHandler implements IDCEventHandler {

    private ILogger logger;

    public LoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        logger.debug("got command from peer: " + new String(data));
    }


}
