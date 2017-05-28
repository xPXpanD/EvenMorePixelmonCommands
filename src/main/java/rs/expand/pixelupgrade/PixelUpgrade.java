/* Welcome! Here's the obligatory short history of how this came to be.
 * I always do these, it's pretty fun for me to see how stuff all came together.
 *
 * 0.01: First version. Didn't do much of anything.
 * 0.02: Switched to Gradle, actually figured out how to do stuff.
 * 0.03: Progress! Reading IVs!
 * 0.1: Figured out how to write IVs! Plugin actually started looking viable!
 * 0.2: Figured out how to resize, also!
 * 0.3: Major rewrite, and changed /getivs to /upgrade stats.
 * 0.4: Removed "other person" option on /upgrade stats because it didn't work, and added pretty formatting.
 * 0.41: /upgrade stats turned into /getstats. Added a bunch of failsafes.
 * 0.42: Added /upgrade fixevs, and made /getstats check for wasted EVs >252.
 * 0.5: Added /upgrade force! First work towards making /upgrade IVs an actual thing.
 * 0.5.1: Rewrote /getstats, so that now null Pokémon no longer made everything die.
 * 0.5.2: Rewrote /upgrade force, too. Added sane caps.
 * 0.5.3: Added /upgrade resetevs. Finally figured out how to add optional parameters.
 * 0.5.4: Capped /upgrade force a bunch more, added a bypass flag. Made the main /upgrade info command.
 * 0.5.5: Sanity checking, part 3. Re-added other person support on /getstats, removed some useless checks, migrated to IntelliJ IDEA.
 * 0.5.6: Flipped parameters so that people could now run either /getstats SLOT or /getstats PLAYER SLOT, instead of the awkward /getstats SLOT PLAYER. Cleanup.
 * 0.6: Started work on /upgrade ivs. First launch version! Still private at this point.
 * 0.7: /upgrade IVs logic pretty much done, but still had to make it take cash and set stats. Got an exponential scale going.
 * 0.8: /upgrade IVs done!!
 * 0.8.1: Added a remote listing of all party Pokémon to /getstats, if the slot provided was empty.
 * 0.8.2: Added /forcehatch.
 * 0.9: Full internal rewrite of the way command arguments are handled. No more issues with certain characters causing massive console errors!
 * 1.0: Fixed up new bugs. Second launch version! Started private, became public after we decided to shut down server.
 * 1.1: Made /dittofusion. Yes, Ditto Fusion. Only /the/ most kick-ass command.
 * 1.2: Added caps to /dittofusion, and made it triple cash required when using a pre-upgraded sacrifice.
 * 1.3: Added a /checkegg.
 * 1.4: Properly paginated /upgrade, the main info command.
 * 1.5: Started work on configs. Changed /upgrade ivs to /upgrade, /upgrade (command listing) to /pixelupgrade and /upgrade force to /forcestats.
 * 1.6: Config pretty much done. Rewrote /checkegg, now supports remote players. Completely redid my parameter parser. Again.
 * 1.7-1.9: Finished checking over all the commands, and adding configs to them. This took a while!
 *
 * Enjoy the plugin!
 */

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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

//TODO: Add fixlevel.
//TODO: Maybe make a /showstats or /printstats.
//TODO: Consider making a shiny upgrade command and a gender swapper.
//TODO: Make a Pokémon transfer command.
//TODO: Check if setting stuff to player entities works, too!
//TODO: Upgrade token support.
//TODO: Maybe make a heal command with a hour-long cooldown.
//TODO: Make a /pokesell, maybe one that sells based on ball worth.
//TODO: Make a hidden ability switcher, maybe.
//TODO: Make an admin command that de-flags upgraded/fused Pokémon.
//TODO: Check if the proper events are called on commands like forcehatch.
//TODO: Add a /weakness, which checks the weaknesses of what you're fighting. Thanks for the idea, MageFX!
//TODO: Add egg step checking to /checkegg.
//TODO: Check public static final String PC_RAVE = "rave";
//TODO: Tab completion on player names.
//TODO: Rework /getstats fuse/upgrade printing once configs are in place for those commands.

@Plugin
(
        id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "1.9",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds a whole bunch of utility commands to Pixelmon, and also a good few economy-paid upgrades.",
        authors = "XpanD, with a bunch of help from Xenoyia"
        // + breakthrough snippets from NickImpact (NBT editing), Proxying (writing to entities in a copy-persistent manner) and Karanum (fancy paginated command list)!
)

public class PixelUpgrade
{
    private static final String name = "PixelUpgrade";
    public static final Logger log = LoggerFactory.getLogger(name);
    public static EconomyService economyService;

    private static PixelUpgrade instance;
    public static PixelUpgrade getInstance()
    {   return instance;    }

    @Listener
    public void shareInstance(GameConstructionEvent event)
    {   instance = this;    }

    public String path = "config" + FileSystems.getDefault().getSeparator() + "PixelUpgrade";

    public Path cmdCheckEggPath = Paths.get(path, "CheckEgg.conf");
    public Path cmdDittoFusionPath = Paths.get(path, "DittoFusion.conf");
    public Path cmdFixEVsPath = Paths.get(path, "FixEVs.conf");
    public Path cmdForceHatchPath = Paths.get(path, "ForceHatch.conf");
    public Path cmdForceStatsPath = Paths.get(path, "ForceStats.conf");
    public Path cmdGetStatsPath = Paths.get(path, "GetStats.conf");
    public Path cmdPixelUpgradeInfoPath = Paths.get(path, "PixelUpgradeInfo.conf");
    public Path cmdResetEVsPath = Paths.get(path, "ResetEVs.conf");
    public Path cmdUpgradePath = Paths.get(path, "Upgrade.conf");
    //public Path cmdWeaknessPath = Paths.get(path, "Weakness.conf");

    public ConfigurationLoader<CommentedConfigurationNode> cmdCheckEggLoader = HoconConfigurationLoader.builder().setPath(cmdCheckEggPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdDittoFusionLoader = HoconConfigurationLoader.builder().setPath(cmdDittoFusionPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdFixEVsLoader = HoconConfigurationLoader.builder().setPath(cmdFixEVsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdForceHatchLoader = HoconConfigurationLoader.builder().setPath(cmdForceHatchPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdForceStatsLoader = HoconConfigurationLoader.builder().setPath(cmdForceStatsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdGetStatsLoader = HoconConfigurationLoader.builder().setPath(cmdGetStatsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdPixelUpgradeInfoLoader = HoconConfigurationLoader.builder().setPath(cmdPixelUpgradeInfoPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdResetEVsLoader = HoconConfigurationLoader.builder().setPath(cmdResetEVsPath).build();
    public ConfigurationLoader<CommentedConfigurationNode> cmdUpgradeLoader = HoconConfigurationLoader.builder().setPath(cmdUpgradePath).build();
    //public ConfigurationLoader<CommentedConfigurationNode> cmdWeaknessLoader = HoconConfigurationLoader.builder().setPath(cmdWeaknessPath).build();

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event)
    {
        if (event.getService().equals(EconomyService.class))
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
    }

    private CommandSpec checkegg = CommandSpec.builder()
            .description(Text.of("Checks the contents of an egg. Can be set to vague or explicit."))
            .permission("pixelupgrade.command.checkegg")
            .executor(new CheckEgg())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))

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

    private CommandSpec getstats = CommandSpec.builder()
            .description(Text.of("Shows a comprehensive list of Pok\u00E9mon stats, such as EVs/IVs/natures."))
            //.permission("pixelupgrade.command.getstats")
            .executor(new GetStats())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))

            .build();

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

    private CommandSpec resetevs = CommandSpec.builder()
            .description(Text.of("Completely wipes a local Pok\u00E9mon's EVs."))
            .permission("pixelupgrade.command.resetevs")
            .executor(new ResetEVs())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))

            .build();

    private CommandSpec upgrade = CommandSpec.builder()
            .description(Text.of("Enables upgrading of Pok\u00E9mon IVs, for economy balance."))
            .permission("pixelupgrade.command.upgradeivs")
            .executor(new Upgrade())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("quantity"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))

            .build();

    private CommandSpec weakness = CommandSpec.builder()
            .description(Text.of("Shows you the weaknesses of the Pok\u00E9mon you're fighting."))
            .permission("pixelupgrade.command.weakness")
            .executor(new Weakness())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))

            .build();

    @Listener
    public void onPreInitializationEvent(GameInitializationEvent event)
    {
        Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck", "eggsee");
        Sponge.getCommandManager().register(this, dittofusion, "fuse", "dittofuse", "dittofusion", "fuseditto", "fusedittos", "amalgamate"); // There you go, Xen. /amalgamate is now a thing.
        Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev");
        Sponge.getCommandManager().register(this, forcehatch, "forcehatch");
        Sponge.getCommandManager().register(this, forcestats, "forcestats", "forcestat", "forceivs", "forceiv", "forceevs", "forceev");
        Sponge.getCommandManager().register(this, getstats, "getstats", "getstat", "gs");
        Sponge.getCommandManager().register(this, pixelupgradeinfo, "pixelupgrade", "pu", "pixelupgradeinfo");
        Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev");
        Sponge.getCommandManager().register(this, upgrade, "upgrade", "upgradeiv", "upgradeivs");
        Sponge.getCommandManager().register(this, weakness, "weakness", "weaknesses");

        CheckEggConfig.getInstance().loadOrCreateConfig(cmdCheckEggPath, cmdCheckEggLoader);
        DittoFusionConfig.getInstance().loadOrCreateConfig(cmdDittoFusionPath, cmdDittoFusionLoader);
        FixEVsConfig.getInstance().loadOrCreateConfig(cmdFixEVsPath, cmdFixEVsLoader);
        ForceStatsConfig.getInstance().loadOrCreateConfig(cmdForceStatsPath, cmdForceStatsLoader);
        ForceHatchConfig.getInstance().loadOrCreateConfig(cmdForceHatchPath, cmdForceHatchLoader);
        GetStatsConfig.getInstance().loadOrCreateConfig(cmdGetStatsPath, cmdGetStatsLoader);
        PixelUpgradeInfoConfig.getInstance().loadOrCreateConfig(cmdPixelUpgradeInfoPath, cmdPixelUpgradeInfoLoader);
        ResetEVsConfig.getInstance().loadOrCreateConfig(cmdResetEVsPath, cmdResetEVsLoader);
        //WeaknessConfig.getInstance().loadOrCreateConfig(cmdWeaknessPath, cmdWeaknessLoader);
        UpgradeConfig.getInstance().loadOrCreateConfig(cmdUpgradePath, cmdUpgradeLoader);

        log.info("\u00A7aCommands registered!");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    {
        log.info("\u00A7bAll systems nominal.");
    }
}