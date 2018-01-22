package rs.expand.pixelupgrade.utilities;

// Remote imports.
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;
import java.util.ArrayList;
import java.util.Optional;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;

// A collection of methods that are commonly used. One changed word or color here, and half the mod changes. Sweet.
public class CommonMethods
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
    public static void printDebugMessage(String callSource, int debugNum, String inputString)
    {
        if (debugNum <= PixelUpgrade.debugVerbosityMode)
        {
            switch (debugNum)
            {
                case 0:
                {
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§c[§4" + callSource + " §c:: §4ERROR§c] " + inputString)));
                    break;
                }
                case 1:
                {
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§b[§3" + callSource + " §b:: §3INFO§b] " + inputString)));
                    break;
                }
                default:
                {
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§a[§2" + callSource + " §a:: §2DEBUG§a] " + inputString)));
                    break;
                }
            }
        }
    }

    // If we need to print something without any major formatting, do it here. Good for console lists.
    public static void printBasicMessage(String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§f" + inputString)));
    }

    // If we can't read a main config parameter, format and throw this error.
    public static void printMainNodeError(String callSource, ArrayList<String> nodes)
    {
        for (String node : nodes)
        { printDebugMessage(callSource, 0, "Could not read node \"§4" + node + "§c\"."); }

        printDebugMessage(callSource, 0, "The main config contains invalid variables. Exiting.");
        printDebugMessage(callSource, 0, "Check the related config, and when fixed use §4/pureload§c.");
    }

    // And here's one for the per-command configs.
    public static void printCommandNodeError(String callSource, ArrayList<String> nodes)
    {
        for (String node : nodes)
        { printDebugMessage(callSource, 0, "Could not read node \"§4" + node + "§c\"."); }

        printDebugMessage(callSource, 0, "This command's config could not be parsed. Exiting.");
        printDebugMessage(callSource, 0, "Check the related config, and when fixed use §4/pureload§c.");
    }

    // Use this one if we have to check multiple configs, then end with a separate message.
    public static void printPartialNodeError(String callSource, String targetCommand, ArrayList<String> nodes)
    {
        for (String node : nodes)
        {
            printDebugMessage(callSource, 0, "Could not read node \"§4" + node +
                    "§c\" for command \"§4/" + targetCommand.toLowerCase() + "§c\".");
        }
    }

    // If a command has a cost, we'll want to get explicit confirmation. This lets players know how that works.
    // Used so commonly that we might as well set it up here.
    public static void checkAndAddFooter(int cost, CommandSource src)
    {
        if (cost > 0)
        {
            src.sendMessage(Text.of(""));
            src.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
            src.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coins."));
            src.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }
}
