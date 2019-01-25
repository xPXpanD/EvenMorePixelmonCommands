// Originally created for testing things for the Pixelmon Wiki, after which it slowly morphed into this crazy thing.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import rs.expand.pixelupgrade.utilities.PokemonMethods;

// TODO: Add target and console support.
// TODO: Add more flags, like scale/special texture/IVs/EnumBossMode. Needs testing, already went through booleans.
// FIXME: Some names with multiple words (like "Mime Jr.") don't work right. Hard to fix, but nice polish?
public class SpawnDex implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias, fakeMessage;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String arg1String)
    { PrintingMethods.printDebugMessage("SpawnDex", debugNum, arg1String); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (fakeMessage == null)
                nativeErrorArray.add("fakeMessage");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("SpawnDex", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                boolean canContinue = true, doFakeAnnouncement = false, makeOutlined = false, doRadiusSpawn = false,
                        makeShiny = false;
                final Optional<String> arg1Optional = args.getOne("Pokémon name/ID");
                String arg1String;
                String pokemonName = null;
                int diameter = 0;

                if (!arg1Optional.isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");
                    printError(src, "§4Error: §cNo arguments found. Please enter a Pokédex number.");

                    canContinue = false;
                }
                else
                {
                    arg1String = arg1Optional.get();

                    if (arg1String.matches("^[1-9]\\d*$"))
                    {
                        printToLog(2, "Provided argument is numeric. Checking if it's in range.");
                        final int pokedexNumber = Integer.parseInt(arg1Optional.get());

                        if (pokedexNumber > 807 || pokedexNumber < 1)
                        {
                            printToLog(1, "Invalid Pokédex number provided. Exit.");
                            printError(src, "§4Error: §cInvalid Pokédex number! Valid range is 1-807.");
                            canContinue = false;
                        }
                        else
                        {
                            printToLog(2, "Attempting to grab a name for the given Pokédex number, §2" +
                                    pokedexNumber + "§a.");

                            try
                            {
                                pokemonName = Objects.requireNonNull(PokemonMethods.getPokemonFromID(pokedexNumber)).name();
                                printToLog(2, "Successfully grabbed a Pokémon name: §2" + pokemonName);
                            }
                            catch (final NullPointerException F)
                            {
                                printToLog(1, "Spawning failed, Pokémon may not be in the mod yet. Exit.");
                                src.sendMessage(Text.of("§4Error: §cSpawning failed. This Pokémon is likely not in yet."));

                                canContinue = false;
                            }
                        }
                    }
                    else if (arg1String.matches("[-.'2éÉA-Za-z]+"))
                    {
                        printToLog(2, "Provided argument is a String. Checking if it's valid.");

                        try
                        {
                            // Awkward hacky fix to get around internal Pixelmon logic, and some mistake fixes.
                            switch (arg1String.toUpperCase())
                            {
                                // Workarounds.
                                case "HOOH":
                                    arg1String = "Hooh"; break;
                                case "MIMEJR.":
                                    arg1String = "MimeJr"; break;

                                // Convenience fixes.
                                case "MR.MIME":
                                    arg1String = "MrMime"; break;
                                case "FLABÉBE": case "FLABEBÉ":
                                    arg1String = "MrMime"; break;
                            }

                            pokemonName = EnumSpecies.getFromName(arg1String).orElseThrow(Exception::new).toString();
                            printToLog(2, "Successfully grabbed a Pokémon name: §2" + pokemonName);
                        }
                        catch (final Exception F)
                        {
                            printToLog(1, "Spawning failed, input either invalid or not added yet. Exit.");
                            src.sendMessage(Text.of("§4Error: §cSpawning failed. Check input, make sure it's been added."));

                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(1, "Invalid value provided for Pokémon number/name. Exit.");
                        printError(src, "§4Error: §cInvalid number or name for Pokémon. See below.");

                        canContinue = false;
                    }
                }

                // Dumb flags.
                if (args.hasAny("f"))
                    doFakeAnnouncement = true;

                if (args.hasAny("o"))
                    makeOutlined = true;

                if (args.hasAny("s"))
                    makeShiny = true;

                // Advanced flags with actual logic.
                if (args.hasAny("r"))
                {
                    doRadiusSpawn = true;

                    final Optional<String> arg2Optional = args.getOne("optional square radius");

                    if (arg2Optional.isPresent())
                    {
                        final String arg2String = arg2Optional.get();

                        if (arg2String.matches("^[1-9]\\d*$"))
                        {
                            try
                            {
                                // Read as a short so we can safely squeeze it into an int even when doubled.
                                // This also gives us an easy cap on spawning area size, no need to go out >32767 blocks.
                                // We need doubling to turn the player-friendly radius into our internally-used diameter.
                                // "spawn up to X blocks away from me" is probably more intuitive for our players.
                                diameter = Short.parseShort(arg2String) * 2;
                                printToLog(2, "Got a radius flag and valid matching number: §2" + arg2String);
                            }
                            catch (final NumberFormatException F)
                            {
                                printToLog(1, "Out-of-bounds value provided for radius short. Exit.");
                                printError(src, "§4Error: §cRadius was too big to handle safely, try lowering it.");

                                canContinue = false;
                            }
                        }
                        else
                        {
                            printToLog(1, "Invalid value provided for radius int. Exit.");
                            printError(src, "§4Error: §cInvalid radius provided for flag -r. See below.");

                            canContinue = false;
                        }
                    }
                    else
                    {
                        printToLog(1, "Radius flag passed, but no radius provided. Exit.");
                        printError(src, "§4Error: §cA radius is needed for flag -r, please add one.");

                        canContinue = false;
                    }
                }

                if (canContinue)
                {
                    try
                    {
                        printToLog(2, "Starting spawning setup...");

                        // Make pretty names out of internal ones. They seem to be spawnable, too.
                        switch (pokemonName.toUpperCase())
                        {
                            // Stuff that's currently in.
                            case "NIDORANFEMALE":
                                pokemonName = "Nidoran♀"; break;
                            case "NIDORANMALE":
                                pokemonName = "Nidoran♂"; break;
                            case "FARFETCHD":
                                pokemonName = "Farfetch'd"; break;
                            case "MRMIME":
                                pokemonName = "Mr. Mime"; break;
                            case "HOOH":
                                pokemonName = "Ho-Oh"; break;
                            case "MIMEJR":
                                pokemonName = "Mime Jr."; break;
                            case "FLABEBE":
                                pokemonName = "Flabébé"; break;

                            // Stuff that's not in, yet. Added so we're ready for future updates. Probably needs work.
                            case "TYPENULL":
                                pokemonName = "Type: Null"; break;
                            case "JANGMOO":
                                pokemonName = "Jangmo-O"; break;
                            case "HAKAMOO":
                                pokemonName = "Hakamo-O"; break;
                            case "KOMMOO":
                                pokemonName = "Kommo-O"; break;
                            case "TAPUKOKO":
                                pokemonName = "Tapu Koko"; break;
                            case "TAPULELE":
                                pokemonName = "Tapu Lele"; break;
                            case "TAPUBULU":
                                pokemonName = "Tapu Bulu"; break;
                            case "TAPUFINI":
                                pokemonName = "Tapu Fini"; break;
                        }

                        // Set up variables for spawning.
                        // Bit of Pixelmon, a raytracing snippet, and a lot of messing around... but I finally got it working!
                        final EntityPlayerMP playerEntity = (EntityPlayerMP) src;
                        final WorldServer world = playerEntity.getServerWorld();
                        final EntityPixelmon pokemonToSpawn = PokemonSpec.from(pokemonName).create(world);
                        final Player player = (Player) src;
                        final Location playerPos = player.getLocation();

                        if (doRadiusSpawn)
                        {
                            // Calculate a random X offset, and apply it to our player's X coordinate.
                            final int offsetX = RandomHelper.rand.nextInt(diameter) - (diameter / 2);
                            final int newXPos;
                            newXPos = playerPos.getBlockX() + offsetX;

                            // Calculate a random Z offset, and apply it to our player's Z coordinate.
                            final int offsetZ = RandomHelper.rand.nextInt(diameter) - (diameter / 2);
                            final int newZPos;
                            newZPos = playerPos.getBlockZ() + offsetZ;

                            printToLog(2, "Checked radius, spawning at random coords: §2" +
                                    newXPos + ", " + playerPos.getBlockY() + ", " + newZPos);

                            pokemonToSpawn.setPosition(newXPos, playerPos.getBlockY() + 1, newZPos);
                        }
                        else
                        {
                            final Optional<BlockRayHit<World>> optTargetBlock =
                                    BlockRay.from(player) // v : Stop filtering after we hit the first non-air block.
                                            .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                                            .distanceLimit(10).build().end();

                            if (optTargetBlock.isPresent())
                            {
                                final BlockRayHit<World> targetBlock = optTargetBlock.get();

                                printToLog(2, "Raytracing successful, spawning on block: §2" +
                                        targetBlock.getBlockPosition());

                                // Spawn on the air block closest to our found solid block.
                                // If no solid blocks are found, we'll just spawn in air at the distanceLimit.
                                pokemonToSpawn.setPosition(
                                        targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
                            }
                            else
                            {
                                printToLog(2, "Raytracing failed, spawning near player at: §2" +
                                        playerPos.getBlockX() + ", " + playerPos.getBlockY() + ", " + playerPos.getBlockZ());

                                pokemonToSpawn.setPosition(
                                        playerPos.getX() + 1, // Offset to avoid spawning inside of people.
                                        playerPos.getY() + 2, // Also avoid spawning below the ground.
                                        playerPos.getZ() + 1  // Same story as the first offset.
                                );
                            }
                        }

                        // Do fancy stuff to the spawned Pokémon (if a flag is passed), and show some fitting messages.
                        src.sendMessage(Text.of("§7-----------------------------------------------------"));
                        src.sendMessage(Text.of("§aSetting up a fresh §2" + pokemonName + "§a..."));

                        // Run through our flag executors.
                        if (doFakeAnnouncement)
                        {
                            // TODO: Maybe register our spawns directly with Pixelmon, using the stuff below? Dunno.
                            //SpawnEvent spawnEvent = new SpawnEvent();
                            //Pixelmon.EVENT_BUS.post(spawnEvent);

                            // Tell the calling player what we're doing, as usual.
                            src.sendMessage(Text.of("§eBroadcasting the §lconfig-set spawn message§r§e..."));

                            // Grab a biome name. This compiles fine if the access transformer is loaded correctly, despite any errors.
                            String biome =
                                    pokemonToSpawn.getEntityWorld().getBiomeForCoordsBody(pokemonToSpawn.getPosition()).biomeName;

                            // Add a space in front of every capital letter after the first.
                            int capitalCount = 0, iterator = 0;
                            while (iterator < biome.length())
                            {
                                // Is there an upper case character at the checked location?
                                if (Character.isUpperCase(biome.charAt(iterator)))
                                {
                                    // Add to the pile.
                                    capitalCount++;

                                    // Did we get more than one capital letter on the pile?
                                    if (capitalCount > 1)
                                    {
                                        // Look back: Was the previous character a space? If not, proceed with adding one.
                                        if (biome.charAt(iterator - 1) != ' ')
                                        {
                                            // Add a space at the desired location.
                                            biome = biome.substring(0, iterator) + ' ' + biome.substring(iterator);

                                            // Up the main iterator so we do not repeat the check on the character we're at now.
                                            iterator++;
                                        }
                                    }
                                }

                                // Up the iterator for another go, if we're below length().
                                iterator++;
                            }

                            // Replace any included placeholders in our message with what they represent.
                            String completedMessage = fakeMessage.replaceAll("(?i)%biome%", biome);
                            completedMessage = completedMessage.replaceAll("(?i)%pokemon%", pokemonName);

                            // Deserialize the given message as a Text, with given formatting turned into Text metadata.
                            MessageChannel.TO_PLAYERS.send(TextSerializers.FORMATTING_CODE.deserialize(completedMessage));

                            // Tell the console what we did, too.
                            printToLog(1, "Faked a Pixelmon message, in biome §3" + biome + "§b.");
                        }
                        if (makeOutlined)
                        {
                            src.sendMessage(Text.of("§eGiving the Pokémon §lan outline§r§e..."));
                            pokemonToSpawn.setGlowing(true); // Yeah, weird name. Works, though.
                        }
                        if (makeShiny)
                        {
                            src.sendMessage(Text.of("§eMaking the Pokémon §lshiny§r§e..."));
                            pokemonToSpawn.getPokemonData().setIsShiny(true);
                        }

                        // Actually spawn it.
                        world.spawnEntity(pokemonToSpawn);

                        // Notify the player and wrap up.
                        src.sendMessage(Text.of("§aThe chosen Pokémon has been spawned!"));
                        src.sendMessage(Text.of("§7-----------------------------------------------------"));

                        printToLog(1, "Spawning successful! Exiting.");
                    }
                    catch (final NullPointerException F)
                    {
                        src.sendMessage(Text.of("§cSpawning failed! Check console for what went wrong."));
                        printToLog(1, "Encountered an unknown spawning error. Please report. Trace:");
                        F.printStackTrace();
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printError(final CommandSource src, final String errorString)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(errorString));
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokémon name/number> {flags?} [radius?]"));
        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of("§6Valid flags:"));
        src.sendMessage(Text.of("§f➡ §6-f §f- §eBroadcasts a fake spawning message, as per the config."));
        src.sendMessage(Text.of("§f➡ §6-o §f- §eGives spawns an outline that shows through walls."));
        src.sendMessage(Text.of("§f➡ §6-r §f- §eSpawns a Pokémon randomly within the given radius."));
        src.sendMessage(Text.of("§f➡ §6-s §f- §eMakes spawns shiny."));
        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of("§5Please note: §dOutlined Pokémon stay outlined if caught."));
        src.sendMessage(Text.of("§dThe effect persists even through trades and evolutions!"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
