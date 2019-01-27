// This was a pain. Nice to have, though.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import java.math.BigDecimal;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
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
    private boolean /*gotExternalConfigError = false, */outdatedAltCooldownInSeconds = false;
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

                /*if (showCounts)
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
                }*/

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
                    // Get the player's party, and then get the Pokémon in the targeted slot.
                    final Pokemon pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot);

                    if (pokemon == null)
                    {
                        printToLog(1, "No Pokémon data found in slot, probably empty. Exit.");
                        src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                    }
                    else if (pokemon.isEgg())
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
                                            checkAndShowStats(pokemon, (Player) src);
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
                                checkAndShowStats(pokemon, (Player) src);
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

    private void checkAndShowStats(final Pokemon pokemon, final Player player)
    {
        // Set up IVs and matching math.
        final IVStore IVs = pokemon.getIVs();
        final int totalIVs =
                IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
        final int percentIVs = totalIVs * 100 / 186;

        // Format the IVs for use later, so we can print them.
        String ivs1 = String.valueOf(IVs.get(StatsType.HP) + " §2" + shortenedHP + statSeparator);
        String ivs2 = String.valueOf(IVs.get(StatsType.Attack) + " §2" + shortenedAttack + statSeparator);
        String ivs3 = String.valueOf(IVs.get(StatsType.Defence) + " §2" + shortenedDefense + statSeparator);
        String ivs4 = String.valueOf(IVs.get(StatsType.SpecialAttack) + " §2" + shortenedSpecialAttack + statSeparator);
        String ivs5 = String.valueOf(IVs.get(StatsType.SpecialDefence) + " §2" + shortenedSpecialDefense + statSeparator);
        String ivs6 = String.valueOf(IVs.get(StatsType.Speed) + " §2" + shortenedSpeed);

        if (IVs.get(StatsType.HP) > 30)
            ivs1 = String.valueOf("§o") + ivs1;
        if (IVs.get(StatsType.Attack) > 30)
            ivs2 = String.valueOf("§o") + ivs2;
        if (IVs.get(StatsType.Defence) > 30)
            ivs3 = String.valueOf("§o") + ivs3;
        if (IVs.get(StatsType.SpecialAttack) > 30)
            ivs4 = String.valueOf("§o") + ivs4;
        if (IVs.get(StatsType.SpecialDefence) > 30)
            ivs5 = String.valueOf("§o") + ivs5;
        if (IVs.get(StatsType.Speed) > 30)
            ivs6 = String.valueOf("§o") + ivs6;

        // Set up for our anti-cheat notifier.
        boolean nicknameTooLong = false;

        // Get a bunch of important Pokémon stat data.
        final EnumNature nature = pokemon.getNature();
        final EnumGrowth growth = pokemon.getGrowth();
        final String plusVal = '+' + pokemon.getNature().increasedStat.name();
        final String minusVal = '-' + pokemon.getNature().decreasedStat.name();

        // Create a copy of the Pokémon's persistent data for extracting specific NBT info from.
        final NBTTagCompound pokemonNBT = pokemon.getPersistentData();

        // Grab a gender string.
        final String genderString;
        switch (pokemon.getGender())
        {
            case Male:
                genderString = "is §2male§a."; break;
            case Female:
                genderString = "is §2female§a."; break;
            default:
                genderString = "has §2no gender§a.";
        }

        // Grab a growth string.
        final String sizeString;
        switch (growth)
        {
            case Microscopic:
                sizeString = " is §2§omicroscopic§r§a."; break; // NOW with fancy italicization!
            case Pygmy:
                sizeString = " is §2a pygmy§a."; break;
            case Runt:
                sizeString = " is §2a runt§a."; break;
            case Small:
                sizeString = " is §2small§a."; break;
            case Ordinary:
                sizeString = " is §2ordinary§a."; break;
            case Huge:
                sizeString = " is §2huge§a."; break;
            case Giant:
                sizeString = " is §2giant§a."; break;
            case Enormous:
                sizeString = " is §2enormous§a."; break;
            case Ginormous:
                sizeString = " is §2§nginormous§r§a."; break; // NOW with fancy underlining!
            default:
                sizeString = "'s size is §2unknown§a...?";
        }

        // These always get added to printing, but are filled in only when necessary.
        String shinyString = "", nameAdditionString = "";

        // Set up name-related stuff.
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final String baseName = pokemon.getSpecies().getPokemonName();
        String nickname = pokemon.getNickname();
        final String formattedName = "§6" + localizedName;

        // Do the first of two cheating checks. Might catch some less clever cheat tools.
        if (nickname.length() > 11)
        {
            printToLog(1, "Found a nickname over the 11-char limit. Player may be cheating?");

            if (clampBadNicknames)
                nickname = nickname.substring(0, 11);

            nicknameTooLong = true;
        }

        // Alter our earlier strings if necessary.
        if (pokemon.getIsShiny())
            shinyString = "§6§lshiny §r";
        if (showNicknames)
        {
            if (nickname != null && !nickname.isEmpty() && !nickname.equals(localizedName))
                nameAdditionString = "§e, nicknamed §6" + nickname + "§e:";
        }

        // Do the setup for our nature String separately, as it's a bit more involved.
        final String natureString;
        if (nature.index >= 0 && nature.index <= 4)
            natureString = "is §2" + nature.name() + "§a, with well-balanced stats.";
        else if (nature.index < 0 || nature.index > 24)
            natureString = "has an §2unknown §anature...";
        else
            natureString = "is §2" + nature.name() + "§a, boosting §2" + plusVal + " §aand cutting §2" + minusVal + "§a.";

        // Populate our ArrayList. Every entry will be its own line. May be a bit hacky, but it'll do.
        final List<String> hovers = new ArrayList<>();
        hovers.add("§eStats of §6" + player.getName() + "§e's " + shinyString + formattedName + nameAdditionString);
        hovers.add("");
        hovers.add("§bCurrent IVs§f:");
        hovers.add("➡ §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)");
        hovers.add("➡ §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6);

        if (showEVs)
        {
            // Rinse and repeat the earlier IV code for EVs, sort of.
            final EVsStore EVs = pokemon.getEVs();
            final int totalEVs =
                    EVs.get(StatsType.HP) + EVs.get(StatsType.Attack) + EVs.get(StatsType.Defence) +
                    EVs.get(StatsType.SpecialAttack) + EVs.get(StatsType.SpecialDefence) + EVs.get(StatsType.Speed);
            final int percentEVs = totalEVs * 100 / 510;

            // Also format the strings for EVs.
            String evs1 = String.valueOf(EVs.get(StatsType.HP) + " §2" + shortenedHP + statSeparator);
            String evs2 = String.valueOf(EVs.get(StatsType.Attack) + " §2" + shortenedAttack + statSeparator);
            String evs3 = String.valueOf(EVs.get(StatsType.Defence) + " §2" + shortenedDefense + statSeparator);
            String evs4 = String.valueOf(EVs.get(StatsType.SpecialAttack) + " §2" + shortenedSpecialAttack + statSeparator);
            String evs5 = String.valueOf(EVs.get(StatsType.SpecialDefence) + " §2" + shortenedSpecialDefense + statSeparator);
            String evs6 = String.valueOf(EVs.get(StatsType.Speed) + " §2" + shortenedSpeed);

            if (EVs.get(StatsType.HP) > 251)
                evs1 = String.valueOf("§o") + evs1;
            if (EVs.get(StatsType.Attack) > 251)
                evs2 = String.valueOf("§o") + evs2;
            if (EVs.get(StatsType.Defence) > 251)
                evs3 = String.valueOf("§o") + evs3;
            if (EVs.get(StatsType.SpecialAttack) > 251)
                evs4 = String.valueOf("§o") + evs4;
            if (EVs.get(StatsType.SpecialDefence) > 251)
                evs5 = String.valueOf("§o") + evs5;
            if (EVs.get(StatsType.Speed) > 251)
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

            if (baseName.equals("Mew"))
            {
                final int cloneCount = pokemonNBT.getInteger(NbtKeys.STATS_NUM_CLONED);

                if (cloneCount == 0)
                    hovers.add("➡ §aCloning has not yet been attempted.");
                else
                    hovers.add("➡ §aCloning has been attempted §2" + cloneCount + "§f/§23 §atimes.");
            }
            else if (baseName.equals("Azelf") || baseName.equals("Mesprit") || baseName.equals("Uxie"))
            {
                final int enchantCount = pokemonNBT.getInteger(NbtKeys.STATS_NUM_ENCHANTED);
                final int maxEnchants = PixelmonConfig.getConfig().getNode("General", "lakeTrioMaxEnchants").getInt();

                if (enchantCount == 0)
                    hovers.add("➡ §aIt has not enchanted any rubies yet.");
                else
                    hovers.add("➡ §aIt has enchanted §2" + enchantCount + "§f/§2" + maxEnchants + " §arubies.");
            }
        }

        /*if (showCounts && !gotExternalConfigError)
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
                final boolean isLegendary = EnumSpecies.legendaries.contains(nbt.getString("Name"));

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
        }*/

        // Put every String in our ArrayList on its own line, and reset formatting.
        final Text toPrint = Text.of(String.join("\n§r", hovers));

        // Format our chat messages.
        // FIXME: This message can be too long with long player names and shiny Pokémon. Not a big deal, but nice polish?
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
