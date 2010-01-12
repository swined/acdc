package peer;

interface IPeerHandler {

    public boolean handlePeerCommand(byte[] data) throws Exception;
    public void handlePeerData(byte[] data) throws Exception;

}
