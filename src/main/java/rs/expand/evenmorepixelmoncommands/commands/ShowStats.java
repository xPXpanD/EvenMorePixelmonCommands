// This was a pain. Nice to have, though.
package rs.expand.evenmorepixelmoncommands.commands;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.forms.RegionalForms;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods;
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import java.math.BigDecimal;
import java.util.*;

import static rs.expand.evenmorepixelmoncommands.EMPC.*;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

public class ShowStats implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean showNicknames, showEVs, showExtraInfo, showCounts, clampBadNicknames, notifyBadNicknames,
                          reshowIsFree;

    // Set up some more variables for internal use.
    private final String sourceName = this.getClass().getSimpleName();
    private final HashMap<UUID, Long> cooldownMap = new HashMap<>();
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

            if (!commandErrorList.isEmpty())
            {
                PrintingMethods.printCommandNodeError("ShowStats", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
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
                                    printSourcedMessage(sourceName, "§bShowing off player §3" + ((Player) src).getName() +
                                            "§b, slot §3" + slot + "§b. Taking §3" + costToConfirm + "§b coins.");

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

                            if (economyEnabled && reshowIsFree)
                            {
                                src.sendMessage(Text.EMPTY);
                                src.sendMessage(Text.of("§5Note: §dShowing already-shown Pokémon is free!"));
                            }

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

    // Create and print a command-specific error box (box bits optional) that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input, final boolean hitCooldown)
    {
        if (!hitCooldown)
            src.sendMessage(Text.of("§5-----------------------------------------------------"));

        src.sendMessage(Text.of(input));

        if (!hitCooldown)
        {
            if (economyEnabled && commandCost != 0)
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
            else
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));

            if (economyEnabled && commandCost > 0)
            {
                src.sendMessage(Text.EMPTY);

                if (commandCost == 1)
                    src.sendMessage(Text.of("§eConfirming will cost you §6one §ecoin."));
                else
                    src.sendMessage(Text.of("§eConfirming will cost you §6" + commandCost + "§e coins."));

                if (economyEnabled && reshowIsFree)
                {
                    src.sendMessage(Text.EMPTY);
                    src.sendMessage(Text.of("§5Note: §dShowing already-shown Pokémon is free!"));
                }
            }

            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }

    private void checkAndShowStats(final Pokemon pokemon, final Player player)
    {
        // Set up IVs and matching math.
        final IVStore IVs = pokemon.getIVs();
        final int totalIVs =
                IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
        final int percentIVs = (int) Math.round(totalIVs * 100.0 / 186.0);

        // Build an array of relevant stats, and only those. We don't need Accuracy or Eveasion where we're going!
        StatsType[] stats = new StatsType[]
                { StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed };

        // Set up a builder for our IV String. Add early text, we'll fill in the rest later.
        final StringBuilder ivStrings = new StringBuilder().append("➡ ");

        // Set up a bool for later checking. We'll append a message to explain Hyper Training if necessary.
        boolean wasHyperTrained = true;

        // Check if we have any stats that aren't hyper-trained.
        for (StatsType stat : stats)
        {
            if (!IVs.isHyperTrained(stat))
                wasHyperTrained = false;
        }

        // Apply different formatting based on whether all stats were hyper-trained or not.
        if (wasHyperTrained)
        {
            // Loop through the different stats and add them to our String builder.
            for (int i = 0; i < 6; i++)
            {
                // Append a fitting color tag. Has to be done here, as applying these breaks non-color formatting tags.
                ivStrings.append("§d");

                // If the IV was already 31+, italicize the text for extra fanciness.
                if (IVs.get(stats[i]) > 30)
                    ivStrings.append("§o");

                // Append the rest of our info.
                ivStrings.append(IVs.get(stats[i]))
                        .append(" §5")
                        .append(statShorthands[i]);

                // Add a separator if we're not at the end, so we can separate stats for display.
                if (i < 5)
                    ivStrings.append(statSeparator);
            }
        }
        else
        {
            // Loop through the different stats and add them to our String builder.
            for (int i = 0; i < 6; i++)
            {
                // Append a fitting color tag. Has to be done here, as applying these breaks non-color formatting tags.
                if (IVs.isHyperTrained(stats[i]))
                    ivStrings.append("§d");
                else
                    ivStrings.append("§a");

                // If the IV was already 31+, italicize the text for extra fanciness.
                if (IVs.get(stats[i]) > 30)
                    ivStrings.append("§o");

                // Add the original IV. Again, make it yellow/orange if we're dealing with a boosted stat.
                if (IVs.isHyperTrained(stats[i]))
                {
                    wasHyperTrained = true;

                    ivStrings.append(IVs.get(stats[i])).append(" §5");
                }
                else
                    ivStrings.append(IVs.get(stats[i])).append(" §2");

                // Append the common shorthand String.
                ivStrings.append(statShorthands[i]);

                // Add a separator if we're not at the end, so we can separate stats for display.
                if (i < 5)
                    ivStrings.append(statSeparator);
            }
        }

        // Set up for our anti-cheat notifier.
        boolean nicknameTooLong = false;

        // Set up name-related stuff.
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String formattedName = "§6" + localizedName;
        String nickname = pokemon.getNickname();

        // These always get added to printing, but are filled in only when necessary.
        final String shinyString = pokemon.isShiny() ? "§6§lshiny §r" : "";

        // Add a regional indicator for Pokémon from later-gen regions.
        final String regionString;
        if (pokemon.getFormEnum() == RegionalForms.ALOLAN)
            regionString = "§6Alolan ";
        else if (pokemon.getFormEnum() == RegionalForms.GALARIAN)
            regionString = "§6Galarian ";
        else
            regionString = "";

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
                shinyString + regionString + formattedName + nameAdditionString);
        hovers.add("");
        hovers.add("§bCurrent IVs§f:");
        hovers.add("➡ §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)");
        hovers.add(ivStrings.toString());

        if (showEVs)
        {
            // Rinse and repeat the earlier IV code for EVs, sort of.
            final EVStore EVs = pokemon.getEVs();
            final int totalEVs =
                    EVs.get(StatsType.HP) + EVs.get(StatsType.Attack) + EVs.get(StatsType.Defence) +
                    EVs.get(StatsType.SpecialAttack) + EVs.get(StatsType.SpecialDefence) + EVs.get(StatsType.Speed);
            final int percentEVs = (int) Math.round(totalEVs * 100.0 / 510.0);

            // Set up a builder for our EV String. Add some early text, we'll fill in the rest later.
            final StringBuilder evStrings = new StringBuilder();

            // Loop through the stats, and format our EV String.
            for (int i = 0; i < 6; i++)
            {
                if (EVs.get(stats[i]) > 251) // Italicize the text for extra fanciness.
                    evStrings.append("§o").append(EVs.get(stats[i]));
                else
                    evStrings.append(EVs.get(stats[i]));

                // Append common Strings.
                evStrings.append(" §2").append(statShorthands[i]);

                // Add a separator if we're not at the end, so we can separate stats for display.
                if (i < 5)
                    evStrings.append(statSeparator);
            }

            hovers.add("§bCurrent EVs§f:");
            hovers.add("➡ §a" + totalEVs + "§f/§a510§f (§a" + percentEVs + "%§f)");
            hovers.add("➡ §a" + evStrings.toString());
        }

        if (showExtraInfo)
        {
            // Start adding miscellaneous Pokémon info.
            hovers.add("§bExtra info§f:");

            // Get and add the Pokémon's growth, their size. Omit it entirely if it can't be grabbed, somehow.
            final EnumGrowth growth = pokemon.getGrowth();
            switch (growth)
            {
                case Microscopic:
                    hovers.add("➡ §aIt is §2§omicroscopic§r§a."); break; // NOW with fancy italicization!
                case Pygmy:
                    hovers.add("➡ §aIt is §2a pygmy§a."); break;
                case Runt:
                    hovers.add("➡ §aIt is §2a runt§a."); break;
                case Small:
                    hovers.add("➡ §aIt is §2small§a."); break;
                case Ordinary:
                    hovers.add("➡ §aIt is §2ordinary§a."); break;
                case Huge:
                    hovers.add("➡ §aIt is §2huge§a."); break;
                case Giant:
                    hovers.add("➡ §aIt is §2giant§a."); break;
                case Enormous:
                    hovers.add("➡ §aIt is §2enormous§a."); break;
                case Ginormous:
                    hovers.add("➡ §aIt is §2§nginormous§r§a."); // NOW with fancy underlining!
            }

            // Get and add the Pokémon's gender, if applicable.
            switch (pokemon.getGender())
            {
                case Male:
                    hovers.add("➡ §aIt is §2male§a."); break;
                case Female:
                    hovers.add("➡ §aIt is §2female§a."); break;
                default:
                    hovers.add("➡ §aIt has §2no gender§a.");
            }

            // Get and add the nature and the boosted/cut stats. Capitalize the nature's name properly.
            final EnumNature nature = pokemon.getNature();
            final String plusVal = PokemonMethods.getShorthand(nature.increasedStat);
            final String minusVal = PokemonMethods.getShorthand(nature.decreasedStat);
            if (nature.index >= 0 && nature.index <= 4)
                hovers.add("➡ §aIt is §2" + nature.name().toLowerCase() + "§a, with well-balanced stats.");
            else if (nature.index < 0 || nature.index > 24)
                hovers.add("➡ §aIt has an §2unknown §anature...");
            else
                hovers.add("➡ §aIt is §2" + nature.name().toLowerCase() + "§a, boosting §2" + plusVal + " §aand cutting §2" + minusVal + "§a.");

            // Get and add the ability, and show whether it's hidden or not.
            if (pokemon.getAbilitySlot() == 2)
                hovers.add("➡ §aIt has the \"§2" + pokemon.getAbility().getLocalizedName() + "§a\" hidden ability!");
            else
                hovers.add("➡ §aIt has the \"§2" + pokemon.getAbility().getLocalizedName() + "§a\" ability.");

            if (wasHyperTrained)
            {
                hovers.add("");
                hovers.add("§dIVs displayed in pink are hyper-trained.");
            }

            // Mew-specific check for cloning counts. A bit cheap, but it'll work down here. Also, lake trio enchant check.
            if (baseName.equals("Mew"))
            {
                hovers.add("");

                final int cloneCount = ((MewStats) pokemon.getExtraStats()).numCloned;

                if (cloneCount == 0)
                    hovers.add("§eCloning has not yet been attempted.");
                else
                    hovers.add("§eCloning has been attempted §6" + cloneCount + "§f/§63 §etimes.");
            }
            else if (baseName.equals("Azelf") || baseName.equals("Mesprit") || baseName.equals("Uxie"))
            {
                hovers.add("");

                final int enchantCount = ((LakeTrioStats) pokemon.getExtraStats()).numEnchanted;
                final int maxEnchants = PixelmonConfig.lakeTrioMaxEnchants;

                if (enchantCount == 0)
                    hovers.add("§eThis §6" + baseName + "§e has not yet enchanted any rubies.");
                else
                    hovers.add("§eThis §6" + baseName + "§e has enchanted §6" + enchantCount + "§f/§6" + maxEnchants + " §erubies.");
            }
        }

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

        // Set a tag so future showings are free, provided that's enabled in the configs.
        if (economyEnabled && commandCost > 0)
            pokemon.getPersistentData().setBoolean("wasShown", true);

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
