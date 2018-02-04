// PixelUpgrade's very first command. Originally known as /upgrade stats, then /getstats, and then finally this.
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
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
import rs.expand.pixelupgrade.utilities.CommonMethods;
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class CheckStats implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Boolean showTeamWhenSlotEmpty, showEVs, showFixEVsHelper, showUpgradeHelper, showDittoFusionHelper;
    public static Boolean enableCheckEggIntegration;
    public static Integer commandCost;

    // Set up some more variables for internal use.
    private boolean gotUpgradeError = false, gotFusionError = false, calledRemotely;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    // If we're running from console, we need to swallow everything to avoid cluttering it.
    private void printToLog (int debugNum, String inputString)
    {
        if (!calledRemotely)
            CommonMethods.printDebugMessage("CheckStats", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        // Are we running from the console? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        ArrayList<String> nativeErrorArray = new ArrayList<>();
        if (commandAlias == null)
            nativeErrorArray.add("commandAlias");
        if (showTeamWhenSlotEmpty == null)
            nativeErrorArray.add("showTeamWhenSlotEmpty");
        if (showEVs == null)
            nativeErrorArray.add("showEVs");
        if (showFixEVsHelper == null)
            nativeErrorArray.add("showFixEVsHelper");
        if (showUpgradeHelper == null)
            nativeErrorArray.add("showUpgradeHelper");
        if (showDittoFusionHelper == null)
            nativeErrorArray.add("showDittoFusionHelper");
        if (enableCheckEggIntegration == null)
            nativeErrorArray.add("enableCheckEggIntegration");
        if (commandCost == null)
            nativeErrorArray.add("commandCost");

        // Also get some stuff from PixelUpgrade.conf.
        ArrayList<String> mainConfigErrorArray = new ArrayList<>();
        if (shortenedHP == null)
            mainConfigErrorArray.add("shortenedHP");
        if (shortenedAttack == null)
            mainConfigErrorArray.add("shortenedAttack");
        if (shortenedDefense == null)
            mainConfigErrorArray.add("shortenedDefense");
        if (shortenedSpecialAttack == null)
            mainConfigErrorArray.add("shortenedSpecialAttack");
        if (shortenedSpecialDefense == null)
            mainConfigErrorArray.add("shortenedSpecialDefense");
        if (shortenedSpeed == null)
            mainConfigErrorArray.add("shortenedSpeed");

        if (!nativeErrorArray.isEmpty())
        {
            CommonMethods.printCommandNodeError("CheckStats", nativeErrorArray);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else if (!mainConfigErrorArray.isEmpty())
        {
            CommonMethods.printMainNodeError("PixelUpgrade", mainConfigErrorArray);
            src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
        }
        else
        {
            if (calledRemotely)
            {
                CommonMethods.printDebugMessage("CheckStats", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean gotCheckEggError = false;

            if (showDittoFusionHelper || showUpgradeHelper || enableCheckEggIntegration)
            {
                printToLog(2, "Entering external config loading. Errors will be logged.");
                ArrayList<String> upgradeErrorArray = new ArrayList<>(), fusionErrorArray = new ArrayList<>();

                if (showDittoFusionHelper)
                {
                    if (DittoFusion.regularCap == null)
                        fusionErrorArray.add("regularCap");
                    if (DittoFusion.shinyCap == null)
                        fusionErrorArray.add("shinyCap");

                    CommonMethods.printPartialNodeError("CheckStats", "DittoFusion", fusionErrorArray);
                }

                if (showUpgradeHelper)
                {
                    if (UpgradeIVs.legendaryAndShinyCap == null)
                        upgradeErrorArray.add("legendaryAndShinyCap");
                    if (UpgradeIVs.legendaryCap == null)
                        upgradeErrorArray.add("legendaryCap");
                    if (UpgradeIVs.shinyBabyCap == null)
                        upgradeErrorArray.add("shinyBabyCap");
                    if (UpgradeIVs.babyCap == null)
                        upgradeErrorArray.add("babyCap");
                    if (UpgradeIVs.shinyCap == null)
                        upgradeErrorArray.add("shinyCap");
                    if (UpgradeIVs.regularCap == null)
                        upgradeErrorArray.add("regularCap");

                    CommonMethods.printPartialNodeError("CheckStats", "UpgradeIVs", upgradeErrorArray);
                }

                if (enableCheckEggIntegration && CheckEgg.commandAlias == null)
                {
                    CommonMethods.printDebugMessage("CheckStats", 0,
                            "Could not read alias for command \"§4/checkegg§c\".");
                    gotCheckEggError = true;
                }

                if (!fusionErrorArray.isEmpty() || !upgradeErrorArray.isEmpty() || gotCheckEggError)
                {
                    printToLog(0, "Could not read one or more remote config nodes.");
                    printToLog(0, "Disabling integration for now, please check this.");

                    // Set up our "got an error" flags. Reset to false if we didn't, so we don't cause issues later.
                    gotFusionError = !fusionErrorArray.isEmpty();
                    gotUpgradeError = !upgradeErrorArray.isEmpty();
                }

                printToLog(2, "External config loading is done. Moving on to argument parsing.");
            }

            boolean canContinue = false, commandConfirmed = false;
            boolean hasOtherPerm = src.hasPermission("pixelupgrade.command.other.checkstats");
            Optional<String> arg1Optional = args.getOne("target/slot");
            Optional<String> arg2Optional = args.getOne("slot/confirmation");
            Player target = null;
            int slot = 0;

            if (calledRemotely)
            {
                // Do we have an argument in the first slot?
                if (arg1Optional.isPresent())
                {
                    String arg1String = arg1Optional.get();

                    // Do we have a valid online player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();
                        canContinue = true;
                    }
                    else
                        src.sendMessage(Text.of("§4Error: §cInvalid target on first argument. See below."));
                }
                else
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. See below."));

                // Do we have an argument in the second slot, and no error from arg 1?
                // If arg 2 is not present, the user probably wants to see the target's whole party.
                if (canContinue && arg2Optional.isPresent())
                {
                    String arg2String = arg2Optional.get();

                    // Do we have a slot?
                    if (arg2String.matches("^[1-6]"))
                    {
                        // canContinue is already flagged true here, no need to re-set it.
                        slot = Integer.parseInt(arg2String);
                    }
                    else
                    {
                        // ...we should totally set it false here, though.
                        // Otherwise it'll print the error but then move on to execution anyway. That happened, oops.
                        src.sendMessage(Text.of("§4Error: §cInvalid slot on second argument. See below."));
                        canContinue = false;
                    }
                }

                if (!canContinue)
                    printSyntaxHelper(src, true, true);
            }
            else
            {
                printToLog(2, "Starting argument check for player's input.");
                String errorString = "§4There's an error message missing, please report this!";
                boolean canSkip = false;

                // Ugly, but it'll do for now... Doesn't seem like my usual way of getting flags will work here.
                Optional<String> arg3Optional = args.getOne("confirmation");

                if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 2.");
                    commandConfirmed = true;
                }
                else if (arg3Optional.isPresent() && arg3Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 3.");
                    commandConfirmed = true;
                }

                // Start checking arguments for non-flag contents. First up: argument 1.
                if (arg1Optional.isPresent())
                {
                    printToLog(2, "There's something in the first argument slot!");
                    String arg1String = arg1Optional.get();

                    // Do we have a slot?
                    if (arg1String.matches("^[1-6]"))
                    {
                        printToLog(2, "Found a valid slot in argument 1.");
                        slot = Integer.parseInt(arg1String);
                        canContinue = true;

                        // Is the player not allowed to check other people's Pokémon, and is there no cost? Skip ahead!
                        if (!hasOtherPerm && commandCost == 0)
                        {
                            printToLog(2, "Player is missing \"other\" perm, cost is 0. Skip to execution!");
                            canSkip = true;
                        }
                    }
                    // Is our calling player allowed to check other people's Pokémon, and is arg 1 a valid target?
                    else if (hasOtherPerm && Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();

                        if (!src.getName().equalsIgnoreCase(arg1String))
                        {
                            printToLog(2, "Found a valid target in argument 1.");
                            canContinue = true;
                        }
                        else
                        {
                            printToLog(1, "Player targeted own name. Wow. Exit.");
                            errorString = "§4Error: §cIf you want to see your own team, just look left!";
                        }
                    }
                    else
                    {
                        printToLog(1, "Invalid slot (or target?) on first argument. Exit.");

                        if (hasOtherPerm)
                            errorString = "§4Error: §cInvalid target or slot on first argument. See below.";
                        else
                            errorString = "§4Error: §cInvalid slot on first argument. See below.";
                    }
                }
                else
                {
                    printToLog(1, "No arguments were found. Exit.");
                    errorString = "§4Error: §cNo arguments found. See below.";
                }

                // Can we continue, were we not told to skip and do we not have a slot already? Check arg 2 for one.
                // Keep in mind: canContinue is now inverted, so we have to explicitly set false on hitting an error.
                if (canContinue && !canSkip && slot == 0)
                {
                    if (arg2Optional.isPresent())
                    {
                        printToLog(2, "There's something in the second argument slot, and we need it!");
                        String arg2String = arg2Optional.get();

                        // Do we have a slot?
                        if (arg2String.matches("^[1-6]"))
                        {
                            printToLog(2, "Found a valid slot in argument 2. Moving to execution.");
                            slot = Integer.parseInt(arg2String);
                        }
                        else
                        {
                            printToLog(1, "Invalid slot on second argument. Exit.");
                            errorString = "§4Error: §cInvalid slot on second argument. See below.";
                            canContinue = false;
                        }
                    }
                    else if (!showTeamWhenSlotEmpty)
                    {
                        printToLog(1, "Missing slot on second argument, team showing is off. Exit.");
                        errorString = "§4Error: §cMissing slot on second argument. See below.";
                        canContinue = false;
                    }
                }

                if (!canContinue)
                {
                    if (commandCost > 0)
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    src.sendMessage(Text.of(errorString));
                    printSyntaxHelper(src, false, hasOtherPerm);

                    CommonMethods.checkAndAddFooter(commandCost, src);
                }
            }

            if (canContinue)
            {
                Optional<PlayerStorage> storage;
                if (target != null)
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
                else
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                // Running this from the console with no target? We'll already have hit an error and exited!
                if (!storage.isPresent())
                {
                    if (target != null)
                        printToLog(0, "§4" + target.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                    else
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");

                    src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                }
                else
                {
                    PlayerStorage storageCompleted = storage.get();
                    NBTTagCompound nbt;
                    if (slot != 0)
                        nbt = storageCompleted.partyPokemon[slot - 1];
                    else
                        nbt = null;

                    if (showTeamWhenSlotEmpty && slot == 0)
                        checkParty(src, target, storageCompleted, calledRemotely);
                    else if (nbt == null)
                    {
                        if (target != null)
                        {
                            printToLog(1, "No Pokémon was found in the provided target slot. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYour target has no Pokémon in that slot!"));
                        }
                        else
                        {
                            printToLog(1, "No Pokémon was found in the provided slot. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThere's no Pokémon in that slot!"));
                        }
                    }
                    else if (!calledRemotely && nbt.getBoolean("isEgg")) // Allow egg checking for console!
                    {
                        if (enableCheckEggIntegration && !gotCheckEggError)
                        {
                            printToLog(1, "Found an egg, recommended CheckEgg alias as per config. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThis command only checks hatched Pokémon. Try: §4/" +
                                    CheckEgg.commandAlias + "§c."));
                        }
                        else
                        {
                            printToLog(1, "Found an egg. Printing error instead of recommending CheckEgg, as per config.");
                            src.sendMessage(Text.of("§4Error: §cYou can only check hatched Pokémon."));
                        }
                    }
                    else if (!calledRemotely && commandCost > 0) // Don't use the economy for console!
                    {
                        @SuppressWarnings("ConstantConditions") // !calledRemotely already guarantees src is a Player.
                            Player player = (Player) src;

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
                                    boolean haveTarget = false;
                                    if (target != null)
                                        haveTarget = true;

                                    printToLog(1, "Checked slot §3" + slot +
                                            "§b, taking §3" + costToConfirm + "§b coins.");
                                    checkSpecificSlot(src, target, nbt, haveTarget);
                                }
                                else
                                {
                                    BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                    printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                            "§b, and we're lacking §3" + balanceNeeded);
                                    src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
                                }
                            }
                            else
                            {
                                printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                                src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                            }
                        }
                        else
                        {
                            printToLog(1, "Got cost but no confirmation; end of the line.");

                            // Is cost to confirm exactly one coin?
                            if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's status costs §6one §ecoin."));
                            else
                            {
                                src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's status costs §6" +
                                        costToConfirm + "§e coins."));
                            }

                            if (target != null)
                            {
                                src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " +
                                        target.getName() + " " + slot + " -c"));
                            }
                            else
                                src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                        }
                    }
                    else
                    {
                        boolean haveTarget = false;
                        if (target != null)
                            haveTarget = true;

                        // Debug message gets swallowed if run from console, as usual.
                        printToLog(1, "Checked slot §3" + slot + "§b. Config price is §30§b, taking nothing.");
                        checkSpecificSlot(src, target, nbt, haveTarget);
                    }
                }
            }
        }

        return CommandResult.success();
	}

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(CommandSource src, boolean isConsole, boolean hasOtherPerm)
    {
        if (isConsole)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target> [slot? 1-6]"));
        else
        {
            String confirmString;
            if (commandCost != 0)
                confirmString = " {-c to confirm}";
            else
                confirmString = "";

            if (hasOtherPerm && showTeamWhenSlotEmpty)
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] [slot? 1-6]" + confirmString));
            else if (hasOtherPerm)
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] <slot, 1-6>" + confirmString));
            else
            {
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>" + confirmString +
                        " §7(no perms for target)"));
            }
        }
    }

    private void checkParty(CommandSource src, Player target, PlayerStorage storageCompleted, boolean calledRemotely)
    {
        printToLog(1, "No target slot provided, printing team to chat as per config. Exit.");

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
        src.sendMessage(Text.of("§eNo slot found, showing the target's whole team."));
        src.sendMessage(Text.of(""));

        int slotTicker = 0;
        for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
        {
            if (slotTicker > 5)
                break;

            String start = "§bSlot " + (slotTicker + 1) + "§f: ";
            if (loopValue == null)
                src.sendMessage(Text.of(start + "§2Empty§a."));
            else if (loopValue.getBoolean("isEgg"))
                src.sendMessage(Text.of(start + "§aAn §2egg§a."));
            else
            {
                String name = loopValue.getInteger("Level") + "§2 " + loopValue.getString("Name");

                if (!loopValue.getString("Nickname").equals(""))
                {
                    String nickname = "§a, also known as §2" + loopValue.getString("Nickname");
                    src.sendMessage(Text.of(start + "§aA level " + name + nickname + "§a."));
                }
                else
                    src.sendMessage(Text.of(start + "§aA level " + name + "§a."));
            }

            slotTicker++;
        }

        src.sendMessage(Text.of(""));

        if (!calledRemotely && commandCost > 0)
        {
            src.sendMessage(Text.of("§eWant more info? §6/" + commandAlias + " " + target.getName() +
                    " <slot, 1-6> {-c to confirm}"));

            if (commandCost == 1)
                src.sendMessage(Text.of("§5Warning: §dThis will cost you §5one §dcoin."));
            else
                src.sendMessage(Text.of("§5Warning: §dThis will cost you §5" +
                    commandCost + " §dcoins."));
        }
        else
            src.sendMessage(Text.of("§eWant to know more? §6/" + commandAlias + " " + target.getName()));

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }

    private void checkSpecificSlot(CommandSource src, Player target, NBTTagCompound nbt, boolean haveTarget)
    {
        // Set up IVs and matching math.
        int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        int defenseIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int spAttIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int spDefIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
        BigDecimal percentIVs = totalIVs.multiply(new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

        // Format the IVs for use later, so we can print them.
        String ivs1 = String.valueOf(HPIV + " §2" + shortenedHP + " §r|§a ");
        String ivs2 = String.valueOf(attackIV + " §2" + shortenedAttack + " §r|§a ");
        String ivs3 = String.valueOf(defenseIV + " §2" + shortenedDefense + " §r|§a ");
        String ivs4 = String.valueOf(spAttIV + " §2" + shortenedSpecialAttack + " §r|§a ");
        String ivs5 = String.valueOf(spDefIV + " §2" + shortenedSpecialDefense + " §r|§a ");
        String ivs6 = String.valueOf(speedIV + " §2" + shortenedSpeed);

        if (HPIV > 30)
            ivs1 = String.valueOf("§o") + ivs1;
        if (attackIV > 30)
            ivs2 = String.valueOf("§o") + ivs2;
        if (defenseIV > 30)
            ivs3 = String.valueOf("§o") + ivs3;
        if (spAttIV > 30)
            ivs4 = String.valueOf("§o") + ivs4;
        if (spDefIV > 30)
            ivs5 = String.valueOf("§o") + ivs5;
        if (speedIV > 30)
            ivs6 = String.valueOf("§o") + ivs6;

        // Do the same for EVs.
        int HPEV = nbt.getInteger(NbtKeys.EV_HP);
        int attackEV = nbt.getInteger(NbtKeys.EV_ATTACK);
        int defenceEV = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int spAttEV = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int spDefEV = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int speedEV = nbt.getInteger(NbtKeys.EV_SPEED);
        BigDecimal totalEVs = BigDecimal.valueOf(HPEV + attackEV + defenceEV + spAttEV + spDefEV + speedEV);
        BigDecimal percentEVs = totalEVs.multiply(new BigDecimal("100")).divide(new BigDecimal("510"), 2, BigDecimal.ROUND_HALF_UP);
        String evs1, evs2, evs3, evs4, evs5, evs6;

        // Figure out what to print on the EV end, too.
        if (HPEV > 255 || HPEV == 252)
            evs1 = String.valueOf("§o" + HPEV + " §2" + shortenedHP + " §r|§a ");
        else if (HPEV > 252 && showFixEVsHelper)
            evs1 = String.valueOf("§c" + HPEV + " §4" + shortenedHP + " §r|§a ");
        else
            evs1 = String.valueOf(HPEV + " §2" + shortenedHP + " §r|§a ");

        if (attackEV > 255 || attackEV == 252)
            evs2 = String.valueOf("§o" + attackEV + " §2" + shortenedAttack + " §r|§a ");
        else if (attackEV > 252 && showFixEVsHelper)
            evs2 = String.valueOf("§c" + attackEV + " §4" + shortenedAttack + " §r|§a ");
        else
            evs2 = String.valueOf(attackEV + " §2" + shortenedAttack + " §r|§a ");

        if (defenceEV > 255 || defenceEV == 252)
            evs3 = String.valueOf("§o" + defenceEV + " §2" + shortenedDefense + " §r|§a ");
        else if (defenceEV > 252 && showFixEVsHelper)
            evs3 = String.valueOf("§c" + defenceEV + " §4" + shortenedDefense + " §r|§a ");
        else
            evs3 = String.valueOf(defenceEV + " §2" + shortenedDefense + " §r|§a ");

        if (spAttEV > 255 || spAttEV == 252)
            evs4 = String.valueOf("§o" + spAttEV + " §2" + shortenedSpecialAttack + " §r|§a ");
        else if (spAttEV > 252 && showFixEVsHelper)
            evs4 = String.valueOf("§c" + spAttEV + " §4" + shortenedSpecialAttack + " §r|§a ");
        else
            evs4 = String.valueOf(spAttEV + " §2" + shortenedSpecialAttack + " §r|§a ");

        if (spDefEV > 255 || spDefEV == 252)
            evs5 = String.valueOf("§o" + spDefEV + " §2" + shortenedSpecialDefense + " §r|§a ");
        else if (spDefEV > 252 && showFixEVsHelper)
            evs5 = String.valueOf("§c" + spDefEV + " §4" + shortenedSpecialDefense + " §r|§a ");
        else
            evs5 = String.valueOf(spDefEV + " §2" + shortenedSpecialDefense + " §r|§a ");

        if (speedEV > 255 || speedEV == 252)
            evs6 = String.valueOf("§o" + speedEV + " §2" + shortenedSpeed);
        else if (speedEV > 252 && showFixEVsHelper)
            evs6 = String.valueOf("§c" + speedEV + " §4" + shortenedSpeed);
        else
            evs6 = String.valueOf(speedEV + " §2" + shortenedSpeed);

        // Get a bunch of data from our GetPokemonInfo utility class. Used for messages, later on.
        // TODO: Fix gender printing on console. On Windows and possibly other OSes, the character becomes a "?".
        ArrayList<String> natureArray = GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE));
        String natureName = natureArray.get(0);
        String plusVal = natureArray.get(1);
        String minusVal = natureArray.get(2);
        String growthName = GetPokemonInfo.getGrowthName(nbt.getInteger(NbtKeys.GROWTH));
        String genderCharacter = GetPokemonInfo.getGenderCharacter(nbt.getInteger(NbtKeys.GENDER));

        src.sendMessage(Text.of("§7-----------------------------------------------------"));

        // Format and show the target Pokémon's name.
        String startString, nicknameString = ", also known as §6" + nbt.getString("Nickname");
        if (haveTarget)
        {
            if (nbt.getBoolean("isEgg") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                startString = "§eStats of §6" + target.getName() + "§e's §lshiny§r §6" + nbt.getString("Name") + " §eegg";
            else if (nbt.getBoolean("isEgg"))
                startString = "§eStats of §6" + target.getName() + "§e's §6" + nbt.getString("Name") + " §eegg";
            else if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                startString = "§eStats of §6" + target.getName() + "§e's §lshiny§r §6" + nbt.getString("Name") + "§e";
            else
                startString = "§eStats of §6" + target.getName() + "§e's §6" + nbt.getString("Name") + "§e";
        }
        else
        {
            // Some future-proofing, here. Probably won't hit the egg ones anytime soon.
            if (nbt.getBoolean("isEgg") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                startString = "§eStats of your §lshiny§r §6" + nbt.getString("Name") + " §eegg";
            else if (nbt.getBoolean("isEgg"))
                startString = "§eStats of your §6" + nbt.getString("Name") + " §eegg";
            else if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                startString = "§eStats of your §lshiny§r §6" + nbt.getString("Name") + "§e";
            else
                startString = "§eStats of your §6" + nbt.getString("Name") + "§e";
        }

        if (!nbt.getString("Nickname").equals(""))
            src.sendMessage(Text.of(startString + nicknameString + "§e:"));
        else
            src.sendMessage(Text.of(startString + "§e:"));

        // Print out IVs using previously formatted Strings.
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)"));
        src.sendMessage(Text.of("§bIVs§f: §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6));

        // Do the same for EVs, if enabled in the config.
        if (showEVs)
        {
            src.sendMessage(Text.of("§bTotal EVs§f: §a" + totalEVs + "§f/§a510§f (§a" + percentEVs + "%§f)"));
            src.sendMessage(Text.of("§bEVs§f: §a" + evs1 + evs2 + evs3 + evs4 + evs5 + evs6));
        }

        // Show extra info, which we grabbed from GetPokemonInfo.
        String extraInfo1 = String.valueOf("§bGender§f: " + genderCharacter +
                "§f | §bSize§f: " + growthName + "§f | ");
        String extraInfo2 = String.valueOf("§bNature§f: " + natureName +
                "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
        src.sendMessage(Text.of(extraInfo1 + extraInfo2));

        // Check and show whether the Pokémon can be upgraded/fused further, if enabled in config.
        boolean showedCapMessage = false;
        boolean isDitto = nbt.getString("Name").equals("Ditto");
        if (isDitto && showDittoFusionHelper && !gotFusionError || !isDitto && showUpgradeHelper && !gotUpgradeError)
        {
            EntityPlayerMP playerEntity;
            if (haveTarget)
                playerEntity = (EntityPlayerMP) target;
            else
                playerEntity = (EntityPlayerMP) src;

            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, playerEntity.getServerWorld());
            boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
            src.sendMessage(Text.of(""));

            // Let's re-use the startString String. It's still relevant.
            if (isDitto)
            {
                int fuseCount = pokemon.getEntityData().getInteger("fuseCount"), fusionCap;

                if (isShiny)
                {
                    startString = "§eThis §6shiny Ditto §e";
                    fusionCap = DittoFusion.shinyCap; // Shiny cap.
                }
                else
                {
                    startString = "§eThis §6Ditto §e";
                    fusionCap = DittoFusion.regularCap; // Regular cap.
                }

                if (fuseCount != 0 && fuseCount < fusionCap)
                    src.sendMessage(Text.of(startString + "has been fused §6" + fuseCount + "§e/§6" + fusionCap + " §etimes."));
                else if (fuseCount == 0 && fuseCount < fusionCap)
                    src.sendMessage(Text.of(startString + "can be fused §6" + fusionCap + "§e more times."));
                else
                    src.sendMessage(Text.of(startString + "cannot be fused any further!"));
            }
            else
            {
                int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount"), upgradeCap;
                boolean isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));
                boolean isBaby = false, isEvolvedBaby = false;

                // Check babies. Babies spawn with 3*31 guaranteed IVs. Nicked from /upgradeivs.
                switch (nbt.getString("Name"))
                {
                    case "Pichu": case "Cleffa": case "Igglybuff": case "Togepi": case "Tyrogue":
                    case "Smoochum": case "Elekid": case "Magby": case "Azurill": case "Wynaut":
                    case "Budew": case "Chingling": case "Bonsly": case "Mime Jr.": case "Happiny":
                    case "Munchlax": case "Riolu": case "Mantyke":
                    {
                        printToLog(2, "Provided Pokémon is a known 3*31 IV baby.");
                        isBaby = true;
                        break;
                    }

                    case "Pickachu": case "Raichu": case "Clefairy": case "Clefable": case "Jigglypuff":
                    case "Wigglytuff": case "Togetic": case "Togekiss": case "Hitmonlee": case "Hitmonchan":
                    case "Hitmontop": case "Jynx": case "Electabuzz": case "Electivire": case "Magmar":
                    case "Magmortar": case "Marill": case "Azumarill": case "Wobbuffet":case "Roselia":
                    case "Roserade": case "Chimecho": case "Sudowoodo": case "Mr. Mime": case "Chansey":
                    case "Blissey": case "Snorlax": case "Lucario": case "Mantine":
                    {
                        printToLog(2, "Provided Pokémon is a known 3*31 IV baby evolution.");
                        isEvolvedBaby = true;
                        break;
                    }
                }

                if (isShiny)
                {
                    if (isLegendary)
                    {
                        startString = "§eThis §l§oshiny legendary §r§e";
                        upgradeCap = UpgradeIVs.legendaryAndShinyCap; // Legendary + shiny cap.
                    }
                    else if (isEvolvedBaby)
                    {
                        startString = "§eThis §l§oshiny evolved baby §r§e";
                        upgradeCap = UpgradeIVs.shinyBabyCap; // Shiny cap.
                    }
                    else if (isBaby)
                    {
                        startString = "§eThis §l§oshiny baby §r§e";
                        upgradeCap = UpgradeIVs.shinyBabyCap; // Shiny cap.
                    }
                    else
                    {
                        startString = "§eThis §lshiny §r§ePokémon ";
                        upgradeCap = UpgradeIVs.shinyCap; // Shiny cap.
                    }
                }
                else
                {
                    if (isLegendary)
                    {
                        startString = "§eThis §olegendary §r§ePokémon ";
                        upgradeCap = UpgradeIVs.legendaryCap; // Legendary cap.
                    }
                    else if (isEvolvedBaby)
                    {
                        startString = "§eThis §oevolved baby §r§e";
                        upgradeCap = UpgradeIVs.babyCap; // Baby cap.
                    }
                    else if (isBaby)
                    {
                        startString = "§eThis §obaby §r§e";
                        upgradeCap = UpgradeIVs.babyCap; // Baby cap.
                    }
                    else
                    {
                        startString = "§eThis Pokémon ";
                        upgradeCap = UpgradeIVs.regularCap; // Regular cap.
                    }
                }

                if (upgradeCount != 0 && upgradeCount < upgradeCap)
                    src.sendMessage(Text.of(startString + "has been upgraded §6" + upgradeCount + "§e/§6" + upgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                    src.sendMessage(Text.of(startString + "can be upgraded §6" + upgradeCap + "§e more times."));
                else
                    src.sendMessage(Text.of(startString + "has been fully upgraded!"));
            }

            showedCapMessage = true;
        }

        // Show the wasted EVs helper message if, again, it's enabled in the config. Configs are awesome.
        if (showFixEVsHelper && showEVs && !haveTarget)
        {
            // Set up a message to print if any IVs are wasted.
            String warnEVs = "§5Warning: §dEVs above §5252 §ddo nothing. Try using §5/fixevs§d.";
            boolean needToWarn = false;

            if (HPEV < 256 && HPEV > 252 || attackEV < 256 && attackEV > 252 || defenceEV < 256 && defenceEV > 252)
                needToWarn = true;
            else if (spAttEV < 256 && spAttEV > 252 || spDefEV < 256 && spDefEV > 252 || speedEV < 256 && speedEV > 252)
                needToWarn = true;

            // Add a new line if we don't already have an upgrade/fusion message. Keep them together otherwise.
            if (needToWarn)
            {
                if (!showedCapMessage)
                    src.sendMessage(Text.of(""));

                src.sendMessage(Text.of(warnEVs));
            }
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
