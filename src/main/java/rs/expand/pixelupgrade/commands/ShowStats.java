// This was a pain. Nice to have, though.
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
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
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
import rs.expand.pixelupgrade.utilities.CommonMethods;
import rs.expand.pixelupgrade.utilities.GetPokemonInfo;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class ShowStats implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean showNicknames, showEVs, showExtraInfo, showCounts, clampBadNicknames, notifyBadNicknames;

    // Set up some more variables for internal use.
    private boolean gotExternalConfigError = false, outdatedAltCooldownInSeconds = false;
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("ShowStats", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            ArrayList<String> nativeErrorArray = new ArrayList<>();
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
            ArrayList<String> mainConfigErrorArray = new ArrayList<>();
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
                CommonMethods.printCommandNodeError("ShowStats", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                CommonMethods.printMainNodeError("ShowStats", mainConfigErrorArray);
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
                    ArrayList<String> upgradeErrorArray = new ArrayList<>(), fusionErrorArray = new ArrayList<>();

                    printToLog(2, "Entering external config validation. Errors will be logged.");

                    if (DittoFusion.regularCap == null)
                        fusionErrorArray.add("regularCap");
                    if (DittoFusion.shinyCap == null)
                        fusionErrorArray.add("shinyCap");
                    CommonMethods.printPartialNodeError("ShowStats", "DittoFusion", fusionErrorArray);

                    if (UpgradeIVs.legendaryAndShinyCap == null)
                        upgradeErrorArray.add("legendaryAndShinyCap");
                    if (UpgradeIVs.legendaryCap == null)
                        upgradeErrorArray.add("legendaryCap");
                    if (UpgradeIVs.regularCap == null)
                        upgradeErrorArray.add("regularCap");
                    if (UpgradeIVs.shinyCap == null)
                        upgradeErrorArray.add("shinyCap");
                    CommonMethods.printPartialNodeError("ShowStats", "UpgradeIVs", upgradeErrorArray);

                    if (!fusionErrorArray.isEmpty() || !upgradeErrorArray.isEmpty())
                    {
                        printToLog(0, "Found invalid variables in remote config(s). Disabling integration.");

                        // Set up our "got an error" flags. Reset to false if we didn't, so we don't cause issues later.
                        if (!fusionErrorArray.isEmpty() || !upgradeErrorArray.isEmpty())
                            gotExternalConfigError = true;
                    }
                }

                boolean commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    if (commandCost > 0)
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
                    printSyntaxHelper(src);
                    CommonMethods.checkAndAddFooter(commandCost, src);

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

                        if (commandCost > 0)
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printSyntaxHelper(src);
                        CommonMethods.checkAndAddFooter(commandCost, src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(2, "No errors encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

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
                            UUID playerUUID = ((Player) src).getUniqueId(); // why is the "d" in "Id" lowercase :(
                            long currentTime = System.currentTimeMillis();

                            if (!src.hasPermission("pixelupgrade.command.bypass.showstats") && cooldownMap.containsKey(playerUUID))
                            {
                                long cooldownInMillis = cooldownInSeconds * 1000;
                                long timeDifference = currentTime - cooldownMap.get(playerUUID), timeRemaining;

                                if (!outdatedAltCooldownInSeconds && src.hasPermission("pixelupgrade.command.altcooldown.showstats"))
                                    timeRemaining = altCooldownInSeconds - timeDifference / 1000;
                                else
                                    timeRemaining = cooldownInSeconds - timeDifference / 1000;

                                if (cooldownMap.get(playerUUID) > currentTime - cooldownInMillis)
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
                                        src.sendMessage(Text.of("§4Error: §cYou must wait another §4" +
                                                timeRemaining + "§c seconds."));
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
                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
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
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                                        economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
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
                                            src.sendMessage(Text.of("§6Warning: §eShowing off a Pokémon's stats costs §6one §ecoin."));
                                        else
                                        {
                                            src.sendMessage(Text.of("§6Warning: §eShowing off a Pokémon's stats costs §6" +
                                                    costToConfirm + "§e coins."));
                                        }

                                        src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Showing off slot §3" + slot + "§b. Config price is §30§b, taking nothing.");
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

    private void printSyntaxHelper(CommandSource src)
    {
        if (commandCost != 0)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }

    private void checkAndShowStats(NBTTagCompound nbt, Player player)
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

        // Rinse and repeat for EVs.
        int HPEV = nbt.getInteger(NbtKeys.EV_HP);
        int attackEV = nbt.getInteger(NbtKeys.EV_ATTACK);
        int defenseEV = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int spAttEV = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int spDefEV = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int speedEV = nbt.getInteger(NbtKeys.EV_SPEED);
        BigDecimal totalEVs = BigDecimal.valueOf(HPEV + attackEV + defenseEV + spAttEV + spDefEV + speedEV);
        BigDecimal percentEVs = totalEVs.multiply(new BigDecimal("100")).divide(new BigDecimal("510"), 2, BigDecimal.ROUND_HALF_UP);

        // Also format the strings for EVs.
        String evs1 = String.valueOf(HPEV + " §2" + shortenedHP + " §r|§a ");
        String evs2 = String.valueOf(attackEV + " §2" + shortenedAttack + " §r|§a ");
        String evs3 = String.valueOf(defenseEV + " §2" + shortenedDefense + " §r|§a ");
        String evs4 = String.valueOf(spAttEV + " §2" + shortenedSpecialAttack + " §r|§a ");
        String evs5 = String.valueOf(spDefEV + " §2" + shortenedSpecialDefense + " §r|§a ");
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

        // Set up for our anti-cheat notifier.
        boolean nicknameTooLong = false;

        // Get a bunch of data from our GetPokemonInfo utility class. Used for messages, later on.
        // FIXME: Fix gender printing on console. On Windows and possibly other OSes, the character becomes a "?".
        ArrayList<String> natureArray = GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE));
        String natureName = natureArray.get(0).toLowerCase();
        String plusVal;
        String minusVal;

        // Some of this logic could be in a more limited scope, but it's more convenient to do it now.
        String name = nbt.getString("Name");
        String formattedName = "§6" + name;

        // Format our nature Strings.
        plusVal = natureArray.get(1);
        minusVal = natureArray.get(2);

        // Grab a gender string.
        String genderString;
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
        String sizeString;
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
                sizeString = " is §2gigantic§a."; break;
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

        // Alter our earlier strings if necessary.
        if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            shinyString = "§6§lshiny §r";
        if (showNicknames)
        {
            String nickname = nbt.getString(NbtKeys.NICKNAME);

            if (nickname != null && !nickname.isEmpty())
                nameAdditionString = "§e, nicknamed §6" + nickname + "§e:";
        }

        // Do the setup for our nature String separately, as it's a bit more involved.
        String natureString;
        if (nbt.getInteger(NbtKeys.NATURE) >= 0 && nbt.getInteger(NbtKeys.NATURE) <= 4)
            natureString = "is §2" + natureName + "§a, with well-balanced stats.";
        else if (nbt.getInteger(NbtKeys.NATURE) < 0 || nbt.getInteger(NbtKeys.NATURE) > 24)
            natureString = "has an §2unknown §anature...";
        else
            natureString = "is §2" + natureName + "§a, boosting §2" + plusVal + " §aand cutting §2" + minusVal + "§a.";

        // Populate our ArrayList. Every entry will be its own line. May be a bit hacky, but it'll do.
        final ArrayList<String> hovers = new ArrayList<>();
        hovers.add("§eStats of §6" + player.getName() + "§e's " + shinyString + formattedName + nameAdditionString);
        hovers.add("");
        hovers.add("§bCurrent IVs§f:");
        hovers.add("➡ §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)");
        hovers.add("➡ §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6);

        if (showEVs)
        {
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
                int cloneCount = nbt.getInteger(NbtKeys.STATS_NUM_CLONED);

                if (cloneCount == 0)
                    hovers.add("➡ §aCloning has not yet been attempted.");
                else
                    hovers.add("➡ §aCloning has been attempted §2" + cloneCount + "§f/§23 §atimes.");
            }
        }

        if (showCounts && !gotExternalConfigError)
        {
            hovers.add("");

            String introString;
            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

            if (name.equals("Ditto"))
            {
                int fuseCount = pokemon.getEntityData().getInteger("fuseCount"), fusionCap;

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
                int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount"), upgradeCap;
                boolean isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

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
                    introString = "§eThis " + shinyString + "§6Pokémon §e";

                if (upgradeCount != 0 && upgradeCount < upgradeCap)
                    hovers.add(introString + "has been upgraded §6" + upgradeCount + "§e/§6" + upgradeCap + " §etimes.");
                else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                    hovers.add(introString + "has not yet been upgraded.");
                else
                    hovers.add(introString + "has hit the upgrade limit!");
            }
        }

        // Put every String in our ArrayList on its own line, and reset formatting.
        Text toPrint = Text.of(String.join("\n§r", hovers));

        // FIXME: This message can be too long with long player names and shiny Pokémon. Not a big deal, but nice polish?
        // Format our chat messages.
        String ivHelper = "§6" + player.getName() + "§e is showing off a " +
                shinyString + formattedName + "§e, hover for info.";

        // Set up our hover.
        Text ivBuilder = Text.builder(ivHelper)
                .onHover(TextActions.showText(toPrint))
                .build();

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
        MessageChannel.TO_PLAYERS.send(ivBuilder);

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

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
    }
}
