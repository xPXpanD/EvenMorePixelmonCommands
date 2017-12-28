package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import java.math.BigDecimal;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.configs.DittoFusionConfig;
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;
import rs.expand.pixelupgrade.PixelUpgrade;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class DittoFusion implements CommandExecutor
{
    // Not sure how this works yet, but nicked it from TotalEconomy.
    // Will try to figure this out later, just glad to have this working for now.
    private PixelUpgrade pixelUpgrade;
    public DittoFusion(PixelUpgrade pixelUpgrade) { this.pixelUpgrade = pixelUpgrade; }

    // Grab the command's alias.
    private static String alias = null;
    private void getCommandAlias()
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + DittoFusionConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
            PixelUpgrade.log.info("§4DittoFusion // critical: §cConfig variable \"commandAlias\" could not be found!");
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            boolean presenceCheck = true;
            Boolean passOnShinyStatus = null, useBritishSpelling = null;
            Integer stat0to5, stat6to10, stat11to15, stat16to20, stat21to25, stat26to30, stat31plus;
            Integer regularCap, shinyCap, previouslyUpgradedMultiplier, pointMultiplierForCost, addFlatFee;

            stat0to5 = getConfigInt("stat0to5");
            stat6to10 = getConfigInt("stat6to10");
            stat11to15 = getConfigInt("stat11to15");
            stat16to20 = getConfigInt("stat16to20");
            stat21to25 = getConfigInt("stat21to25");
            stat26to30 = getConfigInt("stat26to30");
            stat31plus = getConfigInt("stat31plus");
            regularCap = getConfigInt("regularCap");
            shinyCap = getConfigInt("shinyCap");
            previouslyUpgradedMultiplier = getConfigInt("previouslyUpgradedMultiplier");
            pointMultiplierForCost = getConfigInt("pointMultiplierForCost");
            addFlatFee = getConfigInt("addFlatFee");

            if (!DittoFusionConfig.getInstance().getConfig().getNode("passOnShinyStatus").isVirtual())
                passOnShinyStatus = DittoFusionConfig.getInstance().getConfig().getNode("passOnShinyStatus").getBoolean();
            else
                PixelUpgrade.log.info("§4DittoFusion // critical: §cCould not parse config variable \"passOnShinyStatus\"!");

            // Grab the useBritishSpelling value from the main config.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").isVirtual())
                useBritishSpelling = PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").getBoolean();

            // Set up the command's preferred alias.
            getCommandAlias();

            if (passOnShinyStatus == null || regularCap == null || shinyCap == null || stat0to5 == null || stat6to10 == null)
                presenceCheck = false;
            else if (stat11to15 == null || stat16to20 == null || stat21to25 == null || stat26to30 == null || stat31plus == null)
                presenceCheck = false;
            else if (previouslyUpgradedMultiplier == null || pointMultiplierForCost == null || addFlatFee == null)
                presenceCheck = false;

            if (!presenceCheck || alias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4DittoFusion // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (useBritishSpelling == null)
            {
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                printToLog(0, "Couldn't get value of \"useBritishSpelling\" from the main config.");
                printToLog(0, "Please check (or wipe and /pureload) your PixelUpgrade.conf file.");
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                int slot1 = 0, slot2 = 0;
                boolean commandConfirmed = false, canContinue = true;

                if (!args.<String>getOne("target slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo slots were provided. Please provide two valid slots."));
                    checkAndAddFooter(player);

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("target slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Target slot was a valid slot number. Let's move on!");
                        slot1 = Integer.parseInt(args.<String>getOne("target slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot for target Pokémon. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid value on target slot. Valid values are 1-6."));
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                }

                if (!args.<String>getOne("sacrifice slot").isPresent() && canContinue)
                {
                    printToLog(1, "No sacrifice Pokémon slot provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo sacrifice provided. Please provide two valid slots."));
                    checkAndAddFooter(player);

                    canContinue = false;
                }
                else if (canContinue)
                {
                    String slotString = args.<String>getOne("sacrifice slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Valid slot found on argument 2. Checking against arg1...");
                        slot2 = Integer.parseInt(args.<String>getOne("sacrifice slot").get());

                        if (slot2 == slot1)
                        {
                            printToLog(1, "Caught " + src.getName() + " attempting to fuse a Pokémon with itself. Exit, before the universe collapses.");
                            src.sendMessage(Text.of("§4Error: §cYou can't fuse a Pokémon with itself."));
                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(1, "Invalid slot for sacrifice Pokémon. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid value on sacrifice slot. Valid values are 1-6."));
                        checkAndAddFooter(player);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(2, "No error encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt1 = storageCompleted.partyPokemon[slot1 - 1];
                        NBTTagCompound nbt2 = storageCompleted.partyPokemon[slot2 - 1];

                        if (nbt1 == null && nbt2 != null)
                        {
                            printToLog(1, "No NBT found for target Pokémon, slot probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThe target Pokémon does not seem to exist."));
                        }
                        else if (nbt1 != null && nbt2 == null)
                        {
                            printToLog(1, "No NBT found for sacrifice Pokémon, slot probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThe sacrifice Pokémon does not seem to exist."));
                        }
                        else if (nbt1 == null)
                        {
                            printToLog(1, "No NBT found for target not sacrifice, slots probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cBoth the target and sacrifice do not seem to exist."));
                        }
                        else
                        {
                            Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                if (!nbt1.getString("Name").equals("Ditto") && nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(1, "Target was not a Ditto. Abort, abort!");
                                    src.sendMessage(Text.of("§4Error: §cYour target Pokémon is not a Ditto."));
                                }
                                else if (nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(1, "Sacrifice was not a Ditto. Abort, abort!");
                                    src.sendMessage(Text.of("§4Error: §cSorry, but the sacrifice needs to be a Ditto."));
                                }
                                else if (!nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(1, "No Dittos in provided slots; Let's not create some unholy abomination. Exit.");
                                    src.sendMessage(Text.of("§4Error: §cThis command only works on Dittos."));
                                }
                                else
                                {
                                    EntityPixelmon targetPokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt1, (World) player.getWorld());
                                    EntityPixelmon sacrificePokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt2, (World) player.getWorld());
                                    int targetFuseCount = targetPokemon.getEntityData().getInteger("fuseCount");
                                    int sacrificeFuseCount = sacrificePokemon.getEntityData().getInteger("fuseCount");

                                    if (targetFuseCount >= shinyCap && nbt1.getInteger(NbtKeys.IS_SHINY) == 1)
                                    {
                                        printToLog(1, "Hit the shiny cap on target Ditto. Exit.");

                                        src.sendMessage(Text.of("§4Error: §cYour target shiny Ditto cannot grow any further."));
                                        src.sendMessage(Text.of("§6Tip: §eYou could still sacrifice §othis§r§e Ditto... You monster."));
                                    }
                                    else if (targetFuseCount >= regularCap && nbt1.getInteger(NbtKeys.IS_SHINY) != 1)
                                    {
                                        printToLog(1, "Hit the non-shiny cap on target Ditto. Exit.");

                                        src.sendMessage(Text.of("§4Error: §cYour target Ditto cannot grow any further."));
                                        src.sendMessage(Text.of("§6Tip: §eYou could still sacrifice §othis§r§e Ditto... You monster."));
                                    }
                                    else
                                    {
                                        printToLog(2, "Passed the majority of checks, moving on to actual command logic.");

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
                                            printToLog(1, "Sacrifice was too weak to add any stats, apparently. Wow. Exit.");
                                            src.sendMessage(Text.of("§4Error: §cYour sacrificial Ditto is too weak to make a difference."));
                                        }
                                        else if (commandConfirmed)
                                        {
                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.of(EventContext.empty(), pixelUpgrade.getPluginContainer()));
                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                                src.sendMessage(Text.of("§eThe §6Ditto §ein slot §6" + slot2 +
                                                    "§e was eaten, taking §6" + costToConfirm + "§e coins with it."));
                                                src.sendMessage(Text.of(""));

                                                if (HPPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded HP!"));
                                                    src.sendMessage(Text.of("§7" + targetHP + " §f-> §a" + (targetHP + HPPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_HP, nbt1.getInteger(NbtKeys.IV_HP) + HPPlusNum);
                                                }
                                                if (ATKPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded Attack!"));
                                                    src.sendMessage(Text.of("§7" + targetATK + " §f-> §a" + (targetATK + ATKPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_ATTACK, nbt1.getInteger(NbtKeys.IV_ATTACK) + ATKPlusNum);
                                                }
                                                if (DEFPlusNum != 0)
                                                {
                                                    if (useBritishSpelling)
                                                        src.sendMessage(Text.of("§bUpgraded Defence!"));
                                                    else
                                                        src.sendMessage(Text.of("§bUpgraded Defense!"));

                                                    src.sendMessage(Text.of("§7" + targetDEF + " §f-> §a" + (targetDEF + DEFPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_DEFENCE, nbt1.getInteger(NbtKeys.IV_DEFENCE) + DEFPlusNum);
                                                }
                                                if (SPATKPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded Special Attack!"));
                                                    src.sendMessage(Text.of("§7" + targetSPATK + " §f-> §a" + (targetSPATK + SPATKPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SP_ATT, nbt1.getInteger(NbtKeys.IV_SP_ATT) + SPATKPlusNum);
                                                }
                                                if (SPDEFPlusNum != 0)
                                                {
                                                    if (useBritishSpelling)
                                                        src.sendMessage(Text.of("§bUpgraded Special Defence!"));
                                                    else
                                                        src.sendMessage(Text.of("§bUpgraded Special Defense!"));

                                                    src.sendMessage(Text.of("§7" + targetSPDEF + " §f-> §a" + (targetSPDEF + SPDEFPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SP_DEF, nbt1.getInteger(NbtKeys.IV_SP_DEF) + SPDEFPlusNum);
                                                }
                                                if (SPDPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded Speed!"));
                                                    src.sendMessage(Text.of("§7" + targetSPD + " §f-> §a" + (targetSPD + SPDPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SPEED, nbt1.getInteger(NbtKeys.IV_SPEED) + SPDPlusNum);
                                                }

                                                if (sacrificeFuseCount > 0)
                                                {
                                                    src.sendMessage(Text.of(""));
                                                    src.sendMessage(Text.of("§dSacrifice had prior upgrades. You paid an extra §5" + extraCost + "§d coins."));
                                                }

                                                if (nbt2.getInteger(NbtKeys.IS_SHINY) == 1 && passOnShinyStatus)
                                                {
                                                    printToLog(2, "Passing on shinyness is enabled, and sacrifice is shiny. Go go go!");

                                                    // Not sure which one I need, so I set both. Doesn't seem to matter much.
                                                    nbt1.setInteger(NbtKeys.IS_SHINY, 1);
                                                    nbt1.setInteger(NbtKeys.SHINY, 1);

                                                    // Force the client to update.
                                                    storageCompleted.sendUpdatedList();
                                                }

                                                player.sendMessage(Text.of("§7-----------------------------------------------------"));

                                                targetPokemon.getEntityData().setInteger("fuseCount", targetFuseCount + 1);
                                                storageCompleted.changePokemonAndAssignID(slot2 - 1, null);
                                                //storageCompleted.update(targetPokemon, EnumUpdateType.Status);

                                                printToLog(1, "Transaction successful. Took: " + costToConfirm +
                                                        " and a Ditto. Current cash: " + uniqueAccount.getBalance(economyService.getDefaultCurrency()));
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                                printToLog(1, "Not enough coins! Cost: §3" + costToConfirm + "§b, lacking: §3" + balanceNeeded);

                                                src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
                                            }
                                        }
                                        else
                                        {
                                            printToLog(1, "Got cost but no confirmation; end of the line. Exit.");

                                            player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                            if (nbt2.getInteger(NbtKeys.IS_SHINY) == 1 && passOnShinyStatus)
                                                src.sendMessage(Text.of("§eThe Ditto in slot §6" + slot1 + "§e will be upgraded. Transferring shiny status!"));
                                            else
                                                src.sendMessage(Text.of("§eYou are about to upgrade the Ditto in slot §6" + slot1 + "§e."));

                                            src.sendMessage(Text.of(""));

                                            if (HPPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bHP will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetHP + " §f-> §a" + (targetHP + HPPlusNum)));
                                            }
                                            if (ATKPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bAttack will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetATK + " §f-> §a" + (targetATK + ATKPlusNum)));
                                            }
                                            if (DEFPlusNum != 0)
                                            {
                                                if (useBritishSpelling)
                                                    src.sendMessage(Text.of("§bDefence will be upgraded."));
                                                else
                                                    src.sendMessage(Text.of("§bDefense will be upgraded."));

                                                src.sendMessage(Text.of("§7" + targetDEF + " §f-> §a" + (targetDEF + DEFPlusNum)));
                                            }
                                            if (SPATKPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bSpecial Attack will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetSPATK + " §f-> §a" + (targetSPATK + SPATKPlusNum)));
                                            }
                                            if (SPDEFPlusNum != 0)
                                            {
                                                if (useBritishSpelling)
                                                    src.sendMessage(Text.of("§bSpecial Defence will be upgraded."));
                                                else
                                                    src.sendMessage(Text.of("§bSpecial Defense will be upgraded."));

                                                src.sendMessage(Text.of("§7" + targetSPDEF + " §f-> §a" + (targetSPDEF + SPDEFPlusNum)));
                                            }
                                            if (SPDPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bSpeed will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetSPD + " §f-> §a" + (targetSPD + SPDPlusNum)));
                                            }

                                            src.sendMessage(Text.of(""));
                                            if (sacrificeFuseCount > 0)
                                                src.sendMessage(Text.of("§eFusing costs §6" + costToConfirm + "§e coins plus §6"
                                                    + extraCost + "§e coins from prior upgrades."));
                                            else
                                                src.sendMessage(Text.of("§eThis upgrade will cost you §6" + costToConfirm + "§e coins."));
                                            src.sendMessage(Text.of("§aReady? Use: §2" + alias + " " + slot1 + " " + slot2 + " -c"));

                                            src.sendMessage(Text.of("§4Warning: §cThe Ditto in slot §4" + slot2 + "§c will be §ldeleted§r§c!"));
                                            player.sendMessage(Text.of("§7-----------------------------------------------------"));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                printToLog(0, "§4" + src.getName() + "§c does not have an economy account, Exit. May be a bug?");
                            }
                        }
                    }
                }
            }
        }
        else
            PixelUpgrade.log.info("§cThis command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4DittoFusion // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§3DittoFusion // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2DittoFusion // debug: §a" + inputString);
        }
    }
    private Integer getConfigInt(String node)
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode(node).isVirtual())
            return DittoFusionConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4DittoFusion // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private void checkAndAddFooter(Player player)
    {
        player.sendMessage(Text.of("§4Usage: §c" + alias + " <target slot> <sacrifice slot> {-c to confirm}"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're ready to spend money!"));
        player.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
