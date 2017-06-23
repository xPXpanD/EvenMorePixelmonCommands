// Thanks for the command idea, MageFX!
package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.enums.EnumType;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.CheckTypesConfig;
import rs.expand.pixelupgrade.utilities.EnumPokemonList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.pixelmonmod.pixelmon.enums.EnumType.getTotalEffectiveness;
import static org.spongepowered.api.text.TextTemplate.of;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

// TODO: Some super long lists like " + alias + " 599 cause minor visual issues. Fixing that would be nice polish.
// TODO: Maybe look into paginated lists that you can move through. Lots of work, but would be real neat for evolutions.

public class CheckTypes implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!CheckTypesConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = CheckTypesConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!CheckTypesConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + CheckTypesConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            boolean presenceCheck = true;
            Boolean showFormMessage = checkConfigBool("showFormMessage");
            Boolean showAlolanMessage = checkConfigBool("showAlolanMessage");
            Integer commandCost = null;
            if (!CheckTypesConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                commandCost = CheckTypesConfig.getInstance().getConfig().getNode("commandCost").getInt();
            else
                PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cCould not parse config variable \"commandCost\"!");

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (showFormMessage == null || showAlolanMessage == null || commandCost == null)
                presenceCheck = false;

            if (!presenceCheck || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cCheck your config. If need be, wipe and \u00A74/pixelupgrade reload\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Player player = (Player) src;
                EnumPokemonList returnedPokemon = null;
                boolean canContinue = true, commandConfirmed = false, inputIsInteger = false;
                String inputString = null;
                int inputInteger;

                if (!args.<String>getOne("pokemon").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    checkAndAddHeader(commandCost, player);
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Provide a Pok\u00E9mon or Dex ID."));
                    printCorrectHelper(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }
                else
                {
                    inputString = args.<String>getOne("pokemon").get();

                    if (inputString.matches("\\d+"))
                    {
                        printToLog(3, "Got a number, converting input into Dex ID.");

                        inputIsInteger = true;
                        inputInteger = Integer.parseInt(inputString);

                        if (inputInteger > 802 || inputInteger < 1)
                        {
                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9dex number! Valid range is 1-802."));
                            printCorrectHelper(commandCost, player);
                            checkAndAddFooter(commandCost, player);

                            canContinue = false;
                        }
                        else
                            returnedPokemon = EnumPokemonList.getPokemonFromID(inputInteger);
                    }
                    else
                    {
                        printToLog(3, "Checking if input is valid.");
                        String updatedString = inputString;

                        switch (inputString.toUpperCase())
                        {
                            /*                                                        *\
                                TODO: Add space support for arguments. Low priority.
                                Tapu Koko, Tapu Lele, Tapu Bunu, Tapu Fini = broken.
                                Passing something like "tapukoko" should still work!
                                TODO: Also, go find out why Flabébé input is broken.
                            \*                                                        */

                            // Possibly dodgy inputs and names that are different internally for technical reasons.
                            case "NIDORANF": case "FNIDORAN": case "FEMALENIDORAN": case "NIDORAN♀":
                                updatedString = "NidoranFemale"; break;
                            case "NIDORANM": case "MNIDORAN": case "MALENIDORAN": case "NIDORAN♂":
                                updatedString = "NidoranMale"; break;
                            case "FARFETCH'D": case "FARFETCHED":
                                updatedString = "Farfetchd"; break;
                            case "MR.MIME": case "MISTERMIME":
                                updatedString = "MrMime"; break;
                            case "MIMEJR.": case "MIMEJUNIOR":
                                updatedString = "MimeJr"; break;
                            //case "FLABÉBÉ": case "FLABÈBÈ":
                            //    updatedString = "Flabebe"; break;
                            case "TYPE:NULL": case "TYPE:": case "TYPE": // A bit cheeky, but nothing else starts with "type" right now.
                                updatedString = "TypeNull"; break;
                            case "JANGMO-O":
                                updatedString = "JangmoO"; break;
                            case "HAKAMO-O":
                                updatedString = "HakamoO"; break;
                            case "KOMMO-O":
                                updatedString = "KommoO"; break;
                        }

                        if (!Objects.equals(updatedString, inputString))
                            printToLog(3, "Found a fixable input! Original: " + inputString + " | Changed to: " + updatedString);

                        inputString = updatedString;
                        returnedPokemon = EnumPokemonList.getPokemonFromName(inputString);

                        if (returnedPokemon == null)
                        {
                            printToLog(2, "Could not find a Pok\u00E9mon. Input: " + inputString);

                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9mon! Check spelling, or try a number."));
                            printCorrectHelper(commandCost, player);
                            checkAndAddFooter(commandCost, player);

                            canContinue = false;
                        }
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(3, "No errors encountered yet, running code on input!");

                    if (commandCost > 0)
                    {
                        BigDecimal costToConfirm = new BigDecimal(commandCost);

                        if (commandConfirmed)
                        {
                            Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                UniqueAccount uniqueAccount = optionalAccount.get();
                                TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());

                                if (transactionResult.getResult() == ResultType.SUCCESS)
                                {
                                    printToLog(1, "Checked Pok\u00E9mon for input string \"" + inputString + "\", and took " + costToConfirm + " coins.");
                                    checkTypes(returnedPokemon, inputIsInteger, inputString, player, showFormMessage, showAlolanMessage);
                                }
                                else
                                {
                                    BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                    printToLog(2, "Not enough coins! Cost: \u00A73" + costToConfirm + "\u00A7b, lacking: \u00A73" + balanceNeeded);

                                    src.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                                }
                            }
                            else
                            {
                                printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));
                            }
                        }
                        else
                        {
                            printToLog(2, "Got cost but no confirmation; end of the line.");

                            src.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking a Pok\u00E9mon's type stats costs \u00A76" + costToConfirm + "\u00A7e coins."));
                            src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + inputString + " -c"));
                        }
                    }
                    else
                    {
                        printToLog(2, "Checked Pok\u00E9mon for input string \"" + inputString + "\". Config price is 0, taking nothing.");
                        checkTypes(returnedPokemon, inputIsInteger, inputString, player, showFormMessage, showAlolanMessage);
                    }
                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    //private int getTypePosition(String[] types, String )

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
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <Pok\u00E9mon name/number> {-c to confirm}"));
        else
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <Pok\u00E9mon name/number>"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76CheckTypes // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73CheckTypes // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72CheckTypes // debug: \u00A7a" + inputString);
        }
    }

    private Boolean checkConfigBool(String node)
    {
        if (!CheckTypesConfig.getInstance().getConfig().getNode(node).isVirtual())
            return CheckTypesConfig.getInstance().getConfig().getNode(node).getBoolean();
        else
        {
            PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }

    private void checkTypes(EnumPokemonList returnedPokemon, boolean inputIsInteger, String inputString, Player player, boolean showFormMessage, boolean showAlolanMessage)
    {
        /*                                                           *\
             Check for differently typed forms or Alolan variants.
        \*                                                           */
        boolean hasForms = true, hasAlolanVariants = true;
        if (inputIsInteger)
        {
            switch (returnedPokemon.index) // Differently typed forms.
            {
                case 351: case 413: case 479: case 492: case 555: case 648: case 720: break;
                default: hasForms = false; // Gotta love fallthroughs!
            }

            switch (returnedPokemon.index) // Alolan variants.
            {
                case 19: case 20: case 26: case 27: case 28: case 37: case 38: case 50: case 51: case 52:
                case 53: case 74: case 75: case 76: case 88: case 89: case 103: case 105: break;
                default: hasAlolanVariants = false;
            }
        }
        else
        {
            switch (inputString.toUpperCase()) // Differently typed forms.
            {
                case "CASTFORM": case "WORMADAM": case "ROTOM": case "SHAYMIN":
                case "DARMANITAN": case "MELOETTA": case "HOOPA": break;
                default: hasForms = false;
            }

            switch (inputString.toUpperCase()) // Alolan variants.
            {
                case "RATTATA": case "RATICATE": case "RAICHU": case "SANDSHREW": case "SANDSLASH": case "VULPIX":
                case "NINETALES": case "DIGLETT": case "DUGTRIO": case "MEOWTH": case "PERSIAN": case "GEODUDE":
                case "GRAVELER": case "GOLEM": case "GRIMER": case "MUK": case "EXEGGUTOR": case "MAROWAK": break;
                default: hasAlolanVariants = false;
            }
        }

        /*                                                        *\
             Set up internal variables for (almost) EVERYTHING.
        \*                                                        */
        boolean type2Present = true;
        int pNumber = returnedPokemon.index;
        String pName = returnedPokemon.name(), nameMessage, typeMessage;
        EnumType type1 = EnumType.parseType(returnedPokemon.type1);
        EnumType type2 = EnumType.parseType(returnedPokemon.type2);
        if (returnedPokemon.type2.contains("EMPTY"))
            type2Present = false;

        String typeString =
                "\u00A7fNormal, \u00A74Fighting, \u00A79Flying, \u00A75Poison, \u00A76Ground, " +
                "\u00A77Rock, \u00A72Bug, \u00A75Ghost, \u00A77Steel, \u00A7cFire, \u00A73Water, " +
                "\u00A7aGrass, \u00A7eElectric, \u00A7dPsychic, \u00A7bIce, \u00A79Dragon, " +
                "\u00A78Dark, \u00A7dFairy";
        String[] typeList = typeString.split(", ");

        String unformattedTypeString =
                "Normal, Fighting, Flying, Poison, Ground, Rock, Bug, Ghost, Steel, " +
                "Fire, Water, Grass, Electric, Psychic, Ice, Dragon, Dark, Fairy";
        String[] unformattedTypeList = unformattedTypeString.split(", ");

        ArrayList<EnumType> foundTypes = new ArrayList<>();
        foundTypes.add(type1);
        int indexType1 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type1)), indexType2;
        if (type2Present)
        {
            printToLog(3, "Found two types on provided Pok\u00E9mon.");
            foundTypes.add(type2);
            indexType2 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type2));

            // Used way later, but setting it up now avoids some repeat code.
            typeMessage = " \u00A7f(" + typeList[indexType1] + "\u00A7f, " + typeList[indexType2] + "\u00A7f)";
        }
        else
        {
            printToLog(3, "Found one type on provided Pok\u00E9mon.");
            typeMessage = " \u00A7f(" + typeList[indexType1] + "\u00A7f)";
        }

        /*                                                                         *\
             Run through the big list of Pokémon and check the target's type(s).
        \*                                                                         */
        StringBuilder weaknessBuilder2x = new StringBuilder(), weaknessBuilder4x = new StringBuilder();
        StringBuilder strengthBuilder50p = new StringBuilder(), strengthBuilder25p = new StringBuilder();
        StringBuilder immunityBuilder = new StringBuilder();

        printToLog(3, "Building the type list... Loop is go!");
        for (int i = 1; i < 19; i++)
        {
            EnumType typeToTest = EnumType.parseType(unformattedTypeList[i - 1]);
            float typeEffectiveness = getTotalEffectiveness(foundTypes, typeToTest);

            if (typeEffectiveness < 1.0f)
            {
                if (typeEffectiveness == 0.5f) // 50% effectiveness
                    strengthBuilder50p.append(typeList[i - 1]).append("\u00A7f, ");
                else if (typeEffectiveness == 0.25f) // 25% effectiveness
                    strengthBuilder25p.append(typeList[i - 1]).append("\u00A7f, ");
                else if (typeEffectiveness == 0.00f) // Immune!
                    immunityBuilder.append(typeList[i - 1]).append("\u00A7f, ");
            }
            else if (typeEffectiveness > 1.0f)
            {
                if (typeEffectiveness == 2.0f) // 200% effectiveness
                    weaknessBuilder2x.append(typeList[i - 1]).append("\u00A7f, ");
                else if (typeEffectiveness == 4.0f) // 400% effectiveness, ouch!
                    weaknessBuilder4x.append(typeList[i - 1]).append("\u00A7f, ");
            }
        }

        /*                                                 *\
             Fix the shown Pokémon's name, if necessary.
        \*                                                 */
        printToLog(3, "Checking whether the Pok\u00E9mon needs its shown name adjusted.");
        player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
        switch (pName)
        {
            // Forms.
            case "CastformSunny":
                nameMessage = "\u00A71(\u00A79#351\u00A71) \u00A76Sunny Castform"; break;
            case "CastformRainy":
                nameMessage = "\u00A71(\u00A79#351\u00A71) \u00A76Rainy Castform"; break;
            case "CastformSnowy":
                nameMessage = "\u00A71(\u00A79#351\u00A71) \u00A76Snowy Castform"; break;
            case "WormadamSandy":
                nameMessage = "\u00A71(\u00A79#413\u00A71) \u00A76Sandy Wormadam"; break;
            case "WormadamTrash":
                nameMessage = "\u00A71(\u00A79#413\u00A71) \u00A76Trashy Wormadam"; break;
            case "RotomHeat":
                nameMessage = "\u00A71(\u00A79#479\u00A71) \u00A76Microwave Rotom"; break;
            case "RotomWash":
                nameMessage = "\u00A71(\u00A79#479\u00A71) \u00A76Washer Rotom"; break;
            case "RotomFrost":
                nameMessage = "\u00A71(\u00A79#479\u00A71) \u00A76Fridge Rotom"; break;
            case "RotomFan":
                nameMessage = "\u00A71(\u00A79#479\u00A71) \u00A76Fan Rotom"; break;
            case "RotomMow":
                nameMessage = "\u00A71(\u00A79#479\u00A71) \u00A76Mower Rotom"; break;
            case "ShayminSky":
                nameMessage = "\u00A71(\u00A79#492\u00A71) \u00A76Sky Shaymin"; break;
            case "DarmanitanZen":
                nameMessage = "\u00A71(\u00A79#555\u00A71) \u00A76Zen Darmanitan"; break;
            case "MeloettaPirouette":
                nameMessage = "\u00A71(\u00A79#648\u00A71) \u00A76Pirouette Meloetta"; break;
            case "HoopaUnbound":
                nameMessage = "\u00A71(\u00A79#720\u00A71) \u00A76Unbound Hoopa"; break;

            // Alolan variants.
            case "RattataAlolan":
                nameMessage = "\u00A71(\u00A79#19\u00A71) \u00A76Alolan Rattata"; break;
            case "RaticateAlolan":
                nameMessage = "\u00A71(\u00A79#20\u00A71) \u00A76Alolan Raticate"; break;
            case "RaichuAlolan":
                nameMessage = "\u00A71(\u00A79#26\u00A71) \u00A76Alolan Raichu"; break;
            case "SandshrewAlolan":
                nameMessage = "\u00A71(\u00A79#27\u00A71) \u00A76Alolan Sandshrew"; break;
            case "SandslashAlolan":
                nameMessage = "\u00A71(\u00A79#28\u00A71) \u00A76Alolan Sandslash"; break;
            case "VulpixAlolan":
                nameMessage = "\u00A71(\u00A79#37\u00A71) \u00A76Alolan Vulpix"; break;
            case "NinetalesAlolan":
                nameMessage = "\u00A71(\u00A79#38\u00A71) \u00A76Alolan Ninetales"; break;
            case "DiglettAlolan":
                nameMessage = "\u00A71(\u00A79#50\u00A71) \u00A76Alolan Diglett"; break;
            case "DugtrioAlolan":
                nameMessage = "\u00A71(\u00A79#51\u00A71) \u00A76Alolan Dugtrio"; break;
            case "MeowthAlolan":
                nameMessage = "\u00A71(\u00A79#52\u00A71) \u00A76Alolan Meowth"; break;
            case "PersianAlolan":
                nameMessage = "\u00A71(\u00A79#53\u00A71) \u00A76Alolan Persian"; break;
            case "GeodudeAlolan":
                nameMessage = "\u00A71(\u00A79#74\u00A71) \u00A76Alolan Geodude"; break;
            case "GravelerAlolan":
                nameMessage = "\u00A71(\u00A79#75\u00A71) \u00A76Alolan Graveler"; break;
            case "GolemAlolan":
                nameMessage = "\u00A71(\u00A79#76\u00A71) \u00A76Alolan Golem"; break;
            case "GrimerAlolan":
                nameMessage = "\u00A71(\u00A79#88\u00A71) \u00A76Alolan Grimer"; break;
            case "MukAlolan":
                nameMessage = "\u00A71(\u00A79#89\u00A71) \u00A76Alolan Muk"; break;
            case "ExeggutorAlolan":
                nameMessage = "\u00A71(\u00A79#103\u00A71) \u00A76Alolan Exeggutor"; break;
            case "MarowakAlolan":
                nameMessage = "\u00A71(\u00A79#105\u00A71) \u00A76Alolan Marowak"; break;

            // Pokémon with weird internal names due to technical issues.
            case "NidoranFemale":
                nameMessage = "\u00A71(\u00A79#29\u00A71) \u00A76Nidoran \u2640"; break; // Female symbol
            case "NidoranMale":
                nameMessage = "\u00A71(\u00A79#32\u00A71) \u00A76Nidoran \u2642"; break; // Male symbol
            case "Farfetchd":
                nameMessage = "\u00A71(\u00A79#83\u00A71) \u00A76Farfetch'd"; break;
            case "MrMime":
                nameMessage = "\u00A71(\u00A79#122\u00A71) \u00A76Mr. Mime"; break;
            case "MimeJr":
                nameMessage = "\u00A71(\u00A79#439\u00A71) \u00A76Mime Jr."; break;
            case "Flabebe":
                nameMessage = "\u00A71(\u00A79#669\u00A71) \u00A76Flab\u00E9b\u00E9"; break; // é
            case "TypeNull":
                nameMessage = "\u00A71(\u00A79#772\u00A71) \u00A76Type: Null"; break;
            case "JangmoO":
                nameMessage = "\u00A71(\u00A79#782\u00A71) \u00A76Jangmo-O"; break;
            case "HakamoO":
                nameMessage = "\u00A71(\u00A79#783\u00A71) \u00A76Hakamo-O"; break;
            case "KommoO":
                nameMessage = "\u00A71(\u00A79#784\u00A71) \u00A76Kommo-O"; break;

            // Pokémon is not special, print defaults.
            default:
                nameMessage = "\u00A71(\u00A79#" + pNumber + "\u00A71) \u00A76" + pName;
                printToLog(3, "Name did not need to be fixed, showing straight from the list.");
        }

        player.sendMessage(Text.of(nameMessage + typeMessage));
        player.sendMessage(Text.of(""));

        /*                                                                *\
             Get resistances, weaknesses and immunities. Print to chat.
        \*                                                                */
        if (weaknessBuilder2x.length() != 0 || weaknessBuilder4x.length() != 0)
        {
            player.sendMessage(Text.of("\u00A7cWeaknesses\u00A76:"));
            if (weaknessBuilder4x.length() != 0)
            {
                weaknessBuilder4x.setLength(weaknessBuilder4x.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7c400%\u00A7f: " + weaknessBuilder4x));
            }
            if (weaknessBuilder2x.length() != 0)
            {
                weaknessBuilder2x.setLength(weaknessBuilder2x.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7c200%\u00A7f: " + weaknessBuilder2x));
            }
        }

        if (strengthBuilder50p.length() != 0 || strengthBuilder25p.length() != 0)
        {
            player.sendMessage(Text.of("\u00A7aResistances\u00A76:"));
            if (strengthBuilder50p.length() != 0)
            {
                strengthBuilder50p.setLength(strengthBuilder50p.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7a50%\u00A7f: " + strengthBuilder50p));
            }
            if (strengthBuilder25p.length() != 0)
            {
                strengthBuilder25p.setLength(strengthBuilder25p.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7a25%\u00A7f: " + strengthBuilder25p));
            }
        }

        /*                                                          *\
             Find and format a Pokémon's type-relevant abilities.
        \*                                                          */
        player.sendMessage(Text.of("\u00A7bImmunities\u00A76:"));
        printToLog(3, "Grabbing immunities and turning them into a fancy list.");

        // Make a bunch of lists for different type-nullifying abilities.
        String motorDrive =
                "Electivire, Blitzle, Zebstrika, Emolga";
        String suctionCups =
                "Octillery, Lileep, Cradily, Inkay, Malamar";
        String voltAbsorb =
                "Jolteon, Chinchou, Lanturn, Thundurus, Raikou, Minun, Pachirisu";
        String stormDrain =
                "Lileep, Cradily, Shellos, Gastrodon, Finneon, Lumineon, Maractus";
        String drySkin =
                "Paras, Parasect, Croagunk, Toxicroak, Helioptile, Heliolisk, Jynx";
        String justified =
                "Growlithe, Arcanine, Absol, Lucario, Gallade, Cobalion, Terrakion, Virizion, Keldeo";
        String hyperCutter =
                "Krabby, Kingler, Pinsir, Gligar, Mawile, Trapinch, Corphish, Crawdaunt, Gliscor, Crabrawler, " +
                "Crabominable";
        String soundProof =
                "Voltorb, Electrode, MrMime, Whismur, Loudred, Exploud, MimeJr, Shieldon, Bastiodon, Snover, " +
                "Abomasnow, Bouffalant";
        String bigPecks =
                "Pidove, Tranquill, Unfezant, Ducklett, Swanna, Vullaby, Mandibuzz, Fletchling, Pidgey," +
                "Pidgeotto, Pidgeot, Chatot";
        String clearBody =
                "Tentacool, Tentacruel, Beldum, Metang, Metagross, Regirock, Regice, Registeel, Carbink, " +
                "Diancie, Klink, Klang, Klinklang";
        String sapSipper =
                "Deerling, Sawsbuck, Bouffalant, Skiddo, Gogoat, Goomy, Sliggoo, Goodra, Drampa, Marill, " +
                "Azumarill, Girafarig, Stantler, Miltank, Azurill, Blitzle, Zebstrika";
        String damp =
                "Psyduck, Golduck, Paras, Parasect, Horsea, Seadra, Kingdra, Mudkip, Marshtomp, Swampert, " +
                "Frillish, Jellicent, Poliwag, Poliwhirl, Poliwrath, Politoed, Wooper, Quagsire";
        String lightningRod =
                "Cubone, Marowak, Rhyhorn, Rhydon, Electrike, Manectric, Rhyperior, Blitzle, Zebstrika, " +
                "Pikachu, Raichu, Goldeen, Seaking, Zapdos, Pichu, Plusle, Sceptile, MarowakAlolan";
        String flashFire =
                "Vulpix, Ninetales, Growlithe, Arcanine, Ponyta, Rapidash, Flareon, Houndour, Houndoom, " +
                "Heatran, Litwick, Lampent, Chandelure, Heatmor, Cyndaquil, Quilava, Typhlosion, Entei";
        String waterAbsorb =
                "Lapras, Vaporeon, Mantine, Mantyke, Maractus, Volcanion, Chinchou, Lanturn, Suicune, Cacnea, " +
                "Cacturne, Tympole, Palpitoad, Seismitoad, Frillish, Jellicent, Poliwag, Poliwhirl, Poliwrath, " +
                "Politoed, Wooper, Quagsire";
        String sturdy =
                "Geodude, Graveler, Golem, Magnemite, Magneton, Onix, Sudowoodo, Pineco, Forretress, Steelix, " +
                "Shuckle, Skarmory, Donphan, Nosepass, Aron, Lairon, Aggron, Shieldon, Bastiodon, Bonsly, " +
                "Magnezone, Probopass, Roggenrola, Boldore, Gigalith, Sawk, Dwebble, Crustle, Tirtouga, " +
                "Carracosta, Relicanth, Regirock, Tyrunt, Carbink, Bergmite, Avalugg";
        String levitate =
                "Gastly, Haunter, Gengar, Koffing, Weezing, Misdreavus, Unown, Vibrava, Flygon, Lunatone, " +
                "Solrock, Baltoy, Claydol, Duskull, Chimecho, Latias, Latios, Mismagius, Chingling, Bronzor, " +
                "Bronzong, Carnivine, Rotom, RotomHeat, RotomWash, RotomFrost, RotomFan, RotomMow, Uxie, " +
                "Mesprit, Azelf, Giratina, Cresselia, Tynamo, Eelektrik, Eelektross, Cryogonal, Hydreigon, " +
                "Vikavolt";

        // Abilities/hovers are linked. If one has two entries, the other will have two, too!
        ArrayList<String> abilities = new ArrayList<>(), hovers = new ArrayList<>();

        if (immunityBuilder.length() == 0)
            immunityBuilder.append("\u00A78None?"); // Shown when a Pokémon isn't immune against anything.
        else
            immunityBuilder.setLength(immunityBuilder.length() - 2); // Shank any trailing commas.

        Text immunityStart = Text.of("\\- \u00A7b0%\u00A7f: " + immunityBuilder + "\u00A77 (may have ");

        // Check if Pokémon are on certain lists. Create nice Strings to print to chat and add as hovers.
        if (motorDrive.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nMotor Drive");
            hovers.add("\u00A77\u00A7lMotor Drive \u00A7r\u00A77nullifies \u00A7eElectric \u00A77damage.");
        }
        if (suctionCups.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nSuction Cups");
            hovers.add("\u00A77\u00A7lSuction Cups \u00A7r\u00A77prevents \u00A7nswitch-out\u00A7r\u00A77 moves.");
        }
        if (voltAbsorb.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nVolt Absorb");
            hovers.add("\u00A77\u00A7lVolt Absorb \u00A7r\u00A77nullifies \u00A7eElectric \u00A77damage.");
        }
        if (stormDrain.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nStorm Drain");
            hovers.add("\u00A77\u00A7lStorm Drain \u00A7r\u00A77nullifies \u00A73Water \u00A77damage.");
        }
        if (drySkin.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nDry Skin");
            hovers.add("\u00A77\u00A7lDry Skin \u00A7r\u00A77adds 25% \u00A73Water \u00A77absorbance but \u00A7cFire \u00A77hurts 25% more.");
        }
        if (justified.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nJustified");
            hovers.add("\u00A77\u00A7lJustified \u00A7r\u00A77ups \u00A7nAttack\u00A7r\u00A77 by one stage when hit by a \u00A78Dark \u00A77move.");
        }
        if (hyperCutter.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nHyper Cutter");
            hovers.add("\u00A77\u00A7lHyper Cutter \u00A7r\u00A77prevents a Pok\u00E9mon's Attack from being dropped.");
        }
        if (soundProof.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nSoundproof");
            hovers.add("\u00A77\u00A7lSoundproof \u00A7r\u00A77nullifies \u00A7nsound-based\u00A7r\u00A77 moves.");
        }
        if (bigPecks.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nBig Pecks");
            hovers.add("\u00A77\u00A7lBig Pecks \u00A7r\u00A77prevents a Pok\u00E9mon's Defense from being dropped.");
        }
        if (clearBody.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nClear Body");
            hovers.add("\u00A77\u00A7lClear Body \u00A7r\u00A77prevents all of a Pok\u00E9mon's stats from being dropped.");
        }
        if (sapSipper.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nSap Sipper");
            hovers.add("\u00A77\u00A7lSap Sipper \u00A7r\u00A77nullifies \u00A7aGrass \u00A77damage.");
        }
        if (damp.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nDamp");
            hovers.add("\u00A77\u00A7lDamp \u00A7r\u00A77disables \u00A7nSelf-Destruct\u00A7r\u00A77/\u00A7nExplosion\u00A7r\u00A77.");
        }
        if (lightningRod.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nLightning Rod");
            hovers.add("\u00A77\u00A7lLightning Rod \u00A7r\u00A77nullifies \u00A7eElectric \u00A77damage.");
        }
        if (flashFire.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nFlash Fire");
            hovers.add("\u00A77\u00A7lFlash Fire \u00A7r\u00A77nullifies \u00A7cFire \u00A77damage.");
        }
        if (waterAbsorb.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nWater Absorb");
            hovers.add("\u00A77\u00A7lWater Absorb \u00A7r\u00A77nullifies \u00A73Water \u00A77damage.");
        }
        if (sturdy.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nSturdy");
            hovers.add("\u00A77\u00A7lSturdy \u00A7r\u00A77prevents \u00A7n1-hit KO\u00A7r\u00A77 attacks.");
        }
        if (levitate.contains(pName))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nLevitate");
            hovers.add("\u00A77\u00A7lLevitate \u00A7r\u00A77nullifies \u00A7eGround \u00A77damage.");
        }

        // Check if we have certain unique Pokémon with unique abilities.
        if (pName.matches("Torkoal|Heatmor")) // Regular expressions! Woo!
        {
            abilities.add("\u00A7f\u00A7l\u00A7nWhite Smoke");
            hovers.add("\u00A77\u00A7lWhite Smoke \u00A77provides immunity to stat reduction.");
        }
        else if (pName.contains("Shedinja"))
        {
            abilities.add("\u00A7f\u00A7l\u00A7nWonder Guard");
            hovers.add("\u00A77\u00A7lWonder Guard \u00A77disables all \u00A7nnon-super effective\u00A7r\u00A77 damage.");
            immunityStart = Text.of("\\- \u00A7b0%\u00A7f: " + immunityBuilder + "\u00A77 (has "); // Less awkward.
        }

        /*                                                          *\
             Figure out what to show in chat, and how to show it.
        \*                                                          */
        Text immunityPair, immunityPair2, immunityPair3;
        String immunityEnd = "\u00A7r\u00A77)";
        if (abilities.size() == 1)
        {
            immunityPair = Text.builder(abilities.get(0))
                    .onHover(TextActions.showText(Text.of(hovers.get(0))))
                    .build();
            player.sendMessage(of(immunityStart, immunityPair, immunityEnd));
        }
        else if (abilities.size() == 2)
        {
            Text orMessage = Text.of("\u00A7r\u00A77 or \u00A7f\u00A7l\u00A7n");
            immunityPair = Text.builder(abilities.get(0))
                    .onHover(TextActions.showText(Text.of(hovers.get(0))))
                    .build();
            immunityPair2 = Text.builder(abilities.get(1))
                    .onHover(TextActions.showText(Text.of(hovers.get(1))))
                    .build();

            player.sendMessage(of(immunityStart, immunityPair, orMessage, immunityPair2, immunityEnd));
        }
        else if (abilities.size() == 3)
        {
            // Overwrite this here so we can squeeze in more info. Not ideal, but single lines are nice.
            immunityStart = Text.of("\\- \u00A7b0%\u00A7f: " + immunityBuilder + "\u00A77 (may have type abilities, see below)");

            Text orMessage = Text.of("\u00A7r\u00A77 or \u00A7f\u00A7l\u00A7n");
            Text newLineFormat = Text.of("\\- \u00A7b=>\u00A7f: ");
            immunityPair = Text.builder(abilities.get(0))
                    .onHover(TextActions.showText(Text.of(hovers.get(0))))
                    .build();
            immunityPair2 = Text.builder(abilities.get(1))
                    .onHover(TextActions.showText(Text.of(hovers.get(1))))
                    .build();
            immunityPair3 = Text.builder(abilities.get(2))
                    .onHover(TextActions.showText(Text.of(hovers.get(2))))
                    .build();

            player.sendMessage(immunityStart);
            player.sendMessage(of(newLineFormat, immunityPair, orMessage, immunityPair2, orMessage, immunityPair3));
        }
        else
            player.sendMessage(Text.of("\\- \u00A7b0%\u00A7f: " + immunityBuilder));

        /*                                                                              *\
             Print messages if differently typed forms or Alolan forms are available.
        \*                                                                              */
        if (hasForms && showFormMessage)
        {
            printToLog(3, "Showing forms is enabled, and we can show one! Doing it.");
            String commandHelper = "\u00A7cCheck out: \u00A76" + alias + " ";

            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A7dThis Pok\u00E9mon has one or more forms with different types."));

            switch (pName)
            {
                // Big ones. We provide just the names, to keep things manageable. Rotom's super squished by necessity.
                case "Castform":
                    player.sendMessage(Text.of(commandHelper + "CastformSunny \u00A7f(or \u00A76Rainy\u00A7f/\u00A76Snowy\u00A7f)")); break;
                case "Wormadam":
                    player.sendMessage(Text.of(commandHelper + "WormadamSandy\u00A7f, \u00A76WormadamTrash\u00A7f")); break;
                case "Rotom":
                    player.sendMessage(Text.of(commandHelper + "RotomHeat \u00A7f(or \u00A76Wash\u00A7f/\u00A76Frost\u00A7f/\u00A76Fan\u00A7f/\u00A76Mow\u00A7f)")); break;

                // Small ones. We can show types on these, like the Alolan variants.
                case "Shaymin":
                    player.sendMessage(Text.of(commandHelper + "ShayminSky \u00A7f(\u00A7aGrass\u00A7f, \u00A79Flying\u00A7f)")); break;
                case "Darmanitan":
                    player.sendMessage(Text.of(commandHelper + "DarmanitanZen \u00A7f(\u00A7cFire\u00A7f, \u00A7dPsychic\u00A7f)")); break;
                case "Meloetta":
                    player.sendMessage(Text.of(commandHelper + "MeloettaPirouette \u00A7f(Normal, \u00A74Fighting\u00A7f)")); break;
                case "Hoopa":
                    player.sendMessage(Text.of(commandHelper + "HoopaUnbound \u00A7f(\u00A7dPsychic\u00A7f, \u00A78Dark\u00A7f)")); break;
            }
        }
        else if (hasAlolanVariants && showAlolanMessage)
        {
            printToLog(3, "Showing Alolan variants is enabled, and we've got one! Showing.");
            String commandHelper = "\u00A7cCheck out: \u00A76" + alias + " ";

            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A7dThis Pok\u00E9mon has an Alolan variant."));

            switch (pName)
            {
                // Alolan variants. Same as above.
                case "Rattata":
                    player.sendMessage(Text.of(commandHelper + "RattataAlolan \u00A7f(\u00A78Dark\u00A7f, Normal)")); break;
                case "Raticate":
                    player.sendMessage(Text.of(commandHelper + "RaticateAlolan \u00A7f(\u00A78Dark\u00A7f, Normal)")); break;
                case "Raichu":
                    player.sendMessage(Text.of(commandHelper + "RaichuAlolan \u00A7f(\u00A7eElectric\u00A7f, \u00A7dPsychic\u00A7f)")); break;
                case "Sandshrew":
                    player.sendMessage(Text.of(commandHelper + "SandshrewAlolan \u00A7f(\u00A7bIce\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "Sandslash":
                    player.sendMessage(Text.of(commandHelper + "SandslashAlolan \u00A7f(\u00A7bIce\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "Vulpix":
                    player.sendMessage(Text.of(commandHelper + "VulpixAlolan \u00A7f(\u00A7bIce\u00A7f)")); break;
                case "Ninetales":
                    player.sendMessage(Text.of(commandHelper + "NinetalesAlolan \u00A7f(\u00A7bIce\u00A7f, \u00A7dFairy\u00A7f)")); break;
                case "Diglett":
                    player.sendMessage(Text.of(commandHelper + "DiglettAlolan \u00A7f(\u00A76Ground\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "Dugtrio":
                    player.sendMessage(Text.of(commandHelper + "DugtrioAlolan \u00A7f(\u00A76Ground\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "Meowth":
                    player.sendMessage(Text.of(commandHelper + "MeowthAlolan \u00A7f(\u00A78Dark\u00A7f)")); break;
                case "Persian":
                    player.sendMessage(Text.of(commandHelper + "PersianAlolan \u00A7f(\u00A78Dark\u00A7f)")); break;
                case "Geodude":
                    player.sendMessage(Text.of(commandHelper + "GeodudeAlolan \u00A7f(\u00A77Rock\u00A7f, \u00A7eElectric\u00A7f)")); break;
                case "Graveler":
                    player.sendMessage(Text.of(commandHelper + "GravelerAlolan \u00A7f(\u00A77Rock\u00A7f, \u00A7eElectric\u00A7f)")); break;
                case "Golem":
                    player.sendMessage(Text.of(commandHelper + "GolemAlolan \u00A7f(\u00A77Rock\u00A7f, \u00A7eElectric\u00A7f)")); break;
                case "Grimer":
                    player.sendMessage(Text.of(commandHelper + "GrimerAlolan \u00A7f(\u00A75Poison\u00A7f, \u00A78Dark\u00A7f)")); break;
                case "Muk":
                    player.sendMessage(Text.of(commandHelper + "MukAlolan \u00A7f(\u00A75Poison\u00A7f, \u00A78Dark\u00A7f)")); break;
                case "Exeggutor":
                    player.sendMessage(Text.of(commandHelper + "ExeggutorAlolan \u00A7f(\u00A7aGrass\u00A7f, \u00A79Dragon\u00A7f)")); break;
                case "Marowak":
                    player.sendMessage(Text.of(commandHelper + "MarowakAlolan \u00A7f(\u00A7cFire\u00A7f, \u00A75Ghost\u00A7f)")); break;
            }
        }

        player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
    }
}