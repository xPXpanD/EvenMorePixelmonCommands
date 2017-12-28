package rs.expand.pixelupgrade.commands;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

import static rs.expand.pixelupgrade.configs.CheckEggConfig.getInstance;

// Command format helper! Use this format if you want to add your own stuff.
// [] = optional, {} = flag, <> = required, () = add comment here
// Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.

public class PixelUpgradeInfo implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
	{
	    // Figure out how many helper lines we'll show per page. Set to 0 (unreachable) on error, to be handled later.
        // No need for this to be on a separate method, as we don't use the value outside of command execution.
        int numLinesPerPage;

        if (!PixelUpgradeInfoConfig.getInstance().getConfig().getNode("numLinesPerPage").isVirtual())
        {
            numLinesPerPage = PixelUpgradeInfoConfig.getInstance().getConfig().getNode("numLinesPerPage").getInt();

            if (numLinesPerPage < 2 || numLinesPerPage > 50)
            {
                numLinesPerPage = 0; // Error!
                PixelUpgrade.log.info("§4PUInfo // critical: §cInvalid value on config variable \"numLinesPerPage\"! Valid range: 2-50");
            }
        }
        else
        {
            numLinesPerPage = 0; // Error!
            PixelUpgrade.log.info("§4PUInfo // critical: §cConfig variable \"numLinesPerPage\" could not be found!");
        }

        if (numLinesPerPage == 0)
        {
            // Specific errors are already called earlier on -- this is tacked on to the end.
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            PixelUpgrade.log.info("§4PUInfo // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
        }
        else
        {
            Player player = (Player) src;
            List<Text> permissionMessageList = new ArrayList<>();
            String path = PixelUpgrade.getInstance().path;
            boolean hasNoPermission = true;

            Boolean permCheckEgg = player.hasPermission("pixelupgrade.command.checkegg");
            Boolean permCheckEggOther = player.hasPermission("pixelupgrade.command.other.checkegg");
            Boolean permCheckStats = player.hasPermission("pixelupgrade.command.checkstats");
            Boolean permCheckStatsOther = player.hasPermission("pixelupgrade.command.other.checkstats");
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
            Boolean permShowStats = player.hasPermission("pixelupgrade.command.showstats");
            Boolean permUpgradeIVs = player.hasPermission("pixelupgrade.command.upgradeivs");

            if (permCheckEgg)
            {
                Integer commandCost = getConfigInt("CheckEgg");

                if (Files.exists(Paths.get(path + "CheckEgg.conf")))
                {
                    if (commandCost != null && getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/checkegg §apermission found, adding helper to list.");

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
                Integer commandCost = getConfigInt("CheckStats");

                if (Files.exists(Paths.get(path + "CheckStats.conf")))
                {
                    if (commandCost != null && CheckStatsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = CheckStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/checkstats §apermission found, adding helper to list.");

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
                Integer commandCost = getConfigInt("CheckTypes");

                if (Files.exists(Paths.get(path + "CheckTypes.conf")))
                {
                    if (commandCost != null && CheckTypesConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = CheckTypesConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/checktypes §apermission found, adding helper to list.");

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
                if (Files.exists(Paths.get(path + "DittoFusion.conf")))
                {
                    if (DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/dittofusion §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <target slot> <sacrifice slot> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eSacrifice one Ditto to improve another, for a price..."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/dittofusion");
                }
                else printMalformedError("/dittofusion");
            }

            if (permFixEVs)
            {
                Integer commandCost = getConfigInt("FixEVs");

                if (Files.exists(Paths.get(path + "FixEVs.conf")))
                {
                    if (commandCost != null && FixEVsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = FixEVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/fixevs §apermission found, adding helper to list.");

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
                Integer commandCost = getConfigInt("FixLevel");

                if (Files.exists(Paths.get(path + "FixLevel.conf")))
                {
                    if (commandCost != null && FixLevelConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = FixLevelConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/fixlevel §apermission found, adding helper to list.");

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
                if (Files.exists(Paths.get(path + "ForceHatch.conf")))
                {
                    if (ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/forcehatch §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " [optional target] <slot>"));
                        permissionMessageList.add(Text.of("§f --> §eHatch any eggs instantly. Supports remote players!"));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/forcehatch");
                }
                else printMalformedError("/forcehatch");
            }

            if (permAdminForceStats)
            {
                if (Files.exists(Paths.get(path + "ForceStats.conf")))
                {
                    if (ForceStatsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ForceStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/forcestats §apermission found, adding helper to list.");

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
                printDebug("§2/pureload §apermission found, adding helper to list.");
                permissionMessageList.add(Text.of("§6/pureload <config>"));
                permissionMessageList.add(Text.of("§f --> §eReload one or more of the configs on the fly."));
                hasNoPermission = false;
            }

            if (permResetCount)
            {
                if (Files.exists(Paths.get(path + "ResetCount.conf")))
                {
                    if (ResetCountConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ResetCountConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/resetcount §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot, 1-6> <count> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eWant to upgrade further? Reset counters with this."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/resetcount");
                }
                else printMalformedError("/resetcount");
            }

            if (permResetEVs)
            {
                Integer commandCost = getConfigInt("ResetEVs");

                if (Files.exists(Paths.get(path + "ResetEVs.conf")))
                {
                    if (commandCost != null && ResetEVsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ResetEVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/resetevs §apermission found, adding helper to list.");

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
                Integer commandCost = getConfigInt("SwitchGender");

                if (Files.exists(Paths.get(path + "SwitchGender.conf")))
                {
                    if (commandCost != null && SwitchGenderConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = SwitchGenderConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/switchgender §apermission found, adding helper to list.");

                        permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag}"));
                        permissionMessageList.add(Text.of("§f --> §eWant to change a Pokémon's gender? Try this."));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/switchgender");
                }
                else printMalformedError("/switchgender");
            }

            if (permShowStats)
            {
                Integer commandCost = getConfigInt("ShowStats");

                if (Files.exists(Paths.get(path + "ShowStats.conf")))
                {
                    if (commandCost != null && ShowStatsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = ShowStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/showstats §apermission found, adding helper to list.");

                        if (commandCost != 0)
                            permissionMessageList.add(Text.of("§6/" + alias + " <slot> {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + alias + " <slot>"));

                        permissionMessageList.add(Text.of("§f --> §eCaught something special? Show it off!"));
                        hasNoPermission = false;
                    }
                    else printMalformedError("/showstats");
                }
                else printMalformedError("/showstats");
            }

            if (permUpgradeIVs)
            {
                if (Files.exists(Paths.get(path + "UpgradeIVs.conf")))
                {
                    if (UpgradeIVsConfig.getInstance().getConfig().getNode("commandAlias").getString() != null)
                    {
                        String alias = UpgradeIVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
                        printDebug("§2/upgradeivs §apermission found, adding helper to list.");

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

            PaginationList.builder()
                    .title(Text.of(TextColors.DARK_PURPLE, "§dPixelUpgrade commands"))
                    .contents(permissionMessageList)
                    .padding(Text.of(TextColors.DARK_PURPLE, "="))
                    .linesPerPage(numLinesPerPage)
                    .sendTo(player);
        }
        return CommandResult.success();
	}

	private void printMalformedError(String command)
    {
        PixelUpgrade.log.info("§3PUInfo // notice: §bMalformed config on §3" + command + "§b, hiding from list.");
    }

    private void printDebug(String inputString)
    {
        PixelUpgrade.log.info("§2PUInfo // debug: §a" + inputString);
    }

    private Integer getConfigInt(String config)
    {
        switch (config)
        {
            case "CheckEgg":
                if (!CheckEggConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return CheckEggConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "CheckStats":
                if (!CheckStatsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return CheckStatsConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "CheckTypes":
                if (!CheckTypesConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return CheckTypesConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "FixEVs":
                if (!FixEVsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return FixEVsConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "FixLevel":
                if (!FixLevelConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return FixLevelConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "ResetEVs":
                if (!ResetEVsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return ResetEVsConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "SwitchGender":
                if (!SwitchGenderConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return SwitchGenderConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;
            case "ShowStats":
                if (!ShowStatsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                    return ShowStatsConfig.getInstance().getConfig().getNode("commandCost").getInt();
                else
                    return null;

            default:
                PixelUpgrade.log.info("§6PUInfo // warning: §eCouldn't figure out what config to read. Please report this!");
                return null;
        }
    }
}