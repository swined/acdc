package hub;

public interface IHubEventHandler {

    void onHubConnected(HubConnection hub);
    void onPeerConnectionRequested(HubConnection hub, String ip, int port);
    void onSearchResult(HubConnection hub, SearchResult result);

}
