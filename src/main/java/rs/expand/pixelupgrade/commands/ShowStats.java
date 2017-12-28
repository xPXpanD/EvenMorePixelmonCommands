package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

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
import org.spongepowered.api.text.channel.MessageChannel;

import rs.expand.pixelupgrade.configs.UpgradeIVsConfig;
import rs.expand.pixelupgrade.configs.DittoFusionConfig;
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;
import rs.expand.pixelupgrade.configs.ShowStatsConfig;
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class ShowStats implements CommandExecutor
{
    // Not sure how this works yet, but nicked it from TotalEconomy.
    // Will try to figure this out later, just glad to have this working for now.
    private PixelUpgrade pixelUpgrade;
    public ShowStats(PixelUpgrade pixelUpgrade) { this.pixelUpgrade = pixelUpgrade; }

    // Grab the command's alias.
    private static String alias = null;
    private void getCommandAlias()
    {
        if (!ShowStatsConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ShowStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
            PixelUpgrade.log.info("§4ShowStats // critical: §cConfig variable \"commandAlias\" could not be found!");
    }

    // Set up some variables that we'll be using later on.
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();
    private Boolean showCounts, showNicknames, clampBadNicknames, notifyBadNicknames, showExtraInfo;
    private Integer regularFusionCap, shinyFusionCap, legendaryShinyUpgradeCap;
    private Integer legendaryUpgradeCap, regularUpgradeCap, shinyUpgradeCap, babyUpgradeCap;
    private String shortenedHP, shortenedAttack, shortenedDefense, shortenedSpAtt, shortenedSpDef, shortenedSpeed;

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            boolean canContinue = false;
            boolean presenceCheck = true, fusionCheck = true, upgradeCheck = true, mainConfigCheck = true;

            Integer commandCost = getConfigInt("commandCost");
            Integer cooldownInSeconds = getConfigInt("cooldownInSeconds");
            showCounts = getConfigBool("showCounts");
            showNicknames = getConfigBool("showNicknames");
            clampBadNicknames = getConfigBool("clampBadNicknames");
            notifyBadNicknames = getConfigBool("notifyBadNicknames");
            showExtraInfo = getConfigBool("showExtraInfo");

            // Load up Ditto Fusion config values. Used for showing fusion limits.
            regularFusionCap = getFusionConfigInt("regularCap");
            shinyFusionCap = getFusionConfigInt("shinyCap");

            // Load up UpgradeIVs, too. Sorry for the long names, clarity over style.
            legendaryShinyUpgradeCap = getUpgradeConfigInt("legendaryAndShinyCap");
            legendaryUpgradeCap = getUpgradeConfigInt("legendaryCap");
            regularUpgradeCap = getUpgradeConfigInt("regularCap");
            shinyUpgradeCap = getUpgradeConfigInt("shinyCap");
            babyUpgradeCap = getUpgradeConfigInt("babyCap");

            // And finally, grab the shortened formats from the main config.
            shortenedHP = getMainConfigString("shortenedHealth");
            shortenedAttack = getMainConfigString("shortenedAttack");
            shortenedDefense = getMainConfigString("shortenedDefense");
            shortenedSpAtt = getMainConfigString("shortenedSpecialAttack");
            shortenedSpDef = getMainConfigString("shortenedSpecialDefense");
            shortenedSpeed = getMainConfigString("shortenedSpeed");

            // Set up the command's preferred alias.
            getCommandAlias();

            if (commandCost == null || cooldownInSeconds == null || showCounts == null || showNicknames == null)
                presenceCheck = false;
            if (clampBadNicknames == null || notifyBadNicknames == null || showExtraInfo == null)
                presenceCheck = false;
            if (regularFusionCap == null || shinyFusionCap == null)
                fusionCheck = false;
            if (legendaryShinyUpgradeCap == null || legendaryUpgradeCap == null || regularUpgradeCap == null)
                upgradeCheck = false;
            else if (shinyUpgradeCap == null || babyUpgradeCap == null)
                upgradeCheck = false;
            if (shortenedHP == null || shortenedAttack == null || shortenedDefense == null)
                mainConfigCheck = false;
            else if (shortenedSpAtt == null || shortenedSpDef == null || shortenedSpeed == null)
                mainConfigCheck = false;

            if (!presenceCheck || alias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4ShowStats // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (!mainConfigCheck)
            {
                // Same as above.
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
                printToLog(0, "Please check (or wipe and /pureload) your PixelUpgrade.conf file.");
            }
            else
            {
                if (showCounts && (!fusionCheck || !upgradeCheck))
                { // Check UtilityFunctions for the messages before these ones.
                    printToLog(0, "Integration has been disabled until this is fixed.");
                    printToLog(0, "If need be, remove the file and §4/pureload§c.");

                    showCounts = false;
                }

                canContinue = true;
            }

            if (canContinue)
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                boolean commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    checkAndAddHeader(commandCost, src);
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
                    printCorrectHelper(commandCost, src);
                    checkAndAddFooter(commandCost, src);

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        checkAndAddHeader(commandCost, src);
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printCorrectHelper(commandCost, src);
                        checkAndAddFooter(commandCost, src);

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
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to show off an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            Player player = (Player) src;
                            UUID playerUUID = player.getUniqueId(); // why is the "d" in "Id" lowercase :(
                            long currentTime = System.currentTimeMillis();
                            long cooldownInMillis = cooldownInSeconds * 1000;

                            if (!src.hasPermission("pixelupgrade.command.bypass.showstats") && cooldownMap.containsKey(playerUUID))
                            {
                                long timeDifference = currentTime - cooldownMap.get(playerUUID);
                                long timeRemaining = cooldownInSeconds - timeDifference / 1000; // Stored in milliseconds, so /1000.

                                if (cooldownMap.get(playerUUID) > currentTime - cooldownInMillis)
                                {
                                    if (timeRemaining == 1)
                                    {
                                        printToLog(1, "§4" + src.getName() + "§c has to wait §4one §cmore second. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cYou must wait §4one §cmore second. You can do this!"));
                                    }
                                    else
                                    {
                                        printToLog(1, "§4" + src.getName() + "§c has to wait another §4" + timeRemaining + "§c seconds. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cYou must wait another §4" + timeRemaining + "§c seconds."));
                                    }

                                    canContinue = false;
                                }
                            }

                            if (canContinue)
                            {
                                if (commandCost > 0)
                                {
                                    BigDecimal costToConfirm = new BigDecimal(commandCost);

                                    if (commandConfirmed)
                                    {
                                        Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(playerUUID);

                                        if (optionalAccount.isPresent())
                                        {
                                            UniqueAccount uniqueAccount = optionalAccount.get();
                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.of(EventContext.empty(), pixelUpgrade.getPluginContainer()));

                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                printToLog(1, "Showing off slot " + slot + ", and taking " + costToConfirm + " coins.");
                                                cooldownMap.put(playerUUID, currentTime);
                                                checkAndShowStats(nbt, player);
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

                                        src.sendMessage(Text.of("§6Warning: §eShowing off a Pokémon's stats costs §6" + costToConfirm + "§e coins."));
                                        src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + slot + " -c"));
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Showing off slot " + slot + ". Config price is 0, taking nothing.");
                                    cooldownMap.put(playerUUID, currentTime);
                                    checkAndShowStats(nbt, player);
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

    private void checkAndAddHeader(int cost, CommandSource src)
    {
        if (cost > 0)
        {
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }

    private void checkAndAddFooter(int cost, CommandSource src)
    {
        if (cost > 0)
        {
            src.sendMessage(Text.of(""));
            src.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
            src.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coins."));
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }

    private void printCorrectHelper(int cost, CommandSource src)
    {
        if (cost != 0)
            src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6>"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4ShowStats // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§3ShowStats // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2ShowStats // debug: §a" + inputString);
        }
    }

    private Integer getFusionConfigInt(String node)
    {
        if (!DittoFusionConfig.getInstance().getConfig().getNode(node).isVirtual())
            return DittoFusionConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4ShowStats // critical: §cCan't read remote config variable \"" + node + "\" for /dittofusion!");
            return null;
        }
    }

    private Integer getUpgradeConfigInt(String node)
    {
        if (!UpgradeIVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return UpgradeIVsConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4ShowStats // critical: §cCan't read remote config variable \"" + node + "\" for /upgradeivs!");
            return null;
        }
    }

    private String getMainConfigString(String node)
    {
        if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode(node).isVirtual())
            return PixelUpgradeMainConfig.getInstance().getConfig().getNode(node).getString();
        else
        {
            PixelUpgrade.log.info("§4ShowStats // critical: §cCan't read remote variable \"" + node + "\" from main config!");
            return null;
        }
    }

    private Integer getConfigInt(String node)
    {
        if (!ShowStatsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return ShowStatsConfig.getInstance().getConfig().getNode(node).getInt();
        else
        {
            PixelUpgrade.log.info("§4ShowStats // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private Boolean getConfigBool(String node)
    {
        if (!ShowStatsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return ShowStatsConfig.getInstance().getConfig().getNode(node).getBoolean();
        else
        {
            PixelUpgrade.log.info("§4ShowStats // critical: §cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private void checkAndShowStats(NBTTagCompound nbt, Player player)
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

        // Set up for our anti-cheat notifier.
        boolean nicknameTooLong = false;

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

        // Format the last few bits and print!
        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
        String startString = "§6" + player.getName() + "§e is showing off their §6" + nbt.getString("Name");
        String nickname = nbt.getString("Nickname");
        if (nickname.length() > 11)
        {
            if (clampBadNicknames)
                nickname = nickname.substring(0, 11);

            if (notifyBadNicknames)
                nicknameTooLong = true;
        }
        String nicknameString = "§e, \"§6" + nickname + "§e\"!";

        if (!nickname.equals("") && showNicknames && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
            MessageChannel.TO_PLAYERS.send(Text.of(startString + nicknameString + "§f (§e" + genderCharacter + "§r)"));
        else if (!nickname.equals("") && showNicknames && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            MessageChannel.TO_PLAYERS.send(Text.of(startString + nicknameString + "§f (§e§lshiny§r §e" + genderCharacter + "§r)"));
        else if (nickname.equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            MessageChannel.TO_PLAYERS.send(Text.of(startString + "§f (§e§lshiny§r §e" + genderCharacter + "§r)"));
        else
            MessageChannel.TO_PLAYERS.send(Text.of(startString + "§f (§e" + genderCharacter + "§r)"));

        MessageChannel.TO_PLAYERS.send(Text.of(""));
        MessageChannel.TO_PLAYERS.send(Text.of("§bIVs§f: §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6));

        // Show extra info, which we mostly grabbed from GetPokemonInfo, if enabled.
        if (showExtraInfo)
        {
            String extraInfo1 = String.valueOf("§bTotal§f: §a" + totalIVs + "§f (§a" + percentIVs +
                    "%§f) | §bSize§f: " + growthName + "§f | ");
            String extraInfo2 = String.valueOf("§bNature§f: " + natureName +
                    "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
            MessageChannel.TO_PLAYERS.send(Text.of(extraInfo1 + extraInfo2));
        }

        // If our anti-cheat caught something, notify people with the correct permissions here.
        if (nicknameTooLong)
        {
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(""));
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§cThis Pokémon's nickname is longer than the 11 character limit."));
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§cSome Pixelmon cheat mods enable longer names, often silently."));
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§cIf sidemods aren't responsible, the player may be cheating!"));
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§4(only those with the staff permission can see this warning)"));
        }

        // Check and show whether the Pokémon can be upgraded/fused further, if enabled in config.
        if (showCounts)
        {
            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
            boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
            boolean isDitto = nbt.getString("Name").equals("Ditto");
            MessageChannel.TO_PLAYERS.send(Text.of(""));

            if (isDitto)
            {
                int fuseCount = pokemon.getEntityData().getInteger("fuseCount"), fusionCap;

                if (isShiny)
                {
                    startString = "§eThis §6shiny Ditto §e"; // Adjust for shinyness!
                    fusionCap = shinyFusionCap; // Shiny cap.
                }
                else
                {
                    startString = "§eThis §6Ditto §e";
                    fusionCap = regularFusionCap; // Regular cap.
                }

                if (fuseCount != 0 && fuseCount < fusionCap)
                    MessageChannel.TO_PLAYERS.send(Text.of(startString + "has been fused §6" + fuseCount + "§e/§6" + fusionCap + " §etimes."));
                else if (fuseCount == 0 && fuseCount < fusionCap)
                    MessageChannel.TO_PLAYERS.send(Text.of(startString + "can be fused §6" + fusionCap + "§e more times."));
                else
                    MessageChannel.TO_PLAYERS.send(Text.of(startString + "cannot be fused any further!"));
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
                    MessageChannel.TO_PLAYERS.send(Text.of(startString + "has been upgraded §6" + upgradeCount + "§e/§6" + upgradeCap + " §etimes."));
                else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                    MessageChannel.TO_PLAYERS.send(Text.of(startString + "can be upgraded §6" + upgradeCap + "§e more times."));
                else
                    MessageChannel.TO_PLAYERS.send(Text.of(startString + "has been fully upgraded!"));
            }
        }

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
    }
}
