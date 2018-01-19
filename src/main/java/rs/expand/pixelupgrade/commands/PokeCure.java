package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
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
import org.spongepowered.api.text.channel.MessageChannel;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

// Note: printUnformattedMessage is a static import for a function from CommonMethods, for convenience.
public class PokeCure implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds;
    public static Integer altCooldownInSeconds;
    public static Boolean healParty;
    public static Boolean cureAilments;
    public static Integer commandCost;

    // Set up some more variables for internal use.
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printFormattedMessage("ShowStats", debugNum, inputString); }

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
            if (healParty == null)
                nativeErrorArray.add("healParty");
            if (cureAilments == null)
                nativeErrorArray.add("cureAilments");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                CommonMethods.printNodeError("ShowStats", nativeErrorArray, 1);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!healParty)
                {
                    if (!args.<String>getOne("slot").isPresent())
                    {
                        printToLog(1, "No arguments provided. Exit.");

                        if (commandCost > 0)
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
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

                            if (commandCost > 0)
                                src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                            printCorrectHelper(commandCost, src);
                            checkAndAddFooter(commandCost, src);

                            canContinue = false;
                        }
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

                        if (!healParty)
                        {
                            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                            if (nbt == null)
                            {
                                printToLog(1, "No NBT found in slot, probably empty. Exit.");
                                src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                                canContinue = false;
                            }
                            else if (nbt.getBoolean("isEgg"))
                            {
                                printToLog(1, "Tried to show off an egg. Exit.");
                                src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                                canContinue = false;
                            }
                        }

                        if (canContinue)
                        {
                            UUID playerUUID = ((Player) src).getUniqueId(); // why is the "d" in "Id" lowercase :(
                            long currentTime = System.currentTimeMillis();
                            long cooldownInMillis = cooldownInSeconds * 1000;

                            if (!src.hasPermission("pixelupgrade.command.pokecure.bypasscooldown") && cooldownMap.containsKey(playerUUID))
                            {
                                // Time is stored in milliseconds, so /1000.
                                long timeDifference = currentTime - cooldownMap.get(playerUUID), timeRemaining;
                                if (src.hasPermission("pixelupgrade.command.pokecure.altcooldown"))
                                    timeRemaining = altCooldownInSeconds - timeDifference / 1000;
                                else
                                    timeRemaining = cooldownInSeconds - timeDifference / 1000;

                                if (cooldownMap.get(playerUUID) > currentTime - cooldownInMillis)
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
                                                if (healParty)
                                                    printToLog(1, "Healing player's party, and taking §3" +
                                                            costToConfirm + "§b coins.");
                                                else
                                                    printToLog(1, "Healing slot §3" + slot +
                                                            "§b, and taking §3" + costToConfirm + "§b coins.");

                                                cooldownMap.put(playerUUID, currentTime);
                                                doHeal();
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                                        economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                                printToLog(1, "Not enough coins! Cost: §3" +
                                                        costToConfirm + "§b, lacking: §3" + balanceNeeded);
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

                                        if (healParty)
                                            src.sendMessage(Text.of("§6Warning: §eHealing a Pokémon's wounds costs §6" +
                                                    costToConfirm + "§e coins."));
                                        else
                                            src.sendMessage(Text.of("§6Warning: §eHealing your team's wounds costs §6" +
                                                    costToConfirm + "§e coins."));
                                        src.sendMessage(Text.of("§2Ready? Type: §a" + commandAlias + " " + slot + " -c"));
                                    }
                                }
                                else
                                {
                                    if (healParty)
                                        printToLog(1, "Healing player's party. Config price is §30§b, taking nothing.");
                                    else
                                        printToLog(1, "Healing slot §3" + slot + "§b. Config price is §30§b, taking nothing.");

                                    cooldownMap.put(playerUUID, currentTime);
                                    doHeal();
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

    private void doHeal()
    {


        MessageChannel.TO_PLAYERS.send(Text.of("§7------------------------TODO-------------------------"));
    }
}
