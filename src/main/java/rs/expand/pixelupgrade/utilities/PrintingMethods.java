package rs.expand.pixelupgrade.utilities;

// Remote imports.
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import static rs.expand.pixelupgrade.PixelUpgrade.economyEnabled;

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

    // Depending on the global debug level, decide whether or not to print debug messages here.
    public static void printDebugMessage(final String callSource, final int debugNum, final String inputString)
    {
        if (PixelUpgrade.debugVerbosityMode != null && debugNum <= PixelUpgrade.debugVerbosityMode)
        {
            switch (debugNum)
            {
                case 0:
                {
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§4" + callSource + " error §f// §c" + inputString)));
                    break;
                }
                case 1:
                {
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§3" + callSource + " info §f// §b" + inputString)));
                    break;
                }
                default:
                {
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§2" + callSource + " debug §f// §a" + inputString)));
                    break;
                }
            }
        }
        else if (debugNum == 1337) // Used for test logging, should go unused for release builds.
        {
            getConsole().ifPresent(console ->
                    console.sendMessage(Text.of("§6" + callSource + " TEST §f// §e" + inputString)));
        }
    }

    // If we need to print something without any major formatting, do it here. Good for console lists.
    public static void printBasicMessage(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§f" + inputString)));
    }

    // If we can't read a main config parameter, format and throw this error.
    public static void printMainNodeError(final String callSource, final List<String> nodes)
    {
        for (final String node : nodes)
            printDebugMessage(callSource, 0, "Could not read remote node \"§4" + node + "§c\".");

        printDebugMessage(callSource, 0, "The main config contains invalid variables. Exiting.");
        printDebugMessage(callSource, 0, "Check the related config, and when fixed use §4/pureload§c.");
    }

    // And here's one for the per-command configs.
    public static void printCommandNodeError(final String callSource, final List<String> nodes)
    {
        for (final String node : nodes)
            printDebugMessage(callSource, 0, "Could not read node \"§4" + node + "§c\".");

        printDebugMessage(callSource, 0, "This command's config could not be parsed. Exiting.");
        printDebugMessage(callSource, 0, "Check the related config, and when fixed use §4/pureload§c.");
    }

    // Use this one if we have to check multiple configs, then end with a separate message.
    public static void printPartialNodeError(final String callSource, final String targetCommand, final List<String> nodes)
    {
        for (final String node : nodes)
        {
            printDebugMessage(callSource, 0, "Could not read node \"§4" + node +
                    "§c\" for command \"§4/" + targetCommand.toLowerCase() + "§c\".");
        }
    }

    // Adds a footer based on input and matching intent. Used so commonly it might as well be here.
    public static void checkAndAddFooter(final boolean requireConfirmation, final long cost, final CommandSource src)
    {
        if (requireConfirmation || economyEnabled && cost > 0)
        {
            src.sendMessage(Text.of(""));
            src.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
        }

        if (economyEnabled && cost > 0)
        {
            if (cost == 1)
                src.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coin."));
            else
                src.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coins."));
        }

        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    // Takes a config String, and changes any ampersands to section symbols, which we can use internally.
    public static String parseRemoteString(final String input)
    {
        // Set up a list of valid formatting codes.
        final List<Character> validFormattingCharacters = Arrays.asList
        (
                // Color numbers.
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                // Color letters, lower and upper case.
                'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F',
                // Other formatting codes.
                'k', 'l', 'm', 'n', 'o', 'r'
        );

        // Start replacing our ampersands.
        final StringBuilder mutableInput = new StringBuilder(input);
        for (int i = 0; i < mutableInput.length(); i++)
        {
            // Is the character that's currently being checked an ampersand?
            if (mutableInput.charAt(i) == '&')
            {
                // Make sure the iterator is still inside of the input String's length. Let's not check out of bounds.
                if ((i + 1) < mutableInput.length())
                {
                    // Look ahead: Does the next character contain a known formatting character? Replace the ampersand!
                    if (validFormattingCharacters.contains(mutableInput.charAt(i + 1)))
                        mutableInput.setCharAt(i, '§');
                }
            }
        }

        // Replace our old input String with the one that we fixed formatting on.
        return mutableInput.toString();
    }

    // TODO: Add support for multiple input sets.
    // Takes a config String, and replaces a single placeholder with the proper replacement as many times as needed.
    public static String replacePlaceholder(final String input, final String placeholder, final String replacement)
    {
        // If our input has a placeholder inside, replace it with the provided replacement String. Case-insensitive.
        if (input.toLowerCase().contains(placeholder))
            return input.replaceAll("(?i)" + placeholder, replacement);
        else
            return input;
    }
}
