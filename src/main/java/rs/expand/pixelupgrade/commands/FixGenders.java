// Created to fix the aftermath of a Pixelmon bug where Pokémon could inherit Ditto's lack of a gender.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.RandomHelper;
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
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;

// TODO: Add a -s flag to allow silent messages even if sneakyMode is off?
public class FixGenders implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    public static String commandAlias;
    public static Boolean sneakyMode;

    // Set up a console-checking variable for internal use.
    private boolean calledRemotely;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    // If we're running from console, we need to swallow everything to avoid cluttering it.
    private void printToLog (int debugNum, String inputString)
    {
        if (!calledRemotely)
            CommonMethods.printDebugMessage("FixGenders", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        // Are we running from the console? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        ArrayList<String> nativeErrorArray = new ArrayList<>();
        if (commandAlias == null)
            nativeErrorArray.add("commandAlias");
        if (sneakyMode == null)
            nativeErrorArray.add("sneakyMode");

        if (!nativeErrorArray.isEmpty())
        {
            CommonMethods.printCommandNodeError("FixGenders", nativeErrorArray);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else
        {
            if (calledRemotely)
            {
                CommonMethods.printDebugMessage("FixGenders", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean canContinue = true, targetIsValid = false, commandConfirmed = false;
            boolean hasStaffPerm = src.hasPermission("pixelupgrade.command.staff.fixgenders");
            Optional<String> arg1Optional = args.getOne("target/confirmation");
            Optional<String> arg2Optional = args.getOne("confirmation");
            Player target = null;

            if (calledRemotely)
            {
                canContinue = false; // Done so we can avoid some bool-flipping code later, in our "called by player" checks.

                // Do we have an argument in the first slot?
                if (arg1Optional.isPresent())
                {
                    String arg1String = arg1Optional.get();

                    // Do we have a valid online player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();
                        targetIsValid = true;
                        canContinue = true;
                    }
                    else
                        src.sendMessage(Text.of("§4Error: §cInvalid target on first argument. See below."));
                }
                else
                    src.sendMessage(Text.of("§4Error: §cNo target found. See below."));

                if (!canContinue)
                    src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target>"));
            }
            else
            {
                printToLog(2, "Starting argument check for player's input.");

                //noinspection ConstantConditions - safe, we've already guaranteed we're not running from console.
                target = (Player) src;

                if (arg1Optional.isPresent() && arg1Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 2.");
                    commandConfirmed = true;
                }
                else if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 3.");
                    commandConfirmed = true;
                }

                // Start checking argument 1 for non-flag contents.
                if (arg1Optional.isPresent() && !arg1Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "There's something in the first argument slot!");
                    String arg1String = arg1Optional.get();

                    // Is our calling player allowed to check other people's Pokémon, and is arg 1 a valid target?
                    if (hasStaffPerm)
                    {
                        printToLog(2, "Player has the staff permission. Checking for target.");

                        if (Sponge.getServer().getPlayer(arg1String).isPresent())
                        {
                            if (src.getName().equalsIgnoreCase(arg1String))
                                printToLog(2, "Player targeted self. Continuing.");
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
                            src.sendMessage(Text.of("§4Error: §cInvalid target on first argument. See below."));
                            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] [-c to confirm]"));
                            canContinue = false;
                        }
                    }
                }
                else
                    printToLog(2, "No target was found, targeting source player.");
            }

            if (canContinue)
            {
                if (calledRemotely || commandConfirmed)
                {
                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
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
                            printToLog(1, "Checking fixing for player §3" + target.getName() +
                                    "§b, informing if need be.");
                        }

                        src.sendMessage(Text.of("§7-----------------------------------------------------"));

                        if (hasStaffPerm)
                        {
                            if (targetIsValid)
                                src.sendMessage(Text.of("§eTarget found, checking their whole team..."));
                            else
                                src.sendMessage(Text.of("§eNo target found, checking your whole team..."));
                        }
                        else
                            src.sendMessage(Text.of("§eChecking your whole team..."));

                        fixParty(src, target, storage.get(), targetIsValid);
                    }
                }
                else
                {
                    printToLog(1, "No confirmation provided, printing warning and aborting.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo confirmation was found. Please confirm to proceed."));

                    if (hasStaffPerm)
                        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] {-c to confirm}"));
                    else
                        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " {-c to confirm}"));

                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("§5Please note: §dAny broken genders will be immediately rerolled."));
                    src.sendMessage(Text.of("§dThis command is experimental. Stuff may break, report issues!"));
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                }
            }
        }

        return CommandResult.success();
	}

	private void fixParty(CommandSource src, Player target, PlayerStorage storageCompleted, boolean targetIsValid)
    {
        EntityPixelmon pokemon;
        int slotTicker = 1, fixCount = 0, pokemonGender, malePercent;
        for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
        {
            if (slotTicker > 6)
                break;

            if (loopValue != null)
            {
                pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(loopValue, (World) target.getWorld());
                pokemonGender = loopValue.getInteger(NbtKeys.GENDER);
                malePercent = pokemon.baseStats.malePercent;

                if (pokemonGender > 1 && pokemon.baseStats.malePercent >= 0)
                {
                    if (fixCount == 0)
                        src.sendMessage(Text.of(""));

                    fixCount++;

                    if (fixCount == 1 && !sneakyMode && targetIsValid)
                    {
                        target.sendMessage(Text.of("§7-----------------------------------------------------"));
                        target.sendMessage(Text.of("§6" + src.getName() +
                                " §eis checking your party for broken genders."));
                        target.sendMessage(Text.of(""));
                    }

                    if (RandomHelper.rand.nextInt(100) < malePercent)
                    {
                        printToLog(1, "§bSlot §3" + slotTicker +
                            " §bshould be gendered. Rerolled, now §3male§b.");

                        loopValue.setInteger(NbtKeys.GENDER, 0);

                        if (!sneakyMode && targetIsValid)
                        {
                            if (calledRemotely)
                            {
                                target.sendMessage(Text.of("§bSlot §3" + slotTicker +
                                        " §bwas randomly rerolled to §3male §bthrough the console!"));
                            }
                            else
                            {
                                target.sendMessage(Text.of("§bSlot §3" + slotTicker +
                                        " §bwas randomly rerolled to §3male§b!"));
                            }
                        }

                        src.sendMessage(Text.of("§bSlot §3" + slotTicker + "§b was randomly rerolled to §3male§b!"));
                    }
                    else
                    {
                        printToLog(1, "§aSlot §2" + slotTicker +
                            " §bshould be gendered. Rerolled, now §5female§b.");

                        loopValue.setInteger(NbtKeys.GENDER, 1);

                        if (!sneakyMode && targetIsValid)
                        {
                            if (calledRemotely)
                            {
                                target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                        " §dwas randomly rerolled to §5female §dthrough the console!"));
                            }
                            else
                            {
                                target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                        " §dwas randomly rerolled to §5female§d!"));
                            }
                        }

                        src.sendMessage(Text.of("§dSlot §5" + slotTicker + "§d was randomly rerolled to §5female§d!"));
                    }
                }
            }

            slotTicker++;
        }

        if (fixCount == 0)
        {
            src.sendMessage(Text.of("§eEverything looks good, nothing fixable was found."));
            printToLog(1, "We're done! Found nothing to fix, exiting.");
        }
        else
        {
            storageCompleted.sendUpdatedList();

            src.sendMessage(Text.of(""));
            src.sendMessage(Text.of("§aWe're done! Glad to be of service."));
            printToLog(1, "We're done! Broken Pokémon were found, logged and fixed, exiting.");

            if (!sneakyMode)
            {
                target.sendMessage(Text.of(""));
                target.sendMessage(Text.of("§eYour party has been checked, and glitched genders fixed."));
                target.sendMessage(Text.of("§7-----------------------------------------------------"));
            }
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}