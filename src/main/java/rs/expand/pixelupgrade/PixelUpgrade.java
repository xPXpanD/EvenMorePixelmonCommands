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
import static rs.expand.pixelupgrade.utilities.CommonMethods.printBasicMessage;

/*                                                              *\
       THE WHO-KNOWS-WHEN LIST OF POTENTIALLY AWESOME IDEAS
    TODO: Add new TODOs here. Cross off TODOs if they're done.
\*                                                              */

// New things:
// TODO: Make a token redeeming command for shinies. Maybe make it a starter picker command, even. - Xenoyia
// TODO: Make a /pokesell, maybe one that sells based on ball worth.
// TODO: Maybe see if a cooldown on trading machines is possible? - FrostEffects
// TODO: Maybe use setIsRed on an EntityPixelmon to emulate "Who's that Pokémon"? Could use statues. - DaeM0nS
// TODO: Look into name colors?
// TODO: Make a Pokéball changing command, get it to write the old ball to the Pokémon for ball sale purposes.
// TODO: Do something with setPixelmonScale. Maybe a /spawnboss for super big high HP IV bosses with custom loot?
// TODO: Make a legendary spawner that mimics the vanilla Pixelmon spawning message.
// TODO: Make a random legendary spawner.
// TODO: Add a /pokehatch if that's not done yet. Something with two cooldowns, /timedheal-style.

// Improvements to existing things:
// TODO: Tab completion on player names.
// TODO: Maybe add some nice "====" borders to config node errors.
// TODO: Actually add an economy safe mode to things. Baby steps are done, time to take the leap.
// TODO: Make just about every command with a target show said target a message when stuff is being used on them.

@Plugin
(
        id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "4.0.0 beta",
        dependencies = @Dependency(id = "pixelmon"),
        //description = "Adds a whole bunch of utility commands to Pixelmon, with optional economy integration.",
        description = "Adds a whole bunch of utility commands to Pixelmon, some with economy integration.",
        authors = "XpanD"

        // Not listed but certainly appreciated:

        // NickImpact (helping me understand NBTs, and a good few useful snippets)
        // Proxying (writing to entities in a copy-persistent manner)
        // Karanum (fancy paginated command lists)
        // Hiroku (tip + snippet for setting up UTF-8 encoding; made § work)
        // Simon_Flash (helping with Sponge-related questions)
        // Xenoyia (helping get PU off the ground, and co-owning the server it started on)
        // ...and everybody else who contributed ideas and reported issues.

        // Thanks for helping make PU what it is now, people!
)

// Note: printBasicMessage is a static import for a function from CommonMethods, for convenience.
public class PixelUpgrade
{
    // Some basic setup.
    public static EconomyService economyService;
    public static boolean economyEnabled;

    // Load up a ton of variables for use by other commands. We'll fill these in during pre-init.
    public static Integer configVersion;
    public static Integer debugVerbosityMode;
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
    public static Path fixGendersPath = Paths.get(commandConfigPath, "FixGenders.conf");
    public static Path forceHatchPath = Paths.get(commandConfigPath, "ForceHatch.conf");
    public static Path forceStatsPath = Paths.get(commandConfigPath, "ForceStats.conf");
    public static Path puInfoPath = Paths.get(commandConfigPath, "PixelUpgradeInfo.conf");
    public static Path resetCountPath = Paths.get(commandConfigPath, "ResetCount.conf");
    public static Path resetEVsPath = Paths.get(commandConfigPath, "ResetEVs.conf");
    public static Path showStatsPath = Paths.get(commandConfigPath, "ShowStats.conf");
    public static Path spawnDexPath = Paths.get(commandConfigPath, "SpawnDex.conf");
    public static Path switchGenderPath = Paths.get(commandConfigPath, "SwitchGender.conf");
    public static Path timedHatchPath = Paths.get(commandConfigPath, "TimedHatch.conf");
    public static Path timedHealPath = Paths.get(commandConfigPath, "TimedHeal.conf");
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
    public static ConfigurationLoader<CommentedConfigurationNode> fixGendersLoader =
            HoconConfigurationLoader.builder().setPath(fixGendersPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> forceHatchLoader =
            HoconConfigurationLoader.builder().setPath(forceHatchPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> forceStatsLoader =
            HoconConfigurationLoader.builder().setPath(forceStatsPath).build();
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
    public static ConfigurationLoader<CommentedConfigurationNode> timedHatchLoader =
            HoconConfigurationLoader.builder().setPath(timedHatchPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> timedHealLoader =
            HoconConfigurationLoader.builder().setPath(timedHealPath).build();
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
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec checkstats = CommandSpec.builder()
            .permission("pixelupgrade.command.checkstats")
            .executor(new CheckStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec checktypes = CommandSpec.builder()
            .permission("pixelupgrade.command.checktypes")
            .executor(new CheckTypes())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("Pokémon name/ID"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("optional second word of name"))))
            .build();

    public static CommandSpec dittofusion = CommandSpec.builder()
            .permission("pixelupgrade.command.dittofusion")
            .executor(new DittoFusion())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("sacrifice slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec fixgenders = CommandSpec.builder()
            .permission("pixelupgrade.command.fixgenders")
            .executor(new FixGenders())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec forcehatch = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcehatch")
            .executor(new ForceHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))))
            .build();

    public static CommandSpec forcestats = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcestats")
            .executor(new ForceStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat/value"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value/force flag"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("force flag"))))
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
                    GenericArguments.flags().flag("b").flag("o").flag("s").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec switchgender = CommandSpec.builder()
            .permission("pixelupgrade.command.switchgender")
            .executor(new SwitchGender())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    public static CommandSpec timedhatch = CommandSpec.builder()
            .permission("pixelupgrade.command.timedhatch")
            .executor(new TimedHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    public static CommandSpec timedheal = CommandSpec.builder()
            .permission("pixelupgrade.command.timedheal")
            .executor(new TimedHeal())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target/slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot/confirmation"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
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
        printBasicMessage("");
        printBasicMessage("========================= P I X E L U P G R A D E =========================");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        ConfigOperations.checkConfigDir();

        printBasicMessage("--> §aLoading and validating primary config...");
        ConfigOperations.loadConfig("PixelUpgrade");

        printBasicMessage("--> §aLoading and validating command-specific settings...");
        ConfigOperations.loadAllCommandConfigs();
        ConfigOperations.printCommandsAndAliases();

        printBasicMessage("--> §aRegistering commands and known aliases with Sponge...");
        boolean registrationCompleted = ConfigOperations.registerCommands();

        if (registrationCompleted)
            printBasicMessage("--> §aPre-init completed.");
        printBasicMessage("===========================================================================");
        printBasicMessage("");
    }

    @Listener
    public void onPostInitEvent(GamePostInitializationEvent event)
    {
        printBasicMessage("");
        printBasicMessage("========================= P I X E L U P G R A D E =========================");
        printBasicMessage("--> §aChecking whether an economy plugin is present...");

        Optional<EconomyService> potentialEconomyService = Sponge.getServiceManager().provide(EconomyService.class);
        if (!potentialEconomyService.isPresent())
        {
            printBasicMessage("--> §cNo economy plugin was found. Some commands will break!");
            printBasicMessage("--> §eProper support for running without economies is coming soon.");
            //printBasicMessage("--> §eNo economy plugin was found. Proceeding with integration disabled.");
        }
        else
        {
            printBasicMessage("--> §aAn economy plugin was detected.");
            //printBasicMessage("--> §aAn economy plugin was detected. Enabling integration!");
            economyEnabled = true;
            economyService = potentialEconomyService.get();

            printBasicMessage("--> §aAll systems nominal.");
        }

        //printBasicMessage("--> §aAll systems nominal.");
        printBasicMessage("===========================================================================");
        printBasicMessage("");
    }

    @Listener
    public void onServerStartedEvent(GameStartedServerEvent event)
    {
        int currentInternalVersion = 400;
        if (PixelUpgrade.configVersion != null && currentInternalVersion > PixelUpgrade.configVersion)
        {
            printBasicMessage("");
            printBasicMessage("===========================================================================");
            printBasicMessage("§4/showstats §clikely has an outdated (§43.0§c or earlier) config.");
            printBasicMessage("");
            printBasicMessage("§6Please follow these steps to fix this:");
            printBasicMessage("§61. §eDelete §6ShowStats.conf§e, or move it somewhere safe.");
            printBasicMessage("§62. §eOpen §6PixelUpgrade.conf §eand change §6configVersion§e's value to §6400§e.");
            printBasicMessage("§63. §eUse §6/pureload all§e to create a new config and update the version.");
            printBasicMessage("§64. §eIf so desired, manually recover old settings and §6/pureload §eagain.");
            printBasicMessage("");
            printBasicMessage("§cThis command will have reduced functionality until this is fixed.");
            printBasicMessage("===========================================================================");
            printBasicMessage("");
        }
    }

    // Don't mind this, just me messing around.
    // This won't make it to release, that'd be awful. Might use it for another future feature's core, though!
    /*
    @Listener
    public void spawnTest(SpawnEntityEvent event)
    {
        event.getEntities().forEach(entity ->
        {
            if (entity instanceof EntityPixelmon)
            {
                printBasicMessage("We've got a Pixelmon entity!");
                EntityPixelmon targetEntity = (EntityPixelmon) entity;
                targetEntity.setHealth(5000);
                targetEntity.setIsRed(true); // Could use this for a "Who's that Pokémon?" kind of deal? :O
                //targetEntity.setFire(60);
                //targetEntity.setPixelmonScale(5);
            }
        });
    }
    */
}