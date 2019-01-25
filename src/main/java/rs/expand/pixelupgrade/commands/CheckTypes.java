// Thanks for the command idea, MageFX!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.enums.EnumType;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import rs.expand.pixelupgrade.utilities.PokemonMethods;

// FIXME: Fix some super long lists like /checktypes 599 causing minor visual issues. Nice polish.
// TODO: Maybe look into paginated lists that you can move through. Lots of work, but would be real neat for evolutions.
public class CheckTypes implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    public static String commandAlias;
    public static Boolean showFormMessage, showAlolanMessage;

    // Are we running from console? We'll flag this true, and proceed accordingly.
    private boolean calledRemotely;

    /* TODO: Maybe move over to this later. Might be good practice?
    private static Map<String, String> formattedTypeMap = new HashMap<>();
    {
        formattedTypeMap.put("Normal", "§fNormal");
        formattedTypeMap.put("Fighting", "§4Fighting");
        formattedTypeMap.put("Flying", "§9Flying");
        formattedTypeMap.put("Poison", "§5Poison");
        formattedTypeMap.put("Ground", "§6Ground");
        formattedTypeMap.put("Rock", "§7Rock");
        formattedTypeMap.put("Bug", "§2Bug");
        formattedTypeMap.put("Ghost", "§5Ghost");
        formattedTypeMap.put("Steel", "§7Steel");
        formattedTypeMap.put("Fire", "§cFire");
        formattedTypeMap.put("Water", "§3Water");
        formattedTypeMap.put("Grass", "§aGrass");
        formattedTypeMap.put("Electric", "§eElectric");
        formattedTypeMap.put("Psychic", "§dPsychic");
        formattedTypeMap.put("Ice", "§bIce");
        formattedTypeMap.put("Dragon", "§9Dragon");
        formattedTypeMap.put("Dark", "§8Dark");
        formattedTypeMap.put("Fairy", "§dFairy");
    }
     */

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    // If we're running from console, we need to swallow everything to avoid cluttering.
    private void printToLog (final int debugNum, final String inputString)
    {
        if (!calledRemotely)
            PrintingMethods.printDebugMessage("CheckTypes", debugNum, inputString);
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (!(src instanceof CommandBlock))
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (showFormMessage == null)
                nativeErrorArray.add("showFormMessage");
            if (showAlolanMessage == null)
                nativeErrorArray.add("showAlolanMessage");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("CheckTypes", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                if (!(src instanceof Player))
                {
                    PrintingMethods.printDebugMessage("CheckTypes", 1,
                            "Called by console, starting. Silencing further log messages.");

                    // Running from console? Let's tell our code that. If "src" is not a Player, this becomes true.
                    calledRemotely = true;
                }
                else
                    printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                PokemonMethods returnedPokemon = null;
                boolean canContinue = true, inputIsInteger = false, inputWasEdited = false;
                String arg1String = null, arg2String = "", errorString = "§4There's an error message missing, please report this!";
                final int inputInteger;

                // Do we have an argument in the first slot?
                if (args.<String>getOne("Pokémon name/ID").isPresent())
                {
                    printToLog(2, "Starting argument check for player's input.");
                    arg1String = args.<String>getOne("Pokémon name/ID").get();
                    final Optional<String> arg2Optional = args.getOne("optional second word");

                    if (arg1String.matches("-?\\d+"))
                    {
                        printToLog(2, "Got a number, converting input into Dex ID and checking.");

                        inputIsInteger = true;
                        inputInteger = Integer.parseInt(arg1String);

                        if (inputInteger > 807 || inputInteger < 1)
                        {
                            printToLog(1, "Dex ID \"§3" + inputInteger + "§b\" was out of range. Exit.");
                            errorString = "§4Error: §cInvalid Pokédex number! Valid range is 1-807.";
                            canContinue = false;
                        }
                        else
                            returnedPokemon = PokemonMethods.getPokemonFromID(inputInteger);
                    }
                    else
                    {
                        printToLog(2, "Input not numeric, checking if it's a valid name.");
                        String updatedString = arg1String;

                        switch (arg1String.toUpperCase())
                        {
                            // Possibly dodgy inputs and names that are different internally for technical reasons.
                            case "NIDORANF": case "FNIDORAN": case "FEMALENIDORAN": case "NIDORAN♀":
                                updatedString = "NidoranFemale"; break;
                            case "NIDORANM": case "MNIDORAN": case "MALENIDORAN": case "NIDORAN♂":
                                updatedString = "NidoranMale"; break;
                            case "FARFETCH'D": case "FARFETCHED":
                                updatedString = "Farfetchd"; break;
                            case "MR.MIME": case "MISTERMIME":
                                updatedString = "MrMime"; break;
                            case "HO-OH":
                                updatedString = "HoOh"; break;
                            case "MIMEJR.": case "MIMEJUNIOR":
                                updatedString = "MimeJr"; break;
                            case "FLABÉBÉ": case "FLABÈBÈ":
                                updatedString = "Flabebe"; break;
                            case "TYPE:NULL":
                                updatedString = "TypeNull"; break;
                            case "JANGMO-O":
                                updatedString = "JangmoO"; break;
                            case "HAKAMO-O":
                                updatedString = "HakamoO"; break;
                            case "KOMMO-O":
                                updatedString = "KommoO"; break;
                        }

                        if (arg2Optional.isPresent())
                        {
                            // Used purely so we don't show extraneous input.
                            boolean isFixed = true;

                            arg2String = arg2Optional.get();
                            printToLog(2, "Found second arg \"§2" + arg2String +
                                    "§a\", checking if it's part of a name.");

                            switch (arg2String.toUpperCase())
                            {
                                // Alolan variants.
                                case "RATTATA": case "RATICATE": case "RAICHU": case "SANDSHREW": case "SANDSLASH": case "VULPIX":
                                case "NINETALES": case "DIGLETT": case "DUGTRIO": case "MEOWTH": case "PERSIAN": case "GEODUDE":
                                case "GRAVELER": case "GOLEM": case "GRIMER": case "MUK": case "EXEGGUTOR": case "MAROWAK":
                                {
                                    if (arg1String.toUpperCase().equals("ALOLAN"))
                                    {
                                        // Split our arg2String String into constituent characters.
                                        final char[] characterArray = arg2String.toCharArray();

                                        // Uppercase the first letter. We'll go from "eXaMpLe" to "EXaMpLe".
                                        characterArray[0] = Character.toUpperCase(characterArray[0]);

                                        // Lowercase the rest, start from char 2 at pos 1. Go from "EXaMpLe" to "Example".
                                        for (int i = 1; i < characterArray.length; i++)
                                            characterArray[i] = Character.toLowerCase(characterArray[i]);

                                        updatedString = new String(characterArray) + "Alolan";
                                    }

                                    break;
                                }

                                // Generic (potential) two-word names.
                                case "OH":
                                {
                                    if (arg1String.toUpperCase().equals("HO"))
                                        updatedString = "HoOh";

                                    break;
                                }
                                case "O":
                                {
                                    switch (arg1String.toUpperCase())
                                    {
                                        case "JANGMO":
                                            updatedString = "JangmoO"; break;
                                        case "HAKAMO":
                                            updatedString = "HakamoO"; break;
                                        case "KOMMO":
                                            updatedString = "KommoO"; break;
                                    }

                                    break;
                                }
                                case "NULL":
                                {
                                    if (arg1String.toUpperCase().equals("TYPE") || arg1String.toUpperCase().equals("TYPE:"))
                                        updatedString = "TypeNull";

                                    break;
                                }
                                case "KOKO":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        updatedString = "TapuKoko";

                                    break;
                                }
                                case "LELE":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        updatedString = "TapuLele";

                                    break;
                                }
                                case "Bunu":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        updatedString = "TapuBunu";

                                    break;
                                }
                                case "Fini":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        updatedString = "TapuFini";

                                    break;
                                }
                                default:
                                {
                                    printToLog(2, "Nope, nothing here. Let's just check the first argument.");
                                    isFixed = false; // Flag as false so we don't show the second arg in our debug messages.
                                }
                            }

                            if (isFixed && !Objects.equals(updatedString, arg1String))
                            {
                                printToLog(2, "Fixable input found! Was \"§2" + arg1String + " " +
                                        arg2String + "§a\", now \"§2" + updatedString + "§a\"");

                                inputWasEdited = true;
                            }
                        }
                        else if (!Objects.equals(updatedString, arg1String))
                        {
                            printToLog(2, "Fixable input found! Was \"§2" + arg1String +
                                    "§a\", now \"§2" + updatedString + "§a\"");

                            inputWasEdited = true;
                        }

                        arg1String = updatedString;
                        returnedPokemon = PokemonMethods.getPokemonFromName(arg1String);

                        if (returnedPokemon == null)
                        {
                            // arg2String is initialized as "", so we'll only get a blank space if it's not there.
                            printToLog(1, "Could not find a Pokémon. Exit. Input was: §3" + arg1String +
                                    " " + arg2String);

                            errorString = "§4Error: §cInvalid Pokémon! Check your spelling, or try a number.";
                            canContinue = false;
                        }
                    }
                }
                else
                {
                    printToLog(1, "No arguments provided. Exit.");

                    errorString = "§4Error: §cNo arguments found. Provide a Pokémon or Dex ID.";
                    canContinue = false;
                }

                if (!canContinue)
                {
                    src.sendMessage(Text.of(errorString));
                    src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokémon name/number>"));
                }
                else
                {
                    if (inputWasEdited)
                        printToLog(2, "Moving to execution with previously-fixed input.");
                    else
                        printToLog(2, "Moving to execution with input \"§2" + arg1String + "§a\".");

                    checkTypes(returnedPokemon, inputIsInteger, arg1String, src);
                }
            }
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
    }

    private void checkTypes(final PokemonMethods returnedPokemon, final boolean inputIsInteger, final String arg1String, final CommandSource src)
    {
        // Check for differently typed forms or Alolan variants. Use fallthroughs to flag specific dex IDs as special.
        boolean hasForms = false, hasAlolanVariants = false;
        if (inputIsInteger)
        {
            switch (returnedPokemon.index) // Differently typed forms.
            {
                case 351: case 413: case 479: case 492: case 555: case 648: case 720:
                {
                    hasForms = true;
                    break;
                }
            }

            switch (returnedPokemon.index) // Alolan variants.
            {
                case 19: case 20: case 26: case 27: case 28: case 37: case 38: case 50: case 51: case 52: case 53:
                case 74: case 75: case 76: case 88: case 89: case 103: case 105:
                {
                    hasAlolanVariants = true;
                    break;
                }
            }
        }
        else
        {
            switch (arg1String.toUpperCase()) // Differently typed forms.
            {
                case "CASTFORM": case "WORMADAM": case "ROTOM": case "SHAYMIN": case "DARMANITAN": case "MELOETTA":
                case "HOOPA":
                {
                    hasForms = true;
                    break;
                }
            }

            switch (arg1String.toUpperCase()) // Alolan variants.
            {
                case "RATTATA": case "RATICATE": case "RAICHU": case "SANDSHREW": case "SANDSLASH": case "VULPIX":
                case "NINETALES": case "DIGLETT": case "DUGTRIO": case "MEOWTH": case "PERSIAN": case "GEODUDE":
                case "GRAVELER": case "GOLEM": case "GRIMER": case "MUK": case "EXEGGUTOR": case "MAROWAK":
                {
                    hasAlolanVariants = true;
                    break;
                }
            }
        }

        // Set up internal variables for (almost) EVERYTHING. Plenty of room to improve, but it'll work for now.
        boolean type2Present = true;
        final int pNumber = returnedPokemon.index;
        final char arrowChar;
        final String nameMessage, typeMessage;
        final String pName = returnedPokemon.name();
        final EnumType type1 = EnumType.parseType(returnedPokemon.type1);
        final EnumType type2 = EnumType.parseType(returnedPokemon.type2);
        if (returnedPokemon.type2.contains("EMPTY"))
            type2Present = false;

        // Decide which arrow to show. Unicode stuff tends to print as question marks in console.
        if (calledRemotely)
            arrowChar = '>';
        else
            arrowChar = '➡';

        final String typeString =
                "§fNormal, §4Fighting, §9Flying, §5Poison, §6Ground, " +
                "§7Rock, §2Bug, §5Ghost, §7Steel, §cFire, §3Water, " +
                "§aGrass, §eElectric, §dPsychic, §bIce, §9Dragon, " +
                "§8Dark, §dFairy";
        final String[] typeList = typeString.split(", ");

        final String unformattedTypeString =
                "Normal, Fighting, Flying, Poison, Ground, Rock, Bug, Ghost, Steel, " +
                "Fire, Water, Grass, Electric, Psychic, Ice, Dragon, Dark, Fairy";
        final String[] unformattedTypeList = unformattedTypeString.split(", ");

        final List<EnumType> foundTypes = new ArrayList<>();
        foundTypes.add(type1);
        final int indexType1 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type1));
        final int indexType2;
        if (type2Present)
        {
            printToLog(2, "Found two types on provided Pokémon.");
            foundTypes.add(type2);
            indexType2 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type2));

            // Used way later, but setting it up now avoids some repeated code.
            typeMessage = " §f(" + typeList[indexType1] + "§f, " + typeList[indexType2] + "§f)";
        }
        else
        {
            printToLog(2, "Found one type on provided Pokémon.");
            typeMessage = " §f(" + typeList[indexType1] + "§f)";
        }

        // Run through the big list of Pokémon and check the target's type(s).
        final StringBuilder weaknessBuilder2x = new StringBuilder();
        final StringBuilder weaknessBuilder4x = new StringBuilder();
        final StringBuilder strengthBuilder50p = new StringBuilder();
        final StringBuilder strengthBuilder25p = new StringBuilder();
        final StringBuilder immunityBuilder = new StringBuilder();

        printToLog(2, "Building the type list... Loop is go!");
        for (int i = 1; i < 19; i++)
        {
            final EnumType typeToTest = EnumType.parseType(unformattedTypeList[i - 1]);
            final float typeEffectiveness = EnumType.getTotalEffectiveness(foundTypes, typeToTest);

            if (typeEffectiveness < 1.0f)
            {
                if (typeEffectiveness == 0.5f) // 50% effectiveness
                    strengthBuilder50p.append(typeList[i - 1]).append("§f, ");
                else if (typeEffectiveness == 0.25f) // 25% effectiveness
                    strengthBuilder25p.append(typeList[i - 1]).append("§f, ");
                else if (typeEffectiveness == 0.00f) // Immune!
                    immunityBuilder.append(typeList[i - 1]).append("§f, ");
            }
            else if (typeEffectiveness > 1.0f)
            {
                if (typeEffectiveness == 2.0f) // 200% effectiveness
                    weaknessBuilder2x.append(typeList[i - 1]).append("§f, ");
                else if (typeEffectiveness == 4.0f) // 400% effectiveness, ouch!
                    weaknessBuilder4x.append(typeList[i - 1]).append("§f, ");
            }
        }

        // Fix the Pokémon's name, if necessary.
        printToLog(2, "Checking whether the Pokémon needs its shown name adjusted.");
        src.sendMessage(Text.of("§7-----------------------------------------------------"));
        switch (pName)
        {
            // Forms.
            case "CastformSunny":
                nameMessage = "§1(§9#351§1) §6Sunny Castform"; break;
            case "CastformRainy":
                nameMessage = "§1(§9#351§1) §6Rainy Castform"; break;
            case "CastformSnowy":
                nameMessage = "§1(§9#351§1) §6Snowy Castform"; break;
            case "WormadamSandy":
                nameMessage = "§1(§9#413§1) §6Sandy Wormadam"; break;
            case "WormadamTrash":
                nameMessage = "§1(§9#413§1) §6Trashy Wormadam"; break;
            case "RotomHeat":
                nameMessage = "§1(§9#479§1) §6Microwave Rotom"; break;
            case "RotomWash":
                nameMessage = "§1(§9#479§1) §6Washer Rotom"; break;
            case "RotomFrost":
                nameMessage = "§1(§9#479§1) §6Fridge Rotom"; break;
            case "RotomFan":
                nameMessage = "§1(§9#479§1) §6Fan Rotom"; break;
            case "RotomMow":
                nameMessage = "§1(§9#479§1) §6Mower Rotom"; break;
            case "ShayminSky":
                nameMessage = "§1(§9#492§1) §6Sky Shaymin"; break;
            case "DarmanitanZen":
                nameMessage = "§1(§9#555§1) §6Zen Darmanitan"; break;
            case "MeloettaPirouette":
                nameMessage = "§1(§9#648§1) §6Pirouette Meloetta"; break;
            case "HoopaUnbound":
                nameMessage = "§1(§9#720§1) §6Unbound Hoopa"; break;

            // Alolan variants.
            case "RattataAlolan":
                nameMessage = "§1(§9#19§1) §6Alolan Rattata"; break;
            case "RaticateAlolan":
                nameMessage = "§1(§9#20§1) §6Alolan Raticate"; break;
            case "RaichuAlolan":
                nameMessage = "§1(§9#26§1) §6Alolan Raichu"; break;
            case "SandshrewAlolan":
                nameMessage = "§1(§9#27§1) §6Alolan Sandshrew"; break;
            case "SandslashAlolan":
                nameMessage = "§1(§9#28§1) §6Alolan Sandslash"; break;
            case "VulpixAlolan":
                nameMessage = "§1(§9#37§1) §6Alolan Vulpix"; break;
            case "NinetalesAlolan":
                nameMessage = "§1(§9#38§1) §6Alolan Ninetales"; break;
            case "DiglettAlolan":
                nameMessage = "§1(§9#50§1) §6Alolan Diglett"; break;
            case "DugtrioAlolan":
                nameMessage = "§1(§9#51§1) §6Alolan Dugtrio"; break;
            case "MeowthAlolan":
                nameMessage = "§1(§9#52§1) §6Alolan Meowth"; break;
            case "PersianAlolan":
                nameMessage = "§1(§9#53§1) §6Alolan Persian"; break;
            case "GeodudeAlolan":
                nameMessage = "§1(§9#74§1) §6Alolan Geodude"; break;
            case "GravelerAlolan":
                nameMessage = "§1(§9#75§1) §6Alolan Graveler"; break;
            case "GolemAlolan":
                nameMessage = "§1(§9#76§1) §6Alolan Golem"; break;
            case "GrimerAlolan":
                nameMessage = "§1(§9#88§1) §6Alolan Grimer"; break;
            case "MukAlolan":
                nameMessage = "§1(§9#89§1) §6Alolan Muk"; break;
            case "ExeggutorAlolan":
                nameMessage = "§1(§9#103§1) §6Alolan Exeggutor"; break;
            case "MarowakAlolan":
                nameMessage = "§1(§9#105§1) §6Alolan Marowak"; break;

            // Pokémon with weird internal names due to technical issues.
            case "NidoranFemale":
                nameMessage = "§1(§9#29§1) §6Nidoran♀"; break;
            case "NidoranMale":
                nameMessage = "§1(§9#32§1) §6Nidoran♂"; break;
            case "Farfetchd":
                nameMessage = "§1(§9#83§1) §6Farfetch'd"; break;
            case "MrMime":
                nameMessage = "§1(§9#122§1) §6Mr. Mime"; break;
            case "HoOh":
                nameMessage = "§1(§9#250§1) §6Ho-Oh"; break;
            case "MimeJr":
                nameMessage = "§1(§9#439§1) §6Mime Jr."; break;
            case "Flabebe":
                nameMessage = "§1(§9#669§1) §6Flabébé"; break;
            case "TypeNull":
                nameMessage = "§1(§9#772§1) §6Type: Null"; break;
            case "JangmoO":
                nameMessage = "§1(§9#782§1) §6Jangmo-O"; break;
            case "HakamoO":
                nameMessage = "§1(§9#783§1) §6Hakamo-O"; break;
            case "KommoO":
                nameMessage = "§1(§9#784§1) §6Kommo-O"; break;
            case "TapuKoko":
                nameMessage = "§1(§9#784§1) §6Tapu Koko"; break;
            case "TapuLele":
                nameMessage = "§1(§9#784§1) §6Tapu Lele"; break;
            case "TapuBulu":
                nameMessage = "§1(§9#784§1) §6Tapu Bulu"; break;
            case "TapuFini":
                nameMessage = "§1(§9#784§1) §6Tapu Fini"; break;

            // Pokémon is not special, print defaults.
            default:
            {
                nameMessage = "§1(§9#" + pNumber + "§1) §6" + pName;
                printToLog(2, "Name did not need to be fixed, showing straight from the list.");
            }
        }

        src.sendMessage(Text.of(nameMessage + typeMessage));
        src.sendMessage(Text.EMPTY);

        // Get resistances, weaknesses and immunities. Print to chat.
        if (weaknessBuilder2x.length() != 0 || weaknessBuilder4x.length() != 0)
        {
            src.sendMessage(Text.of("§cWeaknesses§f:"));
            if (weaknessBuilder4x.length() != 0)
            {
                weaknessBuilder4x.setLength(weaknessBuilder4x.length() - 2); // Cut off the last comma.
                src.sendMessage(Text.of(arrowChar + " §c400%§f: " + weaknessBuilder4x));
            }
            if (weaknessBuilder2x.length() != 0)
            {
                weaknessBuilder2x.setLength(weaknessBuilder2x.length() - 2); // Cut off the last comma.
                src.sendMessage(Text.of(arrowChar + " §c200%§f: " + weaknessBuilder2x));
            }
        }

        if (strengthBuilder50p.length() != 0 || strengthBuilder25p.length() != 0)
        {
            src.sendMessage(Text.of("§aResistances§f:"));
            if (strengthBuilder50p.length() != 0)
            {
                strengthBuilder50p.setLength(strengthBuilder50p.length() - 2); // Cut off the last comma.
                src.sendMessage(Text.of(arrowChar + " §a50%§f: " + strengthBuilder50p));
            }
            if (strengthBuilder25p.length() != 0)
            {
                strengthBuilder25p.setLength(strengthBuilder25p.length() - 2); // Cut off the last comma.
                src.sendMessage(Text.of(arrowChar + " §a25%§f: " + strengthBuilder25p));
            }
        }

        // Find and format a Pokémon's type-relevant abilities.
        src.sendMessage(Text.of("§bImmunities§f:"));
        printToLog(2, "Grabbing immunities and turning them into a fancy overview.");

        // Abilities/hovers are linked. If one has two entries, the other will have two, too!
        final List<String> abilities = new ArrayList<>();
        final List<String> hovers = new ArrayList<>();

        if (immunityBuilder.length() == 0)
            immunityBuilder.append("§8None"); // Shown when a Pokémon isn't immune against anything.
        else
            immunityBuilder.setLength(immunityBuilder.length() - 2); // Shank any trailing commas.

        Text immunityStart = Text.of(arrowChar + " §b0%§f: " + immunityBuilder + "§7 (may have ");

        // Make a bunch of lists for different type-nullifying abilities.
        final String motorDrive =
                "Electivire, Blitzle, Zebstrika, Emolga";
        final String suctionCups =
                "Octillery, Lileep, Cradily, Inkay, Malamar";
        final String voltAbsorb =
                "Jolteon, Chinchou, Lanturn, Thundurus, Raikou, Minun, Pachirisu, Zeraora";
        final String stormDrain =
                "Lileep, Cradily, Shellos, Gastrodon, Finneon, Lumineon, Maractus";
        final String drySkin =
                "Paras, Parasect, Croagunk, Toxicroak, Helioptile, Heliolisk, Jynx";
        final String justified =
                "Growlithe, Arcanine, Absol, Lucario, Gallade, Cobalion, Terrakion, Virizion, Keldeo";
        final String hyperCutter =
                "Krabby, Kingler, Pinsir, Gligar, Mawile, Trapinch, Corphish, Crawdaunt, Gliscor, Crabrawler, " +
                "Crabominable";
        final String soundProof =
                "Voltorb, Electrode, MrMime, Whismur, Loudred, Exploud, MimeJr, Shieldon, Bastiodon, Snover, " +
                "Abomasnow, Bouffalant";
        final String bigPecks =
                "Pidove, Tranquill, Unfezant, Ducklett, Swanna, Vullaby, Mandibuzz, Fletchling, Pidgey, Pidgeotto, " +
                "Pidgeot, Chatot";
        final String clearBody =
                "Tentacool, Tentacruel, Beldum, Metang, Metagross, Regirock, Regice, Registeel, Carbink, Diancie, " +
                "Klink, Klang, Klinklang";
        final String sapSipper =
                "Deerling, Sawsbuck, Bouffalant, Skiddo, Gogoat, Goomy, Sliggoo, Goodra, Drampa, Marill, Azumarill, " +
                "Girafarig, Stantler, Miltank, Azurill, Blitzle, Zebstrika";
        final String damp =
                "Psyduck, Golduck, Paras, Parasect, Horsea, Seadra, Kingdra, Mudkip, Marshtomp, Swampert, Frillish, " +
                "Jellicent, Poliwag, Poliwhirl, Poliwrath, Politoed, Wooper, Quagsire";
        final String lightningRod =
                "Cubone, Marowak, Rhyhorn, Rhydon, Electrike, Manectric, Rhyperior, Blitzle, Zebstrika, Pikachu, " +
                "Raichu, Goldeen, Seaking, Zapdos, Pichu, Plusle, Sceptile, MarowakAlolan";
        final String flashFire =
                "Vulpix, Ninetales, Growlithe, Arcanine, Ponyta, Rapidash, Flareon, Houndour, Houndoom, Heatran, " +
                "Litwick, Lampent, Chandelure, Heatmor, Cyndaquil, Quilava, Typhlosion, Entei";
        final String waterAbsorb =
                "Lapras, Vaporeon, Mantine, Mantyke, Maractus, Volcanion, Chinchou, Lanturn, Suicune, Cacnea, " +
                "Cacturne, Tympole, Palpitoad, Seismitoad, Frillish, Jellicent, Poliwag, Poliwhirl, Poliwrath, " +
                "Politoed, Wooper, Quagsire";
        final String sturdy =
                "Geodude, Graveler, Golem, Magnemite, Magneton, Onix, Sudowoodo, Pineco, Forretress, Steelix, " +
                "Shuckle, Skarmory, Donphan, Nosepass, Aron, Lairon, Aggron, Shieldon, Bastiodon, Bonsly, Magnezone, " +
                "Probopass, Roggenrola, Boldore, Gigalith, Sawk, Dwebble, Crustle, Tirtouga, Carracosta, Relicanth, " +
                "Regirock, Tyrunt, Carbink, Bergmite, Avalugg";
        final String levitate =
                "Gastly, Haunter, Gengar, Koffing, Weezing, Misdreavus, Unown, Vibrava, Flygon, Lunatone, Solrock, " +
                "Baltoy, Claydol, Duskull, Chimecho, Latias, Latios, Mismagius, Chingling, Bronzor, Bronzong, " +
                "Carnivine, Rotom, RotomHeat, RotomWash, RotomFrost, RotomFan, RotomMow, Uxie, Mesprit, Azelf, " +
                "Giratina, Cresselia, Tynamo, Eelektrik, Eelektross, Cryogonal, Hydreigon, Vikavolt";

        // Check if Pokémon are on these lists. Create nice Strings to print to chat and add as hovers.
        if (motorDrive.contains(pName))
        {
            abilities.add("§f§l§nMotor Drive");
            hovers.add("§7§lMotor Drive §r§7nullifies §eElectric §7damage.");
        }
        if (suctionCups.contains(pName))
        {
            abilities.add("§f§l§nSuction Cups");
            hovers.add("§7§lSuction Cups §r§7prevents §nswitch-out§r§7 moves.");
        }
        if (voltAbsorb.contains(pName))
        {
            abilities.add("§f§l§nVolt Absorb");
            hovers.add("§7§lVolt Absorb §r§7nullifies §eElectric §7damage.");
        }
        if (stormDrain.contains(pName))
        {
            abilities.add("§f§l§nStorm Drain");
            hovers.add("§7§lStorm Drain §r§7nullifies §3Water §7damage.");
        }
        if (drySkin.contains(pName))
        {
            abilities.add("§f§l§nDry Skin");
            hovers.add("§7§lDry Skin §r§7adds 25% §3Water §7absorbance but §cFire §7hurts 25% more.");
        }
        if (justified.contains(pName))
        {
            abilities.add("§f§l§nJustified");
            hovers.add("§7§lJustified §r§7ups §nAttack§r§7 by one stage when hit by a §8Dark §7move.");
        }
        if (hyperCutter.contains(pName))
        {
            abilities.add("§f§l§nHyper Cutter");
            hovers.add("§7§lHyper Cutter §r§7prevents a Pokémon's Attack from being dropped.");
        }
        if (soundProof.contains(pName))
        {
            abilities.add("§f§l§nSoundproof");
            hovers.add("§7§lSoundproof §r§7nullifies §nsound-based§r§7 moves.");
        }
        if (bigPecks.contains(pName))
        {
            abilities.add("§f§l§nBig Pecks");
            hovers.add("§7§lBig Pecks §r§7prevents a Pokémon's Defense from being dropped.");
        }
        if (clearBody.contains(pName))
        {
            abilities.add("§f§l§nClear Body");
            hovers.add("§7§lClear Body §r§7prevents all of a Pokémon's stats from being dropped.");
        }
        if (sapSipper.contains(pName))
        {
            abilities.add("§f§l§nSap Sipper");
            hovers.add("§7§lSap Sipper §r§7nullifies §aGrass §7damage.");
        }
        if (damp.contains(pName))
        {
            abilities.add("§f§l§nDamp");
            hovers.add("§7§lDamp §r§7disables §nSelf-Destruct§r§7/§nExplosion§r§7.");
        }
        if (lightningRod.contains(pName))
        {
            abilities.add("§f§l§nLightning Rod");
            hovers.add("§7§lLightning Rod §r§7nullifies §eElectric §7damage.");
        }
        if (flashFire.contains(pName))
        {
            abilities.add("§f§l§nFlash Fire");
            hovers.add("§7§lFlash Fire §r§7nullifies §cFire §7damage.");
        }
        if (waterAbsorb.contains(pName))
        {
            abilities.add("§f§l§nWater Absorb");
            hovers.add("§7§lWater Absorb §r§7nullifies §3Water §7damage.");
        }
        if (sturdy.contains(pName))
        {
            abilities.add("§f§l§nSturdy");
            hovers.add("§7§lSturdy §r§7prevents §n1-hit KO§r§7 attacks.");
        }
        if (levitate.contains(pName))
        {
            abilities.add("§f§l§nLevitate");
            hovers.add("§7§lLevitate §r§7nullifies §eGround §7damage.");
        }

        // Check if we have certain unique Pokémon with unique abilities.
        if (pName.equals("Torkoal") || pName.equals("Heatmor"))
        {
            abilities.add("§f§l§nWhite Smoke");
            hovers.add("§7§lWhite Smoke §7provides immunity to stat reduction.");
        }
        else if (pName.contains("Shedinja"))
        {
            abilities.add("§f§l§nWonder Guard");
            hovers.add("§7§lWonder Guard §7disables all §nnon-super effective§r§7 damage.");
            immunityStart = Text.of(arrowChar + " §b0%§f: " + immunityBuilder + "§7 (has "); // Less awkward.
        }

        // Figure out what to show in chat, and how to show it.
        final Text immunityPair;
        final Text immunityPair2;
        final Text immunityPair3;
        final String immunityEnd = "§r§7)";
        switch (abilities.size())
        {
            case 1:
            {
                immunityPair = Text.builder(abilities.get(0))
                        .onHover(TextActions.showText(Text.of(hovers.get(0))))
                        .build();

                src.sendMessage(Text.of(immunityStart, immunityPair, immunityEnd));
                break;
            }
            case 2:
            {
                final Text orMessage = Text.of("§r§7 or §f§l§n");
                immunityPair = Text.builder(abilities.get(0))
                        .onHover(TextActions.showText(Text.of(hovers.get(0))))
                        .build();
                immunityPair2 = Text.builder(abilities.get(1))
                        .onHover(TextActions.showText(Text.of(hovers.get(1))))
                        .build();

                src.sendMessage(Text.of(immunityStart, immunityPair, orMessage, immunityPair2, immunityEnd));
                break;
            }
            case 3:
            {
                // Overwrite this here so we can squeeze in more info.
                // Not ideal, but not rolling over to double lines is nice.
                immunityStart = Text.of(
                        arrowChar + " §b0%§f: " + immunityBuilder + "§7 (may have type abilities, see below)");

                final Text orMessage = Text.of("§r§7 or §f§l§n");
                final Text newLineFormat = Text.of(arrowChar + " §b=>§f: ");
                immunityPair = Text.builder(abilities.get(0))
                        .onHover(TextActions.showText(Text.of(hovers.get(0))))
                        .build();
                immunityPair2 = Text.builder(abilities.get(1))
                        .onHover(TextActions.showText(Text.of(hovers.get(1))))
                        .build();
                immunityPair3 = Text.builder(abilities.get(2))
                        .onHover(TextActions.showText(Text.of(hovers.get(2))))
                        .build();

                src.sendMessage(immunityStart);
                src.sendMessage(Text.of(newLineFormat, immunityPair, orMessage, immunityPair2, orMessage, immunityPair3));
                break;
            }
            default:
                src.sendMessage(Text.of(arrowChar + " §b0%§f: " + immunityBuilder));
        }

        // Print messages if differently typed forms or Alolan forms are available.
        if (hasForms && showFormMessage)
        {
            printToLog(2, "Showing forms is enabled, and we can show one! Doing it.");
            final String commandHelper = "§cCheck out: §6/" + commandAlias + " ";

            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of("§dThis Pokémon has one or more forms with different types."));

            switch (pName)
            {
                // Big ones. We provide just the names, to keep things manageable. Rotom's super squished by necessity.
                case "Castform":
                    src.sendMessage(Text.of(commandHelper + "CastformSunny §f(or §6Rainy§f/§6Snowy§f)")); break;
                case "Wormadam":
                    src.sendMessage(Text.of(commandHelper + "WormadamSandy§f, §6WormadamTrash§f")); break;
                case "Rotom":
                    src.sendMessage(Text.of(commandHelper + "RotomHeat §f(or §6Wash§f/§6Frost§f/§6Fan§f/§6Mow§f)")); break;

                // Small ones. We can show types on these, like the Alolan variants.
                case "Shaymin":
                    src.sendMessage(Text.of(commandHelper + "ShayminSky §f(§aGrass§f, §9Flying§f)")); break;
                case "Darmanitan":
                    src.sendMessage(Text.of(commandHelper + "DarmanitanZen §f(§cFire§f, §dPsychic§f)")); break;
                case "Meloetta":
                    src.sendMessage(Text.of(commandHelper + "MeloettaPirouette §f(Normal, §4Fighting§f)")); break;
                case "Hoopa":
                    src.sendMessage(Text.of(commandHelper + "HoopaUnbound §f(§dPsychic§f, §8Dark§f)")); break;
            }
        }
        else if (hasAlolanVariants && showAlolanMessage)
        {
            printToLog(2, "Showing Alolan variants is enabled, and we've got one! Showing.");
            final String commandHelper = "§cCheck out: §6/" + commandAlias + " ";

            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of("§dThis Pokémon has an Alolan variant."));

            switch (pName)
            {
                // Alolan variants. Same as above.
                case "Rattata":
                    src.sendMessage(Text.of(commandHelper + "Alolan Rattata §f(§8Dark§f, Normal)")); break;
                case "Raticate":
                    src.sendMessage(Text.of(commandHelper + "Alolan Raticate §f(§8Dark§f, Normal)")); break;
                case "Raichu":
                    src.sendMessage(Text.of(commandHelper + "Alolan Raichu §f(§eElectric§f, §dPsychic§f)")); break;
                case "Sandshrew":
                    src.sendMessage(Text.of(commandHelper + "Alolan Sandshrew §f(§bIce§f, §7Steel§f)")); break;
                case "Sandslash":
                    src.sendMessage(Text.of(commandHelper + "Alolan Sandslash §f(§bIce§f, §7Steel§f)")); break;
                case "Vulpix":
                    src.sendMessage(Text.of(commandHelper + "Alolan Vulpix §f(§bIce§f)")); break;
                case "Ninetales":
                    src.sendMessage(Text.of(commandHelper + "Alolan Ninetales §f(§bIce§f, §dFairy§f)")); break;
                case "Diglett":
                    src.sendMessage(Text.of(commandHelper + "Alolan Diglett §f(§6Ground§f, §7Steel§f)")); break;
                case "Dugtrio":
                    src.sendMessage(Text.of(commandHelper + "Alolan Dugtrio §f(§6Ground§f, §7Steel§f)")); break;
                case "Meowth":
                    src.sendMessage(Text.of(commandHelper + "Alolan Meowth §f(§8Dark§f)")); break;
                case "Persian":
                    src.sendMessage(Text.of(commandHelper + "Alolan Persian §f(§8Dark§f)")); break;
                case "Geodude":
                    src.sendMessage(Text.of(commandHelper + "Alolan Geodude §f(§7Rock§f, §eElectric§f)")); break;
                case "Graveler":
                    src.sendMessage(Text.of(commandHelper + "Alolan Graveler §f(§7Rock§f, §eElectric§f)")); break;
                case "Golem":
                    src.sendMessage(Text.of(commandHelper + "Alolan Golem §f(§7Rock§f, §eElectric§f)")); break;
                case "Grimer":
                    src.sendMessage(Text.of(commandHelper + "Alolan Grimer §f(§5Poison§f, §8Dark§f)")); break;
                case "Muk":
                    src.sendMessage(Text.of(commandHelper + "Alolan Muk §f(§5Poison§f, §8Dark§f)")); break;
                case "Exeggutor":
                    src.sendMessage(Text.of(commandHelper + "Alolan Exeggutor §f(§aGrass§f, §9Dragon§f)")); break;
                case "Marowak":
                    src.sendMessage(Text.of(commandHelper + "Alolan Marowak §f(§cFire§f, §5Ghost§f)")); break;
            }
        }

        printToLog(1, "Went through lists, and put together a type overview. Done!");
        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}