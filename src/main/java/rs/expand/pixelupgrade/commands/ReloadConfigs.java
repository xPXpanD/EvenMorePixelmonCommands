package rs.expand.pixelupgrade.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.CommonMethods;
import rs.expand.pixelupgrade.utilities.ConfigOperations;

import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class ReloadConfigs implements CommandExecutor
{
    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printFormattedMessage("CheckEgg", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        boolean showError = false;

        if (args.<String>getOne("config").isPresent())
        {
            String configString = args.<String>getOne("config").get(), path = PixelUpgrade.path;
            String cappedConfigString = configString.toUpperCase(), commandAlias = null, commandString = null;

            // Create a config directory if it doesn't exist. Silently catch an error if it does. I/O is awkward.
            try
            {
                Files.createDirectory(Paths.get(path));
                LoggerFactory.getLogger("PixelUpgrade")
                        .info("§dCould not find a PixelUpgrade config folder. Creating it!");
            }
            catch (IOException ignored) {} // We don't need to show a message if the folder already exists.

            if (cappedConfigString.equals("ALL"))
            { ConfigOperations.initializeAndGrabAliases(false); }
            else
            {
                /*switch (cappedConfigString)
                {
                    // Special.
                    case "MAINCONFIG": case "MAIN":
                    {
                        ConfigOperations.setupPrimaryConfig(PixelUpgrade.primaryConfigPath, PixelUpgrade.primaryPath);

                        pLog.info("§aReloaded global config.");
                        break;
                    }
                    case "PIXELUPGRADEINFO": case "INFO":
                        commandAlias = PixelUpgradeInfoConfig.setupConfig(PixelUpgrade.puInfoPath,
                                path, PixelUpgrade.puInfoLoader);
                        pLog.info("§aReloaded config for the command listing (§2/pixelupgrade§a), alias §2/" + commandAlias + "§a.");
                        break;

                    // Commands.
                    case "CHECKEGG":
                    {
                        commandAlias = ConfigOperations.setupConfig("CheckEgg",
                                "egg", checkEggPath, path);

                        ConfigOperations.loadConfig("CheckEgg");

                        pLog.info("§aReloaded config for command §2/checkegg§a, alias §2/" + commandAlias + "§a.");
                        break;
                    }
                    case "CHECKSTATS":
                        commandAlias = CheckStatsConfig.setupConfig(PixelUpgrade.checkStatsPath,
                                path, PixelUpgrade.checkStatsLoader);
                        pLog.info("§aReloaded config for command §2/checkstats§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "CHECKTYPES":
                        commandAlias = CheckTypesConfig.setupConfig(PixelUpgrade.checkTypesPath,
                                path, PixelUpgrade.checkTypesLoader);
                        pLog.info("§aReloaded config for command §2/checktypes§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "DITTOFUSION":
                        commandAlias = DittoFusionConfig.setupConfig(PixelUpgrade.dittoFusionPath,
                                path, PixelUpgrade.dittoFusionLoader);
                        pLog.info("§aReloaded config for command §2/dittofusion§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FIXEVS":
                        commandAlias = FixEVsConfig.setupConfig(PixelUpgrade.fixEVsPath,
                                path, PixelUpgrade.fixEVsLoader);
                        pLog.info("§aReloaded config for command §2/fixevs§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FIXLEVEL":
                        commandAlias = FixLevelConfig.setupConfig(PixelUpgrade.fixLevelPath,
                                path, PixelUpgrade.fixLevelLoader);
                        pLog.info("§aReloaded config for command §2/fixlevel§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FORCEHATCH":
                        commandAlias = ForceHatchConfig.setupConfig(PixelUpgrade.forceHatchPath,
                                path, PixelUpgrade.forceHatchLoader);
                        pLog.info("§aReloaded config for command §2/forcehatch§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FORCESTATS":
                        commandAlias = ForceStatsConfig.setupConfig(PixelUpgrade.forceStatsPath,
                                path, PixelUpgrade.forceStatsLoader);
                        pLog.info("§aReloaded config for command §2/forcestats§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "RESETCOUNT":
                        commandAlias = ResetCountConfig.setupConfig(PixelUpgrade.resetCountPath,
                                path, PixelUpgrade.resetCountLoader);
                        pLog.info("§aReloaded config for command §2/resetcount§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "RESETEVS":
                        commandAlias = ResetEVsConfig.setupConfig(PixelUpgrade.resetEVsPath,
                                path, PixelUpgrade.resetEVsLoader);
                        pLog.info("§aReloaded config for command §2/resetevs§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "SWITCHGENDER":
                        commandAlias = SwitchGenderConfig.setupConfig(PixelUpgrade.switchGenderPath,
                                path, PixelUpgrade.switchGenderLoader);
                        pLog.info("§aReloaded config for command §2/switchgender§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "SHOWSTATS":
                        commandAlias = ShowStatsConfig.setupConfig(PixelUpgrade.showStatsPath,
                                path, PixelUpgrade.showStatsLoader);
                        pLog.info("§aReloaded config for command §2/showstats§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "UPGRADEIVS":
                        commandAlias = UpgradeIVsConfig.setupConfig(PixelUpgrade.upgradeIVsPath,
                                path, PixelUpgrade.upgradeIVsLoader);
                        pLog.info("§aReloaded config for command §2/upgradeivs§a, alias §2/" + commandAlias + "§a.");
                        break;

                    // Input did not match any of the above, abort.
                    default:
                        showError = true;
                }*/
            }
        }
        else
            showError = true;

        if (showError)
        {
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
            if (args.<String>getOne("config").isPresent())
                src.sendMessage(Text.of("§4Error: §cInvalid config provided. See below for valid configs."));
            else
                src.sendMessage(Text.of("§4Error: §cNo config provided. See below for valid configs."));
            src.sendMessage(Text.of("§4Usage: §c/pureload <config>"));
            src.sendMessage(Text.of(""));
            src.sendMessage(Text.of("§6Commands: §eCheckEgg, CheckStats, CheckTypes, DittoFusion"));
            src.sendMessage(Text.of("§6Commands: §eFixEVs, FixLevel, ForceHatch, ForceStats, Info"));
            src.sendMessage(Text.of("§6Commands: §eResetCount, ResetEVs, SwitchGender, ShowStats"));
            src.sendMessage(Text.of("§6Commands: §eUpgradeIVs"));
            src.sendMessage(Text.of("§6Other: §eAll (reloads ALL configs!), Main (reloads global config)"));
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
        else
        {
            if (src instanceof Player)
            {
                src.sendMessage(Text.of("§7-----------------------------------------------------"));
                src.sendMessage(Text.of("§3PU Reload: §bReloaded the provided config(s)!"));
                src.sendMessage(Text.of("§3PU Reload: §bPlease check the console for any errors."));
                src.sendMessage(Text.of("§7-----------------------------------------------------"));
            }
            else
                LoggerFactory.getLogger("PixelUpgrade").info("§bReloaded the provided config(s)!");
        }

        return CommandResult.success();
    }
}
