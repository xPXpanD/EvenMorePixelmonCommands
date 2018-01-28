// Originally created for testing things for the Pixelmon Wiki, but might be useful to other people?
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import java.util.Objects;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;
import rs.expand.pixelupgrade.utilities.EnumPokemonList;

/*                                                      *\
    TODO: Maybe add target and console usage support?
    Spawning stuff near online players could be cool.
\*                                                      */

public class SpawnDex implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("SpawnDex", debugNum, inputString); }

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

                boolean canContinue = true, makeShiny = false;
                String numberString;
                int pokedexNumber = 0;

                if (!args.<String>getOne("number").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please enter a Pokédex number."));
                    addFooter(src);

                    canContinue = false;
                }
                else
                {
                    numberString = args.<String>getOne("number").get();

                    if (numberString.matches("\\d+"))
                    {
                        printToLog(2, "Provided argument is an integer, let's check if it's in range.");
                        pokedexNumber = Integer.parseInt(args.<String>getOne("number").get());
                        
                        if (pokedexNumber > 807 || pokedexNumber < 1)
                        {
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cInvalid Pokédex number! Valid range is 1-807."));
                            addFooter(src);

                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(1, "Invalid value provided for Pokédex number. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInput was not a number. Valid range is 1-807."));
                        addFooter(src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("s"))
                    makeShiny = true;

                if (canContinue)
                {
                    printToLog(2, "No errors encountered, input should be valid. Continuing!");

                    /*                                                                    *\
                        TODO: Get the player's facing angle, and spawn in front of them.
                        Currently it spawns one nearby, one block away, on a fixed axis.
                    \*                                                                    */

                    try
                    {
                        printToLog(2, "Attempting to grab a name for the given Pokédex number, §2" +
                                pokedexNumber + "§a.");

                        // Try to grab a Pokémon name for the given Pokédex number.
                        String pokemonName = Objects.requireNonNull(EnumPokemonList.getPokemonFromID(pokedexNumber)).name();

                        // Set up variables for spawning. Nabbed some code here from Pixelmon.
                        EntityPlayerMP playerEntity = (EntityPlayerMP) src;
                        WorldServer world = playerEntity.getServerWorld();
                        BlockPos playerPos = playerEntity.getPosition();
                        EntityPixelmon pokemonToSpawn = PokemonSpec.from(pokemonName).create(world);

                        // Configure the Pokémon-to-spawn.
                        pokemonToSpawn.setPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                        pokemonToSpawn.setSpawnLocation(pokemonToSpawn.getDefaultSpawnLocation());

                        if (!makeShiny)
                        {
                            printToLog(1, "Now spawning a §2" + pokemonName + "§a, and exiting.");
                            src.sendMessage(Text.of("§aSpawning a fresh §2" + pokemonName + "§a nearby!"));
                        }
                        else
                        {
                            pokemonToSpawn.setIsShiny(true);
                            printToLog(1, "Now spawning a shiny §2" + pokemonName + "§a, and exiting.");
                            src.sendMessage(Text.of("§aSpawning a fresh (and shiny) §2" + pokemonName + "§a nearby!"));
                        }

                        // Actually spawn it.
                        world.spawnEntity(pokemonToSpawn);
                    }
                    catch (NullPointerException F)
                    {
                        src.sendMessage(Text.of("§4Error: §cSpawning failed. This Pokémon is likely not in yet."));
                        printToLog(1, "The requested Pokémon does not yet exist. Exit.");
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void addFooter(CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokédex number> {-s to make shiny}"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
