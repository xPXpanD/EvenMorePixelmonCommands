package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;

public class ForceHatch implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.doPrint("ForceHatch", false, debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
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
                Player player = (Player) src;
                Optional<Player> target = player.getPlayer();
                String targetString;
                boolean targetAcquired = false, canContinue = false;
                int slot = 0;

                printToLog(1, "Called by player §6" + src.getName() + "§e. Starting!");

                if (args.<String>getOne("target or slot").isPresent())
                {
                    targetString = args.<String>getOne("target or slot").get();
                    target = Sponge.getServer().getPlayer(targetString);
                    if (!args.<String>getOne("slot").isPresent())
                    {
                        if (targetString.matches("^[1-6]"))
                        {
                            printToLog(2, "Found a slot in argument 1. Skipping everything else.");
                            slot = Integer.parseInt(targetString);
                            canContinue = true;
                        }
                        else
                        {
                            if (target.isPresent())
                            {
                                printToLog(1, "Found a target, but no slot was provided. Exit.");
                                src.sendMessage(Text.of("§4Error: §cFound a target, but no slot was provided."));
                            }
                            else if (targetString.matches("\\d+"))
                            {
                                printToLog(1, "First argument was numeric, but not valid. Exit.");
                                src.sendMessage(Text.of("§4Error: §cSlot value out of bounds! Valid values are 1-6."));
                            }
                            else
                            {
                                printToLog(1, "Target does not exist, or is offline. Exit.");
                                src.sendMessage(Text.of("§4Error: §cYour target does not exist, or is offline."));
                            }

                            printCorrectPerm(player);
                        }
                    }
                    else if (!target.isPresent())
                    {
                        printToLog(1, "Provided target does not seem to be present. Exit.");

                        src.sendMessage(Text.of("§4Error: §cYour target does not exist, or is offline."));
                        printCorrectPerm(player);
                    }
                    else
                    {
                        String slotString = args.<String>getOne("slot").get();

                        if (slotString.matches("\\d+"))
                        {
                            slot = Integer.parseInt(args.<String>getOne("slot").get());

                            if (!(slot < 7 && slot > 0))
                            {
                                printToLog(1, "Second argument was numeric, but not a valid slot. Exit.");

                                src.sendMessage(Text.of("§4Error: §cSlot value out of bounds. Valid values are 1-6."));
                                printCorrectPerm(player);
                            }
                            else
                            {
                                printToLog(2, "Provided target exists and is online! Target logic is go.");
                                targetAcquired = true;
                                canContinue = true;
                            }
                        }
                        else
                        {
                            printToLog(1, "Slot value was not an integer. Exit.");

                            src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                            printCorrectPerm(player);
                        }
                    }
                }
                else
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide at least a slot."));
                    printCorrectPerm(player);
                }

                if (canContinue)
                {
                    printToLog(2, "No error encountered, input should be valid. Continuing!");

                    Optional<PlayerStorage> storage;
                    if (targetAcquired)
                        storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target.get()));
                    else
                        storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No Pokémon was found in the provided slot. Abort, abort!");
                            src.sendMessage(Text.of("§4Error: §cThere's nothing in that slot!"));
                        }
                        else if (!nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to hatch an actual Pokémon. Since that's too brutal, let's exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's not an egg. Don't hatch actual Pokémon, kids!"));
                        }
                        else
                        {
                            printToLog(1, "Passed all checks, hatching us an egg!");

                            nbt.setBoolean("isEgg", false);
                            storageCompleted.changePokemonAndAssignID(slot - 1, nbt);
                            src.sendMessage(Text.of("§eCongratulations, it's a healthy baby §6" + nbt.getString("Name") + "§e!"));
                        }
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printCorrectPerm(Player player)
    {
        player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " [optional target] <slot, 1-6>"));
    }
}