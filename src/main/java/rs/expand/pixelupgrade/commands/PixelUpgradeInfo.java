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

public class PixelUpgradeInfo implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
	    // [] = optional, {} = flag, <> = required, () = add comment here
        // Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.

        // HEAVILY WIP, PROBABLY WON'T WORK

		Player player = (Player) src;
		Boolean hasNoPermission = true;
        List<Text> permissionMessageList = new ArrayList<>();

        Boolean permCheckEgg = player.hasPermission("pixelupgrade.command.checkegg");
        Boolean permCheckEggOther = player.hasPermission("pixelupgrade.command.checkegg.other");
        Boolean permDittoFusion = player.hasPermission("pixelupgrade.command.dittofusion");
        Boolean permFixEVs = player.hasPermission("pixelupgrade.command.fixevs");
        Boolean permAdminForceHatch = player.hasPermission("pixelupgrade.command.admin.forcehatch");
        Boolean permAdminForceStats = player.hasPermission("pixelupgrade.command.admin.forcestats");
        Boolean permGetStats = player.hasPermission("pixelupgrade.command.getstats");
        Boolean permGetStatsOther = player.hasPermission("pixelupgrade.command.getstats.other");
        Boolean permResetEVs = player.hasPermission("pixelupgrade.command.resetevs");
        Boolean permUpgrade = player.hasPermission("pixelupgrade.command.upgradeivs");
        Boolean permWeakness = player.hasPermission("pixelupgrade.command.weakness");

        boolean checkEggEnabled = false;
        if (!CheckEggConfig.getInstance().getConfig().getNode("commandEnabled").isVirtual())
            checkEggEnabled = CheckEggConfig.getInstance().getConfig().getNode("commandEnabled").getBoolean();

        String header = "\u00A75================ \u00A7dPixelUpgrade commands \u00A75================";

        if (checkEggEnabled && permCheckEgg)
        {
            Integer commandCost = checkConfigInt(CheckEggConfig.class, "commandCost", false);

            if (commandCost == null)
                printToLog(0, "Could not read config for command \u00A74/checkegg\u00A7c, hiding command.");
            if (commandCost != 0)
            {
                if (permCheckEggOther)
                    permissionMessageList.add(Text.of("\u00A76/checkegg [optional target] <slot, 1-6> {confirm flag}"));
                else
                    permissionMessageList.add(Text.of("\u00A76/checkegg <slot> {confirm flag} \u00A77(no perms for target)"));

                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eCheck an egg to see what's inside! Costs \u00A76" + commandCost + "\u00A7e coins."));
            }
            else
            {
                if (permCheckEggOther)
                    permissionMessageList.add(Text.of("\u00A76/checkegg [optional target] <slot, 1-6>"));
                else
                    permissionMessageList.add(Text.of("\u00A76/checkegg <slot> \u00A77(no perms for target)"));

                permissionMessageList.add(Text.of("\u00A7f --> \u00A7eCheck an egg to see what's inside!"));
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
            permissionMessageList.add(Text.of("\u00A7f --> \u00A7eHatch eggs instantly, without any cooldowns!"));
            hasNoPermission = false;
        }
        if (permAdminForceStats)
        {
            permissionMessageList.add(Text.of("\u00A76/forcestats <slot, 1-6> <stat> <value> [force flag]"));
            permissionMessageList.add(Text.of("\u00A7f --> \u00A7eChange supported stats freely, or pass -f and go crazy."));
            hasNoPermission = false;
        }
        if (permGetStats)
        {
            permissionMessageList.add(Text.of("\u00A76/getstats or /gs (player name, optional) <slot, 1-6>"));
            permissionMessageList.add(Text.of("\u00A7f --> \u00A7eLists many of a Pok\u00E9mon's stats in one place."));
            hasNoPermission = false;
        }
        if (permResetEVs)
        {
            permissionMessageList.add(Text.of("\u00A76/resetevs <slot, 1-6> {confirmation flag]"));
            permissionMessageList.add(Text.of("\u00A7f --> \u00A7eResets all EVs down to zero, when confirmed."));
            hasNoPermission = false;
        }
        if (permUpgrade)
        {
            permissionMessageList.add(Text.of("\u00A76/upgrade <slot, 1-6> <IV type> [amount]"));
            permissionMessageList.add(Text.of("\u00A7f --> \u00A7eBuy upgrades for your Pok\u00E9mon's stats!"));
            hasNoPermission = false;
        }
        if (permWeakness)
        {
            permissionMessageList.add(Text.of("\u00A76/weakness <name of Pok\u00E9mon to check> {confirmation flag}"));
            permissionMessageList.add(Text.of("\u00A7f --> \u00A7eBuy upgrades for your Pok\u00E9mon's stats!"));
            hasNoPermission = false;
        }

        //TODO: Remove title color once Sponge bug is fixed. Written 22/5/2017. Messages should still look right once this gets fixed, as there's a fallback.
		PaginationList.builder()
		    .title(Text.of(TextColors.DARK_PURPLE, header))
            .contents(permissionMessageList)
            .padding(Text.of(TextColors.DARK_PURPLE, "="))
            .linesPerPage(16)
            .sendTo(player);

		return CommandResult.success();
	}

    private void printToLog(Integer debugNum, String inputString)
    {
        Integer debugVerbosityMode = checkConfigInt(PixelUpgradeInfoConfig.class, "debugVerbosityMode", true);

        if (debugVerbosityMode == null)
            debugVerbosityMode = 4;

        if (debugNum <= debugVerbosityMode)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74PU Command Helper // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76PU Command Helper // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73PU Command Helper // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72PU Command Helper // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt(Class configClass, String node, Boolean noMessageMode)
    {
        //WIP

        try
        {
            if (!configClass.getInstance().getConfig().getNode(node).isVirtual())
                return configClass.getInstance().getConfig().getNode(node).getInt();
            else if (noMessageMode)
                return null;
            else
            {
                PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
                return null;
            }
        }
        catch (Exception F)
        {
            PixelUpgrade.log.info("\u00A74PU Command Helper // critical: \u00A7c");
            return null;
        }
    }
}