// The command listing. Only shows console-accessible commands if used from there.
package rs.expand.evenmorepixelmoncommands.commands.subcommands;

import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rs.expand.evenmorepixelmoncommands.EMPC;
import rs.expand.evenmorepixelmoncommands.commands.*;

import java.util.ArrayList;

import static rs.expand.evenmorepixelmoncommands.EMPC.economyEnabled;
import static rs.expand.evenmorepixelmoncommands.EMPC.numLinesPerPage;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;

// Command format helper! Use this format if you want to add your own stuff.
// [] = optional, {} = flag, <> = required, () = add comment here
// Make comments gray (color 7) so they don't look like part of the syntax. Useful for showing missing arg perms.
public class ListCommands implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes.
    public static String commandAlias;

    // TODO: Add the correct args for non-player use on supported commands.
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
	{
	    if (!(src instanceof CommandBlock))
        {
            // Set up a class name variable for internal use. We'll pass this to logging when showing a source is desired.
            final String sourceName = "EMPC list";

            // Running from console? Let's tell our code that. If "src" is not a Player, this becomes true.
            final boolean calledRemotely = !(src instanceof Player);

            // Create a fresh list to store messages in for every permitted command. We'll iterate over this at the end.
            final java.util.List<Text> permissionMessageList = new ArrayList<>();

            // Validate the data we get from the command's main config. Revert to safe values if necessary.
            final int sanitizedNumLinesPerPage;

            if (numLinesPerPage == null)
            {
                printSourcedError(sourceName, "§cCould not read node \"§4numLinesPerPage§c\".");
                sanitizedNumLinesPerPage = 20;
                printBasicError("We'll proceed with safe defaults. Please fix this.");
            }
            else
                sanitizedNumLinesPerPage = numLinesPerPage;

            if (EMPC.commandAlias != null)
            {
                permissionMessageList.add(Text.of("§6/" + EMPC.commandAlias + " [option?]"));
                permissionMessageList.add(Text.of("§f ➡ §eShows core mod commands such as this list! Clickable."));
            }
            else
                printSourcedError(sourceName, "§3The main config is malformed! Hiding from list.");

            if (calledRemotely || src.hasPermission("empc.command.checkevs"))
            {
                if (CheckEVs.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + CheckEVs.commandAlias + " <Pokémon name/number>"));
                    permissionMessageList.add(Text.of("§f ➡ §eShows a Pokémon's EV yields when defeated."));
                }
                else
                    printSourcedError(sourceName, "§6/checkevs §ehas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("empc.command.checkstats"))
            {
                if (CheckStats.commandCost != null && CheckStats.commandAlias != null && CheckStats.showTeamWhenSlotEmpty != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias + " <target> [slot? 1-6]"));
                    else
                    {
                        final String flagString =
                                economyEnabled && CheckStats.commandCost != 0 ? " {-c to confirm}" : "";

                        if (src.hasPermission("empc.command.other.checkstats") && CheckStats.showTeamWhenSlotEmpty)
                        {
                            permissionMessageList.add(Text.of("§6/" + CheckStats.commandAlias +
                                    " [target?] [slot? 1-6]" + flagString));
                        }
                        else if (src.hasPermission("empc.command.other.checkstats"))
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
                    printSourcedError(sourceName, "§6/checkstats §ehas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("empc.command.checktypes"))
            {
                if (CheckTypes.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + CheckTypes.commandAlias + " <Pokémon name/number>"));
                    permissionMessageList.add(Text.of("§f ➡ §eShows a Pokémon's resistances, weaknesses and more."));
                }
                else
                    printSourcedError(sourceName, "§6/checktypes §ehas a malformed config, hiding from list.");
            }

            /*if (!calledRemotely && src.hasPermission("empc.command.dittofusion"))
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
                    printSourcedError(sourceName, "§6/dittofusion §ehas a malformed config, hiding from list.");
            }*/

            if (calledRemotely || src.hasPermission("empc.command.fixgenders"))
            {
                if (FixGenders.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " <target>"));
                    else
                    {
                        if (src.hasPermission("empc.command.staff.fixgenders") && FixGenders.requireConfirmation)
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " [target?] {-c to confirm}"));
                        else if (src.hasPermission("empc.command.staff.fixgenders"))
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " [target?]"));
                        else if (FixGenders.requireConfirmation)
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias + " {-c to confirm}"));
                        else
                            permissionMessageList.add(Text.of("§6/" + FixGenders.commandAlias));
                    }

                    permissionMessageList.add(Text.of("§f ➡ §eFixes genders broken by commands or bugs."));
                }
                else
                    printSourcedError(sourceName, "§6/fixgenders §ehas a malformed config, hiding from list.");
            }

            /*if (calledRemotely || src.hasPermission("empc.command.staff.forcestats"))
            {
                if (ForceStats.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + ForceStats.commandAlias + " <target> <slot> <stat> <value> {-f to force}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + ForceStats.commandAlias + " [target?] <slot> <stat> <value> {-f to force}"));

                    permissionMessageList.add(Text.of("§f ➡ §eChanges stats freely, with an optional safety bypass."));
                }
                else
                    printSourcedError(sourceName, "§6/forcestats §ehas a malformed config, hiding from list.");
            }*/

            if (calledRemotely || src.hasPermission("empc.command.partyhatch"))
            {
                if (PartyHatch.commandCost != null && PartyHatch.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + PartyHatch.commandAlias + " <target>"));
                    else
                    {
                        final String flagString =
                                economyEnabled && PartyHatch.commandCost != 0 ? " {-c to confirm}" : "";

                        if (src.hasPermission("empc.command.other.partyhatch"))
                            permissionMessageList.add(Text.of("§6/" + PartyHatch.commandAlias + " [target?]" + flagString));
                        else
                            permissionMessageList.add(Text.of("§6/" + PartyHatch.commandAlias + " " + flagString));
                    }

                    if (src.hasPermission("empc.command.other.partyhatch"))
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches all eggs in a targeted party."));
                    else
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches all of your eggs."));
                }
                else
                    printSourcedError(sourceName, "§6/partyhatch §ehas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("empc.command.partyheal"))
            {
                if (PartyHeal.commandCost != null && PartyHeal.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + PartyHeal.commandAlias + " <target>"));
                    else
                    {
                        final String flagString =
                                economyEnabled && PartyHeal.commandCost != 0 ? " {-c to confirm}" : "";

                        if (src.hasPermission("empc.command.other.partyheal"))
                            permissionMessageList.add(Text.of("§6/" + PartyHeal.commandAlias + " [target?]" + flagString));
                        else
                            permissionMessageList.add(Text.of("§6/" + PartyHeal.commandAlias + " " + flagString));
                    }

                    if (src.hasPermission("empc.command.other.partyheal"))
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately heals all Pokémon in a targeted party."));
                    else
                        permissionMessageList.add(Text.of("§f ➡ §eImmediately heals all of your Pokémon."));
                }
                else
                    printSourcedError(sourceName, "§6/partyheal §ehas a malformed config, hiding from list.");
            }

            /*if (!calledRemotely && src.hasPermission("empc.command.staff.resetcount"))
            {
                if (ResetCount.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + ResetCount.commandAlias + " <slot, 1-6> <count> {-c to confirm}"));
                    permissionMessageList.add(Text.of("§f ➡ §eResets fusion/upgrade counts on maxed-out Pokémon."));
                }
                else
                    printSourcedError(sourceName, "§6/resetcount §ehas a malformed config, hiding from list.");
            }*/

            if (calledRemotely || src.hasPermission("empc.command.staff.randomtm"))
            {
                if (RandomTM.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + RandomTM.commandAlias + " [target] {-a to include HMs}"));
                    permissionMessageList.add(Text.of("§f ➡ §eGives the chosen player a random TM/HM."));
                }
                else
                    printSourcedError(sourceName, "§6/randomtm §ehas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("empc.command.staff.resetdex"))
            {
                if (ResetDex.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + ResetDex.commandAlias + " <target> {-c to confirm}"));
                    permissionMessageList.add(Text.of("§f ➡ §eWipes the chosen player's Pokédex. Handle with care."));
                }
                else
                    printSourcedError(sourceName, "§6/resetdex §ehas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("empc.command.resetevs"))
            {
                if (ResetEVs.commandCost != null && ResetEVs.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + ResetEVs.commandAlias + " <slot, 1-6> {-c to confirm}"));
                    permissionMessageList.add(Text.of("§f ➡ §eWipes all EVs. Use if you're unhappy with your spread."));
                }
                else
                    printSourcedError(sourceName, "§6/resetevs §ehas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("empc.command.showstats"))
            {
                if (ShowStats.commandCost != null && ShowStats.commandAlias != null)
                {
                    if (economyEnabled && ShowStats.commandCost != 0)
                        permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot, 1-6> {-c to confirm}"));
                    else
                        permissionMessageList.add(Text.of("§6/" + ShowStats.commandAlias + " <slot, 1-6>"));

                    permissionMessageList.add(Text.of("§f ➡ §eShows off a Pokémon of choice to the server."));
                }
                else
                    printSourcedError(sourceName, "§6/showstats §ehas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("empc.command.staff.spawndex"))
            {
                if (SpawnDex.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + SpawnDex.commandAlias +
                            " <Pokémon name/number> {flags?} [radius?]"));
                    permissionMessageList.add(Text.of("§f ➡ §eSpawns a heavily customizable Pokémon at the cursor."));
                }
                else
                    printSourcedError(sourceName, "§6/spawndex §ehas a malformed config, hiding from list.");
            }

            if (!calledRemotely && src.hasPermission("empc.command.switchgender"))
            {
                if (SwitchGender.commandCost != null && SwitchGender.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + SwitchGender.commandAlias + " <slot, 1-6> {-c to confirm}"));
                    permissionMessageList.add(Text.of("§f ➡ §eTurns a Pokémon into the other gender, if possible."));
                }
                else
                    printSourcedError(sourceName, "§6/switchgender §ehas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("empc.command.timedhatch"))
            {
                if (TimedHatch.commandCost != null && TimedHatch.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " <target> <slot, 1-6>"));
                    else
                    {
                        final String flagString =
                                economyEnabled && TimedHatch.commandCost != 0 ? " {-c to confirm}" : "";

                        if (src.hasPermission("empc.command.other.timedhatch"))
                            permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " [target?] <slot, 1-6>" + flagString));
                        else
                            permissionMessageList.add(Text.of("§6/" + TimedHatch.commandAlias + " <slot, 1-6>" + flagString));
                    }

                    permissionMessageList.add(Text.of("§f ➡ §eImmediately hatches a targeted egg."));
                }
                else
                    printSourcedError(sourceName, "§6/timedhatch §ehas a malformed config, hiding from list.");
            }

            if (calledRemotely || src.hasPermission("empc.command.timedheal"))
            {
                if (TimedHeal.commandCost != null && TimedHeal.commandAlias != null)
                {
                    if (calledRemotely)
                        permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " <target> <slot, 1-6>"));
                    else
                    {
                        final String flagString =
                                economyEnabled && TimedHeal.commandCost != 0 ? " {-c to confirm}" : "";

                        if (src.hasPermission("empc.command.other.timedheal"))
                            permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " [target?] <slot, 1-6>" + flagString));
                        else
                            permissionMessageList.add(Text.of("§6/" + TimedHeal.commandAlias + " <slot, 1-6>" + flagString));
                    }

                    permissionMessageList.add(Text.of("§f ➡ §eImmediately heals a targeted Pokémon."));
                }
                else
                    printSourcedError(sourceName, "§6/timedheal §ehas a malformed config, hiding from list.");
            }

            /*if (!calledRemotely && src.hasPermission("empc.command.upgradeivs"))
            {
                if (UpgradeIVs.commandAlias != null)
                {
                    permissionMessageList.add(Text.of("§6/" + UpgradeIVs.commandAlias + " <slot> <IV type> [amount?] {-c to confirm}"));

                    if (economyEnabled)
                        permissionMessageList.add(Text.of("§f ➡ §eUpgrades a Pokémon's IVs for economy money."));
                    else
                        permissionMessageList.add(Text.of("§f ➡ §eUpgrades a Pokémon's IVs."));
                }
                else
                    printSourcedError(sourceName, "§6/upgradeivs §ehas a malformed config, hiding from list.");
            }*/

            final PaginationList.Builder paginatedList = PaginationList.builder()
                        .title(Text.of(TextColors.DARK_PURPLE, "§dEven More Pixelmon Commands 5.0.0"))
                        .contents(permissionMessageList)
                        .padding(Text.of(TextColors.DARK_PURPLE, '='));

            if (permissionMessageList.size() <= 2)
            {
                permissionMessageList.add(Text.EMPTY);
                permissionMessageList.add(Text.of("§cYou have no permissions for any other EMPC commands."));
                permissionMessageList.add(Text.of("§cPlease contact staff if you believe this to be in error."));
            }
            else
            {
                // Add 2 to the list size so title/padding doesn't create a new page. Can't click those in console.
                if (calledRemotely)
                {
                    permissionMessageList.add(Text.EMPTY);
                    permissionMessageList.add(Text.of("§6Please note: §eCommands without console functionality were omitted."));
                    paginatedList.linesPerPage(permissionMessageList.size() + 2);
                }
                else
                    paginatedList.linesPerPage(sanitizedNumLinesPerPage);
            }

            paginatedList.sendTo(src);
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
	}
}