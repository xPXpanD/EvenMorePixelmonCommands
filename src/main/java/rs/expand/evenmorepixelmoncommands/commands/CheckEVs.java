// Thanks for the idea, ElaDiDu! Managed to squeeze this in.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import java.util.*;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;
import rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;

// TODO: Apparently some of the EnumSpecies#getFromName() stuff does translation, too. Slow, but usable. Check.
public class CheckEVs implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    public static String commandAlias;

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (!(src instanceof CommandBlock))
        {
            // Validate the data we get from the command's main config.
            final List<String> commandErrorList = new ArrayList<>();
            if (commandAlias == null)
                commandErrorList.add("commandAlias");

            if (!commandErrorList.isEmpty())
            {
                PrintingMethods.printCommandNodeError("CheckEVs", commandErrorList);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                final PokemonMethods enumData;
                boolean inputIsInt = false;
                String arg1String;
                final String arg2String;
                final int inputInteger;

                // Do we have an argument in the first slot?
                if (args.<String>getOne("Pokémon name/ID").isPresent())
                {
                    arg1String = args.<String>getOne("Pokémon name/ID").get();
                    final Optional<String> arg2Optional = args.getOne("optional second word");

                    if (arg1String.matches("-?\\d+"))
                    {
                        inputIsInt = true;
                        inputInteger = Integer.parseInt(arg1String);

                        if (inputInteger > 807 || inputInteger < 1)
                        {
                            printLocalError(src, "§4Error: §cInvalid Pokédex number! Valid range is 1-807.");
                            return CommandResult.empty();
                        }
                        else
                            enumData = PokemonMethods.getPokemonFromID(inputInteger);
                    }
                    else
                    {
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
                            // Get the contents of the second argument, if provided. Validate.
                            arg2String = arg2Optional.get();
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
                                case "BULU":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        updatedString = "TapuBulu";

                                    break;
                                }
                                case "FINI":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        updatedString = "TapuFini";

                                    break;
                                }
                            }
                        }

                        arg1String = updatedString;
                        enumData = PokemonMethods.getPokemonFromName(arg1String);

                        if (enumData == null)
                        {
                            printLocalError(src, "§4Error: §cInvalid Pokémon! Check your spelling, or try a number.");
                            return CommandResult.empty();
                        }
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cNo arguments found. Provide a Pokémon or Dex ID.");
                    return CommandResult.empty();
                }

                // Let's do this thing!
                checkEVs(enumData, inputIsInt, src);
            }
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
    }

    private void checkEVs(final PokemonMethods enumData, final boolean inputIsInt, final CommandSource src)
    {
        // Set up some variables for managing forms and Alolan variants.
        boolean hasForms = false, hasAlolanVariants = false;

        // Check for forms or Alolan variants with different yields. Use fallthroughs to flag specific dex IDs as special.
        if (inputIsInt)
        {
            switch (enumData.index)
            {
                // Forms with different yields.
                case 386: case 413: case 492: case 555: case 641: case 642: case 645: case 646: case 648: case 681:
                case 774: case 800:
                {
                    hasForms = true;
                    break;
                }

                // Alolan variants with different yields.
                case 38: case 51:
                {
                    hasAlolanVariants = true;
                    break;
                }
            }
        }
        else
        {
            switch (enumData.name().toUpperCase())
            {
                // Forms with different yields.
                case "DEOXYS": case "WORMADAM": case "SHAYMIN": case "DARMANITAN": case "TORNADUS": case "THUNDURUS":
                case "LANDORUS": case "KYUREM": case "MELOETTA": case "AEGISLASH": case "MINIOR": case "NECROZMA":
                {
                    hasForms = true;
                    break;
                }

                // Alolan variants with different yields.
                case "NINETALES": case "DUGTRIO":
                {
                    hasAlolanVariants = true;
                    break;
                }
            }
        }

        // Set up internal variables for (almost) EVERYTHING. Plenty of room to improve, but it'll work for now.
        final EnumSpecies species = EnumSpecies.getFromDex(enumData.index);

        // Figure out which form to grab EV yields from.
        printBasicError("Name:" + enumData.name());
        IEnumForm form = species.getFormEnum(enumData.form);
        printBasicError("IEnumForm form:" + form);
        printBasicError("base form:" + enumData.form);
        final LinkedHashMap<StatsType, Integer> yields = species.getBaseStats(form).evYields;
        printBasicError("yields:" + (yields == null ? null : yields.toString()));

        // Get a formatted title that shows the Pokémon's ID, name and, if applicable, form name. Print.
        src.sendMessage(Text.of("§7-----------------------------------------------------"));
        src.sendMessage(Text.of(PokemonMethods.getTitleWithIDAndFormName(enumData.index, enumData.name()) + "§e EV yields:"));
        src.sendMessage(Text.EMPTY);

        // Start inserting stats, if present.
        printStatMessage(src, "HP", yields != null ? yields.get(StatsType.HP) : null);
        printStatMessage(src, "Attack", yields != null ? yields.get(StatsType.Attack) : null);
        printStatMessage(src, "Defense", yields != null ? yields.get(StatsType.Defence) : null);
        printStatMessage(src, "Sp. Attack", yields != null ? yields.get(StatsType.SpecialAttack) : null);
        printStatMessage(src, "Sp. Defense", yields != null ? yields.get(StatsType.SpecialDefence) : null);
        printStatMessage(src, "Speed", yields != null ? yields.get(StatsType.Speed) : null);

        // Print messages if forms or Alolans with different EV yields are available.
        if (hasForms)
        {
            src.sendMessage(Text.EMPTY);

            final String commandHelper = "§cForms found! §6/" + commandAlias + " ";
            switch (enumData.name())
            {
                // Some of these are super squished by necessity, but it'll do.
                case "Deoxys":
                    src.sendMessage(Text.of(commandHelper + "DeoxysAttack §f(or §6Defense§f/§6Speed§f)")); break;
                case "Wormadam":
                    src.sendMessage(Text.of(commandHelper + "WormadamSandy§f, §6WormadamTrash")); break;
                case "Shaymin":
                    src.sendMessage(Text.of(commandHelper + "ShayminSky")); break;
                case "Darmanitan":
                    src.sendMessage(Text.of(commandHelper + "DarmanitanZen")); break;
                case "Tornadus":
                    src.sendMessage(Text.of(commandHelper + "TornadusTherian")); break;
                case "Thundurus":
                    src.sendMessage(Text.of(commandHelper + "ThundurusTherian")); break;
                case "Landorus":
                    src.sendMessage(Text.of(commandHelper + "LandorusTherian")); break;
                case "Kyurem":
                    src.sendMessage(Text.of(commandHelper + "KyuremBlack§f, §6KyuremWhite")); break;
                case "Meloetta":
                    src.sendMessage(Text.of(commandHelper + "MeloettaPirouette")); break;
                case "Aegislash":
                    src.sendMessage(Text.of(commandHelper + "AegislashBlade")); break;
                case "Minior":
                    src.sendMessage(Text.of(commandHelper + "MiniorCore")); break;
                case "Necrozma":
                    src.sendMessage(Text.of(commandHelper + "NecrozmaDarkMane §f(or §6DawnWings§f/§6Ultra§f)")); break;
            }
        }
        else if (hasAlolanVariants)
        {
            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of("§cAlolan found! §6/" + commandAlias + " Alolan " + enumData.name()));
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }

    // Create and print a stat message based on the presence and value of the stat.
    private void printStatMessage(final CommandSource src, final String stat, final Integer value)
    {
        if (value != null)
            src.sendMessage(Text.of("§b" + stat + "§f: §a" + value + (value == 1 ? " point." : " points.")));
        else
            src.sendMessage(Text.of("§b" + stat + "§f: §cNone."));
    }

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokémon name/number>"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}