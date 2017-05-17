package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import rs.expand.pixelupgrade.PixelUpgrade;

public class ResetEVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Integer slot = args.<Integer>getOne("slot").get();
		Player player = (Player) src;
		Boolean commandConfirmed = false;
		String confirm;

        PixelUpgrade.log.info("\u00A7bResetEVs debug: Called by player " + player.getName() + ", starting command.");

		if (args.<String>getOne("confirm").isPresent())
        {
            confirm = args.<String>getOne("confirm").get();

            if (confirm.contains("confirm") || confirm.contains("true"))
                commandConfirmed = true;

            PixelUpgrade.log.info("\u00A7aResetEVs debug: Confirm argument passed. Argument: " + confirm);
        }

	    if (slot > 6 || slot < 1)
	    	player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
	    else
	    {
	    	Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
			PlayerStorage storageCompleted = storage.get();
			NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

			if (nbt == null)
				player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			else if (commandConfirmed)
			{
                Integer EVHP = nbt.getInteger(NbtKeys.EV_HP);
                Integer EVATT = nbt.getInteger(NbtKeys.EV_ATTACK);
                Integer EVDEF = nbt.getInteger(NbtKeys.EV_DEFENCE);
                Integer EVSPATT = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
                Integer EVSPDEF = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
                Integer EVSPD = nbt.getInteger(NbtKeys.EV_SPEED);

                PixelUpgrade.log.info("\u00A7aResetEVs debug: Command has been confirmed, printing old EVs...");
                PixelUpgrade.log.info("\u00A7aResetEVs debug: Starting wipe, old EVs: " + EVHP + " " + EVATT + " " + EVDEF + " " + EVSPATT + " " + EVSPDEF + " " + EVSPD);

				nbt.setInteger(NbtKeys.EV_HP, 0);
				nbt.setInteger(NbtKeys.EV_ATTACK, 0);
				nbt.setInteger(NbtKeys.EV_DEFENCE, 0);
				nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 0);
				nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 0);
				nbt.setInteger(NbtKeys.EV_SPEED, 0);

				if (nbt.getString("Nickname").equals(""))
					player.sendMessage(Text.of("\u00A76" + nbt.getString("Name") + "\u00A7e had its EVs wiped."));
				else
					player.sendMessage(Text.of("\u00A7eYour \u00A76" + nbt.getString("Nickname") + "\u00A7e had its EVs wiped."));

                PixelUpgrade.log.info("\u00A7aResetEVs debug: Wipe succesful, values set to zero.");
			}
			else
			{
				player.sendMessage(Text.of("\u00A75Warning: \u00A7dYou are about to reset this Pok\u00E9mon's EVs to zero!"));
				player.sendMessage(Text.of("\u00A7bIf you want to continue, type: \u00A7a/resetevs " + slot + " confirm"));

                PixelUpgrade.log.info("\u00A7aResetEVs debug: Command not confirmed by player, aborting.");
			}
	    }
        PixelUpgrade.log.info("\u00A7bResetEVs debug: Command ended.");
	    return CommandResult.success();
	}
}