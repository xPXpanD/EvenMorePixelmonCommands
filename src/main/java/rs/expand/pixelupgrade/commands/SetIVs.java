package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
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
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.stream.IntStream;

public class SetIVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
		if (!args.getOne("stat").isPresent() || !args.getOne("slot").isPresent())
		{
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			player.sendMessage(Text.of("\u00A74Error: \u00A7cNot all arguments were provided!"));
			player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (confirm)"));
			player.sendMessage(Text.of(""));
			player.sendMessage(Text.of("\u00A76Warning: \u00A7eDo not add \"confirm\" unless you're sure!"));
			player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
		}
		else
		{
			String stat = null;
			Integer slot = null, quantity = 1;
			Boolean canContinue = false, commandConfirmed = false;
            if (!args.<String>getOne("confirm").isPresent())
                ; // Do nothing! Just need the next statement to not run if this is the case, really.
            else
            {
                String confirm = args.<String>getOne("confirm").get();

                if (confirm.contains("confirm") || confirm.contains("true"))
                    commandConfirmed = true;
            }

            if (!args.<String>getOne("quantity").isPresent())
                ; // Do nothing! Just need the next statement to not run if this is the case, really.
            else
                quantity = args.<Integer>getOne("quantity").get();

			try
			{
				slot = args.<Integer>getOne("slot").get();
				stat = args.<String>getOne("stat").get();

				canContinue = true;
			}
			catch (NumberFormatException e)
			{
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
				player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid arguments! Format is #, text, #."));
				player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (confirm)"));
				player.sendMessage(Text.of(""));
				player.sendMessage(Text.of("\u00A76Warning: \u00A7eDo not add \"confirm\" unless you're sure!"));
				player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			}

			if (canContinue)
			{
				if (slot > 6 || slot < 1)
					player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
				else
				{
					Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
					PlayerStorage storageCompleted = (PlayerStorage) storage.get();
					NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

					if (nbt == null)
						player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
					else
					{
						String fixedStat = stat, cleanedStat = "Error, please report!";
						Boolean statWasValid = false;
						switch (fixedStat.toUpperCase())
						{
							case "HP": case "HITPOINTS": case "HEALTH": case "IVHP": case "IV_HP":
							    fixedStat = "IVHP";
							    cleanedStat = "HP";
							    statWasValid = true;
							    break;
							case "ATTACK": case "ATK": case "IVATTACK": case "IV_ATTACK":
							    fixedStat = "IVAttack";
							    cleanedStat = "Attack";
							    statWasValid = true;
							    break;
                            case "DEFENCE": case "DEFENSE": case "DEF": case "IVDEFENCE": case "IV_DEFENCE":
                                fixedStat = "IVDefence";
                                cleanedStat = "Defence";
                                statWasValid = true;
                                break;
                            case "SPECIALATTACK": case "SPATT" : case "SPATK": case "IVSPATT": case "IV_SP_ATT":
                                fixedStat = "IVSpAtt";
                                cleanedStat = "Sp. Attack";
                                statWasValid = true;
                                break;
                            case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEF" : case "SPDEFENCE": case "SPDEFENSE": case "IVSPDEF": case "IV_SP_DEF":
                                fixedStat = "IVSpDef";
                                cleanedStat = "Sp. Defence";
                                statWasValid = true;
                                break;
                            case "SPEED": case "SPD": case "IVSPEED" : case "IV_SPEED":
                                fixedStat = "IVSpeed";
                                cleanedStat = "Speed";
                                statWasValid = true;
                                break;
						}

                        if (statWasValid)
                        {
                            Optional<UniqueAccount> economyAccount = PixelUpgrade.economyService.getOrCreateAccount(player.getUniqueId());

                            if (economyAccount.isPresent())
                            {
                                UniqueAccount economyAccountUnique = economyAccount.get();
                                BigDecimal balance = economyAccountUnique.getBalance(PixelUpgrade.economyService.getDefaultCurrency());
                                Integer statOld = nbt.getInteger(fixedStat);

                                if (nbt.getBoolean("isEgg"))
                                    player.sendMessage(Text.of("\u00A74Error: \u00A7cThat's an egg. Wait until it hatches, first."));
                                else if (statOld >= 31)
                                    player.sendMessage(Text.of("\u00A74Error: \u00A7cYou cannot upgrade this stat any further, it's maxed!"));
                                else
                                {
                                    Boolean isShiny;
                                        isShiny = nbt.getInteger(NbtKeys.SHINY) == 1;

                                    Boolean isLegendary;
                                        isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

                                    Integer upgradeCount = nbt.getInteger("upgradeCount");

                                    if (isLegendary && !isShiny && upgradeCount >= 20)
                                        player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached! (1)"));
                                    else if (isLegendary && isShiny && upgradeCount >= 40)
                                        player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached! (2)"));
                                    if (!isShiny && upgradeCount >= 30)
                                        player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached! (3)"));
                                    else if (isShiny && upgradeCount >= 50)
                                        player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached! (4)"));
                                    else /// HEAVILY WIP
                                    {
                                        Integer priceMultiplier = 1, upgradeLimit = 30, costToConfirm = 0;
                                        if (isShiny)
                                        {
                                            priceMultiplier = 3;
                                            upgradeLimit = 50;
                                        }
                                        else if (isLegendary)
                                        {
                                            priceMultiplier = 3;
                                            upgradeLimit = 20;
                                        }

                                        Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
                                        Integer IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                                        Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                                        Integer IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                                        Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                                        Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                                        Integer totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;

                                        Boolean fireElseError = false;
                                        StringBuilder listOfValues = new StringBuilder();
                                        IntStream.rangeClosed(totalIVs + 1, 186).forEach(listOfValues::append);
                                        String[] outputArray = listOfValues.toString().split("");

                                        //TODO: Add egg check.

                                        if (quantity == 1)
                                        {
                                            String finalValue = outputArray[0];
                                            player.sendMessage(Text.of("\u00A74OutputArray[0] value: " + finalValue + " | Cost to confirm: " + costToConfirm));
                                            costToConfirm += Integer.parseInt(finalValue) * (1 * priceMultiplier);
                                            player.sendMessage(Text.of("\u00A74Cost to confirm: " + costToConfirm));
                                        }
                                        else if (quantity > 1)
                                        {
                                            if (quantity > (31 - statOld))
                                                quantity = (31 - statOld);

                                            Integer upgradeTicker = 0, iteratedValue = 0;

                                            for(String loopValue : outputArray)
                                            {
                                                if(upgradeTicker > quantity) break;

                                                iteratedValue += Integer.valueOf(loopValue);
                                                player.sendMessage(Text.of("\u00A74LV: " + loopValue + " | LV Int: " + Integer.valueOf(loopValue)));
                                                upgradeTicker++;
                                            }

                                            costToConfirm = iteratedValue * (1 * priceMultiplier);
                                        }
                                        else
                                            fireElseError = true;



                                        //outputValues.setText(listOfValues.toString());

                                        if (!commandConfirmed && quantity == 1)
                                        {
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                            player.sendMessage(Text.of("\u00A7dYour Pok\u00E9mon's \u00A75" + cleanedStat + "\u00A7d stat will be upgraded by \u00A75one \u00A7dpoint!"));
                                            player.sendMessage(Text.of("\u00A7dThis will cost you: \u00A75" + costToConfirm + " coins!"));
                                            player.sendMessage(Text.of("\u00A7eReady? Use: \u00A76/upgrade ivs " + slot + " " + stat + " confirm"));
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                        }
                                        if (!commandConfirmed && quantity > 1 && quantity <= (186 - totalIVs))
                                        {
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                            player.sendMessage(Text.of("\u00A7dYour Pok\u00E9mon's \u00A75" + cleanedStat + "\u00A7d stat will be upgraded by \u00A75" + quantity + "\u00A7d points!"));
                                            player.sendMessage(Text.of("\u00A7dThis will cost you: \u00A75" + costToConfirm + " coins!"));
                                            player.sendMessage(Text.of("\u00A7eReady? Use: \u00A76/upgrade ivs " + slot + " " + stat + " # confirm"));
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                        }
                                        else if (fireElseError)
                                        {
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                            player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid # of times! Please provide a positive number."));
                                            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (confirm)"));
                                            player.sendMessage(Text.of(""));
                                            player.sendMessage(Text.of("\u00A76Warning: \u00A7eDo not add \"confirm\" unless you're sure!"));
                                            player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have an economy account. Please contact staff!"));
                                PixelUpgrade.log.info("\u00A74/upgrade IVs:" + player.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                            }
                        }
						else
						{
							player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
							player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid parameter. See below!"));
							player.sendMessage(Text.of(""));
							player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defence, SpecialAttack, SpecialDefense, Speed"));
							player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
						}
					}
				}
			}
		}
		return CommandResult.success();
	}
}