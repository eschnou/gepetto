package sh.gepetto.app.service;

import sh.gepetto.app.config.Constants;
import sh.gepetto.app.model.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service to manage configuration file operations
 */
@Service
public class ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private final ObjectMapper yamlMapper;
    private Path configFilePath;

    public ConfigurationService() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.configFilePath = getConfigFilePath();
    }

    /**
     * Check if configuration file exists
     *
     * @return true if configuration file exists, false otherwise
     */
    public boolean configurationExists() {
        Path configPath = Paths.get(Constants.PROJECT_DIR, Constants.CONFIG_FILE);
        if (Files.exists(configPath)) {
            configFilePath = configPath;
            return true;
        }
        
        return false;
    }

    /**
     * Load configuration from file
     *
     * @return the loaded configuration or a new default configuration if file doesn't exist
     */
    public Configuration loadConfiguration() {
        try {
            if (configurationExists()) {
                logger.info("Loading configuration from {}", configFilePath);
                return yamlMapper.readValue(configFilePath.toFile(), Configuration.class);
            } else {
                logger.info("Configuration file doesn't exist, creating default configuration");
                return new Configuration();
            }
        } catch (IOException e) {
            logger.error("Error loading configuration: {}", e.getMessage(), e);
            return new Configuration();
        }
    }

    /**
     * Save configuration to file
     *
     * @param configuration the configuration to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveConfiguration(Configuration configuration) {
        try {
            ensureConfigDirectoryExists();
            logger.info("Saving configuration to {}", configFilePath);
            yamlMapper.writeValue(configFilePath.toFile(), configuration);
            return true;
        } catch (IOException e) {
            logger.error("Error saving configuration: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get the configuration file path
     */
    private Path getConfigFilePath() {
        return Paths.get(Constants.PROJECT_DIR, Constants.CONFIG_FILE);
    }

    /**
     * Ensure the configuration directory exists
     */
    private void ensureConfigDirectoryExists() throws IOException {
        Path configDir = configFilePath.getParent();
        if (!Files.exists(configDir)) {
            logger.info("Creating configuration directory: {}", configDir);
            Files.createDirectories(configDir);
        }
    }
}