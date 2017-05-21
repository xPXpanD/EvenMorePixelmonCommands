package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.util.Optional;

public class FuseDitto implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Integer slot1 = 0, slot2 = 0;
        Boolean commandConfirmed = false, canContinue = true;
        Player player = (Player) src;

        PixelUpgrade.log.info("\u00A7bFuseDitto: Called by player " + player.getName() + ", starting command.");

        if (!args.<String>getOne("target slot").isPresent())
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo slots were provided. Please provide two valid slots."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot, 1-6> <sacrifice slot, 1-6> (-c to confirm)"));

            canContinue = false;
        }
        else
        {
            String slotString = args.<String>getOne("target slot").get();

            if (slotString.matches("^[1-6]"))
                slot1 = Integer.parseInt(args.<String>getOne("target slot").get());
            else
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid value on target slot. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot, 1-6> <sacrifice slot, 1-6> (-c to confirm)"));

                canContinue = false;
            }
        }

        if (!args.<String>getOne("sacrifice slot").isPresent() && canContinue)
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo sacrifice slot provided. Please provide two valid slots."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot, 1-6> <sacrifice slot, 1-6> (-c to confirm)"));

            canContinue = false;
        }
        else if (canContinue)
        {
            String slotString = args.<String>getOne("sacrifice slot").get();

            if (slotString.matches("^[1-6]"))
                slot1 = Integer.parseInt(args.<String>getOne("sacrifice slot").get());
            else
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid value on sacrifice slot. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot, 1-6> <sacrifice slot, 1-6> (-c to confirm)"));

                canContinue = false;
            }
        }

        if (args.hasAny("c"))
            commandConfirmed = true;

        if (canContinue)
        {
            Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
            PlayerStorage storageCompleted = (PlayerStorage) storage.get();
            NBTTagCompound nbt1 = storageCompleted.partyPokemon[slot1 - 1];
            NBTTagCompound nbt2 = storageCompleted.partyPokemon[slot2 - 1];

            if (nbt1 == null && nbt2 != null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cCould not find target Pok\u00E9mon. Make sure it exists."));
            else if (nbt1 != null && nbt2 == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cCould not find sacrifice Pok\u00E9mon. Make sure it exists."));
            else if (nbt1 == null && nbt2 == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cCould not find a target nor a sacrifice. Make sure they exist."));
            else
            {

            }
        }

        return CommandResult.success();
    }
}
