// The one and only. Accept no imitations.
package rs.expand.evenmorepixelmoncommands.commands;

import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import static rs.expand.evenmorepixelmoncommands.EMPC.commandAlias;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;

public class BaseCommand implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Are we being called by a player, or something else capable of receiving multiple messages? Beware: negation.
        if (!(src instanceof CommandBlock))
        {
            // Add a header to start things off.
            src.sendMessage(Text.of("§5-----------------------------------------------------"));

            // Show an error if the alias isn't set right. Continue after.
            String checkedAlias = commandAlias;
            if (commandAlias == null)
            {
                printBasicError("Could not read config node \"§4commandAlias§c\" while executing hub command.");
                printBasicError("We'll continue, but will fall back to the default alias. Check your config.");

                // Insert a safe default.
                checkedAlias = "empc";
            }

            src.sendMessage(Text.builder("§6/" + checkedAlias + " list")
                    .onClick(TextActions.runCommand("/empc list"))
                    .build()
            );

            if (src instanceof Player)
                src.sendMessage(Text.of("➡ §eShows a list of all EMPC commands you have access to."));
            else
            {
                // Message locked in, as it's not visible in-game. Keeps the lang workload down, with minimal loss.
                src.sendMessage(Text.of(
                        "➡ §eShows a list of all loaded EMPC commands."));
            }

            // Check for the reload permission.
            if (src.hasPermission("empc.command.staff.reload"))
            {
                src.sendMessage(Text.builder("§6/" + checkedAlias + " reload")
                        .onClick(TextActions.runCommand("/empc reload"))
                        .build()
                );

                src.sendMessage(Text.of("➡ §eReloads all EMPC configs on the fly."));
            }

            // End with a footer.
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
        else
            printBasicError("§cThis command cannot run from command blocks.");

        return CommandResult.success();
    }
}
