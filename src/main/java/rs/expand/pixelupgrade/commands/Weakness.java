package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import rs.expand.pixelupgrade.PixelUpgrade;

public class Weakness implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Player player = (Player) src;

        PixelUpgrade.log.info("\u00A7bWeakness: Called by player " + player.getName() + ", starting command.");

        return CommandResult.success();
    }
}

/*package rs.expand.pixelupgrade.listeners;

        import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
        import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
        import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

        import rs.expand.pixelupgrade.PixelUpgrade;

        import java.util.ArrayList;

public class BattleStartTracker
{
    public static BattleParticipant[] team1;
    public static BattleParticipant[] team2;
    @SubscribeEvent
    public void battleStartTracker(BattleStartedEvent battle)
    {
        BattleParticipant[] team1 = battle.participant1;
        BattleParticipant[] team2 = battle.participant2;
        PixelUpgrade.log.info("\u00A7aEvent fired!" + team1 + " | " + team2);
    }
}
*/