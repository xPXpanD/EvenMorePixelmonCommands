// Originally made to work around a Pixelmon bug, but the bug got squashed as this released. Oh, well.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;

public class FixLevel implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("FixLevel", debugNum, inputString); }

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
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                boolean canContinue = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
                    printSyntaxHelper(src);
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                        canContinue = true;
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printSyntaxHelper(src);
                    }
                }

                if (canContinue)
                {
                    printToLog(2, "No errors encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));
                    Player player = (Player) src;

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to fix level on an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            int pokemonLevel = pokemon.getLvl().getLevel(), configLevel = PixelmonConfig.maxLevel;

                            if (pokemonLevel != configLevel)
                            {
                                printToLog(1, "Config cap and target Pokémon's level did not match. Exit.");
                                src.sendMessage(Text.of("§4Error: §cYour Pokémon is not at level §4" + configLevel + "§c, yet."));
                            }
                            else
                            {
                                storageCompleted.sendUpdatedList();
                                printToLog(1, "Succesfully fixed the level of a Pokémon!");

                                if (nbt.getString("Nickname").equals(""))
                                    src.sendMessage(Text.of("§6" + nbt.getString("Name") + "§e had its level fixed!"));
                                else
                                    src.sendMessage(Text.of("§eYour §6" + nbt.getString("Nickname") + "§e had its level fixed!"));
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

    // Might look a bit odd, but done this way so we only have to edit one message if this ever changes.
	private void printSyntaxHelper(CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }
}
