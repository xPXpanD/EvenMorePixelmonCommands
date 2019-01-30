// Created to fix the aftermath of a Pixelmon bug where Pokémon could inherit Ditto's lack of a gender.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

// TODO: Add a -s flag to allow silent messages even if sneakyMode is off?
public class FixGenders implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    public static String commandAlias;
    public static Boolean sneakyMode, requireConfirmation;

    // Are we running from console or command blocks? We'll flag this true, and proceed accordingly.
    private boolean calledRemotely;

    // Allows us to redirect printed messages away from command blocks, and into the console if need be.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printUnformattedMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }

    // Set up a class name variable for internal use. We'll pass this to logging when showing a source is desired.
    private String sourceName = this.getClass().getSimpleName();

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        final List<String> commandErrorList = new ArrayList<>();
        if (commandAlias == null)
            commandErrorList.add("commandAlias");
        if (sneakyMode == null)
            commandErrorList.add("sneakyMode");
        if (requireConfirmation == null)
            commandErrorList.add("requireConfirmation");

        if (!commandErrorList.isEmpty())
        {
            PrintingMethods.printCommandNodeError("FixGenders", commandErrorList);
            sendCheckedMessage(src, "§4Error: §cThis command's config is invalid! Please report to staff.");
        }
        else
        {
            boolean targetedSelf = false, targetIsValid = false, commandConfirmed = false;
            final boolean hasStaffPerm = src.hasPermission("empc.command.staff.fixgenders");
            final Optional<String> arg1Optional = args.getOne("target/confirmation");
            final Optional<String> arg2Optional = args.getOne("confirmation");
            Player target;

            if (calledRemotely)
            {
                // Do we have an argument in the first slot?
                if (arg1Optional.isPresent())
                {
                    final String arg1String = arg1Optional.get();

                    // Do we have a valid online player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();
                        targetIsValid = true;
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target on first argument. See below.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cNo target found. See below.");
                    return CommandResult.empty();
                }
            }
            else
            {
                //noinspection ConstantConditions - safe, we've already guaranteed we're not running from console/blocks.
                target = (Player) src;

                if (arg1Optional.isPresent() && arg1Optional.get().equalsIgnoreCase("-c"))
                    commandConfirmed = true;
                else if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                    commandConfirmed = true;

                // Start checking argument 1 for non-flag contents if our calling player has the right permissions.
                if (hasStaffPerm)
                {
                    if (arg1Optional.isPresent() && !arg1Optional.get().equalsIgnoreCase("-c"))
                    {
                        final String arg1String = arg1Optional.get();
                        if (Sponge.getServer().getPlayer(arg1String).isPresent())
                        {
                            if (src.getName().equalsIgnoreCase(arg1String))
                                targetedSelf = true;
                            else
                            {
                                targetIsValid = true;
                                target = Sponge.getServer().getPlayer(arg1String).get();
                            }
                        }
                        else
                        {
                            printLocalError(src, "§4Error: §cInvalid target on first argument. See below.");
                            return CommandResult.empty();
                        }
                    }
                }
            }

            // Do in-battle checks. This first one is only hittable if we got called by an actual Player.
            if (!targetIsValid && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
                src.sendMessage(Text.of("§4Error: §cYou can't use this command while in a battle!"));
            else if (targetIsValid && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
            else
            {
                if (calledRemotely || !requireConfirmation || commandConfirmed)
                {
                    // Get the player's party.
                    final PartyStorage party = Pixelmon.storageManager.getParty((EntityPlayerMP) src);

                    if (sneakyMode)
                    {
                        printSourcedMessage(sourceName, "Silently fixing genders for player §3" + target.getName() +
                                "§b. Any changes will be logged.");
                    }
                    else
                    {
                        printSourcedMessage(sourceName, "Fixing genders for player §3" + target.getName() +
                                "§b. Any changes will be logged.");
                    }

                    sendCheckedMessage(src, "§7-----------------------------------------------------");

                    if (hasStaffPerm && targetIsValid)
                        sendCheckedMessage(src, "§eTarget found, checking their whole team...");
                    else if (hasStaffPerm && !targetedSelf)
                        sendCheckedMessage(src, "§eNo target found, checking your whole team...");
                    else
                        sendCheckedMessage(src, "§eChecking your whole team...");

                    fixParty(src, target, party, targetIsValid);
                }
                else
                {
                    sendCheckedMessage(src, "§5-----------------------------------------------------");
                    sendCheckedMessage(src, "§4Error: §cNo confirmation was found. Please confirm to proceed.");

                    if (calledRemotely)
                        sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target>");
                    else
                    {
                        if (hasStaffPerm && requireConfirmation)
                            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?] {-c to confirm}");
                        else if (hasStaffPerm)
                            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?]");
                        else if (requireConfirmation)
                            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " {-c to confirm}");
                        else
                            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias);
                    }

                    sendCheckedMessage(src, "");
                    sendCheckedMessage(src, "§5Please note: §dAny broken genders will be immediately rerolled.");
                    sendCheckedMessage(src, "§5-----------------------------------------------------");
                }
            }
        }

        return CommandResult.success();
	}

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));

        if (calledRemotely)
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target>");
        else if (requireConfirmation)
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?] {-c to confirm}");
        else
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?]");

        sendCheckedMessage(src, "");
        sendCheckedMessage(src, "§5Please note: §dAny broken genders will be immediately rerolled.");
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

	private void fixParty(final CommandSource src, final Player target, final PartyStorage party, final boolean targetIsValid)
    {
        // Add a blank line, so we can print results below that.
        sendCheckedMessage(src, "");

        // Show our target that we're checking them, if allowed to.
        if (!sneakyMode && targetIsValid)
        {
            target.sendMessage(Text.of("§7-----------------------------------------------------"));

            if (calledRemotely)
            {
                if (src instanceof CommandBlock)
                    target.sendMessage(Text.of("§eA check for broken genders was started from a §6block§e."));
                else
                    target.sendMessage(Text.of("§eA check for broken genders was started by §6console§e."));
            }
            else
                target.sendMessage(Text.of("§6" + src.getName() + " §eis checking your party for broken genders."));

            target.sendMessage(Text.EMPTY);
        }

        // Set up for our loop, and then actually loop.
        int slotTicker = 1, fixCount = 0, malePercent;
        Gender gender;
        for (final Pokemon pokemon : party.getAll())
        {
            if (slotTicker > 6)
                break;

            // Does the slot we're on have a Pokémon in it?
            if (pokemon != null)
            {
                gender = pokemon.getGender();
                malePercent = pokemon.getBaseStats().malePercent;

                // Is the Pokémon not male (0) or female (1), and is our male percentage above -1? Fix!
                if (gender == Gender.None && malePercent > -1)
                {
                    // Increment our fix count, since we had to fix something.
                    fixCount++;

                    // Roll the dice! Do we chop, or do we plop?
                    if (RandomHelper.rand.nextInt(100) < malePercent)
                    {
                        printSourcedMessage(sourceName, "§bSlot §3" + slotTicker +
                            " §bshould be gendered. Rerolled, now §3male§b!");

                        pokemon.setGender(Gender.Male);

                        if (!sneakyMode && targetIsValid)
                        {
                            target.sendMessage(Text.of("§bSlot §3" + slotTicker +
                                    " §bwas randomly rerolled to §3male§b!"));
                        }

                        sendCheckedMessage(src, "§bSlot §3" + slotTicker + "§b was randomly rerolled to §3male§b!");
                    }
                    else
                    {
                        printSourcedMessage(sourceName, "§bSlot §2" + slotTicker +
                            " §bshould be gendered. Rerolled, now §5female§b!");

                        pokemon.setGender(Gender.Female);

                        if (!sneakyMode && targetIsValid)
                        {
                            target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                    " §dwas randomly rerolled to §5female§d!"));
                        }

                        sendCheckedMessage(src, "§dSlot §5" + slotTicker + "§d was randomly rerolled to §5female§d!");
                    }
                }
                // Is our Pokémon male (0), and is the chance of that happening 0%? Fix!
                else if (gender == Gender.Male && malePercent == 0)
                {
                    // Increment our fix count, since we had to fix something.
                    fixCount++;

                    printSourcedMessage(sourceName, "§bSlot §2" + slotTicker +
                            " §bis male, should be female. Fixing!");

                    pokemon.setGender(Gender.Female);

                    if (!sneakyMode && targetIsValid)
                    {
                        target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                " §dhad its gender flipped to §5female§d!"));
                    }

                    sendCheckedMessage(src, "§dSlot §5" + slotTicker + "§d was changed to §5female§d!");
                }
                // Is our Pokémon female (1), and is the chance of it being male 100%? Fix!
                else if (gender == Gender.Female && malePercent == 100)
                {
                    // Increment our fix count, since we had to fix something.
                    fixCount++;

                    printSourcedMessage(sourceName, "§bSlot §2" + slotTicker +
                                                " §bis female, should be male. Fixing!");

                    pokemon.setGender(Gender.Male);

                    if (!sneakyMode && targetIsValid)
                    {
                        target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                " §dhad its gender flipped to §3male§b!"));
                    }

                    sendCheckedMessage(src, "§dSlot §5" + slotTicker + "§d was changed to §3male§b!");
                }
            }

            slotTicker++;
        }

        if (fixCount == 0)
        {
            sendCheckedMessage(src, "§eEverything looks good, nothing fixable was found.");

            if (!sneakyMode && targetIsValid)
                target.sendMessage(Text.of("§eYour party has been checked, everything looks good."));
        }
        else // We add another blank line here to create space between the fixing messages and the end message.
        {
            sendCheckedMessage(src, "");
            sendCheckedMessage(src, "§aWe're done! Glad to be of service.");

            if (!sneakyMode && targetIsValid)
            {
                target.sendMessage(Text.EMPTY);
                target.sendMessage(Text.of("§eYour party has been checked, and broken genders fixed."));
            }
        }

        if (!sneakyMode && targetIsValid)
            target.sendMessage(Text.of("§7-----------------------------------------------------"));

        sendCheckedMessage(src, "§7-----------------------------------------------------");
    }
}