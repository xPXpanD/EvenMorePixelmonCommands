package rs.expand.pixelupgrade.configs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// This is the loader for the main PixelUpgrade config. It includes some globally-used variables.

public class PixelUpgradeMainConfig
{
    private CommentedConfigurationNode config;
    private static PixelUpgradeMainConfig instance = new PixelUpgradeMainConfig();
    public static PixelUpgradeMainConfig getInstance()
    {   return instance;    }

    private String separator = FileSystems.getDefault().getSeparator();
    private String path = "config" + separator;

    // If necessary, create an internal config for plugin-wide stuff like British/American spelling options.
    public void loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                PixelUpgrade.log.info("§eNo primary configuration file found, creating and populating...");
                Path targetLocation = Paths.get(path, "PixelUpgrade.conf");
                Files.copy(getClass().getResourceAsStream("/assets/PixelUpgradeMain.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("§4Error during loading of primary PixelUpgrade config!");
                PixelUpgrade.log.info("§cPlease report this, along with any useful info you may have (operating system?). Stack trace follows:");
                F.printStackTrace();
            }
        }
        else
        {
            try
            {
                config = configLoader.load();
                PixelUpgrade.log.info("§aLoaded primary PixelUpgrade config and set up global variables.");
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("§cError during loading of primary PixelUpgrade config!");
                PixelUpgrade.log.info("§cPlease make sure this config is formatted correctly. Stack trace follows:");
                F.printStackTrace();
            }
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}