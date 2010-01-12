package hub;

public interface IHubEventHandler {

    void onHubConnected(HubConnection hub) throws Exception;
    void onSearchResult(HubConnection hub, SearchResult result) throws Exception;
    void onPeerConnectionRequested(HubConnection hub, String ip, int port) throws Exception;

}
