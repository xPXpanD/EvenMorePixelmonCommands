// heal pls
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.NbtKeys;
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

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

// Note: printBasicMessage is a static import for a function from CommonMethods, for convenience.
public class PokeCure implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean healParty, sneakyMode;

    // Set up some more variables for internal use.
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();
    private boolean calledRemotely;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("PokeCure", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        // Are we running from the console? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        ArrayList<String> nativeErrorArray = new ArrayList<>();
        if (commandAlias == null)
            nativeErrorArray.add("commandAlias");
        if (cooldownInSeconds == null)
            nativeErrorArray.add("cooldownInSeconds");
        if (altCooldownInSeconds == null)
            nativeErrorArray.add("altCooldownInSeconds");
        if (healParty == null)
            nativeErrorArray.add("healParty");
        if (sneakyMode == null)
            nativeErrorArray.add("sneakyMode");
        if (commandCost == null)
            nativeErrorArray.add("commandCost");

        if (!nativeErrorArray.isEmpty())
        {
            CommonMethods.printCommandNodeError("ShowStats", nativeErrorArray);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else
        {
            if (calledRemotely)
            {
                CommonMethods.printDebugMessage("PokeCure", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean canContinue = false, commandConfirmed = false;
            boolean hasOtherPerm = src.hasPermission("pixelupgrade.command.other.pokecure");
            Optional<String> arg1Optional = args.getOne("target, slot or confirmation");
            Optional<String> arg2Optional = args.getOne("slot or confirmation");
            Player target = null;
            int slot = 0;

            if (calledRemotely)
            {
                // Do we have an argument in the first slot?
                if (arg1Optional.isPresent())
                {
                    String arg1String = arg1Optional.get();

                    // Do we have a valid online player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();
                        canContinue = true;
                    }
                    else
                        src.sendMessage(Text.of("§4Error: §cInvalid target on first argument. See below."));
                }
                else
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. See below."));

                if (!canContinue)
                    printSyntaxHelper(src);
            }
            else
            {
                printToLog(2, "Starting argument check for player's input.");

                if (arg1Optional.isPresent() && arg1Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 1.");
                    commandConfirmed = true;
                }
                else if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                {
                    printToLog(2, "Discovered a confirmation flag in argument slot 2.");
                    commandConfirmed = true;
                }

                if (healParty)
                {
                    printToLog(2, "Party healing is on. Skipping ahead, checking confirmation there.");
                    canContinue = true;
                }
                else
                {
                    String errorString = "ERROR PLEASE REPORT";

                    if (arg1Optional.isPresent())
                    {
                        String arg1String = arg1Optional.get();

                        if (arg1String.matches("^[1-6]"))
                        {
                            printToLog(2, "Found a valid slot in argument 1.");
                            slot = Integer.parseInt(arg1String);
                            canContinue = true;
                        }
                        // Is our calling player allowed to check other people's Pokémon, and is arg 1 a valid target?
                        else if (hasOtherPerm && Sponge.getServer().getPlayer(arg1String).isPresent())
                        {
                            if (!src.getName().equalsIgnoreCase(arg1String))
                            {
                                printToLog(2, "Found a valid target in argument 1.");
                                target = Sponge.getServer().getPlayer(arg1String).get();
                            }
                            else
                                printToLog(2, "Player targeted self. Continuing.");
                        }
                        else
                        {
                            printToLog(1, "Invalid slot (or target?) on first argument. Exit.");

                            if (hasOtherPerm)
                                errorString = "§4Error: §cInvalid target or slot on first argument. See below.";
                            else
                                errorString = "§4Error: §cInvalid slot on first argument. See below.";
                        }
                    }
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
                    UUID playerUUID = ((Player) src).getUniqueId(); // why is the "d" in "Id" lowercase :(
                    long currentTime = System.currentTimeMillis();

                    if (!src.hasPermission("pixelupgrade.command.bypass.pokecure") && cooldownMap.containsKey(playerUUID))
                    {
                        long cooldownInMillis = cooldownInSeconds * 1000;
                        long timeDifference = currentTime - cooldownMap.get(playerUUID), timeRemaining;

                        if (src.hasPermission("pixelupgrade.command.altcooldown.pokecure"))
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

                    if (canContinue && !healParty)
                    {
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1]; // Should be safe, now.

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                            canContinue = false;
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to show off an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! You won't need to heal that."));
                            canContinue = false;
                        }
                    }

                    if (canContinue)
                    {
                        NBTTagCompound nbt = null; // Changed if needed.
                        if (!healParty)
                            nbt = storageCompleted.partyPokemon[slot - 1]; // Should be safe, now.

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
                                        {
                                            printToLog(1, "Healing player's party, and taking §3" +
                                                    costToConfirm + "§b coins.");
                                        }
                                        else
                                        {
                                            printToLog(1, "Healing slot §3" + slot +
                                                    "§b, and taking §3" + costToConfirm + "§b coins.");
                                        }

                                        cooldownMap.put(playerUUID, currentTime);
                                        doHeal(src, storageCompleted, nbt);
                                    }
                                    else
                                    {
                                        BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                                economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

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
                                printToLog(1, "Got cost but no confirmation; end of the line.");

                                if (healParty)
                                {
                                    // Is cost to confirm exactly one coin?
                                    if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                        src.sendMessage(Text.of("§6Warning: §eHealing a Pokémon costs §6one §ecoin."));
                                    else
                                    {
                                        src.sendMessage(Text.of("§6Warning: §eHealing a Pokémon costs §6" +
                                                costToConfirm + "§e coins."));
                                    }

                                    src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " -c"));
                                }
                                else
                                {
                                    // Is cost to confirm exactly one coin?
                                    if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                        src.sendMessage(Text.of("§6Warning: §eHealing your team costs §6one §ecoin."));
                                    else
                                    {
                                        src.sendMessage(Text.of("§6Warning: §eHealing your team costs §6" +
                                                costToConfirm + "§e coins."));
                                    }

                                    src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                                }
                            }
                        }
                        else
                        {
                            if (healParty)
                                printToLog(1, "Healing player's party. Config price is §30§b, taking nothing.");
                            else
                            {
                                printToLog(1, "Healing slot §3" + slot +
                                        "§b. Config price is §30§b, taking nothing.");
                            }

                            cooldownMap.put(playerUUID, currentTime);
                            doHeal(src, storageCompleted, nbt);
                        }
                    }
                }
            }
        }

        return CommandResult.success();
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(CommandSource src)
    {
        if (commandCost != 0)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }

    private void doHeal(CommandSource src, PlayerStorage storage, NBTTagCompound nbt)
    {
        if (healParty)
        {
            EntityPlayerMP playerEntity = (EntityPlayerMP) src;
            storage.healAllPokemon(playerEntity.getServerWorld());

            src.sendMessage(Text.of("§aYour party has been healed!"));
        }
        else
        {
            // Partially nicked from the "heal" method in Pixelmon, as that's private.
            nbt.setFloat(NbtKeys.HEALTH, (float) nbt.getInteger(NbtKeys.STATS_HP));
            nbt.setBoolean(NbtKeys.IS_FAINTED, false);
            nbt.removeTag("Status");

            int numberOfMoves = nbt.getInteger(NbtKeys.PIXELMON_NUMBER_MOVES);
            for (int i = 0; i < numberOfMoves; i++)
                nbt.setInteger(NbtKeys.PIXELMON_MOVE_PP + i, nbt.getInteger(NbtKeys.PIXELMON_MOVE_PPBASE + i));

            storage.sendUpdatedList();

            src.sendMessage(Text.of("§aThe chosen Pokémon has been healed."));
        }
    }
}
