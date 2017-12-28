package rs.expand.pixelupgrade.commands;

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

import rs.expand.pixelupgrade.configs.ForceHatchConfig;
import rs.expand.pixelupgrade.PixelUpgrade;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;

public class ForceHatch implements CommandExecutor
{
    // Grab the command's alias.
    private static String alias = null;
    private void getCommandAlias()
    {
        if (!ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
            PixelUpgrade.log.info("§4ForceHatch // critical: §cConfig variable \"commandAlias\" could not be found!");
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            // Set up the command's preferred alias.
            getCommandAlias();

            if (alias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4ForceHatch // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else
            {
                Player player = (Player) src;
                Optional<Player> target = player.getPlayer();
                String targetString;
                boolean targetAcquired = false, canContinue = false;
                int slot = 0;

                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

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
            PixelUpgrade.log.info("§cThis command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printCorrectPerm(Player player)
    {
        player.sendMessage(Text.of("§4Usage: §c" + alias + " [optional target] <slot, 1-6>"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4ForceHatch // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§3ForceHatch // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2ForceHatch // debug: §a" + inputString);
        }
    }
}