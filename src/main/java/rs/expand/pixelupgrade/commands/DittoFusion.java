package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
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
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.DittoFusionConfig;
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;

import java.math.BigDecimal;
import java.util.Optional;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class DittoFusion implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!DittoFusionConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = DittoFusionConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            boolean presenceCheck = true;
            Boolean passOnShinyStatus, useBritishSpelling = null;
            Integer stat0to5, stat6to10, stat11to15, stat16to20, stat21to25, stat26to30, stat31plus;
            Integer regularCap, shinyCap, previouslyUpgradedMultiplier, pointMultiplierForCost, addFlatFee;

            stat0to5 = checkConfigInt("stat0to5");
            stat6to10 = checkConfigInt("stat6to10");
            stat11to15 = checkConfigInt("stat11to15");
            stat16to20 = checkConfigInt("stat16to20");
            stat21to25 = checkConfigInt("stat21to25");
            stat26to30 = checkConfigInt("stat26to30");
            stat31plus = checkConfigInt("stat31plus");
            regularCap = checkConfigInt("regularCap");
            shinyCap = checkConfigInt("shinyCap");
            previouslyUpgradedMultiplier = checkConfigInt("previouslyUpgradedMultiplier");
            passOnShinyStatus = checkConfigBool();
            pointMultiplierForCost = checkConfigInt("pointMultiplierForCost");
            addFlatFee = checkConfigInt("addFlatFee");

            // Grab the useBritishSpelling value from the main config.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").isVirtual())
                useBritishSpelling = PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").getBoolean();

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (passOnShinyStatus == null || regularCap == null || shinyCap == null || stat0to5 == null || stat6to10 == null)
                presenceCheck = false;
            else if (stat11to15 == null || stat16to20 == null || stat21to25 == null || stat26to30 == null || stat31plus == null)
                presenceCheck = false;
            else if (previouslyUpgradedMultiplier == null || pointMultiplierForCost == null || addFlatFee == null)
                presenceCheck = false;

            if (!presenceCheck || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7cCheck your config. If need be, wipe and \u00A74/pureload\u00A7c.");
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
                int slot1 = 0, slot2 = 0;
                boolean commandConfirmed = false, canContinue = true;

                if (!args.<String>getOne("target slot").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo slots were provided. Please provide two valid slots."));
                    checkAndAddFooter(player);

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("target slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Target slot was a valid slot number. Let's move on!");
                        slot1 = Integer.parseInt(args.<String>getOne("target slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot for target Pok\u00E9mon. Aborting.");

                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid value on target slot. Valid values are 1-6."));
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                }

                if (!args.<String>getOne("sacrifice slot").isPresent() && canContinue)
                {
                    printToLog(2, "No sacrifice Pok\u00E9mon slot provided. Aborting.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo sacrifice provided. Please provide two valid slots."));
                    checkAndAddFooter(player);

                    canContinue = false;
                }
                else if (canContinue)
                {
                    String slotString = args.<String>getOne("sacrifice slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Valid slot found on argument 2. Checking against arg1...");
                        slot2 = Integer.parseInt(args.<String>getOne("sacrifice slot").get());

                        if (slot2 == slot1)
                        {
                            printToLog(2, "Caught " + src.getName() + " attempting to upgrade a Pok\u00E9mon with itself. Abort.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou can't fuse a Pok\u00E9mon with itself."));
                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(2, "Invalid slot for sacrifice Pok\u00E9mon. Aborting.");

                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid value on sacrifice slot. Valid values are 1-6."));
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt1 = storageCompleted.partyPokemon[slot1 - 1];
                        NBTTagCompound nbt2 = storageCompleted.partyPokemon[slot2 - 1];

                        if (nbt1 == null && nbt2 != null)
                        {
                            printToLog(2, "No NBT found for target Pok\u00E9mon, slot probably empty. Aborting.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThe target Pok\u00E9mon does not seem to exist."));
                        }
                        else if (nbt1 != null && nbt2 == null)
                        {
                            printToLog(2, "No NBT found for sacrifice Pok\u00E9mon, slot probably empty. Aborting.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThe sacrifice Pok\u00E9mon does not seem to exist."));
                        }
                        else if (nbt1 == null)
                        {
                            printToLog(2, "No NBT found for target not sacrifice, slots probably empty. Aborting.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cBoth the target and sacrifice do not seem to exist."));
                        }
                        else
                        {
                            Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                if (!nbt1.getString("Name").equals("Ditto") && nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(2, "Target was not a Ditto. Abort, abort!");
                                    src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target Pok\u00E9mon is not a Ditto."));
                                }
                                else if (nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(2, "Sacrifice was not a Ditto. Abort, abort!");
                                    src.sendMessage(Text.of("\u00A74Error: \u00A7cSorry, but the sacrifice needs to be a Ditto."));
                                }
                                else if (!nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(2, "No Dittos in provided slots; Let's not create some unholy abomination. Abort.");
                                    src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command only works on Dittos."));
                                }
                                else
                                {
                                    EntityPixelmon targetPokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt1, (World) player.getWorld());
                                    EntityPixelmon sacrificePokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt2, (World) player.getWorld());
                                    int targetFuseCount = targetPokemon.getEntityData().getInteger("fuseCount");
                                    int sacrificeFuseCount = sacrificePokemon.getEntityData().getInteger("fuseCount");

                                    if (targetFuseCount >= shinyCap && nbt1.getInteger(NbtKeys.IS_SHINY) == 1)
                                    {
                                        printToLog(2, "Hit the shiny cap on target Ditto. Aborting.");

                                        src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target shiny Ditto cannot grow any further."));
                                        src.sendMessage(Text.of("\u00A76Tip: \u00A7eYou could still sacrifice \u00A7othis\u00A7r\u00A7e Ditto... You monster."));
                                    }
                                    else if (targetFuseCount >= regularCap && nbt1.getInteger(NbtKeys.IS_SHINY) != 1)
                                    {
                                        printToLog(2, "Hit the non-shiny cap on target Ditto. Aborting.");

                                        src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target Ditto cannot grow any further."));
                                        src.sendMessage(Text.of("\u00A76Tip: \u00A7eYou could still sacrifice \u00A7othis\u00A7r\u00A7e Ditto... You monster."));
                                    }
                                    else
                                    {
                                        printToLog(3, "Passed the majority of checks, moving on to actual command logic.");

                                        UniqueAccount uniqueAccount = optionalAccount.get();
                                        int HPPlusNum = 0, ATKPlusNum = 0, DEFPlusNum = 0, SPATKPlusNum = 0;
                                        int SPDEFPlusNum = 0, SPDPlusNum = 0, statToCheck = 0, statToUpgrade;

                                        int targetHP = nbt1.getInteger(NbtKeys.IV_HP);
                                        int targetATK = nbt1.getInteger(NbtKeys.IV_ATTACK);
                                        int targetDEF = nbt1.getInteger(NbtKeys.IV_DEFENCE);
                                        int targetSPATK = nbt1.getInteger(NbtKeys.IV_SP_ATT);
                                        int targetSPDEF = nbt1.getInteger(NbtKeys.IV_SP_DEF);
                                        int targetSPD = nbt1.getInteger(NbtKeys.IV_SPEED);

                                        int sacrificeHP = nbt2.getInteger(NbtKeys.IV_HP);
                                        int sacrificeATK = nbt2.getInteger(NbtKeys.IV_ATTACK);
                                        int sacrificeDEF = nbt2.getInteger(NbtKeys.IV_DEFENCE);
                                        int sacrificeSPATK = nbt2.getInteger(NbtKeys.IV_SP_ATT);
                                        int sacrificeSPDEF = nbt2.getInteger(NbtKeys.IV_SP_DEF);
                                        int sacrificeSPD = nbt2.getInteger(NbtKeys.IV_SPEED);

                                        for (int i = 0; i <= 5; i++)
                                        {
                                            switch (i)
                                            {
                                                case 0: statToCheck = sacrificeHP; break;
                                                case 1: statToCheck = sacrificeATK; break;
                                                case 2: statToCheck = sacrificeDEF; break;
                                                case 3: statToCheck = sacrificeSPATK; break;
                                                case 4: statToCheck = sacrificeSPDEF; break;
                                                case 5: statToCheck = sacrificeSPD; break;
                                            }

                                            if (statToCheck < 6) // my fancy old switched range check didn't work right... how embarassing!
                                                statToUpgrade = stat0to5;
                                            else if (statToCheck < 11)
                                                statToUpgrade = stat6to10;
                                            else if (statToCheck < 16)
                                                statToUpgrade = stat11to15;
                                            else if (statToCheck < 21)
                                                statToUpgrade = stat16to20;
                                            else if (statToCheck < 26)
                                                statToUpgrade = stat21to25;
                                            else if (statToCheck < 31)
                                                statToUpgrade = stat26to30;
                                            else
                                                statToUpgrade = stat31plus;

                                            switch (i)
                                            {
                                                case 0: HPPlusNum = statToUpgrade; break;
                                                case 1: ATKPlusNum = statToUpgrade; break;
                                                case 2: DEFPlusNum = statToUpgrade; break;
                                                case 3: SPATKPlusNum = statToUpgrade; break;
                                                case 4: SPDEFPlusNum = statToUpgrade; break;
                                                case 5: SPDPlusNum = statToUpgrade; break;
                                            }
                                        }

                                        if (targetHP >= 31)
                                            HPPlusNum = 0;
                                        else if (HPPlusNum + targetHP >= 31)
                                            HPPlusNum = 31 - targetHP;

                                        if (targetATK >= 31)
                                            ATKPlusNum = 0;
                                        else if (ATKPlusNum + targetATK >= 31)
                                            ATKPlusNum = 31 - targetATK;

                                        if (targetDEF >= 31)
                                            DEFPlusNum = 0;
                                        else if (DEFPlusNum + targetDEF >= 31)
                                            DEFPlusNum = 31 - targetDEF;

                                        if (targetSPATK >= 31)
                                            SPATKPlusNum = 0;
                                        else if (SPATKPlusNum + targetSPATK >= 31)
                                            SPATKPlusNum = 31 - targetSPATK;

                                        if (targetSPDEF >= 31)
                                            SPDEFPlusNum = 0;
                                        else if (SPDEFPlusNum + targetSPDEF >= 31)
                                            SPDEFPlusNum = 31 - targetSPDEF;

                                        if (targetSPD >= 31)
                                            SPDPlusNum = 0;
                                        else if (SPDPlusNum + targetSPD >= 31)
                                            SPDPlusNum = 31 - targetSPD;

                                        int totalUpgradeCount = HPPlusNum + ATKPlusNum + DEFPlusNum + SPATKPlusNum;
                                        totalUpgradeCount = totalUpgradeCount + SPDEFPlusNum + SPDPlusNum;
                                        BigDecimal costToConfirm = BigDecimal.valueOf((totalUpgradeCount * pointMultiplierForCost) + addFlatFee);
                                        BigDecimal nonMultipliedCost = costToConfirm, extraCost = new BigDecimal(0);

                                        if (sacrificeFuseCount > 0)
                                        {
                                            costToConfirm = costToConfirm.multiply(new BigDecimal(previouslyUpgradedMultiplier));
                                            extraCost = costToConfirm.subtract(nonMultipliedCost);
                                        }

                                        if (totalUpgradeCount == 0)
                                        {
                                            printToLog(2, "Sacrifice was too weak to add any stats, apparently. Wow. Abort.");
                                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYour sacrificial Ditto is too weak to make a difference."));
                                        }
                                        else if (commandConfirmed)
                                        {
                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());
                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                                src.sendMessage(Text.of("\u00A7eThe \u00A76Ditto \u00A7ein slot \u00A76" + slot2 +
                                                    "\u00A7e was eaten, taking \u00A76" + costToConfirm + "\u00A7e coins with it."));
                                                src.sendMessage(Text.of(""));

                                                if (HPPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("\u00A7bUpgraded HP!"));
                                                    src.sendMessage(Text.of("\u00A77" + targetHP + " \u00A7f-> \u00A7a" + (targetHP + HPPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_HP, nbt1.getInteger(NbtKeys.IV_HP) + HPPlusNum);
                                                }
                                                if (ATKPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("\u00A7bUpgraded Attack!"));
                                                    src.sendMessage(Text.of("\u00A77" + targetATK + " \u00A7f-> \u00A7a" + (targetATK + ATKPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_ATTACK, nbt1.getInteger(NbtKeys.IV_ATTACK) + ATKPlusNum);
                                                }
                                                if (DEFPlusNum != 0)
                                                {
                                                    if (useBritishSpelling)
                                                        src.sendMessage(Text.of("\u00A7bUpgraded Defence!"));
                                                    else
                                                        src.sendMessage(Text.of("\u00A7bUpgraded Defense!"));

                                                    src.sendMessage(Text.of("\u00A77" + targetDEF + " \u00A7f-> \u00A7a" + (targetDEF + DEFPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_DEFENCE, nbt1.getInteger(NbtKeys.IV_DEFENCE) + DEFPlusNum);
                                                }
                                                if (SPATKPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("\u00A7bUpgraded Special Attack!"));
                                                    src.sendMessage(Text.of("\u00A77" + targetSPATK + " \u00A7f-> \u00A7a" + (targetSPATK + SPATKPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SP_ATT, nbt1.getInteger(NbtKeys.IV_SP_ATT) + SPATKPlusNum);
                                                }
                                                if (SPDEFPlusNum != 0)
                                                {
                                                    if (useBritishSpelling)
                                                        src.sendMessage(Text.of("\u00A7bUpgraded Special Defence!"));
                                                    else
                                                        src.sendMessage(Text.of("\u00A7bUpgraded Special Defense!"));

                                                    src.sendMessage(Text.of("\u00A77" + targetSPDEF + " \u00A7f-> \u00A7a" + (targetSPDEF + SPDEFPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SP_DEF, nbt1.getInteger(NbtKeys.IV_SP_DEF) + SPDEFPlusNum);
                                                }
                                                if (SPDPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("\u00A7bUpgraded Speed!"));
                                                    src.sendMessage(Text.of("\u00A77" + targetSPD + " \u00A7f-> \u00A7a" + (targetSPD + SPDPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SPEED, nbt1.getInteger(NbtKeys.IV_SPEED) + SPDPlusNum);
                                                }

                                                if (sacrificeFuseCount > 0)
                                                {
                                                    src.sendMessage(Text.of(""));
                                                    src.sendMessage(Text.of("\u00A7dSacrifice had prior upgrades. You paid an extra \u00A75" + extraCost + "\u00A7d coins."));
                                                }

                                                if (nbt2.getInteger(NbtKeys.IS_SHINY) == 1 && passOnShinyStatus)
                                                {
                                                    printToLog(3, "Passing on shinyness is enabled, and sacrifice is shiny. Go go go!");

                                                    // Not sure which one I need, so I set both. Doesn't seem to matter much.
                                                    nbt1.setInteger(NbtKeys.IS_SHINY, 1);
                                                    nbt1.setInteger(NbtKeys.SHINY, 1);

                                                    src.sendMessage(Text.of("\u00A7dPlease reconnect to update your fused \u00A75Ditto\u00A7d's shiny status!"));
                                                }

                                                player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));

                                                targetPokemon.getEntityData().setInteger("fuseCount", targetFuseCount + 1);
                                                storageCompleted.changePokemonAndAssignID(slot2 - 1, null);
                                                //storageCompleted.update(targetPokemon, EnumUpdateType.Status);

                                                printToLog(1, "Transaction successful. Took: " + costToConfirm + " and a Ditto. Current cash: " + uniqueAccount.getBalance(economyService.getDefaultCurrency()));
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                                printToLog(2, "Not enough coins! Cost: \u00A73" + costToConfirm + "\u00A7b, lacking: \u00A73" + balanceNeeded);

                                                src.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                                            }
                                        }
                                        else
                                        {
                                            printToLog(2, "Got cost but no confirmation; end of the line.");

                                            player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                            if (nbt2.getInteger(NbtKeys.IS_SHINY) == 1 && passOnShinyStatus)
                                                src.sendMessage(Text.of("\u00A7eThe Ditto in slot \u00A76" + slot1 + "\u00A7e will be upgraded. Transferring shiny status!"));
                                            else
                                                src.sendMessage(Text.of("\u00A7eYou are about to upgrade the Ditto in slot \u00A76" + slot1 + "\u00A7e."));

                                            src.sendMessage(Text.of(""));

                                            if (HPPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("\u00A7bHP will be upgraded."));
                                                src.sendMessage(Text.of("\u00A77" + targetHP + " \u00A7f-> \u00A7a" + (targetHP + HPPlusNum)));
                                            }
                                            if (ATKPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("\u00A7bAttack will be upgraded."));
                                                src.sendMessage(Text.of("\u00A77" + targetATK + " \u00A7f-> \u00A7a" + (targetATK + ATKPlusNum)));
                                            }
                                            if (DEFPlusNum != 0)
                                            {
                                                if (useBritishSpelling)
                                                    src.sendMessage(Text.of("\u00A7bDefence will be upgraded."));
                                                else
                                                    src.sendMessage(Text.of("\u00A7bDefense will be upgraded."));

                                                src.sendMessage(Text.of("\u00A77" + targetDEF + " \u00A7f-> \u00A7a" + (targetDEF + DEFPlusNum)));
                                            }
                                            if (SPATKPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("\u00A7bSpecial Attack will be upgraded."));
                                                src.sendMessage(Text.of("\u00A77" + targetSPATK + " \u00A7f-> \u00A7a" + (targetSPATK + SPATKPlusNum)));
                                            }
                                            if (SPDEFPlusNum != 0)
                                            {
                                                if (useBritishSpelling)
                                                    src.sendMessage(Text.of("\u00A7bSpecial Defence will be upgraded."));
                                                else
                                                    src.sendMessage(Text.of("\u00A7bSpecial Defense will be upgraded."));

                                                src.sendMessage(Text.of("\u00A77" + targetSPDEF + " \u00A7f-> \u00A7a" + (targetSPDEF + SPDEFPlusNum)));
                                            }
                                            if (SPDPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("\u00A7bSpeed will be upgraded."));
                                                src.sendMessage(Text.of("\u00A77" + targetSPD + " \u00A7f-> \u00A7a" + (targetSPD + SPDPlusNum)));
                                            }

                                            src.sendMessage(Text.of(""));
                                            if (sacrificeFuseCount > 0)
                                                src.sendMessage(Text.of("\u00A7eFusing costs \u00A76" + costToConfirm + "\u00A7e coins plus \u00A76"
                                                    + extraCost + "\u00A7e coins from prior upgrades."));
                                            else
                                                src.sendMessage(Text.of("\u00A7eThis upgrade will cost you \u00A76" + costToConfirm + "\u00A7e coins."));
                                            src.sendMessage(Text.of("\u00A7aReady? Use: \u00A72" + alias + " " + slot1 + " " + slot2 + " -c"));

                                            src.sendMessage(Text.of("\u00A74Warning: \u00A7cThe Ditto in slot \u00A74" + slot2 + "\u00A7c will be \u00A7ldeleted\u00A7r\u00A7c!"));
                                            player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));
                                printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
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

    private Boolean checkConfigBool()
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode("passOnShinyStatus").isVirtual())
            return DittoFusionConfig.getInstance().getConfig().getNode("passOnShinyStatus").getBoolean();
        else
        {
            PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7cCould not parse config variable \"passOnShinyStatus\"!");
            return null;
        }
    }

    private Integer checkConfigInt(String node)
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode(node).isVirtual())
            return DittoFusionConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("\u00A74DittoFusion // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private void checkAndAddFooter(Player player)
    {
        player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <target slot> <sacrifice slot> {-c to confirm}"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're ready to spend money!"));
        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }
}
