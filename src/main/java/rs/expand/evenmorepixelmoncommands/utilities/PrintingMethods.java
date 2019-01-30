package rs.expand.evenmorepixelmoncommands.utilities;

// Remote imports.
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;
import java.util.List;
import java.util.Optional;

// Local imports.
import rs.expand.evenmorepixelmoncommands.EMPC;

// A collection of methods that are commonly used. One changed word or color here, and half the mod changes. Sweet.
public class PrintingMethods
{
    // Remove the ugly prefix from console commands, so we can roll our own. Thanks for the examples, NickImpact!
    private static Optional<ConsoleSource> getConsole()
    {
        if (Sponge.isServerAvailable())
            return Optional.of(Sponge.getServer().getConsole());
        else
            return Optional.empty();
    }

    // If we need to print something without any major formatting, do it here. Good for console lists.
    public static void printUnformattedMessage(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§f" + inputString)));
    }

    // If we need to print something originating from a specific command, do it here.
    public static void printSourcedMessage(final String callSource, final String inputString)
    {
        if (EMPC.logImportantInfo == null || EMPC.logImportantInfo)
        {
            if (EMPC.logImportantInfo == null)
                printBasicError("Could not determine logging status from main config! Falling back to defaults.");

            getConsole().ifPresent(console ->
                    console.sendMessage(Text.of("§3EMPC §f// §3" + callSource + " §f// §b" + inputString)));
        }
    }

    // If we need to show a generic error with no specific source, do it here.
    public static void printBasicError(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§4EMPC §f// §4Error: §c" + inputString)));
    }

    // If we need to print an error from a specific command, this is the one we go to.
    public static void printSourcedError(final String callSource, final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§4EMPC §f// §4" + callSource + " §f// §4Error: §c" + inputString)));
    }

    // If we can't read a main config parameter, get a bit clever and show everything that went wrong.
    public static void printMainNodeError(final String callSource, final List<String> nodes)
    {
        printSourcedError(callSource, "§cErrors were found in the main config. See below:");

        for (final String node : nodes)
            printBasicError("Could not read remote node \"§4" + node + "§c\".");

        printBasicError("§cCheck the main config, and when fixed use §4/empc reload§c. Exiting.");
    }

    // If we can't read a command config parameter, get a bit clever and show everything that went wrong.
    public static void printCommandNodeError(final String callSource, final List<String> nodes)
    {
        printSourcedError(callSource, "§cErrors were found in this command's config. See below:");

        for (final String node : nodes)
            printBasicError("Could not read node \"§4" + node + "§c\".");

        printBasicError("§cCheck the mentioned config, and when fixed use §4/empc reload§c. Exiting.");
    }

    /*// Use this one if we have to check multiple configs, then end with a separate message.
    public static void printPartialNodeError(final String callSource, final String targetCommand, final List<String> nodes)
    {
        for (final String node : nodes)
        {
            printDebugMessage(callSource, 0, "Could not read node \"§4" + node +
                    "§c\" for command \"§4/" + targetCommand.toLowerCase() + "§c\".");
        }
    }*/
}
