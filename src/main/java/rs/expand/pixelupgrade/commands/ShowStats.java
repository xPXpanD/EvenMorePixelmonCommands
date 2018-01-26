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
    public static Boolean compactMode, showCounts, showNicknames, clampBadNicknames, notifyBadNicknames, showExtraInfo;

    // Set up some more variables for internal use.
    private boolean gotExternalConfigError = false, outdatedCompactMode = false, outdatedAltCooldownInSeconds = false;
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
            if (showCounts == null)
                nativeErrorArray.add("showCounts");
            if (showNicknames == null)
                nativeErrorArray.add("showNicknames");
            if (clampBadNicknames == null)
                nativeErrorArray.add("clampBadNicknames");
            if (notifyBadNicknames == null)
                nativeErrorArray.add("notifyBadNicknames");
            if (showExtraInfo == null)
                nativeErrorArray.add("showExtraInfo");
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
            else if (compactMode == null && configVersion >= 310 || altCooldownInSeconds == null && configVersion >= 310)
            {
                // These are new 3.1 features. Run a separate check, so we can fail gracefully if need be.
                if (compactMode == null)
                    nativeErrorArray.add("compactMode");
                if (altCooldownInSeconds == null)
                    nativeErrorArray.add("altCooldownInSeconds");

                CommonMethods.printCommandNodeError("ShowStats", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                CommonMethods.printMainNodeError("PixelUpgrade", mainConfigErrorArray);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");
                boolean canContinue = true;

                if (compactMode == null || altCooldownInSeconds == null)
                {
                    if (compactMode == null)
                        outdatedCompactMode = true;
                    if (altCooldownInSeconds == null)
                        outdatedAltCooldownInSeconds = true;

                    printToLog(0, "Outdated §4/showstats§c config! Check §4latest.log§c startup for help.");
                    printToLog(0, "Running in safe mode. Stuff will work the way it did in 3.0.");
                }

                if (outdatedCompactMode && showCounts || !outdatedCompactMode && !compactMode && showCounts)
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
                    if (UpgradeIVs.babyCap == null)
                        upgradeErrorArray.add("babyCap");
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
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
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
                                            printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. May be a bug?");
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
        BigDecimal percentIVs =
                totalIVs.multiply(new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

        // Set up for our anti-cheat notifier.
        boolean nicknameTooLong = false;

        // Get a bunch of data from our GetPokemonInfo utility class. Used for messages, later on.
        ArrayList<String> natureArray = GetPokemonInfo.getNatureStrings(nbt.getInteger(NbtKeys.NATURE));
        String natureName = natureArray.get(0);
        String plusVal = natureArray.get(1);
        String minusVal = natureArray.get(2);
        String growthName = GetPokemonInfo.getGrowthName(nbt.getInteger(NbtKeys.GROWTH));
        String genderCharacter = GetPokemonInfo.getGenderCharacter(nbt.getInteger(NbtKeys.GENDER));

        // Some of this logic could be in a more limited scope, but it's more convenient to do it now.
        String startString = "§6" + player.getName() + "§e is showing off their ";
        String name = "§6" + nbt.getString("Name");
        String nickname = nbt.getString("Nickname");
        if (nickname.length() > 11 && notifyBadNicknames)
            nicknameTooLong = true;

        if (!outdatedCompactMode && compactMode)
        {
            // Grab a gender string from GetPokemonInfo. Returns a blank ("") string if the Pokémon is ungendered.
            String gender = GetPokemonInfo.getGender(nbt.getInteger(NbtKeys.GENDER));

            if (nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                MessageChannel.TO_PLAYERS.send(Text.of("§7--- " + startString + gender + "§lshiny§r §e" + name + "§7 ---"));
            else
                MessageChannel.TO_PLAYERS.send(Text.of(" §7---" + startString + gender + name + "§7 ---"));

            // Format some IV strings for use later, so we can print them.
            String ivHelper;
            if (totalIVs.compareTo(new BigDecimal(186)) > 0) // totalIVs is greater than 186
                ivHelper = "§aThis Pokémon has §2" + totalIVs + "§a out of §2186§a IVs, hover over for more info.";
            else
                ivHelper = "§aThis Pokémon has §2" + totalIVs + "§a IVs, hover over for more info.";

            String HPString = "§2Health IVs§f: §a";
            String attackString = "§2Attack IVs§f: §a";
            String defenseString = "§2Defense IVs§f: §a";
            String spAttString = "§2Special Attack IVs§f: §a";
            String spDefString = "§2Special Defense IVs§f: §a";
            String speedString = "§2Speed IVs§f: §a";

            if (HPIV > 30)
                HPString = HPString + String.valueOf("§o");
            if (attackIV > 30)
                attackString = attackString + String.valueOf("§o");
            if (defenseIV > 30)
                defenseString = defenseString + String.valueOf("§o");
            if (spAttIV > 30)
                spAttString = spAttString + String.valueOf("§o");
            if (spDefIV > 30)
                spDefString = spDefString + String.valueOf("§o");
            if (speedIV > 30)
                speedString = speedString + String.valueOf("§o");

            ArrayList<String> hovers = new ArrayList<>();
            hovers.add(HPString + HPIV + "\n" + attackString + attackIV + "\n" + defenseString + defenseIV +
                    "\n" + spAttString + spAttIV + "\n" + spDefString + spDefIV + "\n" + speedString + speedIV);

            Text ivBuilder = Text.builder(ivHelper)
                    .onHover(TextActions.showText(Text.of(hovers.get(0))))
                    .build();

            MessageChannel.TO_PLAYERS.send(ivBuilder);
        }
        else
        {
            // Format the last few bits and print!
            MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));

            if (nicknameTooLong && clampBadNicknames)
                nickname = nickname.substring(0, 11);
            String nicknameString = "§e, \"§6" + nickname + "§e\"!";

            if (!nickname.equals("") && showNicknames && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                MessageChannel.TO_PLAYERS.send(Text.of(startString + name + nicknameString + "§f (§e" +
                        genderCharacter + "§r)"));
            else if (!nickname.equals("") && showNicknames && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                MessageChannel.TO_PLAYERS.send(Text.of(startString + name + nicknameString + "§f (§e§lshiny§r §e" +
                        genderCharacter + "§r)"));
            else if (nickname.equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                MessageChannel.TO_PLAYERS.send(Text.of(startString + name + "§f (§e§lshiny§r §e" +
                        genderCharacter + "§r)"));
            else
                MessageChannel.TO_PLAYERS.send(Text.of(startString + name + "§f (§e" +
                        genderCharacter + "§r)"));

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

            // Check and show whether the Pokémon can be upgraded/fused further, if enabled in config.
            if (showCounts && !gotExternalConfigError)
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
                        fusionCap = DittoFusion.shinyCap; // Shiny cap.
                    }
                    else
                    {
                        startString = "§eThis §6Ditto §e";
                        fusionCap = DittoFusion.regularCap; // Regular cap.
                    }

                    if (fuseCount != 0 && fuseCount < fusionCap)
                        MessageChannel.TO_PLAYERS.send(Text.of(startString + "has been fused §6" +
                                fuseCount + "§e/§6" + fusionCap + " §etimes."));
                    else if (fuseCount == 0 && fuseCount < fusionCap)
                        MessageChannel.TO_PLAYERS.send(Text.of(startString + "can be fused §6" +
                                fusionCap + "§e more times."));
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
                        upgradeCap = UpgradeIVs.legendaryAndShinyCap; // Legendary + shiny cap.
                    }
                    else if (isShiny)
                    {
                        startString = "§eThis §6shiny Pokémon §e";
                        upgradeCap = UpgradeIVs.shinyCap; // Shiny cap.
                    }
                    else if (isLegendary)
                    {
                        startString = "§eThis §6legendary Pokémon §e";
                        upgradeCap = UpgradeIVs.legendaryCap; // Legendary cap.
                    }
                    else if (isBaby)
                    {
                        startString = "§eThis §6baby Pokémon §e";
                        upgradeCap = UpgradeIVs.babyCap; // Baby cap.
                    }
                    else
                    {
                        startString = "§eThis §6Pokémon §e";
                        upgradeCap = UpgradeIVs.regularCap; // Regular cap.
                    }

                    if (upgradeCount != 0 && upgradeCount < upgradeCap)
                        MessageChannel.TO_PLAYERS.send(Text.of(startString + "has been upgraded §6" +
                                upgradeCount + "§e/§6" + upgradeCap + " §etimes."));
                    else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                        MessageChannel.TO_PLAYERS.send(Text.of(startString + "can be upgraded §6" +
                                upgradeCap + "§e more times."));
                    else
                        MessageChannel.TO_PLAYERS.send(Text.of(startString + "has been fully upgraded!"));
                }
            }
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

        if (!outdatedCompactMode && compactMode)
            MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
    }
}
