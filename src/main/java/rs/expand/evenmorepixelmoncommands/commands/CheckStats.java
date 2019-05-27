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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rs.expand.evenmorepixelmoncommands.EMPC.*;
import static rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods.getShorthand;
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
                printCommandNodeError("CheckStats", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                PrintingMethods.printMainNodeError("CheckStats", mainConfigErrorArray);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
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
                    final boolean pokemonIsFree =
                            recheckIsFree && pokemon.getPersistentData().getBoolean("wasChecked");

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

                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        }
                    }
                    else
                    {
                        boolean haveTarget = false;
                        if (target != null)
                            haveTarget = true;

                        checkSpecificSlot(src, target, pokemon, haveTarget);
                    }
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
            src.sendMessage(Text.of("§eWant more info? §6/" + commandAlias + targetString +
                    " <slot, 1-6> {-c to confirm}"));

            if (commandCost == 1)
                src.sendMessage(Text.of("§5Warning: §dThis will cost you §5one §dcoin."));
            else
                src.sendMessage(Text.of("§5Warning: §dThis will cost you §5" +
                    commandCost + " §dcoins."));
        }
        else
            src.sendMessage(Text.of("§eWant to know more? §6/" + commandAlias + targetString + " <slot, 1-6>"));

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
            // Format the IVs for use later, so we can print them.
            String ivs1 = IVs.get(StatsType.HP) + " §2" + shortenedHP + statSeparator;
            String ivs2 = IVs.get(StatsType.Attack) + " §2" + shortenedAttack + statSeparator;
            String ivs3 = IVs.get(StatsType.Defence) + " §2" + shortenedDefense + statSeparator;
            String ivs4 = IVs.get(StatsType.SpecialAttack) + " §2" + shortenedSpecialAttack + statSeparator;
            String ivs5 = IVs.get(StatsType.SpecialDefence) + " §2" + shortenedSpecialDefense + statSeparator;
            String ivs6 = IVs.get(StatsType.Speed) + " §2" + shortenedSpeed;

            if (IVs.get(StatsType.HP) > 30)
                ivs1 = "§o" + ivs1;
            if (IVs.get(StatsType.Attack) > 30)
                ivs2 = "§o" + ivs2;
            if (IVs.get(StatsType.Defence) > 30)
                ivs3 = "§o" + ivs3;
            if (IVs.get(StatsType.SpecialAttack) > 30)
                ivs4 = "§o" + ivs4;
            if (IVs.get(StatsType.SpecialDefence) > 30)
                ivs5 = "§o" + ivs5;
            if (IVs.get(StatsType.Speed) > 30)
                ivs6 = "§o" + ivs6;

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
                    .append(!pokemon.isEgg() && nickname != null && !nickname.isEmpty() ?
                            ", nicknamed §6" + nickname + "§e:" : "§e:")
            ));

            // Print out IVs using previously formatted Strings.
            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of("§bIVs§f: §a" + percentIVs +
                    "% §f(§a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6 + "§f)"));

            // Do the same for EVs, if enabled in the config.
            if (showEVs && !pokemon.isEgg())
            {
                // Rinse and repeat for EVs.
                final EVStore EVs = pokemon.getEVs();
                final int totalEVs =
                        EVs.get(StatsType.HP) + EVs.get(StatsType.Attack) + EVs.get(StatsType.Defence) +
                        EVs.get(StatsType.SpecialAttack) + EVs.get(StatsType.SpecialDefence) + EVs.get(StatsType.Speed);
                final int percentEVs = (int) Math.round(totalEVs * 100.0 / 510.0);

                // Also format the strings for EVs.
                String evs1 = EVs.get(StatsType.HP) + " §2" + shortenedHP + statSeparator;
                String evs2 = EVs.get(StatsType.Attack) + " §2" + shortenedAttack + statSeparator;
                String evs3 = EVs.get(StatsType.Defence) + " §2" + shortenedDefense + statSeparator;
                String evs4 = EVs.get(StatsType.SpecialAttack) + " §2" + shortenedSpecialAttack + statSeparator;
                String evs5 = EVs.get(StatsType.SpecialDefence) + " §2" + shortenedSpecialDefense + statSeparator;
                String evs6 = EVs.get(StatsType.Speed) + " §2" + shortenedSpeed;

                if (EVs.get(StatsType.HP) > 251)
                    evs1 = "§o" + evs1;
                if (EVs.get(StatsType.Attack) > 251)
                    evs2 = "§o" + evs2;
                if (EVs.get(StatsType.Defence) > 251)
                    evs3 = "§o" + evs3;
                if (EVs.get(StatsType.SpecialAttack) > 251)
                    evs4 = "§o" + evs4;
                if (EVs.get(StatsType.SpecialDefence) > 251)
                    evs5 = "§o" + evs5;
                if (EVs.get(StatsType.Speed) > 251)
                    evs6 = "§o" + evs6;

                src.sendMessage(Text.of("§bEVs§f: §a" + percentEVs +
                        "% §f(§a" + evs1 + evs2 + evs3 + evs4 + evs5 + evs6 + "§f)"));
            }

            // Get a bunch of important Pokémon stat data.
            final EnumNature nature = pokemon.getNature();
            final String plusVal = '+' + getShorthand(nature.increasedStat);
            final String minusVal = '-' + getShorthand(nature.decreasedStat);

            // Set up a gender character. Console doesn't like Unicode genders, so if src is not a Player we'll use M/F/-.
            final String genderChar = PokemonMethods.getGenderCharacter(src, pokemon.getGender().getForm());

            // NOTE: Currently commented out because kind of useless, and too long. May make a comeback.
            /*// Figure out how happy the Pokémon is.
            final int happiness = pokemon.getFriendship();
            final String happinessString;
            if (happiness > 250) // Whoa!
                happinessString = "§o" + happiness + "!§r";
            else if (happiness < 50) // Oof...
                happinessString = happiness + "...";
            else // Eh.
                happinessString = String.valueOf(happiness);*/

            // Show extra info, which we grabbed from PokemonMethods.
            src.sendMessage(Text.of("§bSize§f: " + pokemon.getGrowth().name() + "§f | §bGender§f: " + genderChar +
                    "§r | §bNature§f: " + nature.name() + "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)"));

            // Show the ability on a separate line. Localize it so we can show name/description in the user's language!
            if (src instanceof Player)
                sendTranslatedAbilityMessage((Player) src, pokemon);
            else
                src.sendMessage(Text.of("§bAbility§f: " + pokemon.getAbility().getLocalizedName()));

            // Check and show whether the Pokémon can be upgraded/fused further, if enabled in config.
            /*final boolean isDitto = pokemon.getSpecies().getPokemonName().equals("Ditto");
            if (isDitto && showDittoFusionHelper && !gotFusionError || !isDitto && showUpgradeHelper && !gotUpgradeError)
            {
                // Let's not forget to do this. Moves the count helper message to its own line, right at the bottom.
                src.sendMessage(Text.EMPTY);

                // Let's re-use the startString String. It's still relevant.
                if (isDitto)
                {
                    final int fuseCount = pokemonNBT.getInteger("fuseCount");
                    final int fusionCap;

                    if (pokemon.getIsShiny())
                    {
                        startString = "§eThis §6§lshiny §r§6Ditto §e";
                        fusionCap = DittoFusion.shinyCap; // Shiny cap.
                    }
                    else
                    {
                        startString = "§eThis §6Ditto §e";
                        fusionCap = DittoFusion.regularCap; // Regular cap.
                    }

                    if (fuseCount != 0 && fuseCount < fusionCap)
                        src.sendMessage(Text.of(startString + "has been fused §6" + fuseCount + "§e/§6" + fusionCap + " §etimes."));
                    else if (fuseCount == 0 && fuseCount < fusionCap)
                        src.sendMessage(Text.of(startString + "can be fused §6" + fusionCap + "§e more times."));
                    else
                        src.sendMessage(Text.of(startString + "cannot be fused any further!"));
                }
                else
                {
                    final int upgradeCount = pokemonNBT.getInteger("upgradeCount");
                    final int upgradeCap;
                    final boolean isLegendary = EnumSpecies.legendaries.contains(baseName);

                    if (pokemon.getIsShiny() && isLegendary)
                    {
                        startString = "§eThis §6§lshiny legendary §r§e";
                        upgradeCap = UpgradeIVs.legendaryAndShinyCap; // Legendary + shiny cap.
                    }
                    else if (isLegendary)
                    {
                        startString = "§eThis §6§llegendary §r§ePokémon ";
                        upgradeCap = UpgradeIVs.legendaryCap; // Legendary cap.
                    }
                    else if (pokemon.getIsShiny())
                    {
                        startString = "§eThis §6§lshiny §r§ePokémon ";
                        upgradeCap = UpgradeIVs.shinyCap; // Shiny cap.
                    }
                    else
                    {
                        startString = "§eThis Pokémon ";
                        upgradeCap = UpgradeIVs.regularCap; // Regular cap.
                    }

                    if (upgradeCount != 0 && upgradeCount < upgradeCap)
                    {
                        src.sendMessage(Text.of(startString + "has been upgraded §6" + upgradeCount + "§e/§6" +
                                upgradeCap + " §etimes."));
                    }
                    else if (upgradeCount == 0 && upgradeCount < upgradeCap)
                        src.sendMessage(Text.of(startString + "can be upgraded §6" + upgradeCap + "§e more times."));
                    else
                        src.sendMessage(Text.of(startString + "has been fully upgraded!"));
                }
            }*/

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
                // Mew-specific check for cloning counts. A bit cheap, but it'll work down here. Also, lake trio enchant check.
                if (baseName.equals("Mew"))
                {
                    // If we haven't added a new line yet, do it now.
                    /*if (isDitto && !showDittoFusionHelper || !isDitto && !showUpgradeHelper)*/
                    src.sendMessage(Text.EMPTY);

                    final int cloneCount = ((MewStats) pokemon.getExtraStats()).numCloned;

                    if (cloneCount == 0)
                        src.sendMessage(Text.of("§eCloning has not yet been attempted."));
                    else
                        src.sendMessage(Text.of("§eCloning has been attempted §6" + cloneCount + "§f/§63 §etimes."));
                }
                else if (baseName.equals("Azelf") || baseName.equals("Mesprit") || baseName.equals("Uxie"))
                {
                    // If we haven't added a new line yet, do it now.
                    /*if (isDitto && !showDittoFusionHelper || !isDitto && !showUpgradeHelper)*/
                    src.sendMessage(Text.EMPTY);

                    final int enchantCount = ((LakeTrioStats) pokemon.getExtraStats()).numEnchanted;
                    final int maxEnchants = LakeTrioStats.MAX_ENCHANTED;

                    if (enchantCount == 0)
                        src.sendMessage(Text.of("§eIt has not yet enchanted any rubies."));
                    else
                        src.sendMessage(Text.of("§eIt has enchanted §6" + enchantCount + "§f/§6" + maxEnchants + " §erubies."));
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

        if (economyEnabled && commandCost > 0 && recheckIsFree)
        {
            if (pokemon.getPersistentData().getBoolean("wasChecked"))
            {
                src.sendMessage(Text.EMPTY);
                src.sendMessage(Text.of("§dThis Pokémon was checked before, so this check was free!"));
            }
            else // Write the "free rechecks" tag to the Pokémon for future use.
                pokemon.getPersistentData().setBoolean("wasChecked", true);
        }

        // Finish up the output text box. Done!
        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }

    // Shoutout to happyzlife, who wrote several snippets for me to get this working. Thanks a ton!
    private static void sendTranslatedAbilityMessage(Player player, Pokemon pokemon)
    {
        final String ability = pokemon.getAbility().getName();

        ITextComponent component = new TextComponentString("§bAbility§f: §n")
                .appendSibling(new TextComponentTranslation("ability." + ability + ".name"));

        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TextComponentTranslation("ability." + ability + ".description")));

        // Send!
        ((EntityPlayerMP) player).sendMessage(component);
    }
}
