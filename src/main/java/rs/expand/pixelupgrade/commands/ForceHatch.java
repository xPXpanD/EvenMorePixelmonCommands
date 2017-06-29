package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import org.spongepowered.api.Sponge;
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
                PixelUpgrade.log.info("§4ForceHatch // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4ForceHatch // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
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
            PixelUpgrade.log.info("§4ForceHatch // critical: §cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
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
                boolean targetAcquired = false, canContinue = true;
                int slot = 0;

                printToLog(2, "Called by player §3" + src.getName() + "§b. Starting!");

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

                                src.sendMessage(Text.of("§4Error: §cFound a target, but no slot was provided."));
                                src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));
                            }
                            else if (targetString.matches("\\d+"))
                            {
                                printToLog(2, "First argument was numeric, but not valid. Abort.");

                                src.sendMessage(Text.of("§4Error: §cSlot value out of bounds! Valid values are 1-6."));
                                src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));
                            }
                            else
                            {
                                printToLog(2, "Target does not exist, or is offline. Abort.");

                                src.sendMessage(Text.of("§4Error: §cYour target does not exist, or is offline."));
                                src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));
                            }

                            canContinue = false;
                        }
                    }
                    else if (!target.isPresent())
                    {
                        printToLog(2, "Provided target does not seem to be present. Abort.");

                        src.sendMessage(Text.of("§4Error: §cYour target does not exist, or is offline."));
                        src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));

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

                                src.sendMessage(Text.of("§4Error: §cSlot value out of bounds. Valid values are 1-6."));
                                src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));

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

                            src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                            src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));

                            canContinue = false;
                        }
                    }
                }
                else
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide at least a slot."));
                    src.sendMessage(Text.of("§4Usage: §c" + alias + " (optional target) <slot, 1-6>"));

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
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(2, "No Pokémon was found in the provided slot. Abort, abort!");
                            src.sendMessage(Text.of("§4Error: §cThere's nothing in that slot!"));
                        }
                        else if (!nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Tried to hatch an actual Pokémon. Since that's too brutal, let's abort.");
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
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4ForceHatch // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§6ForceHatch // important: §e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("§3ForceHatch // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2ForceHatch // debug: §a" + inputString);
        }
    }
}