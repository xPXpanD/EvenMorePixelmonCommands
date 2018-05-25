// Ohhhh, the cheeky jokes I could make here.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.economyEnabled;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

// TODO: Update the economy setup to be in line with most other economy-using commands.
public class SwitchGender implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("SwitchGender", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("SwitchGender", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                printToLog(0, "Called by player §4" + src.getName() + "§c, but in a battle. Exit.");
                src.sendMessage(Text.of("§4Error: §cYou can't use this command while in a battle!"));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                final Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
                    addHelperAndFooter(src);

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
                        addHelperAndFooter(src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    final Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + player.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        final PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        final NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean(NbtKeys.IS_EGG))
                        {
                            printToLog(1, "Tried to switch gender on an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else if (nbt.getInteger(NbtKeys.GENDER) != 0 && nbt.getInteger(NbtKeys.GENDER) != 1)
                        {
                            printToLog(1, "Tried to switch gender on a genderless (or broken?) Pokémon. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou can only switch genders on a gendered Pokémon!"));
                        }
                        else
                        {
                            if (commandConfirmed)
                            {
                                printToLog(2, "Command was confirmed, checking balances.");
                                final int gender = nbt.getInteger(NbtKeys.GENDER);

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
                                            printToLog(1, "Switched gender for slot §3" + slot +
                                                    "§b, taking §3" + costToConfirm + "§b coins.");
                                            switchGenders(nbt, src, gender);
                                            storageCompleted.sendUpdatedList();
                                        }
                                        else
                                        {
                                            final BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
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
                                    if (economyEnabled)
                                    {
                                        printToLog(1, "Switching gender for slot §3" + slot +
                                                "§b. Config price is §30§b, taking nothing.");
                                    }
                                    else
                                    {
                                        printToLog(1, "Switching gender for slot §3" + slot +
                                                "§b. No economy, so we skipped eco checks.");
                                    }

                                    switchGenders(nbt, src, gender);
                                    storageCompleted.sendUpdatedList();
                                }
                            }
                            else
                            {
                                printToLog(1, "No confirmation provided, printing warning and aborting.");

                                src.sendMessage(Text.of("§5-----------------------------------------------------"));
                                src.sendMessage(Text.of("§6Warning: §eYou are about to switch this Pokémon's gender!"));
                                src.sendMessage(Text.EMPTY);

                                if (economyEnabled && commandCost > 0)
                                    src.sendMessage(Text.of("§eSwitching will cost §6" + commandCost + "§e coins!"));

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

    private void switchGenders(final NBTTagCompound nbt, final CommandSource src, final int genderNum)
    {
        final String pokemonName;
        final String genderName;
        if (nbt.getString("Nickname").equals(""))
            pokemonName = nbt.getString("Name");
        else
            pokemonName = nbt.getString("Nickname");

        switch (genderNum)
        {
            case 0: // male
                nbt.setInteger(NbtKeys.GENDER, 1); // female
                genderName = "female"; break;
            default: // female, no worries here as we check for non-binary Pokémon earlier
                nbt.setInteger(NbtKeys.GENDER, 0); // male
                genderName = "male"; break;
        }

        src.sendMessage(Text.of("§aMagic! Your §2" + pokemonName + "§a just turned into a " + genderName + "!"));
    }

    private void addHelperAndFooter(final CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
        if (economyEnabled && commandCost > 0)
            src.sendMessage(Text.of("§eConfirming will cost you §6" + commandCost + "§e coins."));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
