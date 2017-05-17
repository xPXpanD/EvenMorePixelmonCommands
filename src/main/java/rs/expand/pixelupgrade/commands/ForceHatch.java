package rs.expand.pixelupgrade.commands;

import java.util.Objects;
import java.util.Optional;

import org.spongepowered.api.Sponge;
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
        Player player = (Player) src;
        Optional<Player> target = player.getPlayer();
        String targetString;
        Boolean targetAcquired = false, canContinue = true;
        Integer slot = 0;

        PixelUpgrade.log.info("\u00A7bHatch debug: Called by player " + player.getName() + ", starting command.");

        if (args.<String>getOne("target or slot").isPresent())
        {
            targetString = args.<String>getOne("target or slot").get();
            target = Sponge.getServer().getPlayer(targetString);
            if (targetString.matches("^[1-6]") && targetString.length() == 1)
                slot = Integer.parseInt(targetString);
            else if (targetString.length() == 1)
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot must be a number between 1 and 6."));
                canContinue = false;
            }
            else
            {
                if (target.isPresent() && !player.equals(target.get()))
                    targetAcquired = true;
                else if (target.isPresent() && player.equals(target.get()))
                    player.sendMessage(Text.of("\u00A75Info: \u00A7dYou don't need to provide your own name. Ignoring!"));
                else
                {
                    player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid target name or slot number!"));
                    player.sendMessage(Text.of("\u00A74Usage: \u00A7c/hatch (target) <slot>"));
                    player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }
            }
        }

        if (args.<Integer>getOne("slot").isPresent() && canContinue)
            slot = args.<Integer>getOne("slot").get();

        if (!(slot < 7 && slot > 0) && canContinue)
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
            canContinue = false;
        }

        PixelUpgrade.log.info("\u00A7aHatch debug: Finished input checks.");

        if (canContinue)
        {
            Optional<PlayerStorage> storage;
            if (targetAcquired)
                storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target.get()));
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
                PixelUpgrade.log.info("\u00A7aHatch debug: Attempting to hatch...");

                //nbt.setString("originalTrainer", player.getName());
                nbt.setBoolean("isEgg", false);
                storageCompleted.changePokemonAndAssignID(slot - 1, nbt);
                player.sendMessage(Text.of("\u00A7eCongratulations, it's a healthy baby \u00A76" + nbt.getString("Name") + "\u00A7e!"));

                PixelUpgrade.log.info("\u00A7aHatch debug: Hatch done.");
            }
        }
        PixelUpgrade.log.info("\u00A7bHatch debug: Command ended.");
        return CommandResult.success();
    }
}