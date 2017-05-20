package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

/**
 * Created by Justin on 20-5-2017.
 */
public class FuseDitto implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Integer slot1 = 0, slot2 = 0;
        Boolean commandConfirmed = false, canContinue = true;

        return CommandResult.success();
    }
}
