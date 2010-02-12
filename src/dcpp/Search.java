package dcpp;

import hub.HubConnection;
import logger.ILogger;
import dcpp.DownloadManager.IHubConnectedEventHandler;
import dcpp.DownloadManager.ISelectLoopEventHandler;

public class Search implements IHubConnectedEventHandler, ISelectLoopEventHandler {

	private final ILogger logger;
	private final String tth;
	private final long searchPeriod = 5 * 60 * 1000;
	private HubConnection hub;
	private long lastSearch = 0;
	
	public Search(ILogger logger, String tth) {
		this.logger = logger;
		this.tth = tth;
	}
	
	public void onHubConnected(DownloadManager manager, HubConnection hub) {
		this.hub = hub;		
	}
	
	public void onSelectLoop(DownloadManager manager) {
        if (System.currentTimeMillis() - lastSearch > searchPeriod && hub != null) {
            lastSearch = System.currentTimeMillis();
            logger.info("looking for peers");
            try {
            	hub.search(tth);
            } catch (Exception e) {
            	logger.warn("search failed: " + e.getMessage());
            }
        }
	}
	
}
