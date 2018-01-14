package rs.expand.pixelupgrade.commands;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import static rs.expand.pixelupgrade.utilities.ConfigOperations.*;
import static rs.expand.pixelupgrade.PixelUpgrade.*;
import static rs.expand.pixelupgrade.utilities.CommonMethods.*;

public class ReloadConfigs implements CommandExecutor
{
    // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
    private void checkConfigDir()
    {
        try
        {
            Files.createDirectory(Paths.get(path));
            printUnformattedMessage("--> §aPixelUpgrade folder not found, creating a new one for command configs...");
        }
        catch (IOException ignored) {}
    }

    private void printLineAndAddition(CommandSource src, boolean loadAll)
    {
        printUnformattedMessage("===========================================================================");
        if (src instanceof Player)
            printUnformattedMessage("--> §aPixelUpgrade config reload called by player §2" + src.getName() + "§a, starting.");

        if (loadAll)
            printUnformattedMessage("--> §aStarting a (re-)load of all configuration files...");
        else
            printUnformattedMessage("--> §aStarting a command-specific reload...");
    }

    // Please note:
    // For convenience, we use static imports for everything from PixelUpgrade, CommonMethods and ConfigOperations.
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        boolean showError = false;

        if (args.<String>getOne("config").isPresent())
        {
            String configString = args.<String>getOne("config").get();
            String cappedConfigString = configString.toUpperCase();

            if (cappedConfigString.equals("ALL"))
            {
                printLineAndAddition(src, true);
                checkConfigDir();
                initializeAndGrabAliases(false);
                printUnformattedMessage("--> §aLoaded command settings. All done!");
            }
            else
            {
                checkConfigDir();
                String returnString = "ERROR";

                switch (cappedConfigString)
                {
                    // Special.
                    case "MAINCONFIG": case "MAIN":
                    {
                        printLineAndAddition(src, false);
                        setupPrimaryConfig(PixelUpgrade.primaryConfigPath, PixelUpgrade.primaryPath);
                        returnString = "--> §aLoaded global config.";
                        break;
                    }

                    // Commands.
                    case "CHECKEGG":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("CheckEgg", "egg", checkEggPath, path);
                        returnString = "--> §aLoaded config for command §2/checkegg§a, alias §2/" + CheckEgg.commandAlias + "§a.";
                        break;
                    }
                    case "CHECKSTATS":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("CheckStats", "cs", checkStatsPath, path);
                        returnString = "--> §aLoaded config for command §2/checkstats§a, alias §2/" + CheckStats.commandAlias + "§a.";
                        break;
                    }
                    case "CHECKTYPES":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("CheckTypes", "type", checkTypesPath, path);
                        returnString = "--> §aLoaded config for command §2/checktypes§a, alias §2/" + CheckTypes.commandAlias + "§a.";
                        break;
                    }
                    case "DITTOFUSION":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("DittoFusion", "fuse", dittoFusionPath, path);
                        returnString = "--> §aLoaded config for command §2/dittofusion§a, alias §2/" + DittoFusion.commandAlias + "§a.";
                        break;
                    }
                    case "FIXEVS":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("FixEVs", "fixevs", fixEVsPath, path);
                        returnString = "--> §aLoaded config for command §2/fixevs§a, alias §2/" + FixEVs.commandAlias + "§a.";
                        break;
                    }
                    case "FIXLEVEL":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("FixLevel", "fixlevel", fixLevelPath, path);
                        returnString = "--> §aLoaded config for command §2/fixlevel§a, alias §2/" + FixLevel.commandAlias + "§a.";
                        break;
                    }
                    case "FORCEHATCH":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("ForceHatch", "fhatch", forceHatchPath, path);
                        returnString = "--> §aLoaded config for command §2/forcehatch§a, alias §2/" + ForceHatch.commandAlias + "§a.";
                        break;
                    }
                    case "FORCESTATS":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("ForceStats", "fstats", forceStatsPath, path);
                        returnString = "--> §aLoaded config for command §2/forcestats§a, alias §2/" + ForceStats.commandAlias + "§a.";
                        break;
                    }
                    case "PIXELUPGRADEINFO": case "INFO":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("CheckEgg", "egg", puInfoPath, path);
                        returnString = "--> §aLoaded config for the command listing (§2/pixelupgrade§a), alias §2/" +
                                PixelUpgradeInfo.commandAlias + "§a.";
                        break;
                    }
                    case "RESETCOUNT":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("ResetCount", "delcount", resetCountPath, path);
                        returnString = "--> §aLoaded config for command §2/resetcount§a, alias §2/" + ResetCount.commandAlias + "§a.";
                        break;
                    }
                    case "RESETEVS":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("ResetEVs", "delevs", resetEVsPath, path);
                        returnString = "--> §aLoaded config for command §2/resetevs§a, alias §2/" + ResetEVs.commandAlias + "§a.";
                        break;
                    }
                    case "SHOWSTATS":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("ShowStats", "show", showStatsPath, path);
                        returnString = "--> §aLoaded config for command §2/showstats§a, alias §2/" + ShowStats.commandAlias + "§a.";
                        break;
                    }
                    case "SWITCHGENDER":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("SwitchGender", "bend", switchGenderPath, path);
                        returnString = "--> §aLoaded config for command §2/switchgender§a, alias §2/" + SwitchGender.commandAlias + "§a.";
                        break;
                    }
                    case "UPGRADEIVS":
                    {
                        printLineAndAddition(src, false);
                        setupConfig("UpgradeIVs", "upgrade", upgradeIVsPath, path);
                        returnString = "--> §aLoaded config for command §2/upgradeivs§a, alias §2/" + UpgradeIVs.commandAlias + "§a.";
                        break;
                    }

                    // Input did not match any of the above, abort.
                    default:
                        showError = true;
                }

                if (!showError)
                    printUnformattedMessage(returnString);
            }
        }
        else
            showError = true;

        if (showError)
        {
            if (src instanceof Player)
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
                printUnformattedMessage("===========================================================================");

                if (args.<String>getOne("config").isPresent())
                    src.sendMessage(Text.of("§cInvalid config provided. See below for valid configs."));
                else
                    src.sendMessage(Text.of("§cNo config provided. See below for valid configs."));

                src.sendMessage(Text.of("§4Usage: §c/pureload <config>"));
                src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("§6Commands: §eCheckEgg, CheckStats, CheckTypes, DittoFusion"));
                src.sendMessage(Text.of("§6Commands: §eFixEVs, FixLevel, ForceHatch, ForceStats, Info"));
                src.sendMessage(Text.of("§6Commands: §eResetCount, ResetEVs, SwitchGender, ShowStats"));
                src.sendMessage(Text.of("§6Commands: §eUpgradeIVs"));
                src.sendMessage(Text.of("§6Other: §eAll (reloads ALL configs), Main (reloads global config)"));

                printUnformattedMessage("===========================================================================");
            }
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

            printUnformattedMessage("===========================================================================");
        }

        return CommandResult.success();
    }
}
