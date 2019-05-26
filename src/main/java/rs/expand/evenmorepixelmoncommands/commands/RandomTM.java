// PixelUpgrade's very first command. Originally /upgrade stats, then /getstats, and then finally this as part of EMPC.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.

import java.util.*;

import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
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

// Local imports.
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.*;

public class RandomTM implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;

    // TODO: Permission check!
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console? Let's tell our code that. If "src" is not a Player, this becomes true.
        // Set up a variable for internal use.
        final boolean calledRemotely = !(src instanceof Player);

        // Validate the data we get from the command's main config.
        // TODO: Override printCommandNodeError and move these single-parameter classes over there.
        final List<String> commandErrorList = new ArrayList<>();
        if (commandAlias == null)
            commandErrorList.add("commandAlias");

        if (!commandErrorList.isEmpty())
        {
            printCommandNodeError("RandomTM", commandErrorList);
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
        }
        else
        {
            final Optional<String> arg1Optional = args.getOne("target");
            final Player target;

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
                        src.sendMessage(Text.of("§4Error: §cInvalid target. See below."));
                        return CommandResult.empty();
                    }
                }
                else
                {
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. See below."));
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
                        target = Sponge.getServer().getPlayer(arg1String).get();
                    else
                    {
                        if (src instanceof CommandBlock)
                            src.sendMessage(Text.of("§4Error: §cNo target found."));
                        else
                        {
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cInvalid target. See below."));
                            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target]"));
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        }

                        return CommandResult.empty();
                    }
                }
                else
                    target = (Player) src;
            }

            // TODO: HM support. There's 10 of those, so maybe +10 the random number if the -a flag is set.

            // Create a new random number between 0 and 173, then shift it up by 1 for the TM range of 1-174.
            final int machineNum = new Random().nextInt(173) + 1;

            // Get a random TM using Pixelmon's item types.
            final Optional<ItemType> item = Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm" + machineNum);

            // Did we get a valid TM? Should always happen, but yeah.
            if (item.isPresent())
            {
                final ItemStack stack = ItemStack.builder().itemType(item.get()).build();
                final Inventory inventory = getHotbarFirst(target.getInventory());

                // Can we insert our stack into the target's inventory? Niiiiice.
                if (inventory.offer(stack).equals(InventoryTransactionResult.successNoTransactions()))
                    sendCheckedMessage(src, "§aGave out a fresh TM, number " + machineNum + ".");
                else
                {
                    sendCheckedMessage(src, "§aDropping a fresh TM, number " + machineNum + ".");

                    // Don't send this to command blocks.
                    if (target.gameMode().get().equals(GameModes.CREATIVE) && !(src instanceof CommandBlock))
                        sendCheckedMessage(src, "§5Please note: §dThe item may disappear due to Creative mode.");

                    // Create a drop entity.
                    Item drop = (Item) target.getLocation().getExtent().createEntity(EntityTypes.ITEM, target.getLocation().getPosition());

                    // Define the actual drop item using our ItemStack.
                    drop.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());

                    // Drop it like it's hot.
                    target.getWorld().spawnEntity(drop);
                }
            }
            else
                src.sendMessage(Text.of("§4Error: §cRandom TM not found! Please report this. Item number: " + machineNum));
        }

        return CommandResult.success();
	}

	// Based on a snippet by Faithcaio from https://github.com/SpongePowered/SpongeCommon/issues/1840. Confusing stuff.
    // Sends items to the hotbar first, main inventory second. Prevents stuff suddenly appearing in armor/hand slots.
	private Inventory getHotbarFirst(Inventory inventory)
    {
        return inventory
                .query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                .union(inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));
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
