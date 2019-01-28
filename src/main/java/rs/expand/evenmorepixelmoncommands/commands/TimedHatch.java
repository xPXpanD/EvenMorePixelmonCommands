// Seemed like a good thing to have, and now it exists! Fancier than the PE version, but also heavier.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import java.math.BigDecimal;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import static rs.expand.evenmorepixelmoncommands.EMPC.*;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

public class TimedHatch implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean hatchParty, sneakyMode;

    // Set up some more variables for internal use.
    private String sourceName = this.getClass().getSimpleName();
    private UUID playerUUID;
    private boolean calledRemotely;
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

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
        if (sneakyMode == null)
            nativeErrorArray.add("sneakyMode");
        if (commandCost == null)
            nativeErrorArray.add("commandCost");

        if (!nativeErrorArray.isEmpty())
        {
            PrintingMethods.printCommandNodeError(sourceName, nativeErrorArray);
            sendCheckedMessage(src,"§4Error: §cThis command's config is invalid! Please report to staff.");
        }
        else
        {
            int slot = 0;
            final long currentTime = System.currentTimeMillis() / 1000; // Grab seconds.
            boolean commandConfirmed = false;
            final Optional<String> arg1Optional = args.getOne("target/slot/confirmation");
            final Optional<String> arg2Optional = args.getOne("slot/confirmation");
            Player target = null, player = null;

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
                        printLocalError(src, "§4Error: §cInvalid target on first argument. See below.", false);
                        return CommandResult.empty();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cNo arguments found. See below.", false);
                    return CommandResult.empty();
                }

                // Do we have an argument in the second slot?
                if (arg2Optional.isPresent())
                {
                    final String arg2String = arg2Optional.get();

                    // Do we have a Pokémon slot?
                    if (arg2String.matches("^[1-6]"))
                        slot = Integer.parseInt(arg2String);
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid slot on optional second argument. See below.", false);
                        return CommandResult.empty();
                    }
                }
            }
            else
            {
                //noinspection ConstantConditions - safe, we've already verified we're not console/a command block...
                player = (Player) src;
                playerUUID = player.getUniqueId(); // why is the "d" in "Id" lowercase :(

                if (!src.hasPermission("empc.command.bypass.timedhatch") && cooldownMap.containsKey(playerUUID))
                {
                    final boolean hasAltPerm = src.hasPermission("empc.command.altcooldown.timedhatch");
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

                if (hatchParty)
                {
                    // Do we have an argument in the first argument slot?
                    // This can be a player name, a confirmation flag or nothing.
                    if (arg1Optional.isPresent())
                    {
                        final String argString = arg1Optional.get();

                        // Do we have a confirmation flag?
                        if (argString.equalsIgnoreCase("-c") && commandCost != 0)
                            commandConfirmed = true;
                        else if (src.hasPermission("empc.command.other.timedhatch"))
                        {
                            if (Sponge.getServer().getPlayer(argString).isPresent()) // Do we have a valid online player?
                            {
                                // Check if the player is targeting themselves. (if they are, just let target stay null)
                                if (!argString.equalsIgnoreCase(player.getName()))
                                    target = Sponge.getServer().getPlayer(argString).get();
                            }
                            else
                            {
                                printLocalError(src, "§4Error: §cInvalid target or slot on first argument. See below.", false);
                                return CommandResult.empty();
                            }
                        }
                        else
                        {
                            printLocalError(src, "§4Error: §cInvalid slot on first argument. See below.", false);
                            return CommandResult.empty();
                        }
                    }

                    // Do we have an argument in the second argument slot, and is it a flag?
                    if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                        commandConfirmed = true;
                }
                else
                {
                    // Do we have an argument in the first argument slot?
                    // This can be a Pokémon slot, a player name or nothing.
                    if (arg1Optional.isPresent())
                    {
                        final String argString = arg1Optional.get();

                        if (argString.matches("^[1-6]")) // Do we have a valid slot?
                            slot = Integer.parseInt(argString);
                        else if (src.hasPermission("empc.command.other.timedhatch"))
                        {
                            if (Sponge.getServer().getPlayer(argString).isPresent()) // Do we have a valid online player?
                            {
                                // Check if the player is targeting themselves. (if they are, just let target stay null)
                                if (!argString.equalsIgnoreCase(player.getName()))
                                    target = Sponge.getServer().getPlayer(argString).get();
                            }
                            else
                            {
                                printLocalError(src, "§4Error: §cInvalid target or slot on first argument. See below.", false);
                                return CommandResult.empty();
                            }
                        }
                        else
                        {
                            printLocalError(src, "§4Error: §cInvalid slot on first argument. See below.", false);
                            return CommandResult.empty();
                        }
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cNo arguments found. See below.", false);
                        return CommandResult.empty();
                    }

                    // Do we have an argument in the second argument slot?
                    if (arg2Optional.isPresent())
                    {
                        final String argString = arg2Optional.get();

                        // Has no slot been defined yet?
                        if (slot == 0)
                        {
                            if (argString.matches("^[1-6]")) // Do we have a valid slot?
                                slot = Integer.parseInt(argString);
                            else if (argString.equalsIgnoreCase("-c") && commandCost != 0 && hatchParty)
                                commandConfirmed = true;
                            else
                            {
                                printLocalError(src, "§4Error: §cInvalid slot on second argument. See below.", false);
                                return CommandResult.empty();
                            }
                        }
                        else if (argString.equalsIgnoreCase("-c"))
                            commandConfirmed = true;
                    }
                    else if (slot == 0)
                    {
                        printLocalError(src, "§4Error: §cNo slot found. See below.", false);
                        return CommandResult.empty();
                    }

                    // Do we have an argument in the third slot? A bit ugly, but it'll do.
                    final Optional<String> arg3Optional = args.getOne("confirmation");
                    if (arg3Optional.isPresent() && arg3Optional.get().equalsIgnoreCase("-c"))
                        commandConfirmed = true;
                }
            }

            // Is the player in a battle?
            if (target == null && !calledRemotely && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
                sendCheckedMessage(src, "§4Error: §cYou can't use this command while in a battle!");
            // Is the chosen target in a battle?
            else if (target != null && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
            else
            {
                // At this point we should always have a valid input. Now we just need confirmation, if applicable.
                // See whose storage we need to access.
                final PartyStorage party;
                if (target != null)
                    party = Pixelmon.storageManager.getParty((EntityPlayerMP) target);
                else
                    party = Pixelmon.storageManager.getParty((EntityPlayerMP) src);

                // Let's see if we have a specific Pokémon, and if so, where it's at. Prepare for a party check otherwise.
                final Pokemon pokemon = slot != 0 ? party.get(slot - 1) : null;

                if (!hatchParty)
                {
                    if (pokemon == null) // Did we actually get a specific Pokémon from the slot/our checks? If not, end.
                    {
                        if (target != null)
                            sendCheckedMessage(src,"§4Error: §cYour target does not have anything in that slot!");
                        else
                            sendCheckedMessage(src,"§4Error: §cYou don't have anything in that slot!");

                        return CommandResult.empty();
                    }
                    else if (!pokemon.isEgg()) // Is the Pokémon we got not an egg?
                    {
                        sendCheckedMessage(src,"§4Error: §cThat's not an egg. Don't hatch actual Pokémon, kids!");
                        return CommandResult.empty();
                    }
                }

                if (economyEnabled && !calledRemotely && commandCost > 0)
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
                                // Create a cooldown for the calling player.
                                cooldownMap.put(playerUUID, currentTime);

                                if (target == null)
                                {
                                    if (hatchParty)
                                    {
                                        printSourcedMessage(sourceName, "Hatching player's party, and taking §3" +
                                                costToConfirm + "§b coins.");

                                        hatchParty(src, null, party);
                                    }
                                    else
                                    {
                                        printSourcedMessage(sourceName, "Hatching player slot §3" + slot +
                                                "§b, and taking §3" + costToConfirm + "§b coins.");

                                        //noinspection ConstantConditions - safe, just too complicated
                                        hatchEgg(src, null, pokemon);
                                    }
                                }
                                else
                                {
                                    if (hatchParty)
                                    {
                                        printSourcedMessage(sourceName, "Player §3" + player.getName() + " §bis hatching §3" +
                                                target.getName() + "§b's party. Taking §3" + costToConfirm + "§b coins.");

                                        hatchParty(src, target, party);
                                    }
                                    else
                                    {
                                        printSourcedMessage(sourceName, "Player §3" + player.getName() + " §bis hatching slot §3" +
                                                slot + "§b for §3" + target.getName() + "§b. Taking §3" + costToConfirm + "§b coins.");

                                        //noinspection ConstantConditions - safe, just too complicated
                                        hatchEgg(src, target, pokemon);
                                    }
                                }
                            }
                            else
                            {
                                final BigDecimal balanceNeeded = uniqueAccount.getBalance(
                                        economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                sendCheckedMessage(src,"§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this.");
                            }
                        }
                        else
                        {
                            printSourcedError(sourceName, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                            sendCheckedMessage(src,"§4Error: §cNo economy account found. Please contact staff!");
                        }
                    }
                    else
                    {
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

                        if (hatchParty)
                        {
                            // Is cost to confirm exactly one coin?
                            if (target == null)
                            {
                                if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                    sendCheckedMessage(src,"§6Warning: §eHatching your team costs §6one §ecoin.");
                                else
                                {
                                    sendCheckedMessage(src,"§6Warning: §eHatching your team costs §6" +
                                            costToConfirm + "§e coins.");
                                }

                                sendCheckedMessage(src,"§2Ready? Type: §a/" + commandAlias + " -c");
                            }
                            else
                            {
                                if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                    sendCheckedMessage(src,"§6Warning: §eHatching §6" + target.getName() +
                                            "§e's team costs §6one §ecoin.");
                                else
                                {
                                    sendCheckedMessage(src,"§6Warning: §eHatching §6" + target.getName() +
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
                                sendCheckedMessage(src,"§6Warning: §eHatching this egg costs §6one §ecoin.");
                            else
                            {
                                sendCheckedMessage(src,"§6Warning: §eHatching this egg costs §6" +
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

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    }
                }
                else
                {
                    if (!calledRemotely)
                    {

                        if (target == null)
                        {
                            if (hatchParty)
                                hatchParty(src, null, party);
                            else //noinspection ConstantConditions - safe, just too complicated
                                hatchEgg(src, null, pokemon);
                        }
                        else
                        {
                            if (hatchParty)
                            {
                                //noinspection ConstantConditions - calledRemotely guarantees this is safe
                                printSourcedMessage(sourceName, "Called by §3" + player.getName() +
                                        "§b, hatching §3" + target.getName() + "§b's party.");

                                hatchParty(src, target, party);
                            }
                            else
                            {
                                //noinspection ConstantConditions - calledRemotely guarantees this is safe
                                printSourcedMessage(sourceName, "Called by §3" + player.getName() +
                                        "§b, hatching slot §3" +  slot + "§b for §3" + target.getName() + "§b.");

                                //noinspection ConstantConditions - safe, just too complicated
                                hatchEgg(src, target, pokemon);
                            }
                        }

                        cooldownMap.put(playerUUID, currentTime);
                    }
                    else
                    {
                        //noinspection ConstantConditions - safe, just too complicated
                        printSourcedMessage(sourceName, "Called from remote source, hatching §3" +
                                target.getName() + "§b's party.");

                        hatchParty(src, target, party);
                    }
                }
            }
        }

        return CommandResult.success();
    }

    // Redirect messages away from command blocks and into the console if need be. Prevents useless spam in the block UI.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printUnformattedMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input, final boolean hitCooldown)
    {
        sendCheckedMessage(src, "§5-----------------------------------------------------");
        sendCheckedMessage(src, input);

        if (!hitCooldown)
            printSyntaxHelper(src);

        if (!calledRemotely && economyEnabled && commandCost > 0)
        {
            sendCheckedMessage(src, "");

            if (commandCost == 1)
                src.sendMessage(Text.of("§eConfirming will cost you §6one §ecoin."));
            else
                src.sendMessage(Text.of("§eConfirming will cost you §6" + commandCost + "§e coins."));
        }

        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(final CommandSource src)
    {
        if (calledRemotely)
            sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " <target> [slot? 1-6]");
        else
        {
            final String confirmString;
            if (economyEnabled && commandCost != 0)
                confirmString = " {-c to confirm}";
            else
                confirmString = "";

            if (hatchParty)
            {
                if (src.hasPermission("empc.command.other.timedhatch"))
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " [target?]" + confirmString);
                else
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " " + confirmString);
            }
            else
            {
                if (src.hasPermission("empc.command.other.timedhatch"))
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " [target?] <slot, 1-6>" + confirmString);
                else
                    sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " <slot, 1-6>" + confirmString);
            }
        }
    }

    // Hatch us an egg! Also, show the right messages.
    private void hatchEgg(final CommandSource src, final Player target, final Pokemon pokemon)
    {
        pokemon.hatchEgg();
        printBasicError("Yo, did it update? If not, TODO.");

        if (target != null)
        {
            if (calledRemotely && sneakyMode)
            {
                sendCheckedMessage(src,"§aThe targeted egg has been silently hatched!");
                target.sendMessage(Text.of("§aThe targeted egg was hatched remotely!"));
            }
            else
            {
                sendCheckedMessage(src,"§aThe targeted egg has been hatched!");
                if (!calledRemotely)
                    target.sendMessage(Text.of("§aThe targeted egg was hatched by §2" + src.getName() + "§a!"));
            }
        }
        else
            sendCheckedMessage(src,"§aThe chosen egg has been hatched!");
    }

    // Hatch us a whole party of eggs! Possibly. Check before you wreck.
    private void hatchParty(final CommandSource src, final Player target, final PartyStorage storage)
    {
        // Create a Pokemon object that we can fill in with party slot data when we get it.
        Pokemon pokemon;
        for (int i = 1; i <= 6; i++)
        {
            pokemon = storage.get(i);
            if (pokemon != null)
                pokemon.hatchEgg();
        }
        printBasicError("Yo, did it update? If not, TODO.");

        if (target == null)
            sendCheckedMessage(src,"§aAll eggs in your party have been hatched!");
        else
        {
            if (calledRemotely && sneakyMode)
                sendCheckedMessage(src,"§aAll eggs in the target's party have been silently hatched!");
            else
                sendCheckedMessage(src,"§aAll eggs in the target's party have been hatched!");

            if (calledRemotely && !sneakyMode)
                target.sendMessage(Text.of("§aYour party's eggs were hatched remotely!"));
            else if (!calledRemotely)
                target.sendMessage(Text.of("§aYour party's eggs were hatched by §2" + src.getName() + "§a!"));
        }
    }
}
