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
import rs.expand.pixelupgrade.configs.*;

import static rs.expand.pixelupgrade.PixelUpgrade.getInstance;

public class ReloadConfigs implements CommandExecutor
{
    // Set up a nice compact private logger specifically for showing command reloads.
    private static final String pName = "PU";
    private static final Logger pLog = LoggerFactory.getLogger(pName);

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        boolean showError = false;

        if (args.<String>getOne("config").isPresent())
        {
            String configString = args.<String>getOne("config").get(), path = getInstance().path;
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
            {
                // Reload the primary config and the info command config, and figure out the info alias.
                // We start printing stuff, here. If any warnings/errors pop up they'll be shown here.
                pLog.info("===========================================================================");
                pLog.info("--> §aReloading global settings and §2/pixelupgrade§a command listing...");
                PixelUpgradeMainConfig.getInstance().setupConfig(getInstance().primaryConfigPath, getInstance().primaryConfigLoader);
                String puInfoAlias = PixelUpgradeInfoConfig.getInstance().setupConfig(getInstance().puInfoPath, path, getInstance().puInfoLoader);
                pLog.info("--> §aReloading command-specific configs...");

                // Grab other aliases and get some configs. Similar to the above, any errors/warnings will be printed.
                String checkEggAlias = CheckEggConfig.getInstance().setupConfig(getInstance().checkEggPath,
                        path, getInstance().checkEggLoader);
                String checkStatsAlias = CheckStatsConfig.getInstance().setupConfig(getInstance().checkStatsPath,
                        path, getInstance().checkStatsLoader);
                String checkTypesAlias = CheckTypesConfig.getInstance().setupConfig(getInstance().checkTypesPath,
                        path, getInstance().checkTypesLoader);
                String dittoFusionAlias = DittoFusionConfig.getInstance().setupConfig(getInstance().dittoFusionPath,
                        path, getInstance().dittoFusionLoader);
                String fixEVsAlias = FixEVsConfig.getInstance().setupConfig(getInstance().fixEVsPath,
                        path, getInstance().fixEVsLoader);
                String fixLevelAlias = FixLevelConfig.getInstance().setupConfig(getInstance().fixLevelPath,
                        path, getInstance().fixLevelLoader);
                String forceHatchAlias = ForceHatchConfig.getInstance().setupConfig(getInstance().forceHatchPath,
                        path, getInstance().forceHatchLoader);
                String forceStatsAlias = ForceStatsConfig.getInstance().setupConfig(getInstance().forceStatsPath,
                        path, getInstance().forceStatsLoader);
                String resetCountAlias = ResetCountConfig.getInstance().setupConfig(getInstance().resetCountPath,
                        path, getInstance().resetCountLoader);
                String resetEVsAlias = ResetEVsConfig.getInstance().setupConfig(getInstance().resetEVsPath,
                        path, getInstance().resetEVsLoader);
                String switchGenderAlias = SwitchGenderConfig.getInstance().setupConfig(getInstance().switchGenderPath,
                        path, getInstance().switchGenderLoader);
                String showStatsAlias = ShowStatsConfig.getInstance().setupConfig(getInstance().showStatsPath,
                        path, getInstance().showStatsLoader);
                String upgradeIVsAlias = UpgradeIVsConfig.getInstance().setupConfig(getInstance().upgradeIVsPath,
                        path, getInstance().upgradeIVsLoader);

                // Do some initial setup for our formatted messages later on. We'll show three commands per line.
                ArrayList<String> commandList = new ArrayList<>();
                StringBuilder formattedCommand = new StringBuilder(), printableList = new StringBuilder();

                // Format our commands and aliases and add them to the lists that we'll print in a bit.
                for (int i = 1; i <= 15; i++)
                {
                    switch (i)
                    {
                        // Normal commands. If the alias is null (error returned), we pass the base command again instead.
                        // This prevents NPEs while also letting us hide commands by checking whether they've returned null.
                        case 1:
                            commandAlias = checkEggAlias;
                            commandString = "/checkegg";
                            break;
                        case 2:
                            commandAlias = checkStatsAlias;
                            commandString = "/checkstats";
                            break;
                        case 3:
                            commandAlias = checkTypesAlias;
                            commandString = "/checktypes";
                            break;
                        case 4:
                            commandAlias = dittoFusionAlias;
                            commandString = "/dittofusion";
                            break;
                        case 5:
                            commandAlias = fixEVsAlias;
                            commandString = "/fixevs";
                            break;
                        case 6:
                            commandAlias = fixLevelAlias;
                            commandString = "/fixlevel";
                            break;
                        case 7:
                            commandAlias = forceHatchAlias;
                            commandString = "/forcehatch";
                            break;
                        case 8:
                            commandAlias = forceStatsAlias;
                            commandString = "/forcestats";
                            break;
                        case 9:
                            commandAlias = puInfoAlias;
                            commandString = "/pixelupgrade";
                            break;
                        case 10:
                            commandAlias = "no alias";
                            commandString = "/pureload";
                            break;
                        case 11:
                            commandAlias = resetCountAlias;
                            commandString = "/resetcount";
                            break;
                        case 12:
                            commandAlias = resetEVsAlias;
                            commandString = "/resetevs";
                            break;
                        case 13:
                            commandAlias = switchGenderAlias;
                            commandString = "/switchgender";
                            break;
                        case 14:
                            commandAlias = showStatsAlias;
                            commandString = "/showstats";
                            break;
                        case 15:
                            commandAlias = upgradeIVsAlias;
                            commandString = "/upgradeivs";
                            break;
                    }

                    if (commandAlias != null)
                    {
                        // Format the command.
                        formattedCommand.append("§2");
                        formattedCommand.append(commandString);
                        if (!commandString.equals("/" + commandAlias) || commandAlias.equals("no alias"))
                        {
                            formattedCommand.append("§a (§2/");
                            formattedCommand.append(commandAlias.toLowerCase());
                            formattedCommand.append("§a), ");
                        }
                        else
                            formattedCommand.append("§a, ");

                        // If we're at the last command, shank the trailing comma for a clean end.
                        if (i == 15)
                            formattedCommand.setLength(formattedCommand.length() - 2);

                        // Add the formatted command to the list, and then clear the StringBuilder so we can re-use it.
                        commandList.add(formattedCommand.toString());
                        formattedCommand.setLength(0);
                    }
                }

                // Print the formatted commands + aliases.
                int listSize = commandList.size();
                pLog.info("--> §aSuccessfully reloaded a bunch of commands! See below.");

                for (int q = 1; q < listSize + 1; q++)
                {
                    printableList.append(commandList.get(q - 1));

                    if (q == listSize) // Are we on the last entry of the list? Exit.
                        pLog.info("    " + printableList);
                    else if (q % 3 == 0) // Is the loop number a multiple of 3? If so, we have three commands stocked up. Print!
                    {
                        pLog.info("    " + printableList);
                        printableList.setLength(0); // Wipe the list so we can re-use it for the next three commands.
                    }
                }

                pLog.info("===========================================================================");
            }
            else
            {
                switch (cappedConfigString)
                {
                    // Special.
                    case "MAINCONFIG": case "MAIN":
                        PixelUpgradeMainConfig.getInstance().setupConfig(getInstance().primaryConfigPath,
                                getInstance().primaryConfigLoader);
                        pLog.info("§aReloaded global config.");
                        break;
                    case "PIXELUPGRADEINFO": case "INFO":
                        commandAlias = PixelUpgradeInfoConfig.getInstance().setupConfig(getInstance().puInfoPath,
                                path, getInstance().puInfoLoader);
                        pLog.info("§aReloaded config for the §2command listing §a(§2/pixelupgrade§a), alias §2/" + commandAlias + "§a.");
                        break;

                    // Commands.
                    case "CHECKEGG":
                        commandAlias = CheckEggConfig.getInstance().setupConfig(getInstance().checkEggPath, 
                                path, getInstance().checkEggLoader);
                        pLog.info("§aReloaded config for command §2/checkegg§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "CHECKSTATS":
                        commandAlias = CheckStatsConfig.getInstance().setupConfig(getInstance().checkStatsPath, 
                                path, getInstance().checkStatsLoader);
                        pLog.info("§aReloaded config for command §2/checkstats§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "CHECKTYPES":
                        commandAlias = CheckTypesConfig.getInstance().setupConfig(getInstance().checkTypesPath, 
                                path, getInstance().checkTypesLoader);
                        pLog.info("§aReloaded config for command §2/checktypes§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "DITTOFUSION":
                        commandAlias = DittoFusionConfig.getInstance().setupConfig(getInstance().dittoFusionPath, 
                                path, getInstance().dittoFusionLoader);
                        pLog.info("§aReloaded config for command §2/dittofusion§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FIXEVS":
                        commandAlias = FixEVsConfig.getInstance().setupConfig(getInstance().fixEVsPath, 
                                path, getInstance().fixEVsLoader);
                        pLog.info("§aReloaded config for command §2/fixevs§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FIXLEVEL":
                        commandAlias = FixLevelConfig.getInstance().setupConfig(getInstance().fixLevelPath, 
                                path, getInstance().fixLevelLoader);
                        pLog.info("§aReloaded config for command §2/fixlevel§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FORCEHATCH":
                        commandAlias = ForceHatchConfig.getInstance().setupConfig(getInstance().forceHatchPath,
                                path, getInstance().forceHatchLoader);
                        pLog.info("§aReloaded config for command §2/forcehatch§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "FORCESTATS":
                        commandAlias = ForceStatsConfig.getInstance().setupConfig(getInstance().forceStatsPath,
                                path, getInstance().forceStatsLoader);
                        pLog.info("§aReloaded config for command §2/forcestats§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "RESETCOUNT":
                        commandAlias = ResetCountConfig.getInstance().setupConfig(getInstance().resetCountPath,
                                path, getInstance().resetCountLoader);
                        pLog.info("§aReloaded config for command §2/resetcount§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "RESETEVS":
                        commandAlias = ResetEVsConfig.getInstance().setupConfig(getInstance().resetEVsPath, 
                                path, getInstance().resetEVsLoader);
                        pLog.info("§aReloaded config for command §2/resetevs§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "SWITCHGENDER":
                        commandAlias = SwitchGenderConfig.getInstance().setupConfig(getInstance().switchGenderPath, 
                                path, getInstance().switchGenderLoader);
                        pLog.info("§aReloaded config for command §2/switchgender§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "SHOWSTATS":
                        commandAlias = ShowStatsConfig.getInstance().setupConfig(getInstance().showStatsPath, 
                                path, getInstance().showStatsLoader);
                        pLog.info("§aReloaded config for command §2/showstats§a, alias §2/" + commandAlias + "§a.");
                        break;
                    case "UPGRADEIVS":
                        commandAlias = UpgradeIVsConfig.getInstance().setupConfig(getInstance().upgradeIVsPath, 
                                path, getInstance().upgradeIVsLoader);
                        pLog.info("§aReloaded config for command §2/upgradeivs§a, alias §2/" + commandAlias + "§a.");
                        break;

                    // Input did not match any of the above, abort.
                    default:
                        showError = true;
                }
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
            // Read the debug logging level and apply it. All commands will refer to this.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
            {
                String modeString = PixelUpgradeMainConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

                if (modeString.matches("^[0-3]"))
                    PixelUpgrade.debugLevel = Integer.parseInt(modeString);
                else
                {
                    PixelUpgrade.log.info("§4PixelUpgrade // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
                    PixelUpgrade.log.info("§4PixelUpgrade // critical: §cLogging will be set to verbose mode (3) until this is resolved.");
                }
            }
            else
            {
                PixelUpgrade.log.info("§4PixelUpgrade // critical: §cConfig variable \"debugVerbosityMode\" could not be read!");
                PixelUpgrade.log.info("§4PixelUpgrade // critical: §cLogging will be set to verbose mode (3) until this is resolved.");
            }

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
