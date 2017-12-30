package rs.expand.pixelupgrade.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import rs.expand.pixelupgrade.PixelUpgrade;

import static rs.expand.pixelupgrade.utilities.UtilityFunctions.checkAlias;
import static rs.expand.pixelupgrade.utilities.UtilityFunctions.printMessages;

public class ConfigOperations
{
    private CommentedConfigurationNode config;
    private static ConfigOperations instance = new ConfigOperations();
    public static ConfigOperations getInstance()
    {   return instance;   }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String setupConfig(String callingCommand, String defaultAlias, Path checkPath, String mainPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(1, callingCommand.toLowerCase());
                if (callingCommand.equals("PixelUpgrade"))
                    callingCommand = "PixelUpgradeMain";

                Files.copy(getClass().getResourceAsStream("/assets/" + callingCommand + ".conf"),
                        Paths.get(mainPath, callingCommand + ".conf"));
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, callingCommand);
                F.printStackTrace();
            }

            return defaultAlias;
        }
        else try
        {
            config = configLoader.load();
            return checkAlias(getConfig().getNode("commandAlias").getString(), callingCommand);
        }
        catch (IOException F)
        {
            printMessages(3, callingCommand);
            F.printStackTrace();
            return null;
        }
    }

    public static String getConfigValue(String callingCommand, String node)
    {
        CommentedConfigurationNode commandConfig;
        String returnString = null;

        try
        {
            switch (callingCommand)
            {
                case "CheckEgg":
                {
                    commandConfig = PixelUpgrade.checkEggLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "CheckStats":
                {
                    commandConfig = PixelUpgrade.checkStatsLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "CheckTypes":
                {
                    commandConfig = PixelUpgrade.checkTypesLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "DittoFusion":
                {
                    commandConfig = PixelUpgrade.dittoFusionLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "FixEVs":
                {
                    commandConfig = PixelUpgrade.fixEVsLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "FixLevel":
                {
                    commandConfig = PixelUpgrade.fixLevelLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "ForceHatch":
                {
                    commandConfig = PixelUpgrade.forceHatchLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "ForceStats":
                {
                    commandConfig = PixelUpgrade.forceStatsLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "PixelUpgradeInfo":
                {
                    commandConfig = PixelUpgrade.puInfoLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "PixelUpgrade":
                {
                    commandConfig = PixelUpgrade.primaryConfigLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "ResetCount":
                {
                    commandConfig = PixelUpgrade.resetCountLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "ResetEVs":
                {
                    commandConfig = PixelUpgrade.resetEVsLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "ShowStats":
                {
                    commandConfig = PixelUpgrade.showStatsLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "SwitchGender":
                {
                    commandConfig = PixelUpgrade.switchGenderLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                case "UpgradeIVs":
                {
                    commandConfig = PixelUpgrade.upgradeIVsLoader.load();
                    returnString = commandConfig.getNode(node).getString();
                }
                default:
                {
                    PixelUpgrade.log.info("§4Yo, there's a config type missing and we're hitting the default. Fix it.");
                }
            }
        }
        catch (IOException F)
        {
            PixelUpgrade.log.info("§4" + callingCommand + " // critical: §cConfig variable \"" + node + "\" could not be found!");
        }

        PixelUpgrade.log.info("§4" + callingCommand + ": §cExiting configuration check.");
        return returnString;
    }

    public CommentedConfigurationNode getConfig()
    {   return config;   }
}
