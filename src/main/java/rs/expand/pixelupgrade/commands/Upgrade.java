package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class Upgrade implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
		Boolean hasNoPermission = true;

		player.sendMessage(Text.of("\u00A75---------------- \u00A7dPixelUpgrade commands \u00A75----------------"));
		if (player.hasPermission("pixelupgrade.commands.fixevs"))
		{
			player.sendMessage(Text.of("\u00A76/fixevs <slot, 1-6>"));
			player.sendMessage(Text.of("\u00A7f --> \u00A7eEVs above 252 are wasted. This command fixes them!"));
			hasNoPermission = false;
		}
		if (player.hasPermission("pixelupgrade.commands.resetevs"))
		{
            player.sendMessage(Text.of("\u00A76/resetevs <slot, 1-6> [confirmation flag]"));
            player.sendMessage(Text.of("\u00A7f --> \u00A7eResets all EVs down to zero, when confirmed."));
            hasNoPermission = false;
        }
        if (player.hasPermission("pixelupgrade.commands.upgradeivs"))
        {
            player.sendMessage(Text.of("\u00A76/upgrade ivs <slot, 1-6> <IV type> (amount of times, optional)"));
            player.sendMessage(Text.of("\u00A7f --> \u00A7eBuy upgrades for your Pok\u00E9mon's stats!"));
            hasNoPermission = false;
        }
        if (player.hasPermission("pixelupgrade.commands.getstats"))
        {
            player.sendMessage(Text.of("\u00A76/getstats or /gs (player name, optional) <slot, 1-6>"));
            player.sendMessage(Text.of("\u00A7f --> \u00A7eLists many of a Pok\u00E9mon's stats in one place."));
            hasNoPermission = false;
        }
        if (player.hasPermission("pixelupgrade.commands.fuseditto"))
        {
            player.sendMessage(Text.of("\u00A76TODO"));
            player.sendMessage(Text.of("\u00A7f --> \u00A7eTODO"));
            hasNoPermission = false;
        }
		if (player.hasPermission("pixelupgrade.commands.admin.force"))
        {
            player.sendMessage(Text.of("\u00A76/upgrade force <slot, 1-6> <stat> <value> [force flag]"));
            player.sendMessage(Text.of("\u00A7f --> \u00A7eChange supported stats freely, or pass -f and go crazy."));
            hasNoPermission = false;
        }
        if (player.hasPermission("pixelupgrade.commands.admin.forcehatch"))
        {
            player.sendMessage(Text.of("\u00A76/forcehatch <slot or target player> (slot, 1-6, optional)"));
            player.sendMessage(Text.of("\u00A7f --> \u00A7eHatch eggs instantly, without any cooldowns!"));
            hasNoPermission = false;
        }

        if (hasNoPermission)
            player.sendMessage(Text.of("\u00A7cYou do not have any of this plugin's permissions. Sorry!"));

		player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

		return CommandResult.success();
	}
}