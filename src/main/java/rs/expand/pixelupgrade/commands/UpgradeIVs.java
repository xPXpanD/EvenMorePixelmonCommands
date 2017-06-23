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
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;
import rs.expand.pixelupgrade.configs.UpgradeIVsConfig;

import java.math.RoundingMode;
import java.util.Optional;
import java.math.BigDecimal;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class UpgradeIVs implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = UpgradeIVsConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

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

    private static String alias;
    private void getCommandAlias()
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + UpgradeIVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    // Set up a variable that we'll be using in command helper messages.
    private Boolean useBritishSpelling = null;

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
        if (src instanceof Player)
        {
            boolean presenceCheck = true;
            Double mathMultiplier = checkConfigDouble("mathMultiplier");
            Integer upgradesFreeBelow = checkConfigInt("upgradesFreeBelow");
            Integer addFlatFee = checkConfigInt("addFlatFee");

            Double legendaryAndShinyMult = checkConfigDouble("legendaryAndShinyMult");
            Double legendaryMult = checkConfigDouble("legendaryMult");
            Double regularMult = checkConfigDouble("regularMult");
            Double shinyMult = checkConfigDouble("shinyMult");
            Double babyMult = checkConfigDouble("babyMult");

            Integer legendaryAndShinyCap = checkConfigInt("legendaryAndShinyCap");
            Integer legendaryCap = checkConfigInt("legendaryCap");
            Integer regularCap = checkConfigInt("regularCap");
            Integer shinyCap = checkConfigInt("shinyCap");
            Integer babyCap = checkConfigInt("babyCap");

            // Grab the useBritishSpelling value from the main config.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").isVirtual())
                useBritishSpelling = PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").getBoolean();

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (legendaryAndShinyCap == null || legendaryCap == null || regularCap == null || shinyCap == null)
                presenceCheck = false;
            else if (babyCap == null || legendaryAndShinyMult == null || legendaryMult == null || regularMult == null)
                presenceCheck = false;
            else if (shinyMult == null || babyMult == null || mathMultiplier == null || upgradesFreeBelow == null)
                presenceCheck = false;
            else if (addFlatFee == null)
                presenceCheck = false;

            if (!presenceCheck || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCheck your config. If need be, wipe and \u00A74/pixelupgrade reload\u00A7c.");
            }
            else if (useBritishSpelling == null)
            {
                src.sendMessage(Text.of("\u00A74Error: \u00A7cCould not parse main config. Please report to staff."));
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cCouldn't get value of \"useBritishSpelling\" from the main config.");
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cPlease check (or wipe and reload) your PixelUpgrade.conf file.");
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
                        case "ATTACK": case "ATK": case "ATT": case "IVATTACK": case "IV_ATTACK":
                            fixedStat = "IVAttack";
                            cleanStat = "Attack";
                            break;
                        case "DEFENCE": case "DEFENSE": case "DEF": case "IVDEFENCE":
                        case "IV_DEFENCE": case "IVDEFENSE": case "IV_DEFENSE":
                            fixedStat = "IVDefence";
                            if (useBritishSpelling)
                                cleanStat = "Defence";
                            else
                                cleanStat = "Defense";
                            break;
                        case "SPECIALATTACK": case "SPATT": case "SPATK": case "SPATTACK": case "IVSPATT":
                        case "IV_SP_ATT": case "IV_SP_ATK": case "IV_SPATK":
                            fixedStat = "IVSpAtt";
                            cleanStat = "Special Attack";
                            break;
                        case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEF": case "SPDEFENCE":
                        case "SPDEFENSE": case "IVSPDEF": case "IV_SP_DEF":
                            fixedStat = "IVSpDef";
                            if (useBritishSpelling)
                                cleanStat = "Special Defence";
                            else
                                cleanStat = "Special Defense";
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
                    else if (!quantityString.matches("\\d+"))
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
                                    iteratedValue += Math.exp(Double.valueOf(loopValue) * mathMultiplier);
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
                                String name = nbt.getString("Name");
                                String upgradeString = "\u00A7eYou upgraded your \u00A76" + name + "\u00A7e's \u00A76" + cleanStat;

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
                                if (costToConfirm.signum() == 0)
                                {
                                    printToLog(3, "Entering final stage, got confirmation. No cost due to low stats or config.");

                                    nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                    pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                    player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                    if (singleUpgrade)
                                        player.sendMessage(Text.of(upgradeString + "\u00A7e stat by \u00A76one \u00A7epoint!"));
                                    else
                                        player.sendMessage(Text.of(upgradeString + "\u00A7e stat by \u00A76" + upgradeTicker + "\u00A7e points!"));
                                    player.sendMessage(Text.of(""));

                                    if (remainder == 1)
                                        src.sendMessage(Text.of("\u00A7dThis upgrade was free. You have \u00A75one \u00A7dupgrade remaining..."));
                                    else if (remainder > 1)
                                        src.sendMessage(Text.of("\u00A7dThis upgrade was free. You have \u00A75" + remainder + " \u00A7dupgrades remaining."));
                                    else
                                        src.sendMessage(Text.of("\u00A7dThis upgrade was free. This Pok\u00E9mon is now at its limits."));
                                    player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                }
                                else
                                {
                                    Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                    if (optionalAccount.isPresent())
                                    {
                                        UniqueAccount uniqueAccount = optionalAccount.get();
                                        BigDecimal newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                        printToLog(1, "Entering final stage, got confirmation. Current cash: \u00A76" + newTotal + "\u00A7e.");

                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());
                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                            pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                            player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                            if (singleUpgrade)
                                                player.sendMessage(Text.of(upgradeString + "\u00A7e stat by \u00A76one \u00A7epoint!"));
                                            else
                                                player.sendMessage(Text.of(upgradeString + "\u00A7e stat by \u00A76" + upgradeTicker + "\u00A7e points!"));

                                            if (costToConfirm.signum() == 1) // 1 = we've got a cost. 0 = cost is zero. -1 would be negative.
                                            {
                                                String paidString = "\u00A7dYou paid \u00A75" + costToConfirm + "\u00A7d coins";
                                                player.sendMessage(Text.of(""));

                                                if (remainder == 1)
                                                    src.sendMessage(Text.of(paidString + ". \u00A75One \u00A7dupgrade remains..."));
                                                else if (remainder > 1)
                                                    src.sendMessage(Text.of(paidString + ". \u00A75" + remainder + " \u00A7dupgrades remain."));
                                                else
                                                    src.sendMessage(Text.of(paidString + ", and reached this Pok\u00E9mon's limits."));
                                            }
                                            else if (costToConfirm.signum() == 0) // Cost is zero, either due to low stats or config.
                                            {
                                                player.sendMessage(Text.of(""));

                                                if (remainder == 1)
                                                    src.sendMessage(Text.of("\u00A75One \u00A7dupgrade remains..."));
                                                else if (remainder > 1)
                                                    src.sendMessage(Text.of("\u00A75" + remainder + " \u00A7dupgrades remain..."));
                                                else
                                                    src.sendMessage(Text.of("You've now reached this Pok\u00E9mon's limits."));
                                            }
                                            player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));

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

                                player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                String helperString = "\u00A7eThe \u00A76" + cleanStat + "\u00A7e stat will be upgraded by \u00A76";
                                String quantityString = "\u00A7aReady? Use: \u00A72" + alias + " " + slot + " " + stat;

                                if (quantity == 1)
                                    src.sendMessage(Text.of(helperString + "one \u00A7epoint!"));
                                else if (quantity > (31 - statOld))
                                    src.sendMessage(Text.of(helperString + upgradeTicker + "\u00A7e points, up to the cap!"));
                                else
                                    src.sendMessage(Text.of(helperString + upgradeTicker + "\u00A7e points!"));

                                if (freeUpgrade && !paidUpgrade && remainder > 0 && costToConfirm.signum() == 0)
                                    src.sendMessage(Text.of("\u00A7eThis upgrade will be free due to your Pok\u00E9mon's low stats."));
                                else if (freeUpgrade && !paidUpgrade && costToConfirm.signum() == 0)
                                    src.sendMessage(Text.of("\u00A7eThis final upgrade will be free due to low stats."));
                                else if (freeUpgrade && remainder > 0)
                                    src.sendMessage(Text.of("\u00A7eThis upgrade costs \u00A76" + costToConfirm + " coins\u00A7e, with low stat compensation."));
                                else if (freeUpgrade) // Lacking space. Slightly awkward message, but it'll do.
                                    src.sendMessage(Text.of("\u00A7eThis last upgrade costs \u00A76" + costToConfirm + " coins\u00A7e with low stat compensation."));
                                else if (remainder == 0)
                                    src.sendMessage(Text.of("\u00A7eThis final upgrade will cost \u00A76" + costToConfirm + " coins\u00A7e upon confirmation."));
                                else
                                    src.sendMessage(Text.of("\u00A7eThis upgrade will cost \u00A76" + costToConfirm + " coins\u00A7e upon confirmation."));
                                src.sendMessage(Text.of(""));

                                if (costToConfirm.compareTo(BigDecimal.ZERO) == 1) // Are we above 0 coins?
                                    src.sendMessage(Text.of("\u00A75Warning: \u00A7dYou can't undo upgrades! Make sure you want this."));

                                if (quantity == 1)
                                    src.sendMessage(Text.of(quantityString + " -c"));
                                else
                                    src.sendMessage(Text.of(quantityString + " " + upgradeTicker + " -c"));
                                player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
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
        if (useBritishSpelling)
            player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defence, SpAtt, SpDef, Speed"));
        else
            player.sendMessage(Text.of("\u00A72Valid types: \u00A7aHP, Attack, Defense, SpAtt, SpDef, Speed"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
        player.sendMessage(Text.of("\u00A7eConfirming will immediately take your money, if you have enough!"));
        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void printCorrectPerm(Player player)
    {
        player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot> <IV type> [amount?] {-c to confirm}"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76DittoFusion // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73DittoFusion // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72DittoFusion // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Double checkConfigDouble(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getDouble();
        else
        {
            PixelUpgrade.log.info("\u00A74Upgrade // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }
}