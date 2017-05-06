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

public class GetStats implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Integer slot = args.<Integer>getOne("slot").get();
		Boolean targetAcquired = false;
		Player player = (Player) src, target;
		if (!args.<Integer>getOne("target").isPresent())
			target = (Player) src;
		else
		{
			target = (Player) args.getOne("target").get();
			targetAcquired = true;
		}

		/* if (!(src instanceof Player))
        	System.out.println("\u00A74Error: \u00A7cYou can't run this command from the console."); */
        if (slot > 6 || slot < 1)
        	player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
		else if (!player.isOnline())
			player.sendMessage(Text.of("\u00A74Error: \u00A7cYou're not online. Are you trying this from the console?"));
        else if (!target.isOnline())
        	player.sendMessage(Text.of("\u00A74Error: \u00A7cTarget player does not exist or is offline."));
        else
	    {
        	Optional<?> storage;
        	if (!targetAcquired)
        		storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
        	else
        		storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));

        	PlayerStorage storageCompleted = (PlayerStorage)storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (nbt == null && !targetAcquired)
            	player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			else if (nbt == null && targetAcquired)
				player.sendMessage(Text.of("\u00A74Error: \u00A7cTarget player does not have anything in that slot!"));
            else
            {
                EnumPokemon pokemonName = EnumPokemon.getFromName(nbt.getString("Name")).get();

                Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
                Integer IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                Integer IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                Integer totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;
                Integer percentIVs = totalIVs * 100 / 186;

                Integer EVHP = nbt.getInteger(NbtKeys.EV_HP);
                Integer EVATK = nbt.getInteger(NbtKeys.EV_ATTACK);
                Integer EVDEF = nbt.getInteger(NbtKeys.EV_DEFENCE);
                Integer EVSPATK = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
                Integer EVSPDEF = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
                Integer EVSPD = nbt.getInteger(NbtKeys.EV_SPEED);
                Integer totalEVs = EVHP + EVATK + EVDEF + EVSPATK + EVSPDEF + EVSPD;
                Integer percentEVs = totalEVs * 100 / 510;

                Integer natureNum = nbt.getInteger(NbtKeys.NATURE);
                Integer growthNum = nbt.getInteger(NbtKeys.GROWTH);
                Integer genderNum = nbt.getInteger(NbtKeys.GENDER);
                String natureName, plusVal, minusVal, growthName, genderName;
                Boolean wastedStats = false;

                String ivs1 = String.valueOf("\u00A7eIVs: \u00A7a" + IVHP + " \u00A72HP \u00A7e|\u00A7a " + IVATK + " \u00A72ATK \u00A7e|\u00A7a " + IVDEF + " \u00A72DEF \u00A7e|\u00A7a ");
                String ivs2 = String.valueOf(IVSPATK + " \u00A72Sp. ATK \u00A7e|\u00A7a " + IVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a " + IVSPD + " \u00A72SPD");
                String evs1 = String.valueOf("\u00A7eEVs: \u00A7a" + EVHP + " \u00A72HP \u00A7e|\u00A7a " + EVATK + " \u00A72ATK \u00A7e|\u00A7a " + EVDEF + " \u00A72DEF \u00A7e|\u00A7a ");
                String evs2 = String.valueOf(EVSPATK + " \u00A72Sp. ATK \u00A7e|\u00A7a " + EVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a " + EVSPD + " \u00A72SPD");

                switch (natureNum)
                {
                	case 0: natureName = "Hardy"; plusVal = "+NONE"; minusVal = "-NONE"; break;
                	case 1: natureName = "Serious"; plusVal = "+NONE"; minusVal = "-NONE"; break;
                	case 2: natureName = "Docile"; plusVal = "+NONE"; minusVal = "-NONE"; break;
                	case 3: natureName = "Bashful"; plusVal = "+NONE"; minusVal = "-NONE"; break;
                	case 4: natureName = "Quirky"; plusVal = "+NONE"; minusVal = "-NONE"; break;
                	case 5: natureName = "Lonely"; plusVal = "+ATK"; minusVal = "-DEF"; break;
                	case 6: natureName = "Brave"; plusVal = "+ATK"; minusVal = "-SPD"; break;
                	case 7: natureName = "Adamant"; plusVal = "+ATK"; minusVal = "-SP. ATK"; break;
                	case 8: natureName = "Naughty"; plusVal = "+ATK"; minusVal = "-SP. DEF"; break;
                	case 9: natureName = "Bold"; plusVal = "+DEF"; minusVal = "-ATK"; break;
                	case 10: natureName = "Relaxed"; plusVal = "+DEF"; minusVal = "-SPD"; break;
                	case 11: natureName = "Impish"; plusVal = "+DEF"; minusVal = "-SP. ATK"; break;
                	case 12: natureName = "Lax"; plusVal = "+DEF"; minusVal = "-SP. DEF"; break;
                	case 13: natureName = "Timid"; plusVal = "+SPD"; minusVal = "-ATK"; break;
                	case 14: natureName = "Hasty"; plusVal = "+SPD"; minusVal = "-DEF"; break;
                	case 15: natureName = "Jolly"; plusVal = "+SPD"; minusVal = "-SP. ATK"; break;
                	case 16: natureName = "Naive"; plusVal = "+SPD"; minusVal = "-SP. DEF"; break;
                	case 17: natureName = "Modest"; plusVal = "+SP. ATK"; minusVal = "-ATK"; break;
                	case 18: natureName = "Mild"; plusVal = "+SP. ATK"; minusVal = "-DEF"; break;
                	case 19: natureName = "Quiet"; plusVal = "+SP. ATK"; minusVal = "-SPD"; break;
                	case 20: natureName = "Rash"; plusVal = "+SP. ATK"; minusVal = "-SP. DEF"; break;
                	case 21: natureName = "Calm"; plusVal = "+SP. DEF"; minusVal = "-ATK"; break;
                	case 22: natureName = "Gentle"; plusVal = "+SP. DEF"; minusVal = "-DEF"; break;
                	case 23: natureName = "Sassy"; plusVal = "+SP. DEF"; minusVal = "-SPD"; break;
                	case 24: natureName = "Careful"; plusVal = "+SP. DEF"; minusVal = "-SP. ATK"; break;
                	default: natureName = "Not found? Please report this."; plusVal = "+N/A"; minusVal = "-N/A"; break;
                }

                switch (growthNum)
                {
                	case 0: growthName = "Pygmy"; break;
                	case 1: growthName = "Runt"; break;
                	case 2: growthName = "Small"; break;
                	case 3: growthName = "Ordinary"; break;
                	case 4: growthName = "Huge"; break;
                	case 5: growthName = "Giant"; break;
                	case 6: growthName = "Enormous"; break;
                	case 7: growthName = "\u00A7cGinormous (!)"; break;
                	case 8: growthName = "\u00A7aMicroscopic (!)"; break;
                	default: growthName = "Not found? Please report this."; break;
                }

                switch (genderNum)
                {
                	case 0: genderName = "\u2642"; break;
                	case 1: genderName = "\u2640"; break;
                	case 2: genderName = "\u26A5"; break;
                	default: genderName = "Not found? Please report this."; break;
                }

                if (EVHP < 256 && EVHP > 252 || EVATK < 256 && EVATK > 252 || EVDEF < 256 && EVDEF > 252 || EVSPATK < 256 && EVSPATK > 252 || EVSPDEF < 256 && EVSPDEF > 252 || EVSPD < 256 && EVSPD > 252)
                	wastedStats = true;

                if (!targetAcquired)
                {
	                if (nbt.getString("Nickname").equals(""))
	                	player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + String.valueOf(pokemonName)));
	                else
	                	player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + String.valueOf(pokemonName) + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname")));

	                if (percentIVs == 100)
	                	player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e, \u00A76nicely done!\u00A7e)"));
	                else
	                	player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e)"));
	                player.sendMessage(Text.of(ivs1 + "" + ivs2));

	                player.sendMessage(Text.of("\u00A7eTotal EVs: \u00A7a" + totalEVs + "\u00A7e/\u00A7a510\u00A7e (\u00A7a" + percentEVs + "%\u00A7e)"));
	                player.sendMessage(Text.of(evs1 + "" + evs2));

	                player.sendMessage(Text.of("\u00A7eGender: \u00A7f" + genderName + "\u00A7f | \u00A7eSize: \u00A7f" + growthName + "\u00A7f | \u00A7eNature: \u00A7f" + natureName + "\u00A7e (\u00A7a" + plusVal + "\u00A7e / \u00A7c" + minusVal + "\u00A7e)"));

	                if (nbt.getInteger("upgradeCount") != 0)
	                	player.sendMessage(Text.of("\u00A79Extra data: \u00A7bPok\u00E9mon had their IVs upgraded \u00A7f" + nbt.getInteger("upgradeCount") + "\u00A7b times."));

	                if (nbt.getString("originalGrowth") != "")
	                {
	                	String originalGrowthString = nbt.getString("originalGrowth");
		                player.sendMessage(Text.of("\u00A79Extra data: \u00A7bPok\u00E9mon's size was\u00A7f \"" + originalGrowthString + "\"\u00A7b."));
	                }

	                if (wastedStats)
	                	player.sendMessage(Text.of("\u00A75Warning: \u00A7dEVs above 252 do nothing. Try using \u00A7c/fixevs\u00A7d."));
                }
                else
                {
	                if (nbt.getString("Nickname").equals(""))
	                	player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target + "'s " + String.valueOf(pokemonName)));
	                else
	                	player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target + "'s " + String.valueOf(pokemonName) + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname")));

                    player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e)"));
	                player.sendMessage(Text.of(ivs1 + "" + ivs2));

	                player.sendMessage(Text.of("\u00A7eTotal EVs: \u00A7a" + totalEVs + "\u00A7e/\u00A7a510\u00A7e (\u00A7a" + percentEVs + "%\u00A7e)"));
	                player.sendMessage(Text.of(evs1 + "" + evs2));

	                player.sendMessage(Text.of("\u00A7eGender: \u00A7f" + genderName + "\u00A7f | \u00A7eSize: \u00A7f" + growthName + "\u00A7f | \u00A7eNature: \u00A7f" + natureName + "\u00A7e (\u00A7a" + plusVal + "\u00A7e / \u00A7c" + minusVal + "\u00A7e)"));

	                if (nbt.getInteger("upgradeCount") != 0)
	                	player.sendMessage(Text.of("\u00A79Extra data: \u00A7bPok\u00E9mon had their IVs upgraded \u00A7f" + nbt.getInteger("upgradeCount") + "\u00A7b times."));

	                if (nbt.getString("originalGrowth") != "")
	                {
	                	String originalGrowthString = nbt.getString("originalGrowth");
		                player.sendMessage(Text.of("\u00A79Extra data: \u00A7bPok\u00E9mon's size was originally\u00A7f \"" + originalGrowthString + "\"\u00A7b."));
	                }
                }
            }
        }
        return CommandResult.success();
	}
}