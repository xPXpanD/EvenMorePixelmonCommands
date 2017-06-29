package rs.expand.pixelupgrade.utilities;

import rs.expand.pixelupgrade.PixelUpgrade;

public class ConfigOperations
{
    private static ConfigOperations instance = new ConfigOperations();
    public static ConfigOperations getInstance()
    {   return instance;    }

    public static void printMessages(int printNumber, String command, String alias)
    {
        switch (printNumber)
        {
            // Typical config loader class messages/errors.
            case 1:
                PixelUpgrade.log.info("§eNo \"§6/" + command.toLowerCase() + "§e\" configuration file found, creating...");
                break;
            case 2:
                PixelUpgrade.log.info("§4" + command + "§c: Error during initial setup of config! Please report this.");
                PixelUpgrade.log.info("§4" + command + "§c: Add any useful info you may have (operating system?). Stack trace:");
                break;
            case 3:
                PixelUpgrade.log.info("§aLoaded existing config for command \"§2/" + command.toLowerCase() +
                        "§a\", alias \"§2/" + alias + "§a\"");
                break;
            case 4:
                PixelUpgrade.log.info("§c" + command + ": Could not read variable §4\"commandAlias\"§c, setting defaults.");
                PixelUpgrade.log.info("§c" + command + ": Check this command's config, or wipe it and §4/pureload§c.");
                break;
            case 5:
                PixelUpgrade.log.info("§cError during config loading for command \"§4/" + command.toLowerCase() + "§c\"!");
                PixelUpgrade.log.info("§cPlease make sure this config is formatted correctly. Stack trace:");
                break;

            // Special cases! Only used in the PixelUpgrade Info command.
            case 11:
                PixelUpgrade.log.info("§eNo \"§6/pixelupgrade§e\" (command list) configuration file found, creating...");
                break;
            case 33:
                PixelUpgrade.log.info("§aLoaded existing config for command \"§2/pixelupgrade§a\" (command list), alias \"§2/"
                    + alias + "§a\"");
                break;
            case 55:
                PixelUpgrade.log.info("§cError during config loading for command \"§4/pixelupgrade§c\" (command list)!");
                PixelUpgrade.log.info("§cPlease make sure this config is formatted correctly. Stack trace:");
                break;
        }
    }
}
