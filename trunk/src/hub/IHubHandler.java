package hub;

interface IHubHandler {

    void handleHubCommand(byte[] data) throws Exception;

}
