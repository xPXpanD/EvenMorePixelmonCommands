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

public class ForceHatchConfig
{
    private CommentedConfigurationNode config;
    private static ForceHatchConfig instance = new ForceHatchConfig();
    public static ForceHatchConfig getInstance()
    {   return instance;    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String fallbackAlias = "fhatch";

        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(1, "forcehatch", "");
                Path targetLocation = Paths.get(PixelUpgrade.getInstance().path, "ForceHatch.conf");
                Files.copy(getClass().getResourceAsStream("/assets/ForceHatch.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, "ForceHatch", "");
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
                printMessages(3, "forcehatch", alias);
                return alias;
            }
            else
            {
                printMessages(4, "ForceHatch", "");
                return fallbackAlias;
            }
        }
        catch (IOException F)
        {
            printMessages(5, "forcehatch", "");
            F.printStackTrace();
            return fallbackAlias;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}
