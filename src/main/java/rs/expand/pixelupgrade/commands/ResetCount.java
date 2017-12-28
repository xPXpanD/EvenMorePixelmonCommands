package rs.expand.pixelupgrade.commands;

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

import rs.expand.pixelupgrade.configs.ResetCountConfig;
import rs.expand.pixelupgrade.PixelUpgrade;

import java.util.Optional;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;

public class ResetCount implements CommandExecutor
{
    private static String alias = null;
    private void getCommandAlias()
    {
        if (!ResetCountConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ResetCountConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
            PixelUpgrade.log.info("§4ResetCount // critical: §cConfig variable \"commandAlias\" could not be found!");
    }
    
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            // Set up the command's debug verbosity mode and preferred alias.
            getCommandAlias();

            if (alias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4ResetEVs // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false, printError = false;
                String fixedCount = "";
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
                    addFooter(player);

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

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
                        addFooter(player);

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
                    printToLog(1, "Could not find a valid count to reset. Exit.");

                    player.sendMessage(Text.of("§5-----------------------------------------------------"));
                    player.sendMessage(Text.of("§4Error: §cInvalid count provided. See below for valid ones."));
                    addFooter(player);

                    canContinue = false;
                }

                if (args.hasAny("c") && canContinue)
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(2, "No error encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + player.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else
                        {
                            if (commandConfirmed)
                            {
                                EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                                Integer upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                                Integer fuseCount = pokemon.getEntityData().getInteger("fuseCount");
                                boolean isDitto = nbt.getString("Name").equals("Ditto");

                                printToLog(2, "Command was confirmed, proceeding to execution.");


                                if (fixedCount.matches("Fusion"))
                                {
                                    printToLog(1, "Resetting Fusion count on target Pokémon. Old count: " + fuseCount);
                                    src.sendMessage(Text.of("§aThis Pokémon's Fusion count has been reset!"));
                                    if (!isDitto)
                                        src.sendMessage(Text.of("§eThis isn't a Ditto -- you may want to wipe Upgrade instead."));
                                    pokemon.getEntityData().setInteger("fuseCount", 0);
                                }
                                else if (fixedCount.matches("Upgrade"))
                                {
                                    printToLog(1, "Resetting Upgrade count on target Pokémon. Old count: " + upgradeCount);
                                    src.sendMessage(Text.of("§aThis Pokémon's Upgrade count has been reset!"));
                                    if (isDitto)
                                        src.sendMessage(Text.of("§eThis is a Ditto -- you may want to wipe Fusion instead."));
                                    pokemon.getEntityData().setInteger("upgradeCount", 0);
                                }

                                printToLog(1, "Target counts have been reset!");
                            }
                            else
                            {
                                printToLog(1, "No confirmation provided, printing warning and aborting.");

                                src.sendMessage(Text.of("§5-----------------------------------------------------"));
                                if (fixedCount.matches("all"))
                                    src.sendMessage(Text.of("§6Warning: §eYou're about to reset all of this Pokémon's improvement counts!"));
                                else
                                    src.sendMessage(Text.of("§6Warning: §eYou are about to reset this Pokémon's §6" + fixedCount + "§e count!"));
                                src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + slot + " -c"));
                                src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            }
                        }
                    }
                }
            }
        }
        else
            PixelUpgrade.log.info("§cThis command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void addFooter(Player player)
    {
        player.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6> <count> {-c to confirm}"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§6Valid counts: §eUpgrade§6, §eFusion"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§5Warning: §dThe -c flag immediately wipes the chosen count!"));
        player.sendMessage(Text.of("§d(these counts are a Pokémon's upgrade/fusion totals)"));
        player.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4ResetCount // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§3ResetCount // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2ResetCount // debug: §a" + inputString);
        }
    }
}
