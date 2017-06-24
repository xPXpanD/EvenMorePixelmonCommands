package rs.expand.pixelupgrade.configs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import rs.expand.pixelupgrade.PixelUpgrade;

import static rs.expand.pixelupgrade.utilities.ConfigOperations.printMessages;

public class PixelUpgradeInfoConfig
{
    private CommentedConfigurationNode config;
    private static PixelUpgradeInfoConfig instance = new PixelUpgradeInfoConfig();
    public static PixelUpgradeInfoConfig getInstance()
    {   return instance;    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String fallbackAlias = "pu";

        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(11, "pixelupgrade (info)", ""); // Special number in check class.
                Path targetLocation = Paths.get(PixelUpgrade.getInstance().path, "PixelUpgradeInfo.conf");
                Files.copy(getClass().getResourceAsStream("/assets/PixelUpgradeInfo.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, "PU Info", "");
                F.printStackTrace();
            }

            return fallbackAlias;
        }
        else try
        {
            config = configLoader.load();
            String alias = getConfig().getNode("commandAlias").getString();

            if (!Objects.equals(alias, null))
            {
                printMessages(33, "pixelupgrade (info)", alias); // Special number in check class.
                return alias;
            }
            else
            {
                printMessages(4, "PU Info", "");
                return fallbackAlias;
            }
        }
        catch (IOException F)
        {
            printMessages(55, "pixelupgrade (info)", ""); // Special number in check class.
            F.printStackTrace();
            return fallbackAlias;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}
