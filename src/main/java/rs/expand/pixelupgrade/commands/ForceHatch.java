package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;

public class ForceHatch implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;

    // Set up a console-checking variable for internal use.
    private boolean calledRemotely;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    // If we're running from console, we need to swallow everything to avoid cluttering it.
    private void printToLog (int debugNum, String inputString)
    {
        if (!calledRemotely)
            CommonMethods.printDebugMessage("ForceHatch", debugNum, inputString);
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
                CommonMethods.printDebugMessage("ForceHatch", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            int slot = 0;
            String targetString = null, slotString;
            boolean targetAcquired = false, canContinue = false;
            Player target;

            // Is the first argument present?
            if (args.<String>getOne("target or slot").isPresent())
            {
                printToLog(2, "There's something in the first argument slot!");
                targetString = args.<String>getOne("target or slot").get();

                // Is the first argument numeric, and does it look like a slot?
                if (targetString.matches("^[1-6]"))
                {
                    if (!calledRemotely)
                    {
                        printToLog(2, "Found a slot in argument 1. Continuing to confirmation checks.");
                        slot = Integer.parseInt(targetString);
                        canContinue = true;
                    }
                    else // Console needs a target.
                    {
                        src.sendMessage(Text.of("§4Error: §cInvalid target. See below."));
                        printSyntaxHelper(src, true);
                    }
                }
                else
                {
                    // Is our target an actual online player?
                    if (Sponge.getServer().getPlayer(targetString).isPresent())
                    {
                        // If we're not running from console, check if the player targeted themself.
                        if (calledRemotely || !src.getName().equalsIgnoreCase(targetString))
                        {
                            target = Sponge.getServer().getPlayer(targetString).get();
                            printToLog(2, "Found a valid online target! Printed for your convenience: §2" +
                                    target.getName());
                            targetAcquired = true;
                        }
                        else
                            printToLog(2, "Player targeted own name. Let's pretend that didn't happen.");

                        canContinue = true;
                    }
                    else
                    {
                        printToLog(1, "Invalid first argument, input is numeric. Showing generic error.");

                        if (calledRemotely)
                            src.sendMessage(Text.of("§4Error: §cInvalid target. See below."));
                        else
                            src.sendMessage(Text.of("§4Error: §cInvalid target or slot provided. See below."));

                        printSyntaxHelper(src, calledRemotely);
                    }
                }
            }
            else
            {
                printToLog(1, "No arguments provided. Exit.");

                if (calledRemotely)
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. See below."));
                else
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide at least a slot."));

                printSyntaxHelper(src, calledRemotely);
            }

            if (!calledRemotely)
            {
                printToLog(2, "We're not running from console, moving on to secondary checks.");

                if (canContinue && args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "There's something in the second argument slot! Checking.");
                    slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Found a slot in argument 2.");
                        slot = Integer.parseInt(slotString);

                        canContinue = true;
                    }
                    else
                    {
                        printToLog(1, "Second argument is not a slot. Exit.");

                        src.sendMessage(Text.of("§4Error: §cInvalid slot provided. See below."));
                        printSyntaxHelper(src, false);
                    }
                }
                else if (canContinue)
                {
                    printToLog(1, "Failed final check, no slot was found. Exit.");

                    src.sendMessage(Text.of("§4Error: §cCould not find a valid slot. See below."));
                    printSyntaxHelper(src, false);
                }
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
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

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
                        printToLog(1, "No Pokémon was found in the provided slot. Abort, abort!");
                        src.sendMessage(Text.of("§4Error: §cThere's nothing in that slot!"));
                    }
                    else if (!nbt.getBoolean("isEgg"))
                    {
                        printToLog(1, "Tried to hatch an actual Pokémon. Since that's too brutal, let's exit.");
                        src.sendMessage(Text.of("§4Error: §cThat's not an egg. Don't hatch actual Pokémon, kids!"));
                    }
                    else
                    {
                        printToLog(1, "Passed all checks, hatching us an egg!");

                        nbt.setBoolean("isEgg", false);
                        storageCompleted.changePokemonAndAssignID(slot - 1, nbt);

                        src.sendMessage(Text.of("§aCongratulations, it's a healthy baby §2" + nbt.getString("Name") + "§a!"));
                    }
                }
            }
        }

        return CommandResult.success();
    }

    // Might look a bit odd, but done this way so we only have to edit one message if this ever changes.
    private void printSyntaxHelper(CommandSource src, boolean isConsole)
    {
        if (isConsole)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target> <slot, 1-6>"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [optional target] <slot, 1-6>"));
    }
}