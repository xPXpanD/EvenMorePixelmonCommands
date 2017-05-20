package rs.expand.pixelupgrade.commands;

import java.util.Arrays;
import java.util.Optional;

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

public class AdminForce implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
		Boolean canContinue = true, statWasFixed = true, forceValue = false, shinyFix = false, valueIsInt = false;
		Integer slot = 0, intValue = null;
		String stat = null, fixedStat = stat, value = null;

		PixelUpgrade.log.info("\u00A7bSetIVs: Called by player " + player.getName() + ", starting command.");

		if (!args.<String>getOne("slot").isPresent())
		{
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			player.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. See below."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <stat> <value> (-f to force)"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
            player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

			canContinue = false;
		}
		else
		{
			String slotString = args.<String>getOne("slot").get();

			if (slotString.matches("^[1-6]"))
				slot = Integer.parseInt(args.<String>getOne("slot").get());
			else
			{
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
				player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <stat> <value> (-f to force)"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

				canContinue = false;
			}
		}

        if (args.hasAny("f"))
            forceValue = true;

		if (args.<String>getOne("stat").isPresent() && canContinue)
		{
			stat = args.<String>getOne("stat").get();

			switch (stat.toUpperCase())
			{
                case "IVHP":
                    fixedStat = "IVHP";
                    break;
                case "IVATTACK":
                    fixedStat = "IVAttack";
                    break;
                case "IVDEFENCE": case "IVDEFENSE":
                    fixedStat = "IVDefence";
                    break;
                case "IVSPATT": case "IVSPATK":
                    fixedStat = "IVSpAtt";
                    break;
                case "IVSPDEF":
                    fixedStat = "IVSpDef";
                    break;
                case "IVSPEED":
                    fixedStat = "IVSpeed";
                    break;
                case "EVHP":
                    fixedStat = "EVHP";
                    break;
                case "EVATTACK":
                    fixedStat = "EVAttack";
                    break;
                case "EVDEFENCE": case "EVDEFENSE":
                    fixedStat = "EVDefence";
                    break;
                case "EVSPECIALATTACK": case "EVSPATT": case "EVSPATK":
                    fixedStat = "EVSpecialAttack";
                    break;
                case "EVSPECIALDEFENCE": case "EVSPDEF":
                    fixedStat = "EVSpecialDefence";
                    break;
                case "EVSPEED":
                    fixedStat = "EVSpeed";
                    break;
                case "GROWTH": case "SIZE":
                    fixedStat = "Growth";
                    break;
                case "NATURE":
                    fixedStat = "Nature";
                    break;
                case "ISSHINY": case "IS_SHINY": case "SHINY":
                    fixedStat = "IsShiny";
                    shinyFix = true;
                    break;
				default: statWasFixed = false;
			}

			if (!statWasFixed && !forceValue)
			{
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid stat provided. See below for valid stats."));
                player.sendMessage(Text.of("\u00A76IVs: \u00A7eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
                player.sendMessage(Text.of("\u00A76EVs: \u00A7eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
                player.sendMessage(Text.of("\u00A76Others: \u00A7eGrowth, Nature, Shiny"));

				canContinue = false;
			}
		}
		else if (canContinue)
		{
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo stat provided. See below for valid stats."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <stat> <value> (-f to force)"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76IVs: \u00A7eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
            player.sendMessage(Text.of("\u00A76EVs: \u00A7eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
            player.sendMessage(Text.of("\u00A76Others: \u00A7eGrowth, Nature, Shiny"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
            player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

			canContinue = false;
		}

		if (!args.<String>getOne("value").isPresent() && canContinue)
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo value or amount was provided."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <stat> <value> (-f to force)"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
            player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

            canContinue = false;
        }
		else if (canContinue)
        {
            String valueString = args.<String>getOne("value").get();

		    if (valueString.matches("^-?[0-9].*"))
            {
                intValue = Integer.parseInt(args.<String>getOne("value").get());
                valueIsInt = true;
            }
            else
                value = args.<String>getOne("value").get();
        }

		if (canContinue)
		{
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
            PlayerStorage storageCompleted = storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (nbt == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
            else if (!forceValue && valueIsInt)
            {
                String[] validIVEV = new String[] {"IVHP", "IVAttack", "IVDefence", "IVSpAtt", "IVSpDef", "IVSpeed", "EVHP", "EVAttack", "EVDefence", "EVSpecialAttack", "EVSpecialDefence", "EVSpeed"};

                System.out.println("\u00A72Params: \u00A7c" + stat + " " + fixedStat);

                if (Arrays.asList(validIVEV).contains(fixedStat) && intValue > 32767 || Arrays.asList(validIVEV).contains(fixedStat) && intValue < -32768)
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cIV/EV value out of bounds. Valid range: -32768 ~ 32767"));
                else if (fixedStat.equals("Growth") && intValue > 8 || fixedStat.equals("Growth") && intValue < 0)
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cSize value out of bounds. Valid range: 0 ~ 8"));
                else if (fixedStat.equals("Nature") && intValue > 24 || fixedStat.equals("Nature") && intValue < 0)
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cNature value out of bounds. Valid range: 0 ~ 24"));
                else if (fixedStat.equals("IsShiny") && intValue != 0 && intValue != 1)
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)"));
                else
                {
                    nbt.setInteger(fixedStat, intValue);

                    if (shinyFix)
                        nbt.setInteger("Shiny", intValue);

                    player.sendMessage(Text.of("\u00A7aValue changed! Not showing? Reconnect to update your client."));
                }
            }
            else if (!forceValue && !valueIsInt)
            {
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                player.sendMessage(Text.of("\u00A74Error: \u00A7cGot a non-integer value, but no flag. Try a number."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <stat> <value> (-f to force)"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            }
            else if (forceValue)
            {
                player.sendMessage(Text.of("\u00A7eForcing value..."));

                if (statWasFixed)
                {
                    player.sendMessage(Text.of("\u00A75Note: \u00AddA known stat was found, and was checked and possibly corrected."));
                    player.sendMessage(Text.of("\u00A75Provided stat: \u00Add" + stat + "\u00A75, changed to: \u00A7d" + fixedStat));

                    stat = fixedStat;
                }

                if (valueIsInt)
                {
                    nbt.setInteger(stat, intValue);
                    if (shinyFix)
                        nbt.setInteger("Shiny", intValue);
                }
                else
                {
                    nbt.setString(stat, value);
                    if (shinyFix)
                        nbt.setString("Shiny", value);
                }

                player.sendMessage(Text.of("\u00A7aThe new value was written, a reconnect may be necessary."));
            }
            else
            {
                player.sendMessage(Text.of("\u00A74Debug: \u00A7cFell through all sanity checks. forceValue is: " + forceValue));
                player.sendMessage(Text.of("\u00A74Debug: \u00A7cPlease report this, along with what you were trying to do!"));
                player.sendMessage(Text.of("\u00A74Debug: \u00A7cDumping NBT: " + nbt));
            }
		}
	    return CommandResult.success();
	}
}