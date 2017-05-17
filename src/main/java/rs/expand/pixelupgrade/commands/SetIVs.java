package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.math.RoundingMode;
import java.util.Optional;
import java.math.BigDecimal;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class SetIVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
        String stat = null;
        Integer slot = null, quantity = 1;
        Boolean canContinue = false, commandConfirmed = false;

        PixelUpgrade.log.info("\u00A7bSetIVs debug: Called by player " + player.getName() + ", starting command.");

        if (args.<String>getOne("confirm").isPresent())
        {
            String confirm = args.<String>getOne("confirm").get();

            if (confirm.contains("confirm") || confirm.contains("true"))
                commandConfirmed = true;

            PixelUpgrade.log.info("\u00A7aSetIVs debug: Command was confirmed.");
        }

        if (args.<String>getOne("quantity").isPresent())
            quantity = args.<Integer>getOne("quantity").get();

        if (args.<Integer>getOne("slot").isPresent() && args.<String>getOne("stat").isPresent())
        {
            try
            {
                slot = args.<Integer>getOne("slot").get();
                stat = args.<String>getOne("stat").get();

                canContinue = true;
            }
            catch (NumberFormatException e)
            {
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid arguments! Format is #, text, #, text."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (confirm)"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A76Warning: \u00A7eDo not add \"confirm\" unless you're sure!"));
                player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                PixelUpgrade.log.info("\u00A7cSetIVs debug: Slot or stat invalid? Aborting.");
            }
        }
        else
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cMissing arguments! Format is #, text, #, text."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (confirm)"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eDo not add \"confirm\" unless you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

            PixelUpgrade.log.info("\u00A7cSetIVs debug: Slot or stat not provided. Aborting.");
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
                        case "SPECIALATTACK": case "SPATT": case "SPATK": case "SPATTACK": case "IVSPATT": case "IV_SP_ATT":
                            fixedStat = "IVSpAtt";
                            cleanedStat = "Sp. Attack";
                            statWasValid = true;
                            break;
                        case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEF": case "SPDEFENCE": case "SPDEFENSE": case "IVSPDEF": case "IV_SP_DEF":
                            fixedStat = "IVSpDef";
                            cleanedStat = "Sp. Defence";
                            statWasValid = true;
                            break;
                        case "SPEED": case "SPD": case "IVSPEED": case "IV_SPEED":
                            fixedStat = "IVSpeed";
                            cleanedStat = "Speed";
                            statWasValid = true;
                            break;
                    }

                    if (statWasValid)
                    {
                        Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                        if (optionalAccount.isPresent())
                        {
                            UniqueAccount uniqueAccount = optionalAccount.get();
                            Integer statOld = nbt.getInteger(fixedStat);
                            Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
                            Integer IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                            Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                            Integer IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                            Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                            Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                            Integer totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;
                            Integer upgradeTicker = 0;

                            if (nbt.getBoolean("isEgg"))
                            {
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cThat's an egg. Wait until it hatches, first."));

                                PixelUpgrade.log.info("\u00A7cSetIVs debug: Tried to upgrade an egg. Abort.");
                            }
                            else if (totalIVs >= 186)
                            {
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's stats are already perfect!"));

                                PixelUpgrade.log.info("\u00A7cSetIVs debug: Total stats at or above 186. Abort.");
                            }
                            else if (statOld >= 31)
                            {
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou cannot upgrade this stat any further, it's maxed!"));

                                PixelUpgrade.log.info("\u00A7cSetIVs debug: At or above the 31 stat cap. Abort.");
                            }
                            else if (nbt.getString("Name").equals("Ditto"))
                            {
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cI'm sorry, \u00A74" + player.getName() + "\u00A7c, but I'm afraid I can't do that."));

                                PixelUpgrade.log.info("\u00A7cSetIVs debug: Tried to upgrade a Ditto. Abort.");
                            }
                            else
                            {
                                Boolean isShiny = false, isLegendary;
                                if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                                    isShiny = true;
                                isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

                                EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                                Integer upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");

                                if (isShiny && upgradeCount >= 50)
                                {
                                    player.sendMessage(Text.of("\u00A74Error: \u00A7cThis shiny Pok\u00E9mon's upgrade cap has been reached!"));

                                    PixelUpgrade.log.info("\u00A7aSetIVs debug: Hit cap on shiny.");
                                }
                                else if (isLegendary && upgradeCount >= 20)
                                {
                                    player.sendMessage(Text.of("\u00A74Error: \u00A7cThis legendary Pok\u00E9mon's upgrade cap has been reached!"));

                                    PixelUpgrade.log.info("\u00A7aSetIVs debug: Hit cap on legendary.");
                                }
                                else if (upgradeCount >= 30)
                                {
                                    player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached!"));

                                    PixelUpgrade.log.info("\u00A7aSetIVs debug: Hit cap on ordinary.");
                                }
                                else
                                {
                                    if (quantity >= 1)
                                    {
                                        Boolean freeUpgrade = false, paidUpgrade = false, singleUpgrade = true;
                                        BigDecimal costToConfirm = new BigDecimal(0);
                                        Double priceMultiplier = 1.0, iteratedValue = 0.0;
                                        Integer remainder;

                                        if (isLegendary)
                                        {
                                            remainder = 20 - upgradeCount;
                                            priceMultiplier = 7.5;
                                        }
                                        else if (isShiny)
                                        {
                                            remainder = 50 - upgradeCount;
                                            priceMultiplier = 2.5;
                                        }
                                        else
                                            remainder = 30 - upgradeCount;

                                        PixelUpgrade.log.info("\u00A7aSetIVs debug: Remainder is " + remainder + ".");

                                        StringBuilder listOfValues = new StringBuilder();
                                        for (int i = totalIVs + 1; i <= 186; i++)
                                        {
                                            listOfValues.append(i);
                                            listOfValues.append(",");
                                        }
                                        listOfValues.setLength(listOfValues.length() - 1);
                                        String[] outputArray = listOfValues.toString().split(",");

                                        if (quantity == 1 && Integer.valueOf(outputArray[0]) > 37)
                                        {
                                            costToConfirm = BigDecimal.valueOf(Math.pow(Double.valueOf(outputArray[0]), 5) / 900000000);
                                            remainder--;
                                            upgradeTicker = 1;

                                            PixelUpgrade.log.info("\u00A7aSetIVs debug: Quantity is one. Remainder: " + remainder + ". Uncorrected price: " + costToConfirm + ".");
                                        }
                                        else if (quantity > 1)
                                        {
                                            if (quantity > (31 - statOld))
                                                quantity = (31 - statOld);

                                            Integer initialRemainder = remainder;

                                            for (String loopValue : outputArray)
                                            {
                                                if (upgradeTicker >= quantity || upgradeTicker >= initialRemainder)
                                                    break;

                                                // Allow a free upgrade in case a Pokémon has 33% or lower total IVs (two full IVs, or a spread totalling no more than 33%).
                                                if (Integer.valueOf(loopValue) <= 62)
                                                    freeUpgrade = true;
                                                else
                                                {
                                                    iteratedValue += Math.pow(Double.valueOf(loopValue), 5) / 900000000;
                                                    paidUpgrade = true;
                                                }

                                                upgradeTicker++;
                                                remainder--;
                                            }

                                            costToConfirm = BigDecimal.valueOf(iteratedValue * priceMultiplier);
                                            singleUpgrade = false;

                                            PixelUpgrade.log.info("\u00A7aSetIVs debug: Quantity is " + upgradeTicker + ". Remainder: " + remainder + ". Uncorrected price: " + costToConfirm + ".");
                                        }
                                        else
                                        {
                                            freeUpgrade = true;
                                            remainder--;
                                            upgradeTicker = 1;

                                            PixelUpgrade.log.info("\u00A7aSetIVs debug: Else check hit. Remainder: " + remainder + ". Uncorrected price: " + costToConfirm + ".");
                                        }

                                        costToConfirm = costToConfirm.setScale(2, RoundingMode.HALF_UP);

                                        if (commandConfirmed)
                                        {
                                            PixelUpgrade.log.info("\u00A7aSetIVs debug: Entering final stage, with confirmation. Current cash: " + uniqueAccount.getBalance(economyService.getDefaultCurrency()) + ".");

                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());
                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                if (isLegendary)
                                                    upgradeCount = 20 - remainder;
                                                else if (isShiny)
                                                    upgradeCount = 50 - remainder;
                                                else
                                                    upgradeCount = 30 - remainder;

                                                pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);
                                                nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);

                                                if (singleUpgrade)
                                                    player.sendMessage(Text.of("\u00A7bYou've upgraded your \u00A73" + nbt.getString("Name") + "\u00A7b's \u00A73" + cleanedStat + "\u00A7b stat by \u00A73one \u00A7bpoint!"));
                                                else
                                                    player.sendMessage(Text.of("\u00A7bYou've upgraded your \u00A73" + nbt.getString("Name") + "\u00A7b's \u00A73" + cleanedStat + "\u00A7b stat by \u00A73" + upgradeTicker + "\u00A7b points!"));

                                                PixelUpgrade.log.info("\u00A7aSetIVs debug: Transaction successful. upgradeCount: " + upgradeCount + ". Took: " + costToConfirm + ".");
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                                                PixelUpgrade.log.info("\u00A7aSetIVs debug: Hit the failed/no funds check. Needed: " + balanceNeeded + ".");
                                            }

                                            PixelUpgrade.log.info("\u00A7aSetIVs debug: Exiting final stage. Current cash: " + uniqueAccount.getBalance(economyService.getDefaultCurrency()) + ".");
                                        }
                                        else
                                        {
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                                            if (quantity == 1)
                                                player.sendMessage(Text.of("\u00A7dThe \u00A75" + cleanedStat + "\u00A7d stat will be upgraded by \u00A75one \u00A7dpoint!"));
                                            else if (quantity > (31 - statOld))
                                                player.sendMessage(Text.of("\u00A7dThe \u00A75" + cleanedStat + "\u00A7d stat will be upgraded by \u00A75" + upgradeTicker + "\u00A7d points, up to the cap!"));
                                            else
                                                player.sendMessage(Text.of("\u00A7dThe \u00A75" + cleanedStat + "\u00A7d stat will be upgraded by \u00A75" + upgradeTicker + "\u00A7d points!"));

                                            if (freeUpgrade && !paidUpgrade && remainder > 0)
                                                player.sendMessage(Text.of("\u00A7dThis upgrade will be free due to your Pok\u00E9mon's low stats."));
                                            else if (freeUpgrade && !paidUpgrade)
                                                player.sendMessage(Text.of("\u00A7dThis final upgrade will be free due to your Pok\u00E9mon's low stats."));
                                            else if (freeUpgrade && remainder > 0)
                                                player.sendMessage(Text.of("\u00A7dThis upgrade costs \u00A75" + costToConfirm + " coins\u00A7d, with low stat compensation."));
                                            else if (freeUpgrade)
                                                player.sendMessage(Text.of("\u00A7dThis final upgrade costs \u00A75" + costToConfirm + " coins\u00A7d, with low stat compensation."));
                                            else if (remainder == 0)
                                                player.sendMessage(Text.of("\u00A7dThis final upgrade will cost you \u00A75" + costToConfirm + " coins\u00A7d."));
                                            else
                                                player.sendMessage(Text.of("\u00A7dThis upgrade will cost you \u00A75" + costToConfirm + " coins\u00A7d."));

                                            if (quantity == 1)
                                                player.sendMessage(Text.of("\u00A7eReady? Use: \u00A76/upgrade ivs " + slot + " " + stat + " confirm"));
                                            else
                                                player.sendMessage(Text.of("\u00A7eReady? Use: \u00A76/upgrade ivs " + slot + " " + stat + " " + upgradeTicker + " confirm"));
                                            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                                            PixelUpgrade.log.info("\u00A7aSetIVs debug: Final stage, no confirmation. cleanedStat: " + cleanedStat + ". upgradeTicker: " + upgradeTicker + ".");
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                        player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid # of times! Please provide a positive number."));
                                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (confirm)"));
                                        player.sendMessage(Text.of(""));
                                        player.sendMessage(Text.of("\u00A76Warning: \u00A7eDo not add \"confirm\" unless you're sure!"));
                                        player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                                        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                                        PixelUpgrade.log.info("\u00A7cSetIVs debug: Invalid # of times provided. Quantity: " + quantity + ".");
                                    }
                                }
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));

                            PixelUpgrade.log.info("\u00A74SetIVs debug:" + player.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid parameter. See below!"));
                        player.sendMessage(Text.of(""));
                        player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defence, SpAtt, SpDef, Speed"));
                        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                        PixelUpgrade.log.info("\u00A7cSetIVs debug: Invalid parameter. Given: " + stat + ".");
                    }
                }
            }
        }
        PixelUpgrade.log.info("\u00A7bSetIVs debug: Command ended.");
        return CommandResult.success();
	}
}