// The second PixelUpgrade command. It's so helpful to have your own NBT editor!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.CommonMethods;

// TODO: Fix Pokémon not changing if certain stats get changed while they're sent out.
public class ForceStats implements CommandExecutor
{
    // Initialize a config variable. We'll load stuff into it when we call the config loader.
    // Other config variables are loaded in from their respective classes.
    public static String commandAlias;

    // Set up some more variables for internal use.
    private boolean calledRemotely;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    {
        if (!calledRemotely)
            CommonMethods.printDebugMessage("ForceStats", debugNum, inputString);
    }

    // Create a few sets of Strings that we query during input checking.
    // This one is for valid IVs and EVs, so we can correctly apply limits.
    private final Set<String> validIVsEVs = new HashSet<>();
    {
        validIVsEVs.add("IVHP");
        validIVsEVs.add("IVAttack");
        validIVsEVs.add("IVDefence");
        validIVsEVs.add("IVSpAtt");
        validIVsEVs.add("IVSpDef");
        validIVsEVs.add("IVSpeed");
        validIVsEVs.add("EVHP");
        validIVsEVs.add("EVAttack");
        validIVsEVs.add("EVDefence");
        validIVsEVs.add("EVSpecialAttack");
        validIVsEVs.add("EVSpecialDefence");
        validIVsEVs.add("EVSpeed");
    }

    // This one is for other valid stats. Suggestions for more are welcome.
    private final Set<String> validOtherStats = new HashSet<>();
    {
        validOtherStats.add("Gender");
        validOtherStats.add("Growth");
        validOtherStats.add("IsShiny");
        validOtherStats.add("Nature");
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        // Are we running from the console? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        if (commandAlias == null)
        {
            printToLog(0, "Could not read node \"§4commandAlias§c\".");
            printToLog(0, "This command's config could not be parsed. Exiting.");
            src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please check the file."));
        }
        else if (PixelUpgrade.useBritishSpelling == null)
        {
            printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
            printToLog(0, "The main config contains invalid variables. Exiting.");
            src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
        }
        else
        {
            if (calledRemotely)
            {
                CommonMethods.printDebugMessage("ForceStats", 1,
                        "Called by console, starting. Omitting debug messages for clarity.");
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean statWasFixed = false, foundValidStat = false, isIVorEV = false, foundUpgradeStyleStat = false;
            boolean canContinue = true, forceValue = false, valueIsInt = false;
            Optional<String> arg1Optional = args.getOne("target/slot");
            Optional<String> arg2Optional = args.getOne("slot/stat");
            Optional<String> arg3Optional = args.getOne("stat/value");
            Optional<String> arg4Optional = args.getOne("value/force flag");
            Optional<String> arg5Optional = args.getOne("confirmation");
            String stat = null;
            int slot = 0, intValue = 0;
            Player target = null;

            // Ugly, but it'll do for now... Doesn't seem like my usual way of getting flags will work here.
            if (arg4Optional.isPresent() && arg4Optional.get().equalsIgnoreCase("-f"))
            {
                printToLog(2, "Discovered a force flag in argument slot 4.");
                forceValue = true;
            }
            else if (arg5Optional.isPresent() && arg5Optional.get().equalsIgnoreCase("-f"))
            {
                printToLog(2, "Discovered a force flag in argument slot 5.");
                forceValue = true;
            }

            if (arg1Optional.isPresent()) // Target or slot.
            {
                String arg1String = arg1Optional.get();

                if (calledRemotely)
                {
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();

                        if (!src.getName().equalsIgnoreCase(arg1String))
                            printToLog(2, "Found a valid target in argument 1.");
                        else
                            printToLog(2, "Player targeted self. Continuing.");
                    }
                    else
                    {
                        printToLog(1, "Invalid target on first argument. Exit.");
                        printError(src, "§4Error: §cInvalid target on first argument. See below.");
                        canContinue = false;
                    }
                }
                else
                {
                    if (arg1String.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(arg1String);
                    }
                    else if (Sponge.getServer().getPlayer(arg1String).isPresent())
                    {
                        target = Sponge.getServer().getPlayer(arg1String).get();

                        if (!src.getName().equalsIgnoreCase(arg1String))
                            printToLog(2, "Found a valid target in argument 1.");
                        else
                            printToLog(2, "Player targeted self. Continuing.");
                    }
                    else
                    {
                        printToLog(1, "Invalid target or slot on first argument. Exit.");
                        printError(src, "§4Error: §cInvalid target or slot on first argument. See below.");
                        canContinue = false;
                    }
                }
            }
            else
            {
                printToLog(1, "No arguments were found. Exit.");
                printError(src, "§4Error: §cNo arguments found. See below.");
                canContinue = false;
            }

            if (canContinue)
            {
                if (arg2Optional.isPresent()) // Slot or stat.
                {
                    String arg2String = arg2Optional.get();

                    if (target != null)
                    {
                        if (arg2String.matches("^[1-6]"))
                        {
                            printToLog(2, "Slot was a valid slot number. Let's move on!");
                            slot = Integer.parseInt(arg2String);
                        }
                        else
                        {
                            printToLog(1, "Missing slot on second argument. Exit.");
                            printError(src, "§4Error: §cMissing slot on second argument. See below.");
                            canContinue = false;
                        }
                    }
                    else
                    {
                        if (validIVsEVs.contains(arg2String) || validOtherStats.contains(arg2String))
                        {
                            if (validIVsEVs.contains(arg2String))
                            {
                                isIVorEV = true;
                                printToLog(1337, "Contained in validIVsEVs.");
                            }
                            else
                                printToLog(1337, "Contained in validOtherStats.");

                            stat = arg2String;
                            foundValidStat = true;
                        }
                        else
                        {
                            stat = checkForUpgradeStats(arg2String);

                            if (!stat.equals(arg2String))
                            {
                                foundUpgradeStyleStat = true;
                                statWasFixed = true;
                            }
                            else
                            {
                                stat = checkForOtherStats(arg2String);

                                if (!stat.equals(arg2String))
                                    statWasFixed = true;
                            }

                            if (!statWasFixed)
                            {
                                if (!forceValue)
                                {
                                    printToLog(1, "Invalid and unfixable stat on arg 2, not in force mode. Exit.");
                                    printError(src, "§4Error: §cInvalid stat on second argument. See below.");
                                    canContinue = false;
                                }
                                else
                                    printToLog(2, "No valid stat found, but force mode is on. Proceeding...");
                            }
                            else
                                printToLog(2, "Invalid stat found, but we managed to fix it. Let's continue.");
                        }
                    }
                }
                else
                {
                    if (calledRemotely)
                    {
                        printToLog(1, "No slot on second argument. Exit.");
                        printError(src, "§4Error: §cMissing slot on second argument. See below.");
                    }
                    else
                    {
                        printToLog(1, "No slot or stat on second argument. Exit.");
                        printError(src, "§4Error: §cMissing slot or stat on second argument. See below.");
                    }

                    canContinue = false;
                }
            }

            if (canContinue)
            {
                if (arg3Optional.isPresent()) // Stat or value.
                {
                    String arg3String = arg3Optional.get();

                    if (target != null)
                    {
                        if (validIVsEVs.contains(arg3String) || validOtherStats.contains(arg3String))
                        {
                            if (validIVsEVs.contains(arg3String))
                            {
                                isIVorEV = true;
                                printToLog(1337, "Contained in validIVsEVs.");
                            }
                            else
                                printToLog(1337, "Contained in validOtherStats.");

                            stat = arg3String;
                            foundValidStat = true;
                        }
                        else
                        {
                            stat = checkForUpgradeStats(arg3String);

                            if (!stat.equals(arg3String))
                            {
                                foundUpgradeStyleStat = true;
                                statWasFixed = true;
                            }
                            else
                            {
                                stat = checkForOtherStats(arg3String);

                                if (!stat.equals(arg3String))
                                    statWasFixed = true;
                            }

                            if (!statWasFixed)
                            {
                                if (!forceValue)
                                {
                                    printToLog(1, "Invalid and unfixable stat on arg 3, not in force mode. Exit.");
                                    printError(src, "§4Error: §cInvalid stat on second argument. See below.");
                                    canContinue = false;
                                }
                                else
                                    printToLog(2, "No valid stat found, but force mode is on. Proceeding...");
                            }
                            else
                                printToLog(2, "Invalid stat found, but we managed to fix it. Let's continue.");
                        }
                    }
                    else
                    {
                        if (arg3String.matches("-?[1-9]\\d*|0"))
                        {
                            printToLog(2, "Checked value, and found out it's an integer. Setting flag.");
                            intValue = Integer.parseInt(arg3String);
                            valueIsInt = true;
                        }
                        else
                            printToLog(2, "Value is not an integer, so treating it as a String.");
                    }
                }
                else
                {
                    if (calledRemotely)
                    {
                        printToLog(1, "No stat on third argument. Exit.");
                        printError(src, "§4Error: §cMissing stat on third argument. See below.");
                    }
                    else
                    {
                        printToLog(1, "No stat or value on third argument. Exit.");
                        printError(src, "§4Error: §cMissing stat or value on third argument. See below.");
                    }

                    canContinue = false;
                }
            }

            if (canContinue && target != null)
            {
                if (arg4Optional.isPresent() && !arg4Optional.get().equalsIgnoreCase("-f")) // Value or force flag.
                {
                    String arg4String = arg4Optional.get();

                    if (arg4String.matches("-?[1-9]\\d*|0"))
                    {
                        printToLog(2, "Checked value, and found out it's an integer. Setting flag.");
                        intValue = Integer.parseInt(arg4String);
                        valueIsInt = true;
                    }
                    else
                        printToLog(2, "Value is not an integer, so treating it as a String.");
                }
                else
                {
                    printToLog(1, "Invalid or missing value on fourth argument. Exit.");
                    printError(src, "§4Error: §cMissing or invalid value on fourth argument. See below.");

                    canContinue = false;
                }
            }

            if (canContinue)
            {
                Optional<PlayerStorage> storage;
                if (target != null)
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target));
                else
                    storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                if (!storage.isPresent())
                {
                    if (target != null)
                        printToLog(0, "§4" + target.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                    else
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");

                    src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Might be a bug?"));
                }
                else
                {
                    printToLog(2, "Checks completed, entering execution.");
                    src.sendMessage(Text.of("§7-----------------------------------------------------"));

                    PlayerStorage storageCompleted = storage.get();
                    NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                    if (nbt == null)
                    {
                        printToLog(1, "No NBT data found in slot, probably empty. Exit.");

                        if (target != null)
                            src.sendMessage(Text.of("§4Error: §cYour target does not have anything in that slot!"));
                        else
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                    }
                    else
                    {
                        // We'll already have errored out if we had an unusable stat at this point, unless forced.
                        if (!foundValidStat && statWasFixed)
                        {
                            printToLog(2, "Found a known bad stat. Fixing...");

                            if (forceValue || valueIsInt)
                            {
                                if (foundUpgradeStyleStat)
                                {
                                    src.sendMessage(Text.of("§eFound Upgrade-style stat, adjusting to \"§6" +
                                            stat + "§e\"..."));
                                }
                                else
                                {
                                    src.sendMessage(Text.of("§eFound known bad stat, adjusting to \"§6" +
                                            stat + "§e\"..."));
                                }

                                src.sendMessage(Text.of(""));
                            }
                        }
                        else // The player's input was already entirely valid! Good job, player. 💮
                            printToLog(2, "Provided stat was valid, proceeding without adjustment.");

                        if (forceValue)
                        {
                            try
                            {
                                src.sendMessage(Text.of("§aForcing value! Old value: §2" +
                                        nbt.getString(stat)));
                                printToLog(1, "Value is being forced! Old value: §3" +
                                        nbt.getString(stat));
                            }
                            catch (Exception F)
                            {
                                src.sendMessage(Text.of("§eForcing value! Tried to grab old value, but couldn't..."));
                                printToLog(1,
                                        "Value is being forced! Tried to grab old value, but couldn't...");
                            }

                            if (valueIsInt)
                            {
                                printToLog(1, "Integer value written. Glad to be of service.");
                                nbt.setInteger(stat, intValue);
                            }
                            else
                            {
                                printToLog(1, "Non-integer value written... Glad to be of service?");

                                // All the arguments shift by one to accomodate for arg1 being a target, if it is one.
                                if (target == null)
                                    nbt.setString(stat, arg3Optional.get());
                                else
                                    nbt.setString(stat, arg4Optional.get());
                            }

                            storageCompleted.sendUpdatedList();

                            src.sendMessage(Text.of("§aThe new value was written. You may have to reconnect."));
                        }
                        else if (valueIsInt)
                        {
                            printToLog(2, "Value is not forced, but is valid. Patching up player's input.");

                            if (isIVorEV && intValue > 32767 || isIVorEV && intValue < -32768)
                            {
                                printToLog(1, "Found an IV/EV value so high that it'd roll over. Exit.");
                                src.sendMessage(Text.of("§4Error: §cIV/EV value out of bounds. Valid range: -32768 ~ 32767"));
                            }
                            else if (stat.equals("Gender") && intValue > 2 || stat.equals("Gender") && intValue < 0)
                            {
                                printToLog(1, "Found a Gender value above 2 or below 0; out of bounds. Exit.");
                                src.sendMessage(Text.of("§4Error: §cSize value out of bounds. Valid range: 0 ~ 2"));
                            }
                            else if (stat.equals("Growth") && intValue > 8 || stat.equals("Growth") && intValue < 0)
                            {
                                printToLog(1, "Found a Growth value above 8 or below 0; out of bounds. Exit.");
                                src.sendMessage(Text.of("§4Error: §cSize value out of bounds. Valid range: 0 ~ 8"));
                            }
                            else if (stat.equals("IsShiny") && intValue != 0 && intValue != 1)
                            {
                                printToLog(1, "Invalid shiny status value detected. Exit.");
                                src.sendMessage(Text.of("§4Error: §cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)"));
                            }
                            else if (stat.equals("Nature") && intValue > 24 || stat.equals("Nature") && intValue < 0)
                            {
                                printToLog(1, "Found a Nature value above 24 or below 0; out of bounds. Exit.");
                                src.sendMessage(Text.of("§4Error: §cNature value out of bounds. Valid range: 0 ~ 24"));
                            }
                            else
                            {
                                printToLog(1, "Setting... Stat is §3" + stat + "§b, old value was §3" +
                                        nbt.getInteger(stat) + "§b, new is §3" + intValue + "§b.");

                                src.sendMessage(Text.of("§aWriting value..."));

                                nbt.setInteger(stat, intValue);
                                storageCompleted.sendUpdatedList();

                                src.sendMessage(Text.of("§aExisting NBT value changed! You may have to reconnect."));
                            }
                        }
                        else
                        {
                            printToLog(1, "We only support Strings in force mode. Exit.");
                            printError(src, "§4Error: §cGot a non-integer value, but no flag. Try a number.");
                        }

                        src.sendMessage(Text.of("§7-----------------------------------------------------"));
                    }
                }
            }
        }

        return CommandResult.success();
	}

	private String checkForUpgradeStats(String stat)
    {
        switch (stat.toUpperCase())
        {
            case "HP":
                return "IVHP";
            case "ATTACK":
                return "IVAttack";
            case "DEFENSE": case "DEFENCE":
                return "IVDefence";
            case "SPATT":
                return "IVSpAtt";
            case "SPDEF":
                return "IVSpDef";
            case "SPEED":
                return "IVSpeed";

            default: // Unchanged, return it as-is so we know our odd stat was not an Upgrade-style stat.
                return stat;
        }
    }

	private String checkForOtherStats(String stat)
    {
        switch (stat.toUpperCase()) // Keep in mind: we toUpperCase() our stat, stuff like ivHP or EvhP will get fixed.
        {
            case "IVHP": case "HITPOINTS": case "HEALTH":
                return "IVHP";
            case "IVATTACK": case "ATK": case "ATT":
                return "IVAttack";
            case "IVDEFENCE": case "IVDEFENSE": case "DEF":
                return "IVDefence";
            case "IVSPATT": case "SPECIALATTACK": case "SPATK": case "SPATTACK":
                return "IVSpAtt";
            case "IVSPDEF": case "SPECIALDEFENSE": case "SPECIALDEFENCE": case "SPDEFENSE": case "SPDEFENCE":
                return "IVSpDef";
            case "IVSPEED": case "SPD":
                return "IVSpeed";
            case "EVHP":
                return "EVHP";
            case "EVATTACK":
                return "EVAttack";
            case "EVDEFENCE": case "EVDEFENSE":
                return "EVDefence";
            case "EVSPECIALATTACK": case "EVSPATT": case "EVSPATK":
                return "EVSpecialAttack";
            case "EVSPECIALDEFENCE": case "EVSPDEF":
                return "EVSpecialDefence";
            case "EVSPEED":
                return "EVSpeed";
            case "GENDER": case "SEX":
                return "Gender";
            case "GROWTH": case "SIZE":
                return "Growth";
            case "ISSHINY": case "IS_SHINY": case "SHINY":
                return "IsShiny";
            case "NATURE":
                return "Nature";

            default: // Unchanged, return it as-is so we know our odd stat could not be fixed.
                return stat;
        }
    }

	private void printError(CommandSource src, String errorString)
    {
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
        src.sendMessage(Text.of(errorString));

        if (calledRemotely)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target> <slot> <stat> <value> {-f to force}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " [target?] <slot> <stat> <value> {-f to force}"));

        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§6IVs: §eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
        src.sendMessage(Text.of("§6EVs: §eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
        src.sendMessage(Text.of("§6Others: §eGender, Growth, Nature, IsShiny"));
        src.sendMessage(Text.of("§eThese are internal names, common mistakes will be fixed."));
        src.sendMessage(Text.of(""));
        src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
        src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
        src.sendMessage(Text.of("§5-----------------------------------------------------"));
    }
}