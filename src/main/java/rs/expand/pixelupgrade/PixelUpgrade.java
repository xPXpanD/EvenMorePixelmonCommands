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
 * 0.6: Started work on /upgrade ivs.
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
// import rs.expand.pixelupgrade.commands.ShowStats;
import rs.expand.pixelupgrade.commands.FixEVs;
import rs.expand.pixelupgrade.commands.Force;
import rs.expand.pixelupgrade.commands.SetIVs;
import rs.expand.pixelupgrade.commands.ResetEVs;
import rs.expand.pixelupgrade.commands.Resize;
import rs.expand.pixelupgrade.commands.Upgrade;

//TODO: add fixlevel
//Cool ideas: Pixelpay. New starter box.
//TODO: Maybe make a /showstats or /printstats.
//TODO: Remake command helper so it follows the /gts help format.
//TODO: Make an /eggsee with economy tie-in?
//TODO: Consider making a shiny upgrade command and a gender swapper.
//TODO: Make a Pokémon transfer command.

@Plugin(id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "0.6",
        dependencies = @Dependency(id = "pixelmon"),
        authors = "XpanD", // Written by XpanD, with a bunch of help from Xenoyia and a breakthrough snippet from NickImpact!
        description = "Change just about everything Pok\u00E9mon-related, and pay people with Pok\u00E9dollars!")

public class PixelUpgrade
{
    public static final String name = "PixelUpgrade";
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

    CommandSpec fixevs = CommandSpec.builder()
            .description(Text.of("Lowers EVs that are above 252, avoiding wasted points."))
            .permission("pixelupgrade.commands.fixevs")
            .executor(new FixEVs())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))))

            .build();

    CommandSpec getstats = CommandSpec.builder()
            .description(Text.of("Shows a comprehensive list of Pok\u00E9mon stats, such as EVs/IVs/natures."))
            .permission("pixelupgrade.commands.getstats")
            .executor(new GetStats())

            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.player(Text.of("target"))),
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))))

            .build();

    /*CommandSpec showstats = CommandSpec.builder()
            .description(Text.of("Shows a comprehensive list of Pok\u00E9mon stats, such as EVs/IVs/natures."))
            .permission("pixelupgrade.commands.showstats")
            .executor(new ShowStats())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))))

            .build();*/

    CommandSpec resetevs = CommandSpec.builder()
            .description(Text.of("Completely wipes a local Pok\u00E9mon's EVs."))
            .permission("pixelupgrade.commands.resetevs")
            .executor(new ResetEVs())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))),
                    GenericArguments.optional(GenericArguments.string(Text.of("confirm"))))

            .build();

    CommandSpec setivs = CommandSpec.builder()
            .description(Text.of("Enables upgrading of Pok\u00E9mon IVs."))
            .permission("pixelupgrade.commands.setivs")
            .executor(new SetIVs())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("quantity"))),
                    GenericArguments.optional(GenericArguments.string(Text.of("confirm"))))

            .build();

    CommandSpec force = CommandSpec.builder()
            .description(Text.of("Allows free setting of IVs and EVs on any Pok\u00E9mon."))
            .permission("pixelupgrade.commands.admin.force")
            .executor(new Force())

            .arguments(
                    GenericArguments.optional(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optional(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optional(GenericArguments.string(Text.of("value"))),
                    GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))

            .build();

    CommandSpec resize = CommandSpec.builder()
            .description(Text.of("Enables changing of Pok\u00E9mon size."))
            .permission("pixelupgrade.commands.resize")
            .executor(new Resize())

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("slot"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("size"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("confirm"))))

            .build();

    CommandSpec upgrade = CommandSpec.builder()
            .description(Text.of("Shows the PixelUpgrade subcommand listing."))
            .executor(new Upgrade())

            .child(setivs, "ivs", "iv", "setivs", "setiv")
            .child(force, "force", "forcestats", "forceivs", "adminivs", "forceiv", "adminiv", "forceevs", "adminevs", "forceev", "adminev")
            .child(resize, "resize", "changesize", "size")

            .build();

    @Listener
    public void onPreInitializationEvent(GameInitializationEvent event)
    {
        Sponge.getCommandManager().register(this, upgrade, "upgrade");
        Sponge.getCommandManager().register(this, getstats, "getstats", "getstat", "gs");
        // Sponge.getCommandManager().register(this, showstats, "showstats", "showstat", "printstats", "printstat");
        Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev");
        Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev");

        log.info("\u00A7aPixelUpgrade: Commands registered!");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    { log.info("\u00A7bPixelUpgrade: Ready to go!"); }
}

/*switch (typeNumPrimary) // 0, 2, 5, 6, 7, 8, a, b, c, d, e, f are used -- 1, 3, 4 and 9 are free -- 3 or 9 would be most legible, 9 may be best.
{
	case 0: typeNamePrimary = "\u00A7fNormal"; break;
	case 1: typeNamePrimary = "\u00A7cFire"; break;
	case 2: typeNamePrimary = "\u00A7bWater"; break;
	case 3: typeNamePrimary = "\u00A7eElectric"; break;
	case 4: typeNamePrimary = "\u00A7aGrass"; break;
	case 5: typeNamePrimary = "\u00A7bIce"; break;
	case 6: typeNamePrimary = "\u00A7fFighting"; break;
	case 7: typeNamePrimary = "\u00A72Poison"; break;
	case 8: typeNamePrimary = "\u00A76Ground"; break;
	case 9: typeNamePrimary = "\u00A77Flying"; break;
	case 10: typeNamePrimary = "\u00A7dPsychic"; break;
	case 11: typeNamePrimary = "\u00A72Bug"; break;
	case 12: typeNamePrimary = "\u00A76Rock"; break;
	case 13: typeNamePrimary = "\u00A75Ghost"; break;
	case 14: typeNamePrimary = "\u00A75Dragon"; break;
	case 15: typeNamePrimary = "\u00A78Dark"; break;
	case 16: typeNamePrimary = "\u00A77Steel"; break;
	case 17: typeNamePrimary = "\u00A70Unknown..."; break;
	case 18: typeNamePrimary = "\u00A7dFairy"; break;
	default: typeNamePrimary = "\u00A7fN/A"; break;
}*/ // Fun fact: PRIMARY_TYPE and SECONDARY_TYPE do not seem to return any proper values. They always return 0 and -1 respectively.

/* if (!(src instanceof Player))
        	System.out.println("\u00A74Error: \u00A7cYou can't run this command from the console."); */