package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.ForceHatchConfig;

public class ForceHatch implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            Integer debugVerbosityMode;

            debugVerbosityMode = checkConfigInt(false);

            if (debugVerbosityMode == null)
            {
                printToLog(0, "Error parsing config! Make sure everything is valid, or regenerate it.");
                src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid config for command! Please report this to staff."));
            }
            else
            {
                Player player = (Player) src;
                Optional<Player> target = player.getPlayer();
                String targetString;
                Boolean targetAcquired = false, canContinue = true;
                Integer slot = 0;

                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                if (args.<String>getOne("target or slot").isPresent())
                {
                    targetString = args.<String>getOne("target or slot").get();
                    target = Sponge.getServer().getPlayer(targetString);
                    if (!args.<String>getOne("slot").isPresent())
                    {
                        if (targetString.matches("^[1-6]"))
                        {
                            printToLog(3, "Found a slot in argument 1. Skipping everything else.");
                            slot = Integer.parseInt(targetString);
                        }
                        else
                        {
                            if (target.isPresent())
                            {
                                printToLog(2, "Found a target, but no slot was provided. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cFound a target, but no slot was provided."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));
                            }
                            else if (targetString.matches("^[0-9].*"))
                            {
                                printToLog(2, "First argument was numeric, but not valid. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds! Valid values are 1-6."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));
                            }
                            else
                            {
                                printToLog(2, "Target does not exist, or is offline. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));
                            }

                            canContinue = false;
                        }
                    }
                    else if (!target.isPresent())
                    {
                        printToLog(2, "Provided target does not seem to be present. Abort.");

                        src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                        src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                        canContinue = false;
                    }
                    else
                    {
                        try
                        {
                            slot = Integer.parseInt(args.<String>getOne("slot").get());

                            if (!(slot < 7 && slot > 0))
                            {
                                printToLog(2, "Second argument was numeric, but not a valid slot. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds. Valid values are 1-6."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                                canContinue = false;
                            }
                            else
                            {
                                printToLog(3, "Provided target exists and is online! Target logic is go.");
                                targetAcquired = true;
                            }
                        }
                        catch (Exception F)
                        {
                            printToLog(2, "Slot value was not an integer, fell through try check. Abort.");

                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                            src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                            canContinue = false;
                        }
                    }
                }
                else
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide at least a slot."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcehatch (optional target) <slot, 1-6>"));

                    canContinue = false;
                }

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
                    Optional<PlayerStorage> storage;
                    if (targetAcquired)
                        storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target.get()));
                    else
                        storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(2, "No Pok\u00E9mon was found in the provided slot. Abort, abort!");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThere's nothing in that slot!"));
                        }
                        else if (!nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Tried to hatch an actual Pok\u00E9mon. Since that's too brutal, let's abort.");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThat's not an egg. Don't hatch actual Pok\u00E9mon, kids!"));
                        }
                        else
                        {
                            printToLog(1, "Passed all checks, hatching us an egg!");

                            nbt.setBoolean("isEgg", false);
                            storageCompleted.changePokemonAndAssignID(slot - 1, nbt);
                            src.sendMessage(Text.of("\u00A7eCongratulations, it's a healthy baby \u00A76" + nbt.getString("Name") + "\u00A7e!"));
                        }
                    }
                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printToLog(Integer debugNum, String inputString)
    {
        Integer debugVerbosityMode = checkConfigInt(true);

        if (debugVerbosityMode == null)
            debugVerbosityMode = 4;

        if (debugNum <= debugVerbosityMode)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74ForceHatch // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76ForceHatch // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73ForceHatch // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72ForceHatch // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt(Boolean noMessageMode)
    {
        if (!ForceHatchConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
            return ForceHatchConfig.getInstance().getConfig().getNode("debugVerbosityMode").getInt();
        else if (noMessageMode)
            return null;
        else
        {
            PixelUpgrade.log.info("\u00A74ForceHatch // critical: \u00A7cCould not parse config variable \"debugVerbosityMode\"!");
            return null;
        }
    }
}