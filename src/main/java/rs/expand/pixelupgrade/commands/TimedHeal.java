// heal pls
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.CommandBlock;
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
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class TimedHeal implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean healParty, sneakyMode;

    // Set up some more variables for internal use.
    private boolean calledRemotely;
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

    // Allows us to redirect printed messages away from command blocks, and into the console if need be.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printBasicMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    {
        if (!calledRemotely)
            PrintingMethods.printDebugMessage("TimedHeal", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        final List<String> nativeErrorArray = new ArrayList<>();
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
            PrintingMethods.printCommandNodeError("TimedHeal", nativeErrorArray);
            sendCheckedMessage(src,"§4Error: §cThis command's config is invalid! Please report to staff.");
        }
        else
        {
            if (calledRemotely)
            {
                if (src instanceof CommandBlock)
                {
                    PrintingMethods.printDebugMessage("TimedHeal", 1,
                            "Called by command block, starting. Silencing logger messages.");
                }
                else
                {
                    PrintingMethods.printDebugMessage("TimedHeal", 1,
                            "Called by console, starting. Silencing further log messages.");
                }
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            int slot = 0;
            final long currentTime = System.currentTimeMillis() / 1000; // Grab seconds.
            boolean canContinue = true, commandConfirmed = false, hitCooldown = false;
            final boolean hasOtherPerm = src.hasPermission("pixelupgrade.command.other.timedheal");
            final Optional<String> arg1Optional = args.getOne("target/slot/confirmation");
            final Optional<String> arg2Optional = args.getOne("slot/confirmation");
            String errorString = "§4There's an error message missing, please report this!";
            UUID playerUUID = null;
            Player target = null;

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
                        errorString = "§4Error: §cInvalid target on first argument. See below.";
                        canContinue = false;
                    }
                }
                else
                {
                    errorString = "§4Error: §cNo arguments found. See below.";
                    canContinue = false;
                }

                if (canContinue && arg2Optional.isPresent())
                {
                    final String arg2String = arg2Optional.get();

                    // Do we have a slot?
                    if (arg2String.matches("^[1-6]"))
                        slot = Integer.parseInt(arg2String);
                    else
                    {
                        errorString = "§4Error: §cInvalid slot on optional second argument. See below.";
                        canContinue = false;
                    }
                }
            }
            else
            {
                // Run this logic first, so we can avoid wasting resources on argument checks if they're not necessary.
                printToLog(2, "Checking if player is (still?) on a cooldown.");

                //noinspection ConstantConditions - safe to do, we've already verified we're not console/a command block.
                playerUUID = ((Player) src).getUniqueId(); // why is the "d" in "Id" lowercase :(

                if (!src.hasPermission("pixelupgrade.command.bypass.timedheal") && cooldownMap.containsKey(playerUUID))
                {
                    final boolean hasAltPerm = src.hasPermission("pixelupgrade.command.altcooldown.timedheal");
                    final long timeDifference = currentTime - cooldownMap.get(playerUUID);
                    final long timeRemaining;

                    if (hasAltPerm)
                    {
                        printToLog(2, "Player has the alternate cooldown permission.");
                        timeRemaining = altCooldownInSeconds - timeDifference;
                    }
                    else
                    {
                        printToLog(2, "Player has the normal cooldown permission.");
                        timeRemaining = cooldownInSeconds - timeDifference;
                    }

                    if (hasAltPerm && cooldownMap.get(playerUUID) > currentTime - altCooldownInSeconds ||
                            !hasAltPerm && cooldownMap.get(playerUUID) > currentTime - cooldownInSeconds)
                    {
                        if (timeRemaining == 1)
                        {
                            printToLog(1, "§3" + src.getName() + "§b has to wait §3one §bmore second. Exit.");
                            errorString = "§4Error: §cYou must wait §4one §cmore second. You can do this!";
                        }
                        else
                        {
                            printToLog(1, "§3" + src.getName() + "§b has to wait another §3" +
                                    timeRemaining + "§b seconds. Exit.");

                            if (timeRemaining > 60)
                                errorString = "§4Error: §cYou must wait another §4" + ((timeRemaining / 60) + 1) + "§c minutes.";
                            else
                                errorString = "§4Error: §cYou must wait another §4" + timeRemaining + "§c seconds.";
                        }

                        hitCooldown = true;
                        canContinue = false;
                    }
                }
                else if (src.hasPermission("pixelupgrade.command.bypass.timedheal"))
                    printToLog(2, "Player has the bypass permission. Moving on.");

                if (canContinue)
                {
                    printToLog(2, "Starting argument check for player's input.");

                    // Ugly, but it'll do for now... Doesn't seem like my usual way of getting flags will work here.
                    final Optional<String> arg3Optional = args.getOne("confirmation");

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
                    else if (arg3Optional.isPresent() && arg3Optional.get().equalsIgnoreCase("-c"))
                    {
                        printToLog(2, "Discovered a confirmation flag in argument slot 3.");
                        commandConfirmed = true;
                    }

                    if (healParty)
                    {
                        if (hasOtherPerm)
                            printToLog(2, "Party healing is on. Checking if we have a target and the perm.");
                        else
                            printToLog(2, "Party healing is on. No other perm, so let's skip.");

                        if (arg1Optional.isPresent() && !arg1Optional.get().equalsIgnoreCase("-c"))
                        {
                            final String arg1String = arg1Optional.get();

                            if (hasOtherPerm)
                            {
                                if (Sponge.getServer().getPlayer(arg1String).isPresent())
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
                                    printToLog(1, "Invalid target on first argument. Exit.");
                                    errorString = "§4Error: §cYour target could not be found. See below.";
                                    canContinue = false;
                                }
                            }
                        }
                    }
                    else
                    {
                        if (arg1Optional.isPresent() && !arg1Optional.get().equalsIgnoreCase("-c"))
                        {
                            final String arg1String = arg1Optional.get();

                            if (arg1String.matches("^[1-6]"))
                            {
                                printToLog(2, "Found a valid slot in argument 1.");
                                slot = Integer.parseInt(arg1String);
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
                                    errorString = "§4Error: §cInvalid target or slot. See below.";
                                else
                                    errorString = "§4Error: §cInvalid slot. See below.";

                                canContinue = false;
                            }
                        }

                        // Can we continue, and do we not have a slot already? Check arg 2 for one.
                        if (canContinue && slot == 0)
                        {
                            if (arg2Optional.isPresent() && !arg2Optional.get().equalsIgnoreCase("-c"))
                            {
                                printToLog(2, "There's something in the second argument slot, and we need it!");
                                final String arg2String = arg2Optional.get();

                                // Do we have a slot?
                                if (arg2String.matches("^[1-6]"))
                                {
                                    printToLog(2, "Found a valid slot in argument 2. Moving to execution.");
                                    slot = Integer.parseInt(arg2String);
                                }
                                else
                                {
                                    printToLog(1, "Invalid slot on second argument. Exit.");
                                    errorString = "§4Error: §cInvalid slot on second argument. See below.";
                                    canContinue = false;
                                }
                            }
                            else
                            {
                                printToLog(1, "Missing slot on second argument. Exit.");
                                errorString = "§4Error: §cPlease provide a slot. See below.";
                                canContinue = false;
                            }
                        }
                    }
                }
            }

            if (!canContinue)
            {
                sendCheckedMessage(src,errorString);
                
                if (!hitCooldown)
                    printSyntaxHelper(src, hasOtherPerm);
            }
            // Do some battle checks. Only hittable if we got called by an actual Player.
            else if (target == null && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                printToLog(0, "Player tried to heal own Pokémon while in a battle. Exit.");
                sendCheckedMessage(src, "§4Error: §cYou can't use this command while in a battle!");
            }
            else if (target != null && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
            {
                printToLog(0, "Target was in a battle, cannot proceed. Exit."); // Swallowed if console.
                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
            }
            else
            {
                // At this point we should always have a valid input, whatever the settings may be.
                // The only thing we'll require now is confirmation, if applicable.
                final Optional<PlayerStorage> storage;
                if (target != null)
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
                else
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                if (!storage.isPresent())
                {
                    if (target != null)
                        printToLog(0, "§4" + target.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                    else
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");

                    sendCheckedMessage(src,"§4Error: §cNo Pixelmon storage found. Please contact staff!");
                }
                else
                {
                    final PlayerStorage storageCompleted = storage.get();
                    final NBTTagCompound nbt;
                    if (slot != 0)
                        nbt = storageCompleted.partyPokemon[slot - 1];
                    else
                        nbt = null;

                    if (!calledRemotely && !healParty || calledRemotely && slot != 0)
                    {
                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");

                            if (target != null)
                                sendCheckedMessage(src,"§4Error: §cYour target does not have anything in that slot!");
                            else
                                sendCheckedMessage(src,"§4Error: §cYou don't have anything in that slot!");

                            canContinue = false;
                        }
                        else if (nbt.getBoolean(NbtKeys.IS_EGG))
                        {
                            printToLog(1, "Tried to show off an egg. Exit.");
                            sendCheckedMessage(src,"§4Error: §cThat's an egg! You won't need to heal that.");
                            canContinue = false;
                        }
                    }

                    if (canContinue)
                    {
                        if (!calledRemotely && commandCost > 0)
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
                                        if (target == null)
                                        {
                                            if (healParty)
                                            {
                                                printToLog(1, "Healing player's party, and taking §3" +
                                                        costToConfirm + "§b coins.");
                                            }
                                            else
                                            {
                                                printToLog(1, "Healing player slot §3" + slot +
                                                        "§b, and taking §3" + costToConfirm + "§b coins.");
                                            }
                                        }
                                        else
                                        {
                                            if (healParty)
                                            {
                                                printToLog(1, "Healing §3" + target.getName() +
                                                        "§b's party, and taking §3" + costToConfirm + "§b coins.");
                                            }
                                            else
                                            {
                                                printToLog(1, "Healing slot §3" + slot + "§b for §3" +
                                                        target.getName() + "§b. Taking §3" + costToConfirm + "§b coins.");
                                            }
                                        }

                                        cooldownMap.put(playerUUID, currentTime);
                                        doHeal(src, target, slot, storageCompleted, nbt);
                                    }
                                    else
                                    {
                                        final BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                                economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                        printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                                "§b, and we're lacking §3" + balanceNeeded);
                                        sendCheckedMessage(src,"§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this.");
                                    }
                                }
                                else
                                {
                                    printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                                    sendCheckedMessage(src,"§4Error: §cNo economy account found. Please contact staff!");
                                }
                            }
                            else
                            {
                                printToLog(1, "Got cost but no confirmation; end of the line.");

                                if (healParty)
                                {
                                    // Is cost to confirm exactly one coin?
                                    if (target == null)
                                    {
                                        if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                            sendCheckedMessage(src,"§6Warning: §eHealing your team costs §6one §ecoin.");
                                        else
                                        {
                                            sendCheckedMessage(src,"§6Warning: §eHealing your team costs §6" +
                                                    costToConfirm + "§e coins.");
                                        }

                                        sendCheckedMessage(src,"§2Ready? Type: §a/" + commandAlias + " -c");
                                    }
                                    else
                                    {
                                        if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                            sendCheckedMessage(src,"§6Warning: §eHealing §6" + target.getName() +
                                                    "§e's team costs §6one §ecoin.");
                                        else
                                        {
                                            sendCheckedMessage(src,"§6Warning: §eHealing §6" + target.getName() +
                                                    "§e's team costs §6" + costToConfirm + "§e coins.");
                                        }

                                        sendCheckedMessage(src,"§2Ready? Type: §a/" + commandAlias + " " +
                                                target.getName() + " -c");
                                    }
                                }
                                else
                                {
                                    // Is cost to confirm exactly one coin?
                                    if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                        sendCheckedMessage(src,"§6Warning: §eHealing this Pokémon costs §6one §ecoin.");
                                    else
                                    {
                                        sendCheckedMessage(src,"§6Warning: §eHealing this Pokémon costs §6" +
                                                costToConfirm + "§e coins.");
                                    }

                                    if (target == null)
                                    {
                                        sendCheckedMessage(src,"§2Ready? Type: §a/" + commandAlias + " " +
                                                slot + " -c");
                                    }
                                    else
                                    {
                                        sendCheckedMessage(src,"§2Ready? Type: §a/" + commandAlias + " " +
                                                target.getName() + " " + slot + " -c");
                                    }
                                }
                            }
                        }
                        else
                        {
                            if (!calledRemotely)
                            {
                                if (target == null)
                                {
                                    if (healParty)
                                        printToLog(1, "Healing player's party. Config price is §30§b, taking nothing.");
                                    else
                                    {
                                        printToLog(1, "Healing slot §3" + slot +
                                                "§b. Config price is §30§b, taking nothing.");
                                    }
                                }
                                else
                                {
                                    if (healParty)
                                    {
                                        printToLog(1, "Healing §3" + target.getName() +
                                                "§b's party. Config price is §30§b, taking nothing.");
                                    }
                                    else
                                    {
                                        printToLog(1, "Healing slot §3" + slot + "§b for §3" +
                                                target.getName() + "§b. Config price is §30§b.");
                                    }
                                }

                                cooldownMap.put(playerUUID, currentTime);
                            }

                            doHeal(src, target, slot, storageCompleted, nbt);
                        }
                    }
                }
            }
        }

        return CommandResult.success();
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(final CommandSource src, final boolean hasOtherPerm)
    {
        if (calledRemotely)
            sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " <target> [slot? 1-6]");
        else
        {
            final String confirmString;
            if (commandCost != 0)
                confirmString = " {-c to confirm}";
            else
                confirmString = "";

            if (healParty)
            {
                if (hasOtherPerm)
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " [target?]" + confirmString);
                else
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " " + confirmString);
            }
            else
            {
                if (hasOtherPerm)
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " [target?] <slot, 1-6>" + confirmString);
                else
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " <slot, 1-6>" + confirmString);
            }
        }
    }

    private void doHeal(final CommandSource src, final Player target, final int slot, final PlayerStorage storage, final NBTTagCompound nbt)
    {
        if (healParty && target == null)
        {
            final EntityPlayerMP playerEntity = (EntityPlayerMP) src;
            storage.healAllPokemon(playerEntity.getServerWorld());

            sendCheckedMessage(src,"§aYour party has been healed!");
        }
        else if (calledRemotely && slot == 0 || !calledRemotely && healParty)
        {
            final EntityPlayerMP playerEntity = (EntityPlayerMP) target;
            storage.healAllPokemon(playerEntity.getServerWorld());

            if (calledRemotely && sneakyMode)
                sendCheckedMessage(src,"§aThe target's party has been silently healed!");
            else
                sendCheckedMessage(src,"§aThe target's party has been healed!");

            if (calledRemotely && !sneakyMode)
                target.sendMessage(Text.of("§aYour party was healed remotely!"));
            else if (!calledRemotely)
                target.sendMessage(Text.of("§aYour party was healed by §2" + src.getName() + "§a!"));
        }
        else
        {
            // Partially nicked from the "heal" method in Pixelmon, as that's private.
            nbt.setFloat(NbtKeys.HEALTH, (float) nbt.getInteger(NbtKeys.STATS_HP));
            nbt.setBoolean(NbtKeys.IS_FAINTED, false);
            nbt.removeTag("Status");

            final int numberOfMoves = nbt.getInteger(NbtKeys.PIXELMON_NUMBER_MOVES);
            for (int i = 0; i < numberOfMoves; i++)
                nbt.setInteger(NbtKeys.PIXELMON_MOVE_PP + i, nbt.getInteger(NbtKeys.PIXELMON_MOVE_PPBASE + i));

            storage.sendUpdatedList();

            if (target != null)
            {
                if (calledRemotely && sneakyMode)
                    sendCheckedMessage(src,"§aThe target's slot §2" + slot + " §aPokémon has been silently healed!");
                else
                    sendCheckedMessage(src,"§aThe target's slot §2" + slot + " §aPokémon has been healed!");

                if (calledRemotely && !sneakyMode)
                    target.sendMessage(Text.of("§aThe Pokémon in slot §2" + slot + "§a was healed remotely!"));
                else if (!calledRemotely)
                    target.sendMessage(Text.of("§aThe Pokémon in slot §2" + slot + "§a was healed by §2" + src.getName() + "§a!"));
            }
            else
                sendCheckedMessage(src,"§aThe Pokémon in slot §2" + slot + " §ahas been healed!");
        }
    }
}
