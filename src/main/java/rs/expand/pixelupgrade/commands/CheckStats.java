package rs.expand.pixelupgrade.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.api.Sponge;
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
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;

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
                PixelUpgrade.log.info("§4CheckStats // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4CheckStats // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
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
            PixelUpgrade.log.info("§4CheckStats // critical: §cConfig variable \"commandAlias\" could not be found!");
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

    public CommandResult execute(CommandSource src, CommandContext args)
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
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4CheckStats // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
                canContinue = false;
            }
            else if (competitiveMode == null)
            {
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                PixelUpgrade.log.info("§4CheckEgg // critical: §cCouldn't get value of \"competitiveMode\" from the main config.");
                PixelUpgrade.log.info("§4CheckEgg // critical: §cPlease check (or wipe and reload) your PixelUpgrade.conf file.");
            }
            else if (!fusionPresenceCheck || !upgradePresenceCheck)
            { // These errors are shown after the config checker method's errors.
                if (!fusionPresenceCheck && upgradePresenceCheck && showDittoFusionHelper)
                {
                    printToLog(0, "Ditto Fusion integration has been disabled!");
                    printToLog(0, "If need be, remove the file and §4/pureload§c.");
                    showDittoFusionHelper = false;
                }
                else if (fusionPresenceCheck && showUpgradeHelper)
                {
                    printToLog(0, "Upgrade integration has been disabled!");
                    printToLog(0, "If need be, remove the file and §4/pureload§c.");
                    showUpgradeHelper = false;
                }
                else if (showDittoFusionHelper && showUpgradeHelper)
                {
                    printToLog(0, "Integration for both commands has been disabled!");
                    printToLog(0, "If need be, remove the file and §6/pureload§e.");
                    showDittoFusionHelper = false;
                    showUpgradeHelper = false;
                }
            }

            if (canContinue)
            {
                printToLog(2, "Called by player §3" + src.getName() + "§b. Starting!");

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
                                src.sendMessage(Text.of("§4Error: §cCould not find the given target. Check your spelling."));
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
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide at least a slot."));
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
                                player.sendMessage(Text.of("§4Error: §cInvalid slot or flag on second argument. See below."));
                            else
                                player.sendMessage(Text.of("§4Error: §cInvalid slot provided. See below."));
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
                    player.sendMessage(Text.of("§4Error: §cCould not find a valid slot. See below."));
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
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
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
                            player.sendMessage(Text.of("§7-----------------------------------------------------"));
                            player.sendMessage(Text.of("§eThat slot is empty, showing the target's whole team!"));
                            player.sendMessage(Text.of(""));

                            for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
                            {
                                if (slotTicker > 5)
                                    break;

                                String start = "§bSlot " + (slotTicker + 1) + "§f: ";
                                if (loopValue == null)
                                    player.sendMessage(Text.of(start + "§2Empty§a."));
                                else if (loopValue.getBoolean("isEgg"))
                                    player.sendMessage(Text.of(start + "§aAn §2egg§a."));
                                else
                                {
                                    String name = loopValue.getInteger("Level") + "§2 " + loopValue.getString("Name");

                                    if (!loopValue.getString("Nickname").equals(""))
                                    {
                                        String nickname = "§a, also known as §2" + loopValue.getString("Nickname");
                                        player.sendMessage(Text.of(start + "§aA level " + name + nickname + "§a."));
                                    }
                                    else
                                        player.sendMessage(Text.of(start + "§aA level " + name + "§a."));
                                }

                                slotTicker++;
                            }

                            player.sendMessage(Text.of(""));

                            if (commandCost > 0)
                            {
                                player.sendMessage(Text.of("§eWant to know more? Use: §6" +
                                    alias + " " + target.getName() + " <slot> {-c to confirm}"));
                                player.sendMessage(Text.of("§5Warning: §dThis will cost you §5" +
                                    commandCost + "§d coins."));
                            }
                            else
                                player.sendMessage(Text.of("§eWant to know more? Use: §6" +
                                    alias + " " + target.getName() + " <slot>"));

                            player.sendMessage(Text.of("§7-----------------------------------------------------"));
                        }
                        else if (nbt == null && targetAcquired)
                        {
                            printToLog(2, "No Pokémon was found in the provided slot on the target. Abort.");
                            src.sendMessage(Text.of("§4Error: §cYour target has no Pokémon in that slot!"));
                        }
                        else if (nbt == null)
                        {
                            printToLog(2, "No Pokémon was found in the provided slot. Abort.");
                            src.sendMessage(Text.of("§4Error: §cThere's no Pokémon in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg") && enableCheckEggIntegration)
                        {
                            printToLog(2, "Found an egg, recommended /checkegg as per config. Abort.");
                            player.sendMessage(Text.of("§4Error: §cI cannot see into an egg. Check out §4/checkegg§c."));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Found an egg, printed an error instead of recommending /checkegg. (config)");
                            player.sendMessage(Text.of("§4Error: §cSorry, but I cannot reveal what is inside an egg."));
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
                                        printToLog(1, "Checked Pokémon in slot " + slot + ", and took " + costToConfirm + " coins.");
                                        checkAndShow(nbt, targetAcquired, player, target);
                                    }
                                    else
                                    {
                                        BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
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
                            else
                            {
                                printToLog(2, "Got cost but no confirmation; end of the line.");

                                if (targetAcquired)
                                {
                                    slot = Integer.parseInt(args.<String>getOne("slot").get());
                                    src.sendMessage(Text.of("§6Warning: §eChecking this Pokémon's status costs §6" + costToConfirm + "§e coins."));
                                    src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + targetString + " " + slot + " -c"));
                                }
                                else
                                {
                                    src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's status costs §6" + costToConfirm + "§e coins."));
                                    src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + slot + " -c"));
                                }
                            }
                        }
                        else
                        {
                            printToLog(2, "Checked Pokémon in slot " + slot + ". Config price is 0, taking nothing.");
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
            player.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coins."));
            player.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printCorrectPerm(int cost, Player player)
    {
        if (cost != 0)
        {
            if (player.hasPermission("pixelupgrade.command.checkstats.other"))
                player.sendMessage(Text.of("§4Usage: §c" + alias + " [optional target] <slot, 1-6> {-c to confirm}"));
            else
                player.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6> {-c to confirm} §7(no perms for target)"));
        }
        else
        {
            if (player.hasPermission("pixelupgrade.command.checkstats.other"))
                player.sendMessage(Text.of("§4Usage: §c" + alias + " [optional target] <slot, 1-6>"));
            else
                player.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6> §7(no perms for target)"));
        }
    }

    private void throwArg1Error(int cost, boolean hasOtherPerm, Player player)
    {
        checkAndAddHeader(cost, player);
        if (hasOtherPerm)
            player.sendMessage(Text.of("§4Error: §cInvalid target or slot on first argument. See below."));
        else if (cost > 0)
            player.sendMessage(Text.of("§4Error: §cInvalid slot provided on first argument. See below."));
        else
            player.sendMessage(Text.of("§4Error: §cInvalid slot provided. See below."));
        printCorrectPerm(cost, player);
        checkAndAddFooter(cost, player);
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4CheckStats // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§6CheckStats // important: §e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("§3CheckStats // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2CheckStats // debug: §a" + inputString);
        }
    }

    private Boolean checkConfigBool(String node)
    {
        if (!CheckStatsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckStatsConfig.getInstance().getConfig().getNode(node).getBoolean();
        else
        {
            PixelUpgrade.log.info("§4CheckStats // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Integer checkConfigInt()
    {
        if (!CheckStatsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
            return CheckStatsConfig.getInstance().getConfig().getNode("commandCost").getInt();
        else
        {
            PixelUpgrade.log.info("§4CheckStats // critical: §cCould not parse config variable \"commandCost\"!");
            return null;
        }
    }

    private Integer checkFusionConfigInt(String node)
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode(node).isVirtual())
            return DittoFusionConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4CheckStats // critical: §cCan't read remote config variable \"" + node + "\" for /dittofusion!");
            return null;
        }
    }

    private Integer checkUpgradeConfigInt(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4CheckStats // critical: §cCan't read remote config variable \"" + node + "\" for /upgradeivs!");
            return null;
        }
    }

    private void checkAndShow(NBTTagCompound nbt, boolean targetAcquired, Player player, Player target)
    {
        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

        // Set up IVs and matching math.
        int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        int defenceIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int spAttackIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int spDefenceIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        int totalIVs = HPIV + attackIV + defenceIV + spAttackIV + spDefenceIV + speedIV;
        int percentIVs = totalIVs * 100 / 186;

        // Set up EVs and matching math.
        int HPEV = nbt.getInteger(NbtKeys.EV_HP);
        int attackEV = nbt.getInteger(NbtKeys.EV_ATTACK);
        int defenceEV = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int spAttackEV = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int spDefenceEV = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int speedEV = nbt.getInteger(NbtKeys.EV_SPEED);
        int totalEVs = HPEV + attackEV + defenceEV + spAttackEV + spDefenceEV + speedEV;
        int percentEVs = totalEVs * 100 / 510;

        // Figure out if we're using competitive standards or my personal preferences.
        String configSpAtk, configSpDef, configSpeed;
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

        // Get a bunch of data from our GetPokemonInfo utility class. Used for messages, later on.
        ArrayList<String> natureArray =
                GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE), configSpAtk, configSpDef, configSpeed);
        String natureName = natureArray.get(0);
        String plusVal = natureArray.get(1);
        String minusVal = natureArray.get(2);
        String growthName = GetPokemonInfo.getGrowthName(nbt.getInteger(NbtKeys.GROWTH));
        String genderCharacter = GetPokemonInfo.getGenderCharacter(nbt.getInteger(NbtKeys.GENDER));

        String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
        String evs1, evs2, evs3, evs4, evs5, evs6;
        String pName = nbt.getString("Name");
        int fuseCount = pokemon.getEntityData().getInteger("fuseCount");
        int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");

        boolean isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));
        boolean isDitto = nbt.getString("Name").equals("Ditto");
        boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
        boolean isBaby = false, showedCapMessage = false;
        if (pName.equals("Riolu") || pName.equals("Mime Jr.") || pName.equals("Happiny"))
            isBaby = true;

        // Format the IVs for use later, so we can print them.
        if (HPIV < 31)
            ivs1 = String.valueOf(HPIV + " §2HP §f|§a ");
        else
            ivs1 = String.valueOf("§l" + HPIV + " §2HP §r§f|§a ");

        if (attackIV < 31)
            ivs2 = String.valueOf(attackIV + " §2Atk §f|§a ");
        else
            ivs2 = String.valueOf("§l" + attackIV + " §2Atk §r§f|§a ");

        if (defenceIV < 31)
            ivs3 = String.valueOf(defenceIV + " §2Def §f|§a ");
        else
            ivs3 = String.valueOf("§l" + defenceIV + " §2Def §r§f|§a ");

        if (spAttackIV < 31)
            ivs4 = String.valueOf(spAttackIV + " §2" + configSpAtk + " §f|§a ");
        else
            ivs4 = String.valueOf("§l" + spAttackIV + " §2" + configSpAtk + " §r§f|§a ");

        if (spDefenceIV < 31)
            ivs5 = String.valueOf(spDefenceIV + " §2" + configSpDef + " §f|§a ");
        else
            ivs5 = String.valueOf("§l" + spDefenceIV + " §2" + configSpDef + " §r§f|§a ");

        if (speedIV < 31)
            ivs6 = String.valueOf(speedIV + " §2" + configSpeed + "");
        else
            ivs6 = String.valueOf("§l" + speedIV + " §2" + configSpeed + "");

        // Figure out what to print on the EV end, too.
        if (HPEV > 255 || HPEV == 252)
            evs1 = String.valueOf("§l" + HPEV + " §2HP §r§f|§a ");
        else if (HPEV > 252 && HPEV < 256 && showFixEVsHelper)
            evs1 = String.valueOf("§c" + HPEV + " §4HP §f|§a ");
        else
            evs1 = String.valueOf(HPEV + " §2HP §f|§a ");

        if (attackEV > 255 || attackEV == 252)
            evs2 = String.valueOf("§l" + attackEV + " §2Atk §r§f|§a ");
        else if (attackEV > 252 && attackEV < 256 && showFixEVsHelper)
            evs2 = String.valueOf("§c" + attackEV + " §4Atk §f|§a ");
        else
            evs2 = String.valueOf(attackEV + " §2Atk §f|§a ");

        if (defenceEV > 255 || defenceEV == 252)
            evs3 = String.valueOf("§l" + defenceEV + " §2Def §r§f|§a ");
        else if (defenceEV > 252 && defenceEV < 256 && showFixEVsHelper)
            evs3 = String.valueOf("§c" + defenceEV + " §4Def §f|§a ");
        else
            evs3 = String.valueOf(defenceEV + " §2Def §f|§a ");

        if (spAttackEV > 255 || spAttackEV == 252)
            evs4 = String.valueOf("§l" + spAttackEV + " §2" + configSpAtk + " §r§f|§a ");
        else if (spAttackEV > 252 && spAttackEV < 256 && showFixEVsHelper)
            evs4 = String.valueOf("§c" + spAttackEV + " §4" + configSpAtk + " §f|§a ");
        else
            evs4 = String.valueOf(spAttackEV + " §2" + configSpAtk + " §f|§a ");

        if (spDefenceEV > 255 || spDefenceEV == 252)
            evs5 = String.valueOf("§l" + spDefenceEV + " §2" + configSpDef + " §r§f|§a ");
        else if (spDefenceEV > 252 && spDefenceEV < 256 && showFixEVsHelper)
            evs5 = String.valueOf("§c" + spDefenceEV + " §4" + configSpDef + " §f|§a ");
        else
            evs5 = String.valueOf(spDefenceEV + " §2" + configSpDef + " §f|§a ");

        if (speedEV > 255 || speedEV == 252)
            evs6 = String.valueOf("§l" + speedEV + " §2" + configSpeed + "");
        else if (speedEV > 252 && speedEV < 256 && showFixEVsHelper)
            evs6 = String.valueOf("§c" + speedEV + " §4" + configSpeed + "");
        else
            evs6 = String.valueOf(speedEV + " §2" + configSpeed + "");

        player.sendMessage(Text.of("§7-----------------------------------------------------"));

        // Format and show the target Pokémon's name.
        if (targetAcquired)
        {
            String startString = "§eStats of §6" + target.getName() + "§e's §6" + nbt.getString("Name");
            String nicknameString = "§e, also known as §6" + nbt.getString("Nickname");

            if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of(startString + nicknameString + nbt.getString("Nickname")));
            else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + nicknameString + "§f (§e§lshiny§r)"));
            else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + "§f (§e§lshiny§r)"));
            else
                player.sendMessage(Text.of(startString));
        }
        else
        {
            String startString = "§eStats of §6" + nbt.getString("Name");
            String nicknameString = "§e, also known as §6" + nbt.getString("Nickname");

            if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of(startString + nicknameString));
            else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + nicknameString + "§f (§e§lshiny§r)"));
            else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of(startString + "§f (§e§lshiny§r)"));
            else
                player.sendMessage(Text.of(startString));
        }

        // Print out IVs using previously formatted Strings.
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs +
            "§f/§a186§f (§a" + percentIVs + "%§f)"));
        player.sendMessage(Text.of("§bIVs§f: §a" +
            ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

        // Do the same for EVs, if enabled in the config.
        if (showEVs)
        {
            player.sendMessage(Text.of("§bTotal EVs§f: §a" + totalEVs +
                    "§f/§a510§f (§a" + percentEVs + "%§f)"));
            player.sendMessage(Text.of("§bEVs§f: §a" +
                    evs1 + "" + evs2 + "" + evs3 + "" + evs4 + "" + evs5 + "" + evs6));
        }

        // Show extra info, which we grabbed from GetPokemonInfo.
        String extraInfo1 = String.valueOf("§bGender§f: " + genderCharacter +
                "§f | §bSize§f: " + growthName + "§f | ");
        String extraInfo2 = String.valueOf("§bNature§f: " + natureName +
                "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
        player.sendMessage(Text.of(extraInfo1 + extraInfo2));

        // Check and show whether the Pokémon can be upgraded/fused further, if enabled in config.
        if (showDittoFusionHelper && isDitto || showUpgradeHelper && !isDitto)
            player.sendMessage(Text.of(""));
        if (showDittoFusionHelper && isDitto)
        {
            if (isShiny)
            {
                if (fuseCount != 0 && fuseCount < shinyFusionCap)
                    player.sendMessage(Text.of("§eThis shiny Ditto has been fused §6" +
                        fuseCount + "§e/§6" + shinyFusionCap + " §etimes."));
                else if (fuseCount == 0 && upgradeCount < shinyFusionCap)
                    player.sendMessage(Text.of("§eThis shiny Ditto can be fused §6" +
                        shinyFusionCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis shiny Ditto cannot be fused any further!"));
            }
            else
            {
                if (fuseCount != 0 && fuseCount < regularFusionCap)
                    player.sendMessage(Text.of("§eThis Ditto has been fused §6" +
                        fuseCount + "§e/§6" + regularFusionCap + " §etimes."));
                else if (fuseCount == 0 && upgradeCount < regularFusionCap)
                    player.sendMessage(Text.of("§eThis Ditto can be fused §6" +
                        regularFusionCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis Ditto cannot be fused any further!"));
            }

            showedCapMessage = true;
        }
        else if (showUpgradeHelper && !isDitto)
        {
            if (isShiny && isLegendary)
            {
                if (upgradeCount != 0 && upgradeCount < legendaryAndShinyUpgradeCap)
                    player.sendMessage(Text.of("§eThis shiny legendary has been upgraded §6" +
                        upgradeCount + "§e/§6" + legendaryAndShinyUpgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < legendaryAndShinyUpgradeCap)
                    player.sendMessage(Text.of("§eThis shiny legendary can be upgraded §6" +
                        legendaryAndShinyUpgradeCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis shiny legendary has been fully upgraded!"));
            }
            else if (isShiny)
            {
                if (upgradeCount != 0 && upgradeCount < shinyUpgradeCap)
                    player.sendMessage(Text.of("§eThis shiny Pokémon has been upgraded §6" +
                        upgradeCount + "§e/§6" + shinyUpgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < shinyUpgradeCap)
                    player.sendMessage(Text.of("§eThis shiny Pokémon can be upgraded §6" +
                        shinyUpgradeCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis shiny Pokémon has been fully upgraded!"));
            }
            else if (isLegendary)
            {
                if (upgradeCount != 0 && upgradeCount < legendaryUpgradeCap)
                    player.sendMessage(Text.of("§eThis legendary has been upgraded §6" +
                        upgradeCount + "§e/§6" + legendaryUpgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < legendaryUpgradeCap)
                    player.sendMessage(Text.of("§eThis legendary can be upgraded §6" +
                        legendaryUpgradeCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis legendary has been fully upgraded!"));
            }
            else if (isBaby)
            {
                if (upgradeCount != 0 && upgradeCount < babyUpgradeCap)
                    player.sendMessage(Text.of("§eThis baby Pokémon has been upgraded §6" +
                        upgradeCount + "§e/§6" + babyUpgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < babyUpgradeCap)
                    player.sendMessage(Text.of("§eThis baby Pokémon can be upgraded §6" +
                        babyUpgradeCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis baby Pokémon has been fully upgraded!"));
            }
            else
            {
                if (upgradeCount != 0 && upgradeCount < regularUpgradeCap)
                    player.sendMessage(Text.of("§eThis Pokémon has been upgraded §6" +
                        upgradeCount + "§e/§6" + regularUpgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < regularUpgradeCap)
                    player.sendMessage(Text.of("§eThis Pokémon can be upgraded §6" +
                        regularUpgradeCap + "§e more times!"));
                else
                    player.sendMessage(Text.of("§eThis Pokémon has been fully upgraded!"));
            }

            showedCapMessage = true;
        }

        // Show the wasted EVs helper message if, again, it's enabled in the config. Configs are awesome.
        if (showFixEVsHelper && showEVs && !targetAcquired)
        {
            if (!showedCapMessage)
                player.sendMessage(Text.of(""));

            // Split up to keep it readable.
            String warnEVs = "§5Warning: §dEVs above 252 do nothing. Try using §5/fixevs§d.";
            if (HPEV < 256 && HPEV > 252 || attackEV < 256 && attackEV > 252)
                player.sendMessage(Text.of(warnEVs));
            else if (defenceEV < 256 && defenceEV > 252 || spAttackEV < 256 && spAttackEV > 252)
                player.sendMessage(Text.of(warnEVs));
            else if (spDefenceEV < 256 && spDefenceEV > 252 || speedEV < 256 && speedEV > 252)
                player.sendMessage(Text.of(warnEVs));
        }

        player.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
