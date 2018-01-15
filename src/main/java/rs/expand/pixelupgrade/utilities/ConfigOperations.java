package rs.expand.pixelupgrade.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.commands.*;
import static rs.expand.pixelupgrade.PixelUpgrade.*;
import static rs.expand.pixelupgrade.utilities.CommonMethods.printUnformattedMessage;

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

    // Called during initial load, and when a command is reloaded. Load configs, and print a pretty list.
    public static void initializeAndGrabAliases(boolean firstRun)
    {
        // Register other aliases and set up configs. Similar to the above, any errors/warnings will be printed.
        String checkEggAlias = ConfigOperations.setupConfig(
                "CheckEgg", "egg", checkEggPath, path);
        String checkStatsAlias = ConfigOperations.setupConfig(
                "CheckStats", "cs", checkStatsPath, path);
        String checkTypesAlias = ConfigOperations.setupConfig(
                "CheckTypes", "type", checkTypesPath, path);
        String dittoFusionAlias = ConfigOperations.setupConfig(
                "DittoFusion", "fuse", dittoFusionPath, path);
        String fixEVsAlias = ConfigOperations.setupConfig(
                "FixEVs", "fixevs", fixEVsPath, path);
        String fixLevelAlias = ConfigOperations.setupConfig(
                "FixLevel", "fixlevel", fixLevelPath, path);
        String forceHatchAlias = ConfigOperations.setupConfig(
                "ForceHatch", "fhatch", forceHatchPath, path);
        String forceStatsAlias = ConfigOperations.setupConfig(
                "ForceStats", "fstats", forceStatsPath, path);
        String puInfoAlias = ConfigOperations.setupConfig(
                "PixelUpgradeInfo", "pu", puInfoPath, path);
        String resetCountAlias = ConfigOperations.setupConfig(
                "ResetCount", "delcount", resetCountPath, path);
        String resetEVsAlias = ConfigOperations.setupConfig(
                "ResetEVs", "delevs", resetEVsPath, path);
        String showStatsAlias = ConfigOperations.setupConfig(
                "ShowStats", "show", showStatsPath, path);
        String spawnDexAlias = ConfigOperations.setupConfig(
                "SpawnDex", "spawndex", spawnDexPath, path);
        String switchGenderAlias = ConfigOperations.setupConfig(
                "SwitchGender", "bend", switchGenderPath, path);
        String upgradeIVsAlias = ConfigOperations.setupConfig(
                "UpgradeIVs", "upgrade", upgradeIVsPath, path);

        // Do some initial setup for our formatted messages later on. We'll show three commands per line.
        ArrayList<String> commandList = new ArrayList<>();
        StringBuilder formattedCommand = new StringBuilder(), printableList = new StringBuilder();
        String commandAlias = "ERROR PLEASE REPORT", commandString = null;

        // Format our commands and aliases and add them to the lists that we'll print in a bit.
        // TODO: If you add a command, update this list!
        for (int i = 1; i <= 16; i++)
        {
            switch (i)
            {
                // Normal commands. If the alias is null (error returned), we pass the base command again instead.
                // This prevents NPEs while also letting us hide commands by checking whether they've returned null.
                case 1:
                {
                    commandAlias = checkEggAlias;
                    commandString = "/checkegg";
                    break;
                }
                case 2:
                {
                    commandAlias = checkStatsAlias;
                    commandString = "/checkstats";
                    break;
                }
                case 3:
                {
                    commandAlias = checkTypesAlias;
                    commandString = "/checktypes";
                    break;
                }
                case 4:
                {
                    commandAlias = dittoFusionAlias;
                    commandString = "/dittofusion";
                    break;
                }
                case 5:
                {
                    commandAlias = fixEVsAlias;
                    commandString = "/fixevs";
                    break;
                }
                case 6:
                {
                    commandAlias = fixLevelAlias;
                    commandString = "/fixlevel";
                    break;
                }
                case 7:
                {
                    commandAlias = forceHatchAlias;
                    commandString = "/forcehatch";
                    break;
                }
                case 8:
                {
                    commandAlias = forceStatsAlias;
                    commandString = "/forcestats";
                    break;
                }
                case 9:
                {
                    commandAlias = puInfoAlias;
                    commandString = "/pixelupgrade";
                    break;
                }
                case 10:
                {
                    commandAlias = "pureload"; // Will be omitted, as there's a check for aliases matching base commands.
                    commandString = "/pureload";
                    break;
                }
                case 11:
                {
                    commandAlias = resetCountAlias;
                    commandString = "/resetcount";
                    break;
                }
                case 12:
                {
                    commandAlias = resetEVsAlias;
                    commandString = "/resetevs";
                    break;
                }
                case 13:
                {
                    commandAlias = showStatsAlias;
                    commandString = "/showstats";
                    break;
                }
                case 14:
                {
                    commandAlias = spawnDexAlias;
                    commandString = "/spawndex";
                    break;
                }
                case 15:
                {
                    commandAlias = switchGenderAlias;
                    commandString = "/switchgender";
                    break;
                }
                case 16:
                {
                    commandAlias = upgradeIVsAlias;
                    commandString = "/upgradeivs";
                    break;
                }
            }

            if (commandAlias != null)
            {
                // Format the command.
                formattedCommand.append("§2");
                formattedCommand.append(commandString);

                if (commandString.equals("/" + commandAlias))
                    formattedCommand.append("§a, ");
                else
                {
                    formattedCommand.append("§a (§2/");
                    formattedCommand.append(commandAlias.toLowerCase());
                    formattedCommand.append("§a), ");
                }

                // If we're at the last command, shank the trailing comma for a clean end.
                if (i == 16)
                    formattedCommand.setLength(formattedCommand.length() - 2);

                // Add the formatted command to the list, and then clear the StringBuilder so we can re-use it.
                commandList.add(formattedCommand.toString());
                formattedCommand.setLength(0);
            }
        }

        // Print the formatted commands + aliases.
        int listSize = commandList.size();
        if (firstRun)
            printUnformattedMessage("--> §aSuccessfully registered a bunch of commands! See below.");
        else
            printUnformattedMessage("--> §aSuccessfully loaded a bunch of commands! See below.");

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
    public static String setupConfig(String callSource, String defaultAlias, Path checkPath, String mainPath)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                printUnformattedMessage("§eNo \"§6/" + callSource.toLowerCase() + "§e\" configuration file found, creating...");

                Files.copy(ConfigOperations.class.getResourceAsStream("/assets/" + callSource + ".conf"),
                        Paths.get(mainPath, callSource + ".conf"));

                loadConfig(callSource);
            }
            catch (IOException F)
            {
                printUnformattedMessage("§cConfig setup for command \"§4/" + callSource.toLowerCase()
                        + "§c\" failed! Please report this.");
                printUnformattedMessage("§cAdd any useful info you may have (operating system?). Stack trace:");
                F.printStackTrace();
            }

            return defaultAlias;
        }
        else
        {
            String alias = loadConfig(callSource);

            if (Objects.equals(alias, null))
            {
                printUnformattedMessage("§cError on \"§4/" + callSource.toLowerCase() +
                        "§c\", variable \"§4commandAlias§c\"! Check or regen this config.");
            }

            return alias;
        }
    }

    // An alternative to setupConfig for use with the main config.
    public static void setupPrimaryConfig(Path checkPath, String mainPath)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                printUnformattedMessage("§eNo primary configuration file found, creating...");

                Files.copy(ConfigOperations.class.getResourceAsStream("/assets/PixelUpgradeMain.conf"),
                        Paths.get(mainPath, "PixelUpgrade.conf"));

                loadConfig("PixelUpgrade");
            }
            catch (IOException F)
            {
                printUnformattedMessage("§cPrimary config setup has failed! Please report this.");
                printUnformattedMessage("§cAdd any useful info you may have (operating system?). Stack trace:");

                F.printStackTrace();
            }
        }
        else
            loadConfig("PixelUpgrade");
    }

    // Grabs a specified config, then loads all of the variables into the matching command.
    // Check the imports, toBooleanObject is imported as static to make things a bit easier to maintain.
    private static String loadConfig(String callSource)
    {
        CommentedConfigurationNode commandConfig;

        try
        {
            switch (callSource) // TODO: Added a new command? Update the switch list! Default should NEVER be called!
            {
                case "CheckEgg":
                {
                    commandConfig = PixelUpgrade.checkEggLoader.load();

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
                    commandConfig = PixelUpgrade.checkStatsLoader.load();

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
                    commandConfig = PixelUpgrade.checkTypesLoader.load();

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
                    commandConfig = PixelUpgrade.dittoFusionLoader.load();

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
                    commandConfig = PixelUpgrade.fixEVsLoader.load();

                    FixEVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    FixEVs.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return FixEVs.commandAlias;
                }
                case "FixLevel":
                {
                    commandConfig = PixelUpgrade.fixLevelLoader.load();

                    FixLevel.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    FixLevel.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return FixLevel.commandAlias;
                }
                case "ForceHatch":
                {
                    commandConfig = PixelUpgrade.forceHatchLoader.load();

                    ForceHatch.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ForceHatch.commandAlias;
                }
                case "ForceStats":
                {
                    commandConfig = PixelUpgrade.forceStatsLoader.load();

                    ForceStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ForceStats.commandAlias;
                }
                case "PixelUpgradeInfo":
                {
                    commandConfig = PixelUpgrade.puInfoLoader.load();

                    PixelUpgradeInfo.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    PixelUpgradeInfo.numLinesPerPage =
                            interpretInteger(commandConfig.getNode("numLinesPerPage").getString());

                    return PixelUpgradeInfo.commandAlias;
                }
                case "PixelUpgrade":
                {
                    commandConfig = PixelUpgrade.primaryConfigLoader.load();

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
                case "ResetCount":
                {
                    commandConfig = PixelUpgrade.resetCountLoader.load();

                    ResetCount.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return ResetCount.commandAlias;
                }
                case "ResetEVs":
                {
                    commandConfig = PixelUpgrade.resetEVsLoader.load();

                    ResetEVs.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    ResetEVs.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return ResetEVs.commandAlias;
                }
                case "ShowStats":
                {
                    commandConfig = PixelUpgrade.showStatsLoader.load();

                    ShowStats.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    ShowStats.cooldownInSeconds =
                            interpretInteger(commandConfig.getNode("cooldownInSeconds").getString());
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
                    commandConfig = PixelUpgrade.spawnDexLoader.load();

                    SpawnDex.commandAlias =
                            commandConfig.getNode("commandAlias").getString();

                    return SpawnDex.commandAlias;
                }
                case "SwitchGender":
                {
                    commandConfig = PixelUpgrade.switchGenderLoader.load();

                    SwitchGender.commandAlias =
                            commandConfig.getNode("commandAlias").getString();
                    SwitchGender.commandCost =
                            interpretInteger(commandConfig.getNode("commandCost").getString());

                    return SwitchGender.commandAlias;
                }
                case "UpgradeIVs":
                {
                    commandConfig = PixelUpgrade.upgradeIVsLoader.load();

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
