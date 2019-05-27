// Based on a suggestion by Mikirae. Requires confirmation and a target, always. Dangerous stuff!
package rs.expand.evenmorepixelmoncommands.commands;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResetDex implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias;

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        // Are we running from console or command blocks? We'll flag this true, and proceed accordingly.
        final boolean calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        // TODO: Override printCommandNodeError and move these single-parameter classes over there.
        final List<String> commandErrorList = new ArrayList<>();
        if (commandAlias == null)
            commandErrorList.add("commandAlias");

        if (!commandErrorList.isEmpty())
        {
            PrintingMethods.printCommandNodeError("ResetDex", commandErrorList);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else
        {
            final Optional<String> arg1Optional = args.getOne("target");
            final Player target;

            // Do we have an argument in the first slot?
            if (arg1Optional.isPresent())
            {
                final String arg1String = arg1Optional.get();

                // Do we have a valid online player?
                if (Sponge.getServer().getPlayer(arg1String).isPresent())
                {
                    target = Sponge.getServer().getPlayer(arg1String).get();

                    // Do we have an argument in the second slot? A bit ugly, but it'll do.
                    if (!args.hasAny("c"))
                    {
                        printLocalError(src, "§4Error: §cNo confirmation was found. Please confirm to proceed.");
                        return CommandResult.success();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cInvalid target.");
                    return CommandResult.empty();
                }
            }
            else
            {
                printLocalError(src, "§4Error: §cNo target found.");
                return CommandResult.empty();
            }

            // Wipe. RIP.
            // TODO: Re-register PC and party Pokémon automatically.
            Pixelmon.storageManager.getParty((EntityPlayerMP) target).pokedex.wipe();

            // Print results.
            if (!calledRemotely)
            {
                if (target == src)
                    src.sendMessage(Text.of("§aYour Pokédex has been wiped."));
                else
                    src.sendMessage(Text.of("§aPlayer " + target.getName() + "'s Pokédex has been wiped."));
            }

            // Always throw a message into console, this is important.
            PrintingMethods.printUnformattedMessage("§aPlayer " + target.getName() + "'s Pokédex has been wiped.");

            // Inform the target player of their lots. Sorry.
            if (target != src)
                target.sendMessage(Text.of("§dYour Pokédex has been wiped by staff."));
        }

        return CommandResult.success();
	}

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        sendCheckedMessage(src, "§5-----------------------------------------------------");
        sendCheckedMessage(src, input);
        sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target> {-c to confirm}");
        sendCheckedMessage(src, "§5-----------------------------------------------------");
    }

    // Allows us to redirect printed messages away from command blocks, and into the console if need be.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printUnformattedMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }
}
