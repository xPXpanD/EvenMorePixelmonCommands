package rs.expand.pixelupgrade.commands;

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

        PixelUpgrade.log.info("\u00A7bForceHatch: Called by player " + player.getName() + ", starting command.");

        if (args.<String>getOne("target or slot").isPresent())
        {
            targetString = args.<String>getOne("target or slot").get();
            target = Sponge.getServer().getPlayer(targetString);
            if (!args.<String>getOne("slot").isPresent())
            {
                if (targetString.matches("^[1-6]"))
                    slot = Integer.parseInt(targetString);
                else
                {
                    if (target.isPresent())
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cFound a target, but no slot was provided."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));
                    }
                    else if (targetString.matches("^[0-9].*"))
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds! Valid values are 1-6."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));
                    }
                    else
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));
                    }

                    canContinue = false;
                }
            }
            else if (!target.isPresent())
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                canContinue = false;
            }
            else
            {
                try
                {
                    slot = Integer.parseInt(args.<String>getOne("slot").get());

                    if (!(slot < 7 && slot > 0))
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds. Valid values are 1-6."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                        canContinue = false;
                    }
                    else
                        targetAcquired = true;
                }
                catch (Exception F)
                {
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                    player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                    canContinue = false;
                }
            }
        }
        else
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide at least a slot."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

            canContinue = false;
        }

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
                //nbt.setString("originalTrainer", player.getName());
                nbt.setBoolean("isEgg", false);
                storageCompleted.changePokemonAndAssignID(slot - 1, nbt);
                player.sendMessage(Text.of("\u00A7eCongratulations, it's a healthy baby \u00A76" + nbt.getString("Name") + "\u00A7e!"));
            }
        }
        return CommandResult.success();
    }
}