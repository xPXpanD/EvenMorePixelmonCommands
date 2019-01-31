// Thanks for the idea, ElaDiDu! Managed to squeeze this in.
package rs.expand.evenmorepixelmoncommands.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.Entity3HasStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import java.util.*;

import com.pixelmonmod.pixelmon.enums.EnumSpecies;
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
                final PokemonMethods returnedPokemon;
                boolean inputIsInteger = false;
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
                        inputIsInteger = true;
                        inputInteger = Integer.parseInt(arg1String);

                        if (inputInteger > 807 || inputInteger < 1)
                        {
                            printLocalError(src, "§4Error: §cInvalid Pokédex number! Valid range is 1-807.");
                            return CommandResult.empty();
                        }
                        else
                            returnedPokemon = PokemonMethods.getPokemonFromID(inputInteger);
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
                        returnedPokemon = PokemonMethods.getPokemonFromName(arg1String);

                        if (returnedPokemon == null)
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
                checkEVs(returnedPokemon, inputIsInteger, arg1String, src);
            }
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
    }

    // Create and print a command-specific error box that shows a provided String as the actual error.
    private void printLocalError(final CommandSource src, final String input)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(input));
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <Pokémon name/number>"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void checkEVs(final PokemonMethods returnedPokemon, final boolean inputIsInteger, final String arg1String, final CommandSource src)
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
                case "HOOPA": case "ARCEUS": case "SILVALLY": case "ORICORIO": case "NECROZMA":
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
        final int pNumber = returnedPokemon.index;
        final String nameMessage;
        final String pName = returnedPokemon.name();

        // Figure out which form to grab EV yields from.
        // TODO: Form support.
        final LinkedHashMap<StatsType, Integer> stats;
        if (hasAlolanVariants)
            stats = new BaseStats(pName, 0).evYields;
        else
            stats = new BaseStats(pName, -1).evYields;

        //BaseStats test = EnumSpecies.getFromName(pName).;

        printBasicError("stats:" + (stats == null ? null : stats.toString()));
        printBasicError("pName:" + pName);

        // Figure out specific yields.
        final Integer HPYield = stats.get(StatsType.HP);
        final int attackYield = stats.get(StatsType.Attack);
        final int defenseYield = stats.get(StatsType.Defence);
        final int spAttYield = stats.get(StatsType.SpecialAttack);
        final int spDefYield = stats.get(StatsType.SpecialDefence);
        final int speedYield = stats.get(StatsType.Speed);

        // Fix the Pokémon's shown name, if necessary.
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
            case "ArceusDragon":
                nameMessage = "§1(§9#492§1) §6Draco Plate Arceus"; break;
            case "ArceusDark":
                nameMessage = "§1(§9#492§1) §6Dread Plate Arceus"; break;
            case "ArceusGround":
                nameMessage = "§1(§9#492§1) §6Earth Plate Arceus"; break;
            case "ArceusFighting":
                nameMessage = "§1(§9#492§1) §6Fist Plate Arceus"; break;
            case "ArceusFire":
                nameMessage = "§1(§9#492§1) §6Flame Plate Arceus"; break;
            case "ArceusIce":
                nameMessage = "§1(§9#492§1) §6Icicle Plate Arceus"; break;
            case "ArceusBug":
                nameMessage = "§1(§9#492§1) §6Insect Plate Arceus"; break;
            case "ArceusSteel":
                nameMessage = "§1(§9#492§1) §6Iron Plate Arceus"; break;
            case "ArceusGrass":
                nameMessage = "§1(§9#492§1) §6Meadow Plate Arceus"; break;
            case "ArceusPsychic":
                nameMessage = "§1(§9#492§1) §6Mind Plate Arceus"; break;
            case "ArceusFairy":
                nameMessage = "§1(§9#492§1) §6Pixie Plate Arceus"; break;
            case "ArceusFlying":
                nameMessage = "§1(§9#492§1) §6Sky Plate Arceus"; break;
            case "ArceusWater":
                nameMessage = "§1(§9#492§1) §6Splash Plate Arceus"; break;
            case "ArceusGhost":
                nameMessage = "§1(§9#492§1) §6Spooky Plate Arceus"; break;
            case "ArceusRock":
                nameMessage = "§1(§9#492§1) §6Stone Plate Arceus"; break;
            case "ArceusPoison":
                nameMessage = "§1(§9#492§1) §6Toxic Plate Arceus"; break;
            case "ArceusElectric":
                nameMessage = "§1(§9#492§1) §6Zap Plate Arceus"; break;
            case "DarmanitanZen":
                nameMessage = "§1(§9#555§1) §6Zen Darmanitan"; break;
            case "MeloettaPirouette":
                nameMessage = "§1(§9#648§1) §6Pirouette Meloetta"; break;
            case "HoopaUnbound":
                nameMessage = "§1(§9#720§1) §6Unbound Hoopa"; break;
            case "OricorioBaile":
                nameMessage = "§1(§9#741§1) §6Baile Oricorio"; break;
            case "OricorioPomPom":
                nameMessage = "§1(§9#741§1) §6Pom Pom Oricorio"; break;
            case "OricorioPau":
                nameMessage = "§1(§9#741§1) §6Pa'u Oricorio"; break;
            case "OricorioSensu":
                nameMessage = "§1(§9#741§1) §6Sensu Oricorio"; break;
            case "SilvallyBug":
                nameMessage = "§1(§9#773§1) §6Bug Memory Silvally"; break;
            case "SilvallyDark":
                nameMessage = "§1(§9#773§1) §6Dark Memory Silvally"; break;
            case "SilvallyDragon":
                nameMessage = "§1(§9#773§1) §6Dragon Memory Silvally"; break;
            case "SilvallyElectric":
                nameMessage = "§1(§9#773§1) §6Electric Memory Silvally"; break;
            case "SilvallyFairy":
                nameMessage = "§1(§9#773§1) §6Fairy Memory Silvally"; break;
            case "SilvallyFighting":
                nameMessage = "§1(§9#773§1) §6Fighting Memory Silvally"; break;
            case "SilvallyFire":
                nameMessage = "§1(§9#773§1) §6Fire Memory Silvally"; break;
            case "SilvallyFlying":
                nameMessage = "§1(§9#773§1) §6Flying Memory Silvally"; break;
            case "SilvallyGhost":
                nameMessage = "§1(§9#773§1) §6Ghost Memory Silvally"; break;
            case "SilvallyGrass":
                nameMessage = "§1(§9#773§1) §6Grass Memory Silvally"; break;
            case "SilvallyGround":
                nameMessage = "§1(§9#773§1) §6Ground Memory Silvally"; break;
            case "SilvallyIce":
                nameMessage = "§1(§9#773§1) §6Ice Memory Silvally"; break;
            case "SilvallyPoison":
                nameMessage = "§1(§9#773§1) §6Poison Memory Silvally"; break;
            case "SilvallyPsychic":
                nameMessage = "§1(§9#773§1) §6Psychic Memory Silvally"; break;
            case "SilvallyRock":
                nameMessage = "§1(§9#773§1) §6Rock Memory Silvally"; break;
            case "SilvallySteel":
                nameMessage = "§1(§9#773§1) §6Steel Memory Silvally"; break;
            case "SilvallyWater":
                nameMessage = "§1(§9#773§1) §6Water Memory Silvally"; break;
            case "NecrozmaDarkMane":
                nameMessage = "§1(§9#800§1) §6Dark Mane Necrozma"; break;
            case "NecrozmaDawnWings":
                nameMessage = "§1(§9#800§1) §6Dawn Wings Necrozma"; break;
            case "NecrozmaUltra":
                nameMessage = "§1(§9#800§1) §6Ultra Necrozma"; break;

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
                nameMessage = "§1(§9#" + pNumber + "§1) §6" + pName;
        }

        // Print!
        src.sendMessage(Text.of(nameMessage));
        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of("§bHP§f: " + (HPYield != 0 ? "§a" + HPYield + " points." : "§cNone.")));
        src.sendMessage(Text.of("§bAttack§f: " + (attackYield != 0 ? "§a" + attackYield + " points." : "§cNone.")));
        src.sendMessage(Text.of("§bDefense§f: " + (defenseYield != 0 ? "§a" + defenseYield + " points." : "§cNone.")));
        src.sendMessage(Text.of("§bSpecial Attack§f: " + (spAttYield != 0 ? "§a" + spAttYield + " points." : "§cNone.")));
        src.sendMessage(Text.of("§bSpecial Defense§f: " + (spDefYield != 0 ? "§a" + spDefYield + " points." : "§cNone.")));
        src.sendMessage(Text.of("§bSpeed§f: " + (speedYield != 0 ? "§a" + speedYield + " points." : "§cNone.")));

        // Print messages if differently typed forms or Alolan forms are available.
        if (hasForms)
        {
            src.sendMessage(Text.EMPTY);

            final String commandHelper = "§cForms found! §6/" + commandAlias + " ";
            switch (pName)
            {
                // Big ones. Provide just the names to keep stuff manageable. Some of these are super squished by necessity.
                case "Castform":
                    src.sendMessage(Text.of(commandHelper + "CastformSunny §f(or §6Rainy§f/§6Snowy§f)")); break;
                case "Wormadam":
                    src.sendMessage(Text.of(commandHelper + "WormadamSandy§f, §6WormadamTrash§f")); break;
                case "Rotom":
                    src.sendMessage(Text.of(commandHelper + "RotomHeat §f(or §6Wash§f/§6Frost§f/§6Fan§f/§6Mow§f)")); break;
                case "Arceus":
                    src.sendMessage(Text.of(commandHelper + "ArceusTYPE §f(where \"§6TYPE§f\" is a type)")); break;
                case "Silvally":
                    src.sendMessage(Text.of(commandHelper + "SilvallyTYPE §f(where \"§6TYPE§f\" is a type)")); break;
                case "Necrozma":
                    src.sendMessage(Text.of(commandHelper + "NecrozmaDarkMane §f(or §6DawnWings§f/§6Ultra§f)")); break;
                case "Oricorio": // This one hurts.
                {
                    src.sendMessage(Text.of("§cShowing Baile, check: §6/" + commandAlias +
                            " OricorioPomPom §f(or §6Pau§f/§6Sensu§f)"));

                    break;
                }

                // Small ones. We can show types on these, like the Alolan variants.
                case "Shaymin":
                    src.sendMessage(Text.of(commandHelper + "ShayminSky")); break;
                case "Darmanitan":
                    src.sendMessage(Text.of(commandHelper + "DarmanitanZen")); break;
                case "Meloetta":
                    src.sendMessage(Text.of(commandHelper + "MeloettaPirouette")); break;
                case "Hoopa":
                    src.sendMessage(Text.of(commandHelper + "HoopaUnbound")); break;
            }
        }
        else if (hasAlolanVariants)
        {
            src.sendMessage(Text.EMPTY);
            src.sendMessage(Text.of("§cAlolan found! §6/" + commandAlias + " Alolan " + pName));
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}