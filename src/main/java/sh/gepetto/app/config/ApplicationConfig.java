package sh.gepetto.app.config;

import sh.gepetto.app.model.Configuration;
import sh.gepetto.app.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Application configuration for storing global settings
 */
@Component
public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    private final ConfigurationService configurationService;
    private Configuration configuration;

    @Autowired
    public ApplicationConfig(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostConstruct
    public void init() {
        // Load configuration from file on startup
        this.configuration = configurationService.loadConfiguration();
        logger.info("Configuration loaded: {}", configuration);
        
        // Set logging level based on debug flag
        if (configuration.isDebug()) {
            System.setProperty("AIGENQA_LOG_LEVEL", "info");
        } else {
            System.setProperty("AIGENQA_LOG_LEVEL", "error");
        }
    }

    public String getTargetHostname() {
        return configuration.getTargetHostname();
    }

    public void setTargetHostname(String targetHostname) {
        configuration.setTargetHostname(targetHostname);
        // Save configuration to file whenever it changes
        boolean saved = configurationService.saveConfiguration(configuration);
        if (saved) {
            logger.info("Configuration saved successfully");
        } else {
            logger.warn("Failed to save configuration");
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        configurationService.saveConfiguration(configuration);
    }
    
    public boolean isDebug() {
        return configuration.isDebug();
    }
    
    public void setDebug(boolean debug) {
        configuration.setDebug(debug);
        // Save configuration to file whenever it changes
        boolean saved = configurationService.saveConfiguration(configuration);
        
        // Set logging level based on debug flag
        if (debug) {
            System.setProperty("AIGENQA_LOG_LEVEL", "info");
        } else {
            System.setProperty("AIGENQA_LOG_LEVEL", "error");
        }
        
        if (saved) {
            logger.info("Configuration saved successfully");
        } else {
            logger.warn("Failed to save configuration");
        }
    }
}