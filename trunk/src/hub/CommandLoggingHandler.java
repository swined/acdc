package hub;

import java.util.Arrays;
import logger.ILogger;
import util.DCReader.IDCEventHandler;

class CommandLoggingHandler implements IDCEventHandler {

    private ILogger logger;

    public CommandLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCEvent(byte[] data, int start, int length) {
        if (logger.supportsDebug())
            logger.debug("recieved command from hub: " + new String(Arrays.copyOfRange(data, start, length)));
    }

}
