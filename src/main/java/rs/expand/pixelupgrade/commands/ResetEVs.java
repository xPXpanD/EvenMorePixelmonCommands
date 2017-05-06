package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class ResetEVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Integer slot = args.<Integer>getOne("slot").get();
		Player player = (Player) src;		
		
		Boolean commandConfirmed = false;
		if (!args.<String>getOne("confirm").isPresent())
			; // Do nothing! Just need the next statement to not run if this is the case, really.
		else
		{
			String confirm = args.<String>getOne("confirm").get();
			
			if (confirm.contains("confirm") || confirm.contains("true"))
				commandConfirmed = true;		
		}

	    if (slot > 6 || slot < 1)
	    	player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
	    else
	    {
	    	Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
			PlayerStorage storageCompleted = (PlayerStorage)storage.get();
			NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

			if (nbt == null)
				player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			else if (commandConfirmed)
			{
				EnumPokemon pokemonName = EnumPokemon.getFromName(nbt.getString("Name")).get();

				nbt.setInteger(NbtKeys.EV_HP, 0);
				nbt.setInteger(NbtKeys.EV_ATTACK, 0);
				nbt.setInteger(NbtKeys.EV_DEFENCE, 0);
				nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 0);
				nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 0);
				nbt.setInteger(NbtKeys.EV_SPEED, 0);

				if (nbt.getString("Nickname").equals(""))
					player.sendMessage(Text.of("\u00A76" + String.valueOf(pokemonName) + "\u00A7e had its EVs wiped."));
				else
					player.sendMessage(Text.of("\u00A7eYour \u00A76" + nbt.getString("Nickname") + "\u00A7e had its EVs wiped."));
			}
			else
			{
				player.sendMessage(Text.of("\u00A75Warning: \u00A7dYou are about to reset this Pok\u00E9mon's EVs to zero!"));
				player.sendMessage(Text.of("\u00A7bIf you want to continue, type: \u00A7a/upgrade resetevs " + slot + " confirm"));
			}
	    }
	    return CommandResult.success();
	}
}