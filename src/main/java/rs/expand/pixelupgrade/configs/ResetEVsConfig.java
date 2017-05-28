package rs.expand.pixelupgrade.configs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResetEVsConfig
{
    private CommentedConfigurationNode config;
    private static ResetEVsConfig instance = new ResetEVsConfig();
    public static ResetEVsConfig getInstance()
    {   return instance;    }

    private String path = "config" + FileSystems.getDefault().getSeparator() + "PixelUpgrade";
    private Path configPath = Paths.get(path, "ResetEVs.conf");

    // Called during initial setup, either when the server is booting up or when /pu reload has been executed.
    public void loadOrCreateConfig(Path configPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        this.configPath = configPath;

        if (Files.notExists(configPath))
        {
            try
            {
                PixelUpgrade.log.info("\u00A7eNo \"/resetevs\" configuration file found, creating...");
                Asset asset = Sponge.getAssetManager().getAsset(PixelUpgrade.getInstance(), "ResetEVs.conf").get();
                asset.copyToFile(configPath);
                config = configLoader.load();
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("\u00A74Error during initial setup of config for command \"/resetevs\"!");
                PixelUpgrade.log.info("\u00A7cPlease report this, along with any useful info you may have (operating system?). Stack trace follows:");
                F.printStackTrace();
            }
        }
        else
        {
            PixelUpgrade.log.info("\u00A7aLoading existing config for command \"/resetevs\"!");
            try
            {
                config = configLoader.load();
            }
            catch (Exception F)
            {
                PixelUpgrade.log.info("\u00A74Error during config loading for command \"/resetevs\"!");
                PixelUpgrade.log.info("\u00A7cPlease make sure this config is formatted correctly. Stack trace follows:");
                F.printStackTrace();
            }
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}