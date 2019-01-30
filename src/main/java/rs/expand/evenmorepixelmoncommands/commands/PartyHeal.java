// The party version of /timedheal, broken off due to the insane complexity of cramming that all into a single class.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import java.math.BigDecimal;
import java.util.*;

// Local imports.
import static rs.expand.evenmorepixelmoncommands.EMPC.economyEnabled;
import static rs.expand.evenmorepixelmoncommands.EMPC.economyService;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

public class PartyHeal implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer cooldownInSeconds, altCooldownInSeconds, commandCost;
    public static Boolean sneakyMode;

    // Set up some more variables for internal use.
    private String sourceName = this.getClass().getSimpleName();
    private boolean calledRemotely;
    private HashMap<UUID, Long> cooldownMap = new HashMap<>();

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        final List<String> commandErrorList = new ArrayList<>();
        if (commandAlias == null)
            commandErrorList.add("commandAlias");
        if (cooldownInSeconds == null)
            commandErrorList.add("cooldownInSeconds");
        if (altCooldownInSeconds == null)
            commandErrorList.add("altCooldownInSeconds");
        if (sneakyMode == null)
            commandErrorList.add("sneakyMode");
        if (commandCost == null)
            commandErrorList.add("commandCost");

        if (!commandErrorList.isEmpty())
        {
            PrintingMethods.printCommandNodeError(sourceName, commandErrorList);
            sendCheckedMessage(src,"§4Error: §cThis command's config is invalid! Please report to staff.");
        }
        else
        {
            final long currentTime = System.currentTimeMillis() / 1000; // Grab seconds.
            boolean commandConfirmed = false;
            final Optional<String> arg1Optional = args.getOne("target/confirmation");
            final Optional<String> arg2Optional = args.getOne("confirmation");
            Player target = null, player = null;
            UUID playerUUID = null;

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
            }
            else
            {
                //noinspection ConstantConditions - safe, we've already verified we're not console/a command block...
                player = (Player) src;
                playerUUID = player.getUniqueId(); // why is the "d" in "Id" lowercase :(

                if (!src.hasPermission("empc.command.bypass.partyheal") && cooldownMap.containsKey(playerUUID))
                {
                    final boolean hasAltPerm = src.hasPermission("empc.command.altcooldown.partyheal");
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

                // Do we have an argument in the first argument slot?
                // This can be a player name, a confirmation flag or nothing.
                if (arg1Optional.isPresent())
                {
                    final String argString = arg1Optional.get();

                    // Do we have a confirmation flag?
                    if (argString.equalsIgnoreCase("-c") && commandCost != 0)
                        commandConfirmed = true;
                    else if (src.hasPermission("empc.command.other.partyheal"))
                    {
                        if (Sponge.getServer().getPlayer(argString).isPresent()) // Do we have a valid online player?
                        {
                            // Check if the player is targeting themselves. (if they are, just let target stay null)
                            if (!argString.equalsIgnoreCase(player.getName()))
                                target = Sponge.getServer().getPlayer(argString).get();
                        }
                        else
                        {
                            printLocalError(src, "§4Error: §cInvalid target on first argument. See below.", false);
                            return CommandResult.empty();
                        }
                    }
                }

                // Do we have an argument in the second argument slot, and is it a flag?
                if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-c"))
                    commandConfirmed = true;
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

                                // Heal!
                                healParty(src, target, party);

                                if (target == null)
                                {
                                    printSourcedMessage(sourceName, "Healing calling player §3" + player.getName() +
                                            "§b's party and taking §3" + costToConfirm + "§b coins.");
                                }
                                else
                                {
                                    printSourcedMessage(sourceName, "Player §3" + player.getName() + " §bis healing §3" +
                                            target.getName() + "§b's party. Taking §3" + costToConfirm + "§b coins.");
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

                        // Is cost to confirm exactly one coin?
                        if (target == null)
                        {
                            if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                sendCheckedMessage(src,"§6Warning: §eHealing your team costs §6one §ecoin.");
                            else
                            {
                                sendCheckedMessage(src,"§6Warning: §eHealing your team costs §6" + costToConfirm +
                                        "§e coins.");
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

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    }
                }
                else
                {
                    // Heal!
                    healParty(src, target, party);

                    if (!calledRemotely)
                    {
                        // Rely on Pixelmon's messages here.
                        if (target != null)
                        {
                            //noinspection ConstantConditions - !calledRemotely guarantees this is safe
                            printSourcedMessage(sourceName, "Called by §3" + player.getName() +
                                    "§b, healing §3" + target.getName() + "§b's party.");
                        }

                        cooldownMap.put(playerUUID, currentTime);
                    }
                    else
                    {
                        //noinspection ConstantConditions - safe, just too complicated
                        printSourcedMessage(sourceName, "Called from remote source, healing §3" +
                                target.getName() + "§b's party if available.");
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
            src.sendMessage(Text.EMPTY);

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
            sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " <target>");
        else
        {
            final String confirmString = economyEnabled && commandCost != 0 ? " {-c to confirm}" : "";

            if (src.hasPermission("empc.command.other.partyheal"))
                sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " [target?]" + confirmString);
            else
                sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " " + confirmString);
        }
    }

    // Heal all of a party's Pokémon! Also, show the right messages.
    private void healParty(final CommandSource src, final Player target, final PartyStorage party)
    {
        party.heal();

        if (target != null)
        {
            if (calledRemotely && sneakyMode)
                sendCheckedMessage(src,"§aThe targeted party has been silently healed!");
            else
            {
                sendCheckedMessage(src,"§aThe targeted party has been healed!");
                if (!calledRemotely)
                    target.sendMessage(Text.of("§aYour Pokémon were healed by §2" + src.getName() + "§a!"));
                else
                    target.sendMessage(Text.of("§aYour Pokémon were healed remotely!"));
            }
        }
        else
            sendCheckedMessage(src,"§aYour party has been healed!");
    }
}
