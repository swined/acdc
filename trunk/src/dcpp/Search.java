package dcpp;

import hub.HubConnection;
import logger.ILogger;
import dcpp.DownloadManager.IHubConnectedEventHandler;
import dcpp.DownloadManager.IMainLoopEventHandler;

public class Search implements IHubConnectedEventHandler, IMainLoopEventHandler {

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
	
	public void onMainLoop(DownloadManager manager) throws Exception {
        if (System.currentTimeMillis() - lastSearch > searchPeriod && hub != null) {
            lastSearch = System.currentTimeMillis();
            logger.info("looking for peers");
           	hub.search(tth);
        }
	}
	
}
