// This was a pain. Nice to have, though.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;

// Local imports.
import rs.expand.pixelupgrade.utilities.PokemonMethods;
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

// TODO: Add ability showing. Thanks for the idea, Mikirae.
public class ShowStats implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean showNicknames, showEVs, showExtraInfo, showCounts, clampBadNicknames, notifyBadNicknames;

    // Set up some more variables for internal use.
    private boolean gotExternalConfigError = false, outdatedAltCooldownInSeconds = false;
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("ShowStats", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (cooldownInSeconds == null)
                nativeErrorArray.add("cooldownInSeconds");
            if (altCooldownInSeconds == null && configVersion != null && configVersion >= 400)
                nativeErrorArray.add("altCooldownInSeconds");
            if (showNicknames == null)
                nativeErrorArray.add("showNicknames");
            if (showEVs == null)
                nativeErrorArray.add("showEVs");
            if (showExtraInfo == null)
                nativeErrorArray.add("showExtraInfo");
            if (showCounts == null)
                nativeErrorArray.add("showCounts");
            if (clampBadNicknames == null)
                nativeErrorArray.add("clampBadNicknames");
            if (notifyBadNicknames == null)
                nativeErrorArray.add("notifyBadNicknames");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            // Also get some stuff from PixelUpgrade.conf.
            final List<String> mainConfigErrorArray = new ArrayList<>();
            if (configVersion == null)
                mainConfigErrorArray.add("configVersion");
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
                PrintingMethods.printCommandNodeError("ShowStats", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                PrintingMethods.printMainNodeError("ShowStats", mainConfigErrorArray);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");
                boolean canContinue = true;

                if (altCooldownInSeconds == null)
                {
                    outdatedAltCooldownInSeconds = true;

                    printToLog(0, "Outdated §4/showstats§c config! Check §4latest.log§c startup for help.");
                    printToLog(0, "Running in safe mode. Stuff will work similarly to 3.0.");
                }

                if (showCounts)
                {
                    final List<String> upgradeErrorArray = new ArrayList<>();
                    final List<String> fusionErrorArray = new ArrayList<>();

                    printToLog(2, "Entering external config validation. Errors will be logged.");

                    if (DittoFusion.regularCap == null)
                        fusionErrorArray.add("regularCap");
                    if (DittoFusion.shinyCap == null)
                        fusionErrorArray.add("shinyCap");
                    PrintingMethods.printPartialNodeError("ShowStats", "DittoFusion", fusionErrorArray);

                    if (UpgradeIVs.legendaryAndShinyCap == null)
                        upgradeErrorArray.add("legendaryAndShinyCap");
                    if (UpgradeIVs.legendaryCap == null)
                        upgradeErrorArray.add("legendaryCap");
                    if (UpgradeIVs.regularCap == null)
                        upgradeErrorArray.add("regularCap");
                    if (UpgradeIVs.shinyCap == null)
                        upgradeErrorArray.add("shinyCap");
                    PrintingMethods.printPartialNodeError("ShowStats", "UpgradeIVs", upgradeErrorArray);

                    if (!fusionErrorArray.isEmpty() || !upgradeErrorArray.isEmpty())
                    {
                        printToLog(0, "Found invalid variables in remote config(s). Disabling integration.");

                        // Set up our "got an error" flags. Reset to false if we didn't, so we don't cause issues later.
                        if (!fusionErrorArray.isEmpty() || !upgradeErrorArray.isEmpty())
                            gotExternalConfigError = true;
                    }
                    else
                        printToLog(2, "External config loading is done. Moving on to argument parsing.");
                }

                boolean commandConfirmed = false;
                int slot = 0;
                final long currentTime = System.currentTimeMillis() / 1000; // Grab seconds.

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));

                    printSyntaxHelper(src);
                    PrintingMethods.checkAndAddFooter(false, commandCost, src);

                    canContinue = false;
                }
                else
                {
                    final String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));

                        printSyntaxHelper(src);
                        PrintingMethods.checkAndAddFooter(false, commandCost, src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    final Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        final PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        final NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean(NbtKeys.IS_EGG))
                        {
                            printToLog(1, "Tried to show off an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            // TODO: Pull this whole thing's position in-line with /timedheal and /timedhatch?
                            printToLog(2, "Checking if player is (still?) on a cooldown.");

                            final UUID playerUUID = ((Player) src).getUniqueId(); // why is the "d" in "Id" lowercase :(

                            if (!src.hasPermission("pixelupgrade.command.bypass.showstats") && cooldownMap.containsKey(playerUUID))
                            {
                                final boolean hasAltPerm = src.hasPermission("pixelupgrade.command.altcooldown.showstats");
                                final long timeDifference = currentTime - cooldownMap.get(playerUUID);
                                final long timeRemaining;

                                if (!outdatedAltCooldownInSeconds && hasAltPerm)
                                {
                                    printToLog(2, "Player has the alternate cooldown permission.");
                                    timeRemaining = altCooldownInSeconds - timeDifference;
                                }
                                else
                                {
                                    printToLog(2, "Player has the normal cooldown permission.");
                                    timeRemaining = cooldownInSeconds - timeDifference;
                                }

                                if (!outdatedAltCooldownInSeconds && hasAltPerm && cooldownMap.get(playerUUID) > currentTime - altCooldownInSeconds ||
                                        !hasAltPerm && cooldownMap.get(playerUUID) > currentTime - cooldownInSeconds)
                                {
                                    if (timeRemaining == 1)
                                    {
                                        printToLog(1, "§3" + src.getName() + "§b has to wait §3one §bmore second. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cYou must wait §4one §cmore second. You can do this!"));
                                    }
                                    else
                                    {
                                        printToLog(1, "§3" + src.getName() + "§b has to wait another §3" +
                                                timeRemaining + "§b seconds. Exit.");

                                        if (timeRemaining > 60)
                                        {
                                            src.sendMessage(Text.of("§4Error: §cYou must wait another §4" +
                                                    ((timeRemaining / 60) + 1) + "§c minutes."));
                                        }
                                        else
                                        {
                                            src.sendMessage(Text.of("§4Error: §cYou must wait another §4" +
                                                    timeRemaining + "§c seconds."));
                                        }
                                    }

                                    canContinue = false;
                                }
                            }

                            if (canContinue)
                            {
                                if (economyEnabled && commandCost > 0)
                                {
                                    final BigDecimal costToConfirm = new BigDecimal(commandCost);

                                    if (commandConfirmed)
                                    {
                                        final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(playerUUID);

                                        if (optionalAccount.isPresent())
                                        {
                                            final UniqueAccount uniqueAccount = optionalAccount.get();
                                            final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                        costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                printToLog(1, "Showing off slot §3" + slot +
                                                        "§b, and taking §3" + costToConfirm + "§b coins.");

                                                cooldownMap.put(playerUUID, currentTime);
                                                checkAndShowStats(nbt, (Player) src);
                                            }
                                            else
                                            {
                                                final BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                                        economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                                printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                                        "§b, and we're lacking §3" + balanceNeeded);
                                                src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded +
                                                        "§c more coins to do this."));
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
                                            src.sendMessage(Text.of("§6Warning: §eShowing off a Pokémon's stats costs §6one §ecoin."));
                                        else
                                        {
                                            src.sendMessage(Text.of("§6Warning: §eShowing off a Pokémon's stats costs §6" +
                                                    costToConfirm + "§e coins."));
                                        }

                                        src.sendMessage(Text.EMPTY);
                                        src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                                    }
                                }
                                else
                                {
                                    if (economyEnabled)
                                    {
                                        printToLog(1, "Showing off slot §3" + slot +
                                                "§b. Config price is §30§b, taking nothing.");
                                    }
                                    else
                                    {
                                        printToLog(1, "Showing off slot §3" + slot +
                                                "§b. No economy, so we skipped eco checks.");
                                    }

                                    cooldownMap.put(playerUUID, currentTime);
                                    checkAndShowStats(nbt, (Player) src);
                                }
                            }
                        }
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printSyntaxHelper(final CommandSource src)
    {
        if (economyEnabled && commandCost != 0)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }

    private void checkAndShowStats(final NBTTagCompound nbt, final Player player)
    {
        // Set up IVs and matching math.
        final int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        final int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        final int defenseIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        final int spAttIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        final int spDefIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        final int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        final BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
        final BigDecimal percentIVs = totalIVs.multiply(new BigDecimal("100")).divide(new BigDecimal("186"),
                2, BigDecimal.ROUND_HALF_UP);

        // Format the IVs for use later, so we can print them.
        String ivs1 = String.valueOf(HPIV + " §2" + shortenedHP + statSeparator);
        String ivs2 = String.valueOf(attackIV + " §2" + shortenedAttack + statSeparator);
        String ivs3 = String.valueOf(defenseIV + " §2" + shortenedDefense + statSeparator);
        String ivs4 = String.valueOf(spAttIV + " §2" + shortenedSpecialAttack + statSeparator);
        String ivs5 = String.valueOf(spDefIV + " §2" + shortenedSpecialDefense + statSeparator);
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

        // Set up for our anti-cheat notifier.
        boolean nicknameTooLong = false;

        // Get a bunch of data from our PokemonMethods utility class. Used for messages, later on.
        final List<String> natureArray = PokemonMethods.getNatureStrings(nbt.getInteger(NbtKeys.NATURE));
        final String natureName = natureArray.get(0).toLowerCase();
        final String plusVal = natureArray.get(1);
        final String minusVal = natureArray.get(2);

        // Some of this logic could be in a more limited scope, but it's more convenient to do it now.
        final String name = nbt.getString("Name");
        final String formattedName = "§6" + name;

        // Grab a gender string.
        final String genderString;
        switch (nbt.getInteger(NbtKeys.GENDER))
        {
            case 0:
                genderString = "is §2male§a."; break;
            case 1:
                genderString = "is §2female§a."; break;
            default:
                genderString = "has §2no gender§a.";
        }

        // Grab a growth string.
        final String sizeString;
        switch (nbt.getInteger(NbtKeys.GROWTH))
        {
            case 0:
                sizeString = " is §2a pygmy§a."; break;
            case 1:
                sizeString = " is §2a runt§a."; break;
            case 2:
                sizeString = " is §2small§a."; break;
            case 3:
                sizeString = " is §2ordinary§a."; break;
            case 4:
                sizeString = " is §2huge§a."; break;
            case 5:
                sizeString = " is §2giant§a."; break;
            case 6:
                sizeString = " is §2enormous§a."; break;
            case 7:
                sizeString = " is §2§nginormous§r§a."; break; // NOW with fancy underlining!
            case 8:
                sizeString = " is §2§omicroscopic§r§a."; break; // NOW with fancy italicization!
            default:
                sizeString = "'s size is §2unknown§a.";
        }

        // These always get added to printing, but are filled in only when necessary.
        String shinyString = "", nameAdditionString = "";

        // Set up nickname stuff.
        String nickname = nbt.getString(NbtKeys.NICKNAME);
        if (nickname.length() > 11)
        {
            printToLog(1, "Found a nickname over the 11-char limit. Player may be cheating?");

            if (clampBadNicknames)
                nickname = nickname.substring(0, 11);

            nicknameTooLong = true;
        }

        // Alter our earlier strings if necessary.
        if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            shinyString = "§6§lshiny §r";
        if (showNicknames)
        {
            if (nickname != null && !nickname.isEmpty() && !nickname.equals(name))
                nameAdditionString = "§e, nicknamed §6" + nickname + "§e:";
        }

        // Do the setup for our nature String separately, as it's a bit more involved.
        final String natureString;
        if (nbt.getInteger(NbtKeys.NATURE) >= 0 && nbt.getInteger(NbtKeys.NATURE) <= 4)
            natureString = "is §2" + natureName + "§a, with well-balanced stats.";
        else if (nbt.getInteger(NbtKeys.NATURE) < 0 || nbt.getInteger(NbtKeys.NATURE) > 24)
            natureString = "has an §2unknown §anature...";
        else
            natureString = "is §2" + natureName + "§a, boosting §2" + plusVal + " §aand cutting §2" + minusVal + "§a.";

        // Populate our ArrayList. Every entry will be its own line. May be a bit hacky, but it'll do.
        final List<String> hovers = new ArrayList<>();
        hovers.add("§eStats of §6" + player.getName() + "§e's " + shinyString + formattedName + nameAdditionString);
        hovers.add("");
        hovers.add("§bCurrent IVs§f:");
        hovers.add("➡ §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)");
        hovers.add("➡ §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6);

        if (showEVs)
        {
            // Rinse and repeat the earlier IV code for EVs.
            final int HPEV = nbt.getInteger(NbtKeys.EV_HP);
            final int attackEV = nbt.getInteger(NbtKeys.EV_ATTACK);
            final int defenseEV = nbt.getInteger(NbtKeys.EV_DEFENCE);
            final int spAttEV = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
            final int spDefEV = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
            final int speedEV = nbt.getInteger(NbtKeys.EV_SPEED);
            final BigDecimal totalEVs = BigDecimal.valueOf(HPEV + attackEV + defenseEV + spAttEV + spDefEV + speedEV);
            final BigDecimal percentEVs = totalEVs.multiply(new BigDecimal("100")).divide(new BigDecimal("510"),
                    2, BigDecimal.ROUND_HALF_UP);

            // Also format the strings for EVs.
            String evs1 = String.valueOf(HPEV + " §2" + shortenedHP + statSeparator);
            String evs2 = String.valueOf(attackEV + " §2" + shortenedAttack + statSeparator);
            String evs3 = String.valueOf(defenseEV + " §2" + shortenedDefense + statSeparator);
            String evs4 = String.valueOf(spAttEV + " §2" + shortenedSpecialAttack + statSeparator);
            String evs5 = String.valueOf(spDefEV + " §2" + shortenedSpecialDefense + statSeparator);
            String evs6 = String.valueOf(speedEV + " §2" + shortenedSpeed);

            if (HPEV > 251)
                evs1 = String.valueOf("§o") + evs1;
            if (attackEV > 251)
                evs2 = String.valueOf("§o") + evs2;
            if (defenseEV > 251)
                evs3 = String.valueOf("§o") + evs3;
            if (spAttEV > 251)
                evs4 = String.valueOf("§o") + evs4;
            if (spDefEV > 251)
                evs5 = String.valueOf("§o") + evs5;
            if (speedEV > 251)
                evs6 = String.valueOf("§o") + evs6;

            hovers.add("§bCurrent EVs§f:");
            hovers.add("➡ §a" + totalEVs + "§f/§a510§f (§a" + percentEVs + "%§f)");
            hovers.add("➡ §a" + evs1 + evs2 + evs3 + evs4 + evs5 + evs6);
        }

        if (showExtraInfo)
        {
            hovers.add("§bExtra info§f:");
            hovers.add("➡ §aThis Pokémon" + sizeString);
            hovers.add("➡ §aIt " + genderString);
            hovers.add("➡ §aIt " + natureString);

            if (name.equals("Mew"))
            {
                final int cloneCount = nbt.getInteger(NbtKeys.STATS_NUM_CLONED);

                if (cloneCount == 0)
                    hovers.add("➡ §aCloning has not yet been attempted.");
                else
                    hovers.add("➡ §aCloning has been attempted §2" + cloneCount + "§f/§23 §atimes.");
            }
            else if (name.equals("Azelf") || name.equals("Mesprit") || name.equals("Uxie"))
            {
                final int enchantCount = nbt.getInteger(NbtKeys.STATS_NUM_ENCHANTED);
                final int maxEnchants = PixelmonConfig.getConfig().getNode("General", "lakeTrioMaxEnchants").getInt();

                if (enchantCount == 0)
                    hovers.add("➡ §aIt has not enchanted any rubies yet.");
                else
                    hovers.add("➡ §aIt has enchanted §2" + enchantCount + "§f/§2" + maxEnchants + " §arubies.");
            }
        }

        if (showCounts && !gotExternalConfigError)
        {
            hovers.add("");

            final String introString;
            final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

            if (name.equals("Ditto"))
            {
                final int fuseCount = pokemon.getEntityData().getInteger("fuseCount"), fusionCap;

                if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                    fusionCap = DittoFusion.shinyCap;
                else
                    fusionCap = DittoFusion.regularCap;

                introString = "§eThis " + shinyString + "§6Ditto §e";

                if (fuseCount != 0 && fuseCount < fusionCap)
                    hovers.add(introString + "has been fused §6" + fuseCount + "§e/§6" + fusionCap + " §etimes.");
                else if (fuseCount == 0 && fuseCount < fusionCap)
                    hovers.add(introString + "has not yet been fused.");
                else
                    hovers.add(introString + "has hit the fusion limit!");
            }
            else
            {
                final int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount"), upgradeCap;
                final boolean isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

                if (nbt.getInteger(NbtKeys.IS_SHINY) == 1 && isLegendary)
                    upgradeCap = UpgradeIVs.legendaryAndShinyCap;
                else if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                    upgradeCap = UpgradeIVs.shinyCap;
                else if (isLegendary)
                    upgradeCap = UpgradeIVs.legendaryCap;
                else
                    upgradeCap = UpgradeIVs.regularCap;

                if (isLegendary)
                    introString = "§eThis " + shinyString + "§6§llegendary§r §e";
                else
                    introString = "§eThis " + shinyString + "Pokémon ";

                if (upgradeCount != 0 && upgradeCount < upgradeCap)
                    hovers.add(introString + "has been upgraded §6" + upgradeCount + "§e/§6" + upgradeCap + " §etimes.");
                else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                    hovers.add(introString + "has not yet been upgraded.");
                else
                    hovers.add(introString + "has hit the upgrade limit!");
            }
        }

        // Put every String in our ArrayList on its own line, and reset formatting.
        final Text toPrint = Text.of(String.join("\n§r", hovers));

        // FIXME: This message can be too long with long player names and shiny Pokémon. Not a big deal, but nice polish?
        // Format our chat messages.
        final String ivHelper = "§6" + player.getName() + "§e is showing off a " +
                shinyString + formattedName + "§e, hover for info.";

        // Set up our hover.
        final Text ivBuilder = Text.builder(ivHelper)
                .onHover(TextActions.showText(toPrint))
                .build();

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
        MessageChannel.TO_PLAYERS.send(ivBuilder);

        // If our anti-cheat caught something, notify people with the correct permissions here.
        if (notifyBadNicknames && nicknameTooLong)
        {
            // Add a space to avoid clutter.
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.EMPTY);

            // Print our warnings.
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§4Staff only: §cPokémon's nickname exceeds the 11 character limit."));
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§cSome Pixelmon cheat mods enable longer names, often silently."));
            MessageChannel.permission("pixelupgrade.notify.staff.showstats").send(Text.of(
                    "§cIf sidemods aren't responsible, this player may be cheating!"));
        }

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
    }
}
