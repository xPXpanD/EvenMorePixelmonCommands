// CheckStats' sister command.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import rs.expand.pixelupgrade.utilities.PokemonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class CheckEgg implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Boolean showName, explicitReveal, recheckIsFree;
    public static Integer babyHintPercentage, commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("CheckEgg", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (showName == null)
                nativeErrorArray.add("showName");
            if (explicitReveal == null)
                nativeErrorArray.add("explicitReveal");
            if (babyHintPercentage == null)
                nativeErrorArray.add("babyHintPercentage");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");
            if (recheckIsFree == null)
                nativeErrorArray.add("recheckIsFree");

            // Also get some stuff from PixelUpgrade.conf.
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

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("CheckEgg", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                PrintingMethods.printMainNodeError("CheckEgg", mainConfigErrorArray);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                int slot = 0;
                boolean commandConfirmed = false, canContinue = true;
                final Player player = (Player) src;

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
                    final Pokemon pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot - 1);

                    if (pokemon == null || !pokemon.isEgg())
                    {
                        printToLog(1, "Provided slot was not an egg, or a proper Pokémon. Exit.");
                        src.sendMessage(Text.of("§4Error: §cCould not find an egg in the provided slot."));
                    }
                    else
                    {
                        printToLog(2, "Egg found. Let's do this!");

                        final boolean wasEggChecked = pokemon.getPersistentData().getBoolean("hadEggChecked");

                        if (!economyEnabled || commandCost == 0 || wasEggChecked && recheckIsFree)
                        {
                            printEggResults(pokemon, wasEggChecked, src);

                            // Keep this below the printEggResults call, or your debug message order will look weird.
                            if (!economyEnabled)
                            {
                                printToLog(1, "Checking egg in slot §3" + slot +
                                        "§b. No economy, so we skipped eco checks.");
                            }
                            else if (commandCost == 0)
                            {
                                printToLog(1, "Checking egg in slot §3" + slot +
                                        "§b. Config price is §30§b, taking nothing.");
                            }
                            else
                            {
                                printToLog(1, "Checking egg, slot §3" + slot +
                                        "§b. Detected a recheck, taking nothing as per config.");
                            }
                        }
                        else
                        {
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
                                        printEggResults(pokemon, wasEggChecked, src);

                                        // Keep this below the printEggResults call, or your debug message order will look weird.
                                        printToLog(1, "Checking egg in slot §3" + slot +
                                                "§b, and taking §3" + costToConfirm + "§b coins.");
                                    }
                                    else
                                    {
                                        final BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

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
                                printToLog(1, "Showed cost, no confirmation was provided. Exit.");

                                src.sendMessage(Text.of("§5-----------------------------------------------------"));

                                // Is cost to confirm exactly one coin?
                                if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                    src.sendMessage(Text.of("§6Warning: §eChecking an egg's status costs §6one §ecoin."));
                                else
                                {
                                    src.sendMessage(Text.of("§6Warning: §eChecking an egg's status costs §6" +
                                            costToConfirm + "§e coins."));
                                }

                                src.sendMessage(Text.EMPTY);
                                src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                                src.sendMessage(Text.of("§5-----------------------------------------------------"));
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

    private void printEggResults(final Pokemon pokemon, final boolean wasEggChecked, final CommandSource src)
    {
        printToLog(2, "We have entered the executing method. Checking stats now!");

        // Set up IVs and matching math.
        final Stats IVs = pokemon.getStats();
        final int totalIVs = IVs.hp + IVs.attack + IVs.defence + IVs.specialAttack + IVs.specialDefence + IVs.speed;
        final int percentIVs = totalIVs * 100 / 186;

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
        if (showName)
        {
            src.sendMessage(Text.of("§eThere's a healthy §6" + pokemon.getSpecies().getLocalizedName() + "§e inside of this egg!"));
            if (explicitReveal)
                src.sendMessage(Text.EMPTY);
        }

        if (explicitReveal)
        {
            printToLog(2, "Explicit reveal enabled. Printing IVs, shiny-ness and other info.");

            // Format the IVs for use later, so we can print them.
            String ivs1 = String.valueOf(IVs.hp + " §2" + shortenedHP + statSeparator);
            String ivs2 = String.valueOf(IVs.attack + " §2" + shortenedAttack + statSeparator);
            String ivs3 = String.valueOf(IVs.defence + " §2" + shortenedDefense + statSeparator);
            String ivs4 = String.valueOf(IVs.specialAttack + " §2" + shortenedSpecialAttack + statSeparator);
            String ivs5 = String.valueOf(IVs.specialDefence + " §2" + shortenedSpecialDefense + statSeparator);
            String ivs6 = String.valueOf(IVs.speed + " §2" + shortenedSpeed);

            if (IVs.hp > 30)
                ivs1 = String.valueOf("§o") + ivs1;
            if (IVs.attack > 30)
                ivs2 = String.valueOf("§o") + ivs2;
            if (IVs.defence > 30)
                ivs3 = String.valueOf("§o") + ivs3;
            if (IVs.specialAttack > 30)
                ivs4 = String.valueOf("§o") + ivs4;
            if (IVs.specialDefence > 30)
                ivs5 = String.valueOf("§o") + ivs5;
            if (IVs.speed > 30)
                ivs6 = String.valueOf("§o") + ivs6;

            src.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)"));
            src.sendMessage(Text.of("§bIVs§f: §a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

            // Get a bunch of data from our PokemonMethods utility class.
            final EnumNature nature = pokemon.getNature();
            final EnumGrowth growth = pokemon.getGrowth();
            final String plusVal = '+' + pokemon.getNature().increasedStat.name();
            final String minusVal = '-' + pokemon.getNature().decreasedStat.name();

            // Set up a gender character. Console doesn't like Unicode genders, so if src is not a Player we'll use M/F/-.
            final char genderChar = PokemonMethods.getGenderCharacter(src, pokemon.getGender().getForm());

            // Show said data.
            final String extraInfo1 = String.valueOf("§bGender§f: " + genderChar +
                    "§f | §bSize§f: " + growth.name() + "§f | ");
            final String extraInfo2 = String.valueOf("§bNature§f: " + nature.name() +
                    "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
            src.sendMessage(Text.of(extraInfo1 + extraInfo2));

            // Lucky!
            if (pokemon.getIsShiny())
            {
                src.sendMessage(Text.EMPTY);
                src.sendMessage(Text.of("§6§lCongratulations! §r§eThis baby is shiny!"));
            }
        }
        else
        {
            printToLog(2, "Explicit reveal disabled, printing vague status.");

            // Figure out whether the baby is anything special. Uses a config-set percentage for stat checks.
            if (percentIVs >= babyHintPercentage && !pokemon.getIsShiny())
                src.sendMessage(Text.of("§6What's this? §eThis baby seems to be bursting with energy!"));
            else if (!(percentIVs >= babyHintPercentage) && pokemon.getIsShiny())
                src.sendMessage(Text.of("§6What's this? §eThis baby seems to have an odd sheen to it!"));
            else if (percentIVs >= babyHintPercentage && pokemon.getIsShiny())
                src.sendMessage(Text.of("§6What's this? §eSomething about this baby seems real special!"));
            else
                src.sendMessage(Text.of("§eThis baby seems to be fairly ordinary..."));
        }

        if (economyEnabled && commandCost > 0 && recheckIsFree)
        {
            if (wasEggChecked)
            {
                if (!pokemon.getIsShiny() || !explicitReveal)
                    src.sendMessage(Text.EMPTY);
                src.sendMessage(Text.of("§dThis egg was checked before, so this check was free!"));
            }
            else
            {
                printToLog(2, "First-time check, recheckIsFree enabled. Flagging for free rechecks.");
                pokemon.getPersistentData().setBoolean("hadEggChecked", true);
            }
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
