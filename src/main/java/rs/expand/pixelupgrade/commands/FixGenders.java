// Created to fix the aftermath of a Pixelmon bug where Pokémon could inherit Ditto's lack of a gender.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;

// TODO: Add a -s flag to allow silent messages even if sneakyMode is off?
public class FixGenders implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    public static String commandAlias;
    public static Boolean sneakyMode, requireConfirmation;

    // Are we running from console or command blocks? We'll flag this true, and proceed accordingly.
    private boolean calledRemotely;

    // Allows us to redirect printed messages away from command blocks, and into the console if need be.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printBasicMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    // If we're running from console or blocks, we need to swallow everything to avoid cluttering.
    private void printToLog (final int debugNum, final String inputString)
    {
        if (!calledRemotely)
            PrintingMethods.printDebugMessage("FixGenders", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        final ArrayList<String> nativeErrorArray = new ArrayList<>();
        if (commandAlias == null)
            nativeErrorArray.add("commandAlias");
        if (sneakyMode == null)
            nativeErrorArray.add("sneakyMode");
        if (requireConfirmation == null)
            nativeErrorArray.add("requireConfirmation");

        if (!nativeErrorArray.isEmpty())
        {
            PrintingMethods.printCommandNodeError("FixGenders", nativeErrorArray);
            sendCheckedMessage(src, "§4Error: §cThis command's config is invalid! Please report to staff.");
        }
        else
        {
            if (calledRemotely)
            {
                if (src instanceof CommandBlock)
                {
                    PrintingMethods.printDebugMessage("FixGenders", 1,
                            "Called by command block, starting. Silencing logger messages.");
                }
                else
                {
                    PrintingMethods.printDebugMessage("FixGenders", 1,
                            "Called by console, starting. Silencing further log messages.");
                }
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean canContinue = true, targetedSelf = false, targetIsValid = false, commandConfirmed = false;
            final boolean hasStaffPerm = src.hasPermission("pixelupgrade.command.other.fixgenders");
            final Optional<String> arg1Optional = args.getOne("target/confirmation");
            final Optional<String> arg2Optional = args.getOne("confirmation");
            String errorString = "§4There's an error message missing, please report this!";
            Player target = null;

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
                        errorString = "§4Error: §cInvalid target on first argument. See below.";
                        canContinue = false;
                    }
                }
                else
                {
                    errorString = "§4Error: §cNo target found. See below.";
                    canContinue = false;
                }
            }
            else
            {
                printToLog(2, "Starting argument check for player's input.");

                //noinspection ConstantConditions - safe, we've already guaranteed we're not running from console/blocks.
                target = (Player) src;

                if (arg1Optional.isPresent() && arg1Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 1.");
                    commandConfirmed = true;
                }
                else if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 2.");
                    commandConfirmed = true;
                }

                // Start checking argument 1 for non-flag contents if our calling player has the right permissions.
                if (hasStaffPerm)
                {
                    printToLog(2, "Player has target permissions. Checking for target.");

                    if (arg1Optional.isPresent() && !arg1Optional.get().equalsIgnoreCase("-c"))
                    {
                        printToLog(2, "There's something in the first argument slot!");

                        final String arg1String = arg1Optional.get();
                        if (Sponge.getServer().getPlayer(arg1String).isPresent())
                        {
                            if (src.getName().equalsIgnoreCase(arg1String))
                            {
                                printToLog(2, "Player targeted self. Continuing.");
                                targetedSelf = true;
                            }
                            else
                            {
                                printToLog(2, "Found a valid target in argument 1.");
                                targetIsValid = true;
                                target = Sponge.getServer().getPlayer(arg1String).get();
                            }
                        }
                        else
                        {
                            printToLog(1, "Invalid target. Printing error and exiting for safety.");
                            errorString = "§4Error: §cInvalid target on first argument. See below.";

                            canContinue = false;
                        }
                    }
                    else
                        printToLog(2, "No target argument was found, targeting source player.");
                }
                else
                    printToLog(2, "Player does not have staff permissions, skipping to execution.");
            }

            if (!canContinue)
            {
                sendCheckedMessage(src, errorString);

                if (calledRemotely)
                    sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target>");
                else if (requireConfirmation)
                    sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?] [-c to confirm]");
                else
                    sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?]");

                // Specifically print our error to the block, altered a bit to make more sense. Kinda awkward, but hey.
                if (src instanceof CommandBlock)
                {
                    errorString = errorString.replaceAll(" See below\\.", " See console.");
                    src.sendMessage(Text.of(errorString));
                }
            }
            // Do some battle checks. Only hittable if we got called by an actual Player.
            else if (!targetIsValid && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                printToLog(0, "Player tried to fix own team while in a battle. Exit.");
                src.sendMessage(Text.of("§4Error: §cYou can't use this command while in a battle!"));
            }
            else if (targetIsValid && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
            {
                if (!calledRemotely)
                    printToLog(0, "Target was in a battle, cannot proceed. Exit.");

                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
            }
            else
            {
                if (calledRemotely || !requireConfirmation || commandConfirmed)
                {
                    // Target will be the source player if no valid player was provided and we didn't hit any errors.
                    final Optional<PlayerStorage> storage =
                            PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + target.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        sendCheckedMessage(src, "§4Error: §cNo Pixelmon storage found. Please contact staff!");
                    }
                    else
                    {
                        if (sneakyMode)
                        {
                            printToLog(1, "Silently fixing genders for player §3" + target.getName() +
                                    "§b as per config.");
                        }
                        else
                        {
                            printToLog(1, "Fixing genders for player §3" + target.getName() +
                                    "§b, informing if need be.");
                        }

                        sendCheckedMessage(src, "§7-----------------------------------------------------");

                        if (hasStaffPerm && targetIsValid)
                            sendCheckedMessage(src, "§eTarget found, checking their whole team...");
                        else if (hasStaffPerm && !targetedSelf)
                            sendCheckedMessage(src, "§eNo target found, checking your whole team...");
                        else
                            sendCheckedMessage(src, "§eChecking your whole team...");

                        fixParty(src, target, storage.get(), targetIsValid);
                    }
                }
                else
                {
                    printToLog(1, "No confirmation provided, printing warning and aborting.");

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
                    sendCheckedMessage(src, "§dThis command is experimental. Stuff may break, report issues!");
                    sendCheckedMessage(src, "§5-----------------------------------------------------");
                }
            }
        }

        return CommandResult.success();
	}

	private void fixParty(final CommandSource src, final Player target, final PlayerStorage storageCompleted, final boolean targetIsValid)
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

            target.sendMessage(Text.of(""));
        }

        // Set up for our loop, and then actually loop.
        EntityPixelmon pokemon;
        int slotTicker = 1, fixCount = 0, gender, malePercent;
        for (final NBTTagCompound loopValue : storageCompleted.partyPokemon)
        {
            if (slotTicker > 6)
                break;

            if (loopValue != null)
            {
                pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(loopValue, (World) target.getWorld());
                gender = loopValue.getInteger(NbtKeys.GENDER);
                malePercent = pokemon.baseStats.malePercent;

                // Is the Pokémon not male (0) or female (1), and is our male percentage above -1? Fix!
                if (gender > 1 && malePercent >= 0)
                {
                    // Increment our fix count, since we had to fix something.
                    fixCount++;

                    // Roll the dice! Do we chop, or do we plop?
                    if (RandomHelper.rand.nextInt(100) < malePercent)
                    {
                        printToLog(1, "§bSlot §3" + slotTicker +
                            " §bshould be gendered. Rerolled, now §3male§b.");

                        loopValue.setInteger(NbtKeys.GENDER, 0);

                        if (!sneakyMode && targetIsValid)
                        {
                            target.sendMessage(Text.of("§bSlot §3" + slotTicker +
                                    " §bwas randomly rerolled to §3male§b!"));
                        }

                        sendCheckedMessage(src, "§bSlot §3" + slotTicker + "§b was randomly rerolled to §3male§b!");
                    }
                    else
                    {
                        printToLog(1, "§bSlot §2" + slotTicker +
                            " §bshould be gendered. Rerolled, now §5female§b.");

                        loopValue.setInteger(NbtKeys.GENDER, 1);

                        if (!sneakyMode && targetIsValid)
                        {
                            target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                    " §dwas randomly rerolled to §5female§d!"));
                        }

                        sendCheckedMessage(src, "§dSlot §5" + slotTicker + "§d was randomly rerolled to §5female§d!");
                    }
                }
                // Is our Pokémon male (0), and is the chance of that happening 0%? Fix!
                else if (gender == 0 && malePercent == 0)
                {
                    // Increment our fix count, since we had to fix something.
                    fixCount++;

                    printToLog(1, "§bSlot §2" + slotTicker +
                            " §bis male, should be female. Fixing.");

                    loopValue.setInteger(NbtKeys.GENDER, 1);

                    if (!sneakyMode && targetIsValid)
                    {
                        target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                " §dhad its gender flipped to §5female§d!"));
                    }

                    sendCheckedMessage(src, "§dSlot §5" + slotTicker + "§d was changed to §5female§d!");
                }
                // Is our Pokémon female (1), and is the chance of it being male 100%? Fix!
                else if (gender == 1 && malePercent == 100)
                {
                    // Increment our fix count, since we had to fix something.
                    fixCount++;

                    printToLog(1, "§bSlot §2" + slotTicker +
                                                " §bis female, should be male. Fixing.");

                    loopValue.setInteger(NbtKeys.GENDER, 0);

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
            // Update the player's sidebar with the new changes.
            storageCompleted.sendUpdatedList();

            sendCheckedMessage(src, "");
            sendCheckedMessage(src, "§aWe're done! Glad to be of service.");

            if (!sneakyMode && targetIsValid)
            {
                target.sendMessage(Text.of(""));
                target.sendMessage(Text.of("§eYour party has been checked, and broken genders fixed."));
            }
        }

        if (!sneakyMode && targetIsValid)
            target.sendMessage(Text.of("§7-----------------------------------------------------"));

        sendCheckedMessage(src, "§7-----------------------------------------------------");
    }
}