package rs.expand.pixelupgrade.commands;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
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

import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.GetStatsConfig;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class GetStats implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            Boolean showTeamWhenSlotEmpty, enableCheckEggIntegration;
            Integer debugVerbosityMode, commandCost;

            debugVerbosityMode = checkConfigInt("debugVerbosityMode", false);
            showTeamWhenSlotEmpty = checkConfigBool("showTeamWhenSlotEmpty");
            enableCheckEggIntegration = checkConfigBool("enableCheckEggIntegration");
            commandCost = checkConfigInt("commandCost", false);

            if (enableCheckEggIntegration == null || showTeamWhenSlotEmpty == null || debugVerbosityMode == null || commandCost == null)
            {
                printToLog(0, "Error parsing config! Make sure everything is valid, or regenerate it.");
                src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid config for command! Please report this to staff."));
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Integer slot = 0;
                String targetString = null, slotString;
                Boolean targetAcquired = false, commandConfirmed = false, canContinue = false, hasOtherPerm = false;
                Player player = (Player) src, target = player;

                if (src.hasPermission("pixelupgrade.command.getstats.other"))
                    hasOtherPerm = true;

                if (args.<String>getOne("target or slot").isPresent())
                {
                    // Check whether we have a confirmation flag.
                    if (!args.<String>getOne("target or slot").get().equalsIgnoreCase("-c"))
                    {
                        printToLog(3, "There's something in the first argument slot!");
                        targetString = args.<String>getOne("target or slot").get();

                        if (targetString.matches("^[1-6]"))
                        {
                            printToLog(3, "Found a slot in argument 1. Continuing to confirmation checks.");
                            slot = Integer.parseInt(targetString);
                            canContinue = true;
                        }
                        else if (hasOtherPerm)
                        {
                            if (Sponge.getServer().getPlayer(targetString).isPresent())
                            {
                                if (!player.getName().equalsIgnoreCase(targetString))
                                {
                                    target = Sponge.getServer().getPlayer(targetString).get();
                                    printToLog(3, "Found a valid online target! Printed for your convenience: " + target.getName());
                                    targetAcquired = true;
                                }
                                else
                                    printToLog(3, "Played entered their own name as target.");

                                canContinue = true;
                            }
                            else if (Pattern.matches("[a-zA-Z]+", targetString)) // Making an assumption; input is non-numeric so probably not a slot.
                            {
                                printToLog(2, "First argument was invalid. Input not numeric, assuming misspelled name.");

                                checkAndAddHeader(commandCost, player);
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cCould not find the given target. Check your spelling."));
                                printCorrectPerm(commandCost, player);
                                checkAndAddFooter(commandCost, player);
                            }
                            else  // Throw a "safe" error that works for both missing slots and targets. Might not be as clean, which is why we check patterns above.
                            {
                                printToLog(2, "First argument was invalid, and input has numbers. Throwing generic error.");
                                throwArg1Error(commandCost, true, player);
                            }
                        }
                        else
                        {
                            printToLog(2, "Invalid slot provided, and player has no .other perm. Abort.");
                            throwArg1Error(commandCost, false, player);
                        }
                    }
                }
                else
                {
                    printToLog(2, "No arguments found, aborting.");

                    checkAndAddHeader(commandCost, player);
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo arguments found. Please provide at least a slot."));
                    printCorrectPerm(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }

                if (args.<String>getOne("slot").isPresent() && canContinue)
                {
                    String confirmString = args.<String>getOne("slot").get();
                    if (confirmString.equalsIgnoreCase("-c"))
                    {
                        printToLog(3, "Got a confirmation flag on argument 2!");
                        commandConfirmed = true;
                    }
                    else if (hasOtherPerm)
                    {
                        printToLog(3, "There's something in the second argument slot!");
                        slotString = args.<String>getOne("slot").get();

                        if (slotString.matches("^[1-6]"))
                        {
                            printToLog(3, "Found a slot in argument 2.");
                            slot = Integer.parseInt(slotString);
                        }
                        else
                        {
                            printToLog(2, "Argument is not a slot or a confirmation flag. Abort.");

                            checkAndAddHeader(commandCost, player);
                            if (commandCost > 0)
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot or flag on second argument. See below."));
                            else
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot provided. See below."));
                            printCorrectPerm(commandCost, player);
                            checkAndAddFooter(commandCost, player);

                            canContinue = false;
                        }
                    }
                }

                if (args.<String>getOne("confirmation").isPresent() && hasOtherPerm && canContinue)
                {
                    String confirmString = args.<String>getOne("confirmation").get();
                    if (confirmString.equalsIgnoreCase("-c"))
                    {
                        printToLog(3, "Got a confirmation flag on argument 3!");
                        commandConfirmed = true;
                    }
                }

                if (slot == 0 && canContinue)
                {
                    printToLog(2, "Failed final check, no slot was found. Abort.");

                    checkAndAddHeader(commandCost, player);
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cCould not find a valid slot. See below."));
                    printCorrectPerm(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");

                    Optional<PlayerStorage> storage;
                    if (targetAcquired)
                        storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
                    else
                        storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        printToLog(3, "Found a Pixelmon storage on the player. Moving along.");

                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (targetAcquired && showTeamWhenSlotEmpty && nbt == null)
                        {
                            printToLog(3, "Slot provided on target is empty, dumping team to chat (config).");

                            Integer slotTicker = 0;
                            player.sendMessage(Text.of("\u00A75Info: \u00A7dThat slot is empty, showing whole team!"));

                            for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
                            {
                                if (slotTicker > 5)
                                    break;

                                if (loopValue == null)
                                    player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A72Empty\u00A7a."));
                                else if (loopValue.getBoolean("isEgg"))
                                    player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A7aAn \u00A72egg\u00A7a."));
                                else
                                {
                                    if (!loopValue.getString("Nickname").equals(""))
                                        player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A7aA level " + loopValue.getInteger("Level") + "\u00A72 " + loopValue.getString("Name") + "\u00A7a, also known as \u00A72" + loopValue.getString("Nickname") + "\u00A7a."));
                                    else
                                        player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A7aA level " + loopValue.getInteger("Level") + "\u00A72 " + loopValue.getString("Name") + "\u00A7a."));
                                }

                                slotTicker++;
                            }

                            player.sendMessage(Text.of("\u00A75Info: \u00A7dWant to know more? Use: \u00A75/gs (player) <slot>"));
                        }
                        else if (nbt == null && targetAcquired)
                        {
                            printToLog(2, "No Pok\u00E9mon was found in the provided slot on the target. Abort.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target has no Pok\u00E9mon in that slot!"));
                        }
                        else if (nbt == null)
                        {
                            printToLog(2, "No Pok\u00E9mon was found in the provided slot. Abort.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThere's no Pok\u00E9mon in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg") && enableCheckEggIntegration)
                        {
                            printToLog(2, "Found an egg, recommended /checkegg as per config. Abort.");
                            player.sendMessage(Text.of("\u00A74Error: \u00A7cI cannot see into an egg. Check out \u00A74/checkegg\u00A7c."));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Found an egg, printed an error instead of recommending /checkegg. (config)");
                            player.sendMessage(Text.of("\u00A74Error: \u00A7cSorry, but I cannot reveal what is inside an egg."));
                        }
                        else if (commandCost > 0)
                        {
                            BigDecimal costToConfirm = new BigDecimal(commandCost);

                            if (commandConfirmed)
                            {
                                Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                if (optionalAccount.isPresent())
                                {
                                    UniqueAccount uniqueAccount = optionalAccount.get();
                                    TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());

                                    if (transactionResult.getResult() == ResultType.SUCCESS)
                                    {
                                        printToLog(1, "Checked Pok\u00E9mon in slot " + slot + ", and took " + costToConfirm + " coins.");
                                        checkAndShow(nbt, targetAcquired, player, target);
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
                                    printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));
                                }
                            }
                            else
                            {
                                printToLog(2, "Got cost but no confirmation; end of the line.");

                                if (targetAcquired)
                                {
                                    slot = Integer.parseInt(args.<String>getOne("slot").get());
                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking this Pok\u00E9mon's status costs \u00A76" + costToConfirm + "\u00A7e coins."));
                                    src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a/gs " + targetString + " " + slot + " -c"));
                                }
                                else
                                {
                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking a Pok\u00E9mon's status costs \u00A76" + costToConfirm + "\u00A7e coins."));
                                    src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a/gs " + slot + " -c"));
                                }
                            }
                        }
                        else
                        {
                            printToLog(2, "Checked Pok\u00E9mon in slot " + slot + ". Config price is 0, taking nothing.");
                            checkAndShow(nbt, targetAcquired, player, target);
                        }
                    }
                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

    private void checkAndAddHeader(Integer cost, Player player)
    {
        if (cost > 0)
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void checkAndAddFooter(Integer cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + cost + "\u00A7e coins."));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printCorrectPerm(Integer cost, Player player)
    {
        if (cost != 0)
        {
            if (player.hasPermission("pixelupgrade.command.getstats.other"))
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs [optional target] <slot, 1-6> {-c to confirm}"));
            else
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs <slot, 1-6> {-c to confirm} \u00A77(no perms for target)"));
        }
        else
        {
            if (player.hasPermission("pixelupgrade.command.getstats.other"))
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs [optional target] <slot, 1-6>"));
            else
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs <slot, 1-6> \u00A77(no perms for target)"));
        }
    }

    private void throwArg1Error(Integer cost, Boolean hasOtherPerm, Player player)
    {
        checkAndAddHeader(cost, player);
        if (hasOtherPerm)
            player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid target or slot on first argument. See below."));
        else if (cost > 0)
            player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot provided on first argument. See below."));
        else
            player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot provided. See below."));
        printCorrectPerm(cost, player);
        checkAndAddFooter(cost, player);
    }

    private void printToLog(Integer debugNum, String inputString)
    {
        Integer debugVerbosityMode = checkConfigInt("debugVerbosityMode", true);

        if (debugVerbosityMode == null)
            debugVerbosityMode = 4;

        if (debugNum <= debugVerbosityMode)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74GetStats // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76GetStats // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73GetStats // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72GetStats // debug: \u00A7a" + inputString);
        }
    }

    private Boolean checkConfigBool(String node)
    {
        if (!GetStatsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return GetStatsConfig.getInstance().getConfig().getNode(node).getBoolean();

        else
        {
            PixelUpgrade.log.info("\u00A74GetStats // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Integer checkConfigInt(String node, Boolean noMessageMode)
    {
        if (!GetStatsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return GetStatsConfig.getInstance().getConfig().getNode(node).getInt();
        else if (noMessageMode)
            return null;
        else
        {
            PixelUpgrade.log.info("\u00A74GetStats // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private void checkAndShow(NBTTagCompound nbt, Boolean targetAcquired, Player player, Player target)
    {
        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

        Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
        Integer IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
        Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
        Integer IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
        Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
        Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
        Integer totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;
        Integer percentIVs = totalIVs * 100 / 186;

        Integer EVHP = nbt.getInteger(NbtKeys.EV_HP);
        Integer EVATK = nbt.getInteger(NbtKeys.EV_ATTACK);
        Integer EVDEF = nbt.getInteger(NbtKeys.EV_DEFENCE);
        Integer EVSPATK = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        Integer EVSPDEF = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        Integer EVSPD = nbt.getInteger(NbtKeys.EV_SPEED);
        Integer totalEVs = EVHP + EVATK + EVDEF + EVSPATK + EVSPDEF + EVSPD;
        Integer percentEVs = totalEVs * 100 / 510;

        Integer natureNum = nbt.getInteger(NbtKeys.NATURE);
        Integer growthNum = nbt.getInteger(NbtKeys.GROWTH);
        Integer genderNum = nbt.getInteger(NbtKeys.GENDER);
        Integer fuseCount = pokemon.getEntityData().getInteger("fuseCount");
        Integer upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
        String natureName, plusVal, minusVal, growthName, genderName;
        String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
        String evs1, evs2, evs3, evs4, evs5, evs6;
        String extraInfo1, extraInfo2;

        Boolean isShiny, isLegendary, isBaby = false;
        isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
        if (nbt.getString("Name").equals("Riolu") || nbt.getString("Name").equals("Mime Jr.") || nbt.getString("Name").equals("Happiny"))
            isBaby = true;
        isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

        if (IVHP >= 31)
            ivs1 = String.valueOf("\u00A7o" + IVHP + " \u00A72HP \u00A7r\u00A7e|\u00A7a ");
        else
            ivs1 = String.valueOf(IVHP + " \u00A72HP \u00A7e|\u00A7a ");

        if (IVATK >= 31)
            ivs2 = String.valueOf("\u00A7o" + IVATK + " \u00A72ATK \u00A7r\u00A7e|\u00A7a ");
        else
            ivs2 = String.valueOf(IVATK + " \u00A72ATK \u00A7e|\u00A7a ");

        if (IVDEF >= 31)
            ivs3 = String.valueOf("\u00A7o" + IVDEF + " \u00A72DEF \u00A7r\u00A7e|\u00A7a ");
        else
            ivs3 = String.valueOf(IVDEF + " \u00A72DEF \u00A7e|\u00A7a ");

        if (IVSPATK == 31)
            ivs4 = String.valueOf("\u00A7o" + IVSPATK + " \u00A72Sp. ATK \u00A7r\u00A7e|\u00A7a ");
        else
            ivs4 = String.valueOf(IVSPATK + " \u00A72Sp. ATK \u00A7e|\u00A7a ");

        if (IVSPDEF == 31)
            ivs5 = String.valueOf("\u00A7o" + IVSPDEF + " \u00A72Sp. DEF \u00A7r\u00A7e|\u00A7a ");
        else
            ivs5 = String.valueOf(IVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a ");

        if (IVSPD == 31)
            ivs6 = String.valueOf("\u00A7o" + IVSPD + " \u00A72SPD");
        else
            ivs6 = String.valueOf(IVSPD + " \u00A72SPD");

        if (EVHP >= 252 || EVHP > 255)
            evs1 = String.valueOf("\u00A7o" + EVHP + " \u00A72HP \u00A7r\u00A7e|\u00A7a ");
        else if (EVHP > 252 && EVHP < 256)
            evs1 = String.valueOf("\u00A7c" + EVHP + " \u00A74HP \u00A7e|\u00A7a ");
        else
            evs1 = String.valueOf(EVHP + " \u00A72HP \u00A7e|\u00A7a ");

        if (EVATK >= 252 || EVATK > 255)
            evs2 = String.valueOf("\u00A7o" + EVATK + " \u00A72ATK \u00A7r\u00A7e|\u00A7a ");
        else if (EVATK > 252 && EVATK < 256)
            evs2 = String.valueOf("\u00A7c" + EVATK + " \u00A74ATK \u00A7e|\u00A7a ");
        else
            evs2 = String.valueOf(EVATK + " \u00A72ATK \u00A7e|\u00A7a ");

        if (EVDEF >= 252 || EVDEF > 255)
            evs3 = String.valueOf("\u00A7o" + EVDEF + " \u00A72DEF \u00A7r\u00A7e|\u00A7a ");
        else if (EVDEF > 252 && EVDEF < 256)
            evs3 = String.valueOf("\u00A7c" + EVDEF + " \u00A74DEF \u00A7e|\u00A7a ");
        else
            evs3 = String.valueOf(EVDEF + " \u00A72DEF \u00A7e|\u00A7a ");

        if (EVSPATK >= 252 || EVSPATK > 255)
            evs4 = String.valueOf("\u00A7o" + EVSPATK + " \u00A72Sp. ATK \u00A7r\u00A7e|\u00A7a ");
        else if (EVSPATK > 252 && EVSPATK < 256)
            evs4 = String.valueOf("\u00A7c" + EVSPATK + " \u00A74Sp. ATK \u00A7e|\u00A7a ");
        else
            evs4 = String.valueOf(EVSPATK + " \u00A72Sp. ATK \u00A7e|\u00A7a ");

        if (EVSPDEF >= 252 || EVSPDEF > 255)
            evs5 = String.valueOf("\u00A7o" + EVSPDEF + " \u00A72Sp. DEF \u00A7r\u00A7e|\u00A7a ");
        else if (EVSPDEF > 252 && EVSPDEF < 256)
            evs5 = String.valueOf("\u00A7c" + EVSPDEF + " \u00A74Sp. DEF \u00A7e|\u00A7a ");
        else
            evs5 = String.valueOf(EVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a ");

        if (EVSPD >= 252 || EVSPD > 255)
            evs6 = String.valueOf("\u00A7o" + EVSPD + " \u00A72SPD");
        else if (EVSPD > 252 && EVSPD < 256)
            evs6 = String.valueOf("\u00A7m" + EVSPD + " \u00A72SPD");
        else
            evs6 = String.valueOf(EVSPD + " \u00A72SPD");

        switch (natureNum)
        {
            case 0:
                natureName = "Hardy";
                plusVal = "+NONE";
                minusVal = "-NONE";
                break;
            case 1:
                natureName = "Serious";
                plusVal = "+NONE";
                minusVal = "-NONE";
                break;
            case 2:
                natureName = "Docile";
                plusVal = "+NONE";
                minusVal = "-NONE";
                break;
            case 3:
                natureName = "Bashful";
                plusVal = "+NONE";
                minusVal = "-NONE";
                break;
            case 4:
                natureName = "Quirky";
                plusVal = "+NONE";
                minusVal = "-NONE";
                break;
            case 5:
                natureName = "Lonely";
                plusVal = "+ATK";
                minusVal = "-DEF";
                break;
            case 6:
                natureName = "Brave";
                plusVal = "+ATK";
                minusVal = "-SPD";
                break;
            case 7:
                natureName = "Adamant";
                plusVal = "+ATK";
                minusVal = "-SP. ATK";
                break;
            case 8:
                natureName = "Naughty";
                plusVal = "+ATK";
                minusVal = "-SP. DEF";
                break;
            case 9:
                natureName = "Bold";
                plusVal = "+DEF";
                minusVal = "-ATK";
                break;
            case 10:
                natureName = "Relaxed";
                plusVal = "+DEF";
                minusVal = "-SPD";
                break;
            case 11:
                natureName = "Impish";
                plusVal = "+DEF";
                minusVal = "-SP. ATK";
                break;
            case 12:
                natureName = "Lax";
                plusVal = "+DEF";
                minusVal = "-SP. DEF";
                break;
            case 13:
                natureName = "Timid";
                plusVal = "+SPD";
                minusVal = "-ATK";
                break;
            case 14:
                natureName = "Hasty";
                plusVal = "+SPD";
                minusVal = "-DEF";
                break;
            case 15:
                natureName = "Jolly";
                plusVal = "+SPD";
                minusVal = "-SP. ATK";
                break;
            case 16:
                natureName = "Naive";
                plusVal = "+SPD";
                minusVal = "-SP. DEF";
                break;
            case 17:
                natureName = "Modest";
                plusVal = "+SP. ATK";
                minusVal = "-ATK";
                break;
            case 18:
                natureName = "Mild";
                plusVal = "+SP. ATK";
                minusVal = "-DEF";
                break;
            case 19:
                natureName = "Quiet";
                plusVal = "+SP. ATK";
                minusVal = "-SPD";
                break;
            case 20:
                natureName = "Rash";
                plusVal = "+SP. ATK";
                minusVal = "-SP. DEF";
                break;
            case 21:
                natureName = "Calm";
                plusVal = "+SP. DEF";
                minusVal = "-ATK";
                break;
            case 22:
                natureName = "Gentle";
                plusVal = "+SP. DEF";
                minusVal = "-DEF";
                break;
            case 23:
                natureName = "Sassy";
                plusVal = "+SP. DEF";
                minusVal = "-SPD";
                break;
            case 24:
                natureName = "Careful";
                plusVal = "+SP. DEF";
                minusVal = "-SP. ATK";
                break;
            default:
                natureName = "Not found? Please report this.";
                plusVal = "+N/A";
                minusVal = "-N/A";
                break;
        }

        switch (growthNum)
        {
            case 0:
                growthName = "Pygmy";
                break;
            case 1:
                growthName = "Runt";
                break;
            case 2:
                growthName = "Small";
                break;
            case 3:
                growthName = "Ordinary";
                break;
            case 4:
                growthName = "Huge";
                break;
            case 5:
                growthName = "Giant";
                break;
            case 6:
                growthName = "Enormous";
                break;
            case 7:
                growthName = "\u00A7cGinormous";
                break;
            case 8:
                growthName = "\u00A7aMicroscopic";
                break;
            default:
                growthName = "Not found? Please report this.";
                break;
        }

        switch (genderNum)
        {
            case 0:
                genderName = "\u2642";
                break;
            case 1:
                genderName = "\u2640";
                break;
            case 2:
                genderName = "\u26A5";
                break;
            default:
                genderName = "Not found? Please report this.";
                break;
        }

        if (targetAcquired)
        {
            if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.getName() + "\u00A76's \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname")));
            else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.getName() + "\u00A76's \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname") + "\u00A7e (\u00A7fshiny\u00A7e)"));
            else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.getName() + "\u00A76's \u00A7c" + nbt.getString("Name") + "\u00A7e (\u00A7fshiny\u00A7e)"));
            else
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.getName() + "\u00A76's \u00A7c" + nbt.getString("Name")));
        }
        else
        {
            if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname")));
            else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname") + "\u00A7e (\u00A7fshiny\u00A7e)"));
            else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name") + "\u00A7e (\u00A7fshiny\u00A7e)"));
            else
                player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name")));
        }

        extraInfo1 = String.valueOf("\u00A7eGender: \u00A7f" + genderName + "\u00A7f | \u00A7eSize: \u00A7f" + growthName + "\u00A7f | ");
        extraInfo2 = String.valueOf("\u00A7eNature: \u00A7f" + natureName + "\u00A7e (\u00A7a" + plusVal + "\u00A7e/\u00A7c" + minusVal + "\u00A7e)");

        player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e)"));
        player.sendMessage(Text.of("\u00A7eIVs: \u00A7a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));
        player.sendMessage(Text.of("\u00A7eTotal EVs: \u00A7a" + totalEVs + "\u00A7e/\u00A7a510\u00A7e (\u00A7a" + percentEVs + "%\u00A7e)"));
        player.sendMessage(Text.of("\u00A7eEVs: \u00A7a" + evs1 + "" + evs2 + "" + evs3 + "" + evs4 + "" + evs5 + "" + evs6));
        player.sendMessage(Text.of(extraInfo1 + extraInfo2));

        if (nbt.getString("Name").equals("Ditto"))
        {
            if (isShiny)
            {
                if (fuseCount != 0 && fuseCount < 10)
                    player.sendMessage(Text.of("\u00A76This shiny Ditto has been fused \u00A7c" + fuseCount + "\u00A76/\u00A7c10 \u00A76times."));
                else if (fuseCount == 0)
                    player.sendMessage(Text.of("\u00A76This shiny Ditto can be fused \u00A7c10 \u00A76more times!"));
                else
                    player.sendMessage(Text.of("\u00A76This shiny Ditto cannot be fused any further!"));
            }
            else
            {
                if (fuseCount != 0 && fuseCount < 5)
                    player.sendMessage(Text.of("\u00A76This Ditto has been fused \u00A7c" + fuseCount + "\u00A76/\u00A7c5 \u00A76times."));
                else if (fuseCount == 0)
                    player.sendMessage(Text.of("\u00A76This Ditto can be fused \u00A7c5 \u00A76more times!"));
                else
                    player.sendMessage(Text.of("\u00A76This Ditto cannot be fused any further!"));
            }
        }
        else if (isShiny && isLegendary)
        {
            if (upgradeCount != 0 && upgradeCount < 40)
                player.sendMessage(Text.of("\u00A76This shiny legendary has been upgraded \u00A7c" + upgradeCount + "\u00A76/\u00A7c40 \u00A76times."));
            else if (upgradeCount == 0)
                player.sendMessage(Text.of("\u00A76This shiny legendary can be upgraded \u00A7c40 \u00A76more times!"));
            else
                player.sendMessage(Text.of("\u00A76This shiny legendary has been fully upgraded!"));
        }
        else if (isShiny)
        {
            if (upgradeCount != 0 && upgradeCount < 60)
                player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon has been upgraded \u00A7c" + upgradeCount + "\u00A76/\u00A7c60 \u00A76times."));
            else if (upgradeCount == 0)
                player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon can be upgraded \u00A7c60 \u00A76more times!"));
            else
                player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon has been fully upgraded!"));
        }
        else if (isLegendary)
        {
            if (upgradeCount != 0 && upgradeCount < 20)
                player.sendMessage(Text.of("\u00A76This legendary has been upgraded \u00A7c" + upgradeCount + "\u00A76/\u00A7c20 \u00A76times."));
            else if (upgradeCount == 0)
                player.sendMessage(Text.of("\u00A76This legendary can be upgraded \u00A7c20 \u00A76more times!"));
            else
                player.sendMessage(Text.of("\u00A76This legendary has been fully upgraded!"));
        }
        else if (isBaby)
        {
            if (upgradeCount != 0 && upgradeCount < 25)
                player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon has been upgraded \u00A7c" + upgradeCount + "\u00A76/\u00A7c25 \u00A76times."));
            else if (upgradeCount == 0)
                player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon can be upgraded \u00A7c25 \u00A76more times!"));
            else
                player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon has been fully upgraded!"));
        }
        else
        {
            if (upgradeCount != 0 && upgradeCount < 35)
                player.sendMessage(Text.of("\u00A76This Pok\u00E9mon has been upgraded \u00A7c" + upgradeCount + "\u00A76/\u00A7c35 \u00A76times."));
            else if (upgradeCount == 0)
                player.sendMessage(Text.of("\u00A76This Pok\u00E9mon can be upgraded \u00A7c35 \u00A76more times!"));
            else
                player.sendMessage(Text.of("\u00A76This Pok\u00E9mon has been fully upgraded!"));
        }

        if (targetAcquired)
        {
            if (EVHP < 256 && EVHP > 252 || EVATK < 256 && EVATK > 252 || EVDEF < 256 && EVDEF > 252 || EVSPATK < 256 && EVSPATK > 252 || EVSPDEF < 256 && EVSPDEF > 252 || EVSPD < 256 && EVSPD > 252)
                player.sendMessage(Text.of("\u00A75Warning: \u00A7dEVs above 252 do nothing. Try using \u00A75/fixevs\u00A7d."));
        }
    }
}
