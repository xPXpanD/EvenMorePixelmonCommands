// Forget berries!
package rs.expand.evenmorepixelmoncommands.commands;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rs.expand.evenmorepixelmoncommands.EMPC.economyEnabled;
import static rs.expand.evenmorepixelmoncommands.EMPC.economyService;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

public class ResetEVs implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer commandCost;

    // Set up a class name variable for internal use. We'll pass this to logging when showing a source is desired.
    private final String sourceName = this.getClass().getSimpleName();

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> commandErrorList = new ArrayList<>();
            if (commandAlias == null)
                commandErrorList.add("commandAlias");
            if (commandCost == null)
                commandErrorList.add("commandCost");

            if (!commandErrorList.isEmpty())
            {
                PrintingMethods.printCommandNodeError("ResetEVs", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (BattleRegistry.getBattle((EntityPlayerMP) src) != null)
                src.sendMessage(Text.of("§4Error: §cYou can't use this command while in a battle!"));
            else
            {
                final Player player = (Player) src;
                boolean commandConfirmed = false;
                final int slot;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printLocalError(src, "§4Error: §cNo arguments found. Please provide a slot.");
                    return CommandResult.empty();
                }
                else
                {
                    final String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid slot value. Valid values are 1-6.");
                        return CommandResult.empty();
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                // Get the player's party, and then get the Pokémon in the targeted slot.
                final Pokemon pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot - 1);

                if (pokemon == null)
                    src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                else if (pokemon.isEgg())
                    src.sendMessage(Text.of("§4Error: §cThat's an egg. Go hatch it, first."));
                else if (commandConfirmed)
                {
                    if (economyEnabled && commandCost > 0)
                    {
                        final BigDecimal costToConfirm = new BigDecimal(commandCost);
                        final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                        if (optionalAccount.isPresent())
                        {
                            final UniqueAccount uniqueAccount = optionalAccount.get();
                            final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                        costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                            if (transactionResult.getResult() == ResultType.SUCCESS)
                            {
                                resetPlayerEVs(pokemon, src);
                                printSourcedMessage(sourceName, "§bResetting EVs for player §3" + player.getName() + "§b, slot §3" + slot +
                                        "§b. Taking §3" + costToConfirm + "§b coins.");
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
                        printSourcedMessage(sourceName, "§bResetting EVs for player §3" + player.getName() + "§b, slot §3" + slot +
                                "§b.");

                        resetPlayerEVs(pokemon, src);
                    }
                }
                else
                {
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§6Warning: §eYou are about to reset this Pokémon's EVs to zero!"));
                    src.sendMessage(Text.EMPTY);

                    if (economyEnabled && commandCost > 0)
                        src.sendMessage(Text.of("§eResetting will cost §6" + commandCost + "§e coins!"));

                    src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                }
            }
        }
        else
            printSourcedError(sourceName,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

	// Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));

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

	private void resetPlayerEVs(final Pokemon pokemon, final CommandSource src)
    {
        final EVStore EVs = pokemon.getEVs();

        printSourcedMessage(sourceName, "§bPrinting old EVS: §3" +
                EVs.get(StatsType.HP) + "§b HP, §3" + EVs.get(StatsType.Attack) + "§b ATK, §3" +
                EVs.get(StatsType.Defence) + "§b DEF, §3" + EVs.get(StatsType.SpecialAttack) + "§b SP. ATK, §3" +
                EVs.get(StatsType.SpecialDefence) + "§b SP. DEF, §3" + EVs.get(StatsType.Speed) + "§b SPD"
        );

        EVs.set(StatsType.HP, 0);
        EVs.set(StatsType.Attack, 0);
        EVs.set(StatsType.Defence, 0);
        EVs.set(StatsType.SpecialAttack, 0);
        EVs.set(StatsType.SpecialDefence, 0);
        EVs.set(StatsType.Speed, 0);

        // Force an update.
        pokemon.markDirty(EnumUpdateType.EVs);

        src.sendMessage(Text.of("§aYour Pokémon had its EVs wiped!"));
    }
}