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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.pixelmonmod.pixelmon.enums.EnumType.getTotalEffectiveness;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;
import static rs.expand.pixelupgrade.commands.CheckTypes.EnumPokemonList.getPokemonFromName;
import static rs.expand.pixelupgrade.commands.CheckTypes.EnumPokemonList.getPokemonFromID;

// TODO: Some super long lists like /checktypes 599 cause minor visual issues. Fixing that would be nice polish.
// TODO: Maybe look into paginated lists that you can move through. Lots of work, but would be real neat for evolutions.

// Thanks for the command idea, MageFX!

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

            // Check the command's debug verbosity mode, as set in the config.
            getVerbosityMode();

            if (showFormMessage == null || showAlolanMessage == null || commandCost == null)
                presenceCheck = false;

            if (!presenceCheck || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74CheckTypes // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pu reload\\u00A7c.");
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
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
                    printCorrectHelper(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }
                else
                {
                    inputString = args.<String>getOne("pokemon").get();

                    if (inputString.matches("\\d+"))
                    {
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
                            returnedPokemon = getPokemonFromID(inputInteger);
                    }
                    else
                    {
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
                                inputString = "NidoranFemale"; break;
                            case "NIDORANM": case "MNIDORAN": case "MALENIDORAN": case "NIDORAN♂":
                                inputString = "NidoranMale"; break;
                            case "FARFETCH'D": case "FARFETCHED":
                                inputString = "Farfetchd"; break;
                            case "MR.MIME": case "MISTERMIME":
                                inputString = "MrMime"; break;
                            case "MIMEJR.": case "MIMEJUNIOR":
                                inputString = "MimeJr"; break;
                            //case "FLABÉBÉ": case "FLABÈBÈ":
                            //    inputString = "Flabebe"; break;
                            case "TYPE:NULL": case "TYPE:": case "TYPE": // A bit cheeky, but nothing else starts with "type" right now.
                                inputString = "TypeNull"; break;
                            case "JANGMO-O":
                                inputString = "JangmoO"; break;
                            case "HAKAMO-O":
                                inputString = "HakamoO"; break;
                            case "KOMMO-O":
                                inputString = "KommoO"; break;
                        }

                        returnedPokemon = getPokemonFromName(inputString);
                        if (returnedPokemon == null)
                        {
                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9mon or Pok\u00E9dex number!"));
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
                                    printToLog(1, "Checked Pokémon for input string \"" + inputString + "\", and took " + costToConfirm + " coins.");
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
                            src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a/checktypes " + inputString + " -c"));
                        }
                    }
                    else
                    {
                        printToLog(2, "Checked Pokémon for input string \"" + inputString + "\". Config price is 0, taking nothing.");
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
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/checktypes <Pok\u00E9mon name/number> {-c to confirm}"));
        else
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/checktypes <Pok\u00E9mon name/number>"));
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
        // Combo flags.
        boolean hasForms = true, hasAlolanVariants = true;

        // Let's see if we have any forms or Alolan variants. Yes, this is a bit odd.
        // It should be super fast, though. Beats using thousands of booleans. Gotta love fallthroughs!
        if (inputIsInteger)
        {
            switch (returnedPokemon.index) // Differently typed forms.
            {
                case 351: case 413: case 479: case 492: case 555: case 648: case 720: break;
                default: hasForms = false;
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

        boolean type2Present = true, needsAbilityMessage = false;
        boolean hasLevitate = false, hasLightningRod = false, hasMotorDrive = false, hasSapSipper = false;
        boolean hasStormDrain = false, hasVoltAbsorb = false, hasWaterAbsorb = false, hasFlashFire = false;

        int pNumber = returnedPokemon.index;
        String pName = returnedPokemon.name();

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

        String levitateString = // Which Pokémon have Levitate, and are thusly immune to Ground?
                "Gastly, Haunter, Gengar, Koffing, Weezing, Misdreavus, Unown, Vibrava, Flygon, " +
                "Lunatone, Solrock, Baltoy, Claydol, Duskull, Chimecho, Latias, Latios, Mismagius, " +
                "Chingling, Bronzor, Bronzong, Carnivine, Rotom, RotomHeat, RotomWash, RotomFrost, " +
                "RotomFan, RotomMow, Uxie, Mesprit, Azelf, Giratina, Cresselia, Tynamo, Eelektrik, " +
                "Eelektross, Cryogonal, Hydreigon, Vikavolt";

        String lightningRodString = // Which Pokémon have Lightning Rod, and are thusly immune to Electric?
                "Cubone, Marowak, Rhyhorn, Rhydon, Electrike, Manectric, Rhyperior, Blitzle, " +
                "Zebstrika, Pikachu, Raichu, Goldeen, Seaking, Zapdos, Pichu, Plusle, Sceptile " +
                "MarowakAlolan";

        String motorDriveString = // Which Pokémon have Motor Drive, and are thusly immune to Electric?
                "Electivire, Blitzle, Zebstrika, Emolga";

        String stormDrainString = // Which Pokémon have Storm Drain, and are thusly immune to Water?
                "Lileep, Cradily, Shellos, Gastrodon, Finneon, Lumineon, Maractus";

        String voltAbsorbString = // Which Pokémon have Volt Absorb, and are thusly immune to Electric?
                "Jolteon, Chinchou, Lanturn, Thundurus, Raikou, Minun, Pachirisu";

        String sapSipperString = // Which Pokémon have Sap Sipper, and are thusly immune to Grass?
                "Deerling, Sawsbuck, Bouffalant, Skiddo, Gogoat, Goomy, Sliggoo, Goodra, Drampa, " +
                "Marill, Azumarill, Girafarig, Stantler, Miltank, Azurill, Blitzle, Zebstrika";

        String waterAbsorbString = // Which Pokémon have Water Absorb, and are thusly immune to Water?
                "Poliwag, Poliwhirl, Poliwrath, Lapras, Vaporeon, Politoed, Wooper, Quagsire, " +
                "Mantine, Mantyke, Maractus, Frillish, Jellicent, Volcanion, Chinchou, Lanturn, " +
                "Suicune, Cacnea, Cacturne, Tympole, Palpitoad, Seismitoad";

        String flashFireString = // Which Pokémon have Flash Fire, and are thusly immune to Fire?
                "Vulpix, Ninetales, Growlithe, Arcanine, Ponyta, Rapidash, Flareon, Houndour, " +
                "Houndoom, Heatran, Litwick, Lampent, Chandelure, Heatmor, Cyndaquil, Quilava, " +
                "Typhlosion, Entei";

        if (levitateString.contains(pName))
            hasLevitate = true;
        if (lightningRodString.contains(pName))
            hasLightningRod = true;
        if (motorDriveString.contains(pName))
            hasMotorDrive = true;
        if (sapSipperString.contains(pName))
            hasSapSipper = true;
        if (stormDrainString.contains(pName))
            hasStormDrain = true;
        if (voltAbsorbString.contains(pName))
            hasVoltAbsorb = true;
        if (waterAbsorbString.contains(pName))
            hasWaterAbsorb = true;
        if (flashFireString.contains(pName))
            hasFlashFire = true;

        if (Objects.equals(pName, "Shedinja") || hasLevitate || hasLightningRod || hasMotorDrive || hasSapSipper)
            needsAbilityMessage = true;
        if (hasStormDrain || hasVoltAbsorb || hasWaterAbsorb || hasFlashFire)
            needsAbilityMessage = true;

        EnumType type1 = EnumType.parseType(returnedPokemon.type1);
        EnumType type2 = EnumType.parseType(returnedPokemon.type2);
        if (returnedPokemon.type2.contains("EMPTY"))
            type2Present = false;

        ArrayList<EnumType> foundTypes = new ArrayList<>();
        foundTypes.add(type1);
        int indexType1 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type1)), indexType2 = 0;
        if (type2Present)
        {
            foundTypes.add(type2);
            indexType2 = Arrays.asList(unformattedTypeList).indexOf(String.valueOf(type2));
        }

        StringBuilder weaknessBuilder2x = new StringBuilder(), weaknessBuilder4x = new StringBuilder();
        StringBuilder strengthBuilder50p = new StringBuilder(), strengthBuilder25p = new StringBuilder();
        StringBuilder immunityBuilder = new StringBuilder();

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

        player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));

        String nameMessage, typeMessage;
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
        }

        if (type2Present)
            typeMessage = " \u00A7f(" + typeList[indexType1] + "\u00A7f, " + typeList[indexType2] + "\u00A7f)";
        else
            typeMessage = " \u00A7f(" + typeList[indexType1] + "\u00A7f)";

        player.sendMessage(Text.of(nameMessage + typeMessage));
        player.sendMessage(Text.of(""));

        if (weaknessBuilder2x.length() != 0 || weaknessBuilder4x.length() != 0)
        {
            player.sendMessage(Text.of("\u00A7cWeaknesses\u00A76:"));
            if (weaknessBuilder4x.length() != 0)
            {
                weaknessBuilder4x.setLength(weaknessBuilder4x.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7c400% damage\u00A7f: " + weaknessBuilder4x));
            }
            if (weaknessBuilder2x.length() != 0)
            {
                weaknessBuilder2x.setLength(weaknessBuilder2x.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7c200% damage\u00A7f: " + weaknessBuilder2x));
            }
        }

        if (strengthBuilder50p.length() != 0 || strengthBuilder25p.length() != 0)
        {
            player.sendMessage(Text.of("\u00A7aResistances\u00A76:"));
            if (strengthBuilder50p.length() != 0)
            {
                strengthBuilder50p.setLength(strengthBuilder50p.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7a50% damage\u00A7f: " + strengthBuilder50p));
            }
            if (strengthBuilder25p.length() != 0)
            {
                strengthBuilder25p.setLength(strengthBuilder25p.length() - 2); // Cut off the last comma.
                player.sendMessage(Text.of("\\- \u00A7a25% damage\u00A7f: " + strengthBuilder25p));
            }
        }

        if (!needsAbilityMessage && immunityBuilder.length() != 0)
        {
            player.sendMessage(Text.of("\u00A7bImmunities\u00A76:"));
            immunityBuilder.setLength(immunityBuilder.length() - 2); // Cut off the last comma.
            player.sendMessage(Text.of("\\- \u00A7b0% damage\u00A7f: " + immunityBuilder));
        }
        else if (needsAbilityMessage)
        {
            player.sendMessage(Text.of("\u00A7bImmunities\u00A76:"));

            Text hoverText = Text.of("HOVER ERROR! PLEASE REPORT.");
            String insertionText = "INSERTION ERROR! PLEASE REPORT.";

            if (hasLevitate)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nLevitate\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lLevitate \u00A7r\u00A77nullifies damage from \u00A7eGround \u00A77moves.");
            }
            else if (hasLightningRod)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nLightning Rod\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lLightning Rod \u00A7r\u00A77nullifies damage from \u00A7eElectric \u00A77moves.");
            }
            else if (hasMotorDrive)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nMotor Drive\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lMotor Drive \u00A7r\u00A77nullifies damage from \u00A7eElectric \u00A77moves.");
            }
            else if (hasSapSipper)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nSap Sipper\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lSap Sipper \u00A7r\u00A77nullifies damage from \u00A7aGrass \u00A77moves.");
            }
            else if (hasStormDrain)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nStorm Drain\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lStorm Drain \u00A7r\u00A77nullifies damage from \u00A73Water \u00A77moves.");
            }
            else if (hasVoltAbsorb)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nVolt Absorb\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lVolt Absorb \u00A7r\u00A77nullifies damage from \u00A7eElectric \u00A77moves.");
            }
            else if (hasWaterAbsorb)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nWater Absorb\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lWater Absorb \u00A7r\u00A77nullifies damage from \u00A73Water \u00A77moves.");
            }
            else if (hasFlashFire)
            {
                insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nFlash Fire\u00A7r\u00A77)";
                hoverText = Text.of("\u00A77\u00A7lFlash Fire \u00A7r\u00A77nullifies damage from \u00A7cFire \u00A77moves.");
            }

            // Executed after the if/else if block, so it overrides that if necessary.
            // Should only work like that for Heatmor.
            switch (pName)
            {
                case "Shedinja":
                {
                    insertionText = " \u00A77(probably has \u00A7f\u00A7l\u00A7nWonder Guard\u00A7r\u00A77)";
                    hoverText = Text.of("\u00A77Makes the Pok\u00E9mon immune to attacks that aren't \u00A7nsuper effective\u00A7r\u00A77.");
                    break;
                }
                case "Torkoal":
                {
                    insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nWhite Smoke\u00A7r\u00A77)";
                    hoverText = Text.of("\u00A77Makes the Pok\u00E9mon immune to stat reduction.");
                    break;
                }
                case "Heatmor":
                {
                    insertionText = " \u00A77(may have \u00A7f\u00A7l\u00A7nWhite Smoke\u00A7r\u00A77 or \u00A7f\u00A7l\u00A7nFlash Fire\u00A7r\u00A77)";
                    hoverText = Text.of("\u00A77\u00A7lWhite Smoke \u00A7r\u00A77prevents stat lowering, "
                            + "\u00A77\u00A7lFlash Fire \u00A7r\u00A77nullifies \u00A7cFire \u00A77moves.");
                    break;
                }
            }

            if (immunityBuilder.length() == 0)
                immunityBuilder.append("\u00A78None?  "); // Two spaces added so it doesn't get shanked below.

            immunityBuilder.setLength(immunityBuilder.length() - 2); // Cut off the last comma.

            Text baseImmunityLine = Text.of("\\- \u00A7b0% damage\u00A7f: " + immunityBuilder + insertionText);
            Text immunityLine = baseImmunityLine.toBuilder().onHover(TextActions.showText(Text.of(hoverText))).build();
            player.sendMessage(immunityLine);
        }

        if (hasForms && showFormMessage)
        {
            String commandHelper = "\u00A7cCheck out: \u00A76/checktypes ";

            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A7dThis Pok\u00E9mon has one or more forms with different types."));

            switch (inputString.toUpperCase())
            {
                // Big ones. We provide just the names, to keep things manageable. Rotom's super squished by necessity.
                case "CASTFORM":
                    player.sendMessage(Text.of(commandHelper + "CastformSunny \u00A7f(or \u00A76Rainy\u00A7f/\u00A76Snowy\u00A7f)")); break;
                case "WORMADAM":
                    player.sendMessage(Text.of(commandHelper + "WormadamSandy\u00A7f, \u00A76WormadamTrash\u00A7f")); break;
                case "ROTOM":
                    player.sendMessage(Text.of(commandHelper + "RotomHeat \u00A7f(or \u00A76Wash\u00A7f/\u00A76Frost\u00A7f/\u00A76Fan\u00A7f/\u00A76Mow\u00A7f)")); break;

                // Small ones. We can show types on these, like the Alolan variants.
                case "SHAYMIN":
                    player.sendMessage(Text.of(commandHelper + "ShayminSky \u00A7f(\u00A7aGrass\u00A7f, \u00A79Flying\u00A7f)")); break;
                case "DARMANITAN":
                    player.sendMessage(Text.of(commandHelper + "DarmanitanZen \u00A7f(\u00A7cFire\u00A7f, \u00A7dPsychic\u00A7f)")); break;
                case "MELOETTA":
                    player.sendMessage(Text.of(commandHelper + "MeloettaPirouette \u00A7f(Normal, \u00A74Fighting\u00A7f)")); break;
                case "HOOPA":
                    player.sendMessage(Text.of(commandHelper + "HoopaUnbound \u00A7f(\u00A7dPsychic\u00A7f, \u00A78Dark\u00A7f)")); break;
            }
        }
        else if (hasAlolanVariants && showAlolanMessage)
        {
            String commandHelper = "\u00A7cCheck out: \u00A76/checktypes ";

            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A7dThis Pok\u00E9mon has an Alolan variant."));

            switch (inputString.toUpperCase())
            {
                // Alolan variants. Same as above.
                case "RATTATA":
                    player.sendMessage(Text.of(commandHelper + "RattataAlolan \u00A7f(\u00A78Dark\u00A7f, Normal)")); break;
                case "RATICATE":
                    player.sendMessage(Text.of(commandHelper + "RaticateAlolan \u00A7f(\u00A78Dark\u00A7f, Normal)")); break;
                case "RAICHU":
                    player.sendMessage(Text.of(commandHelper + "RaichuAlolan \u00A7f(\u00A7eElectric\u00A7f, \u00A7dPsychic\u00A7f)")); break;
                case "SANDSHREW":
                    player.sendMessage(Text.of(commandHelper + "SandshrewAlolan \u00A7f(\u00A7bIce\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "SANDSLASH":
                    player.sendMessage(Text.of(commandHelper + "SandslashAlolan \u00A7f(\u00A7bIce\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "VULPIX":
                    player.sendMessage(Text.of(commandHelper + "VulpixAlolan \u00A7f(\u00A7bIce\u00A7f)")); break;
                case "NINETALES":
                    player.sendMessage(Text.of(commandHelper + "NinetalesAlolan \u00A7f(\u00A7bIce\u00A7f, \u00A7dFairy\u00A7f)")); break;
                case "DIGLETT":
                    player.sendMessage(Text.of(commandHelper + "DiglettAlolan \u00A7f(\u00A76Ground\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "DUGTRIO":
                    player.sendMessage(Text.of(commandHelper + "DugtrioAlolan \u00A7f(\u00A76Ground\u00A7f, \u00A77Steel\u00A7f)")); break;
                case "MEOWTH":
                    player.sendMessage(Text.of(commandHelper + "MeowthAlolan \u00A7f(\u00A78Dark\u00A7f)")); break;
                case "PERSIAN":
                    player.sendMessage(Text.of(commandHelper + "PersianAlolan \u00A7f(\u00A78Dark\u00A7f)")); break;
                case "GEODUDE":
                    player.sendMessage(Text.of(commandHelper + "GeodudeAlolan \u00A7f(\u00A77Rock\u00A7f, \u00A7eElectric\u00A7f)")); break;
                case "GRAVELER":
                    player.sendMessage(Text.of(commandHelper + "GravelerAlolan \u00A7f(\u00A77Rock\u00A7f, \u00A7eElectric\u00A7f)")); break;
                case "GOLEM":
                    player.sendMessage(Text.of(commandHelper + "GolemAlolan \u00A7f(\u00A77Rock\u00A7f, \u00A7eElectric\u00A7f)")); break;
                case "GRIMER":
                    player.sendMessage(Text.of(commandHelper + "GrimerAlolan \u00A7f(\u00A75Poison\u00A7f, \u00A78Dark\u00A7f)")); break;
                case "MUK":
                    player.sendMessage(Text.of(commandHelper + "MukAlolan \u00A7f(\u00A75Poison\u00A7f, \u00A78Dark\u00A7f)")); break;
                case "EXEGGUTOR":
                    player.sendMessage(Text.of(commandHelper + "ExeggutorAlolan \u00A7f(\u00A7aGrass\u00A7f, \u00A79Dragon\u00A7f)")); break;
                case "MAROWAK":
                    player.sendMessage(Text.of(commandHelper + "MarowakAlolan \u00A7f(\u00A7cFire\u00A7f, \u00A75Ghost\u00A7f)")); break;
            }
        }

        player.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
    }

    public enum EnumPokemonList
    {
        // Gen 1
        Bulbasaur(1, "Grass, Poison"),
        Ivysaur(2, "Grass, Poison"),
        Venusaur(3, "Grass, Poison"),
        Charmander(4, "Fire"),
        Charmeleon(5, "Fire"),
        Charizard(6, "Fire, Flying"),
        Squirtle(7, "Water"),
        Wartortle(8, "Water"),
        Blastoise(9, "Water"),
        Caterpie(10, "Bug"),
        Metapod(11, "Bug"),
        Butterfree(12, "Bug, Flying"),
        Weedle(13, "Bug, Poison"),
        Kakuna(14, "Bug, Poison"),
        Beedrill(15, "Bug, Poison"),
        Pidgey(16, "Normal, Flying"),
        Pidgeotto(17, "Normal, Flying"),
        Pidgeot(18, "Normal, Flying"),
        Rattata(19, "Normal"),
        Raticate(20, "Normal"),
        Spearow(21, "Normal, Flying"),
        Fearow(22, "Normal, Flying"),
        Ekans(23, "Poison"),
        Arbok(24, "Poison"),
        Pikachu(25, "Electric"),
        Raichu(26, "Electric"),
        Sandshrew(27, "Ground"),
        Sandslash(28, "Ground"),
        NidoranFemale(29, "Poison"),
        Nidorina(30, "Poison"),
        Nidoqueen(31, "Poison, Ground"),
        NidoranMale(32, "Poison"),
        Nidorino(33, "Poison"),
        Nidoking(34, "Poison, Ground"),
        Clefairy(35, "Fairy"),
        Clefable(36, "Fairy"),
        Vulpix(37, "Fire"),
        Ninetales(38, "Fire"),
        Jigglypuff(39, "Normal, Fairy"),
        Wigglytuff(40, "Normal, Fairy"),
        Zubat(41, "Poison, Flying"),
        Golbat(42, "Poison, Flying"),
        Oddish(43, "Grass, Poison"),
        Gloom(44, "Grass, Poison"),
        Vileplume(45, "Grass, Poison"),
        Paras(46, "Bug, Grass"),
        Parasect(47, "Bug, Grass"),
        Venonat(48, "Bug, Poison"),
        Venomoth(49, "Bug, Poison"),
        Diglett(50, "Ground"),
        Dugtrio(51, "Ground"),
        Meowth(52, "Normal"),
        Persian(53, "Normal"),
        Psyduck(54, "Water"), // so tempted
        Golduck(55, "Water"),
        Mankey(56, "Fighting"),
        Primeape(57, "Fighting"),
        Growlithe(58, "Fire"),
        Arcanine(59, "Fire"),
        Poliwag(60, "Water"),
        Poliwhirl(61, "Water"),
        Poliwrath(62, "Water, Fighting"),
        Abra(63, "Psychic"),
        Kadabra(64, "Psychic"),
        Alakazam(65, "Psychic"),
        Machop(66, "Fighting"),
        Machoke(67, "Fighting"),
        Machamp(68, "Fighting"),
        Bellsprout(69, "Grass, Poison"),
        Weepinbell(70, "Grass, Poison"),
        Victreebel(71, "Grass, Poison"),
        Tentacool(72, "Water, Poison"),
        Tentacruel(73, "Water, Poison"),
        Geodude(74, "Rock, Ground"),
        Graveler(75, "Rock, Ground"),
        Golem(76, "Rock, Ground"),
        Ponyta(77, "Fire"),
        Rapidash(78, "Fire"),
        Slowpoke(79, "Water, Psychic"),
        Slowbro(80, "Water, Psychic"),
        Magnemite(81, "Electric, Steel"),
        Magneton(82, "Electric, Steel"),
        Farfetchd(83, "Normal, Flying"),
        Doduo(84, "Normal, Flying"),
        Dodrio(85, "Normal, Flying"),
        Seel(86, "Water"),
        Dewgong(87, "Water, Ice"),
        Grimer(88, "Poison"),
        Muk(89, "Poison"),
        Shellder(90, "Water"),
        Cloyster(91, "Water, Ice"),
        Gastly(92, "Ghost, Poison"),
        Haunter(93, "Ghost, Poison"),
        Gengar(94, "Ghost, Poison"),
        Onix(95, "Rock, Ground"),
        Drowzee(96, "Psychic"),
        Hypno(97, "Psychic"),
        Krabby(98, "Water"),
        Kingler(99, "Water"),
        Voltorb(100, "Electric"),
        Electrode(101, "Electric"),
        Exeggcute(102, "Grass, Psychic"),
        Exeggutor(103, "Grass, Psychic"),
        Cubone(104, "Ground"),
        Marowak(105, "Ground"),
        Hitmonlee(106, "Fighting"),
        Hitmonchan(107, "Fighting"),
        Lickitung(108, "Normal"),
        Koffing(109, "Poison"),
        Weezing(110, "Poison"),
        Rhyhorn(111, "Ground, Rock"),
        Rhydon(112, "Ground, Rock"),
        Chansey(113, "Normal"),
        Tangela(114, "Grass"),
        Kangaskhan(115, "Normal"),
        Horsea(116, "Water"),
        Seadra(117, "Water"),
        Goldeen(118, "Water"),
        Seaking(119, "Water"),
        Staryu(120, "Water"),
        Starmie(121, "Water, Psychic"),
        MrMime(122, "Psychic, Fairy"),
        Scyther(123, "Bug, Flying"),
        Jynx(124, "Ice, Psychic"),
        Electabuzz(125, "Electric"),
        Magmar(126, "Fire"),
        Pinsir(127, "Bug"),
        Tauros(128, "Normal"),
        Magikarp(129, "Water"),
        Gyarados(130, "Water, Flying"),
        Lapras(131, "Water, Ice"),
        Ditto(132, "Normal"),
        Eevee(133, "Normal"),
        Vaporeon(134, "Water"),
        Jolteon(135, "Electric"),
        Flareon(136, "Fire"),
        Porygon(137, "Normal"),
        Omanyte(138, "Rock, Water"),
        Omastar(139, "Rock, Water"),
        Kabuto(140, "Rock, Water"),
        Kabutops(141, "Rock, Water"),
        Aerodactyl(142, "Rock, Flying"),
        Snorlax(143, "Normal"),
        Articuno(144, "Ice, Flying"),
        Zapdos(145, "Electric, Flying"),
        Moltres(146, "Fire, Flying"),
        Dratini(147, "Dragon"),
        Dragonair(148, "Dragon"),
        Dragonite(149, "Dragon, Flying"),
        Mewtwo(150, "Psychic"),
        Mew(151, "Psychic"),

        // Gen 2 (also known as best gen)
        Chikorita(152, "Grass"),
        Bayleef(153, "Grass"),
        Meganium(154, "Grass"),
        Cyndaquil(155, "Fire"),
        Quilava(156, "Fire"),
        Typhlosion(157, "Fire"),
        Totodile(158, "Water"),
        Croconaw(159, "Water"),
        Feraligatr(160, "Water"),
        Sentret(161, "Normal"),
        Furret(162, "Normal"),
        Hoothoot(163, "Normal, Flying"),
        Noctowl(164, "Normal, Flying"),
        Ledyba(165, "Bug, Flying"),
        Ledian(166, "Bug, Flying"),
        Spinarak(167, "Bug, Poison"),
        Ariados(168, "Bug, Poison"),
        Crobat(169, "Poison, Flying"),
        Chinchou(170, "Water, Electric"),
        Lanturn(171, "Water, Electric"),
        Pichu(172, "Electric"),
        Cleffa(173, "Fairy"),
        Igglybuff(174, "Normal, Fairy"),
        Togepi(175, "Fairy"),
        Togetic(176, "Fairy, Flying"),
        Natu(177, "Psychic, Flying"),
        Xatu(178, "Psychic, Flying"),
        Mareep(179, "Electric"),
        Flaaffy(180, "Electric"),
        Ampharos(181, "Electric"),
        Bellossom(182, "Grass"),
        Marill(183, "Water, Fairy"),
        Azumarill(184, "Water, Fairy"),
        Sudowoodo(185, "Rock"),
        Politoed(186, "Water"),
        Hoppip(187, "Grass, Flying"),
        Skiploom(188, "Grass, Flying"),
        Jumpluff(189, "Grass, Flying"),
        Aipom(190, "Normal"),
        Sunkern(191, "Grass"),
        Sunflora(192, "Grass"),
        Yanma(193, "Bug, Flying"),
        Wooper(194, "Water, Ground"),
        Quagsire(195, "Water, Ground"),
        Espeon(196, "Psychic"),
        Umbreon(197, "Dark"),
        Murkrow(198, "Dark, Flying"),
        Slowking(199, "Water, Psychic"),
        Misdreavus(200, "Ghost"),
        Unown(201, "Psychic"),
        Wobbuffet(202, "Psychic"),
        Girafarig(203, "Normal, Psychic"),
        Pineco(204, "Bug"),
        Forretress(205, "Bug, Steel"),
        Dunsparce(206, "Normal"),
        Gligar(207, "Ground, Flying"),
        Steelix(208, "Steel, Ground"),
        Snubbull(209, "Fairy"),
        Granbull(210, "Fairy"),
        Qwilfish(211, "Water, Poison"),
        Scizor(212, "Bug, Steel"),
        Shuckle(213, "Bug, Rock"),
        Heracross(214, "Bug, Fighting"),
        Sneasel(215, "Dark, Ice"),
        Teddiursa(216, "Normal"),
        Ursaring(217, "Normal"),
        Slugma(218, "Fire"),
        Magcargo(219, "Fire, Rock"),
        Swinub(220, "Ice, Ground"),
        Piloswine(221, "Ice, Ground"),
        Corsola(222, "Water, Rock"),
        Remoraid(223, "Water"),
        Octillery(224, "Water"),
        Delibird(225, "Ice, Flying"),
        Mantine(226, "Water, Flying"),
        Skarmory(227, "Steel, Flying"),
        Houndour(228, "Dark, Fire"),
        Houndoom(229, "Dark, Fire"),
        Kingdra(230, "Water, Dragon"),
        Phanpy(231, "Ground"),
        Donphan(232, "Ground"),
        Porygon2(233, "Normal"),
        Stantler(234, "Normal"),
        Smeargle(235, "Normal"),
        Tyrogue(236, "Fighting"),
        Hitmontop(237, "Fighting"),
        Smoochum(238, "Ice, Psychic"),
        Elekid(239, "Electric"),
        Magby(240, "Fire"),
        Miltank(241, "Normal"),
        Blissey(242, "Normal"),
        Raikou(243, "Electric"),
        Entei(244, "Fire"),
        Suicune(245, "Water"),
        Larvitar(246, "Rock, Ground"),
        Pupitar(247, "Rock, Ground"),
        Tyranitar(248, "Rock, Dark"),
        Lugia(249, "Psychic, Flying"),
        HoOh(250, "Fire, Flying"),
        Celebi(251, "Psychic, Grass"),

        // Gen 3
        Treecko(252, "Grass"),
        Grovyle(253, "Grass"),
        Sceptile(254, "Grass"),
        Torchic(255, "Fire"),
        Combusken(256, "Fire, Fighting"),
        Blaziken(257, "Fire, Fighting"),
        Mudkip(258, "Water"),
        Marshtomp(259, "Water, Ground"),
        Swampert(260, "Water, Ground"),
        Poochyena(261, "Dark"),
        Mightyena(262, "Dark"),
        Zigzagoon(263, "Normal"),
        Linoone(264, "Normal"),
        Wurmple(265, "Bug"),
        Silcoon(266, "Bug"),
        Beautifly(267, "Bug, Flying"),
        Cascoon(268, "Bug"),
        Dustox(269, "Bug, Poison"),
        Lotad(270, "Water, Grass"),
        Lombre(271, "Water, Grass"),
        Ludicolo(272, "Water, Grass"),
        Seedot(273, "Grass"),
        Nuzleaf(274, "Grass, Dark"),
        Shiftry(275, "Grass, Dark"),
        Taillow(276, "Normal, Flying"),
        Swellow(277, "Normal, Flying"),
        Wingull(278, "Water, Flying"),
        Pelipper(279, "Water, Flying"),
        Ralts(280, "Psychic, Fairy"),
        Kirlia(281, "Psychic, Fairy"),
        Gardevoir(282, "Psychic, Fairy"),
        Surskit(283, "Bug, Water"),
        Masquerain(284, "Bug, Flying"),
        Shroomish(285, "Grass"),
        Breloom(286, "Grass, Fighting"),
        Slakoth(287, "Normal"),
        Vigoroth(288, "Normal"),
        Slaking(289, "Normal"),
        Nincada(290, "Bug, Ground"),
        Ninjask(291, "Bug, Flying"),
        Shedinja(292, "Bug, Ghost"),
        Whismur(293, "Normal"),
        Loudred(294, "Normal"),
        Exploud(295, "Normal"),
        Makuhita(296, "Fighting"),
        Hariyama(297, "Fighting"),
        Azurill(298, "Normal, Fairy"),
        Nosepass(299, "Rock"),
        Skitty(300, "Normal"),
        Delcatty(301, "Normal"),
        Sableye(302, "Dark, Ghost"),
        Mawile(303, "Steel, Fairy"),
        Aron(304, "Steel, Rock"),
        Lairon(305, "Steel, Rock"),
        Aggron(306, "Steel, Rock"),
        Meditite(307, "Fighting, Psychic"),
        Medicham(308, "Fighting, Psychic"),
        Electrike(309, "Electric"),
        Manectric(310, "Electric"),
        Plusle(311, "Electric"),
        Minun(312, "Electric"),
        Volbeat(313, "Bug"),
        Illumise(314, "Bug"),
        Roselia(315, "Grass, Poison"),
        Gulpin(316, "Poison"),
        Swalot(317, "Poison"),
        Carvanha(318, "Water, Dark"),
        Sharpedo(319, "Water, Dark"),
        Wailmer(320, "Water"),
        Wailord(321, "Water"),
        Numel(322, "Fire, Ground"),
        Camerupt(323, "Fire, Ground"),
        Torkoal(324, "Fire"),
        Spoink(325, "Psychic"),
        Grumpig(326, "Psychic"),
        Spinda(327, "Normal"),
        Trapinch(328, "Ground"),
        Vibrava(329, "Ground, Dragon"),
        Flygon(330, "Ground, Dragon"),
        Cacnea(331, "Grass"),
        Cacturne(332, "Grass, Dark"),
        Swablu(333, "Normal, Flying"),
        Altaria(334, "Dragon, Flying"),
        Zangoose(335, "Normal"),
        Seviper(336, "Poison"),
        Lunatone(337, "Rock, Psychic"),
        Solrock(338, "Rock, Psychic"), // Praise the sun!
        Barboach(339, "Water, Ground"),
        Whiscash(340, "Water, Ground"),
        Corphish(341, "Water"),
        Crawdaunt(342, "Water, Dark"),
        Baltoy(343, "Ground, Psychic"),
        Claydol(344, "Ground, Psychic"),
        Lileep(345, "Rock, Grass"),
        Cradily(346, "Rock, Grass"),
        Anorith(347, "Rock, Bug"),
        Armaldo(348, "Rock, Bug"),
        Feebas(349, "Water"),
        Milotic(350, "Water"),
        Castform(351, "Normal"),
        Kecleon(352, "Normal"),
        Shuppet(353, "Ghost"),
        Banette(354, "Ghost"),
        Duskull(355, "Ghost"),
        Dusclops(356, "Ghost"),
        Tropius(357, "Grass, Flying"),
        Chimecho(358, "Psychic"),
        Absol(359, "Dark"),
        Wynaut(360, "Psychic"), // Why?
        Snorunt(361, "Ice"),
        Glalie(362, "Ice"),
        Spheal(363, "Ice, Water"),
        Sealeo(364, "Ice, Water"),
        Walrein(365, "Ice, Water"),
        Clamperl(366, "Water"),
        Huntail(367, "Water"),
        Gorebyss(368, "Water"),
        Relicanth(369, "Water, Rock"),
        Luvdisc(370, "Water"),
        Bagon(371, "Dragon"),
        Shelgon(372, "Dragon"),
        Salamence(373, "Dragon, Flying"),
        Beldum(374, "Steel, Psychic"),
        Metang(375, "Steel, Psychic"),
        Metagross(376, "Steel, Psychic"),
        Regirock(377, "Rock"),
        Regice(378, "Ice"),
        Registeel(379, "Steel"),
        Latias(380, "Dragon, Psychic"),
        Latios(381, "Dragon, Psychic"),
        Kyogre(382, "Water"),
        Groudon(383, "Ground"),
        Rayquaza(384, "Dragon, Flying"),
        Jirachi(385, "Steel, Psychic"),
        Deoxys(386, "Psychic"),

        // Gen 4
        Turtwig(387, "Grass"),
        Grotle(388, "Grass"),
        Torterra(389, "Grass, Ground"),
        Chimchar(390, "Fire"),
        Monferno(391, "Fire, Fighting"),
        Infernape(392, "Fire, Fighting"),
        Piplup(393, "Water"),
        Prinplup(394, "Water"),
        Empoleon(395, "Water, Steel"),
        Starly(396, "Normal, Flying"),
        Staravia(397, "Normal, Flying"),
        Staraptor(398, "Normal, Flying"),
        Bidoof(399, "Normal"),
        Bibarel(400, "Normal, Water"),
        Kricketot(401, "Bug"),
        Kricketune(402, "Bug"),
        Shinx(403, "Electric"),
        Luxio(404, "Electric"),
        Luxray(405, "Electric"),
        Budew(406, "Grass, Poison"),
        Roserade(407, "Grass, Poison"),
        Cranidos(408, "Rock"),
        Rampardos(409, "Rock"),
        Shieldon(410, "Rock, Steel"),
        Bastiodon(411, "Rock, Steel"),
        Burmy(412, "Bug"),
        Wormadam(413, "Bug, Grass"),
        Mothim(414, "Bug, Flying"),
        Combee(415, "Bug, Flying"),
        Vespiquen(416, "Bug, Flying"),
        Pachirisu(417, "Electric"),
        Buizel(418, "Water"),
        Floatzel(419, "Water"),
        Cherubi(420, "Grass"),
        Cherrim(421, "Grass"),
        Shellos(422, "Water"),
        Gastrodon(423, "Water, Ground"),
        Ambipom(424, "Normal"),
        Drifloon(425, "Ghost, Flying"),
        Drifblim(426, "Ghost, Flying"),
        Buneary(427, "Normal"),
        Lopunny(428, "Normal"),
        Mismagius(429, "Ghost"),
        Honchkrow(430, "Dark, Flying"),
        Glameow(431, "Normal"),
        Purugly(432, "Normal"),
        Chingling(433, "Psychic"),
        Stunky(434, "Poison, Dark"),
        Skuntank(435, "Poison, Dark"),
        Bronzor(436, "Steel, Psychic"),
        Bronzong(437, "Steel, Psychic"),
        Bonsly(438, "Rock"),
        MimeJr(439, "Psychic, Fairy"),
        Happiny(440, "Normal"),
        Chatot(441, "Normal, Flying"),
        Spiritomb(442, "Ghost, Dark"),
        Gible(443, "Dragon, Ground"),
        Gabite(444, "Dragon, Ground"),
        Garchomp(445, "Dragon, Ground"),
        Munchlax(446, "Normal"),
        Riolu(447, "Fighting"),
        Lucario(448, "Fighting, Steel"),
        Hippopotas(449, "Ground"),
        Hippowdon(450, "Ground"),
        Skorupi(451, "Poison, Bug"),
        Drapion(452, "Poison, Dark"),
        Croagunk(453, "Poison, Fighting"),
        Toxicroak(454, "Poison, Fighting"),
        Carnivine(455, "Grass"),
        Finneon(456, "Water"),
        Lumineon(457, "Water"),
        Mantyke(458, "Water, Flying"),
        Snover(459, "Grass, Ice"),
        Abomasnow(460, "Grass, Ice"),
        Weavile(461, "Dark, Ice"),
        Magnezone(462, "Electric, Steel"),
        Lickilicky(463, "Normal"),
        Rhyperior(464, "Ground, Rock"),
        Tangrowth(465, "Grass"),
        Electivire(466, "Electric"),
        Magmortar(467, "Fire"),
        Togekiss(468, "Fairy, Flying"),
        Yanmega(469, "Bug, Flying"),
        Leafeon(470, "Grass"),
        Glaceon(471, "Ice"),
        Gliscor(472, "Ground, Flying"),
        Mamoswine(473, "Ice, Ground"),
        PorygonZ(474, "Normal"),
        Gallade(475, "Psychic, Fighting"),
        Probopass(476, "Rock, Steel"),
        Dusknoir(477, "Ghost"),
        Froslass(478, "Ice, Ghost"),
        Rotom(479, "Electric, Ghost"),
        Uxie(480, "Psychic"),
        Mesprit(481, "Psychic"),
        Azelf(482, "Psychic"),
        Dialga(483, "Steel, Dragon"),
        Palkia(484, "Water, Dragon"),
        Heatran(485, "Fire, Steel"),
        Regigigas(486, "Normal"),
        Giratina(487, "Ghost, Dragon"),
        Cresselia(488, "Psychic"),
        Phione(489, "Water"),
        Manaphy(490, "Water"),
        Darkrai(491, "Dark"),
        Shaymin(492, "Grass"),
        Arceus(493, "Normal"),

        // Gen 5
        Victini(494, "Psychic, Fire"),
        Snivy(495, "Grass"),
        Servine(496, "Grass"),
        Serperior(497, "Grass"),
        Tepig(498, "Fire"),
        Pignite(499, "Fire, Fighting"),
        Emboar(500, "Fire, Fighting"),
        Oshawott(501, "Water"),
        Dewott(502, "Water"),
        Samurott(503, "Water"),
        Patrat(504, "Normal"),
        Watchog(505, "Normal"),
        Lillipup(506, "Normal"),
        Herdier(507, "Normal"),
        Stoutland(508, "Normal"),
        Purrloin(509, "Dark"),
        Liepard(510, "Dark"),
        Pansage(511, "Grass"),
        Simisage(512, "Grass"),
        Pansear(513, "Fire"),
        Simisear(514, "Fire"),
        Panpour(515, "Water"),
        Simipour(516, "Water"),
        Munna(517, "Psychic"),
        Musharna(518, "Psychic"),
        Pidove(519, "Normal, Flying"),
        Tranquill(520, "Normal, Flying"),
        Unfezant(521, "Normal, Flying"),
        Blitzle(522, "Electric"),
        Zebstrika(523, "Electric"),
        Roggenrola(524, "Rock"),
        Boldore(525, "Rock"),
        Gigalith(526, "Rock"),
        Woobat(527, "Psychic, Flying"),
        Swoobat(528, "Psychic, Flying"),
        Drilbur(529, "Ground"),
        Excadrill(530, "Ground, Steel"),
        Audino(531, "Normal"),
        Timburr(532, "Fighting"),
        Gurdurr(533, "Fighting"),
        Conkeldurr(534, "Fighting"),
        Tympole(535, "Water"),
        Palpitoad(536, "Water, Ground"),
        Seismitoad(537, "Water, Ground"),
        Throh(538, "Fighting"),
        Sawk(539, "Fighting"),
        Sewaddle(540, "Bug, Grass"),
        Swadloon(541, "Bug, Grass"),
        Leavanny(542, "Bug, Grass"),
        Venipede(543, "Bug, Poison"),
        Whirlipede(544, "Bug, Poison"),
        Scolipede(545, "Bug, Poison"),
        Cottonee(546, "Grass, Fairy"),
        Whimsicott(547, "Grass, Fairy"),
        Petilil(548, "Grass"),
        Lilligant(549, "Grass"),
        Basculin(550, "Water"),
        Sandile(551, "Ground, Dark"),
        Krokorok(552, "Ground, Dark"),
        Krookodile(553, "Ground, Dark"),
        Darumaka(554, "Fire"),
        Darmanitan(555, "Fire"),
        Maractus(556, "Grass"),
        Dwebble(557, "Bug, Rock"),
        Crustle(558, "Bug, Rock"),
        Scraggy(559, "Dark, Fighting"),
        Scrafty(560, "Dark, Fighting"),
        Sigilyph(561, "Psychic, Flying"),
        Yamask(562, "Ghost"),
        Cofagrigus(563, "Ghost"),
        Tirtouga(564, "Water, Rock"),
        Carracosta(565, "Water, Rock"),
        Archen(566, "Rock, Flying"),
        Archeops(567, "Rock, Flying"),
        Trubbish(568, "Poison"),
        Garbodor(569, "Poison"),
        Zorua(570, "Dark"),
        Zoroark(571, "Dark"),
        Minccino(572, "Normal"),
        Cinccino(573, "Normal"),
        Gothita(574, "Psychic"),
        Gothorita(575, "Psychic"),
        Gothitelle(576, "Psychic"),
        Solosis(577, "Psychic"),
        Duosion(578, "Psychic"),
        Reuniclus(579, "Psychic"),
        Ducklett(580, "Water, Flying"),
        Swanna(581, "Water, Flying"),
        Vanillite(582, "Ice"),
        Vanillish(583, "Ice"),
        Vanilluxe(584, "Ice"),
        Deerling(585, "Normal, Grass"),
        Sawsbuck(586, "Normal, Grass"),
        Emolga(587, "Electric, Flying"),
        Karrablast(588, "Bug"),
        Escavalier(589, "Bug, Steel"),
        Foongus(590, "Grass, Poison"),
        Amoonguss(591, "Grass, Poison"),
        Frillish(592, "Water, Ghost"),
        Jellicent(593, "Water, Ghost"),
        Alomomola(594, "Water"),
        Joltik(595, "Bug, Electric"),
        Galvantula(596, "Bug, Electric"),
        Ferroseed(597, "Grass, Steel"),
        Ferrothorn(598, "Grass, Steel"),
        Klink(599, "Steel"),
        Klang(600, "Steel"),
        Klinklang(601, "Steel"),
        Tynamo(602, "Electric"),
        Eelektrik(603, "Electric"),
        Eelektross(604, "Electric"),
        Elgyem(605, "Psychic"),
        Beheeyem(606, "Psychic"),
        Litwick(607, "Ghost, Fire"),
        Lampent(608, "Ghost, Fire"),
        Chandelure(609, "Ghost, Fire"),
        Axew(610, "Dragon"),
        Fraxure(611, "Dragon"),
        Haxorus(612, "Dragon"),
        Cubchoo(613, "Ice"),
        Beartic(614, "Ice"),
        Cryogonal(615, "Ice"),
        Shelmet(616, "Bug"),
        Accelgor(617, "Bug"),
        Stunfisk(618, "Ground, Electric"),
        Mienfoo(619, "Fighting"),
        Mienshao(620, "Fighting"),
        Druddigon(621, "Dragon"),
        Golett(622, "Ground, Ghost"),
        Golurk(623, "Ground, Ghost"),
        Pawniard(624, "Dark, Steel"),
        Bisharp(625, "Dark, Steel"),
        Bouffalant(626, "Normal"),
        Rufflet(627, "Normal, Flying"),
        Braviary(628, "Normal, Flying"),
        Vullaby(629, "Dark, Flying"),
        Mandibuzz(630, "Dark, Flying"),
        Heatmor(631, "Fire"),
        Durant(632, "Bug, Steel"),
        Deino(633, "Dark, Dragon"),
        Zweilous(634, "Dark, Dragon"),
        Hydreigon(635, "Dark, Dragon"),
        Larvesta(636, "Bug, Fire"),
        Volcarona(637, "Bug, Fire"),
        Cobalion(638, "Steel, Fighting"),
        Terrakion(639, "Rock, Fighting"),
        Virizion(640, "Grass, Fighting"),
        Tornadus(641, "Flying"),
        Thundurus(642, "Electric, Flying"),
        Reshiram(643, "Dragon, Fire"),
        Zekrom(644, "Dragon, Electric"),
        Landorus(645, "Ground, Flying"),
        Kyurem(646, "Dragon, Ice"),
        Keldeo(647, "Water, Fighting"),
        Meloetta(648, "Normal, Psychic"),
        Genesect(649, "Bug, Steel"),

        // Gen 6
        Chespin(650, "Grass"),
        Quilladin(651, "Grass"),
        Chesnaught(652, "Grass, Fighting"),
        Fennekin(653, "Fire"),
        Braixen(654, "Fire"),
        Delphox(655, "Fire, Psychic"),
        Froakie(656, "Water"),
        Frogadier(657, "Water"),
        Greninja(658, "Water, Dark"),
        Bunnelby(659, "Normal"),
        Diggersby(660, "Normal, Ground"),
        Fletchling(661, "Normal, Flying"),
        Fletchinder(662, "Fire, Flying"),
        Talonflame(663, "Fire, Flying"),
        Scatterbug(664, "Bug"),
        Spewpa(665, "Bug"),
        Vivillon(666, "Bug, Flying"),
        Litleo(667, "Fire, Normal"),
        Pyroar(668, "Fire, Normal"),
        Flabebe(669, "Fairy"),
        Floette(670, "Fairy"),
        Florges(671, "Fairy"),
        Skiddo(672, "Grass"),
        Gogoat(673, "Grass"),
        Pancham(674, "Fighting"),
        Pangoro(675, "Fighting, Dark"),
        Furfrou(676, "Normal"),
        Espurr(677, "Psychic"),
        Meowstic(678, "Psychic"),
        Honedge(679, "Steel, Ghost"),
        Doublade(680, "Steel, Ghost"),
        Aegislash(681, "Steel, Ghost"),
        Spritzee(682, "Fairy"),
        Aromatisse(683, "Fairy"),
        Swirlix(684, "Fairy"),
        Slurpuff(685, "Fairy"),
        Inkay(686, "Dark, Psychic"),
        Malamar(687, "Dark, Psychic"),
        Binacle(688, "Rock, Water"),
        Barbaracle(689, "Rock, Water"),
        Skrelp(690, "Poison, Water"),
        Dragalge(691, "Poison, Dragon"),
        Clauncher(692, "Water"),
        Clawitzer(693, "Water"),
        Helioptile(694, "Electric, Normal"),
        Heliolisk(695, "Electric, Normal"),
        Tyrunt(696, "Rock, Dragon"),
        Tyrantrum(697, "Rock, Dragon"),
        Amaura(698, "Rock, Ice"),
        Aurorus(699, "Rock, Ice"),
        Sylveon(700, "Fairy"),
        Hawlucha(701, "Fighting, Flying"),
        Dedenne(702, "Electric, Fairy"),
        Carbink(703, "Rock, Fairy"),
        Goomy(704, "Dragon"),
        Sliggoo(705, "Dragon"),
        Goodra(706, "Dragon"),
        Klefki(707, "Steel, Fairy"),
        Phantump(708, "Ghost, Grass"),
        Trevenant(709, "Ghost, Grass"),
        Pumpkaboo(710, "Ghost, Grass"),
        Gourgeist(711, "Ghost, Grass"),
        Bergmite(712, "Ice"),
        Avalugg(713, "Ice"),
        Noibat(714, "Flying, Dragon"),
        Noivern(715, "Flying, Dragon"),
        Xerneas(716, "Fairy"),
        Yveltal(717, "Dark, Flying"),
        Zygarde(718, "Dragon, Ground"),
        Diancie(719, "Rock, Fairy"),
        Hoopa(720, "Psychic, Ghost"),
        Volcanion(721, "Fire, Water"),

        // Gen 7
        Rowlet(722, "Grass, Flying"),
        Dartrix(723, "Grass, Flying"),
        Decidueye(724, "Grass, Ghost"),
        Litten(725, "Fire"),
        Torracat(726, "Fire"),
        Incineroar(727, "Fire, Dark"),
        Popplio(728, "Water"),
        Brionne(729, "Water"),
        Primarina(730, "Water, Fairy"),
        Pikipek(731, "Normal, Flying"),
        Trumbeak(732, "Normal, Flying"),
        Toucannon(733, "Normal, Flying"),
        Yungoos(734, "Normal"),
        Gumshoos(735, "Normal"),
        Grubbin(736, "Bug"),
        Charjabug(737, "Bug, Electric"),
        Vikavolt(738, "Bug, Electric"),
        Crabrawler(739, "Fighting"),
        Crabominable(740, "Fighting, Ice"),
        Oricorio(741, "Fire, Flying"),
        Cutiefly(742, "Bug, Fairy"),
        Ribombee(743, "Bug, Fairy"),
        Rockruff(744, "Rock"),
        Lycanroc(745, "Rock"),
        Wishiwashi(746, "Water"),
        Mareanie(747, "Poison, Water"),
        Toxapex(748, "Poison, Water"),
        Mudbray(749, "Ground"),
        Mudsdale(750, "Ground"),
        Dewpider(751, "Water, Bug"),
        Araquanid(752, "Water, Bug"),
        Fomantis(753, "Grass"),
        Lurantis(754, "Grass"),
        Morelull(755, "Grass, Fairy"),
        Shiinotic(756, "Grass, Fairy"),
        Salandit(757, "Poison, Fire"),
        Salazzle(758, "Poison, Fire"),
        Stufful(759, "Normal, Fighting"),
        Bewear(760, "Normal, Fighting"),
        Bounsweet(761, "Grass"),
        Steenee(762, "Grass"),
        Tsareena(763, "Grass"),
        Comfey(764, "Fairy"),
        Oranguru(765, "Normal, Psychic"),
        Passimian(766, "Fighting"),
        Wimpod(767, "Bug, Water"),
        Golisopod(768, "Bug, Water"),
        Sandygast(769, "Ghost, Ground"),
        Palossand(770, "Ghost, Ground"),
        Pyukumuku(771, "Water"),
        TypeNull(772, "Normal"),
        Silvally(773, "Normal"),
        Minior(774, "Rock, Flying"),
        Komala(775, "Normal"),
        Turtonator(776, "Fire, Dragon"),
        Togedemaru(777, "Electric, Steel"),
        Mimikyu(778, "Ghost, Fairy"),
        Bruxish(779, "Water, Psychic"),
        Drampa(780, "Normal, Dragon"),
        Dhelmise(781, "Ghost, Grass"),
        JangmoO(782, "Dragon"),
        HakamoO(783, "Dragon, Fighting"),
        KommoO(784, "Dragon, Fighting"),
        TapuKoko(785, "Electric, Fairy"),
        TapuLele(786, "Psychic, Fairy"),
        TapuBulu(787, "Grass, Fairy"),
        TapuFini(788, "Water, Fairy"),
        Cosmog(789, "Psychic"),
        Cosmoem(790, "Psychic"),
        Solgaleo(791, "Psychic, Steel"),
        Lunala(792, "Psychic, Ghost"),
        Nihilego(793, "Rock, Poison"),
        Buzzwole(794, "Bug, Fighting"),
        Pheromosa(795, "Bug, Fighting"),
        Xurkitree(796, "Electric"),
        Celesteela(797, "Steel, Flying"),
        Kartana(798, "Grass, Steel"),
        Guzzlord(799, "Dark, Dragon"),
        Necrozma(800, "Psychic"),
        Magearna(801, "Steel, Fairy"),
        Marshadow(802, "Fighting, Ghost"),

        // Forms. Be careful -- cannot be accessed or checked through ID, as it disallows checking 0.
        CastformSunny(0, "Fire"),
        CastformRainy(0, "Water"),
        CastformSnowy(0, "Ice"),
        WormadamSandy(0, "Bug, Ground"),
        WormadamTrash(0, "Bug, Steel"),
        RotomHeat(0, "Electric, Fire"),
        RotomWash(0, "Electric, Water"),
        RotomFrost(0, "Electric, Ice"),
        RotomFan(0, "Electric, Flying"),
        RotomMow(0, "Electric, Grass"),
        ShayminSky(0, "Grass, Flying"),
        DarmanitanZen(0, "Fire, Psychic"),
        MeloettaPirouette(0, "Normal, Fighting"),
        HoopaUnbound(0, "Psychic, Dark"),

        // Alolan Pokémon variants. Same rules as above.
        RattataAlolan(0, "Dark, Normal"),
        RaticateAlolan(0, "Dark, Normal"),
        RaichuAlolan(0, "Electric, Psychic"),
        SandshrewAlolan(0, "Ice, Steel"),
        SandslashAlolan(0, "Ice, Steel"),
        VulpixAlolan(0, "Ice"),
        NinetalesAlolan(0, "Ice, Fairy"),
        DiglettAlolan(0, "Ground, Steel"),
        DugtrioAlolan(0, "Ground, Steel"),
        MeowthAlolan(0, "Dark"),
        PersianAlolan(0, "Dark"),
        GeodudeAlolan(0, "Rock, Electric"),
        GravelerAlolan(0, "Rock, Electric"),
        GolemAlolan(0, "Rock, Electric"),
        GrimerAlolan(0, "Poison, Dark"),
        MukAlolan(0, "Poison, Dark"),
        ExeggutorAlolan(0, "Grass, Dragon"),
        MarowakAlolan(0, "Fire, Ghost");

        // Set up some variables for the Pokémon check.
        public int index;
        public String type1, type2;

        EnumPokemonList(int index, String types)
        {
            this.index = index;
            String[] delimitedTypes = types.split(", ");
            int typeCount = delimitedTypes.length;

            if (typeCount == 2)
            {
                type1 = delimitedTypes[0];
                type2 = delimitedTypes[1];
            }
            else
            {
                type1 = delimitedTypes[0];
                type2 = "EMPTY";
            }
        }

        public static EnumPokemonList getPokemonFromID(int index)
        {
            EnumPokemonList[] values = values();
            EnumPokemonList pokemon = values[index - 1];

            if (pokemon != null)
                return values[index - 1];
            else
                return null;
        }

        public static EnumPokemonList getPokemonFromName(String name)
        {
            EnumPokemonList[] allValues = values();

            for (EnumPokemonList pokemon : allValues)
            {
                if (pokemon.name().equalsIgnoreCase(name))
                    return pokemon;
            }
            // If the loop does not find and return a Pokémon, do this.
            return null;
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
    // Make sure to comment out code it complains about. //
    //---------------------------------------------------//

    for (int i = 1; i < 803; i++) // UPDATE ME (replace 803 with your last Pokémon's ID, +1)
    {
        returnedPokemon = getPokemonFromID(i);

        if (!contains(validTypeList.class, returnedPokemon.type1) || !contains(validTypeList.class, returnedPokemon.type2))
            PixelUpgrade.log.info("\u00A74Array error found! \u00A7c" + returnedPokemon.index + " | " + returnedPokemon.type1 + " | " + returnedPokemon.type2);
    }
    */
}