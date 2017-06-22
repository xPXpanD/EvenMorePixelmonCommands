package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
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
                PixelUpgrade.log.info("\u00A74PUInfo // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74PUInfo // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
        Integer numLinesPerPage = checkConfigInt();

        // Check the command's debug verbosity mode, as set in the config.
        getVerbosityMode();

        if (debugLevel == null || debugLevel >= 4 || debugLevel < 0)
        {
            // Specific errors are already called earlier on -- this is tacked on to the end.
            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
            PixelUpgrade.log.info("\u00A74PUInfo // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pixelupgrade reload\\u00A7c.");
        }
        else
        {
            Player player = (Player) src;
            String header = "\u00A75================ \u00A7dPixelUpgrade commands \u00A75================";
            Boolean hasNoPermission = true;
            List<Text> permissionMessageList = new ArrayList<>();

            Boolean permCheckEgg = player.hasPermission("pixelupgrade.command.checkegg");
            Boolean permCheckEggOther = player.hasPermission("pixelupgrade.command.checkegg.other");
            Boolean permDittoFusion = player.hasPermission("pixelupgrade.command.dittofusion");
            Boolean permFixEVs = player.hasPermission("pixelupgrade.command.fixevs");
            Boolean permAdminForceHatch = player.hasPermission("pixelupgrade.command.admin.forcehatch");
            Boolean permAdminForceStats = player.hasPermission("pixelupgrade.command.admin.forcestats");
            Boolean permCheckStats = player.hasPermission("pixelupgrade.command.checkstats");
            Boolean permCheckStatsOther = player.hasPermission("pixelupgrade.command.checkstats.other");
            Boolean permResetEVs = player.hasPermission("pixelupgrade.command.resetevs");
            Boolean permUpgradeIVs = player.hasPermission("pixelupgrade.command.upgradeivs");
            Boolean permCheckTypes = player.hasPermission("pixelupgrade.command.checktypes");


            if (permCheckEgg)
            {
                Integer commandCost = checkCheckEggInt();

                if (commandCost == null)
                    printToLog(0, "Found malformed config on command \u00A74/checkegg\u00A7c, hiding from list.");
                else
                {
                    hasNoPermission = false;

                    if (commandCost != 0)
                    {
                        if (permCheckEggOther)
                            permissionMessageList.add(Text.of("\u00A76/checkegg [optional target] <slot, 1-6> {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("\u00A76/checkegg <slot> {confirm flag} \u00A77(no perms for target)"));

                        permissionMessageList.add(Text.of("\u00A7f --> \u00A7eCheck an egg to see what's inside. Costs \u00A76" + commandCost + "\u00A7e coins."));
                    }
                    else
                    {
                        if (permCheckEggOther)
                            permissionMessageList.add(Text.of("\u00A76/checkegg [optional target] <slot, 1-6>"));
                        else
                            permissionMessageList.add(Text.of("\u00A76/checkegg <slot> \u00A77(no perms for target)"));

                        permissionMessageList.add(Text.of("\u00A7f --> \u00A7eCheck an egg to see what's inside."));
                    }
                }
            }

            if (permDittoFusion)
            {
                permissionMessageList.add(Text.of("\u00A76/fuse <target slot> <sacrifice slot> [confirmation flag]"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eSacrifice one Ditto to make another stronger, for a price."));
                hasNoPermission = false;
            }
            if (permFixEVs)
            {
                permissionMessageList.add(Text.of("\u00A76/fixevs <slot, 1-6>"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eEVs above 252 are wasted. This command fixes them!"));
                hasNoPermission = false;
            }
            if (permAdminForceHatch)
            {
                permissionMessageList.add(Text.of("\u00A76/forcehatch <slot or target player> (slot, 1-6, optional)"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eHatch eggs instantly, without any cooldowns."));
                hasNoPermission = false;
            }
            if (permAdminForceStats)
            {
                permissionMessageList.add(Text.of("\u00A76/forcestats <slot, 1-6> <stat> <value> [force flag]"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eChange supported stats freely, or pass -f and go crazy."));
                hasNoPermission = false;
            }
            if (permCheckStats)
            {
                permissionMessageList.add(Text.of("\u00A76/checkstats or /stats (player name, optional) <slot, 1-6>"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eLists many of a Pok\u00E9mon's stats in one place."));
                hasNoPermission = false;
            }
            if (permResetEVs)
            {
                permissionMessageList.add(Text.of("\u00A76/resetevs <slot, 1-6> {confirmation flag]"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eResets all EVs down to zero, when confirmed."));
                hasNoPermission = false;
            }
            if (permUpgradeIVs)
            {
                permissionMessageList.add(Text.of("\u00A76/upgradeivs <slot, 1-6> <IV type> [amount]"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eBuy upgrades for your Pok\u00E9mon's stats."));
                hasNoPermission = false;
            }
            if (permCheckTypes)
            {
                permissionMessageList.add(Text.of("\u00A76/weakness <name of Pok\u00E9mon to check> {confirmation flag}"));
                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eCheck a Pok\u00E9mon's type resistances/weaknesses."));
                hasNoPermission = false;
            }

            //TODO: Remove title color once Sponge bug is fixed. Written 22/5/2017. Messages should still look right once this gets fixed, as there's a fallback.
            PaginationList.builder()
                    .title(Text.of(TextColors.DARK_PURPLE, header))
                    .contents(permissionMessageList)
                    .padding(Text.of(TextColors.DARK_PURPLE, "="))
                    .linesPerPage(numLinesPerPage)
                    .sendTo(player);

        }
        return CommandResult.success();
	}

    private void printToLog(Integer debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74PUInfo // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76PUInfo // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73PUInfo // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72PUInfo // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt()
    {
        if (!PixelUpgradeInfoConfig.getInstance().getConfig().getNode("numLinesPerPage").isVirtual())
        {
            int numLinesPerPage = PixelUpgradeInfoConfig.getInstance().getConfig().getNode("numLinesPerPage").getInt();

            if (numLinesPerPage < 2 || numLinesPerPage > 50)
            {
                PixelUpgrade.log.info("\u00A74PUInfo // critical: \u00A7cConfig variable \"numLinesPerPage\" is out of bounds. Valid range: 2-50");
                return null;
            }
            else
                return numLinesPerPage;
        }
        else
        {
            PixelUpgrade.log.info("\u00A74PUInfo // critical: \u00A7cCould not parse config variable \"numLinesPerPage\"!");
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
}