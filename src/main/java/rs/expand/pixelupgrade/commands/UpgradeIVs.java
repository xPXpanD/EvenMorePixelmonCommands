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

public class UpgradeIVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
        Player player = (Player) src;
        Boolean canContinue = true, commandConfirmed = false, statWasValid = true;
        Integer slot = 0, quantity = 0;
        String stat = null, fixedStat = null, cleanedStat = "Error, please report!";

        PixelUpgrade.log.info("\u00A7bUpgradeIVs: Called by player " + player.getName() + ", starting command.");

        if (!args.<String>getOne("slot").isPresent())
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (-c to confirm)"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
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
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (-c to confirm)"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
                player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                canContinue = false;
            }
        }

        if (args.<String>getOne("stat").isPresent() && canContinue)
        {
            stat = args.<String>getOne("stat").get();

            switch (stat.toUpperCase())
            {
                case "HP": case "HITPOINTS": case "HEALTH": case "IVHP": case "IV_HP":
                    fixedStat = "IVHP";
                    cleanedStat = "HP";
                    break;
                case "ATTACK": case "ATK": case "IVATTACK": case "IV_ATTACK":
                    fixedStat = "IVAttack";
                    cleanedStat = "Attack";
                    break;
                case "DEFENCE": case "DEFENSE": case "DEF": case "IVDEFENCE": case "IV_DEFENCE":
                    fixedStat = "IVDefence";
                    cleanedStat = "Defence";
                    break;
                case "SPECIALATTACK": case "SPATT": case "SPATK": case "SPATTACK": case "IVSPATT": case "IV_SP_ATT":
                    fixedStat = "IVSpAtt";
                    cleanedStat = "Sp. Attack";
                    break;
                case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEF": case "SPDEFENCE": case "SPDEFENSE": case "IVSPDEF": case "IV_SP_DEF":
                    fixedStat = "IVSpDef";
                    cleanedStat = "Sp. Defence";
                    break;
                case "SPEED": case "SPD": case "IVSPEED": case "IV_SPEED":
                    fixedStat = "IVSpeed";
                    cleanedStat = "Speed";
                    break;
                default: statWasValid = false;
            }

            if (!statWasValid)
            {
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid IV type \"\u00A74" + stat + "\u00A7c\". See below."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (-c to confirm)"));
                player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defence, SpAtt, SpDef, Speed"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
                player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                canContinue = false;
            }
        }
        else if (canContinue)
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo IV type was provided. See below."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (-c to confirm)"));
            player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defence, SpAtt, SpDef, Speed"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

            canContinue = false;
        }

        if (!args.<String>getOne("quantity").isPresent() && canContinue)
            quantity = 1;
        else if (canContinue)
        {
            String quantityString = args.<String>getOne("quantity").get();

            if (quantityString.equals("-c"))
            {
                commandConfirmed = true;
                quantity = 1;
            }
            else if (!quantityString.matches("^[0-9].*"))
            {
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                player.sendMessage(Text.of("\u00A74Error: \u00A7cThe quantity (# of times) must be a positive number."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (-c to confirm)"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
                player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                canContinue = false;
            }
            else
            {
                quantity = Integer.parseInt(args.<String>getOne("quantity").get());

                if (quantity < 1)
                {
                    player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid # of times. Please provide a positive number."));
                    player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade IVs <slot> <type> (# of times) (-c to confirm)"));
                    player.sendMessage(Text.of(""));
                    player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
                    player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
                    player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }
                else if (quantity > 31)
                    quantity = 31;
            }
        }

        if (args.hasAny("c"))
            commandConfirmed = true;

        if (canContinue)
        {
            Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
            PlayerStorage storageCompleted = (PlayerStorage) storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (nbt == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
            else
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

                        PixelUpgrade.log.info("\u00A7cUpgradeIVs debug: Tried to upgrade an egg. Abort.");
                    }
                    else if (totalIVs >= 186)
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's stats are already perfect!"));

                        PixelUpgrade.log.info("\u00A7cUpgradeIVs debug: Total stats at or above 186. Abort.");
                    }
                    else if (statOld >= 31)
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cYou cannot upgrade this stat any further, it's maxed!"));

                        PixelUpgrade.log.info("\u00A7cUpgradeIVs debug: At or above the 31 stat cap. Abort.");
                    }
                    else if (nbt.getString("Name").equals("Ditto"))
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cI'm sorry, \u00A74" + player.getName() + "\u00A7c, but I'm afraid I can't do that."));

                        PixelUpgrade.log.info("\u00A7cUpgradeIVs debug: Tried to upgrade a Ditto. Abort.");
                    }
                    else
                    {
                        Boolean isShiny, isLegendary, isBaby = false;
                        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                        Integer upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");

                        isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
                        if (nbt.getString("Name").equals("Riolu") || nbt.getString("Name").equals("Mime Jr.") || nbt.getString("Name").equals("Happiny"))
                            isBaby = true;
                        isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

                        if (isShiny && upgradeCount >= 60)
                        {
                            player.sendMessage(Text.of("\u00A74Error: \u00A7cThis \u00A7eshiny\u00A7c's upgrade cap has been reached!"));

                            PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Hit cap on shiny.");
                        }
                        else if (!isShiny && isBaby && upgradeCount >= 25)
                        {
                            player.sendMessage(Text.of("\u00A74Error: \u00A7cThis \u00A76legendary\u00A7c's upgrade cap has been reached!"));

                            PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Hit cap on baby.");
                        }
                        else if (!isShiny && !isLegendary && upgradeCount >= 35)
                        {
                            player.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached!"));

                            PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Hit cap on ordinary.");
                        }
                        else
                        {
                            Boolean freeUpgrade = false, paidUpgrade = false, singleUpgrade = false;
                            BigDecimal costToConfirm;
                            Double priceMultiplier = 1.0, iteratedValue = 0.0;
                            Integer remainder;

                            if (isLegendary && isShiny)
                            {
                                remainder = 60 - upgradeCount;
                                priceMultiplier = 5.0;
                            }
                            else if (isBaby)
                            {
                                remainder = 25 - upgradeCount;
                                priceMultiplier = 1.5;
                            }
                            else if (isLegendary)
                            {
                                remainder = 20 - upgradeCount;
                                priceMultiplier = 5.0;
                            }
                            else if (isShiny)
                            {
                                remainder = 50 - upgradeCount;
                                priceMultiplier = 2.5;
                            }
                            else
                                remainder = 35 - upgradeCount;

                            PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Remainder is " + remainder + ".");

                            StringBuilder listOfValues = new StringBuilder();
                            for (int i = totalIVs + 1; i <= 186; i++)
                            {
                                listOfValues.append(i);
                                listOfValues.append(",");
                            }
                            listOfValues.setLength(listOfValues.length() - 1);
                            String[] outputArray = listOfValues.toString().split(",");

                            if (quantity == 1)
                                singleUpgrade = true;
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

                            PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Quantity given is " + upgradeTicker + ". Remainder: " + remainder + ". Uncorrected price: " + costToConfirm + ".");

                            costToConfirm = costToConfirm.setScale(2, RoundingMode.HALF_UP);

                            if (commandConfirmed)
                            {
                                PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Entering final stage, with confirmation. Current cash: " + uniqueAccount.getBalance(economyService.getDefaultCurrency()) + ".");

                                TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());
                                if (transactionResult.getResult() == ResultType.SUCCESS)
                                {
                                    if (isShiny)
                                        upgradeCount = 60 - remainder;
                                    else if (isBaby)
                                        upgradeCount = 25 - remainder;
                                    else if (isLegendary)
                                        upgradeCount = 20 - remainder;
                                    else
                                        upgradeCount = 35 - remainder;

                                    pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);
                                    nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);

                                    if (singleUpgrade)
                                        player.sendMessage(Text.of("\u00A7aYou've upgraded your \u00A72" + nbt.getString("Name") + "\u00A7a's \u00A72" + cleanedStat + "\u00A7a stat by \u00A72one \u00A7apoint!"));
                                    else
                                        player.sendMessage(Text.of("\u00A7aYou've upgraded your \u00A72" + nbt.getString("Name") + "\u00A7a's \u00A72" + cleanedStat + "\u00A7a stat by \u00A72" + upgradeTicker + "\u00A7a points!"));

                                    if (remainder != 0)
                                        player.sendMessage(Text.of("\u00A7aYou paid \u00A72" + costToConfirm + "\u00A7a, and have \u00A72" + remainder + "\u00A7a upgrades remaining."));
                                    else
                                        player.sendMessage(Text.of("\u00A7aYou paid \u00A72" + costToConfirm + "\u00A7a, fully upgrading the Pok\u00E9mon in the process."));

                                    PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Transaction successful. upgradeCount: " + upgradeCount + ". Took: " + costToConfirm + ".");
                                    PixelUpgrade.log.info("\u00A7aDittoFusion debug: Exiting final stage. Current cash: " + uniqueAccount.getBalance(economyService.getDefaultCurrency()) + ".");
                                }
                                else
                                {
                                    BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                    player.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                                    PixelUpgrade.log.info("\u00A7aDittoFusion debug: Hit the failed/no funds check, exiting final stage. Needed: " + balanceNeeded + ".");
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                                if (quantity == 1)
                                    player.sendMessage(Text.of("\u00A7bThe \u00A73" + cleanedStat + "\u00A7b stat will be upgraded by \u00A73one \u00A7bpoint!"));
                                else if (quantity > (31 - statOld))
                                    player.sendMessage(Text.of("\u00A7bThe \u00A73" + cleanedStat + "\u00A7b stat will be upgraded by \u00A73" + upgradeTicker + "\u00A7b points, up to the cap!"));
                                else
                                    player.sendMessage(Text.of("\u00A7bThe \u00A73" + cleanedStat + "\u00A7b stat will be upgraded by \u00A73" + upgradeTicker + "\u00A7b points!"));

                                if (freeUpgrade && !paidUpgrade && remainder > 0)
                                    player.sendMessage(Text.of("\u00A7bThis upgrade will be free due to your Pok\u00E9mon's low stats."));
                                else if (freeUpgrade && !paidUpgrade)
                                    player.sendMessage(Text.of("\u00A7bThis final upgrade will be free due to your Pok\u00E9mon's low stats."));
                                else if (freeUpgrade && remainder > 0)
                                    player.sendMessage(Text.of("\u00A7bThis upgrade costs \u00A73" + costToConfirm + " coins\u00A7b, with low stat compensation."));
                                else if (freeUpgrade)
                                    player.sendMessage(Text.of("\u00A7bThis final upgrade costs \u00A73" + costToConfirm + " coins\u00A7b, with low stat compensation."));
                                else if (remainder == 0)
                                    player.sendMessage(Text.of("\u00A7bThis final upgrade will cost you \u00A73" + costToConfirm + " coins\u00A7b upon confirmation."));
                                else
                                    player.sendMessage(Text.of("\u00A7bThis upgrade will cost you \u00A73" + costToConfirm + " coins\u00A7b upon confirmation."));

                                if (quantity == 1)
                                    player.sendMessage(Text.of("\u00A7aReady? Use: \u00A72/upgrade ivs " + slot + " " + stat + " -c"));
                                else
                                    player.sendMessage(Text.of("\u00A7aReady? Use: \u00A72/upgrade ivs " + slot + " " + stat + " " + upgradeTicker + " -c"));
                                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                                PixelUpgrade.log.info("\u00A7aUpgradeIVs debug: Final stage, no confirmation. cleanedStat: " + cleanedStat + ". upgradeTicker: " + upgradeTicker + ".");
                            }
                        }
                    }
                }
                else
                {
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));

                    PixelUpgrade.log.info("\u00A74UpgradeIVs debug:" + player.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                }
            }
        }
        PixelUpgrade.log.info("\u00A7bUpgradeIVs debug: Command ended.");
        return CommandResult.success();
	}
}