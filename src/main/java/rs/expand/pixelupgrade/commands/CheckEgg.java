package rs.expand.pixelupgrade.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;
import rs.expand.pixelupgrade.utilities.ConfigOperations;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class CheckEgg implements CommandExecutor
{
    // Not sure how this works yet, but nicked it from TotalEconomy.
    // Will try to figure this out later, just glad to have this working for now.
    private PixelUpgrade pixelUpgrade;
    public CheckEgg(PixelUpgrade pixelUpgrade)
        { this.pixelUpgrade = pixelUpgrade; }

    // Load some variables into memory. We'll be using these throughout the command logic.
    private static String alias = ConfigOperations.getConfigValue("CheckEgg", "commandAlias", false);
    private Boolean showName = BooleanUtils.toBooleanObject(ConfigOperations.getConfigValue("CheckEgg", "showName", false));
    private Boolean explicitReveal = BooleanUtils.toBooleanObject(ConfigOperations.getConfigValue("CheckEgg", "explicitReveal", false));
    private Integer babyHintPercentage = Integer.parseInt(ConfigOperations.getConfigValue("CheckEgg", "babyHintPercentage", true));
    private Integer commandCost = Integer.parseInt(ConfigOperations.getConfigValue("CheckEgg", "commandCost", true));
    private Boolean recheckIsFree = BooleanUtils.toBooleanObject(ConfigOperations.getConfigValue("CheckEgg", "recheckIsFree", false));

    // Intialize several variables that we'll be filling in with data from the main config.
    private String shortenedHP, shortenedAttack, shortenedDefense, shortenedSpAtt, shortenedSpDef, shortenedSpeed;

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            boolean presenceCheck = true, mainConfigCheck = true;

            // Fill up the copycat variables. We'll do this here so changes to the main config are synced.
            shortenedHP = PixelUpgrade.getInstance().shortenedHP;
            shortenedAttack = PixelUpgrade.getInstance().shortenedAttack;
            shortenedDefense = PixelUpgrade.getInstance().shortenedDefense;
            shortenedSpAtt = PixelUpgrade.getInstance().shortenedSpAtt;
            shortenedSpDef = PixelUpgrade.getInstance().shortenedSpDef;
            shortenedSpeed = PixelUpgrade.getInstance().shortenedSpeed;

            if (!ObjectUtils.allNotNull(recheckIsFree, showName, explicitReveal, commandCost, babyHintPercentage))
                presenceCheck = false;
            if (!ObjectUtils.allNotNull(shortenedHP, shortenedAttack, shortenedDefense, shortenedSpAtt, shortenedSpDef, shortenedSpeed))
                mainConfigCheck = false;

            if (!presenceCheck || alias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4CheckEgg // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (!mainConfigCheck)
            {
                // Same as above.
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                printToLog(0, "Please check (or wipe and /pureload) your PixelUpgrade.conf file.");
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                alias = "/" + alias;
                int slot = 0;
                String targetString = null, slotString;
                boolean targetAcquired = false, commandConfirmed = false, canContinue = false, hasOtherPerm = false;
                Player player = (Player) src, target = player;

                if (src.hasPermission("pixelupgrade.command.other.checkegg"))
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
                                    printToLog(2, "Played entered their own name as target.");

                                canContinue = true;
                            }
                            else if (Pattern.matches("[a-zA-Z]+", targetString)) // Make an assumption; input is non-numeric so probably not a slot.
                            {
                                printToLog(1, "First argument was invalid. Input not numeric, assuming misspelled name. Exit.");

                                checkAndAddHeader(commandCost, player);
                                src.sendMessage(Text.of("§4Error: §cCould not find the given target. Check your spelling."));
                                printCorrectPerm(commandCost, player);
                                checkAndAddFooter(commandCost, player);
                            }
                            else  // Throw a "safe" error that works for both missing slots and targets.
                            { // Might not be as clean, which is why we check patterns above.
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
                    printToLog(1, "No arguments found. Exit.");

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
                        printToLog(2, "Found something in the second argument slot, and we're clear to use it!");
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
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                    }
                    else
                    {
                        printToLog(2, "Found a Pixelmon storage on the player. Moving along.");

                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null || !nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Could not find an egg in the provided slot, or no Pokémon was found. Exit.");
                            src.sendMessage(Text.of("§4Error: §cCould not find an egg in the provided slot."));
                        }
                        else
                        {
                            printToLog(2, "Egg found. Let's do this!");

                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            boolean wasEggChecked = pokemon.getEntityData().getBoolean("hadEggChecked");

                            if (commandCost == 0 || wasEggChecked && recheckIsFree)
                            {
                                printEggResults(nbt, pokemon, wasEggChecked, commandCost, player);

                                // Keep this below the printEggResults call, or your debug message order will look weird.
                                if (commandCost == 0)
                                    printToLog(1, "Checking egg in slot " + slot + ". Config price is 0, taking nothing.");
                                else
                                    printToLog(1, "Checking egg, slot " + slot + ". Detected a recheck, taking nothing as per config.");
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
                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.of(EventContext.empty(), pixelUpgrade.getPluginContainer()));

                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            printEggResults(nbt, pokemon, wasEggChecked, commandCost, player);

                                            // Keep this below the printEggResults call, or your debug message order will look weird.
                                            printToLog(1, "Checking egg in slot " + slot + ", and taking " + costToConfirm + " coins.");
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
                                    printToLog(1, "Showed cost, no confirmation was provided. Exit.");

                                    if (targetAcquired)
                                    {
                                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                                        src.sendMessage(Text.of("§6Warning: §eChecking this egg's status costs §6" + costToConfirm + "§e coins."));
                                        src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + targetString + " " + slot + " -c"));
                                    }
                                    else
                                    {
                                        src.sendMessage(Text.of("§6Warning: §eChecking an egg's status costs §6" + costToConfirm + "§e coins."));
                                        src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + slot + " -c"));
                                    }
                                }
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

    private void checkAndAddHeader(int cost, Player player)
    {
        if (cost > 0)
            player.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        if (cost != 0)
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
            if (player.hasPermission("pixelupgrade.command.other.checkegg"))
                player.sendMessage(Text.of("§4Usage: §c" + alias + " [optional target] <slot, 1-6> {-c to confirm}"));
            else
                player.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> {-c to confirm} §7(no perms for target)"));
        }
        else
        {
            if (player.hasPermission("pixelupgrade.command.other.checkegg"))
                player.sendMessage(Text.of("§4Usage: §c" + alias + " [optional target] <slot, 1-6>"));
            else
                player.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> §7(no perms for target)"));
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
                PixelUpgrade.log.info("§4CheckEgg // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§3CheckEgg // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2CheckEgg // debug: §a" + inputString);
        }
    }

    private void printEggResults(NBTTagCompound nbt, EntityPixelmon pokemon, boolean wasEggChecked, int cost, Player player)
    {
        printToLog(2, "We have entered the executing method. Checking stats now!");

        // Set up IVs and matching math.
        int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        int defenseIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int spAttIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int spDefIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        int totalIVs = HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV;
        int percentIVs = totalIVs * 100 / 186;
        boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;

        player.sendMessage(Text.of("§7-----------------------------------------------------"));
        if (showName)
        {
            player.sendMessage(Text.of("§eThere's a healthy §6" + nbt.getString("Name") + "§e inside of this egg!"));
            if (explicitReveal)
                player.sendMessage(Text.of(""));
        }

        if (explicitReveal)
        {
            printToLog(2, "Explicit reveal enabled. Printing full IVs, shiny-ness and other info.");

            // Format the IVs for use later, so we can print them.
            String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
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

            player.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)"));
            player.sendMessage(Text.of("§bIVs§f: §a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

            // Get a bunch of data from our GetPokemonInfo utility class.
            ArrayList<String> natureArray = GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE),
                    shortenedSpAtt, shortenedSpDef, shortenedSpeed);
            String natureName = natureArray.get(0);
            String plusVal = natureArray.get(1);
            String minusVal = natureArray.get(2);
            String growthName = GetPokemonInfo.getGrowthName(nbt.getInteger(NbtKeys.GROWTH));
            String genderCharacter = GetPokemonInfo.getGenderCharacter(nbt.getInteger(NbtKeys.GENDER));

            // Show said data.
            String extraInfo1 = String.valueOf("§bGender§f: " + genderCharacter +
                    "§f | §bSize§f: " + growthName + "§f | ");
            String extraInfo2 = String.valueOf("§bNature§f: " + natureName +
                    "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
            player.sendMessage(Text.of(extraInfo1 + extraInfo2));

            // Lucky!
            if (isShiny)
            {
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("§6§lCongratulations! §r§eThis baby is shiny!"));
            }
        }
        else
        {
            printToLog(2, "Explicit reveal disabled, printing vague status.");

            // Figure out whether the baby is anything special. Uses a config-set percentage for stat checks.
            if (percentIVs >= babyHintPercentage && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                player.sendMessage(Text.of("§6What's this? §eThis baby seems to be bursting with energy!"));
            else if (!(percentIVs >= babyHintPercentage) && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("§6What's this? §eThis baby seems to have an odd sheen to it!"));
            else if (percentIVs >= babyHintPercentage && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                player.sendMessage(Text.of("§6What's this? §eSomething about this baby seems real special!"));
            else
                player.sendMessage(Text.of("§eThis baby seems to be fairly ordinary..."));
        }

        if (wasEggChecked && recheckIsFree)
        {
            if (!isShiny || !explicitReveal)
                player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("§dThis egg has been checked before, so this check was free!"));
        }
        else if (!wasEggChecked && recheckIsFree && cost > 0)
        {
            printToLog(2, "First-time check, recheckIsFree is enabled. Flagging the egg for free future rechecks.");
            pokemon.getEntityData().setBoolean("hadEggChecked", true);
        }

        player.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
