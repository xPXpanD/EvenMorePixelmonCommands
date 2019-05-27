// Based on an idea by Dezire that seemed like it'd be fun to implement. (and it was a good excuse to get this updated)
package rs.expand.evenmorepixelmoncommands.commands;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomTM implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias;

    // Are we running from console or command blocks? We'll flag this true, and proceed accordingly.
    private boolean calledRemotely;

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        // Set up a variable for saving flag presence.
        boolean includeHMs = false;

        // Validate the data we get from the command's main config.
        // TODO: Override printCommandNodeError and move these single-parameter classes over there.
        final List<String> commandErrorList = new ArrayList<>();
        if (commandAlias == null)
            commandErrorList.add("commandAlias");

        if (!commandErrorList.isEmpty())
        {
            PrintingMethods.printCommandNodeError("RandomTM", commandErrorList);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else
        {
            final Optional<String> arg1Optional = args.getOne("target/flag");
            final Player target;

            if (calledRemotely)
            {
                // Do we have an argument in the first slot?
                if (arg1Optional.isPresent())
                {
                    final String arg1String = arg1Optional.get();

                    // Do we have a valid online player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();

                        // Do we have an argument in the second slot? A bit ugly, but it'll do.
                        final Optional<String> arg2Optional = args.getOne("flag");
                        if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-a"))
                            includeHMs = true;
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cNo target found.");
                    return CommandResult.empty();
                }
            }
            else
            {
                // See if we have a player.
                if (arg1Optional.isPresent())
                {
                    final String arg1String = arg1Optional.get();

                    // Is the provided first argument a valid player?
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();

                        // Do we have an argument in the second slot? A bit ugly, but it'll do.
                        final Optional<String> arg2Optional = args.getOne("flag");
                        if (arg2Optional.isPresent() && arg2Optional.get().equalsIgnoreCase("-a"))
                            includeHMs = true;
                    }
                    else if (arg1Optional.get().equalsIgnoreCase("-a"))
                    {
                        //noinspection ConstantConditions
                        target = (Player) src;
                        includeHMs = true;
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    //noinspection ConstantConditions
                    target = (Player) src;
                }
            }

            // Create a new random number between 0 and 173/184, then shift it up by 1 for an usable range.
            final int bound = includeHMs ? 184 : 173;
            final int machineNum = new Random().nextInt(bound) + 1;

            // Get a random TM or HM using Pixelmon's item types. Numbers above 174 are shifted down to create a HM range.
            final Optional<ItemType> item;
            if (machineNum > 174)
                item = Sponge.getRegistry().getType(ItemType.class, "pixelmon:hm" + (machineNum - 174));
            else
                item = Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm" + machineNum);

            // Did we get a valid TM? Should always happen, but yeah.
            if (item.isPresent())
            {
                final ItemStack stack = ItemStack.builder().itemType(item.get()).build();

                // Format the message based on what we're giving out.
                if (machineNum > 174)
                    sendCheckedMessage(src, "§aGiving out a fresh HM, number " + (machineNum - 174) + ".");
                else
                    sendCheckedMessage(src, "§aGiving out a fresh TM, number " + machineNum + ".");

                // Create a drop entity.
                Item drop = (Item) target.getLocation().getExtent().createEntity(EntityTypes.ITEM, target.getLocation().getPosition());

                // Define the actual drop item using our ItemStack.
                drop.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());

                // Drop it like it's hot.
                target.getWorld().spawnEntity(drop);

                // Inform the target.
                if (target != src)
                {
                    if (machineNum > 174)
                        target.sendMessage(Text.of("§dYou've received a random HM!"));
                    else
                        target.sendMessage(Text.of("§dYou've received a random TM!"));
                }
            }
            else
            {
                if (machineNum > 174)
                    src.sendMessage(Text.of("§4Error: §cRandom HM not found! Please report this. Item number: " + (machineNum - 174)));
                else
                    src.sendMessage(Text.of("§4Error: §cRandom TM not found! Please report this. Item number: " + machineNum));
            }
        }

        return CommandResult.success();
	}

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        sendCheckedMessage(src, "§5-----------------------------------------------------");
        sendCheckedMessage(src, input);

        if (calledRemotely)
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " <target> {-a to include HMs}");
        else
            sendCheckedMessage(src, "§4Usage: §c/" + commandAlias + " [target] {-a to include HMs}");

        sendCheckedMessage(src, "§5-----------------------------------------------------");
    }

    // Allows us to redirect printed messages away from command blocks, and into the console if need be.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printUnformattedMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }
}
