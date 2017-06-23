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
import rs.expand.pixelupgrade.configs.DittoFusionConfig;
import rs.expand.pixelupgrade.configs.CheckStatsConfig;
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;
import rs.expand.pixelupgrade.configs.UpgradeIVsConfig;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class CheckStats implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!CheckStatsConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = CheckStatsConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!CheckStatsConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + CheckStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    // Set up some variables that we'll be using in the stat-checking method.
    private Boolean showEVs = null;
    private Boolean showFixEVsHelper = null;
    private Boolean showUpgradeHelper = null;
    private Boolean showDittoFusionHelper = null;
    private Boolean competitiveMode = null;
    private Integer regularFusionCap, shinyFusionCap, legendaryAndShinyUpgradeCap;
    private Integer legendaryUpgradeCap, regularUpgradeCap, shinyUpgradeCap, babyUpgradeCap;

    //boolean targetAcquired, boolean showEVs, boolean showFixEVsHelper,
    //boolean showUpgradeHelper, boolean showDittoFusionHelper,

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            boolean canContinue = true, presenceCheck = true, fusionPresenceCheck = true;
            boolean upgradePresenceCheck = true;
            
            Boolean showTeamWhenSlotEmpty = checkConfigBool("showTeamWhenSlotEmpty");
            Boolean enableCheckEggIntegration = checkConfigBool("enableCheckEggIntegration");
            showEVs = checkConfigBool("showEVs");
            showFixEVsHelper = checkConfigBool("showFixEVsHelper");
            showUpgradeHelper = checkConfigBool("showUpgradeHelper");
            showDittoFusionHelper = checkConfigBool("showDittoFusionHelper");
            Integer commandCost = checkConfigInt();

            // Load up Ditto Fusion config values. Used for showing fusion limits.
            regularFusionCap = checkFusionConfigInt("regularCap");
            shinyFusionCap = checkFusionConfigInt("shinyCap");

            // Load up UpgradeIVs config values. Used for showing upgrade limits.
            // Sorry for the insanely long variable name. Clarity over style, there.
            legendaryAndShinyUpgradeCap = checkUpgradeConfigInt("legendaryAndShinyCap");
            legendaryUpgradeCap = checkUpgradeConfigInt("legendaryCap");
            regularUpgradeCap = checkUpgradeConfigInt("regularCap");
            shinyUpgradeCap = checkUpgradeConfigInt("shinyCap");
            babyUpgradeCap = checkUpgradeConfigInt("babyCap");

            // Grab the competitiveMode value from the main config.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("competitiveMode").isVirtual())
                competitiveMode = PixelUpgradeMainConfig.getInstance().getConfig().getNode("competitiveMode").getBoolean();

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (enableCheckEggIntegration == null || showTeamWhenSlotEmpty == null || commandCost == null)
                presenceCheck = false;
            else if (showEVs == null || showFixEVsHelper == null || showUpgradeHelper == null || showDittoFusionHelper == null)
                presenceCheck = false;
            if (regularFusionCap == null || shinyFusionCap == null)
                fusionPresenceCheck = false;
            if (legendaryAndShinyUpgradeCap == null || legendaryUpgradeCap == null || regularUpgradeCap == null)
                upgradePresenceCheck = false;
            else if (shinyUpgradeCap == null || babyUpgradeCap == null)
                upgradePresenceCheck = false;

            if (!presenceCheck || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cCheck your config. If need be, wipe and \u00A74/pixelupgrade reload\u00A7c.");
                canContinue = false;
            }
            else if (competitiveMode == null)
            {
                src.sendMessage(Text.of("\u00A74Error: \u00A7cCould not parse main config. Please report to staff."));
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cCouldn't get value of \"competitiveMode\" from the main config.");
                PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cPlease check (or wipe and reload) your PixelUpgrade.conf file.");
            }
            else if (!fusionPresenceCheck || !upgradePresenceCheck)
            { // These errors are shown after the config checker method's errors.
                if (!fusionPresenceCheck && upgradePresenceCheck && showDittoFusionHelper)
                {
                    printToLog(0, "Ditto Fusion integration has been disabled!");
                    printToLog(0, "If need be, remove the file and \u00A74/pixelupgrade reload\u00A7c.");
                    showDittoFusionHelper = false;
                }
                else if (fusionPresenceCheck && showUpgradeHelper)
                {
                    printToLog(0, "Upgrade integration has been disabled!");
                    printToLog(0, "If need be, remove the file and \u00A74/pixelupgrade reload\u00A7c.");
                    showUpgradeHelper = false;
                }
                else if (showDittoFusionHelper && showUpgradeHelper)
                {
                    printToLog(0, "Integration for both commands has been disabled!");
                    printToLog(0, "If need be, remove the file and \u00A76/pixelupgrade reload\u00A7e.");
                    showDittoFusionHelper = false;
                    showUpgradeHelper = false;
                }
            }

            if (canContinue)
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                int slot = 0;
                String targetString = null, slotString;
                boolean targetAcquired = false, commandConfirmed = false, hasOtherPerm = false;
                Player player = (Player) src, target = player;

                // We reset this here since we used it above. It's a bit different from the other commands, but hey.
                // If we get a valid input, we'll set this to "true" again so we can execute the main body of code.
                canContinue = false;

                if (src.hasPermission("pixelupgrade.command.checkstats.other"))
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
                                {
                                    target = Sponge.getServer().getPlayer(targetString).get();
                                    printToLog(3, "Found a valid online target! Printed for your convenience: " + target.getName());
                                    targetAcquired = true;
                                }
                                //    printToLog(3, "Played entered their own name as target."); // MARK

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
                            printToLog(3, "Slot provided on target is empty, printing team to chat (config).");

                            int slotTicker = 0;
                            player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
                            player.sendMessage(Text.of("\u00A7eThat slot is empty, showing the target's whole team!"));
                            player.sendMessage(Text.of(""));

                            for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
                            {
                                if (slotTicker > 5)
                                    break;

                                String start = "\u00A7bSlot " + (slotTicker + 1) + "\u00A7f: ";
                                if (loopValue == null)
                                    player.sendMessage(Text.of(start + "\u00A72Empty\u00A7a."));
                                else if (loopValue.getBoolean("isEgg"))
                                    player.sendMessage(Text.of(start + "\u00A7aAn \u00A72egg\u00A7a."));
                                else
                                {
                                    String name = loopValue.getInteger("Level") + "\u00A72 " + loopValue.getString("Name");

                                    if (!loopValue.getString("Nickname").equals(""))
                                    {
                                        String nickname = "\u00A7a, also known as \u00A72" + loopValue.getString("Nickname");
                                        player.sendMessage(Text.of(start + "\u00A7aA level " + name + nickname + "\u00A7a."));
                                    }
                                    else
                                        player.sendMessage(Text.of(start + "\u00A7aA level " + name + "\u00A7a."));
                                }

                                slotTicker++;
                            }

                            player.sendMessage(Text.of(""));

                            if (commandCost > 0)
                            {
                                player.sendMessage(Text.of("\u00A7eWant to know more? Use: \u00A76" +
                                    alias + " " + target.getName() + " <slot> {-c to confirm}"));
                                player.sendMessage(Text.of("\u00A75Warning: \u00A7dThis will cost you \u00A75" +
                                    commandCost + "\u00A7d coins."));
                            }
                            else
                                player.sendMessage(Text.of("\u00A7eWant to know more? Use: \u00A76" +
                                    alias + " " + target.getName() + " <slot>"));

                            player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
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
                                    src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + targetString + " " + slot + " -c"));
                                }
                                else
                                {
                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking a Pok\u00E9mon's status costs \u00A76" + costToConfirm + "\u00A7e coins."));
                                    src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + slot + " -c"));
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

	private void checkAndAddHeader(int cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    private void checkAndAddFooter(int cost, Player player)
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
    private void printCorrectPerm(int cost, Player player)
    {
        if (cost != 0)
        {
            if (player.hasPermission("pixelupgrade.command.checkstats.other"))
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " [optional target] <slot, 1-6> {-c to confirm}"));
            else
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6> {-c to confirm} \u00A77(no perms for target)"));
        }
        else
        {
            if (player.hasPermission("pixelupgrade.command.checkstats.other"))
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " [optional target] <slot, 1-6>"));
            else
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6> \u00A77(no perms for target)"));
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
                PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76CheckStats // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73CheckStats // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72CheckStats // debug: \u00A7a" + inputString);
        }
    }

    private Boolean checkConfigBool(String node)
    {
        if (!CheckStatsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckStatsConfig.getInstance().getConfig().getNode(node).getBoolean();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Integer checkConfigInt()
    {
        if (!CheckStatsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return CheckStatsConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cCould not parse config variable \"commandCost\"!");
            return null;
        }
    }

    private Integer checkFusionConfigInt(String node)
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode(node).isVirtual())
            return DittoFusionConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cCan't read remote config variable \"" + node + "\" for /dittofusion!");
            return null;
        }
    }

    private Integer checkUpgradeConfigInt(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckStats // critical: \u00A7cCan't read remote config variable \"" + node + "\" for /upgradeivs!");
            return null;
        }
    }

    private void checkAndShow(NBTTagCompound nbt, boolean targetAcquired, Player player, Player target)
    {
        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

        int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        int defenceIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int spAttackIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int spDefenceIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        int totalIVs = HPIV + attackIV + defenceIV + spAttackIV + spDefenceIV + speedIV;
        int percentIVs = totalIVs * 100 / 186;

        int HPEV = nbt.getInteger(NbtKeys.EV_HP);
        int attackEV = nbt.getInteger(NbtKeys.EV_ATTACK);
        int defenceEV = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int spAttackEV = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int spDefenceEV = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int speedEV = nbt.getInteger(NbtKeys.EV_SPEED);
        int totalEVs = HPEV + attackEV + defenceEV + spAttackEV + spDefenceEV + speedEV;
        int percentEVs = totalEVs * 100 / 510;

        int natureNum = nbt.getInteger(NbtKeys.NATURE);
        int growthNum = nbt.getInteger(NbtKeys.GROWTH);
        int genderNum = nbt.getInteger(NbtKeys.GENDER);
        int fuseCount = pokemon.getEntityData().getInteger("fuseCount");
        int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");

        String natureName = "", plusVal = "", minusVal = "", growthName = "", genderName = "";
        String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
        String evs1, evs2, evs3, evs4, evs5, evs6;
        String extraInfo1, extraInfo2, configSpAtk, configSpDef, configSpeed;
        String pName = nbt.getString("Name");

        boolean isShiny, isLegendary, isBaby = false, showedCapMessage = false;
        boolean isDitto = nbt.getString("Name").equals("Ditto");

        if (competitiveMode)
        {
            configSpAtk = "SpA";
            configSpDef = "SpD";
            configSpeed = "Spe";
        }
        else
        {
            configSpAtk = "SAtk";
            configSpDef = "SDef";
            configSpeed = "Spd";
        }

        isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
        if (pName.equals("Riolu") || pName.equals("Mime Jr.") || pName.equals("Happiny"))
            isBaby = true;
        isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

        if (HPIV >= 31)
            ivs1 = String.valueOf("\u00A7l" + HPIV + " \u00A72HP \u00A7r\u00A7f|\u00A7a ");
        else
            ivs1 = String.valueOf(HPIV + " \u00A72HP \u00A7f|\u00A7a ");

        if (attackIV >= 31)
            ivs2 = String.valueOf("\u00A7l" + attackIV + " \u00A72Atk \u00A7r\u00A7f|\u00A7a ");
        else
            ivs2 = String.valueOf(attackIV + " \u00A72Atk \u00A7f|\u00A7a ");

        if (defenceIV >= 31)
            ivs3 = String.valueOf("\u00A7l" + defenceIV + " \u00A72Def \u00A7r\u00A7f|\u00A7a ");
        else
            ivs3 = String.valueOf(defenceIV + " \u00A72Def \u00A7f|\u00A7a ");

        if (spAttackIV >= 31)
            ivs4 = String.valueOf("\u00A7l" + spAttackIV + " \u00A72" + configSpAtk + " \u00A7r\u00A7f|\u00A7a ");
        else
            ivs4 = String.valueOf(spAttackIV + " \u00A72" + configSpAtk + " \u00A7f|\u00A7a ");

        if (spDefenceIV >= 31)
            ivs5 = String.valueOf("\u00A7l" + spDefenceIV + " \u00A72" + configSpDef + " \u00A7r\u00A7f|\u00A7a ");
        else
            ivs5 = String.valueOf(spDefenceIV + " \u00A72" + configSpDef + " \u00A7f|\u00A7a ");

        if (speedIV >= 31)
            ivs6 = String.valueOf("\u00A7l" + speedIV + " \u00A72" + configSpeed + "");
        else
            ivs6 = String.valueOf(speedIV + " \u00A72" + configSpeed + "");

        if (HPEV > 255 || HPEV == 252)
            evs1 = String.valueOf("\u00A7l" + HPEV + " \u00A72HP \u00A7r\u00A7f|\u00A7a ");
        else if (HPEV > 252 && HPEV < 256 && showFixEVsHelper)
            evs1 = String.valueOf("\u00A7c" + HPEV + " \u00A74HP \u00A7f|\u00A7a ");
        else
            evs1 = String.valueOf(HPEV + " \u00A72HP \u00A7f|\u00A7a ");

        if (attackEV > 255 || attackEV == 252)
            evs2 = String.valueOf("\u00A7l" + attackEV + " \u00A72Atk \u00A7r\u00A7f|\u00A7a ");
        else if (attackEV > 252 && attackEV < 256 && showFixEVsHelper)
            evs2 = String.valueOf("\u00A7c" + attackEV + " \u00A74Atk \u00A7f|\u00A7a ");
        else
            evs2 = String.valueOf(attackEV + " \u00A72Atk \u00A7f|\u00A7a ");

        if (defenceEV > 255 || defenceEV == 252)
            evs3 = String.valueOf("\u00A7l" + defenceEV + " \u00A72Def \u00A7r\u00A7f|\u00A7a ");
        else if (defenceEV > 252 && defenceEV < 256 && showFixEVsHelper)
            evs3 = String.valueOf("\u00A7c" + defenceEV + " \u00A74Def \u00A7f|\u00A7a ");
        else
            evs3 = String.valueOf(defenceEV + " \u00A72Def \u00A7f|\u00A7a ");

        if (spAttackEV > 255 || spAttackEV == 252)
            evs4 = String.valueOf("\u00A7l" + spAttackEV + " \u00A72" + configSpAtk + " \u00A7r\u00A7f|\u00A7a ");
        else if (spAttackEV > 252 && spAttackEV < 256 && showFixEVsHelper)
            evs4 = String.valueOf("\u00A7c" + spAttackEV + " \u00A74" + configSpAtk + " \u00A7f|\u00A7a ");
        else
            evs4 = String.valueOf(spAttackEV + " \u00A72" + configSpAtk + " \u00A7f|\u00A7a ");

        if (spDefenceEV > 255 || spDefenceEV == 252)
            evs5 = String.valueOf("\u00A7l" + spDefenceEV + " \u00A72" + configSpDef + " \u00A7r\u00A7f|\u00A7a ");
        else if (spDefenceEV > 252 && spDefenceEV < 256 && showFixEVsHelper)
            evs5 = String.valueOf("\u00A7c" + spDefenceEV + " \u00A74" + configSpDef + " \u00A7f|\u00A7a ");
        else
            evs5 = String.valueOf(spDefenceEV + " \u00A72" + configSpDef + " \u00A7f|\u00A7a ");

        if (speedEV > 255 || speedEV == 252)
            evs6 = String.valueOf("\u00A7l" + speedEV + " \u00A72" + configSpeed + "");
        else if (speedEV > 252 && speedEV < 256 && showFixEVsHelper)
            evs6 = String.valueOf("\u00A7c" + speedEV + " \u00A74" + configSpeed + "");
        else
            evs6 = String.valueOf(speedEV + " \u00A72" + configSpeed + "");

        switch (natureNum)
        {
            case 0:
                natureName = "Hardy";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 1:
                natureName = "Serious";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 2:
                natureName = "Docile";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 3:
                natureName = "Bashful";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 4:
                natureName = "Quirky";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 5:
                natureName = "Lonely";
                plusVal = "+Atk";
                minusVal = "-Def";
                break;
            case 6:
                natureName = "Brave";
                plusVal = "+Atk";
                minusVal = "-" + configSpeed;
                break;
            case 7:
                natureName = "Adamant";
                plusVal = "+Atk";
                minusVal = "-" + configSpAtk;
                break;
            case 8:
                natureName = "Naughty";
                plusVal = "+Atk";
                minusVal = "-" + configSpDef;
                break;
            case 9:
                natureName = "Bold";
                plusVal = "+Def";
                minusVal = "-Atk";
                break;
            case 10:
                natureName = "Relaxed";
                plusVal = "+Def";
                minusVal = "-" + configSpeed;
                break;
            case 11:
                natureName = "Impish";
                plusVal = "+Def";
                minusVal = "-" + configSpAtk;
                break;
            case 12:
                natureName = "Lax";
                plusVal = "+Def";
                minusVal = "-" + configSpDef;
                break;
            case 13:
                natureName = "Timid";
                plusVal = "+" + configSpeed;
                minusVal = "-Atk";
                break;
            case 14:
                natureName = "Hasty";
                plusVal = "+" + configSpeed;
                minusVal = "-Def";
                break;
            case 15:
                natureName = "Jolly";
                plusVal = "+" + configSpeed;
                minusVal = "-" + configSpAtk;
                break;
            case 16:
                natureName = "Naive";
                plusVal = "+" + configSpeed;
                minusVal = "-" + configSpDef;
                break;
            case 17:
                natureName = "Modest";
                plusVal = "+" + configSpAtk;
                minusVal = "-Atk";
                break;
            case 18:
                natureName = "Mild";
                plusVal = "+" + configSpAtk;
                minusVal = "-Def";
                break;
            case 19:
                natureName = "Quiet";
                plusVal = "+" + configSpAtk;
                minusVal = "-" + configSpeed;
                break;
            case 20:
                natureName = "Rash";
                plusVal = "+" + configSpAtk;
                minusVal = "-" + configSpDef;
                break;
            case 21:
                natureName = "Calm";
                plusVal = "+" + configSpDef;
                minusVal = "-Atk";
                break;
            case 22:
                natureName = "Gentle";
                plusVal = "+" + configSpDef;
                minusVal = "-Def";
                break;
            case 23:
                natureName = "Sassy";
                plusVal = "+" + configSpDef;
                minusVal = "-" + configSpeed;
                break;
            case 24:
                natureName = "Careful";
                plusVal = "+" + configSpDef;
                minusVal = "-" + configSpAtk;
                break;
        }

        switch (growthNum)
        {
            case 0: growthName = "Pygmy"; break;
            case 1: growthName = "Runt"; break;
            case 2: growthName = "Small"; break;
            case 3: growthName = "Ordinary"; break;
            case 4: growthName = "Huge"; break;
            case 5: growthName = "Giant"; break;
            case 6: growthName = "Enormous"; break;
            case 7: growthName = "\u00A7cGinormous"; break;
            case 8: growthName = "\u00A7aMicroscopic"; break;
        }

        switch (genderNum)
        {
            case 0: genderName = "\u2642"; break;
            case 1: genderName = "\u2640"; break;
            case 2: genderName = "\u26A5"; break;
        }

        player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));

        if (targetAcquired)
        {
            String startString = "\u00A7eStats of \u00A76" + target.getName() + "\u00A7e's \u00A76" + nbt.getString("Name");
            String nicknameString = "\u00A7e, also known as \u00A76" + nbt.getString("Nickname");

            if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of(startString + nicknameString + nbt.getString("Nickname")));
            else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + nicknameString + "\u00A7f (\u00A7e\u00A7lshiny\u00A7r)"));
            else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + "\u00A7f (\u00A7e\u00A7lshiny\u00A7r)"));
            else
                player.sendMessage(Text.of(startString));
        }
        else
        {
            String startString = "\u00A7eStats of \u00A76" + nbt.getString("Name");
            String nicknameString = "\u00A7e, also known as \u00A76" + nbt.getString("Nickname");

            if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of(startString + nicknameString));
            else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + nicknameString + "\u00A7f (\u00A7e\u00A7lshiny\u00A7r)"));
            else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + "\u00A7f (\u00A7e\u00A7lshiny\u00A7r)"));
            else
                player.sendMessage(Text.of(startString));
        }

        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("\u00A7bTotal IVs\u00A7f: \u00A7a" + totalIVs +
            "\u00A7f/\u00A7a186\u00A7f (\u00A7a" + percentIVs + "%\u00A7f)"));
        player.sendMessage(Text.of("\u00A7bIVs\u00A7f: \u00A7a" +
            ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

        if (showEVs)
        {
            player.sendMessage(Text.of("\u00A7bTotal EVs\u00A7f: \u00A7a" + totalEVs +
                    "\u00A7f/\u00A7a510\u00A7f (\u00A7a" + percentEVs + "%\u00A7f)"));
            player.sendMessage(Text.of("\u00A7bEVs\u00A7f: \u00A7a" +
                    evs1 + "" + evs2 + "" + evs3 + "" + evs4 + "" + evs5 + "" + evs6));
        }

        extraInfo1 = String.valueOf("\u00A7bGender\u00A7f: " + genderName +
                "\u00A7f | \u00A7bSize\u00A7f: " + growthName + "\u00A7f | ");
        extraInfo2 = String.valueOf("\u00A7bNature\u00A7f: " + natureName +
                "\u00A7f (\u00A7a" + plusVal + "\u00A7f/\u00A7c" + minusVal + "\u00A7f)");
        player.sendMessage(Text.of(extraInfo1 + extraInfo2));

        if (showDittoFusionHelper && isDitto || showUpgradeHelper && !isDitto)
            player.sendMessage(Text.of(""));

        if (showDittoFusionHelper && isDitto)
        {
            if (isShiny)
            {
                if (fuseCount != 0 && fuseCount < shinyFusionCap)
                    player.sendMessage(Text.of("\u00A7eThis shiny Ditto has been fused \u00A76" +
                        fuseCount + "\u00A7e/\u00A76" + shinyFusionCap + " \u00A7etimes."));
                else if (fuseCount == 0 && upgradeCount < shinyFusionCap)
                    player.sendMessage(Text.of("\u00A7eThis shiny Ditto can be fused \u00A76" +
                        shinyFusionCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis shiny Ditto cannot be fused any further!"));
            }
            else
            {
                if (fuseCount != 0 && fuseCount < regularFusionCap)
                    player.sendMessage(Text.of("\u00A7eThis Ditto has been fused \u00A76" +
                        fuseCount + "\u00A7e/\u00A76" + regularFusionCap + " \u00A7etimes."));
                else if (fuseCount == 0 && upgradeCount < regularFusionCap)
                    player.sendMessage(Text.of("\u00A7eThis Ditto can be fused \u00A76" +
                        regularFusionCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis Ditto cannot be fused any further!"));
            }

            showedCapMessage = true;
        }
        else if (showUpgradeHelper && !isDitto)
        {
            if (isShiny && isLegendary)
            {
                if (upgradeCount != 0 && upgradeCount < legendaryAndShinyUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis shiny legendary has been upgraded \u00A76" +
                        upgradeCount + "\u00A7e/\u00A76" + legendaryAndShinyUpgradeCap + " \u00A7etimes."));
                else if (upgradeCount == 0 && upgradeCount < legendaryAndShinyUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis shiny legendary can be upgraded \u00A76" +
                        legendaryAndShinyUpgradeCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis shiny legendary has been fully upgraded!"));
            }
            else if (isShiny)
            {
                if (upgradeCount != 0 && upgradeCount < shinyUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis shiny Pok\u00E9mon has been upgraded \u00A76" +
                        upgradeCount + "\u00A7e/\u00A76" + shinyUpgradeCap + " \u00A7etimes."));
                else if (upgradeCount == 0 && upgradeCount < shinyUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis shiny Pok\u00E9mon can be upgraded \u00A76" +
                        shinyUpgradeCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis shiny Pok\u00E9mon has been fully upgraded!"));
            }
            else if (isLegendary)
            {
                if (upgradeCount != 0 && upgradeCount < legendaryUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis legendary has been upgraded \u00A76" +
                        upgradeCount + "\u00A7e/\u00A76" + legendaryUpgradeCap + " \u00A7etimes."));
                else if (upgradeCount == 0 && upgradeCount < legendaryUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis legendary can be upgraded \u00A76" +
                        legendaryUpgradeCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis legendary has been fully upgraded!"));
            }
            else if (isBaby)
            {
                if (upgradeCount != 0 && upgradeCount < babyUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis baby Pok\u00E9mon has been upgraded \u00A76" +
                        upgradeCount + "\u00A7e/\u00A76" + babyUpgradeCap + " \u00A7etimes."));
                else if (upgradeCount == 0 && upgradeCount < babyUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis baby Pok\u00E9mon can be upgraded \u00A76" +
                        babyUpgradeCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis baby Pok\u00E9mon has been fully upgraded!"));
            }
            else
            {
                if (upgradeCount != 0 && upgradeCount < regularUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis Pok\u00E9mon has been upgraded \u00A76" +
                        upgradeCount + "\u00A7e/\u00A76" + regularUpgradeCap + " \u00A7etimes."));
                else if (upgradeCount == 0 && upgradeCount < regularUpgradeCap)
                    player.sendMessage(Text.of("\u00A7eThis Pok\u00E9mon can be upgraded \u00A76" +
                        regularUpgradeCap + "\u00A7e more times!"));
                else
                    player.sendMessage(Text.of("\u00A7eThis Pok\u00E9mon has been fully upgraded!"));
            }

            showedCapMessage = true;
        }

        if (showFixEVsHelper && showEVs && !targetAcquired)
        {
            if (!showedCapMessage)
                player.sendMessage(Text.of(""));

            // Split up to keep it readable.
            String warnEVs = "\u00A75Warning: \u00A7dEVs above 252 do nothing. Try using \u00A75/fixevs\u00A7d.";
            if (HPEV < 256 && HPEV > 252 || attackEV < 256 && attackEV > 252)
                player.sendMessage(Text.of(warnEVs));
            else if (defenceEV < 256 && defenceEV > 252 || spAttackEV < 256 && spAttackEV > 252)
                player.sendMessage(Text.of(warnEVs));
            else if (spDefenceEV < 256 && spDefenceEV > 252 || speedEV < 256 && speedEV > 252)
                player.sendMessage(Text.of(warnEVs));
        }

        player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
    }
}
