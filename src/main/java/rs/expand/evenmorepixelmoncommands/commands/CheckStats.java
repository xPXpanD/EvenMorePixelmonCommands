// PixelUpgrade's very first command. Originally /upgrade stats, then /getstats, and then finally this as part of EMPC.
package rs.expand.evenmorepixelmoncommands.commands;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.forms.EnumAlolan;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rs.expand.evenmorepixelmoncommands.EMPC.*;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.*;

public class CheckStats implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Boolean showTeamWhenSlotEmpty, showEVs, allowCheckingEggs, revealEggStats, recheckIsFree;
    public static Integer commandCost, babyHintPercentage;

    // Set up some more variables for internal use.
    private boolean calledRemotely;
    private String sourceName = this.getClass().getSimpleName();

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (!(src instanceof CommandBlock))
        {
            // Running from console? Let's tell our code that. If "src" is not a Player, this becomes true.
            calledRemotely = !(src instanceof Player);

            // Validate the data we get from the command's main config.
            final List<String> commandErrorList = new ArrayList<>();
            if (commandAlias == null)
                commandErrorList.add("commandAlias");
            if (showTeamWhenSlotEmpty == null)
                commandErrorList.add("showTeamWhenSlotEmpty");
            if (showEVs == null)
                commandErrorList.add("showEVs");
            if (allowCheckingEggs == null)
                commandErrorList.add("allowCheckingEggs");
            if (revealEggStats == null)
                commandErrorList.add("revealEggStats");
            if (babyHintPercentage == null)
                commandErrorList.add("babyHintPercentage");
            if (commandCost == null)
                commandErrorList.add("commandCost");
            if (recheckIsFree == null)
                commandErrorList.add("recheckIsFree");

            if (!commandErrorList.isEmpty())
            {
                printCommandNodeError("CheckStats", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                boolean commandConfirmed = false;
                final Optional<String> arg1Optional = args.getOne("target/slot");
                final Optional<String> arg2Optional = args.getOne("slot/confirmation");
                Pokemon pokemon = null;
                Player target = null;
                int slot = 0;

                if (calledRemotely)
                {
                    // Do we have an argument in the first slot?
                    if (arg1Optional.isPresent())
                    {
                        final String arg1String = arg1Optional.get();

                        // Do we have a valid online player?
                        if (Sponge.getServer().getPlayer(arg1String).isPresent())
                            target = Sponge.getServer().getPlayer(arg1String).get();
                        else
                        {
                            src.sendMessage(Text.of("§4Error: §cInvalid target on first argument. See below."));
                            return CommandResult.empty();
                        }
                    }
                    else
                    {
                        src.sendMessage(Text.of("§4Error: §cNo arguments found. See below."));
                        return CommandResult.empty();
                    }

                    // Do we have an argument in the second slot, and no error from arg 1?
                    // If arg 2 is not present, the user probably wants to see the target's whole party.
                    if (arg2Optional.isPresent())
                    {
                        final String arg2String = arg2Optional.get();

                        // Do we have a slot?
                        if (arg2String.matches("^[1-6]"))
                            slot = Integer.parseInt(arg2String);
                        else
                        {
                            src.sendMessage(Text.of("§4Error: §cInvalid slot on second argument. See below."));
                            return CommandResult.empty();
                        }
                    }
                }
                else
                {
                    // Ugly, but it'll do for now... Doesn't seem like my usual way of getting flags will work here.
                    final Optional<String> arg3Optional = args.getOne("confirmation");

                    if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                        commandConfirmed = true;
                    else if (arg3Optional.isPresent() && arg3Optional.get().equalsIgnoreCase("-c"))
                        commandConfirmed = true;

                    // Start checking arguments for non-flag contents. First up: argument 1.
                    if (arg1Optional.isPresent())
                    {
                        final String arg1String = arg1Optional.get();

                        // Do we have a slot?
                        if (arg1String.matches("^[1-6]"))
                            slot = Integer.parseInt(arg1String);
                        // Is our calling player allowed to check other people's Pokémon, and is arg 1 a valid target?
                        else if (src.hasPermission("empc.command.other.checkstats") && Sponge.getServer().getPlayer(arg1String).isPresent())
                            target = Sponge.getServer().getPlayer(arg1String).get();
                        else
                        {
                            if (src.hasPermission("empc.command.other.checkstats"))
                                printLocalError(src, "§4Error: §cInvalid target or slot on first argument. See below.");
                            else
                                printLocalError(src, "§4Error: §cInvalid slot on first argument. See below.");

                            return CommandResult.empty();
                        }
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cNo arguments found. See below.");
                        return CommandResult.empty();
                    }

                    // Can we continue, were we not told to skip and do we not have a slot already? Check arg 2 for one.
                    if (slot == 0)
                    {
                        if (arg2Optional.isPresent())
                        {
                            final String arg2String = arg2Optional.get();

                            // Do we have a slot?
                            if (arg2String.matches("^[1-6]"))
                                slot = Integer.parseInt(arg2String);
                            else
                            {
                                printLocalError(src, "§4Error: §cInvalid slot on second argument. See below.");
                                return CommandResult.empty();
                            }
                        }
                        else if (!showTeamWhenSlotEmpty)
                        {
                            printLocalError(src, "§4Error: §cMissing slot on second argument. See below.");
                            return CommandResult.empty();
                        }
                    }
                }

                // Get the player's party.
                final PartyStorage party;
                if (target != null)
                    party = Pixelmon.storageManager.getParty((EntityPlayerMP) target);
                else
                    party = Pixelmon.storageManager.getParty((EntityPlayerMP) src);

                // Get the Pokémon in the targeted slot, if a slot is present.
                if (slot != 0)
                    pokemon = party.get(slot - 1);

                // Start checking!
                if (slot == 0)
                    checkParty(src, target, party, calledRemotely);
                else if (pokemon == null)
                {
                    if (showTeamWhenSlotEmpty)
                        checkParty(src, target, party, calledRemotely);
                    else if (target != null)
                        src.sendMessage(Text.of("§4Error: §cYour target has no Pokémon in that slot!"));
                    else
                        src.sendMessage(Text.of("§4Error: §cThere's no Pokémon in that slot!"));
                }
                else if (pokemon.isEgg() && !allowCheckingEggs && !calledRemotely) // Always allow egg checking for console!
                        src.sendMessage(Text.of("§4Error: §cYou may only check hatched Pokémon."));
                else
                {
                    // Should this check be free? Only passes if we receive a "hey we checked this previously".
                    final boolean pokemonIsFree = recheckIsFree && pokemon.getPersistentData().getBoolean("wasChecked");

                    // Don't use economy for console! Also, skip if we're checking a known Pokémon with free rechecks on.
                    if (economyEnabled && !calledRemotely && commandCost > 0 && !pokemonIsFree)
                    {
                        // !calledRemotely already guarantees src is a Player.
                        @SuppressWarnings("ConstantConditions")
                        final Player player = (Player) src;
                        final BigDecimal costToConfirm = new BigDecimal(commandCost);

                        if (commandConfirmed)
                        {
                            final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                final UniqueAccount uniqueAccount = optionalAccount.get();
                                final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                        costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                if (transactionResult.getResult() == ResultType.SUCCESS)
                                {
                                    if (target == null)
                                    {
                                        printSourcedMessage(sourceName, "§bChecked player §3" + player.getName() +
                                                "§b, slot §3" + slot + "§b. Taking §3" + costToConfirm + "§b coins.");
                                    }
                                    else
                                    {
                                        printSourcedMessage(sourceName, "§bPlayer §3" + player.getName() + "§b is checking player §3" +
                                                target.getName() + "§b, slot §3" + slot + "§b. Taking §3" + costToConfirm + "§b coins.");
                                    }
                                    checkSpecificSlot(src, target, pokemon, target != null);
                                }
                                else
                                {
                                    final BigDecimal balanceNeeded =
                                            uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                    src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
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
                                src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's status costs §6one §ecoin."));
                            else
                            {
                                src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's status costs §6" +
                                        costToConfirm + "§e coins."));
                            }

                            src.sendMessage(Text.EMPTY);

                            if (target != null)
                            {
                                src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " +
                                        target.getName() + " " + slot + " -c"));
                            }
                            else
                                src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));

                            if (economyEnabled && recheckIsFree)
                            {
                                src.sendMessage(Text.EMPTY);
                                src.sendMessage(Text.of("§5Note: §dChecks on already-checked Pokémon are free!"));
                            }

                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        }
                    }
                    else
                        checkSpecificSlot(src, target, pokemon, target != null);
                }
            }
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
	}

	// Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));

        if (calledRemotely)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target> [slot? 1-6]"));
        else
        {
            final String confirmString = economyEnabled && commandCost > 0 ? " {-c to confirm}" : "";

            if (src.hasPermission("empc.command.other.checkstats"))
            {
                if (showTeamWhenSlotEmpty)
                    src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] [slot? 1-6]" + confirmString));
                else
                    src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] <slot, 1-6>" + confirmString));
            }
            else
            {
                src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>" + confirmString +
                        " §7(no perms for target)"));
            }
            if (economyEnabled && commandCost > 0)
            {
                src.sendMessage(Text.EMPTY);

                if (commandCost == 1)
                    src.sendMessage(Text.of("§eConfirming will cost you §6one §ecoin."));
                else
                    src.sendMessage(Text.of("§eConfirming will cost you §6" + commandCost + "§e coins."));

                if (economyEnabled && recheckIsFree)
                {
                    src.sendMessage(Text.EMPTY);
                    src.sendMessage(Text.of("§5Note: §dChecks on already-checked Pokémon are free!"));
                }
            }
        }

        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void checkParty(final CommandSource src, Player target, final PartyStorage party, final boolean calledRemotely)
    {
        src.sendMessage(Text.of("§7-----------------------------------------------------"));

        // Target can't be null if we were called from console/block, as we'd have errored out earlier.
        if (target == null)
            target = (Player) src;

        // Similar to the above. Always grab target if called remotely. Run on the player if they match their target.
        final String targetString = calledRemotely || target != src ? " " + target.getName() : "";

        // Find out who we're checking, and print that.
        if (src instanceof Player && src.getName().equals(target.getName()))
            src.sendMessage(Text.of("§eNo slot found, showing your whole team."));
        else
            src.sendMessage(Text.of("§eNo slot found, showing §6" + target.getName() + "§e's whole team."));

        src.sendMessage(Text.EMPTY);

        int slotTicker = 0;
        for (final Pokemon pokemon : party.getAll())
        {
            if (slotTicker > 5)
                break;

            if (pokemon == null)
                src.sendMessage(Text.of("§bSlot " + (slotTicker + 1) + "§f: §2Empty§a."));
            else if (pokemon.isEgg())
                src.sendMessage(Text.of("§bSlot " + (slotTicker + 1) + "§f: §aAn §2egg§a."));
            else
            {
                // We'll need these a few times, so create variables for them.
                final String localizedName = pokemon.getSpecies().getLocalizedName();
                final String nickname = pokemon.getNickname();

                // Set up some Strings for our message. Fill them in appropriately depending on the Pokémon.
                final String alolanString = pokemon.getFormEnum() == EnumAlolan.ALOLAN ? "Alolan " : "";
                final String shinyString = pokemon.isShiny() ? "§2§lshiny§r §a" : "";
                final String nicknameString;
                if (nickname != null && !nickname.isEmpty() && !nickname.equals(localizedName))
                    nicknameString = ", nicknamed §2" + pokemon.getNickname() + "§a.";
                else
                    nicknameString = ".";

                // Report back.
                src.sendMessage(Text.of("§bSlot " + (slotTicker + 1) + "§f: §aA " + shinyString + "level " + pokemon.getLevel() +
                        "§2 " + alolanString + localizedName + "§a" + nicknameString));
            }

            slotTicker++;
        }

        src.sendMessage(Text.EMPTY);

        if (economyEnabled && !calledRemotely && commandCost > 0)
        {
            src.sendMessage(Text.of("§dWant more info? §6/" + commandAlias + targetString +
                    " <slot, 1-6> {-c to confirm}"));

            if (commandCost == 1)
                src.sendMessage(Text.of("§6Warning: §eThis will cost you §5one §dcoin."));
            else
                src.sendMessage(Text.of("§6Warning: §eThis will cost you §5" +
                    commandCost + " §dcoins."));
        }
        else
            src.sendMessage(Text.of("§dWant to know more? §6/" + commandAlias + targetString + " <slot, 1-6>"));

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }

    // Checks a slot's stats and prints them to chat in a neat list, with contents differing based on config flags.
    private void checkSpecificSlot(final CommandSource src, final Player target, final Pokemon pokemon, final boolean haveTarget)
    {
        // Let's start by printing some stuff! Mark the start of our output text box.
        src.sendMessage(Text.of("§7-----------------------------------------------------"));

        // Set up IVs and matching math. These are used everywhere.
        final IVStore IVs = pokemon.getIVs();
        final int totalIVs =
                IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
        final int percentIVs = (int) Math.round(totalIVs * 100.0 / 186.0);

        // Check if our Pokémon is an egg. If it is, be careful with it and only reveal stats if explictly told to do so.
        if (!pokemon.isEgg() || revealEggStats || calledRemotely)
        {
            // Make some easy Strings for the Pokémon's name, nickname and associated stuff.
            final String localizedName = pokemon.getSpecies().getLocalizedName();
            final String baseName = pokemon.getSpecies().getPokemonName();
            final String nickname = pokemon.getNickname();

            // Let's build a dynamic message that we can print to chat.
            src.sendMessage(Text.of(new StringBuilder()
                    .append("§eStats of ")
                    .append(haveTarget ? "§6" + target.getName() + "§e's " : "your ")
                    .append(pokemon.isShiny() ? "§6§lshiny §r§e" : "§e")
                    .append(!pokemon.isEgg() ? "level " + pokemon.getLevel() + " " : "")
                    .append(pokemon.getFormEnum() == EnumAlolan.ALOLAN ? "§6Alolan " : "§6")
                    .append(localizedName)
                    .append(pokemon.isEgg() ? " §eegg" : "§e")
                    .append(!pokemon.isEgg() && nickname != null && !nickname.isEmpty() ? ", nicknamed §6" + nickname + "§e:" : "§e:")
            ));

            // Build an array of relevant stats, and only those. We don't need Accuracy or Eveasion where we're going!
            StatsType[] stats = new StatsType[]
                    { StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed };

            // Set up a builder for our IV String. Add early text, we'll fill in the rest later.
            final StringBuilder ivStrings = new StringBuilder().append("§bIVs§f: ");

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
                // Make everything yellow/orange, here. This keeps the theme intact.
                ivStrings.append("§d")
                        .append(percentIVs)
                        .append("% §f(");

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
                // Use the normal green/dark green scheme, since we have at least one normal stat.
                ivStrings.append("§a")
                        .append(percentIVs)
                        .append("% §f(");

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

            // Finish our built IV String and print it.
            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of(ivStrings + "§f)"));

            // Do the same for EVs, if enabled in the config and not on an egg. (eggs with EVs? nah)
            if (showEVs && !pokemon.isEgg())
            {
                // Rinse and repeat stat setup for EVs.
                final EVStore EVs = pokemon.getEVs();
                final int totalEVs =
                        EVs.get(StatsType.HP) + EVs.get(StatsType.Attack) + EVs.get(StatsType.Defence) +
                        EVs.get(StatsType.SpecialAttack) + EVs.get(StatsType.SpecialDefence) + EVs.get(StatsType.Speed);
                final int percentEVs = (int) Math.round(totalEVs * 100.0 / 510.0);

                // Set up a builder for our EV String. Add some early text, we'll fill in the rest later.
                final StringBuilder evStrings = new StringBuilder()
                        .append("§bEVs§f: §a")
                        .append(percentEVs)
                        .append("% §f(§a");

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

                // Finish our built EV String and print it.
                src.sendMessage(Text.of(evStrings + "§f)"));
            }

            // Get a bunch of important Pokémon stat data.
            final EnumNature nature = pokemon.getNature();
            final String plusVal = '+' + PokemonMethods.getShorthand(nature.increasedStat);
            final String minusVal = '-' + PokemonMethods.getShorthand(nature.decreasedStat);

            // Set up a gender character. Console doesn't like Unicode genders, so if src is not a Player we'll use M/F/-.
            final String genderChar = PokemonMethods.getGenderCharacter(src, pokemon.getGender().getForm());

            // Show extra info, which we grabbed from PokemonMethods.
            src.sendMessage(Text.of("§bSize§f: " + pokemon.getGrowth().name() + "§f | §bGender§f: " + genderChar +
                    "§r | §bNature§f: " + nature.name() + "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)"));

            // Show the ability on a separate line. Localize it so we can show name/description in the user's language!
            // Shoutout to happyzlife, who wrote several snippets for me to get this working. Thanks a ton!
            if (src instanceof EntityPlayerMP)
            {
                // Set up Forge's counterpart to Sponge's text stuff.
                ITextComponent component = new TextComponentString("§bAbility§f: ");

                // Underline.
                component.getStyle().setUnderlined(true);

                // If we have a hidden ability, italicize.
                if (pokemon.getAbilitySlot() == 2)
                    component.getStyle().setItalic(true);

                // Add the ability's name, translated into the user's language.
                component.appendSibling(new TextComponentTranslation("ability." + pokemon.getAbility().getName() + ".name"));

                // Do the same for the ability's description, as shown when hovered over.
                component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponentTranslation("ability." + pokemon.getAbility().getName() + ".description")));

                // Send!
                ((EntityPlayerMP) src).sendMessage(component);
            }
            else
            {
                if (pokemon.getAbilitySlot() == 2)
                    src.sendMessage(Text.of("§bAbility§f: §o" + pokemon.getAbility().getLocalizedName()));
                else
                    src.sendMessage(Text.of("§bAbility§f: " + pokemon.getAbility().getLocalizedName()));
            }

            if (pokemon.isEgg())
            {
                if (pokemon.isShiny())
                {
                    src.sendMessage(Text.EMPTY);
                    src.sendMessage(Text.of("§6§lCongratulations! §r§eThis baby is shiny!"));
                }
            }
            else
            {
                if (wasHyperTrained)
                {
                    src.sendMessage(Text.EMPTY);
                    src.sendMessage(Text.of("§dIVs displayed in pink are hyper-trained."));
                }

                // Mew-specific check for cloning counts. A bit cheap, but it'll work down here. Also, lake trio enchant check.
                if (baseName.equals("Mew"))
                {
                    src.sendMessage(Text.EMPTY);

                    final int cloneCount = ((MewStats) pokemon.getExtraStats()).numCloned;

                    if (cloneCount == 0)
                        src.sendMessage(Text.of("§eCloning has not yet been attempted."));
                    else
                        src.sendMessage(Text.of("§eCloning has been attempted §6" + cloneCount + "§f/§63 §etimes."));
                }
                else if (baseName.equals("Azelf") || baseName.equals("Mesprit") || baseName.equals("Uxie"))
                {
                    src.sendMessage(Text.EMPTY);

                    final int enchantCount = ((LakeTrioStats) pokemon.getExtraStats()).numEnchanted;
                    final int maxEnchants = LakeTrioStats.MAX_ENCHANTED;

                    if (enchantCount == 0)
                        src.sendMessage(Text.of("§eThis §6" + baseName + "§e has not yet enchanted any rubies."));
                    else
                        src.sendMessage(Text.of("§eThis §6" + baseName + "§e has enchanted §6" + enchantCount + "§f/§6" + maxEnchants + " §erubies."));
                }
            }
        }
        else // If stat revealing for eggs is off, we'll land here. Let's show some vague (and far more responsible) hints.
        {
            // Figure out whether the baby is anything special. Uses a config-set percentage for stat checks.
            if (percentIVs >= babyHintPercentage && !pokemon.isShiny())
                src.sendMessage(Text.of("§6What's this? §eThis baby seems to be bursting with energy!"));
            else if (!(percentIVs >= babyHintPercentage) && pokemon.isShiny())
                src.sendMessage(Text.of("§6What's this? §eThis baby seems to have an odd sheen to it!"));
            else if (percentIVs >= babyHintPercentage && pokemon.isShiny())
                src.sendMessage(Text.of("§6What's this? §eSomething about this baby seems real special!"));
            else
                src.sendMessage(Text.of("§eThis baby seems to be fairly ordinary..."));
        }

        // Set a tag so future rechecks are free, provided that's enabled in the configs.
        if (economyEnabled && commandCost > 0)
            pokemon.getPersistentData().setBoolean("wasChecked", true);

        // Finish up the output text box. Done!
        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
