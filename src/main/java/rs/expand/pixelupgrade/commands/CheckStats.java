package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.commons.lang3.ObjectUtils;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;
import static rs.expand.pixelupgrade.commands.DittoFusion.*;
import static rs.expand.pixelupgrade.commands.UpgradeIVs.*;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class CheckStats implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective class. Check the imports.
    public static String commandAlias;
    public static Boolean showTeamWhenSlotEmpty;
    public static Boolean showEVs;
    public static Boolean showFixEVsHelper;
    public static Boolean showUpgradeHelper;
    public static Boolean showDittoFusionHelper;
    public static Boolean enableCheckEggIntegration;
    public static Integer commandCost;

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            boolean canContinue = false;
            boolean presenceCheck = true, fusionCheck = true, upgradeCheck = true, mainConfigCheck = true;

            if (!ObjectUtils.allNotNull(regularFusionCap, shinyFusionCap))
                fusionCheck = false;
            if (!ObjectUtils.allNotNull(legendaryShinyUpgradeCap, legendaryUpgradeCap, regularUpgradeCap, shinyUpgradeCap, babyUpgradeCap))
                upgradeCheck = false;
            if (!ObjectUtils.allNotNull(shortenedHP, shortenedAttack, shortenedDefense, shortenedSpAtt, shortenedSpDef, shortenedSpeed))
                mainConfigCheck = false;
            if (!ObjectUtils.allNotNull(enableCheckEggIntegration, showTeamWhenSlotEmpty, commandCost, showEVs))
                presenceCheck = false;
            else if (!ObjectUtils.allNotNull(showFixEVsHelper, showUpgradeHelper, showDittoFusionHelper))
                presenceCheck = false;

            if (!presenceCheck || commandAlias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4CheckStats // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (!mainConfigCheck)
            {
                // Same as above.
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                printToLog(0, "Please check (or wipe and /pureload) your PixelUpgrade.conf file.");
            }
            else if (!fusionCheck || !upgradeCheck)
            { // These errors are shown after the config checker method's errors.
                if (!fusionCheck && upgradeCheck && showDittoFusionHelper)
                {
                    printToLog(0, "Ditto Fusion integration has been disabled! Continuing.");
                    showDittoFusionHelper = false;
                }
                else if (fusionCheck && showUpgradeHelper)
                {
                    printToLog(0, "Upgrade integration has been disabled! Continuing.");
                    showUpgradeHelper = false;
                }
                else
                {
                    printToLog(0, "Integration for both commands has been disabled! Continuing.");
                    showDittoFusionHelper = false;
                    showUpgradeHelper = false;
                }

                printToLog(0, "If need be, remove the file and §4/pureload§c.");
                canContinue = true;
            }
            else
                canContinue = true;

            if (canContinue)
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                String checkEggAlias = CheckEgg.commandAlias;
                if (checkEggAlias == null) // Shown after the config checker method's own errors.
                {
                    printToLog(0, "We'll fall back to showing \"/checkegg\" as the alias, for now...");
                    checkEggAlias = "checkegg"; // Forward slash is added later.
                }

                int slot = 0;
                String targetString = null, slotString;
                boolean targetAcquired = false, commandConfirmed = false, hasOtherPerm = false;
                Player player = (Player) src, target = player;

                // We reset this here since we used it above. It's a bit different from the other commands, but hey.
                // If we get a valid input, we'll set this to "true" again so we can execute the main body of code.
                canContinue = false;

                if (src.hasPermission("pixelupgrade.command.other.checkstats"))
                    hasOtherPerm = true;

                if (args.<String>getOne("target or slot").isPresent())
                {
                    // Check whether we have a confirmation flag.
                    if (!args.<String>getOne("target or slot").get().equalsIgnoreCase("-c"))
                    {
                        printToLog(2, "There's something in the first argument slot!");
                        targetString = args.<String>getOne("target or slot").get();

                        if (targetString.matches("^[1-6]"))
                        {
                            printToLog(2, "Found a slot in argument 1. Continuing to confirmation checks.");
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
                                    printToLog(2, "Found a valid online target! Printed for your convenience: " + target.getName());
                                    targetAcquired = true;
                                }
                                else
                                    printToLog(2, "Player targeted their own name. Let's just pretend that didn't happen.");

                                canContinue = true;
                            }
                            else if (Pattern.matches("[a-zA-Z]+", targetString)) // Making an assumption; input is non-numeric so probably not a slot.
                            {
                                printToLog(1, "First argument was invalid. Input not numeric, assuming misspelled name. Exit.");

                                checkAndAddHeader(commandCost, player);
                                src.sendMessage(Text.of("§4Error: §cCould not find the given target. Check your spelling."));
                                printCorrectPerm(commandCost, player);
                                checkAndAddFooter(commandCost, player);
                            }
                            else  // Throw a "safe" error that works for both missing slots and targets. Might not be as clean, which is why we check patterns above.
                            {
                                printToLog(1, "First argument was invalid, and input has numbers. Throwing generic error. Exit.");
                                throwArg1Error(commandCost, true, player);
                            }
                        }
                        else
                        {
                            printToLog(1, "Invalid slot provided, and player has no \"other\" perm. Exit.");
                            throwArg1Error(commandCost, false, player);
                        }
                    }
                }
                else
                {
                    printToLog(1, "No arguments found. Showing command usage. Exit.");

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
                        printToLog(2, "Got a confirmation flag on argument 2!");
                        commandConfirmed = true;
                    }
                    else if (hasOtherPerm)
                    {
                        printToLog(2, "There's something in the second argument slot!");
                        slotString = args.<String>getOne("slot").get();

                        if (slotString.matches("^[1-6]"))
                        {
                            printToLog(2, "Found a slot in argument 2.");
                            slot = Integer.parseInt(slotString);
                        }
                        else
                        {
                            printToLog(1, "Argument is not a slot or a confirmation flag. Exit.");

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
                        printToLog(2, "Got a confirmation flag on argument 3!");
                        commandConfirmed = true;
                    }
                }

                if (slot == 0 && canContinue)
                {
                    printToLog(1, "Failed final check, no slot was found. Exit.");

                    checkAndAddHeader(commandCost, player);
                    player.sendMessage(Text.of("§4Error: §cCould not find a valid slot. See below."));
                    printCorrectPerm(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }

                if (canContinue)
                {
                    printToLog(2, "No error encountered, input should be valid. Continuing!");

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
                        printToLog(2, "Found a Pixelmon storage on the player. Moving along.");

                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (targetAcquired && showTeamWhenSlotEmpty && nbt == null)
                        {
                            printToLog(1, "Slot provided on target is empty, printing team to chat as per config.");

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
                                    commandAlias + " " + target.getName() + " <slot> {-c to confirm}"));
                                player.sendMessage(Text.of("§5Warning: §dThis will cost you §5" +
                                    commandCost + "§d coins."));
                            }
                            else
                                player.sendMessage(Text.of("§eWant to know more? Use: §6" +
                                    commandAlias + " " + target.getName() + " <slot>"));

                            player.sendMessage(Text.of("§7-----------------------------------------------------"));
                        }
                        else if (nbt == null)
                        {
                            if (targetAcquired)
                            {
                                printToLog(1, "No Pokémon was found in the provided slot on the target. Exit.");
                                src.sendMessage(Text.of("§4Error: §cYour target has no Pokémon in that slot!"));
                            }
                            else
                            {
                                printToLog(1, "No Pokémon was found in the provided slot. Exit.");
                                src.sendMessage(Text.of("§4Error: §cThere's no Pokémon in that slot!"));
                            }
                        }
                        else if (nbt.getBoolean("isEgg") && enableCheckEggIntegration)
                        {
                            printToLog(1, "Found an egg, recommended /checkegg's alias as per config. Exit.");
                            player.sendMessage(Text.of("§4Error: §cI cannot see into an egg. Check out §4/" + checkEggAlias + "§c."));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Found an egg. Printed error instead of recommending /checkegg, as per config.");
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
                                    TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                    if (transactionResult.getResult() == ResultType.SUCCESS)
                                    {
                                        printToLog(1, "Checked Pokémon in slot " + slot + ", and took " + costToConfirm + " coins.");
                                        checkAndShow(nbt, targetAcquired, player, target);
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
                                    printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. May be a bug?");
                                    src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                }
                            }
                            else
                            {
                                printToLog(1, "Got cost but no confirmation; end of the line. Exit.");

                                if (targetAcquired)
                                {
                                    slot = Integer.parseInt(args.<String>getOne("slot").get());
                                    src.sendMessage(Text.of("§6Warning: §eChecking this Pokémon's status costs §6" + costToConfirm + "§e coins."));
                                    src.sendMessage(Text.of("§2Ready? Type: §a" + commandAlias + " " + targetString + " " + slot + " -c"));
                                }
                                else
                                {
                                    src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's status costs §6" + costToConfirm + "§e coins."));
                                    src.sendMessage(Text.of("§2Ready? Type: §a" + commandAlias + " " + slot + " -c"));
                                }
                            }
                        }
                        else
                        {
                            printToLog(1, "Checked Pokémon in slot " + slot + ". Config price is 0, taking nothing.");
                            checkAndShow(nbt, targetAcquired, player, target);
                        }
                    }
                }
            }
        }
        else
            PixelUpgrade.log.info("§cThis command cannot run from the console or command blocks.");

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
            if (player.hasPermission("pixelupgrade.command.other.checkstats"))
                player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " [optional target] <slot, 1-6> {-c to confirm}"));
            else
                player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <slot, 1-6> {-c to confirm} §7(no perms for target)"));
        }
        else
        {
            if (player.hasPermission("pixelupgrade.command.other.checkstats"))
                player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " [optional target] <slot, 1-6>"));
            else
                player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <slot, 1-6> §7(no perms for target)"));
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
                PixelUpgrade.log.info("§3CheckStats // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2CheckStats // debug: §a" + inputString);
        }
    }

    private void checkAndShow(NBTTagCompound nbt, boolean targetAcquired, Player player, Player target)
    {
        // Set up IVs and matching math.
        String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
        int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        int defenseIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int spAttIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int spDefIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
        BigDecimal percentIVs = totalIVs.multiply(new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

        // Do the same for EVs.
        String evs1, evs2, evs3, evs4, evs5, evs6;
        int HPEV = nbt.getInteger(NbtKeys.EV_HP);
        int attackEV = nbt.getInteger(NbtKeys.EV_ATTACK);
        int defenceEV = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int spAttEV = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int spDefEV = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int speedEV = nbt.getInteger(NbtKeys.EV_SPEED);
        BigDecimal totalEVs = BigDecimal.valueOf(HPEV + attackEV + defenceEV + spAttEV + spDefEV + speedEV);
        BigDecimal percentEVs = totalEVs.multiply(new BigDecimal("100")).divide(new BigDecimal("510"), 2, BigDecimal.ROUND_HALF_UP);

        // Get a bunch of data from our GetPokemonInfo utility class. Used for messages, later on.
        ArrayList<String> natureArray = GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE),
                shortenedSpAtt, shortenedSpDef, shortenedSpeed);
        String natureName = natureArray.get(0);
        String plusVal = natureArray.get(1);
        String minusVal = natureArray.get(2);
        String growthName = GetPokemonInfo.getGrowthName(nbt.getInteger(NbtKeys.GROWTH));
        String genderCharacter = GetPokemonInfo.getGenderCharacter(nbt.getInteger(NbtKeys.GENDER));

        // Format the IVs for use later, so we can print them.
        if (HPIV < 31)
            ivs1 = String.valueOf(HPIV + " §2" + shortenedHP + " §f|§a ");
        else
            ivs1 = String.valueOf("§l" + HPIV + " §2" + shortenedHP + " §r§f|§a ");

        if (attackIV < 31)
            ivs2 = String.valueOf(attackIV + " §2" + shortenedAttack + " §f|§a ");
        else
            ivs2 = String.valueOf("§l" + attackIV + " §2" + shortenedAttack + " §r§f|§a ");

        if (defenseIV < 31)
            ivs3 = String.valueOf(defenseIV + " §2" + shortenedDefense + " §f|§a ");
        else
            ivs3 = String.valueOf("§l" + defenseIV + " §2" + shortenedDefense + " §r§f|§a ");

        if (spAttIV < 31)
            ivs4 = String.valueOf(spAttIV + " §2" + shortenedSpAtt + " §f|§a ");
        else
            ivs4 = String.valueOf("§l" + spAttIV + " §2" + shortenedSpAtt + " §r§f|§a ");

        if (spDefIV < 31)
            ivs5 = String.valueOf(spDefIV + " §2" + shortenedSpDef + " §f|§a ");
        else
            ivs5 = String.valueOf("§l" + spDefIV + " §2" + shortenedSpDef + " §r§f|§a ");

        if (speedIV < 31)
            ivs6 = String.valueOf(speedIV + " §2" + shortenedSpeed + "");
        else
            ivs6 = String.valueOf("§l" + speedIV + " §2" + shortenedSpeed + "");

        // Figure out what to print on the EV end, too.
        if (HPEV > 255 || HPEV == 252)
            evs1 = String.valueOf("§l" + HPEV + " §2" + shortenedHP + " §r§f|§a ");
        else if (HPEV > 252 && showFixEVsHelper)
            evs1 = String.valueOf("§c" + HPEV + " §4" + shortenedHP + " §f|§a ");
        else
            evs1 = String.valueOf(HPEV + " §2" + shortenedHP + " §f|§a ");

        if (attackEV > 255 || attackEV == 252)
            evs2 = String.valueOf("§l" + attackEV + " §2" + shortenedAttack + " §r§f|§a ");
        else if (attackEV > 252 && showFixEVsHelper)
            evs2 = String.valueOf("§c" + attackEV + " §4" + shortenedAttack + " §f|§a ");
        else
            evs2 = String.valueOf(attackEV + " §2" + shortenedAttack + " §f|§a ");

        if (defenceEV > 255 || defenceEV == 252)
            evs3 = String.valueOf("§l" + defenceEV + " §2" + shortenedDefense + " §r§f|§a ");
        else if (defenceEV > 252 && showFixEVsHelper)
            evs3 = String.valueOf("§c" + defenceEV + " §4" + shortenedDefense + " §f|§a ");
        else
            evs3 = String.valueOf(defenceEV + " §2" + shortenedDefense + " §f|§a ");

        if (spAttEV > 255 || spAttEV == 252)
            evs4 = String.valueOf("§l" + spAttEV + " §2" + shortenedSpAtt + " §r§f|§a ");
        else if (spAttEV > 252 && showFixEVsHelper)
            evs4 = String.valueOf("§c" + spAttEV + " §4" + shortenedSpAtt + " §f|§a ");
        else
            evs4 = String.valueOf(spAttEV + " §2" + shortenedSpAtt + " §f|§a ");

        if (spDefEV > 255 || spDefEV == 252)
            evs5 = String.valueOf("§l" + spDefEV + " §2" + shortenedSpDef + " §r§f|§a ");
        else if (spDefEV > 252 && showFixEVsHelper)
            evs5 = String.valueOf("§c" + spDefEV + " §4" + shortenedSpDef + " §f|§a ");
        else
            evs5 = String.valueOf(spDefEV + " §2" + shortenedSpDef + " §f|§a ");

        if (speedEV > 255 || speedEV == 252)
            evs6 = String.valueOf("§l" + speedEV + " §2" + shortenedSpeed);
        else if (speedEV > 252 && showFixEVsHelper)
            evs6 = String.valueOf("§c" + speedEV + " §4" + shortenedSpeed);
        else
            evs6 = String.valueOf(speedEV + " §2" + shortenedSpeed);

        player.sendMessage(Text.of("§7-----------------------------------------------------"));

        // Format and show the target Pokémon's name.
        String startString, nicknameString = "§e, also known as §6" + nbt.getString("Nickname");
        if (targetAcquired)
            startString = "§eStats of §6" + target.getName() + "§e's §6" + nbt.getString("Name");
        else
            startString = "§eStats of §6" + nbt.getString("Name");


        if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
            player.sendMessage(Text.of(startString + nicknameString));
        else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            player.sendMessage(Text.of(startString + nicknameString + "§f (§e§lshiny§r)"));
        else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            player.sendMessage(Text.of(startString + "§f (§e§lshiny§r)"));
        else
            player.sendMessage(Text.of(startString));

        // Print out IVs using previously formatted Strings.
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)"));
        player.sendMessage(Text.of("§bIVs§f: §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6));

        // Do the same for EVs, if enabled in the config.
        if (showEVs)
        {
            player.sendMessage(Text.of("§bTotal EVs§f: §a" + totalEVs + "§f/§a510§f (§a" + percentEVs + "%§f)"));
            player.sendMessage(Text.of("§bEVs§f: §a" + evs1 + evs2 + evs3 + evs4 + evs5 + evs6));
        }

        // Show extra info, which we grabbed from GetPokemonInfo.
        String extraInfo1 = String.valueOf("§bGender§f: " + genderCharacter +
                "§f | §bSize§f: " + growthName + "§f | ");
        String extraInfo2 = String.valueOf("§bNature§f: " + natureName +
                "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
        player.sendMessage(Text.of(extraInfo1 + extraInfo2));

        // Check and show whether the Pokémon can be upgraded/fused further, if enabled in config.
        boolean showedCapMessage = false;
        boolean isDitto = nbt.getString("Name").equals("Ditto");
        if (showDittoFusionHelper && isDitto || showUpgradeHelper && !isDitto)
        {
            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
            boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
            player.sendMessage(Text.of(""));

            if (isDitto)
            {
                startString = "§eThis §6Ditto §e";
                int fuseCount = pokemon.getEntityData().getInteger("fuseCount"), fusionCap;

                if (isShiny)
                {
                    startString = "§eThis §6shiny Ditto §e"; // Adjust for shinyness!
                    fusionCap = shinyFusionCap; // Shiny cap.
                }
                else
                    fusionCap = regularFusionCap; // Regular cap.

                if (fuseCount != 0 && fuseCount < fusionCap)
                    player.sendMessage(Text.of(startString + "has been fused §6" + fuseCount + "§e/§6" + fusionCap + " §etimes."));
                else if (fuseCount == 0 && fuseCount < fusionCap)
                    player.sendMessage(Text.of(startString + "can be fused §6" + fusionCap + "§e more times."));
                else
                    player.sendMessage(Text.of(startString + "cannot be fused any further!"));
            }
            else
            {
                String pName = nbt.getString("Name");
                int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount"), upgradeCap;
                boolean isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));
                boolean isBaby = false;

                if (pName.equals("Riolu") || pName.equals("Mime Jr.") || pName.equals("Happiny"))
                    isBaby = true;

                if (isShiny && isLegendary)
                {
                    startString = "§eThis §6shiny legendary §e";
                    upgradeCap = legendaryShinyUpgradeCap; // Legendary + shiny cap.
                }
                else if (isShiny)
                {
                    startString = "§eThis §6shiny Pokémon §e";
                    upgradeCap = shinyUpgradeCap; // Shiny cap.
                }
                else if (isLegendary)
                {
                    startString = "§eThis §6legendary Pokémon §e";
                    upgradeCap = legendaryUpgradeCap; // Legendary cap.
                }
                else if (isBaby)
                {
                    startString = "§eThis §6baby Pokémon §e";
                    upgradeCap = babyUpgradeCap; // Baby cap.
                }
                else
                {
                    startString = "§eThis §6Pokémon §e";
                    upgradeCap = regularUpgradeCap; // Regular cap.
                }

                if (upgradeCount != 0 && upgradeCount < upgradeCap)
                    player.sendMessage(Text.of(startString + "has been upgraded §6" + upgradeCount + "§e/§6" + upgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                    player.sendMessage(Text.of(startString + "can be upgraded §6" + upgradeCap + "§e more times."));
                else
                    player.sendMessage(Text.of(startString + "has been fully upgraded!"));
            }

            showedCapMessage = true;
        }

        // Show the wasted EVs helper message if, again, it's enabled in the config. Configs are awesome.
        if (showFixEVsHelper && showEVs && !targetAcquired)
        {
            // Add a new line if we don't already have an upgrade/fusion message. Keep them together otherwise.
            if (!showedCapMessage)
                player.sendMessage(Text.of(""));

            // Print a message if any IVs are wasted.
            String warnEVs = "§5Warning: §dEVs above §5252 §ddo nothing. Try using §5/fixevs§d.";
            if (HPEV < 256 && HPEV > 252 || attackEV < 256 && attackEV > 252)
                player.sendMessage(Text.of(warnEVs));
            else if (defenceEV < 256 && defenceEV > 252 || spAttEV < 256 && spAttEV > 252)
                player.sendMessage(Text.of(warnEVs));
            else if (spDefEV < 256 && spDefEV > 252 || speedEV < 256 && speedEV > 252)
                player.sendMessage(Text.of(warnEVs));
        }

        player.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
