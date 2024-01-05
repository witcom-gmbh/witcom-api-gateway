package de.witcom.api.config;

import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import de.witcom.api.command.client.CommandSessionManager;
import de.witcom.api.mcp.McpSessionManager;
import de.witcom.api.serviceplanet.SplSessionManager;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class ConfigurationRefreshEventListener {

	private final McpSessionManager mcpSessionmanager;
	private final SplSessionManager splSessionManager;
	private final CommandSessionManager commandSessionManager;

	public ConfigurationRefreshEventListener(McpSessionManager mcpSessionmanager,SplSessionManager splSessionManager,CommandSessionManager commandSessionManager){
		this.mcpSessionmanager = mcpSessionmanager;
		this.splSessionManager = splSessionManager;
		this.commandSessionManager = commandSessionManager;

	}

	@EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh(RefreshScopeRefreshedEvent event) {
            log.info("Triggering session-refresh for filters");
			this.mcpSessionmanager.triggerSessionRefresh();
			this.splSessionManager.triggerSessionRefresh();
			this.commandSessionManager.triggerSessionRefresh();       
    }
	
}
