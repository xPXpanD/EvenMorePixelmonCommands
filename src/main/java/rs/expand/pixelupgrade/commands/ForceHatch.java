// Might be nice on command blocks?
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

            boolean canContinue = false;
            Optional<String> arg1Optional = args.getOne("target or slot");
            Optional<String> arg2Optional = args.getOne("slot");
            String errorString = "ERROR PLEASE REPORT";
            Player target = null;
            int slot = 0;

            if (calledRemotely)
            {
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
                        errorString = "§4Error: §cInvalid target on first argument. See below.";
                }
                else
                    errorString = "§4Error: §cNo arguments found. See below.";

                // Did we survive the argument 1 check?
                // Keep in mind: canContinue is now inverted, so we have to explicitly set false on hitting an error.
                if (canContinue)
                {
                    // Is argument 2 present?
                    if (arg2Optional.isPresent())
                    {
                        String arg2String = arg2Optional.get();

                        // Do we have a slot?
                        if (arg2String.matches("^[1-6]"))
                            slot = Integer.parseInt(arg2String);
                        else
                        {
                            canContinue = false;
                            errorString = "§4Error: §cInvalid slot on second argument. See below.";
                        }
                    }
                    else
                    {
                        canContinue = false;
                        errorString = "§4Error: §cMissing slot on second argument. See below.";
                    }
                }
            }
            else
            {
                printToLog(2, "Starting argument check for player's input.");

                // Start checking arguments for non-flag contents. First up: argument 1.
                if (arg1Optional.isPresent())
                {
                    printToLog(2, "There's something in the first argument slot!");
                    String arg1String = arg1Optional.get();

                    // Do we have a slot?
                    if (arg1String.matches("^[1-6]"))
                    {
                        printToLog(2, "Found a valid slot in argument 1.");
                        slot = Integer.parseInt(arg1String);
                        canContinue = true;
                    }
                    // Failing that, do we have a target?
                    else if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        if (!src.getName().equalsIgnoreCase(arg1String))
                        {
                            printToLog(2, "Found a valid target in argument 1.");
                            target = Sponge.getServer().getPlayer(arg1String).get();
                        }
                        else
                            printToLog(2, "Player targeted self. Continuing.");

                        canContinue = true;
                    }
                    else
                    {
                        printToLog(1, "Invalid target or slot on first argument. Exit.");
                        errorString = "§4Error: §cInvalid target or slot on first argument. See below.";
                    }
                }
                else
                {
                    printToLog(1, "No arguments were found. Exit.");
                    errorString = "§4Error: §cNo arguments found. See below.";
                }

                // Can we continue, and do we not have a slot already? Check arg 2 for one.
                // Keep in mind: canContinue is now inverted, so we have to explicitly set false on hitting an error.
                if (canContinue && slot == 0)
                {
                    if (arg2Optional.isPresent())
                    {
                        printToLog(2, "There's something in the second argument slot!");
                        String arg2String = arg2Optional.get();

                        // Do we have a slot, and was the slot not set yet?
                        if (arg2String.matches("^[1-6]"))
                        {
                            printToLog(2, "Found a valid slot in argument 2. Moving to execution.");
                            slot = Integer.parseInt(arg2String);
                        }
                        else
                        {
                            printToLog(1, "Invalid slot on second argument. Exit.");
                            errorString = "§4Error: §cInvalid slot on second argument. See below.";
                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(1, "Missing slot on second argument. Exit.");
                        errorString = "§4Error: §cMissing slot on second argument. See below.";
                        canContinue = false;
                    }
                }
            }

            if (!canContinue)
            {
                src.sendMessage(Text.of(errorString));
                printSyntaxHelper(src);
            }
            else
            {
                Optional<PlayerStorage> storage;
                if (target != null)
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
                else
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                if (!storage.isPresent())
                {
                    printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                    src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                }
                else
                {
                    PlayerStorage storageCompleted = storage.get();
                    NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                    if (nbt == null)
                    {
                        printToLog(1, "No Pokémon was found in the provided slot. Abort, abort!");
                        src.sendMessage(Text.of("§4Error: §cThere's nothing in that slot!"));
                    }
                    else if (!nbt.getBoolean("isEgg"))
                    {
                        printToLog(1, "Tried to hatch an actual Pokémon. That's too brutal; let's exit.");
                        src.sendMessage(Text.of("§4Error: §cThat's not an egg. Don't hatch actual Pokémon, kids!"));
                    }
                    else
                    {
                        printToLog(1, "Passed all checks, hatching us an egg!");

                        nbt.setBoolean("isEgg", false);
                        storageCompleted.changePokemonAndAssignID(slot - 1, nbt);

                        if (calledRemotely)
                        {
                            // http://i0.kym-cdn.com/photos/images/original/000/625/834/a48.png
                            src.sendMessage(Text.of("§aCracked open a healthy §2" +
                                    nbt.getString("Name") + "§a."));
                        }
                        else
                        {
                            src.sendMessage(Text.of("§aCongratulations, it's a healthy baby §2" +
                                    nbt.getString("Name") + "§a!"));
                        }
                    }
                }
            }
        }

        return CommandResult.success();
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(CommandSource src)
    {
        if (calledRemotely)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target> <slot, 1-6>"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target] <slot, 1-6>"));
    }
}