// The one and only. Accept no imitations.
package rs.expand.evenmorepixelmoncommands.commands.subcommands;

// Remote imports.
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.evenmorepixelmoncommands.utilities.ConfigMethods;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printUnformattedMessage;

// Note: printUnformattedMessage is a static import for a method from PrintingMethods, for convenience.
public class ReloadConfigs implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Start printing with a header.
        printUnformattedMessage("========================= P I X E L U P G R A D E =========================");
        if (src instanceof Player)
        {
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
            printUnformattedMessage("--> §aPixelUpgrade config reload called by player §2" + src.getName() + "§a.");
        }

        // Check to see if we have a config directory. If not, create one.
        ConfigMethods.checkConfigDir();

        // Notify.
        printUnformattedMessage("--> §aReloading all configuration files...");

        // Start a config reload. The returned bool tells us what happened.
        final boolean loadedConfigsCorrectly = ConfigMethods.tryLoadConfigs();
        if (loadedConfigsCorrectly)
        {
            // Print the fancy list of loaded commands and their aliases again.
            ConfigMethods.printCommandsAndAliases();
            printUnformattedMessage("--> §aRe-registering commands and known aliases with Sponge...");

            // Re-register mappings with Sponge. This will update aliases and free up any old commands.
            final Game game = Sponge.getGame();
            final PluginContainer puContainer = Sponge.getPluginManager().getPlugin("evenmorepixelmoncommands").orElse(null);

            if (puContainer != null)
            {
                game.getCommandManager().getOwnedBy(puContainer).forEach(game.getCommandManager()::removeMapping);
                ConfigMethods.registerCommands();

                printUnformattedMessage("--> §aAll done!");
                src.sendMessage(Text.of("§7-----------------------------------------------------"));
                src.sendMessage(Text.of("§3PU Reload: §bReloaded the provided config(s)!"));
                src.sendMessage(Text.of("§3PU Reload: §bPlease check the console for any errors."));
                src.sendMessage(Text.of("§7-----------------------------------------------------"));
            }
            else
            {
                printBasicError("    Plugin container is null! Please report this. Stuff will break.");
                if (src instanceof Player)
                    src.sendMessage(Text.of("§4Error: §cPlugin container could not be found! Please report this."));
            }
        }
        else
        {
            printBasicError("    Something went wrong while loading configs. Please check for any errors.");
            if (src instanceof Player)
                src.sendMessage(Text.of("§4Error: §cConfig loading failed. Check console for any errors."));
        }

        // Finish printing with a footer.
        if (src instanceof Player)
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        printUnformattedMessage("===========================================================================");

        return CommandResult.success();
    }
}