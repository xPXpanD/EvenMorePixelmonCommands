// Thanks for the command idea, MageFX!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.enums.EnumType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;
import rs.expand.pixelupgrade.utilities.EnumPokemonList;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

// TODO: Some super long lists like /checktypes 599 cause minor visual issues. Fixing that would be nice polish.
// TODO: Maybe look into paginated lists that you can move through. Lots of work, but would be real neat for evolutions.

public class CheckTypes implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Boolean showFormMessage;
    public static Boolean showAlolanMessage;
    public static Integer commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.doPrint("CheckTypes", false, debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            ArrayList<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (showFormMessage == null)
                nativeErrorArray.add("showFormMessage");
            if (showAlolanMessage == null)
                nativeErrorArray.add("showAlolanMessage");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                CommonMethods.printNodeError("CheckTypes", nativeErrorArray, 1);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §6" + src.getName() + "§e. Starting!");

                Player player = (Player) src;
                EnumPokemonList returnedPokemon = null;
                boolean canContinue = true, commandConfirmed = false, inputIsInteger = false;
                String inputString = null;
                int inputInteger;

                if (!args.<String>getOne("pokemon").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    checkAndAddHeader(commandCost, player);
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Provide a Pokémon or Dex ID."));
                    printCorrectHelper(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }
                else
                {
                    inputString = args.<String>getOne("pokemon").get();

                    if (inputString.matches("\\d+"))
                    {
                        printToLog(2, "Got a number, converting input into Dex ID.");

                        inputIsInteger = true;
                        inputInteger = Integer.parseInt(inputString);

                        if (inputInteger > 802 || inputInteger < 1)
                        {
                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("§4Error: §cInvalid Pokédex number! Valid range is 1-802."));
                            printCorrectHelper(commandCost, player);
                            checkAndAddFooter(commandCost, player);

                            canContinue = false;
                        }
                        else
                            returnedPokemon = EnumPokemonList.getPokemonFromID(inputInteger);
                    }
                    else
                    {
                        printToLog(2, "Checking if input is valid.");
                        String updatedString = inputString;

                        switch (inputString.toUpperCase())
                        {
                            /*                                                        *\
                                TODO: Add space support for arguments. Low priority.
                                Tapu Koko, Tapu Lele, Tapu Bunu, Tapu Fini = broken.
                                Passing something like "tapukoko" should still work!
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
                            case "FLABÉBÉ": case "FLABÈBÈ":
                                updatedString = "Flabebe"; break;
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
                            printToLog(2, "Found a fixable input! Original: \"§2" +
                                    inputString + "§a\", changed to: \"§2" + updatedString + "§a\"");

                        inputString = updatedString;
                        returnedPokemon = EnumPokemonList.getPokemonFromName(inputString);

                        if (returnedPokemon == null)
                        {
                            printToLog(1, "Could not find a Pokémon. Exit. Input was: §2" + inputString);

                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("§4Error: §cInvalid Pokémon! Check spelling, or try a number."));
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
                    printToLog(2, "Everything checks out, running code on input!");

                    if (commandCost > 0)
                    {
                        BigDecimal costToConfirm = new BigDecimal(commandCost);

                        if (commandConfirmed)
                        {
                            Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                UniqueAccount uniqueAccount = optionalAccount.get();
                                TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                if (transactionResult.getResult() == ResultType.SUCCESS)
                                {
                                    printToLog(1, "Checked Pokémon for input string \"§6" +
                                            inputString + "§e\", and took §6" + costToConfirm + "§e coins.");
                                    checkTypes(returnedPokemon, inputIsInteger, inputString, player);
                                }
                                else
                                {
                                    BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                    printToLog(1, "Not enough coins! Cost: §6" + costToConfirm +
                                            "§e, lacking: §6" + balanceNeeded);

                                    src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
                                }
                            }
                            else
                            {
                                printToLog(0, "§4" + src.getName() +
                                        "§c does not have an economy account, aborting. May be a bug?");
                                src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                            }
                        }
                        else
                        {
                            printToLog(1, "Got cost but no confirmation; end of the line. Exit.");

                            src.sendMessage(Text.of("§6Warning: §eChecking a Pokémon's type stats costs §6" +
                                    costToConfirm + "§e coins."));
                            src.sendMessage(Text.of("§2Ready? Type: §a" + commandAlias + " " + inputString + " -c"));
                        }
                    }
                    else
                    {
                        printToLog(1, "Checked Pokémon for input string \"§6" + inputString +
                                "§e\". Config price is §60§e, taking nothing.");
                        checkTypes(returnedPokemon, inputIsInteger, inputString, player);
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void checkAndAddHeader(int cost, Player player)
    {
        if (cost > 0)
            player.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coins."));
            player.sendMessage(Text.of("§5-----------------------------------------------------"));
        }
    }

    private void printCorrectHelper(int cost, Player player)
    {
        if (cost != 0)
            player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <Pokémon name/number> {-c to confirm}"));
        else
            player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <Pokémon name/number>"));
    }

    private void checkTypes(EnumPokemonList returnedPokemon, boolean inputIsInteger, String inputString, Player player)
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
                "§fNormal, §4Fighting, §9Flying, §5Poison, §6Ground, " +
                "§7Rock, §2Bug, §5Ghost, §7Steel, §cFire, §3Water, " +
                "§aGrass, §eElectric, §dPsychic, §bIce, §9Dragon, " +
                "§8Dark, §dFairy";
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
            printToLog(2, "Found two types on provided Pokémon.");
            foundTypes.add(type2);
            indexType2 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type2));

            // Used way later, but setting it up now avoids some repeat code.
            typeMessage = " §f(" + typeList[indexType1] + "§f, " + typeList[indexType2] + "§f)";
        }
        else
        {
            printToLog(2, "Found one type on provided Pokémon.");
            typeMessage = " §f(" + typeList[indexType1] + "§f)";
        }

        /*                                                                         *\
             Run through the big list of Pokémon and check the target's type(s).
        \*                                                                         */
        StringBuilder weaknessBuilder2x = new StringBuilder(), weaknessBuilder4x = new StringBuilder();
        StringBuilder strengthBuilder50p = new StringBuilder(), strengthBuilder25p = new StringBuilder();
        StringBuilder immunityBuilder = new StringBuilder();

        printToLog(2, "Building the type list... Loop is go!");
        for (int i = 1; i < 19; i++)
        {
            EnumType typeToTest = EnumType.parseType(unformattedTypeList[i - 1]);
            float typeEffectiveness = EnumType.getTotalEffectiveness(foundTypes, typeToTest);

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

        /*                                                 *\
             Fix the shown Pokémon's name, if necessary.
        \*                                                 */
        printToLog(2, "Checking whether the Pokémon needs its shown name adjusted.");
        player.sendMessage(Text.of("§7-----------------------------------------------------"));
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
                nameMessage = "§1(§9#29§1) §6Nidoran \u2640"; break; // Female symbol
            case "NidoranMale":
                nameMessage = "§1(§9#32§1) §6Nidoran \u2642"; break; // Male symbol
            case "Farfetchd":
                nameMessage = "§1(§9#83§1) §6Farfetch'd"; break;
            case "MrMime":
                nameMessage = "§1(§9#122§1) §6Mr. Mime"; break;
            case "MimeJr":
                nameMessage = "§1(§9#439§1) §6Mime Jr."; break;
            case "Flabebe":
                nameMessage = "§1(§9#669§1) §6Flabébé"; break; // é
            case "TypeNull":
                nameMessage = "§1(§9#772§1) §6Type: Null"; break;
            case "JangmoO":
                nameMessage = "§1(§9#782§1) §6Jangmo-O"; break;
            case "HakamoO":
                nameMessage = "§1(§9#783§1) §6Hakamo-O"; break;
            case "KommoO":
                nameMessage = "§1(§9#784§1) §6Kommo-O"; break;

            // Pokémon is not special, print defaults.
            default:
                nameMessage = "§1(§9#" + pNumber + "§1) §6" + pName;
                printToLog(2, "Name did not need to be fixed, showing straight from the list.");
        }

        player.sendMessage(Text.of(nameMessage + typeMessage));
        player.sendMessage(Text.of(""));

        /*                                                                *\
             Get resistances, weaknesses and immunities. Print to chat.
        \*                                                                */
        if (weaknessBuilder2x.length() != 0 || weaknessBuilder4x.length() != 0)
        {
            player.sendMessage(Text.of("§cWeaknesses§6:"));
            if (weaknessBuilder4x.length() != 0)
            {
                weaknessBuilder4x.setLength(weaknessBuilder4x.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- §c400%§f: " + weaknessBuilder4x));
            }
            if (weaknessBuilder2x.length() != 0)
            {
                weaknessBuilder2x.setLength(weaknessBuilder2x.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- §c200%§f: " + weaknessBuilder2x));
            }
        }

        if (strengthBuilder50p.length() != 0 || strengthBuilder25p.length() != 0)
        {
            player.sendMessage(Text.of("§aResistances§6:"));
            if (strengthBuilder50p.length() != 0)
            {
                strengthBuilder50p.setLength(strengthBuilder50p.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- §a50%§f: " + strengthBuilder50p));
            }
            if (strengthBuilder25p.length() != 0)
            {
                strengthBuilder25p.setLength(strengthBuilder25p.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- §a25%§f: " + strengthBuilder25p));
            }
        }

        /*                                                          *\
             Find and format a Pokémon's type-relevant abilities.
        \*                                                          */
        player.sendMessage(Text.of("§bImmunities§6:"));
        printToLog(2, "Grabbing immunities and turning them into a fancy list.");

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
            immunityBuilder.append("§8None"); // Shown when a Pokémon isn't immune against anything.
        else
            immunityBuilder.setLength(immunityBuilder.length() - 2); // Shank any trailing commas.

        Text immunityStart = Text.of("\\- §b0%§f: " + immunityBuilder + "§7 (may have ");

        // Check if Pokémon are on certain lists. Create nice Strings to print to chat and add as hovers.
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
        if (pName.matches("Torkoal|Heatmor")) // Regular expressions! Woo!
        {
            abilities.add("§f§l§nWhite Smoke");
            hovers.add("§7§lWhite Smoke §7provides immunity to stat reduction.");
        }
        else if (pName.contains("Shedinja"))
        {
            abilities.add("§f§l§nWonder Guard");
            hovers.add("§7§lWonder Guard §7disables all §nnon-super effective§r§7 damage.");
            immunityStart = Text.of("\\- §b0%§f: " + immunityBuilder + "§7 (has "); // Less awkward.
        }

        /*                                                          *\
             Figure out what to show in chat, and how to show it.
        \*                                                          */
        Text immunityPair, immunityPair2, immunityPair3;
        String immunityEnd = "§r§7)";
        switch (abilities.size())
        {
            case 1:
            {
                immunityPair = Text.builder(abilities.get(0))
                        .onHover(TextActions.showText(Text.of(hovers.get(0))))
                        .build();

                player.sendMessage(Text.of(immunityStart, immunityPair, immunityEnd));
                break;
            }
            case 2:
            {
                Text orMessage = Text.of("§r§7 or §f§l§n");
                immunityPair = Text.builder(abilities.get(0))
                        .onHover(TextActions.showText(Text.of(hovers.get(0))))
                        .build();
                immunityPair2 = Text.builder(abilities.get(1))
                        .onHover(TextActions.showText(Text.of(hovers.get(1))))
                        .build();

                player.sendMessage(Text.of(immunityStart, immunityPair, orMessage, immunityPair2, immunityEnd));
                break;
            }
            case 3:
            {
                // Overwrite this here so we can squeeze in more info.
                // Not ideal, but not rolling over to double lines is nice.
                immunityStart = Text.of("\\- §b0%§f: " + immunityBuilder + "§7 (may have type abilities, see below)");

                Text orMessage = Text.of("§r§7 or §f§l§n");
                Text newLineFormat = Text.of("\\- §b=>§f: ");
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
                player.sendMessage(Text.of(newLineFormat, immunityPair, orMessage, immunityPair2, orMessage, immunityPair3));
                break;
            }
            default:
                player.sendMessage(Text.of("\\- §b0%§f: " + immunityBuilder));
        }

        /*                                                                              *\
             Print messages if differently typed forms or Alolan forms are available.
        \*                                                                              */
        if (hasForms && showFormMessage)
        {
            printToLog(2, "Showing forms is enabled, and we can show one! Doing it.");
            String commandHelper = "§cCheck out: §6" + commandAlias + " ";

            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("§dThis Pokémon has one or more forms with different types."));

            switch (pName)
            {
                // Big ones. We provide just the names, to keep things manageable. Rotom's super squished by necessity.
                case "Castform":
                    player.sendMessage(Text.of(commandHelper + "CastformSunny §f(or §6Rainy§f/§6Snowy§f)")); break;
                case "Wormadam":
                    player.sendMessage(Text.of(commandHelper + "WormadamSandy§f, §6WormadamTrash§f")); break;
                case "Rotom":
                    player.sendMessage(Text.of(commandHelper + "RotomHeat §f(or §6Wash§f/§6Frost§f/§6Fan§f/§6Mow§f)")); break;

                // Small ones. We can show types on these, like the Alolan variants.
                case "Shaymin":
                    player.sendMessage(Text.of(commandHelper + "ShayminSky §f(§aGrass§f, §9Flying§f)")); break;
                case "Darmanitan":
                    player.sendMessage(Text.of(commandHelper + "DarmanitanZen §f(§cFire§f, §dPsychic§f)")); break;
                case "Meloetta":
                    player.sendMessage(Text.of(commandHelper + "MeloettaPirouette §f(Normal, §4Fighting§f)")); break;
                case "Hoopa":
                    player.sendMessage(Text.of(commandHelper + "HoopaUnbound §f(§dPsychic§f, §8Dark§f)")); break;
            }
        }
        else if (hasAlolanVariants && showAlolanMessage)
        {
            printToLog(2, "Showing Alolan variants is enabled, and we've got one! Showing.");
            String commandHelper = "§cCheck out: §6" + commandAlias + " ";

            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("§dThis Pokémon has an Alolan variant."));

            switch (pName)
            {
                // Alolan variants. Same as above.
                case "Rattata":
                    player.sendMessage(Text.of(commandHelper + "RattataAlolan §f(§8Dark§f, Normal)")); break;
                case "Raticate":
                    player.sendMessage(Text.of(commandHelper + "RaticateAlolan §f(§8Dark§f, Normal)")); break;
                case "Raichu":
                    player.sendMessage(Text.of(commandHelper + "RaichuAlolan §f(§eElectric§f, §dPsychic§f)")); break;
                case "Sandshrew":
                    player.sendMessage(Text.of(commandHelper + "SandshrewAlolan §f(§bIce§f, §7Steel§f)")); break;
                case "Sandslash":
                    player.sendMessage(Text.of(commandHelper + "SandslashAlolan §f(§bIce§f, §7Steel§f)")); break;
                case "Vulpix":
                    player.sendMessage(Text.of(commandHelper + "VulpixAlolan §f(§bIce§f)")); break;
                case "Ninetales":
                    player.sendMessage(Text.of(commandHelper + "NinetalesAlolan §f(§bIce§f, §dFairy§f)")); break;
                case "Diglett":
                    player.sendMessage(Text.of(commandHelper + "DiglettAlolan §f(§6Ground§f, §7Steel§f)")); break;
                case "Dugtrio":
                    player.sendMessage(Text.of(commandHelper + "DugtrioAlolan §f(§6Ground§f, §7Steel§f)")); break;
                case "Meowth":
                    player.sendMessage(Text.of(commandHelper + "MeowthAlolan §f(§8Dark§f)")); break;
                case "Persian":
                    player.sendMessage(Text.of(commandHelper + "PersianAlolan §f(§8Dark§f)")); break;
                case "Geodude":
                    player.sendMessage(Text.of(commandHelper + "GeodudeAlolan §f(§7Rock§f, §eElectric§f)")); break;
                case "Graveler":
                    player.sendMessage(Text.of(commandHelper + "GravelerAlolan §f(§7Rock§f, §eElectric§f)")); break;
                case "Golem":
                    player.sendMessage(Text.of(commandHelper + "GolemAlolan §f(§7Rock§f, §eElectric§f)")); break;
                case "Grimer":
                    player.sendMessage(Text.of(commandHelper + "GrimerAlolan §f(§5Poison§f, §8Dark§f)")); break;
                case "Muk":
                    player.sendMessage(Text.of(commandHelper + "MukAlolan §f(§5Poison§f, §8Dark§f)")); break;
                case "Exeggutor":
                    player.sendMessage(Text.of(commandHelper + "ExeggutorAlolan §f(§aGrass§f, §9Dragon§f)")); break;
                case "Marowak":
                    player.sendMessage(Text.of(commandHelper + "MarowakAlolan §f(§cFire§f, §5Ghost§f)")); break;
            }
        }

        printToLog(1, "Successfully went through lists, and put together a type overview. Done!");
        player.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}