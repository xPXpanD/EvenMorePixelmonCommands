// Made for testing, but probably useful on a public server too.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import java.util.Optional;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;

// TODO: Add target support.
public class ResetCount implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("ResetCount", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            if (commandAlias == null)
            {
                printToLog(0, "Could not read node \"§4commandAlias§c\".");
                printToLog(0, "This command's config could not be parsed. Exiting.");
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please check the file."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                final Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false, printError = false;
                String fixedCount = "";
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
                    addFooter(src);

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
                        addFooter(src);

                        canContinue = false;
                    }
                }

                if (args.<String>getOne("count").isPresent() && canContinue)
                {
                    final String countString = args.<String>getOne("count").get();

                    switch (countString.toUpperCase())
                    {
                        case "UPGRADE": case "UPGRADES":
                            fixedCount = "Upgrade";
                            break;
                        case "FUSION": case "FUSIONS":
                            fixedCount = "Fusion";
                            break;
                        case "ALL": case "BOTH": case "EVERYTHING":
                            fixedCount = "All";
                            break;
                        default:
                            printError = true;
                    }
                }
                else if (canContinue) printError = true;

                if (printError)
                {
                    printToLog(1, "Could not find a valid count to reset. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cInvalid count provided. See below for valid ones."));
                    addFooter(src);

                    canContinue = false;
                }

                if (args.hasAny("c") && canContinue)
                    commandConfirmed = true;

                if (canContinue)
                {
                    final Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
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
                        else
                        {
                            if (commandConfirmed)
                            {
                                printToLog(2, "Command was confirmed, proceeding to execution.");

                                final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

                                switch (fixedCount)
                                {
                                    case "Fusion":
                                    {
                                        final int fuseCount = pokemon.getEntityData().getInteger("fuseCount");
                                        final boolean isDitto = nbt.getString("Name").equals("Ditto");

                                        printToLog(1,
                                                "Resetting Fusion count on target Pokémon. Old count: §3" + fuseCount);

                                        src.sendMessage(Text.of("§aThis Pokémon's Fusion count has been reset!"));
                                        if (!isDitto)
                                            src.sendMessage(Text.of("§eThis isn't a Ditto -- you may want to wipe Upgrade instead."));

                                        pokemon.getEntityData().setInteger("fuseCount", 0);
                                        break;
                                    }
                                    case "Upgrade":
                                    {
                                        final int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                                        final boolean isDitto = nbt.getString("Name").equals("Ditto");

                                        printToLog(1,
                                                "Resetting Upgrade count on target Pokémon. Old count: §3" + upgradeCount);

                                        src.sendMessage(Text.of("§aThis Pokémon's Upgrade count has been reset!"));
                                        if (isDitto)
                                            src.sendMessage(Text.of("§eThis is a Ditto -- you may want to wipe Fusion instead."));

                                        pokemon.getEntityData().setInteger("upgradeCount", 0);
                                        break;
                                    }
                                    default:
                                    {
                                        final int upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                                        final int fuseCount = pokemon.getEntityData().getInteger("fuseCount");

                                        printToLog(1, "Resetting all counts. Old: §3" + upgradeCount +
                                                "§b upgrades, §3" + fuseCount + "§b fusions.");

                                        src.sendMessage(Text.of("§aThis Pokémon's counts have both been reset!"));

                                        pokemon.getEntityData().setInteger("fuseCount", 0);
                                        pokemon.getEntityData().setInteger("upgradeCount", 0);
                                    }
                                }

                                printToLog(1, "Successfully reset target count(s)!");
                            }
                            else
                            {
                                printToLog(1, "No confirmation provided, printing warning and aborting.");

                                src.sendMessage(Text.of("§5-----------------------------------------------------"));

                                if (fixedCount.equals("All"))
                                    src.sendMessage(Text.of("§6Warning: §eYou're about to reset this Pokémon's edit counts!"));
                                else
                                    src.sendMessage(Text.of("§6Warning: §eYou're about to reset this Pokémon's §6" + fixedCount + "§e count!"));

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

    private void addFooter(final CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> <count> {-c to confirm}"));
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§6Valid counts: §eUpgrade, Fusion, All (wipes both!)"));
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§5Warning: §dThe -c flag immediately wipes the chosen count!"));
        src.sendMessage(Text.of("§d(these counts are a Pokémon's upgrade/fusion totals)"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
