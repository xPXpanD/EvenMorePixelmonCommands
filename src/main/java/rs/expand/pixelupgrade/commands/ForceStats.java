// The second PixelUpgrade command. It's so helpful to have your own NBT editor!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.utilities.PrintingMethods;

// FIXME: Long numberic inputs cause a NumberFormatException.
public class ForceStats implements CommandExecutor
{
    // Declare a config variable. We'll load stuff into it when we call the config loader.
    // Other config variables are loaded in from their respective classes.
    public static String commandAlias;

    // Set up some more variables for internal use.
    private boolean calledRemotely;

    // Allows us to redirect printed messages away from command blocks, and into the console if need be.
    private void sendCheckedMessage(final CommandSource src, final String input)
    {
        if (src instanceof CommandBlock) // Redirect to console, respecting existing formatting.
            PrintingMethods.printBasicMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
    }

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    {
        if (!calledRemotely)
            PrintingMethods.printDebugMessage("ForceStats", debugNum, inputString);
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
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Running from console or blocks? Let's tell our code that. If "src" is not a Player, this becomes true.
        calledRemotely = !(src instanceof Player);

        if (commandAlias == null)
        {
            printToLog(0, "Could not read node \"§4commandAlias§c\".");
            printToLog(0, "This command's config could not be parsed. Exiting.");
            sendCheckedMessage(src,"§4Error: §cThis command's config is invalid! Please check the file.");
        }
        else if (PixelUpgrade.useBritishSpelling == null)
        {
            printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
            printToLog(0, "The main config contains invalid variables. Exiting.");
            sendCheckedMessage(src,"§4Error: §cCould not parse main config. Please report to staff.");
        }
        else
        {
            if (calledRemotely)
            {
                if (src instanceof CommandBlock)
                {
                    PrintingMethods.printDebugMessage("ForceStats", 1,
                            "Called by command block, starting. Silencing logger messages.");
                }
                else
                {
                    PrintingMethods.printDebugMessage("ForceStats", 1,
                            "Called by console, starting. Silencing further log messages.");
                }
            }
            else
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

            boolean canContinue = true, forceValue = false, valueIsNumeric = false;
            final Optional<String> arg1Optional = args.getOne("target/slot");
            final Optional<String> arg2Optional = args.getOne("slot/stat");
            final Optional<String> arg3Optional = args.getOne("stat/value");
            final Optional<String> arg4Optional = args.getOne("value/force flag");
            final Optional<String> arg5Optional = args.getOne("force flag");
            String stat = null, fixMessageString = null;
            int slot = 0;
            long longValue = 0;
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
                final String arg1String = arg1Optional.get();

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
                    final String arg2String = arg2Optional.get();

                    if (target != null)
                    {
                        if (arg2String.matches("^[1-6]"))
                        {
                            printToLog(2, "Slot was a valid slot number. Let's move on!");
                            slot = Integer.parseInt(arg2String);
                        }
                        else
                        {
                            printToLog(1, "Invalid slot on second argument. Exit.");
                            printError(src, "§4Error: §cInvalid slot on second argument. See below.");
                            canContinue = false;
                        }
                    }
                    else
                    {
                        if (validIVsEVs.contains(arg2String) || validOtherStats.contains(arg2String))
                        {
                            printToLog(2, "Provided stat was valid, proceeding without adjustment.");
                            stat = arg2String;
                        }
                        else
                        {
                            stat = checkForUpgradeStats(arg2String);

                            if (!stat.equals(arg2String))
                            {
                                printToLog(2, "Found a known Upgrade-style stat. Fixing...");
                                fixMessageString = "§eFound Upgrade-style stat, adjusting to \"§6" + stat + "§e\"...";
                            }
                            else
                            {
                                stat = checkForOtherStats(arg2String);

                                if (!stat.equals(arg2String))
                                {
                                    printToLog(2, "Found a known bad stat. Fixing...");
                                    fixMessageString = "§eFound known bad stat, adjusting to \"§6" + stat + "§e\"...";
                                }
                            }

                            if (fixMessageString == null) // No message means we didn't fix anything.
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
                                printToLog(2, "Managed to fix it, let's continue.");
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
                    final String arg3String = arg3Optional.get();

                    if (target != null)
                    {
                        if (validIVsEVs.contains(arg3String) || validOtherStats.contains(arg3String))
                        {
                            printToLog(2, "Provided stat was valid, proceeding without adjustment.");
                            stat = arg3String;
                        }
                        else
                        {
                            stat = checkForUpgradeStats(arg3String);

                            if (!stat.equals(arg3String))
                            {
                                printToLog(2, "Found a known Upgrade-style stat. Fixing...");
                                fixMessageString = "§eFound Upgrade-style stat, adjusting to \"§6" + stat + "§e\"...";
                            }
                            else
                            {
                                stat = checkForOtherStats(arg3String);

                                if (!stat.equals(arg3String))
                                {
                                    printToLog(2, "Found a known bad stat. Fixing...");
                                    fixMessageString = "§eFound known bad stat, adjusting to \"§6" + stat + "§e\"...";
                                }
                            }

                            if (fixMessageString == null) // No message means we didn't fix anything.
                            {
                                if (!forceValue)
                                {
                                    printToLog(1, "Invalid and unfixable stat on arg 3, not in force mode. Exit.");
                                    printError(src, "§4Error: §cInvalid stat on third argument. See below.");
                                    canContinue = false;
                                }
                                else
                                    printToLog(2, "No valid stat found, but force mode is on. Proceeding...");
                            }
                            else
                                printToLog(2, "Managed to fix it, let's continue.");
                        }
                    }
                    else
                    {
                        if (arg3String.matches("-?[1-9]\\d*|0"))
                        {
                            printToLog(2, "Checked value, and found out it's numeric. Setting flag.");
                            longValue = Long.parseLong(arg3String);
                            valueIsNumeric = true;
                        }
                        else
                            printToLog(2, "Value is not numeric, so treating it as a String.");
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

            // Every argument shifts up by one if we have a target. Run this to finalize args if we're shifted.
            if (canContinue && target != null)
            {
                if (arg4Optional.isPresent() && !arg4Optional.get().equalsIgnoreCase("-f")) // Value or force flag.
                {
                    final String arg4String = arg4Optional.get();

                    if (arg4String.matches("-?[1-9]\\d*|0"))
                    {
                        printToLog(2, "Checked value, and found out it's numeric. Setting flag.");
                        longValue = Long.parseLong(arg4String);
                        valueIsNumeric = true;
                    }
                    else
                        printToLog(2, "Value is not numeric, so treating it as a String.");
                }
                else
                {
                    printToLog(1, "Missing or invalid value on fourth argument. Exit.");
                    printError(src, "§4Error: §cMissing or invalid value on fourth argument. See below.");

                    canContinue = false;
                }
            }

            // Do in-battle checks.
            if (canContinue)
            {
                // Only hittable if we got called by an actual Player.
                if (target == null && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
                {
                    printToLog(0, "Player tried to set stats on own Pokémon while in a battle. Exit.");
                    sendCheckedMessage(src, "§4Error: §cYou can't use this command while in a battle!");

                    canContinue = false;
                }
                else if (target != null && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
                {
                    printToLog(0, "Target was in a battle, cannot proceed. Exit."); // Swallowed if console.
                    sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");

                    canContinue = false;
                }
            }

            // Did we pass ALL those checks? Go go go!
            if (canContinue)
            {
                // Get the player's party, and then get the Pokémon in the targeted slot.
                final Pokemon pokemon;
                if (target != null)
                    pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) target).get(slot - 1);
                else
                    pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot - 1);
                    printToLog(2, "Checks completed, entering execution.");

                if (pokemon == null)
                {
                    printToLog(1, "No Pokémon data found in slot, probably empty. Exit.");

                    if (target != null)
                        sendCheckedMessage(src,"§4Error: §cYour target does not have anything in that slot!");
                    else
                        sendCheckedMessage(src,"§4Error: §cYou don't have anything in that slot!");
                }
                else
                {
                    if (!forceValue && !valueIsNumeric)
                    {
                        printToLog(1, "We support Strings only in force mode. Exit.");
                        printError(src, "§4Error: §cGot a non-numeric value, but no flag. Try a number.");
                    }
                    else // ok we good
                    {
                        // Create a copy of the Pokémon's persistent data for extracting specific NBT info from.
                        final NBTTagCompound pokemonNBT = pokemon.getPersistentData();

                        if (forceValue)
                        {
                            sendCheckedMessage(src,"§7-----------------------------------------------------");

                            if (fixMessageString != null)
                            {
                                sendCheckedMessage(src,fixMessageString);
                                sendCheckedMessage(src,"");
                            }

                            if (!pokemonNBT.getString(stat).isEmpty())
                            {
                                printToLog(1, "Value is being forced! Old String value: §3" +
                                        pokemonNBT.getString(stat));
                                sendCheckedMessage(src,"§aForcing value! Old String value: §2" +
                                        pokemonNBT.getString(stat));
                            }
                            else if (pokemonNBT.getLong(stat) != 0)
                            {
                                printToLog(1, "Value is being forced! Old numeric value: §3" +
                                        pokemonNBT.getLong(stat));
                                sendCheckedMessage(src,"§aForcing value! Old numeric value: §2" +
                                        pokemonNBT.getLong(stat));
                            }
                            else
                            {
                                // Slightly awkward, we'll have to make an assumption here.
                                // getLong returns 0 both if something isn't present or if it is actually 0.
                                printToLog(1,
                                        "Value is being forced! Tried to grab old value, but couldn't.");
                                sendCheckedMessage(src,
                                        "§eForcing value! Tried to grab old value, but couldn't.");
                            }

                            if (valueIsNumeric)
                            {
                                printToLog(1, "Numeric value written. Glad to be of service.");
                                pokemonNBT.setLong(stat, longValue);
                            }
                            else
                            {
                                printToLog(1, "Non-numeric value written... Glad to be of service?");

                                // All the arguments shift by one to accomodate for arg1 being a target, if it is one.
                                if (target == null)
                                    pokemonNBT.setString(stat, arg3Optional.get());
                                else
                                    pokemonNBT.setString(stat, arg4Optional.get());
                            }

                            // Update the player's sidebar with the new changes.
                            printToLog(0, "Yo, did it update? If not, TODO.");

                            sendCheckedMessage(src,"§aThe new value was written. You may have to reconnect.");
                            sendCheckedMessage(src,"§7-----------------------------------------------------");
                        }
                        else
                        {
                            printToLog(2, "Found valid non-forced input, testing against limits.");

                            if (stat.equals("Gender") && longValue > 2 || stat.equals("Gender") && longValue < 0)
                            {
                                printToLog(1, "Found a Gender value above 2 or below 0; out of bounds. Exit.");
                                sendCheckedMessage(src,"§4Error: §cSize value out of bounds. Valid range: 0 ~ 2");
                            }
                            else if (stat.equals("Growth") && longValue > 8 || stat.equals("Growth") && longValue < 0)
                            {
                                printToLog(1, "Found a Growth value above 8 or below 0; out of bounds. Exit.");
                                sendCheckedMessage(src,"§4Error: §cSize value out of bounds. Valid range: 0 ~ 8");
                            }
                            else if (stat.equals("IsShiny") && longValue != 0 && longValue != 1)
                            {
                                printToLog(1, "Invalid shiny status value detected. Exit.");
                                sendCheckedMessage(src,"§4Error: §cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)");
                            }
                            else if (stat.equals("Nature") && longValue > 24 || stat.equals("Nature") && longValue < 0)
                            {
                                printToLog(1, "Found a Nature value above 24 or below 0; out of bounds. Exit.");
                                sendCheckedMessage(src,"§4Error: §cNature value out of bounds. Valid range: 0 ~ 24");
                            }
                            else
                            {
                                sendCheckedMessage(src,"§7-----------------------------------------------------");

                                if (fixMessageString != null)
                                {
                                    sendCheckedMessage(src,fixMessageString);
                                    sendCheckedMessage(src,"");
                                }

                                printToLog(1, "Setting... Stat is §3" + stat + "§b, old value was §3" +
                                        pokemonNBT.getLong(stat) + "§b, new is §3" + longValue + "§b.");

                                sendCheckedMessage(src,"§aWriting value...");
                                pokemonNBT.setLong(stat, longValue);

                                // Update the player's sidebar with the new changes.
                                printToLog(0, "Yo, did it update? If not, TODO.");

                                sendCheckedMessage(src,"§aExisting NBT value changed! You may have to reconnect.");
                                sendCheckedMessage(src,"§7-----------------------------------------------------");
                            }
                        }
                    }
                }
            }
        }

        return CommandResult.success();
	}

	private String checkForUpgradeStats(final String stat)
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

	private String checkForOtherStats(final String stat)
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
            case "EVSPECIALDEFENCE": case "EVSPECIALDEFENSE": case "EVSPDEF":
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

	private void printError(final CommandSource src, final String errorString)
    {
        sendCheckedMessage(src,"§5-----------------------------------------------------");
        sendCheckedMessage(src,errorString);

        if (calledRemotely)
            sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " <target> <slot> <stat> <value> {-f to force}");
        else
            sendCheckedMessage(src,"§4Usage: §c/" + commandAlias + " [target?] <slot> <stat> <value> {-f to force}");

        sendCheckedMessage(src,"");
        sendCheckedMessage(src,"§6IVs: §eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed");
        sendCheckedMessage(src,"§6EVs: §eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed");
        sendCheckedMessage(src,"§6Others: §eGender, Growth, Nature, IsShiny");
        sendCheckedMessage(src,"§eThese are internal names, common mistakes will be fixed.");
        sendCheckedMessage(src,"");
        sendCheckedMessage(src,"§5Please note: §dPassing the -f flag will disable safety checks.");
        sendCheckedMessage(src,"§dMay lead to crashes or even corruption, handle with care!");
        sendCheckedMessage(src,"§5-----------------------------------------------------");
    }
}