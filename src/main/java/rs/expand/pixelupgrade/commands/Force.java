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

public class Force implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
		if (!args.getOne("value").isPresent() || !args.getOne("stat").isPresent() || !args.getOne("slot").isPresent())
		{
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			player.sendMessage(Text.of("\u00A74Error: \u00A7cNot all arguments were provided!"));
			player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <type> <value> (-f)"));
			player.sendMessage(Text.of(""));
			player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
			player.sendMessage(Text.of("\u00A7dThis may lead to crashes or other issues. Handle with care!"));
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
		}
		else
		{
			String stat = null, value = null;
			Integer slot = null, intValue = null;
			Boolean canContinue = false, valueIsInt = false;
			try
			{
				slot = Integer.parseInt(args.<String>getOne("slot").get());
				stat = args.<String>getOne("stat").get();
				value = args.<String>getOne("value").get();
				if (value.matches("^-?\\d+$"))
				{ 
					intValue = Integer.parseInt(value); 
					valueIsInt = true;
				}
				
				canContinue = true;
			}
			catch (NumberFormatException e) 
			{
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
				player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid arguments! Format is #, text, #."));
				player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <type> <value> (-f)"));
				player.sendMessage(Text.of(""));
				player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
				player.sendMessage(Text.of("\u00A7dThis may lead to crashes or other issues. Handle with care!"));
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			}
			
			if (canContinue == true)
			{
				Boolean forceValue = false;
				if (args.hasAny("f"))
					forceValue = true;
		
			    if (!(src instanceof Player))
			    	System.out.println("\u00A74Error: \u00A7cYou can't run this command from the console.");
			    else if (slot > 6 || slot < 1)
			    	player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
			    else
			    {
		        	Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
		        	String username = player.getName();
		
			        if (!storage.isPresent())
			        {
			        	player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't seem to have any Pok\u00E9mon."));
			            System.out.println("\u00A72" + username + "\u00A7a failed the storage check, has no Pok\u00E9mon? May be bad.");
			        }
			        else if (forceValue != true && valueIsInt == true)
			        {
		            	PlayerStorage storageCompleted = (PlayerStorage)storage.get();
		                NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];
		                
			            if (nbt == null)
			            	player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			            else
			            {
					    	String[] validIVs = new String[] {"IVHP", "IVAttack", "IVDefence", "IVSpAtt", "IVSpDef", "IVSpeed"};
					    	String[] validEVs = new String[] {"EVHP","EVAttack","EVDefence","EVSpecialAttack","EVSpecialDefence","EVSpeed"};
					    	String[] validGrowth = new String[] {"Growth"};
					    	String[] validNature = new String[] {"Nature"};
					    	
					    	String fixedStat = stat;
					    	Boolean unknownParameter = false;
					    	switch (fixedStat.toUpperCase())
					    	{
					    		case "IVHP": fixedStat = "IVHP"; break;
					    		case "IVATTACK" : fixedStat = "IVAttack"; break;
					    		case "IVDEFENCE" : fixedStat = "IVDefence"; break;
					    		case "IVSPATT" : fixedStat = "IVSpAtt"; break;
					    		case "IVSPDEF" : fixedStat = "IVSpDef"; break;
					    		case "IVSPEED" : fixedStat = "IVSpeed"; break;
					    		case "EVHP" : fixedStat = "EVHP"; break;
					    		case "EVATTACK" : fixedStat = "EVAttack"; break;
					    		case "EVDEFENCE" : fixedStat = "EVDefence"; break;
					    		case "EVSPECIALATTACK" : fixedStat = "EVSpecialAttack"; break;
					    		case "EVSPECIALDEFENCE" : fixedStat = "EVSpecialDefence"; break;
					    		case "EVSPEED" : fixedStat = "EVSpeed"; break;
					    		case "GROWTH" : fixedStat = "Growth"; break;
					    		case "NATURE" : fixedStat = "Nature"; break;
					    		default: unknownParameter = true; break;
					    	}
					    	
					    	System.out.println("\u00A72Params: \u00A7c" + stat + " " + fixedStat);
					    	
					    	if (Arrays.asList(validIVs).contains(fixedStat) && intValue > 32767 || Arrays.asList(validIVs).contains(fixedStat) && intValue < -32768)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cIV value out of bounds. Valid range: -32768 ~ 32767"));
					    	else if (Arrays.asList(validEVs).contains(fixedStat) && intValue > 32767 || Arrays.asList(validEVs).contains(fixedStat) && intValue < -32768)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cEV value out of bounds. Valid range: -32768 ~ 32767"));
					    	else if (Arrays.asList(validGrowth).contains(fixedStat) && intValue > 8 || Arrays.asList(validGrowth).contains(fixedStat) && intValue < 0)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cSize value out of bounds. Valid range: 0 ~ 8"));
					    	else if (Arrays.asList(validNature).contains(fixedStat) && intValue > 24 || Arrays.asList(validNature).contains(fixedStat) && intValue < 0)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cNature value out of bounds. Valid range: 0 ~ 24"));
					    	else if (Arrays.asList(validIVs).contains(fixedStat) || Arrays.asList(validEVs).contains(fixedStat) || Arrays.asList(validGrowth).contains(fixedStat) || Arrays.asList(validNature).contains(fixedStat))
					    	{				    	
						    	if (unknownParameter == false)
						    	{
						            try
						            {              
						                nbt.setInteger(fixedStat, intValue);			                
						                player.sendMessage(Text.of("\u00A7aValue changed! Not showing? Reconnect to update your client."));
						            }
					
					                // Press F to pay respects.
					                catch (Exception F)
					                {
					                	player.sendMessage(Text.of("\u00A74Error: \u00A7cUnknown error! Please tell staff what you were doing!"));
					                    System.out.println("\u00A72" + username + "\u00A7a hit an unknown error executing Force (limited). Stack trace follows.");
					                    F.printStackTrace();
					                    System.out.println("\u00A7aType parameter passed: \u00A72" + fixedStat + "\u00A7a (before correction: \u00A72" + stat + "\u00A7a)");
					                }
						    	}
						    	else
						    	{
						    		player.sendMessage(Text.of("\u00A74Error: \u00A7cUnknown type, but passed first check! Please report this."));
						    		System.out.println("\u00A72" + username + "\u00A7c passed an invalid type, despite passing the first check.");
						    		System.out.println("\u00A72This is a bug. \u00A7cDebug info follows: (stat) \u00A72" + stat + "\u00A7c, (fixedStat) \u00A72" + fixedStat);
						    	}
					    	}
					    	else
					    	{
					    		player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid parameter. See below!"));
								player.sendMessage(Text.of(""));
					    		player.sendMessage(Text.of("\u00A76IV types: \u00A7eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
					    		player.sendMessage(Text.of("\u00A76EV types: \u00A7eEVHP, EVAttack, EVDefence, EVSpecialAttack..."));
					    		player.sendMessage(Text.of("\u00A76EV types: \u00A7eEVSpecialDefence, EVSpeed"));
					    		player.sendMessage(Text.of("\u00A76Other types: \u00A7eGrowth, Nature"));
					    		player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
					    	}
			            }
			        }
			        else if (forceValue != true && valueIsInt != true)
			        {
			        	player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
						player.sendMessage(Text.of("\u00A74Error: \u00A7c<value> should be a number, not text."));
						player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <type> <value> (-f)"));
						player.sendMessage(Text.of(""));
						player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
						player.sendMessage(Text.of("\u00A7dThis may lead to crashes or other issues. Handle with care!"));
						player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			        }
			        else
			        {
		            	PlayerStorage storageCompleted = (PlayerStorage)storage.get();
		                NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];
		                
			            if (nbt == null)
			            	player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			            else try
			            {
					    	String fixedStat = stat;
					    	switch (fixedStat.toUpperCase())
					    	{
					    		case "IVHP": fixedStat = "IVHP"; break;
					    		case "IVATTACK" : fixedStat = "IVAttack"; break;
					    		case "IVDEFENCE" : fixedStat = "IVDefence"; break;
					    		case "IVSPATT" : fixedStat = "IVSpAtt"; break;
					    		case "IVSPDEF" : fixedStat = "IVSpDef"; break;
					    		case "IVSPEED" : fixedStat = "IVSpeed"; break;
					    		case "EVHP" : fixedStat = "EVHP"; break;
					    		case "EVATTACK" : fixedStat = "EVAttack"; break;
					    		case "EVDEFENCE" : fixedStat = "EVDefence"; break;
					    		case "EVSPECIALATTACK" : fixedStat = "EVSpecialAttack"; break;
					    		case "EVSPECIALDEFENCE" : fixedStat = "EVSpecialDefence"; break;
					    		case "EVSPEED" : fixedStat = "EVSpeed"; break;
					    		case "GROWTH" : fixedStat = "Growth"; break;
					    		case "NATURE" : fixedStat = "Nature"; break;
					    	}
					    	
			            	player.sendMessage(Text.of("\u00A7eForcing value..."));
			            	
			            	if (valueIsInt == true)
			            		nbt.setInteger(stat, intValue);
			            	else
			            		nbt.setString(stat, value);
			            	
			            	player.sendMessage(Text.of("\u00A7aValue succesfully set! Not showing? Check caps and spelling."));
			            }
			            
		                // Press F to pay respects.
		                catch (Exception F)
		                {
		                	player.sendMessage(Text.of("\u00A74Error: \u00A7cUnknown error! Please tell staff what you were doing!"));
		                    System.out.println("\u00A72" + username + "\u00A7a hit an unknown error executing Force (-f flag). Stack trace follows.");
		                    F.printStackTrace();
		                }
			        }
			    }
			}
	    }
	    return CommandResult.success();
	}
}