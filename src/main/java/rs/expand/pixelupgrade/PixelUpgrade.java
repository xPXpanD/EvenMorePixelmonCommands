/* Welcome! Here's the obligatory short history of how this came to be.
 * I always do these, it's pretty fun for me to see how stuff all came together.
 *
 * 0.01: First version. Didn't do much of anything.
 * 0.02: Switched to Gradle, actually figured out how to do stuff. Still pretty dead, though.
 * 0.03: Progress! Reading IVs!
 * 0.1: Figured out how to write IVs! Plugin looks viable, woo!
 * 0.2: Figured out how to resize, also!
 * 0.3: Complete rewrite in preparation of proper commands! Changed /getivs to /upgrade stats.
 * 0.4: Formatting fixes. Looks pretty spiffy, now. Also, removed "other person" option on /upgrade stats. May come back to it later.
 * 0.41: /upgrade stats is now /getstats. Added a bunch of failsafes.
 * 0.42: Added /upgrade fixevs, and made /getstats check for wasted EVs >252.
 * 0.5: Added /upgrade force! First work towards making /upgrade ivs a thing.
 * 0.5.1: Rewrote /getstats, actually manages to check for null Pok\u00E9mon properly now.
 * 0.5.2: Rewrote /upgrade force, too. Added sane caps.
 * 0.5.3: Added /upgrade resetevs. Finally figured out how to add optional parameters.
 * 0.5.4: Capped /upgrade force a bunch more, added a bypass flag. Made the main /upgrade info command.
 * 0.5.5: Sanity checking, part 3. Re-added other person support on /getstats, removed a ton of useless checks, migrated to IntelliJ IDEA.
 * 0.5.6: Flipped parameters so that people can now run either /getstats SLOT or /getstats PLAYER SLOT, instead of the awkward /getstats SLOT PLAYER. Cleanup.
 * 0.6: Started work on /upgrade ivs. First launch version! Private, for now.
 * 0.7: /upgrade IVs logic pretty much done, still have to make it take cash and set stats. Got a nice exponential scale going.
 * 0.8: /upgrade IVs done!!
 * 0.8.1: Added a remote listing of all party Pokémon to /getstats, if the slot asked for is empty.
 * 0.8.2: Added /forcehatch.
 * 0.9: Full internal rewrite of the way command arguments are handled. No more issues with certain characters causing massive console errors!
 * 1.0: Everything fixed up. Second launch version! Started private, became public after we decided to shut down server.
 * 1.1: Made /dittofusion. Yes, Ditto Fusion. The most kick-ass command.
 * 1.2: Added caps to /dittofusion, and made it triple cash amounts when using a pre-upgraded sacrifice.
 * 1.3: Added a /checkegg. Config stuff and explicit stat showing toggle coming soon, hopefully.
 *
 * Enjoy the plugin!
 */

/* Useful stuff (personal notes):
 * Use [ \t]+$ as a regex to remove trailing spaces in Eclipse.
 * Sponge.getGame().getServer().getWorlds()
 */

package rs.expand.pixelupgrade;

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

import rs.expand.pixelupgrade.commands.GetStats;
import rs.expand.pixelupgrade.commands.DittoFusion;
import rs.expand.pixelupgrade.commands.FixEVs;
import rs.expand.pixelupgrade.commands.AdminForce;
import rs.expand.pixelupgrade.commands.UpgradeIVs;
import rs.expand.pixelupgrade.commands.ResetEVs;
import rs.expand.pixelupgrade.commands.CheckEgg;
import rs.expand.pixelupgrade.commands.Upgrade;
import rs.expand.pixelupgrade.commands.ForceHatch;

//TODO: Add fixlevel.
//TODO: Cool ideas: Pixelpay. New starter box.
//TODO: Maybe make a /showstats or /printstats.
//TODO: Make an /eggsee with economy tie-in?
//TODO: Consider making a shiny upgrade command and a gender swapper.
//TODO: Make a Pokémon transfer command.
//TODO: Check if setting stuff to player entities works, too!
//TODO: Ditto fusion.
//TODO: Upgrade token support.
//TODO: Configuration.
//TODO: Maybe make a heal command with a hour-long cooldown.
//TODO: Make a /pokesell, maybe one that sells based on ball worth.
//TODO: Make an hidden ability switcher, maybe.
//TODO: Make an admin toggle command for stuff like shinyness.

@Plugin(id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "1.2",
        dependencies = @Dependency(id = "pixelmon"),
        authors = "XpanD", // + a bunch of help from Xenoyia and breakthrough snippets from NickImpact (NBT editing) and Proxying (writing to entities in a way that saves when the entity is re-made)!
        description = "Change just about everything Pok\u00E9mon-related, and pay people with Pok\u00E9dollars!")

public class PixelUpgrade
{
    private static final String name = "PixelUpgrade";
    public static final Logger log = LoggerFactory.getLogger(name);
    public static EconomyService economyService;

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event)
    {
        if (event.getService().equals(EconomyService.class))
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
    }

    //TODO: Check public static final String PC_RAVE = "rave";
    //TODO: Check public static final String PIXEL_DOLLARS = "pixelDollars";

    private CommandSpec fixevs = CommandSpec.builder()
            .description(Text.of("Lowers EVs that are above 252, avoiding wasted points."))
            .permission("pixelupgrade.commands.fixevs")
            .executor(new FixEVs())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))

            .build();

    private CommandSpec dittofusion = CommandSpec.builder()
            .description(Text.of("Fuse Dittos together for economy balance, improving their stats!"))
            .permission("pixelupgrade.commands.dittofusion")
            .executor(new DittoFusion())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("sacrifice slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))

            .build();

    private CommandSpec getstats = CommandSpec.builder()
            .description(Text.of("Shows a comprehensive list of Pok\u00E9mon stats, such as EVs/IVs/natures."))
            .permission("pixelupgrade.commands.getstats")
            .executor(new GetStats())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))

            .build();

    private CommandSpec forcehatch = CommandSpec.builder()
            .description(Text.of("Forcefully hatches a remote or local Pok\u00E9mon egg."))
            .permission("pixelupgrade.commands.admin.forcehatch")
            .executor(new ForceHatch())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))

            .build();

    /*CommandSpec showstats = CommandSpec.builder()
            .description(Text.of("Announces a comprehensive list of a Pok\u00E9mon stats, with a cooldown."))
            .permission("pixelupgrade.commands.showstats")
            .executor(new ShowStats())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))))

            .build();*/

    private CommandSpec checkegg = CommandSpec.builder()
            .description(Text.of("Checks the contents of an egg. Can be set to vague or explicit."))
            .permission("pixelupgrade.commands.checkegg")
            .executor(new CheckEgg())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))

            .build();

    private CommandSpec resetevs = CommandSpec.builder()
            .description(Text.of("Completely wipes a local Pok\u00E9mon's EVs."))
            .permission("pixelupgrade.commands.resetevs")
            .executor(new ResetEVs())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))

            .build();

    private CommandSpec upgradeivs = CommandSpec.builder()
            .description(Text.of("Enables upgrading of Pok\u00E9mon IVs, for economy balance."))
            .permission("pixelupgrade.commands.upgradeivs")
            .executor(new UpgradeIVs())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("quantity"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))

            .build();

    private CommandSpec adminforce = CommandSpec.builder()
            .description(Text.of("Allows free setting of IVs and EVs on any Pok\u00E9mon."))
            .permission("pixelupgrade.commands.admin.force")
            .executor(new AdminForce())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value"))),
                    GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))

            .build();

    /* CommandSpec resize = CommandSpec.builder()
            .description(Text.of("Enables changing of Pok\u00E9mon size."))
            .permission("pixelupgrade.commands.resize")
            .executor(new Resize())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("size"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("confirm"))))

            .build(); */

    private CommandSpec upgrade = CommandSpec.builder()
            .description(Text.of("Shows the PixelUpgrade subcommand listing."))
            .executor(new Upgrade())

            .child(upgradeivs, "ivs", "iv", "setivs", "setiv", "upgradeiv", "upgradeivs")
            .child(adminforce, "force", "forcestats", "forceivs", "forceiv", "forceevs", "adminforce", "forceev")
            // .child(resize, "resize", "changesize", "size")

            .build();

    @Listener
    public void onPreInitializationEvent(GameInitializationEvent event)
    {
        Sponge.getCommandManager().register(this, upgrade, "upgrade");
        Sponge.getCommandManager().register(this, getstats, "getstats", "getstat", "gs");
        Sponge.getCommandManager().register(this, forcehatch, "forcehatch");
        // Sponge.getCommandManager().register(this, showstats, "showstats", "showstat", "printstats", "printstat");
        Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev");
        Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev");
        Sponge.getCommandManager().register(this, dittofusion, "fuse", "dittofuse", "dittofusion", "fuseditto", "fusedittos", "amalgamate");
        Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck", "eggsee");

        log.info("\u00A7aCommands registered!");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    {
        log.info("\u00A7bAll systems nominal.");
    }
}