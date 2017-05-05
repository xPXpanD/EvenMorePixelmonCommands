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
		if (src instanceof Player) 
		{
			Player player = (Player) src;
			
			player.sendMessage(Text.of("\u00A75-------------------- \u00A7dcommands \u00A75--------------------"));
			if (player.hasPermission("pixelupgrade.commands.fixevs"))
				player.sendMessage(Text.of("\u00A76/fixevs <slot>\u00A7f -- \u00A7eFixes EVs above 252 (which are wasted)."));
			if (player.hasPermission("pixelupgrade.commands.resetevs"))
				player.sendMessage(Text.of("\u00A76/resetevs <slot> (confirm)\u00A7f -- \u00A7eResets all EVs to zero."));
			if (player.hasPermission("pixelupgrade.commands.getstats"))
				player.sendMessage(Text.of("\u00A76/getstats (player) <slot>\u00A7f -- \u00A7eLists all of a Pok\u00E9mon's stats."));
			if (player.hasPermission("pixelupgrade.commands.ivs"))
				player.sendMessage(Text.of("\u00A76/upgrade ivs <slot> <IV> <value>\u00A7f -- \u00A7eUpgrades IVs for money."));
			if (player.hasPermission("pixelupgrade.commands.resize"))
				player.sendMessage(Text.of("\u00A76/upgrade resize <slot> <size>\u00A7f -- \u00A7eChanges sizes for money."));
			if (player.hasPermission("pixelupgrade.commands.admin.force"))	
				player.sendMessage(Text.of("\u00A76/upgrade force <slot> <type> <value> (-f)\u00A7f -- \u00A7eSee command."));
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
		}
		else
			System.out.println("\u00A74Error: \u00A7cSorry, but there are no console commands yet.");
		
		return CommandResult.success();
	}
}