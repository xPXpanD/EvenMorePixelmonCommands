// Might be nice on command blocks?
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import java.util.Optional;
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
import rs.expand.pixelupgrade.utilities.PrintingMethods;

public class ForceHatch implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias;

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
            PrintingMethods.printDebugMessage("ForceHatch", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        if (commandAlias == null)
        {
            printToLog(0, "Could not read node \"§4commandAlias§c\".");
            printToLog(0, "This command's config could not be parsed. Exiting.");
            sendCheckedMessage(src, "§4Error: §cThis command's config is invalid! Please check the file.");
        }
        else
        {
            if (calledRemotely)
            {
                if (src instanceof CommandBlock)
                {
                    PrintingMethods.printDebugMessage("ForceHatch", 1,
                            "Called by command block, starting. Silencing logger messages.");
                }
                else
                {
                    PrintingMethods.printDebugMessage("ForceHatch", 1,
                            "Called by console, starting. Silencing further log messages.");
                }
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean canContinue = false;
            final Optional<String> arg1Optional = args.getOne("target/slot");
            final Optional<String> arg2Optional = args.getOne("slot");
            String errorString = "§4There's an error message missing, please report this!";
            Player target = null;
            int slot = 0;

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
                        final String arg2String = arg2Optional.get();

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
                    final String arg1String = arg1Optional.get();

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
                        final String arg2String = arg2Optional.get();

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
                sendCheckedMessage(src, errorString);
                printSyntaxHelper(src);
            }
            // Do some battle checks. Only hittable if we got called by an actual Player.
            else if (target == null && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                printToLog(0, "Player tried to hatch own Pokémon while in a battle. Exit.");
                sendCheckedMessage(src, "§4Error: §cYou can't use this command while in a battle!");
            }
            else if (target != null && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
            {
                printToLog(0, "Target was in a battle, cannot proceed. Exit."); // Swallowed if console.
                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
            }
            else
            {
                // Get the player's party, and then get the Pokémon in the targeted slot.
                final Pokemon pokemon;
                if (target != null)
                    pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) target).get(slot);
                else
                    pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot);

                if (pokemon == null)
                {
                    printToLog(1, "No Pokémon was found in the provided slot. Abort, abort!");
                    sendCheckedMessage(src, "§4Error: §cThere's nothing in that slot!");
                }
                else if (!pokemon.isEgg())
                {
                    printToLog(1, "Tried to hatch an actual Pokémon. That's too brutal; let's exit.");
                    sendCheckedMessage(src, "§4Error: §cThat's not an egg. Don't hatch actual Pokémon, kids!");
                }
                else
                {
                    printToLog(1, "Passed all checks, hatching us an egg!");
                    pokemon.hatch();

                    // Update the player's sidebar with the new changes.
                    printGenericError("Yo, did it update? If not, TODO.");

                    if (calledRemotely)
                    {
                        // http://i0.kym-cdn.com/photos/images/original/000/625/834/a48.png
                        sendCheckedMessage(src, "§aCracked open a healthy §2" +
                                pokemon.getSpecies().getLocalizedName() + "§a.");
                    }
                    else
                    {
                        sendCheckedMessage(src, "§aCongratulations, it's a healthy baby §2" +
                                pokemon.getSpecies().getLocalizedName() + "§a!");
                    }
                }
            }
        }

        return CommandResult.success();
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(final CommandSource src)
    {
        if (calledRemotely)
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target> <slot, 1-6>");
        else
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?] <slot, 1-6>");
    }
}