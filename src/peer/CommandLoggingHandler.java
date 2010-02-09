package peer;

import logger.ILogger;
import util.DCReader.IDCCommandHandler;

public class CommandLoggingHandler implements IDCCommandHandler {

    private ILogger logger;
    private final static byte[] cmd = new byte[0];

    public CommandLoggingHandler(ILogger logger) {
        this.logger = logger;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) {
        if (logger.supportsDebug())
            logger.debug("got command from peer: " + new String(data, start, length));
    }


}
