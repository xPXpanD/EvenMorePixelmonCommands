// Thanks for the idea, ElaDiDu! Managed to squeeze this in.
package rs.expand.evenmorepixelmoncommands.commands;

import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import rs.expand.evenmorepixelmoncommands.utilities.PokemonMethods;
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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

                        // TODO: Update if new Pokémon are added!
                        if (inputInteger > 893|| inputInteger < 1)
                        {
                            // TODO: Update if new Pokémon are added!
                            printLocalError(src, "§4Error: §cInvalid Pokédex number! Valid range is 1-893.");
                            return CommandResult.empty();
                        }
                        else
                        {
                            if (EnumSpecies.getFromDex(inputInteger) == null)
                            {
                                printLocalError(src, "§4Error: §cInvalid Pokémon! It may not be in the mod yet.");
                                return CommandResult.empty();
                            }
                            else
                                enumData = PokemonMethods.getPokemonFromID(inputInteger);
                        }
                    }
                    else
                    {
                        switch (arg1String.toUpperCase())
                        {
                            // Possibly dodgy inputs and names that are different internally for technical reasons.
                            case "NIDORANF": case "FNIDORAN": case "FEMALENIDORAN": case "NIDORAN♀":
                                arg1String = "NidoranFemale"; break;
                            case "NIDORANM": case "MNIDORAN": case "MALENIDORAN": case "NIDORAN♂":
                                arg1String = "NidoranMale"; break;
                            case "FARFETCH'D": case "FARFETCHED":
                                arg1String = "Farfetchd"; break;
                            case "MR.MIME": case "MISTERMIME":
                                arg1String = "MrMime"; break;
                            case "HO-OH":
                                arg1String = "HoOh"; break;
                            case "MIMEJR.": case "MIMEJUNIOR":
                                arg1String = "MimeJr"; break;
                            case "FLABÉBÉ": case "FLABÈBÈ":
                                arg1String = "Flabebe"; break;
                            case "TYPE:NULL":
                                arg1String = "TypeNull"; break;
                            case "JANGMO-O":
                                arg1String = "JangmoO"; break;
                            case "HAKAMO-O":
                                arg1String = "HakamoO"; break;
                            case "KOMMO-O":
                                arg1String = "KommoO"; break;
                            case "SIRFETCH'D": case "SIRFETCHED":
                                arg1String = "Sirfetchd"; break;
                            case "MR.RIME": case "MISTERRIME":
                                arg1String = "MrRime"; break;
                        }

                        if (arg2Optional.isPresent())
                        {
                            // Get the contents of the second argument, if provided. Validate.
                            arg2String = arg2Optional.get();
                            switch (arg2String.toUpperCase())
                            {
                                // Alolan variants. (and Meowth, two regional variants so we handle it as a special case)
                                case "RATTATA": case "RATICATE": case "RAICHU": case "SANDSHREW": case "SANDSLASH": case "VULPIX":
                                case "MEOWTH": case "NINETALES": case "DIGLETT": case "DUGTRIO": case "PERSIAN": case "GEODUDE":
                                case "GRAVELER": case "GOLEM": case "GRIMER": case "MUK": case "EXEGGUTOR": case "MAROWAK":
                                {
                                    if (arg1String.equalsIgnoreCase("Alolan"))
                                        arg1String = arg2String + arg1String;
                                    else if (arg1String.equalsIgnoreCase("Galarian") && arg2String.equalsIgnoreCase("MEOWTH"))
                                        arg1String = arg2String + arg1String;
    
                                    break;
                                }

                                // Galarian variants.
                                case "PONYTA": case "RAPIDASH": case "SLOWPOKE": case "SLOWBRO": case "FARFETCHD":
                                case "WEEZING": case "MRMIME": case "ARTICUNO": case "ZAPDOS": case "MOLTRES": case "CORSOLA":
                                case "ZIGZAGOON": case "LINOONE": case "DARUMAKA": case "DARMANITAN": case "YAMASK": case "STUNFISK":
                                {
                                    if (arg1String.equalsIgnoreCase("Galarian"))
                                        arg1String = arg2String + arg1String;
    
                                    break;
                                }

                                // Generic (potential) two-word names.
                                case "OH":
                                {
                                    if (arg1String.toUpperCase().equals("HO"))
                                        arg1String = "HoOh";

                                    break;
                                }
                                case "O":
                                {
                                    switch (arg1String.toUpperCase())
                                    {
                                        case "JANGMO":
                                            arg1String = "JangmoO"; break;
                                        case "HAKAMO":
                                            arg1String = "HakamoO"; break;
                                        case "KOMMO":
                                            arg1String = "KommoO"; break;
                                    }

                                    break;
                                }
                                case "NULL":
                                {
                                    if (arg1String.toUpperCase().equals("TYPE") || arg1String.toUpperCase().equals("TYPE:"))
                                        arg1String = "TypeNull";

                                    break;
                                }
                                case "KOKO":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        arg1String = "TapuKoko";

                                    break;
                                }
                                case "LELE":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        arg1String = "TapuLele";

                                    break;
                                }
                                case "BULU":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        arg1String = "TapuBulu";

                                    break;
                                }
                                case "FINI":
                                {
                                    if (arg1String.toUpperCase().equals("TAPU"))
                                        arg1String = "TapuFini";

                                    break;
                                }
                            }
                        }

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
        // Set up some variables for managing forms and regional variants.
        boolean hasForms = false, hasAlolanVariant = false, hasGalarianVariant = false;

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
                    hasAlolanVariant = true;
                    break;
                }

                // Galarian variants with different yields.
                case 52: case 122: case 222:
                {
                    hasGalarianVariant = true;
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
                    hasAlolanVariant = true;
                    break;
                }

                // Galarian variants with different yields.
                case "MEOWTH": case "MRMIME": case "CORSOLA":
                {
                    hasGalarianVariant = true;
                    break;
                }
            }
        }

        // Get our Pokémon's data.
        final EnumSpecies species = EnumSpecies.getFromDex(enumData.index);

        // Check if we have a valid Species. (e.g. if the Pokémon actually exists in the mod already)
        // TODO: Remove when they're all in. This is dirty.
        if (species == null)
            printLocalError(src, "§4Error: §cInvalid Pokémon! It may not be in the mod yet.");
        else
        {
            // Figure out which form to grab EV yields from.
            // TODO: Check if Galarian EV grabbing works when we get forms that have differing EV yields.
            IEnumForm form = species.getFormEnum(enumData.form);
            final LinkedHashMap<StatsType, Integer> yields = species.getBaseStats(form).evYields;

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
                        src.sendMessage(Text.of(commandHelper + "NecrozmaDuskMane §f(or §6DawnWings§f/§6Ultra§f)")); break;
                }
            }
            else if (hasAlolanVariant)
            {
                src.sendMessage(Text.EMPTY);
                src.sendMessage(Text.of("§cAlolan found! §6/" + commandAlias + " Alolan " + enumData.name()));
            }
            else if (hasGalarianVariant)
            {
                src.sendMessage(Text.EMPTY);
                src.sendMessage(Text.of("§cGalarian found! §6/" + commandAlias + " Galarian " + enumData.name()));
            }

            src.sendMessage(Text.of("§7-----------------------------------------------------"));
        }
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