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

public class SwitchGenderConfig
{
    private CommentedConfigurationNode config;
    private static SwitchGenderConfig instance = new SwitchGenderConfig();
    public static SwitchGenderConfig getInstance()
    {   return instance;    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String fallbackAlias = "switchgender";

        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(1, "switchgender", "");
                Path targetLocation = Paths.get(PixelUpgrade.getInstance().path, "SwitchGender.conf");
                Files.copy(getClass().getResourceAsStream("/assets/SwitchGender.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, "SwitchGender", "");
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
                printMessages(3, "switchgender", alias);
                return alias;
            }
            else
            {
                printMessages(4, "SwitchGender", "");
                return fallbackAlias;
            }
        }
        catch (IOException F)
        {
            printMessages(5, "switchgender", "");
            F.printStackTrace();
            return fallbackAlias;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}
