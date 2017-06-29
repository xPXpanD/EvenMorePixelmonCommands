package rs.expand.pixelupgrade;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.commands.*;
import rs.expand.pixelupgrade.configs.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// New things:
//TODO: Maybe make a /showstats or /printstats.
//TODO: Make a Pokémon transfer command.
//TODO: Maybe make a heal command with a hour-long cooldown?
//TODO: Make a /pokesell, maybe one that sells based on ball worth.
//TODO: Check public static final String PC_RAVE = "rave";
//TODO: See if recoloring Pokémon is possible.
//TODO: Look into name colors?
//TODO: Make a Pokéball changing command, get it to write the old ball to the Pokémon for ball sale purposes.

// Improvements to existing things:
//TODO: Tab completion on player names.
//TODO: Fancy hovers on /checkstats?
//TODO: It would be nice to just have a credits block and then a single line list of loaded commands on startup.
//TODO: Add natures to /checkegg explicit mode.

@Plugin
(
        id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "2.0-pre3",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds a whole bunch of utility commands to Pixelmon, and some economy-integrated commands, too.",
        authors = "XpanD"

        // Not listed but certainly appreciated:

        // NickImpact (helping me understand how to manipulate NBTs)
        // Proxying (writing to entities in a copy-persistent manner)
        // Karanum (fancy paginated command lists)
        // Hiroku (tip + snippet for setting up UTF-8 encoding; made § work)
        // Xenoyia (a LOT of early help, and some serious later stuff too)

        // Thanks for helping make PU what it is now, people!
)

public class PixelUpgrade
{
    // Primary setup.
    private static final String name = "PixelUpgrade";
    public static final Logger log = LoggerFactory.getLogger(name);
    public static EconomyService economyService;

    // Config-related setup.
    private String separator = FileSystems.getDefault().getSeparator();
    private String privatePath = "config" + separator;
    public String path = "config" + separator + "PixelUpgrade" + separator;
    private Path configPath = Paths.get("config" + separator + "PixelUpgrade" + separator);

    // Create an instance that other classes can access.
    private static PixelUpgrade instance;
    public static PixelUpgrade getInstance()
    {   return instance;   }

    // Set up the primary config.
    public Path primaryConfigPath = Paths.get(privatePath, "PixelUpgrade.conf");
    public ConfigurationLoader<CommentedConfigurationNode> primaryConfigLoader = HoconConfigurationLoader.builder().setPath(primaryConfigPath).build();

    // Set up the command config paths.
    public Path cmdCheckEggPath = Paths.get(path, "CheckEgg.conf");
    public Path cmdCheckStatsPath = Paths.get(path, "CheckStats.conf");
    public Path cmdCheckTypesPath = Paths.get(path, "CheckTypes.conf");
    public Path cmdDittoFusionPath = Paths.get(path, "DittoFusion.conf");
    public Path cmdFixEVsPath = Paths.get(path, "FixEVs.conf");
    public Path cmdFixLevelPath = Paths.get(path, "FixLevel.conf");
    public Path cmdForceHatchPath = Paths.get(path, "ForceHatch.conf");
    public Path cmdForceStatsPath = Paths.get(path, "ForceStats.conf");
    public Path cmdPixelUpgradeInfoPath = Paths.get(path, "PixelUpgradeInfo.conf");
    public Path cmdResetCountPath = Paths.get(path, "ResetCount.conf");
    public Path cmdResetEVsPath = Paths.get(path, "ResetEVs.conf");
    public Path cmdSwitchGenderPath = Paths.get(path, "SwitchGender.conf");
    public Path cmdUpgradeIVsPath = Paths.get(path, "UpgradeIVs.conf");

    // Load the command configs.
    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckEggLoader = HoconConfigurationLoader.builder().setPath(cmdCheckEggPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckStatsLoader = HoconConfigurationLoader.builder().setPath(cmdCheckStatsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckTypesLoader = HoconConfigurationLoader.builder().setPath(cmdCheckTypesPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdDittoFusionLoader = HoconConfigurationLoader.builder().setPath(cmdDittoFusionPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdFixEVsLoader = HoconConfigurationLoader.builder().setPath(cmdFixEVsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdFixLevelLoader = HoconConfigurationLoader.builder().setPath(cmdFixLevelPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdForceHatchLoader = HoconConfigurationLoader.builder().setPath(cmdForceHatchPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdForceStatsLoader = HoconConfigurationLoader.builder().setPath(cmdForceStatsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdPixelUpgradeInfoLoader = HoconConfigurationLoader.builder().setPath(cmdPixelUpgradeInfoPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdResetCountLoader = HoconConfigurationLoader.builder().setPath(cmdResetCountPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdResetEVsLoader = HoconConfigurationLoader.builder().setPath(cmdResetEVsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdSwitchGenderLoader = HoconConfigurationLoader.builder().setPath(cmdSwitchGenderPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdUpgradeIVsLoader = HoconConfigurationLoader.builder().setPath(cmdUpgradeIVsPath).build();

    @Listener // Needed for economy support.
    public void onChangeServiceProvider(ChangeServiceProviderEvent event)
    {
        if (event.getService().equals(EconomyService.class))
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
    }

    @Listener // Needed for the reload command.
    public void shareInstance(GameConstructionEvent event)
    {   instance = this;    }

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
    public void onPreInitializationEvent(GameInitializationEvent event)
    {
        // Create a config directory if it doesn't exist.
        try
        {
            Files.createDirectory(configPath);
            log.info("§dCould not find a PixelUpgrade config folder. Creating it!");
        }
        catch (IOException F)
        {   log.info("§dFound a PixelUpgrade config folder. Trying to load!");   }

        // Let's load up the main config on boot.
        PixelUpgradeMainConfig.getInstance().loadOrCreateConfig(primaryConfigPath, primaryConfigLoader);

        // Also, load up the command configs.
        // They return an alias from their matching configs, so we also assign that for re-use.
        String checkEggAlias = CheckEggConfig.getInstance().loadOrCreateConfig(cmdCheckEggPath, cmdCheckEggLoader);
        String checkStatsAlias = CheckStatsConfig.getInstance().loadOrCreateConfig(cmdCheckStatsPath, cmdCheckStatsLoader);
        String checkTypesAlias = CheckTypesConfig.getInstance().loadOrCreateConfig(cmdCheckTypesPath, cmdCheckTypesLoader);
        String dittoFusionAlias = DittoFusionConfig.getInstance().loadOrCreateConfig(cmdDittoFusionPath, cmdDittoFusionLoader);
        String fixEVsAlias = FixEVsConfig.getInstance().loadOrCreateConfig(cmdFixEVsPath, cmdFixEVsLoader);
        String fixLevelAlias = FixLevelConfig.getInstance().loadOrCreateConfig(cmdFixLevelPath, cmdFixLevelLoader);
        String forceHatchAlias = ForceHatchConfig.getInstance().loadOrCreateConfig(cmdForceHatchPath, cmdForceHatchLoader);
        String forceStatsAlias = ForceStatsConfig.getInstance().loadOrCreateConfig(cmdForceStatsPath, cmdForceStatsLoader);
        String puInfoAlias = PixelUpgradeInfoConfig.getInstance().loadOrCreateConfig(cmdPixelUpgradeInfoPath, cmdPixelUpgradeInfoLoader);
        String resetCountAlias = ResetCountConfig.getInstance().loadOrCreateConfig(cmdResetCountPath, cmdResetCountLoader);
        String resetEVsAlias = ResetEVsConfig.getInstance().loadOrCreateConfig(cmdResetEVsPath, cmdResetEVsLoader);
        String switchGenderAlias = SwitchGenderConfig.getInstance().loadOrCreateConfig(cmdSwitchGenderPath, cmdSwitchGenderLoader);
        String upgradeIVsAlias = UpgradeIVsConfig.getInstance().loadOrCreateConfig(cmdUpgradeIVsPath, cmdUpgradeIVsLoader);

        Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck", checkEggAlias);
        Sponge.getCommandManager().register(this, checkstats, "checkstats", "getstats", checkStatsAlias);
        Sponge.getCommandManager().register(this, checktypes, "checktypes", "checktype", "typecheck", "weakness", checkTypesAlias);
        Sponge.getCommandManager().register(this, dittofusion, "dittofusion", "fuseditto", "amalgamate", dittoFusionAlias); // There you go, Xen. /amalgamate is now a thing!
        Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev", fixEVsAlias);
        Sponge.getCommandManager().register(this, fixlevel, "fixlevel", "fixlevels", fixLevelAlias);
        Sponge.getCommandManager().register(this, forcehatch, "forcehatch", "adminhatch", forceHatchAlias);
        Sponge.getCommandManager().register(this, forcestats, "forcestats", "forcestat", "adminstats", "adminstat", forceStatsAlias);
        Sponge.getCommandManager().register(this, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo", puInfoAlias);
        Sponge.getCommandManager().register(this, reloadconfigs, "pureload", "pixelupgradereload");
        Sponge.getCommandManager().register(this, resetcount, "resetcount", "resetcounts", resetCountAlias);
        Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev", resetEVsAlias);
        Sponge.getCommandManager().register(this, switchgender, "switchgender", switchGenderAlias);
        Sponge.getCommandManager().register(this, upgradeivs, "upgradeivs", "upgradeiv", upgradeIVsAlias);

        log.info("§dCommands registered!");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    {   log.info("§bAll systems nominal.");   }
}