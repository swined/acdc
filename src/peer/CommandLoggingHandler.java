package peer;

import java.util.Arrays;
import logger.ILogger;
import util.DCReader.IDCEventHandler;

public class CommandLoggingHandler implements IDCEventHandler {

    private ILogger logger;

    public CommandLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        logger.debug("got command from peer: " + new String(Arrays.copyOfRange(data, start, start + length)));
    }


}
