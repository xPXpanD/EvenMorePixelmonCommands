// The enormous and rather complicated config handler, version two. Could use some work still, but it'll do for now.
// Version one was just n classes, where n was the number of commands there were minus /pureload. Yeah.
package rs.expand.pixelupgrade.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import java.util.Scanner;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.commands.*;
import static rs.expand.pixelupgrade.PixelUpgrade.*;
import static rs.expand.pixelupgrade.utilities.PrintingMethods.printBasicMessage;

// Note: printBasicMessage is a static import for a function from PrintingMethods, for convenience.
// Also, PixelUpgrade class variables are loaded in the same way. Used in loadConfig and registerCommands.
public class ConfigMethods
{
    // If we find a config that's broken during reloads, we set this flag and print an error.
    private static boolean gotConfigError;

    // Make a little converter for safely handling possibly null Strings that have an integer value inside.
    private static Integer interpretInteger(final String input)
    {
        if (input != null && input.matches("-?[1-9]\\d*|0"))
            return Integer.parseInt(input);
        else
            return null;
    }

    // Do the same for doubles.
    private static Double interpretDouble(final String input)
    {
        if (input != null)
        {
            final Scanner readDouble = new Scanner(input);
            if (readDouble.hasNextDouble())
                return readDouble.nextDouble();
        }

        // Was the input null, or could we not find a double? Return null and let our commands show an error.
        return null;
    }

    // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
    public static void checkConfigDir()
    {
        try
        {
            Files.createDirectory(Paths.get(PixelUpgrade.commandConfigPath));
            printBasicMessage("--> §aPixelUpgrade folder not found, making a new one for command configs...");
        }
        catch (final IOException ignored) {}
    }

    public static boolean registerCommands()
    {
        final PluginContainer puContainer = Sponge.getPluginManager().getPlugin("pixelupgrade").orElse(null);

        // Contains base commands and common (?) mistakes, as well as interchangeable alternatives.
        if (puContainer != null)
        {
            if (CheckEgg.commandAlias != null && !CheckEgg.commandAlias.equals("checkegg"))
                Sponge.getCommandManager().register(puContainer, checkegg, "checkegg", CheckEgg.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, checkegg, "checkegg");

            if (CheckStats.commandAlias != null && !CheckStats.commandAlias.matches("checkstats|getstats"))
                Sponge.getCommandManager().register(puContainer, checkstats, "checkstats", "getstats", CheckStats.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, checkstats, "checkstats", "getstats");

            if (CheckTypes.commandAlias != null && !CheckTypes.commandAlias.matches("checktypes|checktype"))
                Sponge.getCommandManager().register(puContainer, checktypes, "checktypes", "checktype", CheckTypes.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, checktypes, "checktypes", "checktype");

            if (DittoFusion.commandAlias != null && !DittoFusion.commandAlias.equals("dittofusion"))
                Sponge.getCommandManager().register(puContainer, dittofusion, "dittofusion", DittoFusion.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, dittofusion, "dittofusion");

            if (FixGenders.commandAlias != null && !FixGenders.commandAlias.matches("fixgenders|fixgender"))
                Sponge.getCommandManager().register(puContainer, fixgenders, "fixgenders", "fixgender", FixGenders.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, fixgenders, "fixgenders", "fixgender");

            if (ForceHatch.commandAlias != null && !ForceHatch.commandAlias.equals("forcehatch"))
                Sponge.getCommandManager().register(puContainer, forcehatch, "forcehatch", ForceHatch.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, forcehatch, "forcehatch");

            if (ForceStats.commandAlias != null && !ForceStats.commandAlias.matches("forcestats|forcestat"))
                Sponge.getCommandManager().register(puContainer, forcestats, "forcestats", "forcestat", ForceStats.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, forcestats, "forcestats", "forcestat");

            if (PixelUpgradeInfo.commandAlias != null && !PixelUpgradeInfo.commandAlias.matches("pixelupgrade|pixelupgradeinfo"))
                Sponge.getCommandManager().register(puContainer, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo", PixelUpgradeInfo.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo");

            Sponge.getCommandManager().register(puContainer, reloadconfigs, "pureload", "pixelupgradereload");

            if (ResetCount.commandAlias != null && !ResetCount.commandAlias.matches("resetcount|resetcounts"))
                Sponge.getCommandManager().register(puContainer, resetcount, "resetcount", "resetcounts", ResetCount.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, resetcount, "resetcount", "resetcounts");

            if (ResetEVs.commandAlias != null && !ResetEVs.commandAlias.matches("resetevs|resetev"))
                Sponge.getCommandManager().register(puContainer, resetevs, "resetevs", "resetev", ResetEVs.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, resetevs, "resetevs", "resetev");

            if (ShowStats.commandAlias != null && !ShowStats.commandAlias.matches("showstats|showstat"))
                Sponge.getCommandManager().register(puContainer, showstats, "showstats", "showstat", ShowStats.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, showstats, "showstats", "showstat");

            if (SpawnDex.commandAlias != null && !SpawnDex.commandAlias.equals("spawndex"))
                Sponge.getCommandManager().register(puContainer, spawndex, "spawndex", SpawnDex.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, spawndex, "spawndex");

            if (SwitchGender.commandAlias != null && !SwitchGender.commandAlias.matches("switchgender|switchgenders"))
                Sponge.getCommandManager().register(puContainer, switchgender, "switchgender", "switchgenders", SwitchGender.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, switchgender, "switchgender", "switchgenders");

            if (TimedHatch.commandAlias != null && !TimedHatch.commandAlias.matches("timedhatch|timerhatch"))
                Sponge.getCommandManager().register(puContainer, timedhatch, "timedhatch", "timerhatch", TimedHatch.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, timedhatch, "timedhatch", "timerhatch");

            if (TimedHeal.commandAlias != null && !TimedHeal.commandAlias.matches("timedheal|timerheal"))
                Sponge.getCommandManager().register(puContainer, timedheal, "timedheal", "timerheal", TimedHeal.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, timedheal, "timedheal", "timerheal");

            if (UpgradeIVs.commandAlias != null && !UpgradeIVs.commandAlias.matches("upgradeivs|upgradeiv"))
                Sponge.getCommandManager().register(puContainer, upgradeivs, "upgradeivs", "upgradeiv", UpgradeIVs.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, upgradeivs, "upgradeivs", "upgradeiv");

            return true;
        }
        else
        {
            printBasicMessage("§cCommand (re-)initialization failed. Please report this, this is a bug.");
            printBasicMessage("§cPixelUpgrade's commands are likely dead. A reboot or reload may work.");

            return false;
        }
    }

    // Called during initial load, and when a command is reloaded. Load configs, and print a pretty list.
    public static void printCommandsAndAliases()
    {
        // Do some initial setup for our formatted messages later on. We'll show three commands per line.
        final ArrayList<String> commandList = new ArrayList<>();
        final StringBuilder formattedCommand = new StringBuilder();
        final StringBuilder printableList = new StringBuilder();
        String commandAlias = "§4There's an error message missing, please report this!", commandString = null;

        // Format our commands and aliases and add them to the lists that we'll print in a bit.
        // TODO: If you add/remove a command, update this list and the numEntries counter!
        final int numEntries = 17;
        for (int i = 1; i <= numEntries; i++)
        {
            switch (i)
            {
                // Normal commands. If the alias is null (error returned), we pass the base command again instead.
                // This prevents NPEs while also letting us hide commands by checking whether they've returned null.
                case 1:
                {
                    commandAlias = CheckEgg.commandAlias;
                    commandString = "/checkegg";
                    break;
                }
                case 2:
                {
                    commandAlias = CheckStats.commandAlias;
                    commandString = "/checkstats";
                    break;
                }
                case 3:
                {
                    commandAlias = CheckTypes.commandAlias;
                    commandString = "/checktypes";
                    break;
                }
                case 4:
                {
                    commandAlias = DittoFusion.commandAlias;
                    commandString = "/dittofusion";
                    break;
                }
                case 5:
                {
                    commandAlias = FixGenders.commandAlias;
                    commandString = "/fixgenders";
                    break;
                }
                case 6:
                {
                    commandAlias = ForceHatch.commandAlias;
                    commandString = "/forcehatch";
                    break;
                }
                case 7:
                {
                    commandAlias = ForceStats.commandAlias;
                    commandString = "/forcestats";
                    break;
                }
                case 8:
                {
                    commandAlias = PixelUpgradeInfo.commandAlias;
                    commandString = "/pixelupgrade";
                    break;
                }
                case 9:
                {
                    commandAlias = "pureload"; // Alias gets omitted; there's a check for aliases matching base commands.
                    commandString = "/pureload";
                    break;
                }
                case 10:
                {
                    commandAlias = ResetCount.commandAlias;
                    commandString = "/resetcount";
                    break;
                }
                case 11:
                {
                    commandAlias = ResetEVs.commandAlias;
                    commandString = "/resetevs";
                    break;
                }
                case 12:
                {
                    commandAlias = ShowStats.commandAlias;
                    commandString = "/showstats";
                    break;
                }
                case 13:
                {
                    commandAlias = SpawnDex.commandAlias;
                    commandString = "/spawndex";
                    break;
                }
                case 14:
                {
                    commandAlias = SwitchGender.commandAlias;
                    commandString = "/switchgender";
                    break;
                }
                case 15:
                {
                    commandAlias = TimedHatch.commandAlias;
                    commandString = "/timedhatch";
                    break;
                }
                case 16:
                {
                    commandAlias = TimedHeal.commandAlias;
                    commandString = "/timedheal";
                    break;
                }
                case 17:
                {
                    commandAlias = UpgradeIVs.commandAlias;
                    commandString = "/upgradeivs";
                    break;
                }
            }

            if (commandAlias != null)
            {
                // Format the command's shown text.
                formattedCommand.append("§2").append(commandString);

                if (commandString.equals("/" + commandAlias))
                    formattedCommand.append("§a§f, ");
                else
                {
                    formattedCommand.append("§a (§2/");
                    formattedCommand.append(commandAlias.toLowerCase());
                    formattedCommand.append("§a)§f, ");
                }
            }
            else
            {
                // Alias loading went very very wrong, do some special red error formatting.
                formattedCommand.append("§4").append(commandString);
                formattedCommand.append("§c (§4");
                formattedCommand.append("ERROR!");
                formattedCommand.append("§c)§f, ");

                gotConfigError = true;
            }

            // If we're at the last command, shank the trailing formatting code, comma and space and for a clean end.
            if (i == numEntries)
                formattedCommand.setLength(formattedCommand.length() - 4);

            // Add the formatted command to the list, and then clear the StringBuilder so we can re-use it.
            commandList.add(formattedCommand.toString());
            formattedCommand.setLength(0);
        }

        // If we got a config error, warn here.
        if (gotConfigError)
            printBasicMessage("--> §eIssues found. Check for stray/missing characters, or recreate configs.");

        // Print the formatted commands + aliases.
        printBasicMessage("--> §aLoaded a bunch of commands, see below.");

        for (int q = 1; q < numEntries + 1; q++)
        {
            printableList.append(commandList.get(q - 1));

            if (q == numEntries) // Are we on the last entry of the list? Print and exit.
                printBasicMessage("    " + printableList);
            else if (q % 3 == 0) // Can the loop number be divided by 3? If so, we have three commands stocked up. Print!
            {
                printBasicMessage("    " + printableList);
                printableList.setLength(0); // Wipe the list so we can re-use it for the next three commands.
            }
        }
    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    private static void tryCreateConfig(final String callSource, final Path checkPath)
    {
        if (Files.notExists(checkPath))
        {
            if (callSource.equals("PixelUpgrade"))
            {
                try
                {
                    // Spaces added so it falls in line with startup/reload message spacing.
                    printBasicMessage("    §eNo primary configuration file found, creating...");

                    Files.copy(ConfigMethods.class.getResourceAsStream("/assets/PixelUpgradeMain.conf"),
                            Paths.get(PixelUpgrade.primaryPath, "PixelUpgrade.conf"));
                }
                catch (final IOException F)
                {
                    printBasicMessage("§cPrimary config setup has failed! Please report this.");
                    printBasicMessage("§cAdd any useful info you may have (operating system?). Stack trace:");

                    F.printStackTrace();
                }
            }
            else
            {
                try
                {
                    // Spaces added so it falls in line with startup/reload message spacing.
                    printBasicMessage("    §eNo §6/" + callSource.toLowerCase() +
                            "§e configuration file found, creating...");

                    Files.copy(ConfigMethods.class.getResourceAsStream("/assets/" + callSource + ".conf"),
                            Paths.get(PixelUpgrade.commandConfigPath, callSource + ".conf"));
                }
                catch (final IOException F)
                {
                    printBasicMessage("§cConfig setup for command \"§4/" + callSource.toLowerCase()
                            + "§c\" failed! Please report this.");
                    printBasicMessage("§cAdd any useful info you may have (operating system?). Stack trace:");
                    F.printStackTrace();
                }
            }
        }
    }

    //                                             __     __     __     __     __     __     __     __
    //            SO MUCH REPETITION              /__\   /__\   /__\   /__\   /__\   /__\   /__\   /__\
    //                                           /(__)\ /(__)\ /(__)\ /(__)\ /(__)\ /(__)\ /(__)\ /(__)\
    // aaaaaaaaaaaaaaaAAAAAAAAAAAAAAAAAAAAAAAAAA(__)(__|__)(__|__)(__|__)(__|__)(__|__)(__|__)(__|__)(__)
    public static void loadAllCommandConfigs()
    {
        loadConfig("CheckEgg");
        loadConfig("CheckStats");
        loadConfig("CheckTypes");
        loadConfig("DittoFusion");
        loadConfig("FixGenders");
        loadConfig("ForceHatch");
        loadConfig("ForceStats");
        loadConfig("PixelUpgradeInfo");
        loadConfig("ResetCount");
        loadConfig("ResetEVs");
        loadConfig("ShowStats");
        loadConfig("SpawnDex");
        loadConfig("SwitchGender");
        loadConfig("TimedHatch");
        loadConfig("TimedHeal");
        loadConfig("UpgradeIVs");
    }

    // Grab a specified config, create/read its config file, then load all of the variables into the matching command
    public static String loadConfig(final String callSource, final boolean... reloadingAll)
    {
        try
        {
            switch (callSource) // TODO: Added a new command? Update the switch list! Default should NEVER be called!
            {
                // Special.
                case "PixelUpgrade":
                {
                    tryCreateConfig("PixelUpgrade", primaryConfigPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.primaryConfigLoader.load();

                    PixelUpgrade.configVersion =
                            interpretInteger(commandConfig.getNode("configVersion").getString());
                    PixelUpgrade.debugVerbosityMode =
                            interpretInteger(commandConfig.getNode("debugVerbosityMode").getString());
                    PixelUpgrade.useBritishSpelling =
                            toBooleanObject(commandConfig.getNode("useBritishSpelling").getString());
                    PixelUpgrade.statSeparator =
                            commandConfig.getNode("statSeparator").getString();
                    PixelUpgrade.shortenedHP =
                            commandConfig.getNode("shortenedHealth").getString();
                    PixelUpgrade.shortenedAttack =
                            commandConfig.getNode("shortenedAttack").getString();
                    PixelUpgrade.shortenedDefense =
                            commandConfig.getNode("shortenedDefense").getString();
                    PixelUpgrade.shortenedSpecialAttack =
                            commandConfig.getNode("shortenedSpecialAttack").getString();
                    PixelUpgrade.shortenedSpecialDefense =
                            commandConfig.getNode("shortenedSpecialDefense").getString();
                    PixelUpgrade.shortenedSpeed =
                            commandConfig.getNode("shortenedSpeed").getString();

                    // Do some checks, these values are very important for other parts of the code.
                    boolean gotIssue = false;

                    if (debugVerbosityMode == null)
                    {
                        printBasicMessage("§cValue of global setting §4debugVerbosityMode§c could not be read.");
                        printBasicMessage("§cEnabling high verbosity (mode 2) fallback for now...");

                        debugVerbosityMode = 2;
                        gotIssue = true;
                    } // 1337 is the internal testing level, not used in releases.
                    else if (debugVerbosityMode < 0 || debugVerbosityMode > 2 && debugVerbosityMode != 1337)
                    {
                        printBasicMessage("§cValue of global setting §4debugVerbosityMode§c is out of bounds.");
                        printBasicMessage("§cEnabling high debug verbosity (mode §42§c) fallback for now...");

                        debugVerbosityMode = 2;
                        gotIssue = true;
                    }

                    if (statSeparator == null)
                    {
                        printBasicMessage("§cValue of global setting §4statSeparator§c could not be read.");
                        printBasicMessage("§cEnabling default stat separator (\"§4, §c\") fallback for now...");

                        statSeparator = "§r,§a ";
                        gotIssue = true;
                    }
                    else // Replace any provided ampersands with section symbols, which we can use inside of our code.
                        statSeparator = "§r" + PrintingMethods.parseRemoteString(statSeparator) + "§r§a";

                    if (gotIssue)
                        printBasicMessage("§cPlease check the main config carefully. Stuff may break.");

                    return null;
                }

                // Commands.
                case "CheckEgg":
                {
                    tryCreateConfig("CheckEgg", checkEggPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.checkEggLoader.load();

                    CheckEgg.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    CheckEgg.showName =
                            toBooleanObject(commandConfig.getNode("showName").getString());
                    CheckEgg.explicitReveal =
                            toBooleanObject(commandConfig.getNode("explicitReveal").getString());
                    CheckEgg.babyHintPercentage =
                            interpretInteger(commandConfig.getNode("babyHintPercentage").getString());
                    CheckEgg.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());
                    CheckEgg.recheckIsFree =
                            toBooleanObject(commandConfig.getNode("recheckIsFree").getString());

                    return CheckEgg.commandAlias;
                }
                case "CheckStats":
                {
                    tryCreateConfig("CheckStats", checkStatsPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.checkStatsLoader.load();

                    CheckStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    CheckStats.showTeamWhenSlotEmpty =
                            toBooleanObject(commandConfig.getNode("showTeamWhenSlotEmpty").getString());
                    CheckStats.showEVs =
                            toBooleanObject(commandConfig.getNode("showEVs").getString());
                    CheckStats.showUpgradeHelper =
                            toBooleanObject(commandConfig.getNode("showUpgradeHelper").getString());
                    CheckStats.showDittoFusionHelper =
                            toBooleanObject(commandConfig.getNode("showDittoFusionHelper").getString());
                    CheckStats.enableCheckEggIntegration =
                            toBooleanObject(commandConfig.getNode("enableCheckEggIntegration").getString());
                    CheckStats.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return CheckStats.commandAlias;
                }
                case "CheckTypes":
                {
                    tryCreateConfig("CheckTypes", checkTypesPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.checkTypesLoader.load();

                    CheckTypes.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    CheckTypes.showFormMessage =
                            toBooleanObject(commandConfig.getNode("showFormMessage").getString());
                    CheckTypes.showAlolanMessage =
                            toBooleanObject(commandConfig.getNode("showAlolanMessage").getString());

                    return CheckTypes.commandAlias;
                }
                case "DittoFusion":
                {
                    tryCreateConfig("DittoFusion", dittoFusionPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.dittoFusionLoader.load();

                    DittoFusion.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    DittoFusion.stat0to5 =
                            interpretInteger(commandConfig.getNode("stat0to5").getString());
                    DittoFusion.stat6to10 =
                            interpretInteger(commandConfig.getNode("stat6to10").getString());
                    DittoFusion.stat11to15 =
                            interpretInteger(commandConfig.getNode("stat11to15").getString());
                    DittoFusion.stat16to20 =
                            interpretInteger(commandConfig.getNode("stat16to20").getString());
                    DittoFusion.stat21to25 =
                            interpretInteger(commandConfig.getNode("stat21to25").getString());
                    DittoFusion.stat26to30 =
                            interpretInteger(commandConfig.getNode("stat26to30").getString());
                    DittoFusion.stat31plus =
                            interpretInteger(commandConfig.getNode("stat31plus").getString());
                    DittoFusion.regularCap =
                            interpretInteger(commandConfig.getNode("regularCap").getString());
                    DittoFusion.shinyCap =
                            interpretInteger(commandConfig.getNode("shinyCap").getString());
                    DittoFusion.passOnShinyStatus =
                            toBooleanObject(commandConfig.getNode("passOnShinyStatus").getString());
                    DittoFusion.pointMultiplierForCost =
                            interpretInteger(commandConfig.getNode("pointMultiplierForCost").getString());
                    DittoFusion.previouslyUpgradedMultiplier =
                            interpretInteger(commandConfig.getNode("previouslyUpgradedMultiplier").getString());
                    DittoFusion.addFlatFee =
                            interpretInteger(commandConfig.getNode("addFlatFee").getString());

                    return DittoFusion.commandAlias;
                }
                case "FixGenders":
                {
                    tryCreateConfig("FixGenders", fixGendersPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.fixGendersLoader.load();

                    FixGenders.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    FixGenders.sneakyMode =
                            toBooleanObject(commandConfig.getNode("sneakyMode").getString());
                    FixGenders.requireConfirmation =
                            toBooleanObject(commandConfig.getNode("requireConfirmation").getString());

                    return FixGenders.commandAlias;
                }
                case "ForceHatch":
                {
                    tryCreateConfig("ForceHatch", forceHatchPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.forceHatchLoader.load();

                    ForceHatch.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ForceHatch.commandAlias;
                }
                case "ForceStats":
                {
                    tryCreateConfig("ForceStats", forceStatsPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.forceStatsLoader.load();

                    ForceStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ForceStats.commandAlias;
                }
                case "PixelUpgradeInfo":
                {
                    tryCreateConfig("PixelUpgradeInfo", puInfoPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.puInfoLoader.load();

                    PixelUpgradeInfo.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    PixelUpgradeInfo.numLinesPerPage =
                            interpretInteger(commandConfig.getNode("numLinesPerPage").getString());

                    return PixelUpgradeInfo.commandAlias;
                }
                case "ResetCount":
                {
                    tryCreateConfig("ResetCount", resetCountPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.resetCountLoader.load();

                    ResetCount.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ResetCount.commandAlias;
                }
                case "ResetEVs":
                {
                    tryCreateConfig("ResetEVs", resetEVsPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.resetEVsLoader.load();

                    ResetEVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    ResetEVs.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return ResetEVs.commandAlias;
                }
                case "ShowStats":
                {
                    tryCreateConfig("ShowStats", showStatsPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.showStatsLoader.load();

                    ShowStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    ShowStats.cooldownInSeconds =
                            interpretInteger(commandConfig.getNode("cooldownInSeconds").getString());
                    ShowStats.altCooldownInSeconds =
                            interpretInteger(commandConfig.getNode("altCooldownInSeconds").getString());
                    ShowStats.showNicknames =
                            toBooleanObject(commandConfig.getNode("showNicknames").getString());
                    ShowStats.showEVs =
                            toBooleanObject(commandConfig.getNode("showEVs").getString());
                    ShowStats.showExtraInfo =
                            toBooleanObject(commandConfig.getNode("showExtraInfo").getString());
                    ShowStats.showCounts =
                            toBooleanObject(commandConfig.getNode("showCounts").getString());
                    ShowStats.clampBadNicknames =
                            toBooleanObject(commandConfig.getNode("clampBadNicknames").getString());
                    ShowStats.notifyBadNicknames =
                            toBooleanObject(commandConfig.getNode("notifyBadNicknames").getString());
                    ShowStats.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return ShowStats.commandAlias;
                }
                case "SpawnDex":
                {
                    tryCreateConfig("SpawnDex", spawnDexPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.spawnDexLoader.load();

                    SpawnDex.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    SpawnDex.fakeMessage =
                            commandConfig.getNode("fakeMessage").getString();

                    return SpawnDex.commandAlias;
                }
                case "SwitchGender":
                {
                    tryCreateConfig("SwitchGender", switchGenderPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.switchGenderLoader.load();

                    SwitchGender.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    SwitchGender.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return SwitchGender.commandAlias;
                }
                case "TimedHatch":
                {
                    tryCreateConfig("TimedHatch", timedHatchPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.timedHatchLoader.load();

                    TimedHatch.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    TimedHatch.cooldownInSeconds =
                            interpretInteger(commandConfig.getNode("cooldownInSeconds").getString());
                    TimedHatch.altCooldownInSeconds =
                            interpretInteger(commandConfig.getNode("altCooldownInSeconds").getString());
                    TimedHatch.hatchParty  =
                            toBooleanObject(commandConfig.getNode("hatchParty").getString());
                    TimedHatch.sneakyMode  =
                            toBooleanObject(commandConfig.getNode("sneakyMode").getString());
                    TimedHatch.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return TimedHatch.commandAlias;
                }
                case "TimedHeal":
                {
                    tryCreateConfig("TimedHeal", timedHealPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.timedHealLoader.load();

                    TimedHeal.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    TimedHeal.cooldownInSeconds =
                            interpretInteger(commandConfig.getNode("cooldownInSeconds").getString());
                    TimedHeal.altCooldownInSeconds =
                            interpretInteger(commandConfig.getNode("altCooldownInSeconds").getString());
                    TimedHeal.healParty  =
                            toBooleanObject(commandConfig.getNode("healParty").getString());
                    TimedHeal.sneakyMode  =
                            toBooleanObject(commandConfig.getNode("sneakyMode").getString());
                    TimedHeal.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return TimedHeal.commandAlias;
                }
                case "UpgradeIVs":
                {
                    tryCreateConfig("UpgradeIVs", upgradeIVsPath);
                    final CommentedConfigurationNode commandConfig = PixelUpgrade.upgradeIVsLoader.load();

                    UpgradeIVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    UpgradeIVs.legendaryAndShinyCap =
                            interpretInteger(commandConfig.getNode("legendaryAndShinyCap").getString());
                    UpgradeIVs.legendaryCap =
                            interpretInteger(commandConfig.getNode("legendaryCap").getString());
                    UpgradeIVs.shinyCap =
                            interpretInteger(commandConfig.getNode("shinyCap").getString());
                    UpgradeIVs.regularCap =
                            interpretInteger(commandConfig.getNode("regularCap").getString());
                    UpgradeIVs.mathMultiplier =
                            interpretDouble(commandConfig.getNode("mathMultiplier").getString());
                    UpgradeIVs.fixedUpgradeCost =
                            interpretInteger(commandConfig.getNode("fixedUpgradeCost").getString());
                    UpgradeIVs.legendaryAndShinyMult =
                            interpretDouble(commandConfig.getNode("legendaryAndShinyMult").getString());
                    UpgradeIVs.legendaryMult =
                            interpretDouble(commandConfig.getNode("legendaryMult").getString());
                    UpgradeIVs.shinyMult =
                            interpretDouble(commandConfig.getNode("shinyMult").getString());
                    UpgradeIVs.regularMult =
                            interpretDouble(commandConfig.getNode("regularMult").getString());
                    UpgradeIVs.upgradesFreeBelow =
                            interpretInteger(commandConfig.getNode("upgradesFreeBelow").getString());
                    UpgradeIVs.addFlatFee =
                            interpretInteger(commandConfig.getNode("addFlatFee").getString());

                    return UpgradeIVs.commandAlias;
                }
                default:
                {
                    printBasicMessage("§cConfig gathering failed; fell through the switch.");
                    printBasicMessage("§cIf you're on an official release, this is a bug. Source: §4" + callSource);
                    return null;
                }
            }
        }
        catch (final Exception F)
        {
            // Spaces added so it falls in line with startup/reload message spacing.
            printBasicMessage("    §cCould not read config for §4/" + callSource.toLowerCase() + "§c.");

            // Did we get the optional "hey we're reloading everything" argument, and is it true? Print.
            if (reloadingAll.length != 0 && reloadingAll[0])
                printBasicMessage("    §cPlease check your config for any missing or invalid entries.");

            gotConfigError = true;

            // Null the command alias so these commands error out instead of trying to interpret broken values.
            switch (callSource)
            {
                case "CheckEgg": CheckEgg.commandAlias = null; break;
                case "CheckStats": CheckStats.commandAlias = null; break;
                case "CheckTypes": CheckTypes.commandAlias = null; break;
                case "DittoFusion": DittoFusion.commandAlias = null; break;
                case "FixGenders": FixGenders.commandAlias = null; break;
                case "ForceHatch": ForceHatch.commandAlias = null; break;
                case "ForceStats": ForceStats.commandAlias = null; break;
                case "PixelUpgradeInfo": PixelUpgradeInfo.commandAlias = null; break;
                case "ResetCount": ResetCount.commandAlias = null; break;
                case "ResetEVs": ResetEVs.commandAlias = null; break;
                case "ShowStats": ShowStats.commandAlias = null; break;
                case "SpawnDex": SpawnDex.commandAlias = null; break;
                case "SwitchGender": SwitchGender.commandAlias = null; break;
                case "TimedHatch": TimedHatch.commandAlias = null; break;
                case "TimedHeal": TimedHeal.commandAlias = null; break;
                case "UpgradeIVs": UpgradeIVs.commandAlias = null; break;
            }
        }

        return null;
    }
}
