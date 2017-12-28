// This is the loader for the main PixelUpgrade config. It includes some globally-used variables.
package rs.expand.pixelupgrade.configs;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.expand.pixelupgrade.PixelUpgrade;

public class PixelUpgradeMainConfig
{
    private CommentedConfigurationNode config;
    private static PixelUpgradeMainConfig instance = new PixelUpgradeMainConfig();
    public static PixelUpgradeMainConfig getInstance()
    {   return instance;   }

    // Register a copy of the logger used in other config loaders, as we can't use that one here.
    private static final String pName = "PU";
    private static final Logger pLog = LoggerFactory.getLogger(pName);

    // Set up a special path for us to dump the main config into.
    private String separator = FileSystems.getDefault().getSeparator();
    public String path = "config" + separator;

    // If necessary, create an internal config for plugin-wide stuff like British/American spelling options.
    public void setupConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                pLog.info("§eNo primary configuration file found, creating and populating...");
                Files.copy(getClass().getResourceAsStream("/assets/PixelUpgradeMain.conf"), Paths.get(path, "PixelUpgrade.conf"));
                config = configLoader.load();
            }
            catch (Exception F)
            {
                pLog.info("§4Error during loading of primary PixelUpgrade config!");
                pLog.info("§cPlease report this, along with any useful info you may have (operating system?). Stack trace follows:");
                F.printStackTrace();
            }
        }
        else try
        {
            config = configLoader.load();
        }
        catch (Exception F)
        {
            pLog.info("§cError during loading of primary PixelUpgrade config!");
            pLog.info("§cPlease make sure this config is formatted correctly. Stack trace follows:");
            F.printStackTrace();
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;   }
}
