package rs.expand.pixelupgrade.configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import static rs.expand.pixelupgrade.utilities.UtilityFunctions.checkAlias;
import static rs.expand.pixelupgrade.utilities.UtilityFunctions.printMessages;

public class CheckEggConfig
{
    private CommentedConfigurationNode config;
    private static CheckEggConfig instance = new CheckEggConfig();
    public static CheckEggConfig getInstance()
    {   return instance;   }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String setupConfig(Path checkPath, String mainPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String command = "checkegg";

        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(1, command);
                Files.copy(getClass().getResourceAsStream("/assets/CheckEgg.conf"), Paths.get(mainPath, "CheckEgg.conf"));
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, command);
                F.printStackTrace();
            }
            return "egg"; // Load up and register the default alias.
        }
        else try
        {
            config = configLoader.load();
            return checkAlias(getConfig().getNode("commandAlias").getString(), command);
        }
        catch (IOException F)
        {
            printMessages(3, command);
            F.printStackTrace();
            return null;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;   }
}
