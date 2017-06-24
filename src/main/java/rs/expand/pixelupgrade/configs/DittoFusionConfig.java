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

public class DittoFusionConfig
{
    private CommentedConfigurationNode config;
    private static DittoFusionConfig instance = new DittoFusionConfig();
    public static DittoFusionConfig getInstance()
    {   return instance;    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String fallbackAlias = "fuse";

        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(1, "dittofusion", "");
                Path targetLocation = Paths.get(PixelUpgrade.getInstance().path, "DittoFusion.conf");
                Files.copy(getClass().getResourceAsStream("/assets/DittoFusion.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, "DittoFusion", "");
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
                printMessages(3, "dittofusion", alias);
                return alias;
            }
            else
            {
                printMessages(4, "DittoFusion", "");
                return fallbackAlias;
            }
        }
        catch (IOException F)
        {
            printMessages(5, "dittofusion", "");
            F.printStackTrace();
            return fallbackAlias;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}
