// Created to fix the aftermath of a Pixelmon bug where Pokémon could inherit Ditto's lack of a gender.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
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

public class FixGenders implements CommandExecutor
{
    // Initialize a variable. We'll load stuff into these when we call the config loader.
    public static String commandAlias;

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

        if (commandAlias == null)
        {
            printToLog(0, "Could not read node \"§4commandAlias§c\".");
            printToLog(0, "This command's config could not be parsed. Exiting.");
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please check the file."));
        }
        else
        {
            if (calledRemotely)
            {
                CommonMethods.printDebugMessage("CheckStats", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean canContinue = true, targetIsValid = false, commandConfirmed = false;
            boolean hasStaffPerm = src.hasPermission("pixelupgrade.command.staff.fixgenders");
            Optional<String> arg1Optional = args.getOne("target or confirmation");
            Optional<String> arg2Optional = args.getOne("confirmation");
            Player target = null;

            if (calledRemotely)
            {
                canContinue = false; // Done so we don't need to keep repeating changes to the bool later.

                // Do we have an argument in the first slot?
                if (arg1Optional.isPresent())
                {
                    String arg1String = arg1Optional.get();

                    // Do we have a valid online player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();
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
                if (arg1Optional.isPresent())
                {
                    printToLog(2, "There's something in the first argument slot!");
                    String arg1String = arg1Optional.get();

                    // Is our calling player allowed to check other people's Pokémon, and is arg 1 a valid target?
                    if (hasStaffPerm)
                    {
                        if (Sponge.getServer().getPlayer(arg1String).isPresent())
                        {
                            if (src.getName().equalsIgnoreCase(arg1String))
                            {
                                printToLog(2, "Player targeted self. Continuing.");

                                ////noinspection ConstantConditions
                                target = (Player) src;
                            }
                            else
                            {
                                printToLog(2, "A valid non-source target was found.");
                                targetIsValid = true;
                            }

                            target = Sponge.getServer().getPlayer(arg1String).get();
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
            }

            if (canContinue)
            {
                Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));

                if (!storage.isPresent())
                {
                    printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                    src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                }
                else
                {
                    PlayerStorage storageCompleted = storage.get();

                    if (calledRemotely || commandConfirmed)
                    {
                        printToLog(2, "Starting gender checks, any fixes will be printed.");
                        fixParty(src, target, storageCompleted, targetIsValid);
                    }
                    else
                    {
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cNo confirmation was found. Please confirm to proceed."));
                        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] [-c to confirm]"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("§5Please note: §dAny broken genders will be immediately rerolled."));
                        src.sendMessage(Text.of("§dThis command is experimental. Stuff may break, report any issues!"));
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    }
                }
            }
        }

        return CommandResult.success();
	}

	private void fixParty(CommandSource src, Player target, PlayerStorage storageCompleted, boolean targetIsValid)
    {
        src.sendMessage(Text.of("§7-----------------------------------------------------"));

        if (targetIsValid)
            src.sendMessage(Text.of("§eNo slot found, checking the target's whole team."));
        else
            src.sendMessage(Text.of("§eNo slot found, checking your whole team."));

        String genderString = "§3male"; // Adjusted later, if needed.
        EntityPixelmon pokemon;
        int slotTicker = 1, fixCount = 0, pokemonGender, malePercent;
        for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
        {
            if (slotTicker > 6)
            {
                if (fixCount == 0)
                {
                    src.sendMessage(Text.of("§eEverything looks good, nothing fixable was found."));
                    printToLog(1, "We're done! Found nothing to fix, exiting.");
                }
                else
                {
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("§aWe're done! Check the messages above for more info."));
                    printToLog(1, "We're done! Broken Pokémon were found, logged and fixed, exiting.");
                }

                src.sendMessage(Text.of("§7-----------------------------------------------------"));
                break;
            }

            if (loopValue != null)
            {
                pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(loopValue, (World) target.getWorld());
                pokemonGender = loopValue.getInteger(NbtKeys.GENDER);
                malePercent = pokemon.baseStats.malePercent;

                if (pokemonGender == 0 && malePercent < 0 || pokemonGender == 1 && malePercent < 0)
                {
                    if (pokemonGender == 1)
                        genderString = "§5female";

                    src.sendMessage(Text.of("§aPokémon in slot §2" + slotTicker +
                            " §ashould not have a gender! Fixing."));
                    printToLog(1, "§aSlot §2" + slotTicker +
                            " §ashould be ungendered, currently " + genderString + "§a. Fixing.");

                    if (fixCount == 0)
                        src.sendMessage(Text.of(""));

                    if (calledRemotely)
                        target.sendMessage(Text.of("§eSlot §6" + slotTicker + "§e had its gender wiped remotely!"));
                    else if (targetIsValid)
                    {
                        target.sendMessage(Text.of("§eSlot §6" + slotTicker + "§e had its gender wiped by \"§6" +
                                src.getName() + "§e\"!"));
                    }
                    else
                        target.sendMessage(Text.of("§eSlot §6" + slotTicker + "§e had its gender wiped!"));

                    loopValue.setInteger(NbtKeys.GENDER, 2);
                    fixCount++;
                }
                else if (pokemonGender == 2 && pokemon.baseStats.malePercent >= 0)
                {
                    src.sendMessage(Text.of("§aPokémon in slot §2" + slotTicker + " §ashould have a gender! Rolling dice..."));
                    if (fixCount == 0)
                        src.sendMessage(Text.of(""));

                    if (RandomHelper.rand.nextInt(100) < malePercent)
                    {
                        printToLog(1, "§aSlot §2" + slotTicker +
                            " §ashould be gendered. Rerolled, now §3male§a.");

                        if (calledRemotely)
                        {
                            target.sendMessage(Text.of("§bSlot §3" + slotTicker +
                                    " §bwas randomly rerolled to §3male §bthrough the console!"));
                        }
                        else if (targetIsValid)
                        {
                            target.sendMessage(Text.of("§bSlot §3" + slotTicker +
                                    "§b was randomly rerolled to §3male §bby \"§3" + src.getName() + "§b\"!"));
                        }
                        else
                            target.sendMessage(Text.of("§bSlot §3" + slotTicker + "§b was randomly rerolled to §3male§b!"));

                        loopValue.setInteger(NbtKeys.GENDER, 0);
                        src.sendMessage(Text.of("§bThe targeted Pokémon is now §3male§b."));
                    }
                    else
                    {
                        printToLog(1, "§aSlot §2" + slotTicker +
                            " §ashould be gendered. Rerolled, now §5female§a.");

                        if (calledRemotely)
                        {
                            target.sendMessage(Text.of("§dSlot §5" + slotTicker +
                                    " §dwas randomly rerolled to §5female §dthrough the console!"));
                        }
                        else if (targetIsValid)
                        {
                            target.sendMessage(Text.of("§bSlot §5" + slotTicker +
                                    "§d was randomly rerolled to §5female §dby \"§5" + src.getName() + "§d\"!"));
                        }
                        else
                            target.sendMessage(Text.of("§bSlot §5" + slotTicker + "§d was randomly rerolled to §5female§!"));

                        loopValue.setInteger(NbtKeys.GENDER, 1);
                        src.sendMessage(Text.of("§dPokémon in is now §5female§d!"));
                    }

                    fixCount++;
                }
            }

            slotTicker++;
        }

        if (fixCount > 0)
        {
            printToLog(1, "No slot provided, target team was checked and fixed. Exit.");
            storageCompleted.sendUpdatedList();
        }
        else
        {
            printToLog(1, "No slot provided, could not find anything to fix on team. Exit.");
            src.sendMessage(Text.of("§eCould not find anything to fix on that player's team."));
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}