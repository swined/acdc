package peer;

import logger.ILogger;
import util.DCReader.IDCEventHandler;

public class DataLoggingHandler implements IDCEventHandler {

    private ILogger logger;

    public DataLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        logger.debug("got " + data.length + " bytes from peer");
    }


}
