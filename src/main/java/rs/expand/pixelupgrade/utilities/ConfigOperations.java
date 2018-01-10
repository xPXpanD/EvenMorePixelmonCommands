package rs.expand.pixelupgrade.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.commands.CheckEgg;

public class ConfigOperations
{
    private CommentedConfigurationNode config;
    private static ConfigOperations instance = new ConfigOperations();
    public static ConfigOperations getInstance()
    { return instance; }

    // Set up a nice compact private logger specifically for showing command loading.
    private static final String pName = "PU";
    private static final Logger pLog = LoggerFactory.getLogger(pName);

    // Make a little converter for safely handling possibly null Strings that have an integer value inside.
    private static Integer interpretInteger(String input)
    {
        if (input != null && input.matches("^[0-9]\\d*$"))
            return Integer.parseInt(input);
        else
            return null;
    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String setupConfig(String callSource, String defaultAlias, Path checkPath, String mainPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                pLog.info("§eNo \"§6/" + callSource.toLowerCase() + "§e\" configuration file found, creating...");

                Files.copy(getClass().getResourceAsStream("/assets/" + callSource + ".conf"),
                        Paths.get(mainPath, callSource + ".conf"));

                config = configLoader.load();
            }
            catch (IOException F)
            {
                pLog.info("§cInitial \"§4/" + callSource.toLowerCase()
                        + "§c\" config setup failed! Please report this.");
                pLog.info("§cAdd any useful info you may have (operating system?). Stack trace:");
                F.printStackTrace();
            }

            return defaultAlias;
        }
        else try
        {
            config = configLoader.load();
            String alias = getConfig().getNode("commandAlias").getString();

            if (!Objects.equals(alias, null))
                return alias;
            else
            {
                pLog.info("§cError on \"§4/" + callSource.toLowerCase() +
                        "§c\", variable \"§4commandAlias§c\"! Check/regen this config!");
                return null;
            }
        }
        catch (IOException F)
        {
            pLog.info("§cTried loading \"§4/" + callSource.toLowerCase() +
                    "§c\" config but ran into an unknown error!");
            pLog.info("§cPlease make sure this config is formatted correctly. Stack trace:");
            F.printStackTrace();
            return null;
        }
    }

    // An overloaded hardcoded alternative for use with the main config.
    public void setupConfig(Path checkPath, String mainPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                pLog.info("§eNo primary configuration file found, creating...");

                Files.copy(getClass().getResourceAsStream("/assets/PixelUpgradeMain.conf"),
                        Paths.get(mainPath, "PixelUpgrade.conf"));

                config = configLoader.load();
            }
            catch (IOException F)
            {
                pLog.info("§cInitial primary config setup has failed! Please report this.");
                pLog.info("§cAdd any useful info you may have (operating system?). Stack trace:");

                F.printStackTrace();
            }
        }
        else try
        { config = configLoader.load(); }
        catch (IOException F)
        {
            pLog.info("§cTried loading the main config but ran into an unknown error!");
            pLog.info("§cPlease make sure this config is formatted correctly. Stack trace:");
            F.printStackTrace();
        }
    }

    // Grabs a specified config, then loads all of the variables into the matching command.
    @SuppressWarnings({"ConstantConditions"})
    public void loadConfig(String callSource)
    {
        PixelUpgrade.log.info("§4PixelUpgrade // DEBUG: §cReading: §4" + callSource);
        CommentedConfigurationNode commandConfig;

        try
        {
            switch (callSource) // TODO: Added a new command? Update the switch list! Default should NEVER be called!
            {
                case "CheckEgg":
                {
                    commandConfig = PixelUpgrade.checkEggLoader.load();

                    CheckEgg.commandAlias = commandConfig.getNode("commandAlias").getString();
                    CheckEgg.showName = BooleanUtils.toBooleanObject(commandConfig.getNode("showName").getString());
                    CheckEgg.explicitReveal = BooleanUtils.toBooleanObject(commandConfig.getNode("explicitReveal").getString());
                    CheckEgg.babyHintPercentage = interpretInteger(commandConfig.getNode("babyHintPercentage").getString());
                    CheckEgg.commandCost = interpretInteger(commandConfig.getNode("commandCost").getString());
                    CheckEgg.recheckIsFree = BooleanUtils.toBooleanObject(commandConfig.getNode("recheckIsFree").getString());

                    break;
                }
                case "CheckStats": commandConfig = PixelUpgrade.checkStatsLoader.load(); break;
                case "CheckTypes": commandConfig = PixelUpgrade.checkTypesLoader.load(); break;
                case "DittoFusion": commandConfig = PixelUpgrade.dittoFusionLoader.load(); break;
                case "FixEVs": commandConfig = PixelUpgrade.fixEVsLoader.load(); break;
                case "FixLevel": commandConfig = PixelUpgrade.fixLevelLoader.load(); break;
                case "ForceHatch": commandConfig = PixelUpgrade.forceHatchLoader.load(); break;
                case "ForceStats": commandConfig = PixelUpgrade.forceStatsLoader.load(); break;
                case "PixelUpgradeInfo": commandConfig = PixelUpgrade.puInfoLoader.load(); break;
                case "PixelUpgrade":
                {
                    commandConfig = PixelUpgrade.primaryConfigLoader.load();

                    PixelUpgrade.configVersion = interpretInteger(commandConfig.getNode("configVersion").getString());
                    PixelUpgrade.debugLevel = interpretInteger(commandConfig.getNode("debugVerbosityMode").getString());
                    PixelUpgrade.useBritishSpelling = BooleanUtils.toBooleanObject(commandConfig.getNode("useBritishSpelling").getString());
                    PixelUpgrade.shortenedHP = commandConfig.getNode("shortenedHealth").getString();
                    PixelUpgrade.shortenedAttack = commandConfig.getNode("shortenedAttack").getString();
                    PixelUpgrade.shortenedDefense = commandConfig.getNode("shortenedDefense").getString();
                    PixelUpgrade.shortenedSpAtt = commandConfig.getNode("shortenedSpecialAttack").getString();
                    PixelUpgrade.shortenedSpDef = commandConfig.getNode("shortenedSpecialDefense").getString();
                    PixelUpgrade.shortenedSpeed = commandConfig.getNode("shortenedSpeed").getString();

                    break;
                }
                case "ResetCount": commandConfig = PixelUpgrade.resetCountLoader.load(); break;
                case "ResetEVs": commandConfig = PixelUpgrade.resetEVsLoader.load(); break;
                case "ShowStats": commandConfig = PixelUpgrade.showStatsLoader.load(); break;
                case "SwitchGender": commandConfig = PixelUpgrade.switchGenderLoader.load(); break;
                case "UpgradeIVs": commandConfig = PixelUpgrade.upgradeIVsLoader.load(); break;
                default:
                {
                    PixelUpgrade.log.info("§4PixelUpgrade // critical: §cConfig gathering failed; fell through the switch.");
                    PixelUpgrade.log.info("§4PixelUpgrade // critical: §cPlease report -- this is a bug. Source: §4" + callSource);
                }
            }
        }
        catch (Exception F)
        { pLog.info("§4" + callSource + " // error: §cIssue!"); }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;   }
}
