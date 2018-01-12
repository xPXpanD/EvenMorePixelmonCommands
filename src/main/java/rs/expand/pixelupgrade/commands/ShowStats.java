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
    public static Integer cooldownInSeconds;
    public static Boolean showCounts;
    public static Boolean showNicknames;
    public static Boolean clampBadNicknames;
    public static Boolean notifyBadNicknames;
    public static Boolean showExtraInfo;
    public static Integer commandCost;

    // Set up some more variables for internal use.
    private boolean gotExternalConfigError = false;
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.doPrint("ShowStats", false, debugNum, inputString); }

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
                CommonMethods.printNodeError("ShowStats", nativeErrorArray, 1);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                CommonMethods.printNodeError("PixelUpgrade", mainConfigErrorArray, 0);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §6" + src.getName() + "§e. Starting!");
                boolean canContinue = true;

                if (showExtraInfo)
                {
                    ArrayList<String> upgradeErrorArray = new ArrayList<>(), fusionErrorArray = new ArrayList<>();

                    printToLog(2, "Entering external config loading. Errors will be logged.");

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

                printToLog(1, "Called by player §6" + src.getName() + "§e. Starting!");

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
                                        printToLog(1, "§6" + src.getName() + "§e has to wait §6one §emore second. Exit.");
                                        src.sendMessage(Text.of("§4Error: §cYou must wait §4one §cmore second. You can do this!"));
                                    }
                                    else
                                    {
                                        printToLog(1, "§6" + src.getName() + "§e has to wait another §6" +
                                                timeRemaining + "§e seconds. Exit.");
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
                                                printToLog(1, "Showing off slot §6" + slot +
                                                        "§e, and taking §6" + costToConfirm + "§e coins.");
                                                cooldownMap.put(playerUUID, currentTime);
                                                checkAndShowStats(nbt, player);
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                                printToLog(1, "Not enough coins! Cost: §6" +
                                                        costToConfirm + "§e, lacking: §6" + balanceNeeded);
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

                                        src.sendMessage(Text.of("§6Warning: §eShowing off a Pokémon's stats costs §6" +
                                                costToConfirm + "§e coins."));
                                        src.sendMessage(Text.of("§2Ready? Type: §a" + commandAlias + " " + slot + " -c"));
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Showing off slot §6" + slot + "§e. Config price is §60§e, taking nothing.");
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
            printToLog(0,"This command cannot run from the console or command blocks.");

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
            src.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <slot, 1-6>"));
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
            ivs4 = String.valueOf(spAttIV + " §2" + shortenedSpecialAttack + " §f|§a ");
        else
            ivs4 = String.valueOf("§l" + spAttIV + " §2" + shortenedSpecialAttack + " §r§f|§a ");

        if (spDefIV < 31)
            ivs5 = String.valueOf(spDefIV + " §2" + shortenedSpecialDefense + " §f|§a ");
        else
            ivs5 = String.valueOf("§l" + spDefIV + " §2" + shortenedSpecialDefense + " §r§f|§a ");

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
            MessageChannel.TO_PLAYERS.send(Text.of(startString + nicknameString + "§f (§e" +
                    genderCharacter + "§r)"));
        else if (!nickname.equals("") && showNicknames && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            MessageChannel.TO_PLAYERS.send(Text.of(startString + nicknameString + "§f (§e§lshiny§r §e" +
                    genderCharacter + "§r)"));
        else if (nickname.equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
            MessageChannel.TO_PLAYERS.send(Text.of(startString + "§f (§e§lshiny§r §e" +
                    genderCharacter + "§r)"));
        else
            MessageChannel.TO_PLAYERS.send(Text.of(startString + "§f (§e" +
                    genderCharacter + "§r)"));

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

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
    }
}
