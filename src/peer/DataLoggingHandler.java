package peer;

import logger.ILogger;
import util.DCReader.IDCEventHandler;

public class DataLoggingHandler implements IDCEventHandler {

    private ILogger logger;

    public DataLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        logger.debug("got " + length + " bytes from peer");
    }


}
