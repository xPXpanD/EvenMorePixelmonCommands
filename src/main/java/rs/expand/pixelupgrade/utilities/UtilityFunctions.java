package rs.expand.pixelupgrade.utilities;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilityFunctions
{
    private static UtilityFunctions instance = new UtilityFunctions();
    public static UtilityFunctions getInstance()
    {   return instance;    }

    // Set up a nice compact private logger specifically for showing command loading.
    private static final String pName = "PU";
    private static final Logger pLog = LoggerFactory.getLogger(pName);

    public static void printMessages(int printNumber, String command)
    {
        switch (printNumber)
        {
            // Typical config loader class messages/errors.
            case 1:
                pLog.info("§eNo \"§6/" + command + "§e\" configuration file found, creating...");
                break;
            case 2:
                pLog.info("§cInitial \"§4/" + command + "§c\" config setup failed! Please report this.");
                pLog.info("§cAdd any useful info you may have (operating system?). Stack trace:");
                break;
            case 3:
                pLog.info("§cTried loading \"§4/" + command + "§c\" config but ran into an unknown error!");
                pLog.info("§cPlease make sure this config is formatted correctly. Stack trace:");
                break;
        }
    }

    // Used only for /pixelupgradeinfo (AKA /pixelupgrade or /pu), the command listing.
    public static void printInfoMessages(int printNumber)
    {
        switch (printNumber)
        {
            case 1:
                pLog.info("§eNo \"§6/pixelupgrade§e\" (command list) configuration file found, creating...");
                break;
            case 2:
                pLog.info("§cInitial \"§4/pixelupgrade§c\" (list) config setup failed! Please report this.");
                pLog.info("§cAdd any useful info you may have (operating system?). Stack trace:");
                break;
            case 3:
                pLog.info("§cTried loading \"§4/pixelupgrade§c\" (list) but ran into an unknown error!");
                pLog.info("§cPlease make sure this config is formatted correctly. Stack trace:");
                break;
        }
    }

    public static String checkAlias(String alias, String command)
    {
        if (!Objects.equals(alias, null))
            return alias;
        else
        {
            pLog.info("§cError on \"§4/" + command + "§c\", variable \"§4commandAlias§c\"! Check/regen this config!");
            return null;
        }
    }
}
