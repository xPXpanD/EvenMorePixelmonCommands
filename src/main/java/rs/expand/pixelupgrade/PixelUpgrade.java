package rs.expand.pixelupgrade;

// Remote imports.
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.commands.*;
import rs.expand.pixelupgrade.utilities.ConfigOperations;
import static rs.expand.pixelupgrade.utilities.CommonMethods.printUnformattedMessage;

// New things:
// TODO: Make a Pokémon transfer command.
// TODO: Make a token redeeming command for shinies. Maybe make it a starter picker command, even.
// TODO: Maybe make a heal command with a hour-long cooldown?
// TODO: Make a /pokesell, maybe one that sells based on ball worth.
// TODO: Check public static final String PC_RAVE = "rave";
// TODO: See if recoloring Pokémon is possible.
// TODO: Look into name colors?
// TODO: Make a Pokéball changing command, get it to write the old ball to the Pokémon for ball sale purposes.
// TODO: Do something with setPixelmonScale. Maybe a /spawnboss for super big high HP IV bosses with custom loot?
// TODO: Make a /devolve, or something along those lines.
// TODO: Make a /fixgender. Priority.

// Improvements to existing things:
// TODO: Tab completion on player names.
// TODO: Maybe turn /dittofusion into a generic /fuse, with a Ditto-only config option.
// TODO: Add a Mew clone count check to /checkstats and /showstats.
// TODO: Add a compact mode to /showstats, maybe using hovers. Thanks for the idea, Willynator.

@Plugin
(
        id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "3.1 beta",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds a whole bunch of utility commands to Pixelmon, with optional economy integration.",
        authors = "XpanD"

        // Not listed but certainly appreciated:

        // NickImpact (helping me understand NBTs and remove console tags from my messages)
        // Proxying (writing to entities in a copy-persistent manner)
        // Karanum (fancy paginated command lists)
        // Hiroku (tip + snippet for setting up UTF-8 encoding; made § work)
        // Simon_Flash (helping with Sponge-related questions)
        // Xenoyia (helping get PU off the ground, and co-owning the server it started on)
        // ...and everybody else who contributed ideas and reported issues.

        // Thanks for helping make PU what it is now, people!
)

// Note: printUnformattedMessage is a static import for a function from CommonMethods, for convenience.
public class PixelUpgrade
{
    // Some basic setup.
    public static EconomyService economyService;
    public static boolean economyEnabled = false;
    public static int currentInternalVersion = 310;

    // Load up a ton of variables for use by other commands. We'll fill these in during pre-init.
    public static Integer configVersion;
    public static Integer debugLevel;
    public static Boolean useBritishSpelling;
    public static String shortenedHP;
    public static String shortenedAttack;
    public static String shortenedDefense;
    public static String shortenedSpecialAttack;
    public static String shortenedSpecialDefense;
    public static String shortenedSpeed;

    // Set up our config paths, and grab an OS-specific file path separator. This will usually be a forward slash.
    private static String separator = FileSystems.getDefault().getSeparator();
    public static String primaryPath = "config" + separator;
    public static String commandConfigPath = "config" + separator + "PixelUpgrade" + separator;

    // Create the config paths.
    public static Path primaryConfigPath = Paths.get(primaryPath, "PixelUpgrade.conf");
    public static Path checkEggPath = Paths.get(commandConfigPath, "CheckEgg.conf");
    public static Path checkStatsPath = Paths.get(commandConfigPath, "CheckStats.conf");
    public static Path checkTypesPath = Paths.get(commandConfigPath, "CheckTypes.conf");
    public static Path dittoFusionPath = Paths.get(commandConfigPath, "DittoFusion.conf");
    public static Path fixEVsPath = Paths.get(commandConfigPath, "FixEVs.conf");
    public static Path fixLevelPath = Paths.get(commandConfigPath, "FixLevel.conf");
    public static Path forceHatchPath = Paths.get(commandConfigPath, "ForceHatch.conf");
    public static Path forceStatsPath = Paths.get(commandConfigPath, "ForceStats.conf");
    public static Path pokeCurePath = Paths.get(commandConfigPath, "PokeCure.conf");
    public static Path puInfoPath = Paths.get(commandConfigPath, "PixelUpgradeInfo.conf");
    public static Path resetCountPath = Paths.get(commandConfigPath, "ResetCount.conf");
    public static Path resetEVsPath = Paths.get(commandConfigPath, "ResetEVs.conf");
    public static Path showStatsPath = Paths.get(commandConfigPath, "ShowStats.conf");
    public static Path spawnDexPath = Paths.get(commandConfigPath, "SpawnDex.conf");
    public static Path switchGenderPath = Paths.get(commandConfigPath, "SwitchGender.conf");
    public static Path upgradeIVsPath = Paths.get(commandConfigPath, "UpgradeIVs.conf");

    // Set up said paths.
    public static ConfigurationLoader<CommentedConfigurationNode> primaryConfigLoader =
            HoconConfigurationLoader.builder().setPath(primaryConfigPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkEggLoader =
            HoconConfigurationLoader.builder().setPath(checkEggPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkStatsLoader =
            HoconConfigurationLoader.builder().setPath(checkStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkTypesLoader =
            HoconConfigurationLoader.builder().setPath(checkTypesPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> dittoFusionLoader =
            HoconConfigurationLoader.builder().setPath(dittoFusionPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> fixEVsLoader =
            HoconConfigurationLoader.builder().setPath(fixEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> fixLevelLoader =
            HoconConfigurationLoader.builder().setPath(fixLevelPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> forceHatchLoader =
            HoconConfigurationLoader.builder().setPath(forceHatchPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> forceStatsLoader =
            HoconConfigurationLoader.builder().setPath(forceStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> pokeCureLoader =
            HoconConfigurationLoader.builder().setPath(pokeCurePath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> puInfoLoader =
            HoconConfigurationLoader.builder().setPath(puInfoPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetCountLoader =
            HoconConfigurationLoader.builder().setPath(resetCountPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetEVsLoader =
            HoconConfigurationLoader.builder().setPath(resetEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> showStatsLoader =
            HoconConfigurationLoader.builder().setPath(showStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> spawnDexLoader =
            HoconConfigurationLoader.builder().setPath(spawnDexPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> switchGenderLoader =
            HoconConfigurationLoader.builder().setPath(switchGenderPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> upgradeIVsLoader =
            HoconConfigurationLoader.builder().setPath(upgradeIVsPath).build();

    /*                       *\
         Utility commands.
    \*                       */
    public static CommandSpec reloadconfigs = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.reload")
            .executor(new ReloadConfigs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("config"))))
            .build();

    public static CommandSpec pixelupgradeinfo = CommandSpec.builder()
            .executor(new PixelUpgradeInfo())
            .arguments( // Ignore all arguments, don't error on anything. Command doesn't use them, anyways.
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of(""))))
            .build();

    /*                    *\
         Main commands.
    \*                    */
    public static CommandSpec checkegg = CommandSpec.builder()
            .permission("pixelupgrade.command.checkegg")
            .executor(new CheckEgg())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec checkstats = CommandSpec.builder()
            .permission("pixelupgrade.command.checkstats")
            .executor(new CheckStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec checktypes = CommandSpec.builder()
            .permission("pixelupgrade.command.checktypes")
            .executor(new CheckTypes())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("pokemon"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec dittofusion = CommandSpec.builder()
            .permission("pixelupgrade.command.dittofusion")
            .executor(new DittoFusion())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("sacrifice slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec fixevs = CommandSpec.builder()
            .permission("pixelupgrade.command.fixevs")
            .executor(new FixEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec fixlevel = CommandSpec.builder()
            .permission("pixelupgrade.command.fixlevel")
            .executor(new FixLevel())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec forcehatch = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcehatch")
            .executor(new ForceHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))
            .build();

    public static CommandSpec forcestats = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcestats")
            .executor(new ForceStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value"))),
                    GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec pokecure = CommandSpec.builder()
            .permission("pixelupgrade.command.pokecure")
            .executor(new PokeCure())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec resetcount = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.resetcount")
            .executor(new ResetCount())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("count"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec resetevs = CommandSpec.builder()
            .permission("pixelupgrade.command.resetevs")
            .executor(new ResetEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec showstats = CommandSpec.builder()
            .permission("pixelupgrade.command.showstats")
            .executor(new ShowStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec spawndex = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.spawndex")
            .executor(new SpawnDex())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("number"))),
                    GenericArguments.flags().flag("s").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec switchgender = CommandSpec.builder()
            .permission("pixelupgrade.command.switchgender")
            .executor(new SwitchGender())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec upgradeivs = CommandSpec.builder()
            .permission("pixelupgrade.command.upgradeivs")
            .executor(new UpgradeIVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("quantity"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    @Listener
    public void onPreInitEvent(GamePreInitializationEvent event)
    {
        // Load up the primary config and the info command config, and figure out the info alias.
        // We start printing stuff, here. If any warnings/errors pop up they'll be shown here.
        printUnformattedMessage("========================= P I X E L U P G R A D E =========================");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        ConfigOperations.checkConfigDir();

        printUnformattedMessage("--> §aLoading and validating primary config...");
        ConfigOperations.loadConfig("PixelUpgrade");

        // TODO: Catch wrong values.

        printUnformattedMessage("--> §aLoading and validating command-specific settings...");
        ConfigOperations.loadAllCommandConfigs();
        ConfigOperations.printCommandsAndAliases();

        printUnformattedMessage("--> §aRegistering commands and known aliases with Sponge...");
        boolean registrationCompleted = ConfigOperations.registerCommands();

        if (registrationCompleted)
            printUnformattedMessage("--> §aPre-init completed.");
        printUnformattedMessage("===========================================================================");
    }

    @Listener
    public void onPostInitEvent(GamePostInitializationEvent event)
    {
        printUnformattedMessage("========================= P I X E L U P G R A D E =========================");
        printUnformattedMessage("--> §aChecking whether an economy plugin is present...");

        Optional<EconomyService> potentialEconomyService = Sponge.getServiceManager().provide(EconomyService.class);
        if (!potentialEconomyService.isPresent())
            printUnformattedMessage("--> §eNo economy plugin was found. Proceeding with integration disabled.");
        else
        {
            printUnformattedMessage("--> §aAn economy plugin was detected. Enabling integration!");
            economyEnabled = true;
            economyService = potentialEconomyService.get();
        }

        printUnformattedMessage("--> §aAll systems nominal.");
        printUnformattedMessage("===========================================================================");
    }

    @Listener
    public void onServerStartedEvent(GameStartedServerEvent event)
    {
        if (PixelUpgrade.configVersion != null && currentInternalVersion > PixelUpgrade.configVersion)
        {
            printUnformattedMessage("===========================================================================");
            printUnformattedMessage("§4/showstats §clikely has an outdated config due to changes in PU 3.1.");
            printUnformattedMessage("");
            printUnformattedMessage("§6Please follow these steps to fix this:");
            printUnformattedMessage("§61. §eIf you modified §6ShowStats.conf§e, copy it somewhere safe.");
            printUnformattedMessage("§62. §eOpen §6PixelUpgrade.conf §eand change §6configVersion§e to §6310§e.");
            printUnformattedMessage("§63. §eUse §6/pureload all§e to create a new config and update the version.");
            printUnformattedMessage("");
            printUnformattedMessage("§cThe command will have reduced functionality until this is fixed.");
            printUnformattedMessage("===========================================================================");
        }
    }
}