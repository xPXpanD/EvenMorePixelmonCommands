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
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.commands.*;
import rs.expand.pixelupgrade.configs.*;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

//TODO: Add fixlevel.
//TODO: Maybe make a /showstats or /printstats.
//TODO: Consider making a shiny upgrade command and a gender swapper.
//TODO: Make a Pokémon transfer command.
//TODO: Check if setting stuff to player entities works, too!
//TODO: UpgradeIVs token support?
//TODO: Maybe make a heal command with a hour-long cooldown.
//TODO: Make a /pokesell, maybe one that sells based on ball worth.
//TODO: Make a hidden ability switcher, maybe.
//TODO: Make an admin command that de-flags upgraded/fused Pokémon.
//TODO: Check if the proper events are called on commands like forcehatch?
//TODO: Check public static final String PC_RAVE = "rave";
//TODO: Tab completion on player names.
//TODO: See if recoloring Pokémon is possible.
//TODO: Look into name colors.
//TODO: Make a Pokéball changing command, get it to write the old ball to the Pokémon for ball sale purposes.

//TODO: Redo Checkstats styling to fit weakness. Checkegg, too.

@Plugin
(
        id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "1.9",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds a whole bunch of utility commands to Pixelmon, and also a good few economy-paid upgrades.",
        authors = "XpanD"
        // Not listed but certainly appreciated: A lot of early help from Xenoyia, plus breakthrough snippets from...
        // NickImpact (NBT editing), Proxying (writing to entities in a copy-persistent manner) and Karanum (fancy paginated command list)!
)

public class PixelUpgrade
{
    private static final String name = "PixelUpgrade";
    public static final Logger log = LoggerFactory.getLogger(name);
    public static EconomyService economyService;
    public String path = "config" + FileSystems.getDefault().getSeparator() + "PixelUpgrade";

    private static PixelUpgrade instance;
    public static PixelUpgrade getInstance()
    {   return instance;    }

    public Path cmdCheckEggPath = Paths.get(path, "CheckEgg.conf");
    public Path cmdCheckStatsPath = Paths.get(path, "CheckStats.conf");
    public Path cmdCheckTypesPath = Paths.get(path, "CheckTypes.conf");
    public Path cmdDittoFusionPath = Paths.get(path, "DittoFusion.conf");
    public Path cmdFixEVsPath = Paths.get(path, "FixEVs.conf");
    public Path cmdForceHatchPath = Paths.get(path, "ForceHatch.conf");
    public Path cmdForceStatsPath = Paths.get(path, "ForceStats.conf");
    public Path cmdPixelUpgradeInfoPath = Paths.get(path, "PixelUpgradeInfo.conf");
    public Path cmdResetEVsPath = Paths.get(path, "ResetEVs.conf");
    public Path cmdUpgradeIVsPath = Paths.get(path, "UpgradeIVs.conf");

    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckEggLoader = HoconConfigurationLoader.builder().setPath(cmdCheckEggPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckStatsLoader = HoconConfigurationLoader.builder().setPath(cmdCheckStatsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckTypesLoader = HoconConfigurationLoader.builder().setPath(cmdCheckTypesPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdDittoFusionLoader = HoconConfigurationLoader.builder().setPath(cmdDittoFusionPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdFixEVsLoader = HoconConfigurationLoader.builder().setPath(cmdFixEVsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdForceHatchLoader = HoconConfigurationLoader.builder().setPath(cmdForceHatchPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdForceStatsLoader = HoconConfigurationLoader.builder().setPath(cmdForceStatsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdPixelUpgradeInfoLoader = HoconConfigurationLoader.builder().setPath(cmdPixelUpgradeInfoPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdResetEVsLoader = HoconConfigurationLoader.builder().setPath(cmdResetEVsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdUpgradeIVsLoader = HoconConfigurationLoader.builder().setPath(cmdUpgradeIVsPath).build();

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event)
    {
        if (event.getService().equals(EconomyService.class))
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
    }

    /*                       *\
         Utility commands.
    \*                       */
    private CommandSpec reload = CommandSpec.builder()
            .description(Text.of("Reloads all of the PixelUpgrade command configs, or recreates them if missing."))
            .permission("pixelupgrade.command.admin.reload")
            .executor(new ReloadConfigs())
            .build();

    private CommandSpec pixelupgradeinfo = CommandSpec.builder()
            .description(Text.of("Shows the PixelUpgrade subcommand listing."))
            .executor(new PixelUpgradeInfo())
            .child(reload, "reload")
            .build();

    /*                    *\
         Main commands.
    \*                    */

    private CommandSpec checkegg = CommandSpec.builder()
            .description(Text.of("Checks the contents of an egg. Can be set to vague or explicit."))
            .permission("pixelupgrade.command.checkegg")
            .executor(new CheckEgg())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    private CommandSpec checkstats = CommandSpec.builder()
            .description(Text.of("Shows a comprehensive list of Pok\u00E9mon stats, such as EVs/IVs/natures."))
            .permission("pixelupgrade.command.checkstats")
            .executor(new CheckStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    private CommandSpec checktypes = CommandSpec.builder()
            .description(Text.of("Shows resistances and weaknesses for any Pok\u00E9mon."))
            .permission("pixelupgrade.command.checktypes")
            .executor(new CheckTypes())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("pokemon"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec dittofusion = CommandSpec.builder()
            .description(Text.of("Fuse Dittos together for economy balance, improving their stats!"))
            .permission("pixelupgrade.command.dittofusion")
            .executor(new DittoFusion())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("sacrifice slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec fixevs = CommandSpec.builder()
            .description(Text.of("Lowers EVs that are above 252, avoiding wasted points."))
            .permission("pixelupgrade.command.fixevs")
            .executor(new FixEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec forcehatch = CommandSpec.builder()
            .description(Text.of("Forcefully hatches a remote or local Pok\u00E9mon egg."))
            .permission("pixelupgrade.command.admin.forcehatch")
            .executor(new ForceHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))
            .build();

    private CommandSpec forcestats = CommandSpec.builder()
            .description(Text.of("Allows free setting of IVs and EVs on any Pok\u00E9mon."))
            .permission("pixelupgrade.command.admin.forcestats")
            .executor(new ForceStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value"))),
                    GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec resetevs = CommandSpec.builder()
            .description(Text.of("Completely wipes a local Pok\u00E9mon's EVs."))
            .permission("pixelupgrade.command.resetevs")
            .executor(new ResetEVs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec upgradeivs = CommandSpec.builder()
            .description(Text.of("Enables upgrading of Pok\u00E9mon IVs, for economy balance."))
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
        String checkEggAlias = CheckEggConfig.getInstance().loadOrCreateConfig(cmdCheckEggPath, cmdCheckEggLoader);
        String checkStatsAlias = CheckStatsConfig.getInstance().loadOrCreateConfig(cmdCheckStatsPath, cmdCheckStatsLoader);
        String checkTypesAlias = CheckTypesConfig.getInstance().loadOrCreateConfig(cmdCheckTypesPath, cmdCheckTypesLoader);
        String dittoFusionAlias = DittoFusionConfig.getInstance().loadOrCreateConfig(cmdDittoFusionPath, cmdDittoFusionLoader);
        String fixEVsAlias = FixEVsConfig.getInstance().loadOrCreateConfig(cmdFixEVsPath, cmdFixEVsLoader);
        String forceHatchAlias = ForceHatchConfig.getInstance().loadOrCreateConfig(cmdForceHatchPath, cmdForceHatchLoader);
        String forceStatsAlias = ForceStatsConfig.getInstance().loadOrCreateConfig(cmdForceStatsPath, cmdForceStatsLoader);
        String puInfoAlias = PixelUpgradeInfoConfig.getInstance().loadOrCreateConfig(cmdPixelUpgradeInfoPath, cmdPixelUpgradeInfoLoader);
        String resetEVsAlias = ResetEVsConfig.getInstance().loadOrCreateConfig(cmdResetEVsPath, cmdResetEVsLoader);
        String upgradeIVsAlias = UpgradeIVsConfig.getInstance().loadOrCreateConfig(cmdUpgradeIVsPath, cmdUpgradeIVsLoader);

        Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck", checkEggAlias);
        Sponge.getCommandManager().register(this, checkstats, "checkstats", "getstats", checkStatsAlias);
        Sponge.getCommandManager().register(this, checktypes, "checktypes", "checktype", "typecheck", "weakness", checkTypesAlias);
        Sponge.getCommandManager().register(this, dittofusion, "dittofusion", "fuseditto", "amalgamate", dittoFusionAlias); // There you go, Xen. /amalgamate is now a thing!
        Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev", fixEVsAlias);
        Sponge.getCommandManager().register(this, forcehatch, "forcehatch", "adminhatch", forceHatchAlias);
        Sponge.getCommandManager().register(this, forcestats, "forcestats", "forcestat", "adminstats", "adminstat", forceStatsAlias);
        Sponge.getCommandManager().register(this, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo", puInfoAlias);
        Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev", resetEVsAlias);
        Sponge.getCommandManager().register(this, upgradeivs, "upgradeivs", "upgradeiv", upgradeIVsAlias);

        log.info("\u00A7aCommands registered!");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    {
        log.info("\u00A7bAll systems nominal.");
    }
}