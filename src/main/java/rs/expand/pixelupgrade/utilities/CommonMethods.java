package rs.expand.pixelupgrade.utilities;

// Remote imports.
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Optional;

// Local imports.
import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;

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
    public static void doPrint(String callSource, boolean bypassCheck, int debugNum, String inputString)
    {
        if (bypassCheck || debugNum <= debugLevel)
        {
            switch (debugNum)
            {
                case 0:
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§c[§4" + callSource + " §c:: §4ERROR§c] " + inputString)));
                    //break;
                case 1:
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§e[§6" + callSource + " §e:: §6INFO§e] " + inputString)));
                    //break;
                default:
                    getConsole().ifPresent(console ->
                            console.sendMessage(Text.of("§a[§2" + callSource + " §a:: §2DEBUG§a] " + inputString)));
                    break;
            }
        }
    }

    // If we can't read a config parameter, format and throw this error.
    public static void printNodeError(String callSource, ArrayList<String> nodes, int errorType)
    {
        for (String node : nodes)
            doPrint(callSource, true, 0, "Could not read node \"§4" + node + "§c\".");

        switch (errorType)
        {
            case 0: // Reading main config.
                doPrint(callSource, true, 0,
                        "The main config contains invalid variables. Exiting.");
                break;
            case 1: // Reading command config.
                doPrint(callSource, true, 0,
                        "This command's config could not be parsed. Exiting.");
                break;
        }

        doPrint(callSource, true, 0,
                "Check the related config, and when fixed use §4/pureload§c.");
    }

    // Use this one if we have to check multiple configs, then end with a printNodeError.
    public static void printPartialNodeError(String callSource, String targetCommand, ArrayList<String> nodes)
    {
        for (String node : nodes)
            doPrint(callSource, true, 0,
                    "Could not read node \"§4" + node + "§c\" for command \"§4/" + targetCommand + "§c\".");
    }
}
