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

import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.CheckEggConfig;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class CheckEgg implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!CheckEggConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = CheckEggConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!CheckEggConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + CheckEggConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            boolean presenceCheck = true;
            Integer commandCost = checkConfigInt("commandCost");
            Integer babyHintPercent = checkConfigInt("babyHintPercent");
            Boolean explicitReveal = checkConfigBool("explicitReveal");
            Boolean recheckIsFree = checkConfigBool("recheckIsFree");

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (recheckIsFree == null || explicitReveal == null || commandCost == null || babyHintPercent == null)
                presenceCheck = false;

            if (!presenceCheck || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pu reload\\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                int slot = 0;
                String targetString = null, slotString;
                boolean targetAcquired = false, commandConfirmed = false, canContinue = false, hasOtherPerm = false;
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
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                    }
                    else
                    {
                        printToLog(3, "Found a Pixelmon storage on the player. Moving along.");

                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null || !nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Could not find an egg in the provided slot, or no Pok\u00E9mon was found. Abort.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cCould not find an egg in the provided slot."));
                        }
                        else
                        {
                            printToLog(3, "Egg found. Let's do this!");

                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            boolean wasEggChecked = pokemon.getEntityData().getBoolean("hadEggChecked");
                            if (!recheckIsFree)
                                wasEggChecked = false;

                            if (commandCost == 0 || wasEggChecked)
                            {
                                src.sendMessage(Text.of("\u00A76There's a healthy \u00A7c" + nbt.getString("Name") + "\u00A76 inside of this egg!"));
                                printEggResults(nbt, pokemon, babyHintPercent, explicitReveal, recheckIsFree, player);

                                // Keep this below the printEggResults call, or your debug message order will look weird.
                                if (commandCost == 0)
                                    printToLog(2, "Checked egg in slot " + slot + ". Config price is 0, taking nothing.");
                                else
                                    printToLog(2, "Checked egg in slot " + slot + ". Detected a recheck, taking nothing (config).");
                            }
                            else
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
                                            src.sendMessage(Text.of("\u00A76There's a healthy \u00A7c" + nbt.getString("Name") + "\u00A76 inside of this egg!"));
                                            printEggResults(nbt, pokemon, babyHintPercent, explicitReveal, recheckIsFree, player);

                                            // Keep this below the printEggResults call, or your debug message order will look weird.
                                            printToLog(1, "Checked egg in slot " + slot + ", and took " + costToConfirm + " coins.");
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
                                        src.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking this egg's status costs \u00A76" + costToConfirm + "\u00A7e coins."));
                                        src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + targetString + " " + slot + " -c"));
                                    }
                                    else
                                    {
                                        src.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking an egg's status costs \u00A76" + costToConfirm + "\u00A7e coins."));
                                        src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + slot + " -c"));
                                    }
                                }
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

    private void checkAndAddHeader(int cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        if (cost != 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + cost + "\u00A7e coins."));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printCorrectPerm(int cost, Player player)
    {
        if (cost != 0)
        {
            if (player.hasPermission("pixelupgrade.command.checkegg.other"))
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " [optional target] <slot, 1-6> {-c to confirm}"));
            else
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot> {-c to confirm} \u00A77(no perms for target)"));
        }
        else
        {
            if (player.hasPermission("pixelupgrade.command.checkegg.other"))
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " [optional target] <slot, 1-6>"));
            else
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot> \u00A77(no perms for target)"));
        }
    }

    private void throwArg1Error(int cost, boolean hasOtherPerm, Player player)
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

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76CheckEgg // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73CheckEgg // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72CheckEgg // debug: \u00A7a" + inputString);
        }
    }

    private Boolean checkConfigBool(String node)
    {
        if (!CheckEggConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckEggConfig.getInstance().getConfig().getNode(node).getBoolean();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Integer checkConfigInt(String node)
    {
        if (!CheckEggConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckEggConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private void printEggResults(NBTTagCompound nbt, EntityPixelmon pokemon, int babyHintPercent, boolean explicitReveal, boolean recheckIsFree, Player player)
    {
        int IVHP = nbt.getInteger(NbtKeys.IV_HP);
        int IVATT = nbt.getInteger(NbtKeys.IV_ATTACK);
        int IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int IVSPATT = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
        int totalIVs = IVHP + IVATT + IVDEF + IVSPATT + IVSPDEF + IVSPD;
        int percentIVs = totalIVs * 100 / 186;

        if (explicitReveal)
        {
            printToLog(3, "Explicit reveal enabled, printing full IVs and shiny status.");

            String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
            boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;

            if (IVHP == 31)
                ivs1 = String.valueOf("\u00A7o" + IVHP + " \u00A72HP \u00A7r\u00A7e|\u00A7a ");
            else
                ivs1 = String.valueOf(IVHP + " \u00A72HP \u00A7e|\u00A7a ");

            if (IVATT == 31)
                ivs2 = String.valueOf("\u00A7o" + IVATT + " \u00A72ATK \u00A7r\u00A7e|\u00A7a ");
            else
                ivs2 = String.valueOf(IVATT + " \u00A72ATK \u00A7e|\u00A7a ");

            if (IVDEF == 31)
                ivs3 = String.valueOf("\u00A7o" + IVDEF + " \u00A72DEF \u00A7r\u00A7e|\u00A7a ");
            else
                ivs3 = String.valueOf(IVDEF + " \u00A72DEF \u00A7e|\u00A7a ");

            if (IVSPATT == 31)
                ivs4 = String.valueOf("\u00A7o" + IVSPATT + " \u00A72Sp. ATK \u00A7r\u00A7e|\u00A7a ");
            else
                ivs4 = String.valueOf(IVSPATT + " \u00A72Sp. ATK \u00A7e|\u00A7a ");

            if (IVSPDEF == 31)
                ivs5 = String.valueOf("\u00A7o" + IVSPDEF + " \u00A72Sp. DEF \u00A7r\u00A7e|\u00A7a ");
            else
                ivs5 = String.valueOf(IVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a ");

            if (IVSPD == 31)
                ivs6 = String.valueOf("\u00A7o" + IVSPD + " \u00A72SPD");
            else
                ivs6 = String.valueOf(IVSPD + " \u00A72SPD");

            player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e)"));
            player.sendMessage(Text.of("\u00A7eIVs: \u00A7a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));
            if (isShiny)
                player.sendMessage(Text.of("\u00A76Congratulations! \u00A7eThis baby is shiny!"));
        }
        else
        {
            printToLog(3, "Explicit reveal disabled, printing vague status.");

            if (percentIVs >= babyHintPercent && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of("\u00A76What's this? \u00A7eThis baby seems to be bursting with energy..."));
            else if (!(percentIVs >= babyHintPercent) && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("\u00A76What's this? \u00A7eThis baby seems to have an odd sheen to it..."));
            else if (percentIVs >= babyHintPercent && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("\u00A76What's this? \u00A7eSomething about this baby seems real special!"));
            else
                player.sendMessage(Text.of("\u00A7eThis baby seems to be fairly ordinary..."));
        }

        if (pokemon.getEntityData().getBoolean("hadEggChecked") && recheckIsFree)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A7aThis egg has been checked before, so this check was free!"));
        }

        pokemon.getEntityData().setBoolean("hadEggChecked", true);
    }
}
