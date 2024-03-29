package peer;

public interface IPeerEventHandler {

    void onHandShakeDone(PeerConnection peer) throws Exception;
    void onNoFreeSlots(PeerConnection peer) throws Exception;
    void onPeerConnected(PeerConnection peer) throws Exception;
    void onPeerData(PeerConnection peer, long offset, byte[] data, int start, int length) throws Exception;
    void onPeerError(PeerConnection peer, String error) throws Exception;
    void onSupportsReceived(PeerConnection peer, String[] features) throws Exception;
    
}
