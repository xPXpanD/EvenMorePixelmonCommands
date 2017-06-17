package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.WeaknessConfig;
import rs.expand.pixelupgrade.utilities.EnumPokemonList;

import static rs.expand.pixelupgrade.utilities.EnumPokemonList.getPokemonFromID;
import static rs.expand.pixelupgrade.utilities.EnumPokemonList.getPokemonFromName;

/*                                                *\
        HEAVILY WORK IN PROGRESS, STAY TUNED
\*                                                */

public class Weakness implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set 4 (out of range) or null on hitting an error, and let the main code block handle it from there.

    private static Integer debugLevel = 4;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!WeaknessConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = WeaknessConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            Integer commandCost = null;
            if (!WeaknessConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                commandCost = WeaknessConfig.getInstance().getConfig().getNode("commandCost").getInt();
            else
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cCould not parse config variable \"commandCost\"!");

            // Check the command's debug verbosity mode, as set in the config.
            getVerbosityMode();

            if (commandCost == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pu reload\\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false, inputIsInteger = false;
                String inputString = null;
                int inputInteger = 0;

                if (!args.<String>getOne("pokemon").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    checkAndAddHeader(commandCost, player);
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
                    printCorrectHelper(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }
                else
                {
                    inputString = args.<String>getOne("pokemon").get();

                    if (inputString.matches("^[0-9].*"))
                    {
                        inputIsInteger = true;
                        inputInteger = Integer.parseInt(args.<String>getOne("pokemon").get());

                        if (inputInteger > 802 || inputInteger < 1)
                        {
                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9dex number! Valid range is 1-802."));
                            src.sendMessage(Text.of("\u00A75Please note: \u00A7dYou can also enter a Pok\u00E9mon's name!"));
                            printCorrectHelper(commandCost, player);
                            checkAndAddFooter(commandCost, player);
                        }
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
                    EnumPokemonList returnedPokemon;

                    if (inputIsInteger)
                    {
                        returnedPokemon = getPokemonFromID(inputInteger);


                    }
                    else
                        returnedPokemon = getPokemonFromName(inputString);



                    if (returnedPokemon == null)
                    {
                        // TODO: Do checks for common mistakes, first. Add the below messages in an "else".

                        checkAndAddHeader(commandCost, player);
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9dex number! Valid range is 1-802."));
                        src.sendMessage(Text.of("\u00A75Please note: \u00A7dYou can also enter a Pok\u00E9mon's name!"));
                        printCorrectHelper(commandCost, player);
                        checkAndAddFooter(commandCost, player);

                        canContinue = false;
                    }

                    if (canContinue)
                    {
                        boolean type2Present = true;
                        int pokemonNumber = returnedPokemon.index;
                        String type1 = returnedPokemon.type1;
                        String type2 = returnedPokemon.type2;

                        /* NAME ODDITIES
                        029 Nidoran (female) - listed as NidoranFemale
                        032 Nidoran (male) - listed as NidoranMale
                        083 Farfetch'd - listed as Farfetchd
                        122 Mr. Mime - listed as MrMime
                        439 Mime Jr. - listed as MimeJr
                        669 Flabébé - listed as Flabebe
                        772 Type: Null - listed as TypeNull
                        782 Jangmo-o - listed as JangmoO
                        783 Hakamo-o - listed as HakamoO
                        784 Kommo-o - listed as KommoO
                        785 Tapu Koko - listed as TapuKoko
                        786 Tapu Lele - listed as TapuLele
                        787 Tapu Bunu - listed as TapuBunu
                        788 Tapu Fini - listed as TapuFini
                        */

                        /* POKÉMON WITH DIFFERENTLY TYPED FORMS
                        351 Castform
                        413 Wormadam
                        479 Rotom
                        492 Shaymin
                        555 Darmanitan
                        648 Meloetta
                        720 Hoopa
                        */


                    }



                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void checkAndAddHeader(int cost, Player player)
    {
        if (cost > 0)
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + cost + "\u00A7e coins."));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    private void printCorrectHelper(int cost, Player player)
    {
        if (cost != 0)
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/weakness <Pok\u00E9mon name/number> {-c to confirm}"));
        else
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/weakness <Pok\u00E9mon name/number>"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76Weakness // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73Weakness // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72Weakness // debug: \u00A7a" + inputString);
        }
    }

    /*
    //-------------------------------------------------------//
    // Testing routine for new additions to EnumPokemonList. //
    // Uncomment this if you need to test further additions. //
    //-------------------------------------------------------//

    // Taken from http://www.java2s.com/Tutorials/Java/Data_Type_How_to/String/Check_if_enum_contains_a_given_string.htm
    private static <E extends Enum<E>> boolean contains(Class<E> _enumClass,
                                                       String value) {
        try {
            return EnumSet.allOf(_enumClass)
                    .contains(Enum.valueOf(_enumClass, value));
        } catch (Exception e) {
            return false;
        }
    }

    enum validTypeList
    {
        Normal,
        Fighting,
        Flying,
        Poison,
        Ground,
        Rock,
        Bug,
        Ghost,
        Steel,
        Fire,
        Water,
        Grass,
        Electric,
        Psychic,
        Ice,
        Dragon,
        Dark,
        Fairy,
        EMPTY
    }

    //---------------------------------------------------//
    // Add the following under the inputIsInteger check. //
    //---------------------------------------------------//

    for (int i = 1; i < 803; i++) // UPDATE ME (replace 803 with your last Pokémon's ID, +1)
    {
        returnedPokemon = getPokemonFromID(i);

        if (!contains(validTypeList.class, returnedPokemon.type1) || !contains(validTypeList.class, returnedPokemon.type2))
            PixelUpgrade.log.info("\u00A74Array error found! \u00A7c" + returnedPokemon.index + " | " + returnedPokemon.type1 + " | " + returnedPokemon.type2);
    }
    */
}