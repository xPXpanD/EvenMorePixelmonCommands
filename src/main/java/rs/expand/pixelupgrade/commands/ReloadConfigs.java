// The one and only. Accept no imitations.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.ConfigMethods;
import static rs.expand.pixelupgrade.utilities.PrintingMethods.printBasicMessage;

// Note: printBasicMessage is a static import for a method from PrintingMethods, for convenience.
public class ReloadConfigs implements CommandExecutor
{
    // Formats the first of the messages shown to the player, and loads a config while at it.
    private void printHeaderAndCheckFolder(final CommandSource src, final boolean loadingEverything)
    {
        printBasicMessage("========================= P I X E L U P G R A D E =========================");
        if (src instanceof Player)
            printBasicMessage("--> §aPixelUpgrade config reload called by player §2" + src.getName() + "§a.");

        ConfigMethods.checkConfigDir();

        if (loadingEverything)
            printBasicMessage("--> §aStarting a (re-)load of all configuration files...");
        else
            printBasicMessage("--> §aStarting a command-specific reload...");
    }

    private boolean reloadMappings()
    {
        final Game game = Sponge.getGame();
        final PluginContainer puContainer = Sponge.getPluginManager().getPlugin("pixelupgrade").orElse(null);

        if (puContainer != null)
        {
            game.getCommandManager().getOwnedBy(puContainer).forEach(game.getCommandManager()::removeMapping);
            return ConfigMethods.registerCommands();
        }
        else return false;
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        boolean gotConfigError = false, successfulInit = true;

        if (args.<String>getOne("config").isPresent())
        {
            final String configString = args.<String>getOne("config").get();
            final String cappedConfigString = configString.toUpperCase();

            if (cappedConfigString.equals("ALL"))
            {
                printHeaderAndCheckFolder(src, true);
                ConfigMethods.loadConfig("PixelUpgrade");
                ConfigMethods.loadAllCommandConfigs();
                ConfigMethods.printCommandsAndAliases();
                printBasicMessage("--> §aRe-registering commands and known aliases with Sponge...");
                successfulInit = reloadMappings();

                if (successfulInit)
                    printBasicMessage("--> §aLoaded command settings. All done!");
            }
            else
            {
                String returnString = "ERROR", oldAlias = null, newAlias = null;

                switch (cappedConfigString)
                {
                    // Special.
                    case "MAINCONFIG": case "MAIN":
                    {
                        printHeaderAndCheckFolder(src, false);
                        ConfigMethods.loadConfig("PixelUpgrade");
                        returnString = "--> §aLoaded global config.";
                        break;
                    }

                    // Commands.
                    case "CHECKEGG":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = CheckEgg.commandAlias;
                        newAlias = ConfigMethods.loadConfig("CheckEgg");
                        returnString = "--> §aLoaded config for command §2/checkegg§a, alias §2/" + CheckEgg.commandAlias + "§a.";
                        break;
                    }
                    case "CHECKSTATS":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = CheckStats.commandAlias;
                        newAlias = ConfigMethods.loadConfig("CheckStats");
                        returnString = "--> §aLoaded config for command §2/checkstats§a, alias §2/" + CheckStats.commandAlias + "§a.";
                        break;
                    }
                    case "CHECKTYPES":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = CheckTypes.commandAlias;
                        newAlias = ConfigMethods.loadConfig("CheckTypes");
                        returnString = "--> §aLoaded config for command §2/checktypes§a, alias §2/" + CheckTypes.commandAlias + "§a.";
                        break;
                    }
                    /*case "DITTOFUSION":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = DittoFusion.commandAlias;
                        newAlias = ConfigMethods.loadConfig("DittoFusion");
                        returnString = "--> §aLoaded config for command §2/dittofusion§a, alias §2/" + DittoFusion.commandAlias + "§a.";
                        break;
                    }*/
                    case "FIXGENDERS":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = FixGenders.commandAlias;
                        newAlias = ConfigMethods.loadConfig("FixGenders");
                        returnString = "--> §aLoaded config for command §2/fixgenders§a, alias §2/" + FixGenders.commandAlias + "§a.";
                        break;
                    }
                    case "FORCEHATCH":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = ForceHatch.commandAlias;
                        newAlias = ConfigMethods.loadConfig("ForceHatch");
                        returnString = "--> §aLoaded config for command §2/forcehatch§a, alias §2/" + ForceHatch.commandAlias + "§a.";
                        break;
                    }
                    case "FORCESTATS":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = ForceStats.commandAlias;
                        newAlias = ConfigMethods.loadConfig("ForceStats");
                        returnString = "--> §aLoaded config for command §2/forcestats§a, alias §2/" + ForceStats.commandAlias + "§a.";
                        break;
                    }
                    case "PIXELUPGRADEINFO": case "INFO":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = PixelUpgradeInfo.commandAlias;
                        newAlias = ConfigMethods.loadConfig("PixelUpgradeInfo");
                        returnString = "--> §aLoaded config for the command listing (§2/pixelupgrade§a), alias §2/" +
                                PixelUpgradeInfo.commandAlias + "§a.";
                        break;
                    }
                    case "RESETCOUNT":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = ResetCount.commandAlias;
                        newAlias = ConfigMethods.loadConfig("ResetCount");
                        returnString = "--> §aLoaded config for command §2/resetcount§a, alias §2/" + ResetCount.commandAlias + "§a.";
                        break;
                    }
                    case "RESETEVS":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = ResetEVs.commandAlias;
                        newAlias = ConfigMethods.loadConfig("ResetEVs");
                        returnString = "--> §aLoaded config for command §2/resetevs§a, alias §2/" + ResetEVs.commandAlias + "§a.";
                        break;
                    }
                    case "SHOWSTATS":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = ShowStats.commandAlias;
                        newAlias = ConfigMethods.loadConfig("ShowStats");
                        returnString = "--> §aLoaded config for command §2/showstats§a, alias §2/" + ShowStats.commandAlias + "§a.";
                        break;
                    }
                    case "SPAWNDEX":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = SpawnDex.commandAlias;
                        newAlias = ConfigMethods.loadConfig("SpawnDex");
                        returnString = "--> §aLoaded config for command §2/spawndex§a, alias §2/" + SpawnDex.commandAlias + "§a.";
                        break;
                    }
                    case "SWITCHGENDER":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = SwitchGender.commandAlias;
                        newAlias = ConfigMethods.loadConfig("SwitchGender");
                        returnString = "--> §aLoaded config for command §2/switchgender§a, alias §2/" + SwitchGender.commandAlias + "§a.";
                        break;
                    }
                    case "TIMEDHATCH": case "TIMERHATCH":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = TimedHatch.commandAlias;
                        newAlias = ConfigMethods.loadConfig("TimedHatch");
                        returnString = "--> §aLoaded config for command §2/timedhatch§a, alias §2/" + TimedHatch.commandAlias + "§a.";
                        break;
                    }
                    case "TIMEDHEAL": case "TIMERHEAL":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = TimedHeal.commandAlias;
                        newAlias = ConfigMethods.loadConfig("TimedHeal");
                        returnString = "--> §aLoaded config for command §2/timedheal§a, alias §2/" + TimedHeal.commandAlias + "§a.";
                        break;
                    }
                    /*case "UPGRADEIVS":
                    {
                        printHeaderAndCheckFolder(src, false);
                        oldAlias = UpgradeIVs.commandAlias;
                        newAlias = ConfigMethods.loadConfig("UpgradeIVs");
                        returnString = "--> §aLoaded config for command §2/upgradeivs§a, alias §2/" + UpgradeIVs.commandAlias + "§a.";
                        break;
                    }*/
                    default:
                        gotConfigError = true;
                }

                if (!gotConfigError)
                {
                    printBasicMessage(returnString);

                    if (cappedConfigString.toUpperCase().matches("MAIN|MAINCONFIG"))
                        printBasicMessage("--> §aMain config does not have an alias to check. We're done!");
                    else if (newAlias != null && !newAlias.equals(oldAlias))
                    {
                        printBasicMessage("--> §aDetected a possible changed alias, re-registering PU commands.");
                        successfulInit = reloadMappings();

                        if (successfulInit)
                            printBasicMessage("--> §aLoaded command settings. All done!");
                    }
                    else if (newAlias != null)
                        printBasicMessage("--> §aAlias was unchanged, skipping command re-registration. Done!");
                    else
                        printBasicMessage("--> §eCould not parse config. Check for stray/missing characters.");
                }
            }
        }
        else
            gotConfigError = true;

        if (gotConfigError)
        {
            if (src instanceof Player)
                src.sendMessage(Text.of("§5-----------------------------------------------------"));
            else
                printBasicMessage("===========================================================================");

            if (args.<String>getOne("config").isPresent())
                src.sendMessage(Text.of("§4Error: §cInvalid config provided. See below for valid configs."));
            else
                src.sendMessage(Text.of("§4Error: §cNo config provided. See below for valid configs."));

            src.sendMessage(Text.of("§4Usage: §c/pureload <config>"));
            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of("§6Commands: §eCheckEgg, CheckStats, CheckTypes, DittoFusion"));
            src.sendMessage(Text.of("§6Commands: §eFixGenders, ForceHatch, ForceStats, Info"));
            src.sendMessage(Text.of("§6Commands: §eResetCount, ResetEVs, ShowStats, SpawnDex"));
            src.sendMessage(Text.of("§6Commands: §eSwitchGender, TimedHatch, TimedHeal, UpgradeIVs"));
            src.sendMessage(Text.of("§6Other: §eAll (reloads ALL configs!), Main (reloads global config)"));

            if (src instanceof Player)
                src.sendMessage(Text.of("§5-----------------------------------------------------"));
            else
                printBasicMessage("===========================================================================");
        }
        else
        {
            if (successfulInit && src instanceof Player)
            {
                src.sendMessage(Text.of("§7-----------------------------------------------------"));
                src.sendMessage(Text.of("§3PU Reload: §bReloaded the provided config(s)!"));
                src.sendMessage(Text.of("§3PU Reload: §bPlease check the console for any errors."));
                src.sendMessage(Text.of("§7-----------------------------------------------------"));
            }

            printBasicMessage("===========================================================================");
        }

        return CommandResult.success();
    }
}
