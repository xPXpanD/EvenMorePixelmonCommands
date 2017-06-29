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
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;

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
                PixelUpgrade.log.info("§4CheckEgg // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4CheckEgg // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
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
            PixelUpgrade.log.info("§4CheckEgg // critical: §cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    // Set up some variables that we'll be using in the egg-checking method.
    private Boolean showName = null;
    private Boolean explicitReveal = null;
    private Boolean recheckIsFree = null;
    private Boolean competitiveMode = null;
    private Integer babyHintPercentage = null;

    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            boolean presenceCheck = true;
            Integer commandCost = checkConfigInt("commandCost");
            babyHintPercentage = checkConfigInt("babyHintPercentage");
            showName = checkConfigBool("showName");
            explicitReveal = checkConfigBool("explicitReveal");
            recheckIsFree = checkConfigBool("recheckIsFree");

            // Grab the competitiveMode value from the main config.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("competitiveMode").isVirtual())
                competitiveMode = PixelUpgradeMainConfig.getInstance().getConfig().getNode("competitiveMode").getBoolean();

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (recheckIsFree == null || showName == null || explicitReveal == null)
                presenceCheck = false;
            else if (commandCost == null || babyHintPercentage == null)
                presenceCheck = false;

            if (!presenceCheck || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4CheckEgg // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (competitiveMode == null)
            {
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                PixelUpgrade.log.info("§4CheckEgg // critical: §cCouldn't get value of \"competitiveMode\" from the main config.");
                PixelUpgrade.log.info("§4CheckEgg // critical: §cPlease check (or wipe and reload) your PixelUpgrade.conf file.");
            }
            else
            {
                printToLog(2, "Called by player §3" + src.getName() + "§b. Starting!");

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
                            else if (Pattern.matches("[a-zA-Z]+", targetString)) // Make an assumption; input is non-numeric so probably not a slot.
                            {
                                printToLog(2, "First argument was invalid. Input not numeric, assuming misspelled name.");

                                checkAndAddHeader(commandCost, player);
                                src.sendMessage(Text.of("§4Error: §cCould not find the given target. Check your spelling."));
                                printCorrectPerm(commandCost, player);
                                checkAndAddFooter(commandCost, player);
                            }
                            else  // Throw a "safe" error that works for both missing slots and targets.
                            { // Might not be as clean, which is why we check patterns above.
                                printToLog(2, "First argument was invalid, and input has numbers. Throwing generic error.");
                                throwArg1Error(commandCost, true, player);
                            }
                        }
                        else
                        {
                            printToLog(2, "Invalid slot provided, and player has no \"other\" perm. Abort.");
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
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                    }
                    else
                    {
                        printToLog(3, "Found a Pixelmon storage on the player. Moving along.");

                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null || !nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Could not find an egg in the provided slot, or no Pokémon was found. Abort.");
                            src.sendMessage(Text.of("§4Error: §cCould not find an egg in the provided slot."));
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
                                printEggResults(nbt, pokemon, commandCost, player);

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
                                            printEggResults(nbt, pokemon, commandCost, player);

                                            // Keep this below the printEggResults call, or your debug message order will look weird.
                                            printToLog(1, "Checked egg in slot " + slot + ", and took " + costToConfirm + " coins.");
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
                PixelUpgrade.log.info("§6CheckEgg // important: §e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("§3CheckEgg // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2CheckEgg // debug: §a" + inputString);
        }
    }

    private Boolean checkConfigBool(String node)
    {
        if (!CheckEggConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckEggConfig.getInstance().getConfig().getNode(node).getBoolean();
        else
        {
            PixelUpgrade.log.info("§4CheckEgg // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Integer checkConfigInt(String node)
    {
        if (!CheckEggConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckEggConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4CheckEgg // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    // I know, this is a bit ugly. Lots of stuff to pass.
    private void printEggResults(NBTTagCompound nbt, EntityPixelmon pokemon, int cost, Player player)
    {
        int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        int defenceIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        int spAttackIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        int spDefenceIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        int totalIVs = HPIV + attackIV + defenceIV + spAttackIV + spDefenceIV + speedIV;
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
            printToLog(3, "Explicit reveal enabled. Printing full IVs, shiny-ness and other info.");

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

            // Format the IVs for use later, so we can print them.
            String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
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

            player.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)"));
            player.sendMessage(Text.of("§bIVs§f: §a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

            // Get a bunch of data from our GetPokemonInfo utility class.
            ArrayList<String> natureArray =
                    GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE), configSpAtk, configSpDef, configSpeed);
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
            printToLog(3, "Explicit reveal disabled, printing vague status.");

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

        if (pokemon.getEntityData().getBoolean("hadEggChecked") && recheckIsFree && cost > 0)
        {
            if (!isShiny || !explicitReveal)
                player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("§dThis egg has been checked before, so this check was free!"));
        }

        player.sendMessage(Text.of("§7-----------------------------------------------------"));
        pokemon.getEntityData().setBoolean("hadEggChecked", true);
    }
}
