package rs.expand.pixelupgrade.configs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.nio.file.*;

public class FixEVsConfig
{
    private CommentedConfigurationNode config;
    private static FixEVsConfig instance = new FixEVsConfig();
    public static FixEVsConfig getInstance()
    {   return instance;    }

    private String separator = FileSystems.getDefault().getSeparator();
    private String path = "config" + separator + "PixelUpgrade" + separator;

    // Called during initial setup, either when the server is booting up or when /pixelupgrade reload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                PixelUpgrade.log.info("\u00A7eNo \"/fixevs\" configuration file found, creating...");
                Path targetLocation = Paths.get(path, "FixEVs.conf");
                // Fetching files from the .jar is tough! But this will survive Github, at least.
                Files.copy(getClass().getResourceAsStream("/assets/FixEVs.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("\u00A74Error during initial setup of config for command \"/fixevs\"!");
                PixelUpgrade.log.info("\u00A7cPlease report this, along with any useful info you may have (operating system?). Stack trace follows:");
                F.printStackTrace();
            }

            return "fixevs";
        }
        else
        {
            try
            {
                config = configLoader.load();
                String alias = getConfig().getNode("commandAlias").getString();
                PixelUpgrade.log.info("\u00A7aLoaded existing config for command \"/fixevs\", alias \"" + alias + "\"");
                return alias;
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("\u00A7cError during config loading for command \"/fixevs\"!");
                PixelUpgrade.log.info("\u00A7cPlease make sure this config is formatted correctly. Stack trace follows:");
                F.printStackTrace();
                return "fixevs";
            }
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}