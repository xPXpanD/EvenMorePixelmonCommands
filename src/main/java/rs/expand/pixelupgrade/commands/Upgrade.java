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
import rs.expand.pixelupgrade.configs.UpgradeConfig;

import java.math.RoundingMode;
import java.util.Optional;
import java.math.BigDecimal;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class Upgrade implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set 4 (out of range) or null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel = 4;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!UpgradeConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = UpgradeConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
        if (src instanceof Player)
        {
            boolean presenceCheck1 = true, presenceCheck2 = true, presenceCheck3 = true;
            Double legendaryAndShinyMult, legendaryMult, regularMult, shinyMult, babyMult;
            Integer legendaryAndShinyCap, legendaryCap, regularCap, shinyCap, babyCap;
            Integer mathPower, mathDivisor, upgradesFreeBelow, addFlatFee, debugVerbosityMode;

            legendaryAndShinyMult = checkConfigDouble("legendaryAndShinyMult");
            legendaryMult = checkConfigDouble("legendaryMult");
            regularMult = checkConfigDouble("regularMult");
            shinyMult = checkConfigDouble("shinyMult");
            babyMult = checkConfigDouble("babyMult");

            debugVerbosityMode = checkConfigInt("debugVerbosityMode", false);
            mathPower = checkConfigInt("mathPower", false);
            mathDivisor = checkConfigInt("mathDivisor", false);
            upgradesFreeBelow = checkConfigInt("upgradesFreeBelow", false);
            addFlatFee = checkConfigInt("addFlatFee", false);

            legendaryAndShinyCap = checkConfigInt("legendaryAndShinyCap", false);
            legendaryCap = checkConfigInt("legendaryCap", false);
            regularCap = checkConfigInt("regularCap", false);
            shinyCap = checkConfigInt("shinyCap", false);
            babyCap = checkConfigInt("babyCap", false);

            // Check the command's debug verbosity mode, as set in the config.
            getVerbosityMode();

            if (legendaryAndShinyCap == null || legendaryCap == null || regularCap == null || shinyCap == null || babyCap == null)
                presenceCheck1 = false;
            if (legendaryAndShinyMult == null || legendaryMult == null || regularMult == null || shinyMult == null || babyMult == null)
                presenceCheck2 = false;
            if (mathPower == null || mathDivisor == null || upgradesFreeBelow == null || addFlatFee == null || debugVerbosityMode == null)
                presenceCheck3 = false;

            if (!presenceCheck1 || !presenceCheck2 || !presenceCheck3 || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pu reload\\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Player player = (Player) src;
                Boolean canContinue = true, commandConfirmed = false, statWasValid = true;
                Integer slot = 0, quantity = 0;
                String stat = null, fixedStat = null, cleanStat = "Error, please report!";

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "No parameters provided. Abort.");

                    checkAndAddHeader(player);
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
                    printCorrectPerm(player);
                    checkAndAddFooter(player);

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot provided. Aborting.");

                        checkAndAddHeader(player);
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                        printCorrectPerm(player);
                        checkAndAddFooter(player);

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
                            cleanStat = "HP";
                            break;
                        case "ATTACK": case "ATK": case "IVATTACK": case "IV_ATTACK":
                            fixedStat = "IVAttack";
                            cleanStat = "Attack";
                            break;
                        case "DEFENCE": case "DEFENSE": case "DEF": case "IVDEFENCE": case "IV_DEFENCE":
                            fixedStat = "IVDefence";
                            cleanStat = "Defence";
                            break;
                        case "SPECIALATTACK": case "SPATT": case "SPATK": case "SPATTACK": case "IVSPATT": case "IV_SP_ATT":
                            fixedStat = "IVSpAtt";
                            cleanStat = "Sp. Attack";
                            break;
                        case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEF": case "SPDEFENCE": case "SPDEFENSE": case "IVSPDEF": case "IV_SP_DEF":
                            fixedStat = "IVSpDef";
                            cleanStat = "Sp. Defence";
                            break;
                        case "SPEED": case "SPD": case "IVSPEED": case "IV_SPEED":
                            fixedStat = "IVSpeed";
                            cleanStat = "Speed";
                            break;
                        default:
                            statWasValid = false;
                    }

                    if (!statWasValid)
                    {
                        printToLog(2, "Got an invalid IV type, type was: \u00A73" + stat);

                        checkAndAddHeader(player);
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid IV type \"\u00A74" + stat + "\u00A7c\". See below."));
                        printCorrectPerm(player);
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                }
                else if (canContinue)
                {
                    printToLog(2, "No stat (IV type) provided. Aborting.");

                    checkAndAddHeader(player);
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo IV type was provided. See below."));
                    printCorrectPerm(player);
                    checkAndAddFooter(player);

                    canContinue = false;
                }

                if (!args.<String>getOne("quantity").isPresent() && canContinue)
                {
                    printToLog(3, "No quantity was given, setting to 1.");
                    quantity = 1;
                }
                else if (canContinue)
                {
                    String quantityString = args.<String>getOne("quantity").get();

                    if (quantityString.equals("-c"))
                    {
                        printToLog(3, "Found confirmation flag on quantity arg, setting q=1 and flagging.");
                        commandConfirmed = true;
                        quantity = 1;
                    }
                    else if (!quantityString.matches("^[0-9].*"))
                    {
                        printToLog(2, "Quantity was not numeric and not a confirmation flag, abort.");

                        checkAndAddHeader(player);
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cThe quantity (# of times) must be a positive number."));
                        printCorrectPerm(player);
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                    else
                    {
                        quantity = Integer.parseInt(args.<String>getOne("quantity").get());

                        if (quantity < 1)
                        {
                            printToLog(2, "Quantity below 1, abort.");

                            checkAndAddHeader(player);
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid # of times. Please enter a positive number."));
                            printCorrectPerm(player);
                            checkAndAddFooter(player);

                            canContinue = false;
                        }
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));
                    PlayerStorage storageCompleted = storage.get();
                    NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else if (nbt == null)
                    {
                        printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
                    }
                    else if (nbt.getBoolean("isEgg"))
                    {
                        printToLog(2, "Tried to upgrade an egg. Let's not, aborting.");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cThat's an egg! Go hatch it, first."));
                    }
                    else if (nbt.getString("Name").equals("Ditto"))
                    {
                        printToLog(2, "Tried to upgrade a Ditto. Print witty message and abort.");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cI'm sorry, \u00A74" + src.getName() + "\u00A7c, but I'm afraid I can't do that."));
                    }
                    else
                    {
                        Integer statOld = nbt.getInteger(fixedStat);
                        Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
                        Integer IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                        Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                        Integer IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                        Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                        Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                        Integer totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;

                        Boolean isShiny = false, isLegendary = false, isBaby = false;
                        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                        Integer upgradeTicker = 0, upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");

                        // Let's see what kind of Pokémon we've been provided.
                        if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                        {
                            printToLog(3, "Provided Pok\u00E9mon is shiny.");
                            isShiny = true;
                        }
                        if (nbt.getString("Name").equals("Riolu") || nbt.getString("Name").equals("Mime Jr.") || nbt.getString("Name").equals("Happiny"))
                        {
                            printToLog(3, "Provided Pok\u00E9mon is a known 3*31 IV baby.");
                            isBaby = true;
                        }
                        if (EnumPokemon.legendaries.contains(nbt.getString("Name")))
                        {
                            printToLog(3, "Provided Pok\u00E9mon is shiny. Applying shiny config amounts.");
                            isLegendary = true;
                        }

                        // Let's go through the big ol' wall of checks.
                        if (totalIVs >= 186)
                        {
                            printToLog(2, "Found a perfect (>186 IVs) Pok\u00E9mon. Nothing left to do here!");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's stats are already perfect!"));
                        }
                        else if (statOld >= 31)
                        {
                            printToLog(2, "Found a stat >31 that was going to be upgraded. Let's not do that!");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou cannot upgrade this stat any further, it's maxed!"));
                        }
                        else if (isShiny && isLegendary && upgradeCount >= legendaryAndShinyCap)
                        {
                            printToLog(2, "Hit cap on shiny legendary Pok\u00E9mon.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis \u00A7eshiny legendary\u00A7c's upgrade cap has been reached!"));
                        }
                        else if (isShiny && upgradeCount >= shinyCap)
                        {
                            printToLog(2, "Hit cap on shiny Pok\u00E9mon.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis \u00A7eshiny\u00A7c's upgrade cap has been reached!"));
                        }
                        else if (!isShiny && isLegendary && upgradeCount >= legendaryCap)
                        {
                            printToLog(2, "Hit cap on legendary Pok\u00E9mon.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis \u00A7elegendary\u00A7c's upgrade cap has been reached!"));
                        }
                        else if (!isShiny && isBaby && upgradeCount >= babyCap)
                        {
                            printToLog(2, "Hit cap on baby Pok\u00E9mon.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis \u00A76baby\u00A7c's upgrade cap has been reached!"));
                        }
                        else if (!isShiny && !isLegendary && !isBaby && upgradeCount >= regularCap)
                        {
                            printToLog(2, "Hit cap on regular Pok\u00E9mon.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThis Pok\u00E9mon's upgrade cap has been reached!"));
                        }
                        else
                        {
                            printToLog(3, "Passed a billion checks and got to the main body. Let's loop!");

                            Boolean freeUpgrade = false, paidUpgrade = false, singleUpgrade = false;
                            BigDecimal costToConfirm;
                            Double priceMultiplier, iteratedValue = 0.0;
                            Integer remainder;

                            if (isLegendary && isShiny)
                            {
                                remainder = legendaryAndShinyCap - upgradeCount;
                                priceMultiplier = legendaryAndShinyMult;
                            }
                            else if (isShiny)
                            {
                                remainder = shinyCap - upgradeCount;
                                priceMultiplier = shinyMult;
                            }
                            else if (isBaby)
                            {
                                remainder = babyCap - upgradeCount;
                                priceMultiplier = babyMult;
                            }
                            else if (isLegendary)
                            {
                                remainder = legendaryCap - upgradeCount;
                                priceMultiplier = legendaryMult;
                            }
                            else
                            {
                                remainder = regularCap - upgradeCount;
                                priceMultiplier = regularMult;
                            }

                            printToLog(3, "Calculated remainder from previous upgrade count + config: \u00A72" + remainder);

                            StringBuilder listOfValues = new StringBuilder();
                            for (int i = totalIVs + 1; i <= 186; i++)
                            {
                                listOfValues.append(i);
                                listOfValues.append(",");
                            }
                            listOfValues.setLength(listOfValues.length() - 1);
                            String[] outputArray = listOfValues.toString().split(",");
                            Integer initialRemainder = remainder;

                            if (quantity == 1)
                                singleUpgrade = true;
                            if (quantity > (31 - statOld))
                                quantity = (31 - statOld);

                            for (String loopValue : outputArray)
                            {
                                if (upgradeTicker >= quantity || upgradeTicker >= initialRemainder)
                                    break;

                                // freeUpgrade and paidUpgrade can be true at the same time. Pricing and messages change accordingly.
                                if (Integer.valueOf(loopValue) <= upgradesFreeBelow)
                                    freeUpgrade = true;
                                else
                                {
                                    iteratedValue += Math.pow(Double.valueOf(loopValue), mathPower) / mathDivisor;
                                    paidUpgrade = true;
                                }

                                upgradeTicker++;
                                remainder--;
                            }

                            costToConfirm = BigDecimal.valueOf((iteratedValue * priceMultiplier) + addFlatFee);
                            costToConfirm = costToConfirm.setScale(2, RoundingMode.HALF_UP);

                            printToLog(3, "Remainder is now: \u00A72" + remainder + "\u00A7a. Freshly baked price: \u00A72" + costToConfirm + "\u00A7a.");

                            if (commandConfirmed)
                            {
                                if (isShiny && isLegendary)
                                    upgradeCount = legendaryAndShinyCap - remainder;
                                else if (isShiny)
                                    upgradeCount = shinyCap - remainder;
                                else if (isBaby)
                                    upgradeCount = babyCap - remainder;
                                else if (isLegendary)
                                    upgradeCount = legendaryCap - remainder;
                                else
                                    upgradeCount = regularCap - remainder;

                                // A bit confusing, but an output of 0 on the below statement means we're at 0 cost. 1 is above, -1 is below.
                                if (costToConfirm.compareTo(BigDecimal.ZERO) == 0)
                                {
                                    printToLog(3, "Entering final stage, with confirmation. No cost due to low stats.");

                                    String name = nbt.getString("Name");

                                    nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                    pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                    if (singleUpgrade)
                                        player.sendMessage(Text.of("\u00A7aYou upgraded your \u00A72" + name + "\u00A7a's \u00A72" + cleanStat + "\u00A7a stat by \u00A72one \u00A7apoint!"));
                                    else
                                        player.sendMessage(Text.of("\u00A7aYou upgraded your \u00A72" + name + "\u00A7a's \u00A72" + cleanStat + "\u00A7a stat by \u00A72" + upgradeTicker + "\u00A7a points!"));

                                    if (remainder == 1)
                                        src.sendMessage(Text.of("\u00A7aThis upgrade was free. You have \u00A72one \u00A7aupgrade remaining..."));
                                    else if (remainder > 1)
                                        src.sendMessage(Text.of("\u00A7aThis upgrade was free. You have \u00A72" + remainder + " \u00A7aupgrades remaining."));
                                    else
                                        src.sendMessage(Text.of("\u00A7aThis upgrade was free. You've now hit this Pok\u00E9mon's limits."));
                                }
                                else
                                {
                                    Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                    if (optionalAccount.isPresent())
                                    {
                                        UniqueAccount uniqueAccount = optionalAccount.get();
                                        BigDecimal newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                        printToLog(1, "Entering final stage, with confirmation. Current cash: \u00A76" + newTotal + "\u00A7e.");

                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());
                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            String name = nbt.getString("Name");

                                            nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                            pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                            if (singleUpgrade)
                                                player.sendMessage(Text.of("\u00A7aYou upgraded your \u00A72" + name + "\u00A7a's \u00A72" + cleanStat + "\u00A7a stat by \u00A72one \u00A7apoint!"));
                                            else
                                                player.sendMessage(Text.of("\u00A7aYou upgraded your \u00A72" + name + "\u00A7a's \u00A72" + cleanStat + "\u00A7a stat by \u00A72" + upgradeTicker + "\u00A7a points!"));

                                            if (costToConfirm.compareTo(BigDecimal.ZERO) == 0)
                                            {
                                                if (remainder == 1)
                                                    src.sendMessage(Text.of("\u00A7aYou paid \u00A72" + costToConfirm + " coins\u00A7a. \u00A72One \u00A7aupgrade remains..."));
                                                else if (remainder > 1)
                                                    src.sendMessage(Text.of("\u00A7aYou paid \u00A72" + costToConfirm + " coins\u00A7a. \u00A72" + remainder + " \u00A7aupgrades remain."));
                                                else
                                                    src.sendMessage(Text.of("\u00A7aYou paid \u00A72" + costToConfirm + " coins\u00A7a, and reached this Pok\u00E9mon's limits."));
                                            }

                                            newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                            printToLog(1, "Upgraded an IV, and took \u00A77" + costToConfirm + "\u00A7e coins. New total: \u00A76" + newTotal);
                                        }
                                        else
                                        {
                                            BigDecimal balanceNeeded = newTotal.subtract(costToConfirm).abs();
                                            printToLog(2, "Not enough coins! Cost: \u00A73" + costToConfirm + "\u00A7b, lacking: \u00A73" + balanceNeeded);

                                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                                        }
                                    }
                                    else
                                    {
                                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));
                                    }
                                }

                            }
                            else
                            {
                                printToLog(2, "Got no confirmation; end of the line.");

                                src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                if (quantity == 1)
                                    src.sendMessage(Text.of("\u00A7bThe \u00A73" + cleanStat + "\u00A7b stat will be upgraded by \u00A73one \u00A7bpoint!"));
                                else if (quantity > (31 - statOld))
                                    src.sendMessage(Text.of("\u00A7bThe \u00A73" + cleanStat + "\u00A7b stat will be upgraded by \u00A73" + upgradeTicker + "\u00A7b points, up to the cap!"));
                                else
                                    src.sendMessage(Text.of("\u00A7bThe \u00A73" + cleanStat + "\u00A7b stat will be upgraded by \u00A73" + upgradeTicker + "\u00A7b points!"));

                                if (freeUpgrade && !paidUpgrade && remainder > 0)
                                    src.sendMessage(Text.of("\u00A7bThis upgrade will be free due to your Pok\u00E9mon's low stats."));
                                else if (freeUpgrade && !paidUpgrade)
                                    src.sendMessage(Text.of("\u00A7bThis final upgrade will be free due to your Pok\u00E9mon's low stats."));
                                else if (freeUpgrade && remainder > 0)
                                    src.sendMessage(Text.of("\u00A7bThis upgrade costs \u00A73" + costToConfirm + " coins\u00A7b, with low stat compensation."));
                                else if (freeUpgrade)
                                    src.sendMessage(Text.of("\u00A7bThis final upgrade costs \u00A73" + costToConfirm + " coins\u00A7b, with low stat compensation."));
                                else if (remainder == 0)
                                    src.sendMessage(Text.of("\u00A7bThis final upgrade will cost \u00A73" + costToConfirm + " coins\u00A7b upon confirmation."));
                                else
                                    src.sendMessage(Text.of("\u00A7bThis upgrade will cost \u00A73" + costToConfirm + " coins\u00A7b upon confirmation."));
                                src.sendMessage(Text.of(""));

                                if (costToConfirm.compareTo(BigDecimal.ZERO) == 1) // Are we above 0 coins?
                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eYou can't undo upgrades! Make sure this is what you want."));
                                if (quantity == 1)
                                    src.sendMessage(Text.of("\u00A7aReady? Use: \u00A72/upgrade " + slot + " " + stat + " -c"));
                                else
                                    src.sendMessage(Text.of("\u00A7aReady? Use: \u00A72/upgrade " + slot + " " + stat + " " + upgradeTicker + " -c"));
                                src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                            }
                        }
                    }
                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

    private void checkAndAddHeader(Player player)
    {
        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void checkAndAddFooter(Player player)
    {
        player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defence, SpAtt, SpDef, Speed"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
        player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void printCorrectPerm(Player player)
    {
        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade <slot> <IV type> [amount?] {-c to confirm}"));
    }

    private void printToLog(Integer debugNum, String inputString)
    {
        Integer debugVerbosityMode = checkConfigInt("debugVerbosityMode", true);

        if (debugVerbosityMode == null)
            debugVerbosityMode = 4;

        if (debugNum <= debugVerbosityMode)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76Upgrade // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73Upgrade // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72Upgrade // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt(String node, Boolean noMessageMode)
    {
        if (!UpgradeConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeConfig.getInstance().getConfig().getNode(node).getInt();
        else if (noMessageMode)
            return null;
        else
        {
            PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Double checkConfigDouble(String node)
    {
        if (!UpgradeConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeConfig.getInstance().getConfig().getNode(node).getDouble();
        else
        {
            PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }
}