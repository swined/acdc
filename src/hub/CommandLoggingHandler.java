package hub;

import logger.ILogger;
import util.DCReader.IDCCommandHandler;

class CommandLoggingHandler implements IDCCommandHandler {

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
            logger.debug("recieved command from hub: " + new String(data, start, length));
    }

}
