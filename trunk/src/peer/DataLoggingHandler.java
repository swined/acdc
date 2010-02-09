package peer;

import logger.ILogger;
import util.DCReader.IDCDataHandler;

public class DataLoggingHandler implements IDCDataHandler {

    private ILogger logger;

    public DataLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCData(byte[] data, int start, int length) {
        logger.debug("got " + length + " bytes from peer");
    }


}
