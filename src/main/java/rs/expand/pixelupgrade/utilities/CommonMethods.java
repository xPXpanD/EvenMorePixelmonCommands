package rs.expand.pixelupgrade.utilities;

import java.util.ArrayList;
import java.util.Collections;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;

public class CommonMethods
{
    // Depending on the global debug level, decide whether or not to print debug messages here.
    public static void doPrint(String callSource, int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                System.out.println("§4" + callSource + " // critical: §c");
            else if (debugNum == 1)
                System.out.println("§3" + callSource + " // notice: §b" + inputString);
            else
                System.out.println("§2" + callSource + " // debug: §a" + inputString);
        }
    }

    // Called when somebody runs a command from the wrong place.
    public static void showConsoleError(String formattedCommand)
    { System.out.println("§4" + formattedCommand + "§c cannot run from the console or command blocks."); }

    // Called when we read a mangled config while showing the command listing.
    public static void printMalformedConfigError(String formattedCommand)
    { System.out.println("§3PUInfo // notice: §bMalformed config on §3" + formattedCommand + "§b, hiding from list."); }

    // If we can't read a config parameter, format and throw this error.
    public static void printNodeError(String callSource, ArrayList<String> nodes, int messageNum)
    {
        Collections.singletonList(nodes).forEach(node -> doPrint(callSource, 0,
                "Could not read node \"§4" + node + "§c\"."));

        switch (messageNum)
        {
            case 0: // Reading main config.
                doPrint(callSource, 0, "The main config contains invalid variables. Exiting.");
                break;
            case 1: // Reading command config.
                doPrint(callSource, 0, "This command's config could not be parsed. Exiting.");
                break;
        }

        doPrint(callSource, 0, "Check the related config, and when fixed use §4/pureload§c.");
    }

    // Use this one if we have to check multiple configs, then end with a printNodeError.
    public static void printPartialNodeError(String callSource, String targetCommand, ArrayList<String> nodes)
    {
        Collections.singletonList(nodes).forEach(node -> doPrint(callSource, 0,
                "Could not read remote node \"§4" + node + "§c\" for command \"§4/" + targetCommand + "§c\"."));
    }
}
