package hub;

public interface IHubEventHandler {

    void onHubConnected(HubConnection hub);
    void onSearchResult(HubConnection hub, SearchResult result);
    void onPeerConnectionRequested(HubConnection hub, String ip, int port);

}
