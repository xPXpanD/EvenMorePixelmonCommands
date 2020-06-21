// Written for Pixelmon Reforged. Running this on Gens is unsupported and ill-advised, just like Gens itself.
package rs.expand.evenmorepixelmoncommands;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import rs.expand.evenmorepixelmoncommands.commands.*;
import rs.expand.evenmorepixelmoncommands.commands.subcommands.ListCommands;
import rs.expand.evenmorepixelmoncommands.commands.subcommands.ReloadConfigs;
import rs.expand.evenmorepixelmoncommands.utilities.ConfigMethods;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printUnformattedMessage;

/*                                                               *\
       THE WHO-KNOWS-WHEN LIST OF POTENTIALLY AWESOME IDEAS
    TODO: Add new TODOs here. Cross off TODOs if they're done.
      NOTE: Stuff that's here will not necessarily get made.
\*                                                               */

// New things:
// TODO: Make a token redeeming command for shinies? Maybe make it a starter picker command, even. - Xenoyia
// TODO: Look into name colors, or make a full-on rename command with color support. Maybe make it set a tag, and check.
// TODO: Make a Pokéball changing command, get it to write the old ball to the Pokémon for ball sale purposes.
// TODO: Do something with setPixelmonScale. Maybe a /spawnboss for super big high HP IV bosses with custom loot?
// TODO: Make a random legendary dice roll spawner. Give command already exists, but spawn does not.
// TODO: Make a command that counts the Pokémon in the world, maybe also nearby. - Mikirae (comment, not suggestion)
// TODO: Make a Wailord Bomb command that blows apart a Wailord into a LOT of cooked fish. - Faty
// TODO: Show custom textures. - Mikirae

// Improvements to existing things:
// TODO: Tab completion on player names.
// TODO: Make just about every command with a target show said target a message when stuff is being used on them.
// TODO: Move everything to lang files.
// TODO: When doing localization support, check 5 and 7 color ------ lines? Translate both types. (success/error)
// TODO: Replace more of PokemonMethods stuff with Pixelmon's own solutions.
// TODO: GenericArguments.withSuggestions() is a thing, maybe implement, replace current CMD system.
// TODO: Look deeper into whether in-battle healing and stuff is doable now. Cursory check had partial success.
// TODO: Add console use support for /resetevs and /switchgenders.
// TODO: Clickables! Especially the command confirmation messages. Remember to underline.
// TODO: Sending checked messages in printLocalError is a huge inconsistent mess. Fix this, somehow.
// TODO: Somehow support command block selectors.
// TODO: Move duplicate /showstats and /checkstats logic to PokemonMethods or something.

@Plugin
(
        id = "evenmorepixelmoncommands",
        name = "Even More Pixelmon Commands",
        version = "5.1.3",
        dependencies = @Dependency(id = "pixelmon"),
        description = "A sidemod for Pixelmon Reforged that adds a bunch of new commands, some with economy integration.",
        authors = "XpanD"

//        _____________________________________________________________________________________
        /*                                                                                     *\
        |   Not listed but certainly appreciated:                                               |
        |                                                                                       |
|-------|   * NickImpact (helping me understand NBTs, and a bunch of useful snippets)           |
        |   * Proxying (writing to entities in a copy-persistent manner)                        |
        |   * Karanum (fancy paginated command lists)                                           |
        |   * Hiroku (helping with questions and setting up UTF-8 encoding, which made § work)  |
        |   * Simon_Flash (helping with Sponge-related questions)                               |
        |   * happyzlife (providing a ton of snippets and help for translation stuff)           |
        |   * Xenoyia (helping get PU off the ground, and co-owning the server it started on)   |
|-------|   * ...and everybody else who contributed ideas and reported issues.                  |
        |                                                                                       |
        |   Thanks for helping make EMPC what it is now, people!                      -- XpanD  |
        \*_____________________________________________________________________________________*/
)

// Note: printUnformattedMessage is a static import for a method from PrintingMethods, for convenience.
public class EMPC
{
    // Set up an internal variable so we can see if we loaded correctly.
    private boolean loadedCorrectly = false;
    public static EconomyService economyService;
    public static boolean economyEnabled = false;
    public static String statSeparator = "§r, §a"; // Can be changed internally. Awaiting lang support for public tweaking.

    // Create some variables for main command use. We'll fill these in during init.
    public static Integer configVersion, numLinesPerPage;
    public static String commandAlias;

    // Create an array of stat Strings for use wherever we need shorthand notation. Position follows the games/main mod.
    public static String[] statShorthands = new String[6];

    // Set up our config paths, and grab an OS-specific file path separator. This will usually be a forward slash.
    private static final String separator = FileSystems.getDefault().getSeparator();
    public static String primaryPath = "config" + separator;
    public static String commandConfigPath = "config" + separator + "EMPC" + separator;

    // Create the config paths.
    public static Path primaryConfigPath = Paths.get(primaryPath, "EvenMorePixelmonCommands.conf");
    public static Path checkEVsPath = Paths.get(commandConfigPath, "CheckEVs.conf");
    public static Path checkStatsPath = Paths.get(commandConfigPath, "CheckStats.conf");
    public static Path checkTypesPath = Paths.get(commandConfigPath, "CheckTypes.conf");
    public static Path fixGendersPath = Paths.get(commandConfigPath, "FixGenders.conf");
    /*public static Path forceStatsPath = Paths.get(commandConfigPath, "ForceStats.conf");*/
    public static Path partyHatchPath = Paths.get(commandConfigPath, "PartyHatch.conf");
    public static Path partyHealPath = Paths.get(commandConfigPath, "PartyHeal.conf");
    public static Path randomTMPath = Paths.get(commandConfigPath, "RandomTM.conf");
    public static Path resetDexPath = Paths.get(commandConfigPath, "ResetDex.conf");
    public static Path resetEVsPath = Paths.get(commandConfigPath, "ResetEVs.conf");
    public static Path showStatsPath = Paths.get(commandConfigPath, "ShowStats.conf");
    public static Path spawnDexPath = Paths.get(commandConfigPath, "SpawnDex.conf");
    public static Path switchGenderPath = Paths.get(commandConfigPath, "SwitchGender.conf");
    public static Path timedHatchPath = Paths.get(commandConfigPath, "TimedHatch.conf");
    public static Path timedHealPath = Paths.get(commandConfigPath, "TimedHeal.conf");

    // Set up said paths.
    public static ConfigurationLoader<CommentedConfigurationNode> primaryConfigLoader =
            HoconConfigurationLoader.builder().setPath(primaryConfigPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkEVsLoader =
            HoconConfigurationLoader.builder().setPath(checkEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkStatsLoader =
            HoconConfigurationLoader.builder().setPath(checkStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkTypesLoader =
            HoconConfigurationLoader.builder().setPath(checkTypesPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> fixGendersLoader =
            HoconConfigurationLoader.builder().setPath(fixGendersPath).build();
    /*public static ConfigurationLoader<CommentedConfigurationNode> forceStatsLoader =
            HoconConfigurationLoader.builder().setPath(forceStatsPath).build();*/
    public static ConfigurationLoader<CommentedConfigurationNode> partyHatchLoader =
            HoconConfigurationLoader.builder().setPath(partyHatchPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> partyHealLoader =
            HoconConfigurationLoader.builder().setPath(partyHealPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> randomTMLoader =
            HoconConfigurationLoader.builder().setPath(randomTMPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetDexLoader =
            HoconConfigurationLoader.builder().setPath(resetDexPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetEVsLoader =
            HoconConfigurationLoader.builder().setPath(resetEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> showStatsLoader =
            HoconConfigurationLoader.builder().setPath(showStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> spawnDexLoader =
            HoconConfigurationLoader.builder().setPath(spawnDexPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> switchGenderLoader =
            HoconConfigurationLoader.builder().setPath(switchGenderPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> timedHatchLoader =
            HoconConfigurationLoader.builder().setPath(timedHatchPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> timedHealLoader =
            HoconConfigurationLoader.builder().setPath(timedHealPath).build();

    /*                        *\
         Utility commands.
    \*                        */

    private static final CommandSpec reloadconfigs = CommandSpec.builder()
            .permission("empc.command.staff.reload")
            .executor(new ReloadConfigs())
            .build();

    private static final CommandSpec commandlist = CommandSpec.builder()
            .executor(new ListCommands())
            .build();

    public static CommandSpec basecommand = CommandSpec.builder()
            .child(reloadconfigs, "reload")
            .child(commandlist, "list")
            .executor(new BaseCommand())
            .build();

    /*                     *\
         Main commands.
    \*                     */

    public static CommandSpec checkevs = CommandSpec.builder()
            .permission("empc.command.checkevs")
            .executor(new CheckEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("Pokémon name/ID"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("optional second word"))))
            .build();

    public static CommandSpec checkstats = CommandSpec.builder()
            .permission("empc.command.checkstats")
            .executor(new CheckStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec checktypes = CommandSpec.builder()
            .permission("empc.command.checktypes")
            .executor(new CheckTypes())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("Pokémon name/ID"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("optional second word"))))
            .build();

    public static CommandSpec fixgenders = CommandSpec.builder()
            .permission("empc.command.fixgenders")
            .executor(new FixGenders())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    /*public static CommandSpec forcestats = CommandSpec.builder()
            .permission("empc.command.staff.forcestats")
            .executor(new ForceStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat/value"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value/force flag"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("force flag"))))
            .build();*/

    public static CommandSpec partyhatch = CommandSpec.builder()
            .permission("empc.command.partyhatch")
            .executor(new PartyHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec partyheal = CommandSpec.builder()
            .permission("empc.command.partyheal")
            .executor(new PartyHeal())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec randomtm = CommandSpec.builder()
            .permission("empc.command.staff.randomtm")
            .executor(new RandomTM())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/flag"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("flag"))))
            .build();

    public static CommandSpec resetdex = CommandSpec.builder()
            .permission("empc.command.staff.resetdex")
            .executor(new ResetDex())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec resetevs = CommandSpec.builder()
            .permission("empc.command.resetevs")
            .executor(new ResetEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec showstats = CommandSpec.builder()
            .permission("empc.command.showstats")
            .executor(new ShowStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec spawndex = CommandSpec.builder()
            .permission("empc.command.staff.spawndex")
            .executor(new SpawnDex())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("Pokémon name/ID"))),
                    GenericArguments.flags().flag("f").flag("o").flag("r").flag("s").buildWith(GenericArguments.none()),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("optional square radius"))))
            .build();

    public static CommandSpec switchgender = CommandSpec.builder()
            .permission("empc.command.switchgender")
            .executor(new SwitchGender())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec timedhatch = CommandSpec.builder()
            .permission("empc.command.timedhatch")
            .executor(new TimedHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec timedheal = CommandSpec.builder()
            .permission("empc.command.timedheal")
            .executor(new TimedHeal())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();
    
    @Listener
    public void onGamePreInitEvent(final GamePreInitializationEvent event)
    {
        // Load up configs and figure out the hub command alias. Start printing. Methods may insert errors as they go.
        printUnformattedMessage("");
        printUnformattedMessage("======== E V E N  M O R E  P I X E L M O N  C O M M A N D S ========");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        // Store whether we actually loaded things up correctly in this bool, which we can check again later.
        loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            printUnformattedMessage("--> §aRegistering commands and known aliases with Sponge...");

            // Print super fancy command + alias overview to console. Even uses colors to show errors!
            ConfigMethods.printCommandsAndAliases();

            // Finish up.
            if (ConfigMethods.registerCommands())
                printUnformattedMessage("--> §aPre-init completed. All systems nominal.");
        }
        else
            printUnformattedMessage("--> §cLoad aborted due to critical errors.");

        // We're done, one way or another. Add a footer, and a space to avoid clutter with other marginal'd mods.
        printUnformattedMessage("====================================================================");
        printUnformattedMessage("");
    }

    @Listener
    public void onGamePostInitEvent(final GamePostInitializationEvent event)
    {
        if (loadedCorrectly)
        {
            printUnformattedMessage("");
            printUnformattedMessage("======== E V E N  M O R E  P I X E L M O N  C O M M A N D S ========");
            printUnformattedMessage("--> §aChecking whether an economy plugin is present...");

            final Optional<EconomyService> potentialEconomyService = Sponge.getServiceManager().provide(EconomyService.class);
            if (potentialEconomyService.isPresent())
            {
                printUnformattedMessage("--> §aAn economy plugin was detected. Enabling integration!");
                economyEnabled = true;
                economyService = potentialEconomyService.get();
            }
            else
                printUnformattedMessage("--> §eNo economy plugin was found. Proceeding with integration disabled!");

            printUnformattedMessage("--> §aWe are good to go.");
            printUnformattedMessage("====================================================================");
            printUnformattedMessage("");
        }
    }

    /*@Listener
    public void onServerStartedEvent(final GameStartedServerEvent event)
    {
        // Shown when we're running a config that is too outdated. Not shown on 4.0.0 configs, since they're fine.
        if (EMPC.configVersion != null && EMPC.configVersion < 400)
        {
            printUnformattedMessage("");
            printUnformattedMessage("======== E V E N  M O R E  P I X E L M O N  C O M M A N D S ========");
            printUnformattedMessage("§4PixelUpgrade §clikely has an outdated main config.");
            printUnformattedMessage("");
            printUnformattedMessage("§6Please follow these steps to fix this:");
            printUnformattedMessage("§61. §eOpen the \"§6config§e\" folder in the server's root.");
            printUnformattedMessage("§62. §eOpen PixelUpgrade's main config file, \"§6PixelUpgrade.conf\"§e.");
            printUnformattedMessage("§63. §eChange \"§6configVersion\"§e to §6\"410\"§e (without quotes), then save.");
            printUnformattedMessage("§64. §eOpen the \"§6PixelUpgrade§e\" folder and find §6\"ShowStats.conf§e\".");
            printUnformattedMessage("§65. §eDelete this file, or move it somewhere safe if changes were made.");
            printUnformattedMessage("§66. §eUse §6/pureload all §eto recreate this file and update our version.");
            printUnformattedMessage("§67. §eIf so desired, manually recover old settings and §6/pureload all §eagain.");
            printUnformattedMessage("");
            printUnformattedMessage("§cUntil this is done, §4/showstats §cwill have reduced functionality!");
            printUnformattedMessage("====================================================================");
            printUnformattedMessage("");
        }
    }*/
}