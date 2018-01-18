package rs.expand.pixelupgrade.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.commands.*;
import static rs.expand.pixelupgrade.PixelUpgrade.*;
import static rs.expand.pixelupgrade.utilities.CommonMethods.printUnformattedMessage;

// Note: printUnformattedMessage is a static import for a function from CommonMethods, for convenience.
// Also, PixelUpgrade class variables are loaded in the same way. Used in loadConfig and registerCommands.
public class ConfigOperations
{
    // Make a little converter for safely handling possibly null Strings that have an integer value inside.
    private static Integer interpretInteger(String input)
    {
        if (input != null && input.matches("^[0-9]\\d*$"))
            return Integer.parseInt(input);
        else
            return null;
    }

    // Do the same for doubles.
    private static Double interpretDouble(String input)
    {
        if (input != null && input.matches("\\(?\\d+\\.\\d+\\)?"))
            return Double.parseDouble(input);
        else
            return null;
    }

    // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
    public static void checkConfigDir()
    {
        try
        {
            Files.createDirectory(Paths.get(PixelUpgrade.commandConfigPath));
            printUnformattedMessage("--> §aPixelUpgrade folder not found, making a new one for command configs...");
        }
        catch (IOException ignored) {}
    }

    public static boolean registerCommands()
    {
        PluginContainer puContainer = Sponge.getPluginManager().getPlugin("pixelupgrade").orElse(null);

        if (puContainer != null)
        {
            if (CheckEgg.commandAlias != null && !CheckEgg.commandAlias.equals("checkegg"))
                Sponge.getCommandManager().register(puContainer, checkegg, "checkegg", "eggcheck", CheckEgg.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, checkegg, "checkegg", "eggcheck");

            if (CheckStats.commandAlias != null && !CheckStats.commandAlias.equals("checkstats"))
                Sponge.getCommandManager().register(puContainer, checkstats, "checkstats", "getstats", CheckStats.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, checkstats, "checkstats", "getstats");

            if (CheckTypes.commandAlias != null && !CheckTypes.commandAlias.equals("checktypes"))
                Sponge.getCommandManager().register(puContainer, checktypes, "checktypes", "checktype", "weakness", CheckTypes.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, checktypes, "checktypes", "checktype", "weakness");

            if (DittoFusion.commandAlias != null && !DittoFusion.commandAlias.equals("dittofusion"))
                Sponge.getCommandManager().register(puContainer, dittofusion, "dittofusion", "fuseditto", DittoFusion.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, dittofusion, "dittofusion", "fuseditto");

            if (FixEVs.commandAlias != null && !FixEVs.commandAlias.equals("fixevs"))
                Sponge.getCommandManager().register(puContainer, fixevs, "fixevs", "fixev", FixEVs.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, fixevs, "fixevs", "fixev");

            if (FixLevel.commandAlias != null && !FixLevel.commandAlias.equals("fixlevel"))
                Sponge.getCommandManager().register(puContainer, fixlevel, "fixlevel", "fixlevels", FixLevel.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, fixlevel, "fixlevel", "fixlevels");

            if (ForceHatch.commandAlias != null && !ForceHatch.commandAlias.equals("forcehatch"))
                Sponge.getCommandManager().register(puContainer, forcehatch, "forcehatch", ForceHatch.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, forcehatch, "forcehatch");

            if (ForceStats.commandAlias != null && !ForceStats.commandAlias.equals("forcestats"))
                Sponge.getCommandManager().register(puContainer, forcestats, "forcestats", "forcestat", ForceStats.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, forcestats, "forcestats", "forcestat");

            if (PixelUpgradeInfo.commandAlias != null && !PixelUpgradeInfo.commandAlias.equals("pixelupgrade"))
                Sponge.getCommandManager().register(puContainer, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo", PixelUpgradeInfo.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo");

            if (PokeCure.commandAlias != null && !PokeCure.commandAlias.equals("pokecure"))
                Sponge.getCommandManager().register(puContainer, pokecure, "pokecure", "pokécure", PokeCure.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, pokecure, "pokecure", "pokécure");

            Sponge.getCommandManager().register(puContainer, reloadconfigs, "pureload", "pixelupgradereload");

            if (ResetCount.commandAlias != null && !ResetCount.commandAlias.equals("resetcount"))
                Sponge.getCommandManager().register(puContainer, resetcount, "resetcount", "resetcounts", ResetCount.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, resetcount, "resetcount", "resetcounts");

            if (ResetEVs.commandAlias != null && !ResetEVs.commandAlias.equals("resetevs"))
                Sponge.getCommandManager().register(puContainer, resetevs, "resetevs", "resetev", ResetEVs.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, resetevs, "resetevs", "resetev");

            if (ShowStats.commandAlias != null && !ShowStats.commandAlias.equals("showstats"))
                Sponge.getCommandManager().register(puContainer, showstats, "showstats", ShowStats.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, showstats, "showstats");

            if (SpawnDex.commandAlias != null && !SpawnDex.commandAlias.equals("spawndex"))
                Sponge.getCommandManager().register(puContainer, spawndex, "spawndex", "spawnid", SpawnDex.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, spawndex, "spawndex", "spawnid");

            if (SwitchGender.commandAlias != null && !SwitchGender.commandAlias.equals("switchgender"))
                Sponge.getCommandManager().register(puContainer, switchgender, "switchgender", SwitchGender.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, switchgender, "switchgender");

            if (UpgradeIVs.commandAlias != null && !UpgradeIVs.commandAlias.equals("upgradeivs"))
                Sponge.getCommandManager().register(puContainer, upgradeivs, "upgradeivs", "upgradeiv", UpgradeIVs.commandAlias);
            else
                Sponge.getCommandManager().register(puContainer, upgradeivs, "upgradeivs", "upgradeiv");

            return true;
        }
        else
        {
            printUnformattedMessage("§cCommand re-initialization failed. Please report this, this is a bug.");
            printUnformattedMessage("§cPixelUpgrade's commands have been disabled. A reboot or reload may work.");

            return false;
        }
    }

    // Called during initial load, and when a command is reloaded. Load configs, and print a pretty list.
    public static void printCommandsAndAliases()
    {
        // Do some initial setup for our formatted messages later on. We'll show three commands per line.
        ArrayList<String> commandList = new ArrayList<>();
        StringBuilder formattedCommand = new StringBuilder(), printableList = new StringBuilder();
        String commandAlias = "ERROR PLEASE REPORT", commandString = null;

        // Format our commands and aliases and add them to the lists that we'll print in a bit.
        // TODO: If you add a command, update this list and increment the counters! (currently 17)
        for (int i = 1; i <= 17; i++)
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
                    commandAlias = FixEVs.commandAlias;
                    commandString = "/fixevs";
                    break;
                }
                case 6:
                {
                    commandAlias = FixLevel.commandAlias;
                    commandString = "/fixlevel";
                    break;
                }
                case 7:
                {
                    commandAlias = ForceHatch.commandAlias;
                    commandString = "/forcehatch";
                    break;
                }
                case 8:
                {
                    commandAlias = ForceStats.commandAlias;
                    commandString = "/forcestats";
                    break;
                }
                case 9:
                {
                    commandAlias = PixelUpgradeInfo.commandAlias;
                    commandString = "/pixelupgrade";
                    break;
                }
                case 10:
                {
                    commandAlias = PokeCure.commandAlias;
                    commandString = "/pokecure";
                    break;
                }
                case 11:
                {
                    commandAlias = "pureload"; // Will be omitted, as there's a check for aliases matching base commands.
                    commandString = "/pureload";
                    break;
                }
                case 12:
                {
                    commandAlias = ResetCount.commandAlias;
                    commandString = "/resetcount";
                    break;
                }
                case 13:
                {
                    commandAlias = ResetEVs.commandAlias;
                    commandString = "/resetevs";
                    break;
                }
                case 14:
                {
                    commandAlias = ShowStats.commandAlias;
                    commandString = "/showstats";
                    break;
                }
                case 15:
                {
                    commandAlias = SpawnDex.commandAlias;
                    commandString = "/spawndex";
                    break;
                }
                case 16:
                {
                    commandAlias = SwitchGender.commandAlias;
                    commandString = "/switchgender";
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
                // Format the command.
                formattedCommand.append("§2").append(commandString);

                if (commandString.equals("/" + commandAlias))
                    formattedCommand.append("§a, ");
                else
                {
                    formattedCommand.append("§a (§2/");
                    formattedCommand.append(commandAlias.toLowerCase());
                    formattedCommand.append("§a), ");
                }

                // If we're at the last command, shank the trailing comma for a clean end.
                if (i == 17)
                    formattedCommand.setLength(formattedCommand.length() - 2);

                // Add the formatted command to the list, and then clear the StringBuilder so we can re-use it.
                commandList.add(formattedCommand.toString());
                formattedCommand.setLength(0);
            }
        }

        // Print the formatted commands + aliases.
        printUnformattedMessage("--> §aSuccessfully loaded a bunch of commands! See below.");

        int listSize = commandList.size();
        for (int q = 1; q < listSize + 1; q++)
        {
            printableList.append(commandList.get(q - 1));

            if (q == listSize) // Are we on the last entry of the list? Exit.
                printUnformattedMessage("    " + printableList);
            else if (q % 3 == 0) // Is the loop number a multiple of 3? If so, we have three commands stocked up. Print!
            {
                printUnformattedMessage("    " + printableList);
                printableList.setLength(0); // Wipe the list so we can re-use it for the next three commands.
            }
        }
    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    private static void tryCreateConfig(String callSource, Path checkPath)
    {
        if (Files.notExists(checkPath))
        {
            if (callSource.equals("PixelUpgrade"))
            {
                try
                {
                    printUnformattedMessage("§eNo primary configuration file found, creating...");

                    Files.copy(ConfigOperations.class.getResourceAsStream("/assets/PixelUpgradeMain.conf"),
                            Paths.get(PixelUpgrade.primaryPath, "PixelUpgrade.conf"));
                }
                catch (IOException F)
                {
                    printUnformattedMessage("§cPrimary config setup has failed! Please report this.");
                    printUnformattedMessage("§cAdd any useful info you may have (operating system?). Stack trace:");

                    F.printStackTrace();
                }
            }
            else
            {
                try
                {
                    printUnformattedMessage("§eNo §6/" + callSource.toLowerCase() +
                            "§e configuration file found, creating...");

                    Files.copy(ConfigOperations.class.getResourceAsStream("/assets/" + callSource + ".conf"),
                            Paths.get(PixelUpgrade.commandConfigPath, callSource + ".conf"));
                }
                catch (IOException F)
                {
                    printUnformattedMessage("§cConfig setup for command \"§4/" + callSource.toLowerCase()
                            + "§c\" failed! Please report this.");
                    printUnformattedMessage("§cAdd any useful info you may have (operating system?). Stack trace:");
                    F.printStackTrace();
                }
            }
        }
    }

    // aaaaaaaaaaaaaaaAAAAAAAAAAAAAAAAAAAAAAAAAA
    public static void loadAllCommandConfigs()
    {
        loadConfig("CheckEgg");
        loadConfig("CheckStats");
        loadConfig("CheckTypes");
        loadConfig("DittoFusion");
        loadConfig("FixEVs");
        loadConfig("FixLevel");
        loadConfig("ForceHatch");
        loadConfig("ForceStats");
        loadConfig("PixelUpgradeInfo");
        loadConfig("PokeCure");
        loadConfig("ResetCount");
        loadConfig("ResetEVs");
        loadConfig("ShowStats");
        loadConfig("SpawnDex");
        loadConfig("SwitchGender");
        loadConfig("UpgradeIVs");
    }

    // Grab a specified config, create/read its config file, then load all of the variables into the matching command
    public static String loadConfig(String callSource)
    {
        try
        {
            switch (callSource) // TODO: Added a new command? Update the switch list! Default should NEVER be called!
            {
                // Special.
                case "PixelUpgrade":
                {
                    tryCreateConfig("PixelUpgrade", primaryConfigPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.primaryConfigLoader.load();

                    PixelUpgrade.configVersion =
                            interpretInteger(commandConfig.getNode("configVersion").getString());
                    PixelUpgrade.debugLevel =
                            interpretInteger(commandConfig.getNode("debugVerbosityMode").getString());
                    PixelUpgrade.useBritishSpelling =
                            toBooleanObject(commandConfig.getNode("useBritishSpelling").getString());
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

                    return null;
                }

                // Commands.
                case "CheckEgg":
                {
                    tryCreateConfig("CheckEgg", checkEggPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.checkEggLoader.load();

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
                    CommentedConfigurationNode commandConfig = PixelUpgrade.checkStatsLoader.load();

                    CheckStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    CheckStats.showTeamWhenSlotEmpty =
                            toBooleanObject(commandConfig.getNode("showTeamWhenSlotEmpty").getString());
                    CheckStats.showEVs =
                            toBooleanObject(commandConfig.getNode("showEVs").getString());
                    CheckStats.showFixEVsHelper =
                            toBooleanObject(commandConfig.getNode("showFixEVsHelper").getString());
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
                    CommentedConfigurationNode commandConfig = PixelUpgrade.checkTypesLoader.load();

                    CheckTypes.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    CheckTypes.showFormMessage =
                            toBooleanObject(commandConfig.getNode("showFormMessage").getString());
                    CheckTypes.showAlolanMessage =
                            toBooleanObject(commandConfig.getNode("showAlolanMessage").getString());
                    CheckTypes.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return CheckTypes.commandAlias;
                }
                case "DittoFusion":
                {
                    tryCreateConfig("DittoFusion", dittoFusionPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.dittoFusionLoader.load();

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
                case "FixEVs":
                {
                    tryCreateConfig("FixEVs", fixEVsPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.fixEVsLoader.load();

                    FixEVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    FixEVs.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return FixEVs.commandAlias;
                }
                case "FixLevel":
                {
                    tryCreateConfig("FixLevel", fixLevelPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.fixLevelLoader.load();

                    FixLevel.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    FixLevel.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return FixLevel.commandAlias;
                }
                case "ForceHatch":
                {
                    tryCreateConfig("ForceHatch", forceHatchPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.forceHatchLoader.load();

                    ForceHatch.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ForceHatch.commandAlias;
                }
                case "ForceStats":
                {
                    tryCreateConfig("ForceStats", forceStatsPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.forceStatsLoader.load();

                    ForceStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ForceStats.commandAlias;
                }
                case "PixelUpgradeInfo":
                {
                    tryCreateConfig("PixelUpgradeInfo", puInfoPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.puInfoLoader.load();

                    PixelUpgradeInfo.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    PixelUpgradeInfo.numLinesPerPage =
                            interpretInteger(commandConfig.getNode("numLinesPerPage").getString());

                    return PixelUpgradeInfo.commandAlias;
                }
                case "PokeCure":
                {
                    tryCreateConfig("PokeCure", pokeCurePath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.pokeCureLoader.load();

                    PokeCure.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    PokeCure.cooldownInSeconds =
                            interpretInteger(commandConfig.getNode("cooldownInSeconds").getString());
                    PokeCure.altCooldownInSeconds =
                            interpretInteger(commandConfig.getNode("altCooldownInSeconds").getString());
                    PokeCure.healParty  =
                            toBooleanObject(commandConfig.getNode("healParty").getString());
                    PokeCure.cureAilments =
                            toBooleanObject(commandConfig.getNode("cureAilments").getString());
                    PokeCure.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return PokeCure.commandAlias;
                }
                case "ResetCount":
                {
                    tryCreateConfig("ResetCount", resetCountPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.resetCountLoader.load();

                    ResetCount.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ResetCount.commandAlias;
                }
                case "ResetEVs":
                {
                    tryCreateConfig("ResetEVs", resetEVsPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.resetEVsLoader.load();

                    ResetEVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    ResetEVs.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return ResetEVs.commandAlias;
                }
                case "ShowStats":
                {
                    tryCreateConfig("ShowStats", showStatsPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.showStatsLoader.load();

                    ShowStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    ShowStats.cooldownInSeconds =
                            interpretInteger(commandConfig.getNode("cooldownInSeconds").getString());
                    ShowStats.altCooldownInSeconds =
                            interpretInteger(commandConfig.getNode("altCooldownInSeconds").getString());
                    ShowStats.compactMode =
                            toBooleanObject(commandConfig.getNode("compactMode").getString());
                    ShowStats.showCounts =
                            toBooleanObject(commandConfig.getNode("showCounts").getString());
                    ShowStats.showNicknames =
                            toBooleanObject(commandConfig.getNode("showNicknames").getString());
                    ShowStats.clampBadNicknames =
                            toBooleanObject(commandConfig.getNode("clampBadNicknames").getString());
                    ShowStats.notifyBadNicknames =
                            toBooleanObject(commandConfig.getNode("notifyBadNicknames").getString());
                    ShowStats.showExtraInfo =
                            toBooleanObject(commandConfig.getNode("showExtraInfo").getString());
                    ShowStats.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return ShowStats.commandAlias;
                }
                case "SpawnDex":
                {
                    tryCreateConfig("SpawnDex", spawnDexPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.spawnDexLoader.load();

                    SpawnDex.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return SpawnDex.commandAlias;
                }
                case "SwitchGender":
                {
                    tryCreateConfig("SwitchGender", switchGenderPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.switchGenderLoader.load();

                    SwitchGender.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    SwitchGender.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return SwitchGender.commandAlias;
                }
                case "UpgradeIVs":
                {
                    tryCreateConfig("UpgradeIVs", upgradeIVsPath);
                    CommentedConfigurationNode commandConfig = PixelUpgrade.upgradeIVsLoader.load();

                    UpgradeIVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    UpgradeIVs.legendaryAndShinyCap =
                            interpretInteger(commandConfig.getNode("legendaryAndShinyCap").getString());
                    UpgradeIVs.legendaryCap =
                            interpretInteger(commandConfig.getNode("legendaryCap").getString());
                    UpgradeIVs.regularCap =
                            interpretInteger(commandConfig.getNode("regularCap").getString());
                    UpgradeIVs.shinyCap =
                            interpretInteger(commandConfig.getNode("shinyCap").getString());
                    UpgradeIVs.babyCap =
                            interpretInteger(commandConfig.getNode("babyCap").getString());
                    UpgradeIVs.mathMultiplier =
                            interpretDouble(commandConfig.getNode("mathMultiplier").getString());
                    UpgradeIVs.fixedUpgradeCost =
                            interpretInteger(commandConfig.getNode("fixedUpgradeCost").getString());
                    UpgradeIVs.legendaryAndShinyMult =
                            interpretDouble(commandConfig.getNode("legendaryAndShinyMult").getString());
                    UpgradeIVs.legendaryMult =
                            interpretDouble(commandConfig.getNode("legendaryMult").getString());
                    UpgradeIVs.regularMult =
                            interpretDouble(commandConfig.getNode("regularMult").getString());
                    UpgradeIVs.shinyMult =
                            interpretDouble(commandConfig.getNode("shinyMult").getString());
                    UpgradeIVs.babyMult =
                            interpretDouble(commandConfig.getNode("babyMult").getString());
                    UpgradeIVs.upgradesFreeBelow =
                            interpretInteger(commandConfig.getNode("upgradesFreeBelow").getString());
                    UpgradeIVs.addFlatFee =
                            interpretInteger(commandConfig.getNode("addFlatFee").getString());

                    return UpgradeIVs.commandAlias;
                }
                default:
                {
                    printUnformattedMessage("§cConfig gathering failed; fell through the switch.");
                    printUnformattedMessage("§cIf you're on an official release, this is a bug. Source: §4" + callSource);
                    return null;
                }
            }
        }
        catch (Exception F)
        {
            printUnformattedMessage("§4" + callSource + "§c had an issue during config loading!");
            printUnformattedMessage("§cCheck your configs for stray/missing characters. Stack trace:");
            F.printStackTrace();
        }

        return null;
    }
}
