package rs.expand.pixelupgrade.commands;

// Remote imports.
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

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;

// Command format helper! Use this format if you want to add your own stuff.
// [] = optional, {} = flag, <> = required, () = add comment here
// Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.

public class PixelUpgradeInfo implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer numLinesPerPage;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printFormattedMessage("PU Info", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
	{
	    // Validate the data we get from the command's main config.
        ArrayList<String> nativeErrorArray = new ArrayList<>();
        if (commandAlias == null)
            nativeErrorArray.add("commandAlias");
        if (numLinesPerPage == null)
            nativeErrorArray.add("numLinesPerPage");

        if (!nativeErrorArray.isEmpty() || numLinesPerPage <= 0)
        {
            CommonMethods.printNodeError("PUInfo", nativeErrorArray, 1);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else
        {
            List<Text> permissionMessageList = new ArrayList<>();
            boolean hasNoPermission = true, usedFromConsole = false;
            boolean permCheckEgg = false, permCheckEggOther = false, permCheckStats = false, permCheckStatsOther = false;
            boolean permCheckTypes = false, permDittoFusion = false, permFixEVs = false, permFixLevel = false;
            boolean permForceHatch = false, permForceStats = false, permPixelHeal = false, permPixelSell = false;
            boolean permReloadConfig = false, permResetCount = false, permResetEVs = false, permSwitchGender = false;
            boolean permShowStats = false, permUpgradeIVs = false;

            if (src instanceof Player)
            {
                Player player = (Player) src;

                permCheckEgg = player.hasPermission("pixelupgrade.command.checkegg");
                permCheckEggOther = player.hasPermission("pixelupgrade.command.other.checkegg");
                permCheckStats = player.hasPermission("pixelupgrade.command.checkstats");
                permCheckStatsOther = player.hasPermission("pixelupgrade.command.other.checkstats");
                permCheckTypes = player.hasPermission("pixelupgrade.command.checktypes");
                permDittoFusion = player.hasPermission("pixelupgrade.command.dittofusion");
                permFixEVs = player.hasPermission("pixelupgrade.command.fixevs");
                permFixLevel = player.hasPermission("pixelupgrade.command.fixlevel");
                permForceHatch = player.hasPermission("pixelupgrade.command.staff.forcehatch");
                permForceStats = player.hasPermission("pixelupgrade.command.staff.forcestats");
                permPixelHeal = player.hasPermission("pixelupgrade.command.pixelheal");
                permPixelSell = player.hasPermission("pixelupgrade.command.pixelsell");
                permReloadConfig = player.hasPermission("pixelupgrade.command.staff.reload");
                permResetCount = player.hasPermission("pixelupgrade.command.staff.resetcount");
                permResetEVs = player.hasPermission("pixelupgrade.command.resetevs");
                permSwitchGender = player.hasPermission("pixelupgrade.command.switchgender");
                permShowStats = player.hasPermission("pixelupgrade.command.showstats");
                permUpgradeIVs = player.hasPermission("pixelupgrade.command.upgradeivs");
            }
            else
                usedFromConsole = true;

            if (usedFromConsole || permCheckEgg)
            {
                printToLog(2, "§5/checkegg §dpermission found, trying to add helpers to list.");

                if (CheckEgg.commandCost != null && CheckEgg.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    if (CheckEgg.commandCost > 0)
                    {
                        if (usedFromConsole || permCheckEggOther)
                            permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                                    " [optional target] <slot> {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                                    " <slot> {confirm flag} §7(no perms for target)"));
                    }
                    else
                    {
                        if (usedFromConsole || permCheckEggOther)
                            permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                                    " [optional target] <slot>"));
                        else
                            permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                                    " <slot> §7(no perms for target)"));
                    }

                    permissionMessageList.add(Text.of("§f --> §eCheck an egg to see what's inside."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/checkegg §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permCheckStats)
            {
                printToLog(2, "§5/checkstats §dpermission found, trying to add helpers to list.");

                if (CheckStats.commandCost != null && CheckStats.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    if (CheckStats.commandCost > 0)
                    {
                        if (usedFromConsole || permCheckStatsOther)
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " [optional target] <slot> {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " <slot> {confirm flag} §7(no perms for target)"));
                    }
                    else
                    {
                        if (usedFromConsole || permCheckStatsOther)
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " [optional target] <slot>"));
                        else
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " <slot> §7(no perms for target)"));
                    }

                    permissionMessageList.add(Text.of("§f --> §eLists a Pokémon's IVs, nature, size and more."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/checkstats §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permCheckTypes)
            {
                printToLog(2, "§5/checktypes §dpermission found, trying to add helpers to list.");

                if (CheckTypes.commandCost != null && CheckTypes.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    if (CheckTypes.commandCost != 0)
                        permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                " <Pokémon name/number> {confirm flag}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                " <Pokémon name/number>"));

                    permissionMessageList.add(Text.of("§f --> §eSee any Pokémon's resistances, weaknesses and more."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/checktypes §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permDittoFusion)
            {
                printToLog(2, "§5/dittofusion §dpermission found, trying to add helpers to list.");

                if (DittoFusion.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + DittoFusion.commandAlias +
                            " <target slot> <sacrifice slot> {confirm flag}"));

                    permissionMessageList.add(Text.of("§f --> §eSacrifice one Ditto to improve another, for a price..."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/dittofusion §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permFixEVs)
            {
                printToLog(2, "§5/fixevs §dpermission found, trying to add helpers to list.");

                if (FixEVs.commandCost != null && FixEVs.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    if (FixEVs.commandCost != 0)
                        permissionMessageList.add(Text.of("§6/" + FixEVs.commandAlias + " <slot> {confirm flag}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + FixEVs.commandAlias + " <slot>"));

                    permissionMessageList.add(Text.of("§f --> §eEVs above 252 are wasted. This will fix them!"));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/fixevs §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permFixLevel)
            {
                printToLog(2, "§5/fixlevel §dpermission found, trying to add helpers to list.");

                if (FixLevel.commandCost != null && FixLevel.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + FixLevel.commandAlias + " <slot> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f --> §eWant to lower your level to get more EVs? Try this."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/fixlevel §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permForceHatch)
            {
                printToLog(2, "§5/forcehatch §dpermission found, trying to add helpers to list.");

                if (ForceHatch.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + ForceHatch.commandAlias + " [optional target] <slot>"));
                    permissionMessageList.add(Text.of("§f --> §eHatch any eggs instantly. Supports remote players!"));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/forcehatch §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permForceStats)
            {
                printToLog(2, "§5/forcestats §dpermission found, trying to add helpers to list.");

                if (ForceStats.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + ForceHatch.commandAlias + " <slot> <stat> <value> {force flag}"));
                    permissionMessageList.add(Text.of("§f --> §eChange supported stats, or pass -f and go crazy."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/forcestats §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permReloadConfig)
            {
                printToLog(2, "§5/pureload §dpermission found, adding helpers to list.");

                permissionMessageList.add(Text.of("§6/pureload <config>"));
                permissionMessageList.add(Text.of("§f --> §eReload one or more of the configs on the fly."));
                hasNoPermission = false;
            }

            if (usedFromConsole || permResetCount)
            {
                printToLog(2, "§5/resetcount §dpermission found, trying to add helpers to list.");

                if (ResetCount.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + ResetCount.commandAlias + " <slot, 1-6> <count> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f --> §eWant to upgrade further? Reset counters with this."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/resetcount §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permResetEVs)
            {
                printToLog(2, "§5/resetevs §dpermission found, trying to add helpers to list.");

                if (ResetEVs.commandCost != null && ResetEVs.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + ResetEVs.commandAlias + " <slot> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f --> §eNot happy with your EV spread? This wipes all EVs."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/resetevs §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permSwitchGender)
            {
                printToLog(2, "§5/switchgender §dpermission found, trying to add helpers to list.");

                if (SwitchGender.commandCost != null && SwitchGender.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + SwitchGender.commandAlias + " <slot> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f --> §eWant to change a Pokémon's gender? Try this."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/switchgender §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permShowStats)
            {
                printToLog(2, "§5/showstats §dpermission found, trying to add helpers to list.");

                if (ShowStats.commandCost != null && ShowStats.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    if (ShowStats.commandCost != 0)
                        permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot> {confirm flag}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot>"));

                    permissionMessageList.add(Text.of("§f --> §eCaught something special? Show it off!"));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/showstats §bhas a malformed config, hiding from list.");
            }

            if (usedFromConsole || permUpgradeIVs)
            {
                printToLog(2, "§5/upgradeivs §dpermission found, trying to add helpers to list.");

                if (UpgradeIVs.commandAlias != null)
                {
                    printToLog(2, "Valid config found. Printing helpers for this command!");

                    permissionMessageList.add(Text.of("§6/" + UpgradeIVs.commandAlias + " <slot> <IV type> [optional amount] {confirm flag}"));
                    permissionMessageList.add(Text.of("§f --> §eBuy upgrades for your Pokémon's IVs."));
                    hasNoPermission = false;
                }
                else printToLog(1, "§3/upgradeivs §bhas a malformed config, hiding from list.");
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
                    .sendTo(src); // TODO: Check.
        }
        return CommandResult.success();
	}
}