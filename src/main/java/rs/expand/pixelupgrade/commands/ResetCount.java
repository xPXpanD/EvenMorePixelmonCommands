package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import org.spongepowered.api.text.Text;
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.ResetCountConfig;

import java.util.Optional;

public class ResetCount implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!ResetCountConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = ResetCountConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74ResetCount // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74ResetCount // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!ResetCountConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ResetCountConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74ResetCount // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
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
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7cCheck your config. If need be, wipe and \u00A74/pureload\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false, printError = false;
                String fixedCount = "";
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6> <count> {-c to confirm}"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A76Valid counts: \u00A7eUpgrade, Fusion"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A75Warning: \u00A7dThe -c flag immediately wipes the chosen count!"));
                    src.sendMessage(Text.of("\u00A7d(these counts are a Pok\u00E9mon's upgrade/fusion totals)"));
                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot provided. Aborting.");

                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                        src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6> <count> {-c to confirm}"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("\u00A76Valid counts: \u00A7eUpgrade/Fusion/All"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("\u00A75Warning: \u00A7dThe -c flag immediately wipes the chosen count!"));
                        src.sendMessage(Text.of("\u00A7d(these counts are a Pok\u00E9mon's upgrade/fusion totals)"));
                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                        canContinue = false;
                    }
                }

                if (args.<String>getOne("count").isPresent() && canContinue)
                {
                    String countString = args.<String>getOne("count").get();

                    switch (countString.toUpperCase())
                    {
                        case "UPGRADE": case "UPGRADES":
                            fixedCount = "Upgrade";
                            break;
                        case "FUSION": case "FUSIONS":
                            fixedCount = "Fusion";
                            break;
                        default:
                            printError = true;
                    }
                }
                else if (canContinue) printError = true;

                if (printError)
                {
                    printToLog(2, "Could not find a valid count to reset. Aborting.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid count provided. See below for valid ones."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6> <count> {-c to confirm}"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A76Valid counts: \u00A7eUpgrade/Fusion/All"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A75Warning: \u00A7dThe -c flag immediately wipes the chosen count!"));
                    src.sendMessage(Text.of("\u00A7d(these counts are a Pok\u00E9mon's upgrade/fusion totals)"));
                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }

                if (args.hasAny("c") && canContinue)
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + player.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
                        }
                        else
                        {
                            if (commandConfirmed)
                            {
                                EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                                Integer upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                                Integer fuseCount = pokemon.getEntityData().getInteger("fuseCount");
                                boolean isDitto = nbt.getString("Name").equals("Ditto");

                                printToLog(3, "Command was confirmed, proceeding to execution.");


                                if (fixedCount.matches("Fusion"))
                                {
                                    printToLog(1, "Resetting Fusion count on target Pokémon. Old count: " + fuseCount);
                                    src.sendMessage(Text.of("\u00A7aThis Pok\u00E9mon's Fusion count has been reset!"));
                                    if (!isDitto)
                                        src.sendMessage(Text.of("\u00A7eThis isn't a Ditto -- you may want to wipe Upgrade instead."));
                                    pokemon.getEntityData().setInteger("fuseCount", 0);
                                }
                                else if (fixedCount.matches("Upgrade"))
                                {
                                    printToLog(1, "Resetting Upgrade count on target Pokémon. Old count: " + upgradeCount);
                                    src.sendMessage(Text.of("\u00A7aThis Pok\u00E9mon's Upgrade count has been reset!"));
                                    if (isDitto)
                                        src.sendMessage(Text.of("\u00A7eThis is a Ditto -- you may want to wipe Fusion instead."));
                                    pokemon.getEntityData().setInteger("upgradeCount", 0);
                                }

                                printToLog(2, "Target counts have been reset!");
                            }
                            else
                            {
                                printToLog(2, "No confirmation provided, printing warning and aborting.");

                                src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                if (fixedCount.matches("all"))
                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eYou're about to reset all of this Pok\u00E9mon's improvement counts!"));
                                else
                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eYou are about to reset this Pok\u00E9mon's \u00A76" + fixedCount + "\u00A7e count!"));
                                src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + slot + " -c"));
                                src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                            }
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
                PixelUpgrade.log.info("\u00A74ResetCount // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76ResetCount // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73ResetCount // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72ResetCount // debug: \u00A7a" + inputString);
        }
    }
}
