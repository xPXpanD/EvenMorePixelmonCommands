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
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!ForceHatchConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = ForceHatchConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74ForceHatch // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74ForceHatch // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ForceHatchConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckEgg // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74ForceHatch // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pu reload\\u00A7c.");
            }
            else
            {
                Player player = (Player) src;
                Optional<Player> target = player.getPlayer();
                String targetString;
                boolean targetAcquired = false, canContinue = true;
                int slot = 0;

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
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));
                            }
                            else if (targetString.matches("\\d+"))
                            {
                                printToLog(2, "First argument was numeric, but not valid. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds! Valid values are 1-6."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));
                            }
                            else
                            {
                                printToLog(2, "Target does not exist, or is offline. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));
                            }

                            canContinue = false;
                        }
                    }
                    else if (!target.isPresent())
                    {
                        printToLog(2, "Provided target does not seem to be present. Abort.");

                        src.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                        src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));

                        canContinue = false;
                    }
                    else
                    {
                        String slotString = args.<String>getOne("slot").get();

                        if (slotString.matches("\\d+"))
                        {
                            slot = Integer.parseInt(args.<String>getOne("slot").get());

                            if (!(slot < 7 && slot > 0))
                            {
                                printToLog(2, "Second argument was numeric, but not a valid slot. Abort.");

                                src.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds. Valid values are 1-6."));
                                src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));

                                canContinue = false;
                            }
                            else
                            {
                                printToLog(3, "Provided target exists and is online! Target logic is go.");
                                targetAcquired = true;
                            }
                        }
                        else
                        {
                            printToLog(2, "Slot value was not an integer. Abort.");

                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                            src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));

                            canContinue = false;
                        }
                    }
                }
                else
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide at least a slot."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " (optional target) <slot, 1-6>"));

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

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
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
}