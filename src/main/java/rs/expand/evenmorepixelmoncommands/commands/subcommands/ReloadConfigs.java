// Reloads EMPC's configs, aliases included.
package rs.expand.evenmorepixelmoncommands.commands.subcommands;

// Remote imports.
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.evenmorepixelmoncommands.utilities.ConfigMethods;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printUnformattedMessage;

// Note: printUnformattedMessage is a static import for a method from PrintingMethods, for convenience.
public class ReloadConfigs implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Print some identifying stuff to console if a player started the command.
        if (src instanceof Player)
            printUnformattedMessage("§4EMPC §f// §dPlayer §5" + src.getName() + "§d reloaded the EMPC configs!");

        // Load up configs and figure out the hub command alias. Start printing. Methods may insert errors as they go.
        printUnformattedMessage("");
        printUnformattedMessage("======== E V E N  M O R E  P I X E L M O N  C O M M A N D S ========");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        final boolean loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to doing more stuff.
        if (loadedCorrectly)
        {
            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            printUnformattedMessage("--> §aRe-registering commands and known aliases with Sponge...");

            // Print super fancy command + alias overview to console. Even uses colors to show errors!
            ConfigMethods.printCommandsAndAliases();

            // Finish up.
            if (ConfigMethods.registerCommands())
                printUnformattedMessage("--> §aReload completed. All systems nominal.");
        }
        else
            printUnformattedMessage("--> §cLoad aborted due to critical errors.");

        // We're done, one way or another. Add a footer, and a space to avoid clutter with other marginal'd mods.
        printUnformattedMessage("====================================================================");
        printUnformattedMessage("");

        // Also notify our player, if an actual player.
        if (src instanceof Player)
        {
            src.sendMessage(Text.of("§7-----------------------------------------------------"));
            src.sendMessage(Text.of("§3EMPC Reload: §bReloaded all EMPC configs!"));
            src.sendMessage(Text.of("§3EMPC Reload: §bPlease check the console for any errors."));
            src.sendMessage(Text.of("§7-----------------------------------------------------"));
        }
        else // ...or do this if console.
        {
            src.sendMessage(Text.of("§bReloaded the EMPC configs!"));
            src.sendMessage(Text.of("§bPlease check the console for any errors."));
        }

        return CommandResult.success();
    }
}