// The command listing. Only shows console-accessible commands if used from there.
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
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.CommonMethods;

// Command format helper! Use this format if you want to add your own stuff.
// [] = optional, {} = flag, <> = required, () = add comment here
// Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.

public class PixelUpgradeInfo implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes.
    public static String commandAlias;
    public static Integer numLinesPerPage;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("PU Info", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
	{
	    // Are we running from the console? Let's tell our code that. If "src" is not a Player, this becomes true.
        boolean calledRemotely = !(src instanceof Player);

	    if (calledRemotely)
        {
            CommonMethods.printDebugMessage("PU Info", 1,
                    "Called by console, starting. Omitting debug messages for clarity.");
        }
        else
            printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

        // Validate the data we get from the command's main config. Revert to safe values if necessary.
        List<Text> permissionMessageList = new ArrayList<>();
        boolean gotConfigError = false;
        int sanitizedNumLinesPerPage;

        if (commandAlias == null)
        {
            printToLog(0, "Could not read node \"§4commandAlias§c\".");
            gotConfigError = true;
        }

        if (numLinesPerPage == null)
        {
            printToLog(0, "Could not read node \"§4numLinesPerPage§c\".");
            gotConfigError = true;
            sanitizedNumLinesPerPage = 20;
        }
        else
            sanitizedNumLinesPerPage = numLinesPerPage;

        // We got an error. Safe defaults were already loaded earlier, and specific errors printed.
        if (gotConfigError)
            printToLog(0, "We'll proceed with safe defaults. Please fix this.");

        // Check if an economy plugin was found during startup.
        if (!calledRemotely)
        {
            if (PixelUpgrade.economyEnabled)
                printToLog(2, "Found an economy, showing §2/dittofusion §aand §2/upgradeivs§a.");
            else
                printToLog(2, "No economy was found, hiding §2/dittofusion §aand §2/upgradeivs§a.");
        }

        if (src.hasPermission("pixelupgrade.command.checkegg"))
        {
            if (CheckEgg.commandCost != null && CheckEgg.commandAlias != null)
            {
                if (CheckEgg.commandCost > 0)
                {
                    permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                            " <slot, 1-6> {confirm flag} §7(no perms for target)"));
                }
                else
                {
                    permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                            " <slot, 1-6> §7(no perms for target)"));
                }

                permissionMessageList.add(Text.of("§f --> §eCheck an egg to see what's inside."));
            }
            else printToLog(1, "§3/checkegg §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.checkstats"))
        {
            if (CheckStats.commandCost != null && CheckStats.commandAlias != null && CheckStats.showTeamWhenSlotEmpty != null)
            {
                if (calledRemotely)
                    permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias + " <target> [slot? 1-6]"));
                else
                {
                    String flagString;
                    if (CheckStats.commandCost != 0)
                        flagString = " {confirm flag}";
                    else
                        flagString = "";

                    if (src.hasPermission("pixelupgrade.command.other.checkstats") && CheckStats.showTeamWhenSlotEmpty)
                        src.sendMessage(Text.of("§6/" + commandAlias + " [target?] [slot? 1-6]" + flagString));
                    else if (src.hasPermission("pixelupgrade.command.other.checkstats"))
                        src.sendMessage(Text.of("§6/" + commandAlias + " [target?] <slot, 1-6>" + flagString));
                    else
                        src.sendMessage(Text.of("§6/" + commandAlias + " <slot, 1-6>" + flagString + " §7(no perms for target)"));
                }

                permissionMessageList.add(Text.of("§f --> §eLists a Pokémon's IVs, nature, size and more."));
            }
            else printToLog(1, "§3/checkstats §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.checktypes"))
        {
            if (CheckTypes.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + CheckTypes.commandAlias + " <Pokémon name/number>"));
                permissionMessageList.add(Text.of("§f --> §eSee any Pokémon's resistances, weaknesses and more."));
            }
            else printToLog(1, "§3/checktypes §bhas a malformed config, hiding from list.");
        }

        if (PixelUpgrade.economyEnabled && src.hasPermission("pixelupgrade.command.dittofusion"))
        {
            if (DittoFusion.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + DittoFusion.commandAlias +
                        " <target slot> <sacrifice slot> {confirm flag}"));
                permissionMessageList.add(Text.of("§f --> §eSacrifice one Ditto to improve another, for a price..."));
            }
            else printToLog(1, "§3/dittofusion §bhas a malformed config, hiding from list.");
        }

        if (src.hasPermission("pixelupgrade.command.fixevs"))
        {
            if (FixEVs.commandCost != null && FixEVs.commandAlias != null)
            {
                if (FixEVs.commandCost != 0)
                    permissionMessageList.add(Text.of("§6/" + FixEVs.commandAlias + " <slot, 1-6> {confirm flag}"));
                else
                    permissionMessageList.add(Text.of("§6/" + FixEVs.commandAlias + " <slot, 1-6>"));

                permissionMessageList.add(Text.of("§f --> §eEVs above 252 are wasted. This will fix them!"));
            }
            else printToLog(1, "§3/fixevs §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.fixgenders"))
        {
            if (FixGenders.commandAlias != null)
            {
                if (calledRemotely)
                    permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " <target> {confirm flag}"));
                else
                {
                    if (src.hasPermission("pixelupgrade.command.staff.fixgenders"))
                        permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " [target?] {confirm flag}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " {confirm flag}"));
                }

                permissionMessageList.add(Text.of("§f --> §eFixes Pokémon affected by 6.0.x bugs or bad commands."));
            }
            else printToLog(1, "§3/fixgenders §bhas a malformed config, hiding from list.");
        }

        if (src.hasPermission("pixelupgrade.command.fixlevel"))
        {
            if (FixLevel.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + FixLevel.commandAlias + " <slot, 1-6> {confirm flag}"));
                permissionMessageList.add(Text.of("§f --> §eWant to lower your level to get more EVs? Try this."));
            }
            else printToLog(1, "§3/fixlevel §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.staff.forcehatch"))
        {
            if (ForceHatch.commandAlias != null)
            {
                if (calledRemotely)
                    permissionMessageList.add(Text.of("§6/" + ForceHatch.commandAlias + " <target> <slot, 1-6>"));
                else
                    permissionMessageList.add(Text.of("§6/" + ForceHatch.commandAlias + " [target?] <slot, 1-6>"));

                permissionMessageList.add(Text.of("§f --> §eHatch any eggs instantly. Supports remote players!"));
            }
            else printToLog(1, "§3/forcehatch §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.staff.forcestats"))
        {
            if (ForceStats.commandAlias != null)
            {
                if (calledRemotely)
                    permissionMessageList.add(Text.of("§6/" + ForceStats.commandAlias + " <target> <slot> <stat> <value> {force flag}"));
                else
                    permissionMessageList.add(Text.of("§6/" + ForceStats.commandAlias + " [target?] <slot> <stat> <value> {force flag}"));

                permissionMessageList.add(Text.of("§f --> §eChange supported stats, or pass -f and go crazy."));
            }
            else printToLog(1, "§3/forcestats §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.pokecure"))
        {
            if (PokeCure.commandCost != null && PokeCure.commandAlias != null && PokeCure.healParty != null)
            {
                if (calledRemotely)
                    permissionMessageList.add(Text.of("§6/" + PokeCure.commandAlias + " <target> [slot? 1-6]"));
                else
                {
                    String flagString;
                    if (PokeCure.commandCost != 0)
                        flagString = " {confirm flag}";
                    else
                        flagString = "";

                    if (PokeCure.healParty)
                    {
                        if (src.hasPermission("pixelupgrade.command.other.pokecure"))
                            src.sendMessage(Text.of("§6/" + commandAlias + " [target?]" + flagString));
                        else
                            src.sendMessage(Text.of("§6/" + commandAlias + " " + flagString));
                    }
                    else
                    {
                        if (src.hasPermission("pixelupgrade.command.other.pokecure"))
                            src.sendMessage(Text.of("§6/" + commandAlias + " [target?] <slot, 1-6>" + flagString));
                        else
                            src.sendMessage(Text.of("§6/" + commandAlias + " <slot, 1-6>" + flagString));
                    }

                }

                if (PokeCure.healParty)
                    permissionMessageList.add(Text.of("§f --> §eHeals all your Pokémon, and cures status ailments."));
                else
                    permissionMessageList.add(Text.of("§f --> §eHeals a Pokémon, also curing status ailments."));

            }
            else printToLog(1, "§3/pokecure §bhas a malformed config, hiding from list.");
        }

        if (calledRemotely || src.hasPermission("pixelupgrade.command.staff.reload"))
        {
            permissionMessageList.add(Text.of("§6/pureload <config>"));
            permissionMessageList.add(Text.of("§f --> §eReload one or more PixelUpgrade configs on the fly."));
        }

        if (src.hasPermission("pixelupgrade.command.staff.resetcount"))
        {
            if (ResetCount.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + ResetCount.commandAlias + " <slot, 1-6> <count> {confirm flag}"));
                permissionMessageList.add(Text.of("§f --> §eWant to upgrade further? Reset counters with this."));
            }
            else printToLog(1, "§3/resetcount §bhas a malformed config, hiding from list.");
        }

        if (src.hasPermission("pixelupgrade.command.resetevs"))
        {
            if (ResetEVs.commandCost != null && ResetEVs.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + ResetEVs.commandAlias + " <slot, 1-6> {confirm flag}"));
                permissionMessageList.add(Text.of("§f --> §eNot happy with your EV spread? This wipes all EVs."));
            }
            else printToLog(1, "§3/resetevs §bhas a malformed config, hiding from list.");
        }

        if (src.hasPermission("pixelupgrade.command.showstats"))
        {
            if (ShowStats.commandCost != null && ShowStats.commandAlias != null)
            {
                if (ShowStats.commandCost != 0)
                    permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot, 1-6> {confirm flag}"));
                else
                    permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot, 1-6>"));

                permissionMessageList.add(Text.of("§f --> §eCaught something special? Show it off!"));
            }
            else printToLog(1, "§3/showstats §bhas a malformed config, hiding from list.");
        }

        if (/*calledRemotely ||*/ src.hasPermission("pixelupgrade.command.staff.spawndex"))
        {
            if (SpawnDex.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + SpawnDex.commandAlias + " <Pokédex number> {shiny flag}"));
                permissionMessageList.add(Text.of("§f --> §eSpawns a Pokémon from a given Pokédex number."));
            }
            else printToLog(1, "§3/spawndex §bhas a malformed config, hiding from list.");
        }

        if (src.hasPermission("pixelupgrade.command.switchgender"))
        {
            if (SwitchGender.commandCost != null && SwitchGender.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + SwitchGender.commandAlias + " <slot, 1-6> {confirm flag}"));
                permissionMessageList.add(Text.of("§f --> §eWant to change a Pokémon's gender? Try this."));
            }
            else printToLog(1, "§3/switchgender §bhas a malformed config, hiding from list.");
        }

        if (PixelUpgrade.economyEnabled && src.hasPermission("pixelupgrade.command.upgradeivs"))
        {
            if (UpgradeIVs.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + UpgradeIVs.commandAlias + " <slot> <IV type> [amount?] {confirm flag}"));
                permissionMessageList.add(Text.of("§f --> §eBuy upgrades for your Pokémon's IVs."));
            }
            else printToLog(1, "§3/upgradeivs §bhas a malformed config, hiding from list.");
        }

        PaginationList.Builder list = PaginationList.builder()
                    .title(Text.of(TextColors.DARK_PURPLE, "§dPixelUpgrade commands"))
                    .contents(permissionMessageList)
                    .padding(Text.of(TextColors.DARK_PURPLE, "="));

        if (permissionMessageList.isEmpty())
        {
            printToLog(1, "Player has no permissions. Letting them know, and exiting.");
            permissionMessageList.add(Text.of("§cYou have no permissions for any PixelUpgrade commands."));
            permissionMessageList.add(Text.of("§cPlease contact staff if you believe this to be in error."));
        }
        else
        {
            if (calledRemotely)
            {
                permissionMessageList.add(Text.of(""));
                permissionMessageList.add(Text.of("§6Please note: §eCommands without console functionality have been omitted."));
                list.linesPerPage(permissionMessageList.size() + 2);
            }
            else // Add 2 to the list size so title/padding doesn't create a new page. Can't click those in console.
            {
                printToLog(1, "Player was shown a list of commands they have access to. Exit.");
                list.linesPerPage(sanitizedNumLinesPerPage);

            }
        }

        list.sendTo(src);
        return CommandResult.success();
	}
}