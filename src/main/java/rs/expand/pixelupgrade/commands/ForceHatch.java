package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.world.World;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import rs.expand.pixelupgrade.PixelUpgrade;

public class ForceHatch implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Integer slot = args.<Integer>getOne("slot").get();
        Player player = (Player) src, target;
        Boolean targetAcquired = false, canContinue = false;

        PixelUpgrade.log.info("\u00A7bHatch debug: Called by player " + player.getName() + ", starting command.");


        if (args.<Integer>getOne("slot").isPresent())
        {
            try
            {
                slot = args.<Integer>getOne("slot").get();

                canContinue = true;
            }
            catch (NumberFormatException e)
            {
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid arguments! See below. Target is optional."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/hatch (target) <slot>"));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            }
        }
        else
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cMissing arguments! See below. Target is optional."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/hatch (target) <slot>"));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }

        if (!args.getOne("target").isPresent())
            target = (Player) src;
        else
        {
            target = args.<Player>getOne("target").get();
            targetAcquired = true;

            PixelUpgrade.log.info("\u00A7aHatch debug: Target passed. Target: " + target);
        }

        if (slot > 6 || slot < 1)
            player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
        else
        {
            Optional<PlayerStorage> storage;
            if (targetAcquired)
                storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
            else
                storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));

            PlayerStorage storageCompleted = storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (nbt == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cThere's nothing in that slot!"));
            else if (!nbt.getBoolean("isEgg"))
                player.sendMessage(Text.of("\u00A74Error: \u00A7cThat's not an egg. Don't hatch actual Pok\u00E9mon, kids!"));
            else
            {
                PixelUpgrade.log.info("\u00A7aHatch debug: Attempting hatch via changePokemonAndAssignID...");

                storageCompleted.changePokemonAndAssignID(slot - 1, nbt);
                player.sendMessage(Text.of("\u00A7eCongratulations, it's a healthy baby \u00A76" + nbt.getString("Name") + "\u00A7e!"));

                PixelUpgrade.log.info("\u00A7aHatch debug: Hatch done.");
            }
        }
        PixelUpgrade.log.info("\u00A7bHatch debug: Command ended.");
        return CommandResult.success();
    }
}