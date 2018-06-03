// The command listing. Only shows console-accessible commands if used from there.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.format.TextColors;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.economyEnabled;

// Command format helper! Use this format if you want to add your own stuff.
// [] = optional, {} = flag, <> = required, () = add comment here
// Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.
public class PixelUpgradeInfo implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes.
    public static String commandAlias;
    public static Integer numLinesPerPage;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("PU list", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
	{
	    if (!(src instanceof CommandBlock))
        {
            // Running from console? Let's tell our code that. If "src" is not a Player, this becomes true.
            final boolean calledRemotely = !(src instanceof Player);

            // Make an uninitialized String for command confirmation flags. We fill this in when people need to know a flag.
            String flagString;

            if (!calledRemotely)
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            // Validate the data we get from the command's main config. Revert to safe values if necessary.
            final List<Text> permissionMessageList = new ArrayList<>();
            boolean gotConfigError = false;
            final int sanitizedNumLinesPerPage;

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

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.checkegg"))
            {
                if (CheckEgg.commandCost != null && CheckEgg.commandAlias != null)
                {
                    if (economyEnabled && CheckEgg.commandCost > 0)
                    {
                        permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                                " <slot, 1-6> {confirm flag}"));
                    }
                    else
                    {
                        permissionMessageList.add(Text.of("§6/" + CheckEgg.commandAlias +
                                " <slot, 1-6>"));
                    }

                    permissionMessageList.add(Text.of("§f ➡ §eChecks an egg to see what's inside."));
                }
                else
                    printToLog(1, "§3/checkegg §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.checkstats"))
            {
                if (CheckStats.commandCost != null && CheckStats.commandAlias != null && CheckStats.showTeamWhenSlotEmpty != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias + " <target> [slot? 1-6]"));
                    else
                    {
                        if (economyEnabled && CheckStats.commandCost != 0)
                            flagString = " {confirm flag}";
                        else
                            flagString = "";

                        if (src.hasPermission("pixelupgrade.command.other.checkstats") && CheckStats.showTeamWhenSlotEmpty)
                        {
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " [target?] [slot? 1-6]" + flagString));
                        }
                        else if (src.hasPermission("pixelupgrade.command.other.checkstats"))
                        {
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " [target?] <slot, 1-6>" + flagString));
                        }
                        else
                        {
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " <slot, 1-6>" + flagString + " §7(no perms for target)"));
                        }
                    }

                    permissionMessageList.add(Text.of("§f ➡ §eLists a Pokémon's IVs, nature, size and more."));
                }
                else
                    printToLog(1, "§3/checkstats §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.checktypes"))
            {
                if (CheckTypes.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + CheckTypes.commandAlias + " <Pokémon name/number>"));
                    permissionMessageList.add(Text.of("§f ➡ §eShows a Pokémon's resistances, weaknesses and more."));
                }
                else
                    printToLog(1, "§3/checktypes §bhas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.dittofusion"))
            {
                if (DittoFusion.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + DittoFusion.commandAlias +
                            " <target slot> <sacrifice slot> {confirm flag}"));

                    if (economyEnabled)
                        permissionMessageList.add(Text.of("§f ➡ §eSacrifice one Ditto to improve another, for a price..."));
                    else // No creepy "for a price..." note here, folks.
                        permissionMessageList.add(Text.of("§f ➡ §eSacrifice one Ditto to improve another."));
                }
                else
                    printToLog(1, "§3/dittofusion §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.fixgenders"))
            {
                if (FixGenders.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " <target>"));
                    else
                    {
                        if (src.hasPermission("pixelupgrade.command.staff.fixgenders") && FixGenders.requireConfirmation)
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " [target?] {confirm flag}"));
                        else if (src.hasPermission("pixelupgrade.command.staff.fixgenders"))
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " [target?]"));
                        else if (FixGenders.requireConfirmation)
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " {confirm flag}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias));
                    }

                    permissionMessageList.add(Text.of("§f ➡ §eFixes genders broken by commands or bugs."));
                }
                else
                    printToLog(1, "§3/fixgenders §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.staff.forcehatch"))
            {
                if (ForceHatch.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + ForceHatch.commandAlias + " <target> <slot, 1-6>"));
                    else
                        permissionMessageList.add(Text.of("§6/" + ForceHatch.commandAlias + " [target?] <slot, 1-6>"));

                    permissionMessageList.add(Text.of("§f ➡ §eHatches any eggs instantly."));
                }
                else
                    printToLog(1, "§3/forcehatch §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.staff.forcestats"))
            {
                if (ForceStats.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + ForceStats.commandAlias + " <target> <slot> <stat> <value> {force flag}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + ForceStats.commandAlias + " [target?] <slot> <stat> <value> {force flag}"));

                    permissionMessageList.add(Text.of("§f ➡ §eChanges stats freely, with an optional safety bypass."));
                }
                else
                    printToLog(1, "§3/forcestats §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.staff.reload"))
            {
                permissionMessageList.add(Text.of("§6/pureload <config>"));
                permissionMessageList.add(Text.of("§f ➡ §eReload one or more PixelUpgrade configs on the fly."));
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.staff.resetcount"))
            {
                if (ResetCount.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + ResetCount.commandAlias + " <slot, 1-6> <count> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f ➡ §eResets fusion/upgrade counts on maxed-out Pokémon."));
                }
                else
                    printToLog(1, "§3/resetcount §bhas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.resetevs"))
            {
                if (ResetEVs.commandCost != null && ResetEVs.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + ResetEVs.commandAlias + " <slot, 1-6> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f ➡ §eWipes all EVs. Use if you're unhappy with your spread."));
                }
                else
                    printToLog(1, "§3/resetevs §bhas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.showstats"))
            {
                if (ShowStats.commandCost != null && ShowStats.commandAlias != null)
                {
                    if (economyEnabled && ShowStats.commandCost != 0)
                        permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot, 1-6> {confirm flag}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot, 1-6>"));

                    permissionMessageList.add(Text.of("§f ➡ §eShows off a Pokémon of choice to the server."));
                }
                else
                    printToLog(1, "§3/showstats §bhas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.staff.spawndex"))
            {
                if (SpawnDex.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + SpawnDex.commandAlias +
                            " <Pokémon name/number> {flags?} [radius?]"));
                    permissionMessageList.add(Text.of("§f ➡ §eSpawns a heavily customizable Pokémon at the cursor."));
                }
                else
                    printToLog(1, "§3/spawndex §bhas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.switchgender"))
            {
                if (SwitchGender.commandCost != null && SwitchGender.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + SwitchGender.commandAlias + " <slot, 1-6> {confirm flag}"));
                    permissionMessageList.add(Text.of("§f ➡ §eTurns a Pokémon into the other gender, if possible."));
                }
                else
                    printToLog(1, "§3/switchgender §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.timedhatch"))
            {
                if (TimedHatch.commandCost != null && TimedHatch.commandAlias != null && TimedHatch.hatchParty != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " <target> [slot? 1-6]"));
                    else
                    {
                        if (economyEnabled && TimedHatch.commandCost != 0)
                            flagString = " {confirm flag}";
                        else
                            flagString = "";

                        if (TimedHatch.hatchParty)
                        {
                            if (src.hasPermission("pixelupgrade.command.other.timedhatch"))
                                permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " [target?]" + flagString));
                            else
                                permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " " + flagString));
                        }
                        else
                        {
                            if (src.hasPermission("pixelupgrade.command.other.timedhatch"))
                                permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " [target?] <slot, 1-6>" + flagString));
                            else
                                permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " <slot, 1-6>" + flagString));
                        }
                    }

                    if (src.hasPermission("pixelupgrade.command.other.timedhatch") && TimedHatch.hatchParty)
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches all targeted eggs."));
                    else if (TimedHatch.hatchParty)
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches all your eggs."));
                    else if (src.hasPermission("pixelupgrade.command.other.timedhatch"))
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches the targeted egg."));
                    else
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches a egg."));
                }
                else
                    printToLog(1, "§3/timedhatch §bhas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("pixelupgrade.command.timedheal"))
            {
                if (TimedHeal.commandCost != null && TimedHeal.commandAlias != null && TimedHeal.healParty != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " <target> [slot? 1-6]"));
                    else
                    {
                        if (economyEnabled && TimedHeal.commandCost != 0)
                            flagString = " {confirm flag}";
                        else
                            flagString = "";

                        if (TimedHeal.healParty)
                        {
                            if (src.hasPermission("pixelupgrade.command.other.timedheal"))
                                permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " [target?]" + flagString));
                            else
                                permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " " + flagString));
                        }
                        else
                        {
                            if (src.hasPermission("pixelupgrade.command.other.timedheal"))
                                permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " [target?] <slot, 1-6>" + flagString));
                            else
                                permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " <slot, 1-6>" + flagString));
                        }
                    }

                    if (src.hasPermission("pixelupgrade.command.other.timedheal") && TimedHeal.healParty)
                        permissionMessageList.add(Text.of("§f ➡ §eHeals all targeted Pokémon, also curing ailments."));
                    else if (TimedHeal.healParty)
                        permissionMessageList.add(Text.of("§f ➡ §eHeals all your Pokémon, and cures status ailments."));
                    else if (src.hasPermission("pixelupgrade.command.other.timedheal"))
                        permissionMessageList.add(Text.of("§f ➡ §eHeals the targeted Pokémon, also curing ailments."));
                    else
                        permissionMessageList.add(Text.of("§f ➡ §eHeals a Pokémon, also curing status ailments."));
                }
                else
                    printToLog(1, "§3/timedheal §bhas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("pixelupgrade.command.upgradeivs"))
            {
                if (UpgradeIVs.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + UpgradeIVs.commandAlias + " <slot> <IV type> [amount?] {confirm flag}"));

                    if (economyEnabled)
                        permissionMessageList.add(Text.of("§f ➡ §eUpgrades a Pokémon's IVs for economy money."));
                    else
                        permissionMessageList.add(Text.of("§f ➡ §eUpgrades a Pokémon's IVs."));
                }
                else
                    printToLog(1, "§3/upgradeivs §bhas a malformed config, hiding from list.");
            }

            final PaginationList.Builder list = PaginationList.builder()
                        .title(Text.of(TextColors.DARK_PURPLE, "§dPixelUpgrade commands"))
                        .contents(permissionMessageList)
                        .padding(Text.of(TextColors.DARK_PURPLE, '='));

            if (permissionMessageList.isEmpty())
            {
                printToLog(1, "Player has no permissions. Letting them know, and exiting.");
                permissionMessageList.add(Text.of("§cYou have no permissions for any PixelUpgrade commands."));
                permissionMessageList.add(Text.of("§cPlease contact staff if you believe this to be in error."));
            }
            else
            {
                // Add 2 to the list size so title/padding doesn't create a new page. Can't click those in console.
                if (calledRemotely)
                {
                    permissionMessageList.add(Text.EMPTY);
                    permissionMessageList.add(Text.of("§6Please note: §eCommands without console functionality were omitted."));
                    list.linesPerPage(permissionMessageList.size() + 2);
                }
                else
                {
                    printToLog(1, "Player was shown a list of accessible commands. Exit.");
                    list.linesPerPage(sanitizedNumLinesPerPage);
                }
            }

            list.sendTo(src);
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
	}
}