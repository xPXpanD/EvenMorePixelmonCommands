// Might be nice on command blocks?
package rs.expand.evenmorepixelmoncommands.commands;

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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;

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
            PrintingMethods.printUnformattedMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }

    // Set up a class name variable for internal use. We'll pass this to logging when showing a source is desired.
    private String sourceName = this.getClass().getName();

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        if (commandAlias == null)
        {
            printSourcedError(sourceName, "Could not read node \"§4commandAlias§c\".");
            sendCheckedMessage(src, "§4Error: §cThis command's config is invalid! Please check the file.");
        }
        else
        {
            final Optional<String> arg1Optional = args.getOne("target/slot");
            final Optional<String> arg2Optional = args.getOne("slot");
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
                        target = Sponge.getServer().getPlayer(arg1String).get();
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target on first argument. See below.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cNo arguments found. See below.");
                    return CommandResult.empty();
                }

                // Did we survive the argument 1 check? Is argument 2 present?
                if (arg2Optional.isPresent())
                {
                    final String arg2String = arg2Optional.get();

                    // Do we have a slot?
                    if (arg2String.matches("^[1-6]"))
                        slot = Integer.parseInt(arg2String);
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid slot on second argument. See below.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cMissing slot on second argument. See below.");
                    return CommandResult.empty();
                }
            }
            else
            {
                // Start checking arguments for non-flag contents. First up: argument 1.
                if (arg1Optional.isPresent())
                {
                    final String arg1String = arg1Optional.get();

                    // Do we have a slot?
                    if (arg1String.matches("^[1-6]"))
                        slot = Integer.parseInt(arg1String);
                    // Failing that, do we have a target?
                    else if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        // Check if the player is targeting themselves. (if they are, just let target stay null)
                        if (!src.getName().equalsIgnoreCase(arg1String))
                            target = Sponge.getServer().getPlayer(arg1String).get();
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target or slot on first argument. See below.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cNo arguments found. See below.");
                    return CommandResult.empty();
                }

                // Can we continue, and do we not have a slot already? Check arg 2 for one.
                // Keep in mind: canContinue is now inverted, so we have to explicitly set false on hitting an error.
                if (slot == 0)
                {
                    if (arg2Optional.isPresent())
                    {
                        final String arg2String = arg2Optional.get();

                        // Do we have a slot, and was the slot not set yet?
                        if (arg2String.matches("^[1-6]"))
                            slot = Integer.parseInt(arg2String);
                        else
                        {
                            printLocalError(src, "§4Error: §cInvalid slot on second argument. See below.");
                            return CommandResult.empty();
                        }
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cMissing slot on second argument. See below.");
                        return CommandResult.empty();
                    }
                }
            }

            // Do some battle checks. Only hittable if we got called by an actual Player.
            if (target == null && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
                sendCheckedMessage(src, "§4Error: §cYou can't use this command while in a battle!");
            else if (target != null && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
            else
            {
                // Get the player's party, and then get the Pokémon in the targeted slot.
                final Pokemon pokemon;
                if (target != null)
                    pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) target).get(slot);
                else
                    pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot);

                if (pokemon == null)
                    sendCheckedMessage(src, "§4Error: §cThere's nothing in that slot!");
                else if (!pokemon.isEgg())
                    sendCheckedMessage(src, "§4Error: §cThat's not an egg. Don't hatch actual Pokémon, kids!");
                else
                {
                    // Hatch us an egg!
                    pokemon.hatchEgg();

                    // Update the player's sidebar with the new changes.
                    printBasicError("Yo, did it update? If not, TODO.");

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
    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));
        if (calledRemotely)
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target> <slot, 1-6>");
        else
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target?] <slot, 1-6>");
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}