package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

/*                                                         *\
    TODO: Add target and console support. This will hurt.
    This could end up being VERY potent, if done right...
\*                                                         */

public class ForceStats implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("ForceStats", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            if (commandAlias == null)
            {
                printToLog(0, "Could not read node \"§4commandAlias§c\".");
                printToLog(0, "This command's config could not be parsed. Exiting.");
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please check the file."));
            }
            else if (useBritishSpelling == null)
            {
                printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
                printToLog(0, "The main config contains invalid variables. Exiting.");
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                boolean canContinue = true, statWasFixed = true, forceValue = false, shinyFix = false, valueIsInt = false;
                int slot = 0, intValue = 0;
                String stat = null, fixedStat = null, value = null;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. See below."));
                    printSyntaxHelper(src);
                    addFooter(src);

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printSyntaxHelper(src);
                        addFooter(src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("f"))
                    forceValue = true;

                if (args.<String>getOne("stat").isPresent() && canContinue)
                {
                    stat = args.<String>getOne("stat").get();

                    switch (stat.toUpperCase())
                    {
                        case "IVHP":
                            fixedStat = "IVHP";
                            break;
                        case "IVATTACK":
                            fixedStat = "IVAttack";
                            break;
                        case "IVDEFENCE": case "IVDEFENSE":
                            fixedStat = "IVDefence";
                            break;
                        case "IVSPATT": case "IVSPATK":
                            fixedStat = "IVSpAtt";
                            break;
                        case "IVSPDEF":
                            fixedStat = "IVSpDef";
                            break;
                        case "IVSPEED":
                            fixedStat = "IVSpeed";
                            break;
                        case "EVHP":
                            fixedStat = "EVHP";
                            break;
                        case "EVATTACK":
                            fixedStat = "EVAttack";
                            break;
                        case "EVDEFENCE": case "EVDEFENSE":
                            fixedStat = "EVDefence";
                            break;
                        case "EVSPECIALATTACK": case "EVSPATT": case "EVSPATK":
                            fixedStat = "EVSpecialAttack";
                            break;
                        case "EVSPECIALDEFENCE": case "EVSPDEF":
                            fixedStat = "EVSpecialDefence";
                            break;
                        case "EVSPEED":
                            fixedStat = "EVSpeed";
                            break;
                        case "GROWTH": case "SIZE":
                            fixedStat = "Growth";
                            break;
                        case "NATURE":
                            fixedStat = "Nature";
                            break;
                        case "ISSHINY": case "IS_SHINY": case "SHINY":
                            fixedStat = "IsShiny";
                            shinyFix = true;
                            break;
                        default:
                            statWasFixed = false;
                    }

                    if (!statWasFixed && !forceValue)
                    {
                        printToLog(1, "Invalid stat provided, and force flag not passed. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid stat provided. See below for valid stats."));
                        printSyntaxHelper(src);
                        addHelper(src);
                        addFooter(src);

                        canContinue = false;
                    }
                }
                else if (canContinue)
                {
                    printToLog(1, "No stat was provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo stat provided. See below for valid stats."));
                    printSyntaxHelper(src);
                    addHelper(src);
                    addFooter(src);

                    canContinue = false;
                }

                if (!args.<String>getOne("value").isPresent() && canContinue)
                {
                    printToLog(1, "No value was provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo value or amount was provided."));
                    printSyntaxHelper(src);
                    addFooter(src);

                    canContinue = false;
                }
                else if (canContinue)
                {
                    String valueString = args.<String>getOne("value").get();

                    if (valueString.matches("^-?[0-9]*$") && !valueString.matches("-"))
                    {
                        printToLog(2, "Checked value, and found out it's an integer. Setting flag.");
                        intValue = Integer.parseInt(args.<String>getOne("value").get());
                        valueIsInt = true;
                    }
                    else
                    {
                        printToLog(2, "Value is not an integer, so treating it as a string.");
                        value = args.<String>getOne("value").get();
                    }
                }

                if (canContinue)
                {
                    printToLog(2, "No errors encountered, input should be valid. Continuing!");
                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (!forceValue && valueIsInt)
                        {
                            String[] validIVEV = new String[]
                            {
                                "IVHP", "IVAttack", "IVDefense", "IVSpAtt", "IVSpDef", "IVSpeed",
                                "EVHP", "EVAttack", "EVDefense", "EVSpecialAttack", "EVSpecialDefense", "EVSpeed"
                            };

                            printToLog(2, "Value is not forced, but is valid. Let's patch up the player's input.");
                            printToLog(2, "Found stat §2" + stat +
                                    "§a, trying adjustment... It is now §2" + fixedStat + "§a.");

                            if (Arrays.asList(validIVEV).contains(fixedStat) && intValue > 32767 ||
                                    Arrays.asList(validIVEV).contains(fixedStat) && intValue < -32768)
                            {
                                printToLog(1, "Found an IV or EV so high that it'd roll over. Exit.");
                                src.sendMessage(Text.of("§4Error: §cIV/EV value out of bounds. Valid range: -32768 ~ 32767"));
                            }
                            else if (fixedStat.equals("Growth") && intValue > 8 || fixedStat.equals("Growth") && intValue < 0)
                            {
                                printToLog(1, "Found a Growth value above 8 or below 0; out of bounds. Exit.");
                                src.sendMessage(Text.of("§4Error: §cSize value out of bounds. Valid range: 0 ~ 8"));
                            }
                            else if (fixedStat.equals("Nature") && intValue > 24 || fixedStat.equals("Nature") && intValue < 0)
                            {
                                printToLog(1, "Found a Nature value above 24 or below 0; out of bounds. Exit.");
                                src.sendMessage(Text.of("§4Error: §cNature value out of bounds. Valid range: 0 ~ 24"));
                            }
                            else if (fixedStat.equals("IsShiny") && intValue != 0 && intValue != 1)
                            {
                                printToLog(1, "Invalid shiny status value detected. Exit.");
                                src.sendMessage(Text.of("§4Error: §cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)"));
                            }
                            else
                            {
                                printToLog(1, "Changing value. Stat: §3" + fixedStat + "§b. Old: §3" +
                                        nbt.getInteger(fixedStat) + "§b. New: §3" + intValue + "§b.");

                                nbt.setInteger(fixedStat, intValue);
                                if (shinyFix)
                                    nbt.setInteger("Shiny", intValue);

                                src.sendMessage(Text.of("§aExisting NBT value changed! You may have to reconnect."));
                                storageCompleted.sendUpdatedList();
                            }
                        }
                        else if (!forceValue)
                        {
                            printToLog(1, "Provided value was a string, but they're only supported when forced. Exit.");

                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cGot a non-integer value, but no flag. Try a number."));
                            printSyntaxHelper(src);
                            addFooter(src);
                        }
                        else
                        {
                            try
                            {
                                printToLog(1, "Value is being forced! Old value: §3" +
                                    nbt.getInteger(stat) + "§b.");
                            }
                            catch (Exception F)
                            { printToLog(1, "Value is being forced! Tried to grab old value, but couldn't read it..."); }

                            src.sendMessage(Text.of("§eForcing value..."));

                            if (statWasFixed)
                            {
                                printToLog(2, "Found a known stat in force mode. Checking and fixing, just in case...");

                                src.sendMessage(Text.of("§cFound known bad stat \"§4" + stat +
                                        "§c\", adjusting to \"§4" + fixedStat + "§c\"..."));
                                stat = fixedStat;
                            }

                            if (valueIsInt)
                            {
                                printToLog(1, "Integer value written. Glad to be of service.");

                                nbt.setInteger(stat, intValue);
                                if (shinyFix)
                                    nbt.setInteger("Shiny", intValue);
                            }
                            else
                            {
                                printToLog(1, "String value written. Glad to be of service.");

                                nbt.setString(stat, value);
                                if (shinyFix)
                                    nbt.setString("Shiny", value);
                            }

                            src.sendMessage(Text.of("§aThe new value was written. You may have to reconnect."));
                            storageCompleted.sendUpdatedList();
                        }
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

	// Might look a bit odd, but done this way so we only have to edit one message if this ever changes.
    private void printSyntaxHelper(CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot> <stat> <value> {-f to force}"));
    }

    private void addFooter(CommandSource src)
    {
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
        src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void addHelper(CommandSource src)
    {
        src.sendMessage(Text.of(""));
        if (useBritishSpelling)
        {
            src.sendMessage(Text.of("§6IVs: §eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
            src.sendMessage(Text.of("§6EVs: §eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
        }
        else
        {
            src.sendMessage(Text.of("§6IVs: §eIVHP, IVAttack, IVDefense, IVSpAtt, IVSpDef, IVSpeed"));
            src.sendMessage(Text.of("§6EVs: §eEVHP, EVAttack, EVDefense, EVSpAtt, EVSpDef, EVSpeed"));
        }
        src.sendMessage(Text.of("§6Others: §eGrowth, Nature, Shiny"));
    }
}