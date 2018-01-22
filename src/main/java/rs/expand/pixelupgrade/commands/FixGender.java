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

public class FixGender implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;

    // Set up a console-checking variable for internal use.
    private boolean runningFromConsole;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    // If we're running from console, we need to swallow everything to avoid cluttering it.
    private void printToLog (int debugNum, String inputString)
    {
        if (!runningFromConsole)
            CommonMethods.printDebugMessage("FixGender", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        // Are we running from the console? Let's tell our code that. If "src" is not a Player, this becomes true.
        runningFromConsole = !(src instanceof Player);

        if (commandAlias == null)
        {
            printToLog(0, "Could not read node \"§4commandAlias§c\".");
            printToLog(0, "This command's config could not be parsed. Exiting.");
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please check the file."));
        }
        else
        {
            if (runningFromConsole)
                CommonMethods.printDebugMessage("CheckStats", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            int slot = 0;
            String targetString = null, slotString;
            boolean targetAcquired = false, canContinue = false, hasOtherPerm = false;
            Player target;

            if (src.hasPermission("pixelupgrade.command.other.fixgender"))
                hasOtherPerm = true;

            // Is the first argument present?
            if (args.<String>getOne("target or slot").isPresent())
            {
                printToLog(2, "There's something in the first argument slot!");
                targetString = args.<String>getOne("target or slot").get();

                // Is the first argument numeric, and does it look like a slot?
                if (targetString.matches("^[1-6]"))
                {
                    if (!runningFromConsole)
                    {
                        printToLog(2, "Found a slot in argument 1. Continuing to confirmation checks.");
                        slot = Integer.parseInt(targetString);
                        canContinue = true;
                    } else // Console needs a target.
                    {
                        src.sendMessage(Text.of("§4Error: §cInvalid target. See below."));
                        printSyntaxHelper(src, true);
                    }
                } else if (runningFromConsole || hasOtherPerm)
                {
                    // Is our target an actual online player?
                    if (Sponge.getServer().getPlayer(targetString).isPresent())
                    {
                        // If we're not running from console, check if the player targeted themself.
                        if (runningFromConsole || !src.getName().equalsIgnoreCase(targetString))
                        {
                            target = Sponge.getServer().getPlayer(targetString).get();
                            printToLog(2, "Found a valid online target! Printed for your convenience: §2" +
                                    target.getName());
                            targetAcquired = true;
                        } else
                            printToLog(2, "Player targeted own name. Let's pretend that didn't happen.");

                        canContinue = true;
                    } else if (targetString.matches("[a-zA-Z]+")) // Making an assumption; input is non-numeric so probably not a slot.
                    {
                        printToLog(1, "Invalid first argument. Input not numeric, assuming misspelled name. Exit.");

                        src.sendMessage(Text.of("§4Error: §cCould not find the given target. Check your spelling."));
                        printSyntaxHelper(src, runningFromConsole);
                    } else  // Throw a "safe" error that works everywhere. Might not be as clean, which is why we check patterns above.
                    {
                        printToLog(1, "Invalid first argument, input has numbers. Throwing generic error. Exit.");
                        printArg1Error(true, src, runningFromConsole);
                    }
                } else
                {
                    printToLog(1, "Invalid slot provided, and player has no \"§3other§b\" perm. Exit.");
                    printArg1Error(false, src, false);
                }
            }
            else
            {
                if (!runningFromConsole)
                {
                    printToLog(1, "No arguments found. Showing command usage. Exit.");
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide at least a slot."));
                }
                else
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. See below."));

                printSyntaxHelper(src, runningFromConsole);
            }

            if (canContinue && args.<String>getOne("slot").isPresent())
            {
                if (runningFromConsole || hasOtherPerm)
                {
                    printToLog(2, "There's something in the second argument slot! Checking.");
                    slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Found a slot in argument 2.");
                        slot = Integer.parseInt(slotString);
                    }
                    else
                    {
                        printToLog(1, "Second argument is not a valid slot. Exit.");

                        src.sendMessage(Text.of("§4Error: §cInvalid slot provided. See below."));
                        printSyntaxHelper(src, false);

                        canContinue = false;
                    }
                }
            }
            else if (targetAcquired)
            {
                printToLog(1, "Got a target, but no slot was provided. Exit.");

                src.sendMessage(Text.of("§4Error: §cInvalid slot provided. See below."));
                printSyntaxHelper(src, false);

                canContinue = false;
            }

            if (canContinue)
            {
                printToLog(2, "No errors encountered, input should be valid. Continuing!");

                Optional<PlayerStorage> storage;
                if (targetAcquired)
                {
                    target = Sponge.getServer().getPlayer(targetString).get();
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
                }
                else
                {
                    //noinspection ConstantConditions
                    target = (Player) src;
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));
                }

                if (!storage.isPresent())
                {
                    printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                    src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                }
                else
                {
                    printToLog(2, "Found a Pixelmon storage, moving on.");

                    PlayerStorage storageCompleted = storage.get();
                    NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                    if (nbt == null)
                    {
                        if (targetAcquired)
                        {
                            printToLog(1, "No Pokémon was found in the provided slot on the target. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYour target has no Pokémon in that slot!"));
                        }
                        else
                        {
                            printToLog(1, "No Pokémon was found in the provided slot. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThere's no Pokémon in that slot!"));
                        }
                    }
                    else
                    {
                        printToLog(2, "Pokémon in slot exists, starting gender checks.");

                        if (!runningFromConsole)
                        {
                            //noinspection ConstantConditions
                            target = (Player) src;
                        }

                        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) target.getWorld());
                        int pokemonGender = nbt.getInteger(NbtKeys.GENDER), malePercent = pokemon.baseStats.malePercent;

                        //pokemon.setGender(Gender.getGender(nbt.getShort("Gender")));
                        if (pokemonGender == 0 && malePercent < 0 || pokemonGender == 1 && malePercent < 0)
                        {
                            printToLog(1, "Provided Pokémon is not genderless, but should be. Fixing.");
                            nbt.setInteger(NbtKeys.GENDER, 2);

                            src.sendMessage(Text.of("§eThis Pokémon should not have a gender! Fixing."));
                            if (targetAcquired)
                                src.sendMessage(Text.of("§aThe targeted Pokémon had its gender removed."));
                            else
                                src.sendMessage(Text.of("§aYour Pokémon had its gender removed."));

                            storageCompleted.sendUpdatedList();
                        }
                        else if (pokemonGender == 2 && pokemon.baseStats.malePercent >= 0)
                        {
                            printToLog(1, "Provided Pokémon has no gender, but should have one. Fixing.");
                            src.sendMessage(Text.of("§eThis Pokémon should have a gender! Rolling the dice..."));


                            if (RandomHelper.rand.nextInt(100) < malePercent)
                            {
                                printToLog(1, "Rolled the dice and the Pokémon became male, a " +
                                        malePercent + " chance.");
                                nbt.setInteger(NbtKeys.GENDER, 0);

                                if (targetAcquired)
                                    src.sendMessage(Text.of("§bThe targeted Pokémon is now male."));
                                else
                                    src.sendMessage(Text.of("§bYour Pokémon is now male!"));
                            }
                            else
                            {
                                printToLog(1, "Rolled the dice and the Pokémon became male, a " +
                                        (100 - malePercent) + " chance.");
                                nbt.setInteger(NbtKeys.GENDER, 1);

                                if (targetAcquired)
                                    src.sendMessage(Text.of("§dThe targeted Pokémon is now female."));
                                else
                                    src.sendMessage(Text.of("§dYour Pokémon is now female!"));
                            }

                            storageCompleted.sendUpdatedList();
                        }
                        else
                        {
                            printToLog(1, "The targeted Pokémon seems to be fine already. Exit.");
                            src.sendMessage(Text.of("§aThe targeted Pokémon looks good, nothing to do here!"));
                        }
                    }
                }
            }
        }

        return CommandResult.success();
	}

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(CommandSource src, boolean isConsole)
    {
        if (isConsole || src.hasPermission("pixelupgrade.command.other.fixgender"))
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [optional target] <slot, 1-6>"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> §7(no perms for target)"));
    }

    // Called when we need to show an error, but the error is dependant on perms/settings.
    private void printArg1Error(boolean hasOtherPerm, CommandSource src, boolean isConsole)
    {
        if (isConsole)
            src.sendMessage(Text.of("§4Error: §cInvalid target. See below."));
        else if (hasOtherPerm)
            src.sendMessage(Text.of("§4Error: §cInvalid target or slot on first argument. See below."));
        else
            src.sendMessage(Text.of("§4Error: §cInvalid slot provided. See below."));

        printSyntaxHelper(src, isConsole);
    }
}