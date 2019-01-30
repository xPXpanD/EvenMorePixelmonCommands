// This was a pain. Nice to have, though.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.forms.EnumAlolan;
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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import static rs.expand.evenmorepixelmoncommands.EMPC.*;
import static rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods.getShorthand;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

// TODO: Add ability showing. Thanks for the idea, Mikirae.
public class ShowStats implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean showNicknames, showEVs, showExtraInfo, showCounts, clampBadNicknames, notifyBadNicknames,
                          reshowIsFree;

    // Set up some more variables for internal use.
    private String sourceName = this.getClass().getSimpleName();
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();
    /*private boolean gotExternalConfigError = false;*/

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> commandErrorList = new ArrayList<>();
            if (commandAlias == null)
                commandErrorList.add("commandAlias");
            if (cooldownInSeconds == null)
                commandErrorList.add("cooldownInSeconds");
            if (altCooldownInSeconds == null)
                commandErrorList.add("altCooldownInSeconds");
            if (showNicknames == null)
                commandErrorList.add("showNicknames");
            if (showEVs == null)
                commandErrorList.add("showEVs");
            if (showExtraInfo == null)
                commandErrorList.add("showExtraInfo");
            if (showCounts == null)
                commandErrorList.add("showCounts");
            if (clampBadNicknames == null)
                commandErrorList.add("clampBadNicknames");
            if (notifyBadNicknames == null)
                commandErrorList.add("notifyBadNicknames");
            if (commandCost == null)
                commandErrorList.add("commandCost");
            if (reshowIsFree == null)
                commandErrorList.add("reshowIsFree");

            // Also get some stuff from EvenMorePixelmonCommands.conf.
            final List<String> mainConfigErrorArray = new ArrayList<>();
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

            if (!commandErrorList.isEmpty())
            {
                PrintingMethods.printCommandNodeError("ShowStats", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                PrintingMethods.printMainNodeError("ShowStats", mainConfigErrorArray);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                // Start checking whether the player's stuck in a cooldown.
                final long currentTime = System.currentTimeMillis() / 1000; // Grab seconds.
                final UUID playerUUID = ((Player) src).getUniqueId(); // why is the "d" in "Id" lowercase :(
                if (!src.hasPermission("empc.command.bypass.showstats") && cooldownMap.containsKey(playerUUID))
                {
                    final boolean hasAltPerm = src.hasPermission("empc.command.altcooldown.showstats");
                    final long timeDifference = currentTime - cooldownMap.get(playerUUID);
                    final long timeRemaining;

                    if (hasAltPerm)
                        timeRemaining = altCooldownInSeconds - timeDifference;
                    else
                        timeRemaining = cooldownInSeconds - timeDifference;

                    if (hasAltPerm && cooldownMap.get(playerUUID) > currentTime - altCooldownInSeconds ||
                            !hasAltPerm && cooldownMap.get(playerUUID) > currentTime - cooldownInSeconds)
                    {
                        if (timeRemaining == 1)
                            printLocalError(src, "§4Error: §cYou must wait §4one §cmore second. You can do this!", true);
                        else if (timeRemaining > 60)
                            printLocalError(src, "§4Error: §cYou must wait another §4" + ((timeRemaining / 60) + 1) + "§c minutes.", true);
                        else
                            printLocalError(src, "§4Error: §cYou must wait another §4" + timeRemaining + "§c seconds.", true);

                        return CommandResult.success();
                    }
                }

                boolean commandConfirmed = false;
                final int slot;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printLocalError(src, "§4Error: §cNo arguments found. Please provide a slot.", false);
                    return CommandResult.empty();
                }
                else
                {
                    final String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid slot value. Valid values are 1-6.", false);
                        return CommandResult.empty();
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                // Get the player's party, and then get the Pokémon in the targeted slot.
                final Pokemon pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot - 1);

                if (pokemon == null)
                {
                    src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                    return CommandResult.empty();
                }
                else if (pokemon.isEgg()) // TODO: Maybe replace with (configurable) vague stat showing. CheckStats style.
                {
                    src.sendMessage(Text.of("§4Error: §cThat's an egg. Go hatch it, first."));
                    return CommandResult.empty();
                }
                else
                {
                    // Should this show-off be free? Only passes if we receive a "hey we showed this off previously".
                    final boolean pokemonIsFree =
                            reshowIsFree && pokemon.getPersistentData().getBoolean("wasShown");

                    if (economyEnabled && commandCost > 0 && !pokemonIsFree)
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
                                    printSourcedMessage(sourceName, "Showing off slot §3" + slot +
                                            "§b, and taking §3" + costToConfirm + "§b coins.");

                                    cooldownMap.put(playerUUID, currentTime);
                                    checkAndShowStats(pokemon, (Player) src);
                                }
                                else
                                {
                                    final BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                            economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                    src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded +
                                            "§c more coins to do this."));
                                }
                            }
                            else
                            {
                                printSourcedError(sourceName, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                                src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                            }
                        }
                        else
                        {
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));

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
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        }
                    }
                    else
                    {
                        cooldownMap.put(playerUUID, currentTime);
                        checkAndShowStats(pokemon, (Player) src);
                    }
                }
            }
        }
        else
            printSourcedError(sourceName,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input, final boolean hitCooldown)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));

        if (!hitCooldown)
        {
            if (economyEnabled && commandCost != 0)
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
            else
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
        }

        if (economyEnabled && commandCost > 0)
        {
            src.sendMessage(Text.EMPTY);

            if (commandCost == 1)
                src.sendMessage(Text.of("§eConfirming will cost you §6one §ecoin."));
            else
                src.sendMessage(Text.of("§eConfirming will cost you §6" + commandCost + "§e coins."));
        }

        src.sendMessage(Text.of("§5-----------------------------------------------------"));
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

        // Create a copy of the Pokémon's persistent data for extracting specific NBT info from.
        final NBTTagCompound pokemonNBT = pokemon.getPersistentData();

        // Set up name-related stuff.
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String formattedName = "§6" + localizedName;
        String nickname = pokemon.getNickname();

        // These always get added to printing, but are filled in only when necessary.
        final String shinyString = pokemon.isShiny() ? "§6§lshiny §r" : "";
        final String alolanString = pokemon.getFormEnum() == EnumAlolan.ALOLAN ? "§6Alolan " : "";

        // Do the first of two cheating checks. Might catch some less clever cheat tools.
        if (nickname != null && !nickname.isEmpty() && nickname.length() > 11)
        {
            printSourcedMessage(sourceName,
                    "Found a nickname over the 11-char limit. Player " + player.getName() + " may be cheating?");

            if (clampBadNicknames)
                nickname = nickname.substring(0, 11);

            nicknameTooLong = true;
        }

        // Figure out what to add name-wise.
        final String nameAdditionString;
        if (showNicknames && nickname != null && !nickname.isEmpty() && !nickname.equals(localizedName))
            nameAdditionString = "§e, nicknamed §6" + nickname + "§e:";
        else
            nameAdditionString = "§e:";

        // Populate our ArrayList. Every entry will be its own line. May be a bit hacky, but it'll do.
        final List<String> hovers = new ArrayList<>();
        hovers.add("§eStats of §6" + player.getName() + "§e's level " + pokemon.getLevel() + " " +
                shinyString + alolanString + formattedName + nameAdditionString);
        hovers.add("");
        hovers.add("§bCurrent IVs§f:");
        hovers.add("➡ §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)");
        hovers.add("➡ §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6);

        if (showEVs)
        {
            // Rinse and repeat the earlier IV code for EVs, sort of.
            final EVStore EVs = pokemon.getEVs();
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
            // Get a bunch of important Pokémon stat data.
            final EnumNature nature = pokemon.getNature();
            final EnumGrowth growth = pokemon.getGrowth();
            final String plusVal = getShorthand(nature.increasedStat);
            final String minusVal = getShorthand(nature.decreasedStat);

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

            // Do the setup for our nature String separately, as it's a bit more involved.
            final String natureString;
            if (nature.index >= 0 && nature.index <= 4)
                natureString = "is §2" + nature.name().toLowerCase() + "§a, with well-balanced stats.";
            else if (nature.index < 0 || nature.index > 24)
                natureString = "has an §2unknown §anature...";
            else
            {
                natureString = "is §2" + nature.name().toLowerCase() +
                        "§a, boosting §2" + plusVal + " §aand cutting §2" + minusVal + "§a.";
            }

            hovers.add("§bExtra info§f:");
            hovers.add("➡ §aThis Pokémon" + sizeString);
            hovers.add("➡ §aIt " + genderString);
            hovers.add("➡ §aIt " + natureString);
            hovers.add("➡ §aIt has the \"§2" + pokemon.getAbility().getLocalizedName() + "§a\" ability.");

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

        if (economyEnabled && commandCost > 0 && reshowIsFree)
        {
            if (pokemon.getPersistentData().getBoolean("wasShown"))
            {
                player.sendMessage(Text.EMPTY);
                player.sendMessage(Text.of("§dThis Pokémon was shown off before, so this one was free!"));
            }
            else // Write the "free rechecks" tag to the Pokémon for future use.
                pokemon.getPersistentData().setBoolean("wasShown", true);
        }

        // If our anti-cheat caught something, notify people with the correct permissions here.
        if (notifyBadNicknames && nicknameTooLong)
        {
            // Add a space to avoid clutter.
            MessageChannel.permission("empc.notify.staff.showstats").send(Text.EMPTY);

            // Print our warnings.
            MessageChannel.permission("empc.notify.staff.showstats").send(Text.of(
                    "§4Staff only: §cPokémon's nickname exceeds the 11 character limit."));
            MessageChannel.permission("empc.notify.staff.showstats").send(Text.of(
                    "§cSome Pixelmon cheat mods enable longer names, often silently."));
            MessageChannel.permission("empc.notify.staff.showstats").send(Text.of(
                    "§cSidemods and plugins can also do this, but keep an eye out."));
        }

        MessageChannel.TO_PLAYERS.send(Text.of("§7-----------------------------------------------------"));
    }
}
