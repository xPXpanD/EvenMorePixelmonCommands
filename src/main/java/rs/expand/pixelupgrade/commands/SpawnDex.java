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

// TODO: Add target and console support.
// TODO: Add support for names.
// TODO: Add more flags, like scale/special texture/IVs. Needs testing.
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

                boolean canContinue = true, makeShiny = false, makeOutlined = false, makePureBlack = false;
                String numberString;
                int pokedexNumber = 0;

                if (!args.<String>getOne("number").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");
                    printError(src, "§4Error: §cNo arguments found. Please enter a Pokédex number.");

                    canContinue = false;
                }
                else
                {
                    numberString = args.<String>getOne("number").get();

                    if (numberString.matches("-?[1-9]\\d*|0"))
                    {
                        printToLog(2, "Provided argument is an integer, let's check if it's in range.");
                        pokedexNumber = Integer.parseInt(args.<String>getOne("number").get());
                        
                        if (pokedexNumber > 807 || pokedexNumber < 1)
                        {
                            printError(src, "§4Error: §cInvalid Pokédex number! Valid range is 1-807.");
                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(1, "Invalid value provided for Pokédex number. Exit.");
                        printError(src, "§4Error: §cInput was not a number. Valid range is 1-807.");

                        canContinue = false;
                    }
                }

                if (args.hasAny("b"))
                    makePureBlack = true;

                if (args.hasAny("o"))
                    makeOutlined = true;

                if (args.hasAny("s"))
                    makeShiny = true;

                if (canContinue)
                {
                    printToLog(2, "No errors encountered, input should be valid. Continuing!");

                    /*                                                                            *\
                        FIXME: Get the player's facing angle and spawn Pokémon in front of them.
                        Currently it spawns stuff at a one-block distance, in a fixed direction.
                    \*                                                                            */

                    try
                    {
                        printToLog(2, "Attempting to grab a name for the given Pokédex number, §2" +
                                pokedexNumber + "§a.");

                        // Try to grab a Pokémon name for the given Pokédex number. Fix it if it's a known bad name.
                        String pokemonName = Objects.requireNonNull(EnumPokemonList.getPokemonFromID(pokedexNumber)).name();
                        if (pokemonName.equals("HoOh"))
                            pokemonName = "Ho-Oh";

                        // Set up variables for spawning. Nabbed some code here from Pixelmon.
                        EntityPlayerMP playerEntity = (EntityPlayerMP) src;
                        WorldServer world = playerEntity.getServerWorld();
                        BlockPos playerPos = playerEntity.getPosition();
                        EntityPixelmon pokemonToSpawn = PokemonSpec.from(pokemonName).create(world);

                        // Configure the Pokémon-to-spawn.
                        pokemonToSpawn.setPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                        pokemonToSpawn.setSpawnLocation(pokemonToSpawn.getDefaultSpawnLocation());

                        // If we have an odd internal name, fix it up before showing it to our user. Re-use the old String.
                        switch (pokemonName)
                        {
                            // Stuff that's currently in.
                            case "NidoranFemale":
                                pokemonName = "Nidoran♀"; break;
                            case "NidoranMale":
                                pokemonName = "Nidoran♂"; break;
                            case "Farfetchd":
                                pokemonName = "Farfetch'd"; break;
                            case "MrMime":
                                pokemonName = "Mr. Mime"; break;
                            case "HoOh":
                                pokemonName = "Ho-Oh"; break;
                            case "MimeJr":
                                pokemonName = "Mime Jr."; break;
                            case "Flabebe":
                                pokemonName = "Flabébé"; break;

                            // Stuff that's not in, yet. Added so we're ready for future updates. Probably needs work.
                            case "TypeNull":
                                pokemonName = "Type: Null"; break;
                            case "JangmoO":
                                pokemonName = "Jangmo-O"; break;
                            case "HakamoO":
                                pokemonName = "Hakamo-O"; break;
                            case "KommoO":
                                pokemonName = "Kommo-O"; break;
                            case "TapuKoko":
                                pokemonName = "Tapu Koko"; break;
                            case "TapuLele":
                                pokemonName = "Tapu Lele"; break;
                            case "TapuBulu":
                                pokemonName = "Tapu Bulu"; break;
                            case "TapuFini":
                                pokemonName = "Tapu Fini"; break;
                        }

                        // Do fancy stuff to the spawned Pokémon (if a flag is passed), and show some fitting messages.
                        printToLog(1, "Now spawning a §2" + pokemonName + "§a, and exiting.");
                        src.sendMessage(Text.of("§7-----------------------------------------------------"));
                        src.sendMessage(Text.of("§aSetting up a fresh §2" + pokemonName + "§a..."));

                        if (makeShiny)
                        {
                            src.sendMessage(Text.of("§eMaking the Pokémon §lshiny§r§e..."));
                            pokemonToSpawn.setIsShiny(true);
                        }
                        if (makeOutlined)
                        {
                            src.sendMessage(Text.of("§eGiving the Pokémon §lan outline§r§e..."));
                            pokemonToSpawn.setGlowing(true); // Yeah, weird name. Works, though.
                        }
                        if (makePureBlack)
                        {
                            src.sendMessage(Text.of("§eTurning the Pokémon §lpure black§r§e..."));
                            pokemonToSpawn.setIsRed(true); // Yeah, weird name. Works, though.
                        }

                        // Actually spawn it.
                        world.spawnEntity(pokemonToSpawn);

                        // Notify the player and wrap up.
                        src.sendMessage(Text.of("§aThe chosen Pokémon has been spawned!"));
                        src.sendMessage(Text.of("§7-----------------------------------------------------"));
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

    private void printError(CommandSource src, String errorString)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(errorString));
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokédex number> {one or more flags}"));
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§6Valid flags:"));
        src.sendMessage(Text.of("§f--> §6-b §f- §eTurns spawns entirely black. Reverts when caught."));
        src.sendMessage(Text.of("§f--> §6-o §f- §eGives spawns an outline that shows through walls."));
        src.sendMessage(Text.of("§f--> §6-s §f- §eMakes spawns shiny."));
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§5Please note: §dOutlined Pokémon stay outlined if caught."));
        src.sendMessage(Text.of("§dThe effect persists even through trades and evolutions!"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
