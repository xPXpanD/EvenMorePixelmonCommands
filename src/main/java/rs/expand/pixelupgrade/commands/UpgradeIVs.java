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
                PixelUpgrade.log.info("§4Upgrade // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4Upgrade // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
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
            PixelUpgrade.log.info("§4Upgrade // critical: §cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    // Set up a variable that we'll be using in command helper messages.
    private Boolean useBritishSpelling = null;

	public CommandResult execute(CommandSource src, CommandContext args)
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
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4Upgrade // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (useBritishSpelling == null)
            {
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                PixelUpgrade.log.info("§4CheckEgg // critical: §cCouldn't get value of \"useBritishSpelling\" from the main config.");
                PixelUpgrade.log.info("§4CheckEgg // critical: §cPlease check (or wipe and reload) your PixelUpgrade.conf file.");
            }
            else
            {
                printToLog(2, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                String stat = null, fixedStat = null, cleanStat = "Error, please report!";
                boolean canContinue = true, commandConfirmed = false, statWasValid = true;
                int slot = 0, quantity = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "No parameters provided. Abort.");

                    player.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
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

                        player.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
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
                        printToLog(2, "Got an invalid IV type, type was: §3" + stat);

                        player.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid IV type \"§4" + stat + "§c\". See below."));
                        printCorrectPerm(player);
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                }
                else if (canContinue)
                {
                    printToLog(2, "No stat (IV type) provided. Aborting.");

                    player.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo IV type was provided. See below."));
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

                        player.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cThe quantity (# of times) must be a positive number."));
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

                            player.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cInvalid # of times. Please enter a positive number."));
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
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else if (nbt == null)
                    {
                        printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                        src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                    }
                    else if (nbt.getBoolean("isEgg"))
                    {
                        printToLog(2, "Tried to upgrade an egg. Let's not, aborting.");
                        src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                    }
                    else if (nbt.getString("Name").equals("Ditto"))
                    {
                        printToLog(2, "Tried to upgrade a Ditto. Print witty message and abort.");
                        src.sendMessage(Text.of("§4Error: §cI'm sorry, §4" + src.getName() + "§c, but I'm afraid I can't do that."));
                    }
                    else
                    {
                        int statOld = nbt.getInteger(fixedStat);
                        int IVHP = nbt.getInteger(NbtKeys.IV_HP);
                        int IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                        int IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                        int IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                        int IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                        int IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                        int totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;

                        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                        int upgradeTicker = 0, upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                        boolean isShiny = false, isLegendary = false, isBaby = false;

                        // Let's see what kind of Pokémon we've been provided.
                        if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                        {
                            printToLog(3, "Provided Pokémon is shiny.");
                            isShiny = true;
                        }
                        if (nbt.getString("Name").equals("Riolu") || nbt.getString("Name").equals("Mime Jr.") || nbt.getString("Name").equals("Happiny"))
                        {
                            printToLog(3, "Provided Pokémon is a known 3*31 IV baby.");
                            isBaby = true;
                        }
                        if (EnumPokemon.legendaries.contains(nbt.getString("Name")))
                        {
                            printToLog(3, "Provided Pokémon is shiny. Applying shiny config amounts.");
                            isLegendary = true;
                        }

                        // Let's go through the big ol' wall of checks.
                        if (totalIVs >= 186)
                        {
                            printToLog(2, "Found a perfect (>186 IVs) Pokémon. Nothing left to do here!");
                            src.sendMessage(Text.of("§4Error: §cThis Pokémon's stats are already perfect!"));
                        }
                        else if (statOld >= 31)
                        {
                            printToLog(2, "Found a stat >31 that was going to be upgraded. Let's not do that!");
                            src.sendMessage(Text.of("§4Error: §cYou cannot upgrade this stat any further, it's maxed!"));
                        }
                        else if (isShiny && isLegendary && upgradeCount >= legendaryAndShinyCap)
                        {
                            printToLog(2, "Hit cap on shiny legendary Pokémon.");
                            src.sendMessage(Text.of("§4Error: §cThis §eshiny legendary§c's upgrade cap has been reached!"));
                        }
                        else if (isShiny && upgradeCount >= shinyCap)
                        {
                            printToLog(2, "Hit cap on shiny Pokémon.");
                            src.sendMessage(Text.of("§4Error: §cThis §eshiny§c's upgrade cap has been reached!"));
                        }
                        else if (!isShiny && isLegendary && upgradeCount >= legendaryCap)
                        {
                            printToLog(2, "Hit cap on legendary Pokémon.");
                            src.sendMessage(Text.of("§4Error: §cThis §elegendary§c's upgrade cap has been reached!"));
                        }
                        else if (!isShiny && isBaby && upgradeCount >= babyCap)
                        {
                            printToLog(2, "Hit cap on baby Pokémon.");
                            src.sendMessage(Text.of("§4Error: §cThis §6baby§c's upgrade cap has been reached!"));
                        }
                        else if (!isShiny && !isLegendary && !isBaby && upgradeCount >= regularCap)
                        {
                            printToLog(2, "Hit cap on regular Pokémon.");
                            src.sendMessage(Text.of("§4Error: §cThis Pokémon's upgrade cap has been reached!"));
                        }
                        else
                        {
                            printToLog(3, "Passed a billion checks and got to the main body. Let's loop!");

                            BigDecimal costToConfirm;
                            boolean freeUpgrade = false, paidUpgrade = false, singleUpgrade = false;
                            double priceMultiplier, iteratedValue = 0.0;
                            int remainder, initialRemainder;

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

                            printToLog(3, "Calculated remainder from previous upgrade count + config: §2" + remainder);

                            StringBuilder listOfValues = new StringBuilder();
                            for (int i = totalIVs + 1; i <= 186; i++)
                            {
                                listOfValues.append(i);
                                listOfValues.append(",");
                            }
                            listOfValues.setLength(listOfValues.length() - 1);
                            String[] outputArray = listOfValues.toString().split(",");
                            initialRemainder = remainder;

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
                            costToConfirm = costToConfirm.setScale(2, RoundingMode.HALF_UP); // Two decimals is all we need.

                            printToLog(3, "Remainder is now: §2" + remainder + "§a. Freshly baked price: §2" + costToConfirm + "§a.");

                            if (commandConfirmed)
                            {
                                String name = nbt.getString("Name");
                                String upgradeString = "§eYou upgraded your §6" + name + "§e's §6" + cleanStat;

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

                                    player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                    if (singleUpgrade)
                                        player.sendMessage(Text.of(upgradeString + "§e stat by §6one §epoint!"));
                                    else
                                        player.sendMessage(Text.of(upgradeString + "§e stat by §6" + upgradeTicker + "§e points!"));
                                    player.sendMessage(Text.of(""));

                                    if (remainder == 1)
                                        src.sendMessage(Text.of("§dThis upgrade was free. You have §5one §dupgrade remaining..."));
                                    else if (remainder > 1)
                                        src.sendMessage(Text.of("§dThis upgrade was free. You have §5" + remainder + " §dupgrades remaining."));
                                    else
                                        src.sendMessage(Text.of("§dThis upgrade was free. This Pokémon is now at its limits."));
                                    player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                }
                                else
                                {
                                    Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                    if (optionalAccount.isPresent())
                                    {
                                        UniqueAccount uniqueAccount = optionalAccount.get();
                                        BigDecimal newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                        printToLog(1, "Entering final stage, got confirmation. Current cash: §6" + newTotal + "§e.");

                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());
                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            nbt.setInteger(fixedStat, nbt.getInteger(fixedStat) + upgradeTicker);
                                            pokemon.getEntityData().setInteger("upgradeCount", upgradeCount);

                                            player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                            if (singleUpgrade)
                                                player.sendMessage(Text.of(upgradeString + "§e stat by §6one §epoint!"));
                                            else
                                                player.sendMessage(Text.of(upgradeString + "§e stat by §6" + upgradeTicker + "§e points!"));

                                            if (costToConfirm.signum() == 1) // 1 = we've got a cost. 0 = cost is zero. -1 would be negative.
                                            {
                                                String paidString = "§dYou paid §5" + costToConfirm + "§d coins";
                                                player.sendMessage(Text.of(""));

                                                if (remainder == 1)
                                                    src.sendMessage(Text.of(paidString + ". §5One §dupgrade remains..."));
                                                else if (remainder > 1)
                                                    src.sendMessage(Text.of(paidString + ". §5" + remainder + " §dupgrades remain."));
                                                else
                                                    src.sendMessage(Text.of(paidString + ", and reached this Pokémon's limits."));
                                            }
                                            else if (costToConfirm.signum() == 0) // Cost is zero, either due to low stats or config.
                                            {
                                                player.sendMessage(Text.of(""));

                                                if (remainder == 1)
                                                    src.sendMessage(Text.of("§5One §dupgrade remains..."));
                                                else if (remainder > 1)
                                                    src.sendMessage(Text.of("§5" + remainder + " §dupgrades remain..."));
                                                else
                                                    src.sendMessage(Text.of("You've now reached this Pokémon's limits."));
                                            }
                                            player.sendMessage(Text.of("§7-----------------------------------------------------"));

                                            newTotal = uniqueAccount.getBalance(economyService.getDefaultCurrency());
                                            printToLog(1, "Upgraded an IV, and took §7" + costToConfirm + "§e coins. New total: §6" + newTotal);
                                        }
                                        else
                                        {
                                            BigDecimal balanceNeeded = newTotal.subtract(costToConfirm).abs();
                                            printToLog(2, "Not enough coins! Cost: §3" + costToConfirm + "§b, lacking: §3" + balanceNeeded);

                                            src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
                                        }
                                    }
                                    else
                                    {
                                        printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. May be a bug?");
                                        src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                    }
                                }
                            }
                            else
                            {
                                printToLog(2, "Got no confirmation; end of the line.");

                                player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                String helperString = "§eThe §6" + cleanStat + "§e stat will be upgraded by §6";
                                String quantityString = "§aReady? Use: §2" + alias + " " + slot + " " + stat;

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
                                    src.sendMessage(Text.of("§eThis upgrade costs §6" + costToConfirm + " coins§e, with low stat compensation."));
                                else if (freeUpgrade) // Lacking space. Slightly awkward message, but it'll do.
                                    src.sendMessage(Text.of("§eThis last upgrade costs §6" + costToConfirm + " coins§e with low stat compensation."));
                                else if (remainder == 0)
                                    src.sendMessage(Text.of("§eThis final upgrade will cost §6" + costToConfirm + " coins§e upon confirmation."));
                                else
                                    src.sendMessage(Text.of("§eThis upgrade will cost §6" + costToConfirm + " coins§e upon confirmation."));
                                src.sendMessage(Text.of(""));

                                if (costToConfirm.compareTo(BigDecimal.ZERO) == 1) // Are we above 0 coins?
                                    src.sendMessage(Text.of("§5Warning: §dYou can't undo upgrades! Make sure you want this."));

                                if (quantity == 1)
                                    src.sendMessage(Text.of(quantityString + " -c"));
                                else
                                    src.sendMessage(Text.of(quantityString + " " + upgradeTicker + " -c"));
                                player.sendMessage(Text.of("§7-----------------------------------------------------"));
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

    private void checkAndAddFooter(Player player)
    {
        if (useBritishSpelling)
            player.sendMessage(Text.of("§2Valid types: §aHP, Attack, Defence, SpAtt, SpDef, Speed"));
        else
            player.sendMessage(Text.of("§2Valid types: §aHP, Attack, Defense, SpAtt, SpDef, Speed"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
        player.sendMessage(Text.of("§eConfirming will immediately take your money, if you have enough!"));
        player.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void printCorrectPerm(Player player)
    {
        player.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <IV type> [amount?] {-c to confirm}"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4Upgrade // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§6Upgrade // important: §e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("§3Upgrade // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2Upgrade // debug: §a" + inputString);
        }
    }

    private Integer checkConfigInt(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4Upgrade // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Double checkConfigDouble(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getDouble();
        else
        {
            PixelUpgrade.log.info("§4Upgrade // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }
}