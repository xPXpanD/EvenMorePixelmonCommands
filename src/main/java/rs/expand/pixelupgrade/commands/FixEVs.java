package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import rs.expand.pixelupgrade.PixelUpgrade;

public class FixEVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
	    Player player = (Player) src;
	    Boolean canContinue = true;
	    Integer slot = 0;

        PixelUpgrade.log.info("\u00A7bFixEVs: Called by player " + player.getName() + ", starting command.");

        if (!args.<String>getOne("slot").isPresent())
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fixEVs <slot, 1-6>"));

            canContinue = false;
        }
        else
        {
            String slotString = args.<String>getOne("slot").get();

            if (slotString.matches("^[1-6]"))
                slot = Integer.parseInt(args.<String>getOne("slot").get());
            else
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fixEVs <slot, 1-6>"));

                canContinue = false;
            }
        }

	    if (canContinue)
	    {
	    	Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
            PlayerStorage storageCompleted = (PlayerStorage)storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (nbt == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
            else
            {
                EntityPixelmon pokemon = (EntityPixelmon)PixelmonEntityList.createEntityFromNBT(nbt, (World)player.getWorld());
                EnumPokemon pokemonName = EnumPokemon.getFromName(nbt.getString("Name")).get();

                Integer eHP = pokemon.stats.EVs.HP;
                Integer eATK = pokemon.stats.EVs.Attack;
                Integer eDEF = pokemon.stats.EVs.Defence;
                Integer eSPATK = pokemon.stats.EVs.SpecialAttack;
                Integer eSPDEF = pokemon.stats.EVs.SpecialDefence;
                Integer eSPD = pokemon.stats.EVs.Speed;

                if (eHP == 0 && eATK == 0 && eDEF == 0 && eSPATK == 0 && eSPDEF == 0 && eSPD == 0)
                    player.sendMessage(Text.of("\u00A7dNo EVs were found. Go faint some wild Pok\u00E9mon!"));
                else if (eHP < 253 && eHP > 0 && eATK < 253 && eATK > 0 && eDEF < 253 && eDEF > 0 && eSPATK < 253 && eSPATK > 0 && eSPDEF < 253 && eSPDEF > 0 && eSPD < 253 && eSPD > 0)
                    player.sendMessage(Text.of("\u00A7dNo issues found! Want to drop stats? Check \u00A75/warp shop\u00A7d."));
                else if (eHP > 255 || eATK > 255 || eDEF > 255 || eSPATK > 255 || eSPDEF > 255 || eSPD > 255)
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cOne or more EVs are above the limit. Please contact staff."));
                else if (eHP < 0 || eATK < 0 || eDEF < 0 || eSPATK < 0 || eSPDEF < 0 || eSPD < 0)
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cOne or more EVs are negative. Please contact staff."));
                else
                {
                    Boolean wasOptimized = false;

                    if (eHP > 252)
                    {
                        player.sendMessage(Text.of("\u00A7aStat \u00A72HP \u00A7ais above 252 and has been fixed!"));
                        nbt.setInteger(NbtKeys.EV_HP, 252);
                        wasOptimized = true;
                    }
                    if (eATK > 252)
                    {
                        player.sendMessage(Text.of("\u00A7aStat \u00A72Attack \u00A7ais above 252 and has been fixed!"));
                        nbt.setInteger(NbtKeys.EV_ATTACK, 252);
                        wasOptimized = true;
                    }
                    if (eDEF > 252)
                    {
                        player.sendMessage(Text.of("\u00A7aStat \u00A72Defence \u00A7ais above 252 and has been fixed!"));
                        nbt.setInteger(NbtKeys.EV_DEFENCE, 252);
                        wasOptimized = true;
                    }
                    if (eSPATK > 252)
                    {
                        player.sendMessage(Text.of("\u00A7aStat \u00A72Special Attack \u00A7ais above 252 and has been fixed!"));
                        nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 252);
                        wasOptimized = true;
                    }
                    if (eSPDEF > 252)
                    {
                        player.sendMessage(Text.of("\u00A7aStat \u00A72Special Defence \u00A7ais above 252 and has been fixed!"));
                        nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 252);
                        wasOptimized = true;
                    }
                    if (eSPD > 252)
                    {
                        player.sendMessage(Text.of("\u00A7aStat \u00A72Speed \u00A7ais above 252 and has been fixed!"));
                        nbt.setInteger(NbtKeys.EV_SPEED, 252);
                        wasOptimized = true;
                    }

                    if (wasOptimized)
                    {
                        if (nbt.getString("Nickname").equals(""))
                            player.sendMessage(Text.of("\u00A76" + String.valueOf(pokemonName) + "\u00A7e has been checked and optimized!"));
                        else
                            player.sendMessage(Text.of("\u00A7eYour \u00A76" + nbt.getString("Nickname") + "\u00A7e has been checked and optimized!"));
                    }
                    else
                        player.sendMessage(Text.of("\u00A7aNo EV-related problems found! No changes were made."));
                }
	        }
	    }
	    return CommandResult.success();
	}
}
