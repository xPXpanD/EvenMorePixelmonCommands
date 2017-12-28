package rs.expand.pixelupgrade.configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import static rs.expand.pixelupgrade.utilities.UtilityFunctions.checkAlias;
import static rs.expand.pixelupgrade.utilities.UtilityFunctions.printInfoMessages;

public class PixelUpgradeInfoConfig
{
    private CommentedConfigurationNode config;
    private static PixelUpgradeInfoConfig instance = new PixelUpgradeInfoConfig();
    public static PixelUpgradeInfoConfig getInstance()
    {   return instance;    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String setupConfig(Path checkPath, String mainPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String command = "pixelupgrade (info)";

        if (Files.notExists(checkPath))
        {
            try
            {
                printInfoMessages(1);
                Files.copy(getClass().getResourceAsStream("/assets/PixelUpgradeInfo.conf"),
                        Paths.get(mainPath, "PixelUpgradeInfo.conf"));
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printInfoMessages(2);
                F.printStackTrace();
            }
            return "pu"; // Load up and register the default alias.
        }
        else try
        {
            config = configLoader.load();
            return checkAlias(getConfig().getNode("commandAlias").getString(), command);
        }
        catch (IOException F)
        {
            printInfoMessages(3);
            F.printStackTrace();
            return null;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}
