package rs.expand.pixelupgrade.configs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class CheckStatsConfig
{
    private CommentedConfigurationNode config;
    private static CheckStatsConfig instance = new CheckStatsConfig();
    public static CheckStatsConfig getInstance()
    {   return instance;    }

    private String separator = FileSystems.getDefault().getSeparator();
    private String path = "config" + separator + "PixelUpgrade" + separator;

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                PixelUpgrade.log.info("\u00A7eNo \"/checkstats\" configuration file found, creating...");
                Path targetLocation = Paths.get(path, "CheckStats.conf");
                // Fetching files from the .jar is tough! But this will survive Github, at least.
                Files.copy(getClass().getResourceAsStream("/assets/CheckStats.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("\u00A74Error during initial setup of config for command \"/checkstats\"!");
                PixelUpgrade.log.info("\u00A7cPlease report this, along with any useful info you may have (operating system?). Stack trace follows:");
                F.printStackTrace();
            }

            return "gs";
        }
        else
        {
            try
            {
                config = configLoader.load();
                String alias = getConfig().getNode("commandAlias").getString();

                if (!Objects.equals(alias, null))
                {
                    PixelUpgrade.log.info("\u00A7aLoaded existing config for command \"/checkstats\", alias \"" + alias + "\"");
                    return alias;
                }
                else
                {
                    PixelUpgrade.log.info("\u00A7cCheckStats: Could not read command variable \u00A74\"commandAlias\"\u00A7c, setting defaults.");
                    PixelUpgrade.log.info("\u00A7cCheckStats: Check this command's config, or wipe it and \u00A74/pureload\u00A7c.");
                    return "gs";
                }
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("\u00A7cError during config loading for command \"/checkstats\"!");
                PixelUpgrade.log.info("\u00A7cPlease make sure this config is formatted correctly. Stack trace follows:");
                F.printStackTrace();
                return "gs";
            }
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}