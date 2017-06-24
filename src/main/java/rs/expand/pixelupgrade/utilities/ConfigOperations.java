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
                PixelUpgrade.log.info("\u00A7eNo \"\u00A76/" + command.toLowerCase() + "\u00A7e\" configuration file found, creating...");
                break;
            case 2:
                PixelUpgrade.log.info("\u00A74" + command + "\u00A7c: Error during initial setup of config! Please report this.");
                PixelUpgrade.log.info("\u00A74" + command + "\u00A7c: Add any useful info you may have (operating system?). Stack trace:");
                break;
            case 3:
                PixelUpgrade.log.info("\u00A7aLoaded existing config for command \"\u00A72/" + command.toLowerCase() +
                        "\u00A7a\", alias \"\u00A72/" + alias + "\u00A7a\"");
                break;
            case 4:
                PixelUpgrade.log.info("\u00A7c" + command + ": Could not read variable \u00A74\"commandAlias\"\u00A7c, setting defaults.");
                PixelUpgrade.log.info("\u00A7c" + command + ": Check this command's config, or wipe it and \u00A74/pureload\u00A7c.");
                break;
            case 5:
                PixelUpgrade.log.info("\u00A7cError during config loading for command \"\u00A74/" + command.toLowerCase() + "\u00A7c\"!");
                PixelUpgrade.log.info("\u00A7cPlease make sure this config is formatted correctly. Stack trace:");
                break;

            // Special cases! Only used in the PixelUpgrade Info command.
            case 11:
                PixelUpgrade.log.info("\u00A7eNo \"\u00A76/pixelupgrade\u00A7e\" (command list) configuration file found, creating...");
                break;
            case 33:
                PixelUpgrade.log.info("\u00A7aLoaded existing config for command \"\u00A72/pixelupgrade\u00A7a\" (command list), alias \"\u00A72/"
                    + alias + "\u00A7a\"");
                break;
            case 55:
                PixelUpgrade.log.info("\u00A7cError during config loading for command \"\u00A74/pixelupgrade\u00A7c\" (command list)!");
                PixelUpgrade.log.info("\u00A7cPlease make sure this config is formatted correctly. Stack trace:");
                break;
        }
    }
}
