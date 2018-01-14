package rs.expand.pixelupgrade;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
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

        // NickImpact (helping me understand how to manipulate NBTs)
        // Proxying (writing to entities in a copy-persistent manner)
        // Karanum (fancy paginated command lists)
        // Hiroku (tip + snippet for setting up UTF-8 encoding; made § work)
        // Xenoyia (helping get PU off the ground, and co-owning the server it started on)
        // ...and everybody else who contributed ideas and reported issues.

        // Thanks for helping make PU what it is now, people!
)

public class PixelUpgrade
{
    // Some basic setup.
    public static EconomyService economyService;
    public static boolean economyEnabled = false;

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
    public static String path = "config" + separator + "PixelUpgrade" + separator;

    // Create the config paths.
    public static Path primaryConfigPath = Paths.get(primaryPath, "PixelUpgrade.conf");
    public static Path checkEggPath = Paths.get(path, "CheckEgg.conf");
    public static Path checkStatsPath = Paths.get(path, "CheckStats.conf");
    public static Path checkTypesPath = Paths.get(path, "CheckTypes.conf");
    public static Path dittoFusionPath = Paths.get(path, "DittoFusion.conf");
    public static Path fixEVsPath = Paths.get(path, "FixEVs.conf");
    public static Path fixLevelPath = Paths.get(path, "FixLevel.conf");
    public static Path forceHatchPath = Paths.get(path, "ForceHatch.conf");
    public static Path forceStatsPath = Paths.get(path, "ForceStats.conf");
    public static Path puInfoPath = Paths.get(path, "PixelUpgradeInfo.conf");
    public static Path resetCountPath = Paths.get(path, "ResetCount.conf");
    public static Path resetEVsPath = Paths.get(path, "ResetEVs.conf");
    public static Path switchGenderPath = Paths.get(path, "SwitchGender.conf");
    public static Path showStatsPath = Paths.get(path, "ShowStats.conf");
    public static Path upgradeIVsPath = Paths.get(path, "UpgradeIVs.conf");

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
    public static ConfigurationLoader<CommentedConfigurationNode> puInfoLoader =
            HoconConfigurationLoader.builder().setPath(puInfoPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetCountLoader =
            HoconConfigurationLoader.builder().setPath(resetCountPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetEVsLoader =
            HoconConfigurationLoader.builder().setPath(resetEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> switchGenderLoader =
            HoconConfigurationLoader.builder().setPath(switchGenderPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> showStatsLoader =
            HoconConfigurationLoader.builder().setPath(showStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> upgradeIVsLoader =
            HoconConfigurationLoader.builder().setPath(upgradeIVsPath).build();

    /*                       *\
         Utility commands.
    \*                       */
    private CommandSpec reloadconfigs = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.reload")
            .executor(new ReloadConfigs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("config"))))
            .build();

    private CommandSpec pixelupgradeinfo = CommandSpec.builder()
            .executor(new PixelUpgradeInfo())
            .arguments( // Ignore all arguments, don't error on anything. Command doesn't use them, anyways.
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of(""))))
            .build();

    /*                    *\
         Main commands.
    \*                    */
    private CommandSpec checkegg = CommandSpec.builder()
            .permission("pixelupgrade.command.checkegg")
            .executor(new CheckEgg())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    private CommandSpec checkstats = CommandSpec.builder()
            .permission("pixelupgrade.command.checkstats")
            .executor(new CheckStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    private CommandSpec checktypes = CommandSpec.builder()
            .permission("pixelupgrade.command.checktypes")
            .executor(new CheckTypes())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("pokemon"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec dittofusion = CommandSpec.builder()
            .permission("pixelupgrade.command.dittofusion")
            .executor(new DittoFusion())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("sacrifice slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec fixevs = CommandSpec.builder()
            .permission("pixelupgrade.command.fixevs")
            .executor(new FixEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec fixlevel = CommandSpec.builder()
            .permission("pixelupgrade.command.fixlevel")
            .executor(new FixLevel())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec forcehatch = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcehatch")
            .executor(new ForceHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))
            .build();

    private CommandSpec forcestats = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcestats")
            .executor(new ForceStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value"))),
                    GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec resetcount = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.resetcount")
            .executor(new ResetCount())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("count"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec resetevs = CommandSpec.builder()
            .permission("pixelupgrade.command.resetevs")
            .executor(new ResetEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec switchgender = CommandSpec.builder()
            .permission("pixelupgrade.command.switchgender")
            .executor(new SwitchGender())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec showstats = CommandSpec.builder()
            .permission("pixelupgrade.command.showstats")
            .executor(new ShowStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec upgradeivs = CommandSpec.builder()
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
        // Note: We run an overloaded method for the primary config. That's why it knows where to go.
        printUnformattedMessage("========================= P I X E L U P G R A D E =========================");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(path));
            printUnformattedMessage("--> §aDetected first run, created a new folder for the command configs.");
        }
        catch (IOException ignored) {}

        printUnformattedMessage("--> §aLoading and validating primary config...");
        ConfigOperations.setupPrimaryConfig(primaryConfigPath, primaryPath);

        printUnformattedMessage("--> §aLoading and validating command-specific settings...");
        ConfigOperations.initializeAndGrabAliases(true);

        printUnformattedMessage("--> §aLoaded command settings. Pre-init completed.");
        printUnformattedMessage("===========================================================================");

        // And finally, register the aliases we grabbed earlier.
        if (CheckEgg.commandAlias != null && !CheckEgg.commandAlias.equals("checkegg"))
            Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck", CheckEgg.commandAlias);
        else
            Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck");

        if (CheckStats.commandAlias != null && !CheckStats.commandAlias.equals("checkstats"))
            Sponge.getCommandManager().register(this, checkstats, "checkstats", "getstats", CheckStats.commandAlias);
        else
            Sponge.getCommandManager().register(this, checkstats, "checkstats", "getstats");

        if (CheckTypes.commandAlias != null && !CheckTypes.commandAlias.equals("checktypes"))
            Sponge.getCommandManager().register(this, checktypes, "checktypes", "checktype", "weakness", CheckTypes.commandAlias);
        else
            Sponge.getCommandManager().register(this, checktypes, "checktypes", "checktype", "weakness");

        if (DittoFusion.commandAlias != null && !DittoFusion.commandAlias.equals("dittofusion"))
            Sponge.getCommandManager().register(this, dittofusion, "dittofusion", "fuseditto", DittoFusion.commandAlias);
        else
            Sponge.getCommandManager().register(this, dittofusion, "dittofusion", "fuseditto");

        if (FixEVs.commandAlias != null && !FixEVs.commandAlias.equals("fixevs"))
            Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev", FixEVs.commandAlias);
        else
            Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev");

        if (FixLevel.commandAlias != null && !FixLevel.commandAlias.equals("fixlevel"))
            Sponge.getCommandManager().register(this, fixlevel, "fixlevel", "fixlevels", FixLevel.commandAlias);
        else
            Sponge.getCommandManager().register(this, fixlevel, "fixlevel", "fixlevels");

        if (ForceHatch.commandAlias != null && !ForceHatch.commandAlias.equals("forcehatch"))
            Sponge.getCommandManager().register(this, forcehatch, "forcehatch", ForceHatch.commandAlias);
        else
            Sponge.getCommandManager().register(this, forcehatch, "forcehatch");

        if (ForceStats.commandAlias != null && !ForceStats.commandAlias.equals("forcestats"))
            Sponge.getCommandManager().register(this, forcestats, "forcestats", "forcestat", ForceStats.commandAlias);
        else
            Sponge.getCommandManager().register(this, forcestats, "forcestats", "forcestat");

        if (PixelUpgradeInfo.commandAlias != null && !PixelUpgradeInfo.commandAlias.equals("pixelupgrade"))
            Sponge.getCommandManager().register(this, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo", PixelUpgradeInfo.commandAlias);
        else
            Sponge.getCommandManager().register(this, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo");

        Sponge.getCommandManager().register(this, reloadconfigs, "pureload", "pixelupgradereload");

        if (ResetCount.commandAlias != null && !ResetCount.commandAlias.equals("resetcount"))
            Sponge.getCommandManager().register(this, resetcount, "resetcount", "resetcounts", ResetCount.commandAlias);
        else
            Sponge.getCommandManager().register(this, resetcount, "resetcount", "resetcounts");

        if (ResetEVs.commandAlias != null && !ResetEVs.commandAlias.equals("resetevs"))
            Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev", ResetEVs.commandAlias);
        else
            Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev");

        if (SwitchGender.commandAlias != null && !SwitchGender.commandAlias.equals("switchgender"))
            Sponge.getCommandManager().register(this, switchgender, "switchgender", SwitchGender.commandAlias);
        else
            Sponge.getCommandManager().register(this, switchgender, "switchgender");

        if (ShowStats.commandAlias != null && !ShowStats.commandAlias.equals("showstats"))
            Sponge.getCommandManager().register(this, showstats, "showstats", ShowStats.commandAlias);
        else
            Sponge.getCommandManager().register(this, showstats, "showstats");

        if (UpgradeIVs.commandAlias != null && !UpgradeIVs.commandAlias.equals("upgradeivs"))
            Sponge.getCommandManager().register(this, upgradeivs, "upgradeivs", "upgradeiv", UpgradeIVs.commandAlias);
        else
            Sponge.getCommandManager().register(this, upgradeivs, "upgradeivs", "upgradeiv");
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
}