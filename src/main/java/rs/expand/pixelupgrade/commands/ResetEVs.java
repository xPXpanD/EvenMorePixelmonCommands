// Forget berries!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import rs.expand.pixelupgrade.utilities.CommonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class ResetEVs implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("ResetEVs", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            ArrayList<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                CommonMethods.printCommandNodeError("ResetEVs", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    if (commandCost > 0)
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
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
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to reset EVs on an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else if (commandConfirmed)
                        {
                            printToLog(2, "Command was confirmed, checking balances.");

                            if (commandCost > 0)
                            {
                                BigDecimal costToConfirm = new BigDecimal(commandCost);
                                Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                if (optionalAccount.isPresent())
                                {
                                    UniqueAccount uniqueAccount = optionalAccount.get();
                                    TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                    if (transactionResult.getResult() == ResultType.SUCCESS)
                                    {
                                        resetPlayerEVs(nbt, src);
                                        printToLog(1, "Reset EVs for slot §3" + slot +
                                                "§b, taking §3" + costToConfirm + "§b coins.");
                                    }
                                    else
                                    {
                                        BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
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
                                resetPlayerEVs(nbt, src);
                                printToLog(1, "Reset EVs for slot §3" + slot +
                                        "§b. Config price is §30§b, taking nothing.");
                            }
                        }
                        else
                        {
                            printToLog(1, "No confirmation provided, printing warning and aborting.");

                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§6Warning: §eYou are about to reset this Pokémon's EVs to zero!"));
                            if (commandCost > 0)
                                src.sendMessage(Text.of("§eResetting will cost §6" + commandCost + "§e coins!"));
                            src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        }
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

	private void resetPlayerEVs(NBTTagCompound nbt, CommandSource src)
    {
        int EVHP = nbt.getInteger(NbtKeys.EV_HP);
        int EVATT = nbt.getInteger(NbtKeys.EV_ATTACK);
        int EVDEF = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int EVSPATT = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int EVSPDEF = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int EVSPD = nbt.getInteger(NbtKeys.EV_SPEED);

        printToLog(1, "Command has been confirmed, printing old EVs...");
        printToLog(1, "HP: §3" + EVHP + "§b | ATK: §3" + EVATT + "§b | DEF: §3" + EVDEF +
                "§b | SPATK: §3" + EVSPATT + "§b | SPDEF: §3" + EVSPDEF + "§b | SPD: §3" + EVSPD);

        nbt.setInteger(NbtKeys.EV_HP, 0);
        nbt.setInteger(NbtKeys.EV_ATTACK, 0);
        nbt.setInteger(NbtKeys.EV_DEFENCE, 0);
        nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 0);
        nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 0);
        nbt.setInteger(NbtKeys.EV_SPEED, 0);

        if (nbt.getString("Nickname").equals(""))
            src.sendMessage(Text.of("§2" + nbt.getString("Name") + "§a had its EVs wiped!"));
        else
            src.sendMessage(Text.of("§aYour §2" + nbt.getString("Nickname") + "§a had its EVs wiped!"));
    }

    private void printSyntaxHelper(CommandSource src)
    {
        if (commandCost != 0)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }
}