package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.format.TextColors;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.*;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Command format helper! Use this format if you want to add your own stuff.
// [] = optional, {} = flag, <> = required, () = add comment here
// Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.

public class PixelUpgradeInfo implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!PixelUpgradeInfoConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = PixelUpgradeInfoConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("§4PUInfo // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4PUInfo // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

	public CommandResult execute(CommandSource src, CommandContext args)
	{
        Integer numLinesPerPage = checkConfigInt();

        // Check the command's debug verbosity mode, as set in the config.
        getVerbosityMode();

        if (numLinesPerPage == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
        {
            // Specific errors are already called earlier on -- this is tacked on to the end.
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            PixelUpgrade.log.info("§4PUInfo // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
        }
        else
        {
            Player player = (Player) src;
            String header = "§5================ §dPixelUpgrade commands §5================";
            String separator = FileSystems.getDefault().getSeparator();
            Boolean hasNoPermission = true;
            List<Text> permissionMessageList = new ArrayList<>();

            Boolean permCheckEgg = player.hasPermission("pixelupgrade.command.checkegg");
            Boolean permCheckEggOther = player.hasPermission("pixelupgrade.command.checkegg.other");
            Boolean permCheckStats = player.hasPermission("pixelupgrade.command.checkstats");
            Boolean permCheckStatsOther = player.hasPermission("pixelupgrade.command.checkstats.other");
            Boolean permCheckTypes = player.hasPermission("pixelupgrade.command.checktypes");
            Boolean permDittoFusion = player.hasPermission("pixelupgrade.command.dittofusion");
            Boolean permFixEVs = player.hasPermission("pixelupgrade.command.fixevs");
            Boolean permFixLevel = player.hasPermission("pixelupgrade.command.fixlevel");
            Boolean permAdminForceHatch = player.hasPermission("pixelupgrade.command.staff.forcehatch");
            Boolean permAdminForceStats = player.hasPermission("pixelupgrade.command.staff.forcestats");
            Boolean permReloadConfig = player.hasPermission("pixelupgrade.command.staff.reload");
            Boolean permResetCount = player.hasPermission("pixelupgrade.command.staff.resetcount");
            Boolean permResetEVs = player.hasPermission("pixelupgrade.command.resetevs");
            Boolean permSwitchGender = player.hasPermission("pixelupgrade.command.switchgender");
            Boolean permUpgradeIVs = player.hasPermission("pixelupgrade.command.upgradeivs");

            if (permCheckEgg)
            {
                Integer commandCost = checkCheckEggInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "CheckEgg.conf")))
                {
                    if (commandCost != null && CheckEggConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = CheckEggConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/checkegg §apermission found, adding helper to list.");

                        if (commandCost > 0)
                        {
                            if (permCheckEggOther)
                                permissionMessageList.add(Text.of("§6/" + alias + " [optional target] <slot> {confirm flag}"));
                            else
                                permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag} §7(no perms for target)"));
                        }
                        else
                        {
                            if (permCheckEggOther)
                                permissionMessageList.add(Text.of("§6/" + alias + " [optional target] <slot>"));
                            else
                                permissionMessageList.add(Text.of("§6/" + alias + " <slot> §7(no perms for target)"));
                        }

                        permissionMessageList.add(Text.of("§f --> §eCheck an egg to see what's inside."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/checkegg");
                }
                else printMalformedError("/checkegg");
            }

            if (permCheckStats)
            {
                Integer commandCost = checkCheckStatsInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "CheckStats.conf")))
                {
                    if (commandCost != null && CheckStatsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = CheckStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/checkstats §apermission found, adding helper to list.");

                        if (commandCost > 0)
                        {
                            if (permCheckStatsOther)
                                permissionMessageList.add(Text.of("§6/" + alias + " [optional target] <slot> {confirm flag}"));
                            else
                                permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag} §7(no perms for target)"));
                        }
                        else
                        {
                            if (permCheckStatsOther)
                                permissionMessageList.add(Text.of("§6/" + alias + " [optional target] <slot>"));
                            else
                                permissionMessageList.add(Text.of("§6/" + alias + " <slot> §7(no perms for target)"));
                        }

                        permissionMessageList.add(Text.of("§f --> §eLists a Pokémon's IVs, nature, size and more."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/checkstats");
                }
                else printMalformedError("/checkstats");
            }

            if (permCheckTypes)
            {
                Integer commandCost = checkCheckTypesInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "CheckTypes.conf")))
                {
                    if (commandCost != null && CheckTypesConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = CheckTypesConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/checktypes §apermission found, adding helper to list.");

                        if (commandCost != 0)
                            permissionMessageList.add(Text.of("§6/" + alias + " <Pokémon name/number> {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + alias + " <Pokémon name/number>"));

                        permissionMessageList.add(Text.of("§f --> §eSee any Pokémon's resistances, weaknesses and more."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/checktypes");
                }
                else printMalformedError("/checktypes");
            }

            if (permDittoFusion)
            {
                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "DittoFusion.conf")))
                {
                    if (DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/dittofusion §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <target slot> <sacrifice slot> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eSacrifice one Ditto to make another stronger, for a price."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/dittofusion");
                }
                else printMalformedError("/dittofusion");
            }

            if (permFixEVs)
            {
                Integer commandCost = checkFixEVsInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "FixEVs.conf")))
                {
                    if (commandCost != null && FixEVsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = FixEVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/fixevs §apermission found, adding helper to list.");

                        if (commandCost != 0)
                            permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + alias + " <slot>"));

                        permissionMessageList.add(Text.of("§f --> §eEVs above 252 are wasted. This will fix them!"));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/fixevs");
                }
                else printMalformedError("/fixevs");
            }

            if (permFixLevel)
            {
                Integer commandCost = checkFixLevelInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "FixLevel.conf")))
                {
                    if (commandCost != null && FixLevelConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = FixLevelConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/fixlevel §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eWant to lower your level to get more EVs? Try this."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/fixlevel");
                }
                else printMalformedError("/fixlevel");
            }

            if (permAdminForceHatch)
            {
                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "ForceHatch.conf")))
                {
                    if (ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/forcehatch §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " (optional target) <slot>"));
                        permissionMessageList.add(Text.of("§f --> §eHatch any eggs instantly. Supports remote players!"));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/forcehatch");
                }
                else printMalformedError("/forcehatch");
            }

            if (permAdminForceStats)
            {
                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "ForceStats.conf")))
                {
                    if (ForceStatsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ForceStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/forcestats §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot> <stat> <value> {force flag}"));
                        permissionMessageList.add(Text.of("§f --> §eChange supported stats, or pass -f and go crazy."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/forcestats");
                }
                else printMalformedError("/forcestats");
            }

            if (permReloadConfig)
            {
                printToLog(3, "§2/pureload §apermission found, adding helper to list.");
                permissionMessageList.add(Text.of("§6/pureload <config>"));
                permissionMessageList.add(Text.of("§f --> §eReload one or more of the configs on the fly."));
                hasNoPermission = false;
            }

            if (permResetCount)
            {
                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "ResetCount.conf")))
                {
                    if (ResetCountConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ResetCountConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/resetcount §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot, 1-6> <count> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eIf you want a Pokémon to upgrade further, use this."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/resetcount");
                }
                else printMalformedError("/resetcount");
            }

            if (permResetEVs)
            {
                Integer commandCost = checkResetEVsInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "ResetEVs.conf")))
                {
                    if (commandCost != null && ResetEVsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ResetEVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/resetevs §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eNot happy with your EV spread? This wipes all EVs."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/resetevs");
                }
                else printMalformedError("/resetevs");
            }

            if (permSwitchGender)
            {
                Integer commandCost = checkSwitchGenderInt();

                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "SwitchGender.conf")))
                {
                    if (commandCost != null && SwitchGenderConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = SwitchGenderConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/switchgender §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eWant to change a Pokémon's gender? Try this."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/switchgender");
                }
                else printMalformedError("/switchgender");
            }

            if (permUpgradeIVs)
            {
                if (Files.exists(Paths.get("config" + separator + "PixelUpgrade" + separator + "UpgradeIVs.conf")))
                {
                    if (UpgradeIVsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = UpgradeIVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printToLog(3, "§2/upgradeivs §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot> <IV type> [optional amount] {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eBuy upgrades for your Pokémon's IVs."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/upgradeivs");
                }
                else printMalformedError("/upgradeivs");
            }

            if (hasNoPermission)
            {
                permissionMessageList.add(Text.of("§cYou have no permissions for any PixelUpgrade commands."));
                permissionMessageList.add(Text.of("§cPlease contact staff if you believe this to be in error."));
            }

            player.sendMessage(Text.of("numLinesPerPage: " + numLinesPerPage));

            // TODO: Remove title color once Sponge bug is fixed. Written 22/5/2017, last checked 24/6/2017.
            // Messages should still look right once this gets fixed, as there's a fallback.
            PaginationList.builder()
                    .title(Text.of(TextColors.DARK_PURPLE, header))
                    .contents(permissionMessageList)
                    .padding(Text.of(TextColors.DARK_PURPLE, "="))
                    .linesPerPage(numLinesPerPage)
                    .sendTo(player);

        }
        return CommandResult.success();
	}

	private void printMalformedError(String command)
    {
        // There's a line in the default config saying debugNum "1" means malformed configs here.
        printToLog(1, "Malformed config on §6" + command + "§e, hiding from list.");
    }

    private void printToLog(Integer debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4PUInfo // critical: §c" + inputString);
            else // if (debugNum == 1)
                PixelUpgrade.log.info("§6PUInfo // important: §e" + inputString);
            /* else if (debugNum == 2)
                PixelUpgrade.log.info("§3PUInfo // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2PUInfo // debug: §a" + inputString); */
        }
    }

    private Integer checkConfigInt()
    {
        if (!PixelUpgradeInfoConfig.getInstance().getConfig().getNode("numLinesPerPage").isVirtual())
        {
            int numLinesPerPage = PixelUpgradeInfoConfig.getInstance().getConfig().getNode("numLinesPerPage").getInt();

            if (numLinesPerPage < 2 || numLinesPerPage > 50)
            {
                PixelUpgrade.log.info("§4PUInfo // critical: §cConfig variable \"numLinesPerPage\" is out of bounds. Valid range: 2-50");
                return null;
            }
            else
                return numLinesPerPage;
        }
        else
        {
            PixelUpgrade.log.info("§4PUInfo // critical: §cCould not parse config variable \"numLinesPerPage\"!");
            return null;
        }
    }

    private Integer checkCheckEggInt()
    {
        if (!CheckEggConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return CheckEggConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }

    private Integer checkCheckStatsInt()
    {
        if (!CheckStatsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return CheckStatsConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }

    private Integer checkCheckTypesInt()
    {
        if (!CheckTypesConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return CheckTypesConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }

    private Integer checkFixEVsInt()
    {
        if (!FixEVsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return FixEVsConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }

    private Integer checkFixLevelInt()
    {
        if (!FixLevelConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return FixLevelConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }

    private Integer checkResetEVsInt()
    {
        if (!ResetEVsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return ResetEVsConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }

    private Integer checkSwitchGenderInt()
    {
        if (!SwitchGenderConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return SwitchGenderConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
            return null;
    }
}