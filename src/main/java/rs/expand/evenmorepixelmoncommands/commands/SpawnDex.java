// Originally created for testing things for the Pixelmon Wiki, after which it slowly morphed into this crazy thing.
package rs.expand.evenmorepixelmoncommands.commands;

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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedError;

// TODO: Add more flags, like scale/special texture/IVs/EnumBossMode. Needs testing with 7.0 stuff.
// TODO: Add Alolan Pokémon support.
// TODO: Move this to the nice name parsing setup /checktypes uses.
// FIXME: Some names with multiple words (like "Mime Jr.") don't work right. Hard to fix, but nice polish?
public class SpawnDex implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    public static String commandAlias, fakeMessage;

    // Set up a class name variable for internal use. We'll pass this to logging when showing a source is desired.
    private String sourceName = this.getClass().getSimpleName();

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> commandErrorList = new ArrayList<>();
            if (commandAlias == null)
                commandErrorList.add("commandAlias");
            if (fakeMessage == null)
                commandErrorList.add("fakeMessage");

            if (!commandErrorList.isEmpty())
            {
                PrintingMethods.printCommandNodeError("SpawnDex", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                boolean doFakeAnnouncement = false, makeOutlined = false, doRadiusSpawn = false/*, makeShiny = false*/;
                final Optional<String> arg1Optional = args.getOne("Pokémon name/ID");
                String arg1String, pokemonName;
                int diameter = 0;

                if (!arg1Optional.isPresent())
                {
                    printLocalError(src, "§4Error: §cNo arguments found. Please enter a Pokédex number.");
                    return CommandResult.empty();
                }
                else
                {
                    arg1String = arg1Optional.get();

                    if (arg1String.matches("^[1-9]\\d*$"))
                    {
                        final int pokedexNumber = Integer.parseInt(arg1Optional.get());

                        if (pokedexNumber > 807 || pokedexNumber < 1)
                        {
                            printLocalError(src, "§4Error: §cInvalid Pokédex number! Valid range is 1-807.");
                            return CommandResult.empty();
                        }
                        else
                        {
                            try
                            {
                                pokemonName = Objects.requireNonNull(PokemonMethods.getPokemonFromID(pokedexNumber)).name();
                            }
                            catch (final NullPointerException F)
                            {
                                src.sendMessage(Text.of("§4Error: §cSpawning failed. This Pokémon is likely not in yet."));
                                return CommandResult.empty();
                            }
                        }
                    }
                    else if (arg1String.matches("[-.'2éÉA-Za-z]+"))
                    {
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
                        }
                        catch (final Exception F)
                        {
                            src.sendMessage(Text.of("§4Error: §cSpawning failed. Check input, make sure it's been added."));
                            return CommandResult.empty();
                        }
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid number or name for Pokémon. See below.");
                        return CommandResult.empty();
                    }
                }

                // Dumb flags.
                if (args.hasAny("f"))
                    doFakeAnnouncement = true;

                if (args.hasAny("o"))
                    makeOutlined = true;

                /*if (args.hasAny("s"))
                    makeShiny = true;*/

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
                            }
                            catch (final NumberFormatException F)
                            {
                                printLocalError(src, "§4Error: §cRadius was too big to handle safely, try lowering it.");
                                return CommandResult.empty();
                            }
                        }
                        else
                        {
                            printLocalError(src, "§4Error: §cInvalid radius provided for flag -r. See below.");
                            return CommandResult.empty();
                        }
                    }
                    else
                    {
                        printLocalError(src, "§4Error: §cA radius is needed for flag -r, please add one.");
                        return CommandResult.empty();
                    }
                }

                try
                {
                    // Make pretty names out of internal ones. They seem to be spawnable, too.
                    switch (pokemonName.toUpperCase())
                    {
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

                            // Spawn on the air block closest to our found solid block.
                            // If no solid blocks are found, we'll just spawn in air at the distanceLimit.
                            pokemonToSpawn.setPosition(
                                    targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
                        }
                        else
                        {
                            pokemonToSpawn.setPosition(
                                    playerPos.getX() + 1, // Offset to avoid spawning inside of people.
                                    playerPos.getZ() + 1, // Same as above.
                                    playerPos.getY() + 2  // Offset to avoid spawning below the ground.
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
                    }
                    if (makeOutlined)
                    {
                        src.sendMessage(Text.of("§eGiving the Pokémon §lan outline§r§e..."));
                        pokemonToSpawn.setGlowing(true); // Yeah, weird name. Works, though.
                    }
                    /*if (makeShiny)
                    {
                        src.sendMessage(Text.of("§eMaking the Pokémon §lshiny§r§e..."));
                        pokemonToSpawn.getPokemonData().setShiny(true);
                    }*/

                    // Actually spawn it.
                    world.spawnEntity(pokemonToSpawn);

                    // Notify the player and wrap up.
                    src.sendMessage(Text.of("§aThe chosen Pokémon has been spawned!"));
                    src.sendMessage(Text.of("§7-----------------------------------------------------"));
                }
                catch (final NullPointerException F)
                {
                    src.sendMessage(Text.of("§cSpawning failed! Check console for what went wrong."));
                    printSourcedError(sourceName, "§cSomething went wrong during spawning, printing trace. Please report.");
                    F.printStackTrace();
                }
            }
        }
        else
            printSourcedError(sourceName,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokémon name/number> {flags?} [radius?]"));
        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of("§6Valid flags:"));
        src.sendMessage(Text.of("§f➡ §6-f §f- §eBroadcasts a fake spawning message from the config."));
        src.sendMessage(Text.of("§f➡ §6-o §f- §eGives spawns an outline that shows through walls."));
        src.sendMessage(Text.of("§f➡ §6-r §f- §eSpawns a Pokémon randomly within the given radius."));
        /*src.sendMessage(Text.of("§f➡ §6-s §f- §eMakes spawns shiny."));*/
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}
