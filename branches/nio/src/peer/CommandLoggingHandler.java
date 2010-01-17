package peer;

import logger.ILogger;
import util.DCReader.IDCEventHandler;

public class CommandLoggingHandler implements IDCEventHandler {

    private ILogger logger;

    public CommandLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        logger.debug("got command from peer: " + new String(data));
    }


}
