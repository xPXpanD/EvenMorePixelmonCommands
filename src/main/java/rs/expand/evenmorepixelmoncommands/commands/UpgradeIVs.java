/*// One of the first PU/EMPC commands!
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import static rs.expand.evenmorepixelmoncommands.PixelUpgrade.*;

public class UpgradeIVs implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer legendaryAndShinyCap, legendaryCap, shinyCap, regularCap, fixedUpgradeCost;
    public static Integer upgradesFreeBelow, addFlatFee;
    public static Double mathMultiplier;
    public static Double legendaryAndShinyMult, legendaryMult, shinyMult, regularMult;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("UpgradeIVs", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (economyEnabled && src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (legendaryAndShinyCap == null)
                nativeErrorArray.add("legendaryAndShinyCap");
            if (legendaryCap == null)
                nativeErrorArray.add("legendaryCap");
            if (shinyCap == null)
                nativeErrorArray.add("shinyCap");
            if (regularCap == null)
                nativeErrorArray.add("regularCap");
            if (mathMultiplier == null)
                nativeErrorArray.add("mathMultiplier");
            if (fixedUpgradeCost == null)
                nativeErrorArray.add("fixedUpgradeCost");
            if (legendaryAndShinyMult == null)
                nativeErrorArray.add("legendaryAndShinyMult");
            if (legendaryMult == null)
                nativeErrorArray.add("legendaryMult");
            if (regularMult == null)
                nativeErrorArray.add("regularMult");
            if (shinyMult == null)
                nativeErrorArray.add("shinyMult");
            if (upgradesFreeBelow == null)
                nativeErrorArray.add("upgradesFreeBelow");
            if (addFlatFee == null)
                nativeErrorArray.add("addFlatFee");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("UpgradeIVs", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (useBritishSpelling == null)
            {
                printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
                printToLog(0, "The main config contains invalid variables. Exiting.");
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else if (BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                printToLog(0, "Called by player §4" + src.getName() + "§c, but in a battle. Exit.");
                src.sendMessage(Text.of("§4Error: §cYou can't use this command while in a battle!"));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                final Player player = (Player) src;
                String stat = null, fixedStat = null, cleanStat = "Error, please report!";
                boolean canContinue = true, commandConfirmed = false, statWasValid = true;
                int slot = 0, quantity = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
                    addHelperAndFooter(src);

                    canContinue = false;
                }
                else
                {
                    final String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        addHelperAndFooter(src);

                        canContinue = false;
                    }
                }

                if (args.<String>getOne("stat").isPresent() && canContinue)
                {
                    stat = args.<String>getOne("stat").get();

                    switch (stat.toUpperCase())
                    {
                        case "HP": case "HITPOINTS": case "HEALTH": case "IVHP": case "IV_HP":
                        {
                            fixedStat = "IVHP";
                            cleanStat = "HP";
                            break;
                        }
                        case "ATTACK": case "ATK": case "ATT": case "IVATTACK": case "IV_ATTACK":
                        {
                            fixedStat = "IVAttack";
                            cleanStat = "Attack";
                            break;
                        }
                        case "DEFENCE": case "DEFENSE": case "DEF": case "IVDEFENCE":
                        case "IV_DEFENCE": case "IVDEFENSE": case "IV_DEFENSE":
                        {
                            fixedStat = "IVDefence";
                            if (useBritishSpelling)
                                cleanStat = "Defence";
                            else
                                cleanStat = "Defense";
                            break;
                        }
                        case "SPECIALATTACK": case "SPATT": case "SPATK": case "SPATTACK": case "IVSPATT":
                        case "IV_SP_ATT": case "IV_SP_ATK": case "IV_SPATK":
                        {
                            fixedStat = "IVSpAtt";
                            cleanStat = "Special Attack";
                            break;
                        }
                        case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEF": case "SPDEFENCE":
                        case "SPDEFENSE": case "IVSPDEF": case "IV_SP_DEF":
                        {
                            fixedStat = "IVSpDef";
                            if (useBritishSpelling)
                                cleanStat = "Special Defence";
                            else
                                cleanStat = "Special Defense";
                            break;
                        }
                        case "SPEED": case "SPD": case "IVSPEED": case "IV_SPEED":
                        {
                            fixedStat = "IVSpeed";
                            cleanStat = "Speed";
                            break;
                        }
                        default:
                            statWasValid = false;
                    }

                    if (!statWasValid)
                    {
                        printToLog(1, "Got an invalid IV type, exit. Type was: §3" + stat);

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid IV type \"§4" + stat + "§c\". See below."));
                        addHelperAndFooter(src);
                        canContinue = false;
                    }
                }
                else if (canContinue)
                {
                    printToLog(1, "No stat (IV type) provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo IV type was provided. See below."));
                    addHelperAndFooter(src);

                    canContinue = false;
                }

                if (!args.<String>getOne("quantity").isPresent() && canContinue)
                {
                    printToLog(2, "No quantity was given, setting to 1.");
                    quantity = 1;
                }
                else if (canContinue)
                {
                    final String quantityString = args.<String>getOne("quantity").get();

                    if (quantityString.equals("-c"))
                    {
                        printToLog(2, "Found confirmation flag on quantity arg, setting q=1 and flagging.");
                        commandConfirmed = true;
                        quantity = 1;
                    }
                    else if (!quantityString.matches("\\d+"))
                    {
                        printToLog(1, "Quantity was not numeric and not a confirmation flag. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cThe quantity (# of times) must be a positive number."));
                        addHelperAndFooter(src);

                        canContinue = false;
                    }
                    else
                    {
                        quantity = Integer.parseInt(args.<String>getOne("quantity").get());

                        if (quantity < 1)
                        {
                            printToLog(1, "Quantity below 1. Exit.");

                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cInvalid # of times. Please enter a positive number."));
                            addHelperAndFooter(src);

                            canContinue = false;
                        }
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    final Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        final PlayerStorage storageCompleted = storage.get();
                        final NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No Pokémon data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean(NbtKeys.IS_EGG))
                        {
                            printToLog(1, "Tried to upgrade an egg. Let's not. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else if (nbt.getString(NbtKeys.NAME).equals("Ditto"))
                        {
                            printToLog(1, "Tried to upgrade a Ditto. Print witty message and exit.");
                            src.sendMessage(Text.of("§4Error: §cI'm sorry, §4" + src.getName() + "§c, but I'm afraid I can't do that."));
                        }
                        else
                        {
                            final int statOld = nbt.getInteger(fixedStat);
                            final int IVHP = nbt.getInteger(NbtKeys.IV_HP);
                            final int IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                            final int IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                            final int IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                            final int IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                            final int IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                            final int totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;

                            final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                            int upgradeTicker = 0;
                            boolean isShiny = false, isLegendary = false;

                            // Let's go through the big ol' wall of checks.
                            // Flag canContinue as false, so we don't have to repeat it like 20 times.
                            canContinue = false;

                            if (totalIVs >= 186)
                            {
                                printToLog(1, "Found a perfect (>186 IVs) Pokémon. Exit.");
                                src.sendMessage(Text.of("§4Error: §cThis Pokémon's stats are already perfect!"));
                            }
                            else if (statOld >= 31)
                            {
                                printToLog(1, "Found a stat >31 that was going to be upgraded. Exit!");
                                src.sendMessage(Text.of("§4Error: §cYou cannot upgrade this stat any further, it's maxed!"));
                            }
                            else
                            {
                                // Let's see what kind of Pokémon we've been provided.
                                if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                                {
                                    printToLog(2, "Provided Pokémon is shiny.");
                                    isShiny = true;
                                }
                                if (EnumSpecies.legendaries.contains(nbt.getString("Name")))
                                {
                                    printToLog(2, "Provided Pokémon is legendary. Applying legendary config amounts.");
                                    isLegendary = true;
                                }

                                    if (isShiny && isLegendary && upgradeCount >= legendaryAndShinyCap)
                                    {
                                        printToLog(1, "Hit cap on shiny legendary Pokémon. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cThis shiny legendary's upgrade cap has been reached!"));
                                    }
                                    else if (isLegendary && upgradeCount >= legendaryCap)
                                    {
                                        printToLog(1, "Hit cap on legendary Pokémon. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cThis legendary's upgrade cap has been reached!"));
                                    }
                                    else if (isShiny && upgradeCount >= shinyCap)
                                    {
                                        printToLog(1, "Hit cap on shiny-but-otherwise-regular Pokémon. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cThis shiny's upgrade cap has been reached!"));
                                    }
                                    else if (!isLegendary && upgradeCount >= regularCap)
                                    {
                                        printToLog(1, "Hit cap on regular Pokémon. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cThis Pokémon's upgrade cap has been reached!"));
                                    }
                                    else
                                        canContinue = true;
                            }

                            if (canContinue)
                            {
                                printToLog(2, "Passed a billion checks and got to the main body. Let's loop!");

                                BigDecimal costToConfirm;
                                boolean freeUpgrade = false, paidUpgrade = false, singleUpgrade = false;
                                final double priceMultiplier;
                                double iteratedValue = 0.0;
                                int remainder;
                                final int initialRemainder;

                                if (isShiny && isLegendary)
                                {
                                    remainder = legendaryAndShinyCap - upgradeCount;
                                    priceMultiplier = legendaryAndShinyMult;
                                }
                                else if (isLegendary)
                                {
                                    remainder = legendaryCap - upgradeCount;
                                    priceMultiplier = legendaryMult;
                                }
                                else if (isShiny)
                                {
                                    remainder = shinyCap - upgradeCount;
                                    priceMultiplier = shinyMult;
                                }
                                else
                                {
                                    remainder = regularCap - upgradeCount;
                                    priceMultiplier = regularMult;
                                }

                                printToLog(2, "Calculated remainder from previous upgrade count + config: §2" + remainder);

                                final StringBuilder listOfValues = new StringBuilder();
                                for (int i = totalIVs + 1; i <= 186; i++)
                                {
                                    listOfValues.append(i);
                                    listOfValues.append(",");
                                }
                                listOfValues.setLength(listOfValues.length() - 1);
                                final String[] outputArray = listOfValues.toString().split(",");
                                initialRemainder = remainder;

                                if (quantity == 1)
                                    singleUpgrade = true;
                                else if (quantity > (31 - statOld)) // Let's sanitize input so we don't exceed 31.
                                    quantity = (31 - statOld);

                                for (final String loopValueAsString : outputArray)
                                {
                                    if (upgradeTicker >= quantity || upgradeTicker >= initialRemainder)
                                        break;

                                    final int loopValue = Integer.valueOf(loopValueAsString);

                                    // freeUpgrade and paidUpgrade can be true at the same time. Pricing and messages change accordingly.
                                    if (loopValue <= upgradesFreeBelow)
                                        freeUpgrade = true;
                                    else
                                    {
                                        if (fixedUpgradeCost > 0)
                                            iteratedValue += fixedUpgradeCost;
                                        else
                                            iteratedValue += Math.exp(loopValue * mathMultiplier);
                                        paidUpgrade = true;
                                    }

                                    upgradeTicker++;
                                    remainder--;
                                }

                                costToConfirm = BigDecimal.valueOf((iteratedValue * priceMultiplier) + addFlatFee);
                                costToConfirm = costToConfirm.setScale(2, RoundingMode.HALF_UP); // Two decimals is all we need.

                                printToLog(2, "Remainder is now: §2" + remainder +
                                        "§a. Freshly baked price: §2" + costToConfirm + "§a.");

                                if (commandConfirmed)
                                {
                                    final String name = nbt.getString("Name");
                                    final String upgradeString = "§eYou upgraded §6" + name + "§e's §6" + cleanStat;

                                    if (isShiny && isLegendary)
                                        upgradeCount = legendaryAndShinyCap - remainder;
                                    else if (isShiny)
                                        upgradeCount = shinyCap - remainder;
                                    else if (isLegendary)
                                        upgradeCount = legendaryCap - remainder;
                                    else
                                        upgradeCount = regularCap - remainder;

                                    // A bit confusing, but an output of 0 on the below statement means we're at 0 cost. 1 is above, -1 is below.
                                    if (costToConfirm.signum() == 0)
                                    {
                                        printToLog(1, "Command confirmed, upgrading! No cost due to low stats or config.");

                                        nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                        pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                        src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                        if (singleUpgrade)
                                            src.sendMessage(Text.of(upgradeString + "§e stat by §6one §epoint!"));
                                        else
                                            src.sendMessage(Text.of(upgradeString + "§e stat by §6" + upgradeTicker + "§e points!"));
                                        src.sendMessage(Text.EMPTY);

                                        if (remainder == 1)
                                            src.sendMessage(Text.of("§aThis upgrade was free. You have §2one §aupgrade remaining..."));
                                        else if (remainder > 1)
                                            src.sendMessage(Text.of("§aThis upgrade was free. You have §2" + remainder + " §aupgrades remaining."));
                                        else
                                            src.sendMessage(Text.of("§aThis upgrade was free. This Pokémon is now at its limits."));
                                        src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                    }
                                    else
                                    {
                                        final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                        if (optionalAccount.isPresent())
                                        {
                                            final UniqueAccount uniqueAccount = optionalAccount.get();
                                            BigDecimal newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                            printToLog(2, "Entering final stage, got confirmation. Current cash: §2" + newTotal + "§a.");

                                            final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                        costToConfirm, Sponge.getCauseStackManager().getCurrentCause());
                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                                pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                                src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                                if (singleUpgrade)
                                                    src.sendMessage(Text.of(upgradeString + "§e stat by §6one §epoint!"));
                                                else
                                                    src.sendMessage(Text.of(upgradeString + "§e stat by §6" + upgradeTicker + "§e points!"));

                                                if (costToConfirm.signum() == 1) // 1 = we've got a cost. 0 = cost is zero. -1 would be negative.
                                                {
                                                    final String paidString = "§aYou paid §2" + costToConfirm + "§a coins";
                                                    src.sendMessage(Text.EMPTY);

                                                    if (remainder == 1)
                                                        src.sendMessage(Text.of(paidString + ". §2One §aupgrade remains..."));
                                                    else if (remainder > 1)
                                                        src.sendMessage(Text.of(paidString + ". §2" + remainder + " §aupgrades remain."));
                                                    else
                                                        src.sendMessage(Text.of(paidString + ", and reached this Pokémon's limits."));
                                                }
                                                else if (costToConfirm.signum() == 0) // Cost is zero, either due to low stats or config.
                                                {
                                                    src.sendMessage(Text.EMPTY);

                                                    if (remainder == 1)
                                                        src.sendMessage(Text.of("§2One §aupgrade remains..."));
                                                    else if (remainder > 1)
                                                        src.sendMessage(Text.of("§2" + remainder + " §aupgrades remain..."));
                                                    else
                                                        src.sendMessage(Text.of("You've now reached this Pokémon's limits."));
                                                }
                                                src.sendMessage(Text.of("§7-----------------------------------------------------"));

                                                newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                                printToLog(1, "Upgraded one or more IVs, taking §3" +
                                                        costToConfirm + "§b coins. New total: §3" + newTotal);
                                            }
                                            else
                                            {
                                                final BigDecimal balanceNeeded = newTotal.subtract(costToConfirm).abs();
                                                printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                                        "§b, and we're lacking §3" + balanceNeeded);

                                                src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded +
                                                        "§c more coins to do this."));
                                            }
                                        }
                                        else
                                        {
                                            printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                                            src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                        }
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Got no confirmation; end of the line. Exit.");

                                    src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                    final String helperString = "§eThe §6" + cleanStat + "§e stat will be upgraded by §6";
                                    final String syntaxString = "§2Ready? Use: §a/" + commandAlias + " " + slot + " " + stat;

                                    if (quantity == 1)
                                        src.sendMessage(Text.of(helperString + "one §epoint!"));
                                    else if (quantity > (31 - statOld))
                                        src.sendMessage(Text.of(helperString + upgradeTicker + "§e points, up to the cap!"));
                                    else
                                        src.sendMessage(Text.of(helperString + upgradeTicker + "§e points!"));

                                    if (freeUpgrade && !paidUpgrade && remainder > 0 && costToConfirm.signum() == 0)
                                        src.sendMessage(Text.of("§eThis upgrade will be free due to your Pokémon's low stats."));
                                    else if (freeUpgrade && !paidUpgrade && costToConfirm.signum() == 0)
                                        src.sendMessage(Text.of("§eThis final upgrade will be free due to low stats."));
                                    else if (freeUpgrade && remainder > 0)
                                    {
                                        // Is cost to confirm exactly one coin?
                                        if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                            src.sendMessage(Text.of("§eThis upgrade costs §6one §ecoin, with low stat compensation."));
                                        else
                                        {
                                            src.sendMessage(Text.of("§6This upgrade costs §6" + costToConfirm +
                                                    " §ecoins, with low stat compensation."));
                                        }
                                    }
                                    else if (freeUpgrade) // Lacking space. Slightly awkward message, but it'll do.
                                    {
                                        // Is cost to confirm exactly one coin?
                                        if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                            src.sendMessage(Text.of("§eThis last upgrade costs §6one §ecoin with low stat compensation."));
                                        else
                                        {
                                            src.sendMessage(Text.of("§eThis last upgrade costs §6" + costToConfirm +
                                                    " §ecoins with low stat compensation."));
                                        }
                                    }
                                    else if (remainder == 0)
                                    {
                                        // Is cost to confirm exactly one coin?
                                        if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                            src.sendMessage(Text.of("§eThis final upgrade will cost §6one §ecoin upon confirmation."));
                                        else
                                        {
                                            src.sendMessage(Text.of("§eThis final upgrade will cost §6" + costToConfirm +
                                                    " §ecoins upon confirmation."));
                                        }
                                    }
                                    else
                                    {
                                        // Is cost to confirm exactly one coin?
                                        if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                            src.sendMessage(Text.of("§eThis upgrade will cost §6one §ecoin upon confirmation."));
                                        else
                                        {
                                            src.sendMessage(Text.of("§eThis upgrade will cost §6" + costToConfirm +
                                                    " §ecoins upon confirmation."));
                                        }
                                    }

                                    src.sendMessage(Text.EMPTY);

                                    // Are we above 0 coins?
                                    if (costToConfirm.compareTo(BigDecimal.ZERO) > 0)
                                        src.sendMessage(Text.of("§5Warning: §dYou can't undo upgrades! Make sure you want this."));

                                    if (quantity == 1)
                                        src.sendMessage(Text.of(syntaxString + " -c"));
                                    else
                                        src.sendMessage(Text.of(syntaxString + " " + upgradeTicker + " -c"));
                                    src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (!economyEnabled)
            src.sendMessage(Text.of("§4Error: §cThis server does not have an economy plugin installed."));
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

    private void addHelperAndFooter(final CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot> <IV type> [amount?] {-c to confirm}"));

        if (useBritishSpelling)
            src.sendMessage(Text.of("§2Valid types: §aHP, Attack, Defence, SpAtt, SpDef, Speed"));
        else
            src.sendMessage(Text.of("§2Valid types: §aHP, Attack, Defense, SpAtt, SpDef, Speed"));

        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
        src.sendMessage(Text.of("§eConfirming will immediately take your money, if you have enough!"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}*/