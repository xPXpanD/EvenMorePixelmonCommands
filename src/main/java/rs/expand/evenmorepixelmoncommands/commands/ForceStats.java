// The second PixelUpgrade/EMPC command. It's so helpful to have your own NBT editor!
package rs.expand.evenmorepixelmoncommands.commands;

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
import rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods;

import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printBasicError;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printSourcedMessage;

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
            PrintingMethods.printUnformattedMessage(input);
        else // Print normally.
            src.sendMessage(Text.of(input));
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
            sendCheckedMessage(src,"§4Error: §cThis command's config is invalid! Please check the file.");
        else
        {
            boolean forceValue = false, valueIsNumeric = false;
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
                forceValue = true;
            else if (arg5Optional.isPresent() && arg5Optional.get().equalsIgnoreCase("-f"))
                forceValue = true;

            if (arg1Optional.isPresent()) // Target or slot.
            {
                final String arg1String = arg1Optional.get();

                if (calledRemotely)
                {
                    if (Sponge.getServer().getPlayer(arg1String).isPresent())
                        target = Sponge.getServer().getPlayer(arg1String).get();
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target on first argument. See below.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    if (arg1String.matches("^[1-6]"))
                        slot = Integer.parseInt(arg1String);
                    else if (Sponge.getServer().getPlayer(arg1String).isPresent())
                        target = Sponge.getServer().getPlayer(arg1String).get();
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid target or slot on first argument. See below.");
                        return CommandResult.empty();
                    }
                }
            }
            else
            {
                printLocalError(src, "§4Error: §cNo arguments found. See below.");
                return CommandResult.empty();
            }

            if (arg2Optional.isPresent()) // Slot or stat.
            {
                final String arg2String = arg2Optional.get();

                if (target != null)
                {
                    if (arg2String.matches("^[1-6]"))
                        slot = Integer.parseInt(arg2String);
                    else
                    {
                        printLocalError(src, "§4Error: §cInvalid slot on second argument. See below.");
                        return CommandResult.empty();
                    }
                }
                else
                {
                    if (validIVsEVs.contains(arg2String) || validOtherStats.contains(arg2String))
                        stat = arg2String;
                    else
                    {
                        stat = checkForUpgradeStats(arg2String);

                        if (!stat.equals(arg2String))
                            fixMessageString = "§eFound Upgrade-style stat, adjusting to \"§6" + stat + "§e\"...";
                        else
                        {
                            stat = checkForOtherStats(arg2String);

                            if (!stat.equals(arg2String))
                                fixMessageString = "§eFound known bad stat, adjusting to \"§6" + stat + "§e\"...";
                        }

                        if (fixMessageString == null) // No message means we didn't fix anything.
                        {
                            if (!forceValue)
                            {
                                printLocalError(src, "§4Error: §cInvalid stat on second argument. See below.");
                                return CommandResult.empty();
                            }
                        }
                    }
                }
            }
            else
            {
                if (calledRemotely)
                {
                    printLocalError(src, "§4Error: §cMissing slot on second argument. See below.");
                    return CommandResult.empty();
                }
                else
                {
                    printLocalError(src, "§4Error: §cMissing slot or stat on second argument. See below.");
                    return CommandResult.empty();
                }
            }

            if (arg3Optional.isPresent()) // Stat or value.
            {
                final String arg3String = arg3Optional.get();

                if (target != null)
                {
                    if (validIVsEVs.contains(arg3String) || validOtherStats.contains(arg3String))
                        stat = arg3String;
                    else
                    {
                        stat = checkForUpgradeStats(arg3String);

                        if (!stat.equals(arg3String))
                            fixMessageString = "§eFound Upgrade-style stat, adjusting to \"§6" + stat + "§e\"...";
                        else
                        {
                            stat = checkForOtherStats(arg3String);

                            if (!stat.equals(arg3String))
                                fixMessageString = "§eFound known bad stat, adjusting to \"§6" + stat + "§e\"...";
                        }

                        if (fixMessageString == null) // No message means we didn't fix anything.
                        {
                            if (!forceValue)
                            {
                                printLocalError(src, "§4Error: §cInvalid stat on third argument. See below.");
                                return CommandResult.empty();
                            }
                        }
                    }
                }
                else
                {
                    if (arg3String.matches("-?[1-9]\\d*|0"))
                    {
                        longValue = Long.parseLong(arg3String);
                        valueIsNumeric = true;
                    }
                }
            }
            else
            {
                if (calledRemotely)
                    printLocalError(src, "§4Error: §cMissing stat on third argument. See below.");
                else
                    printLocalError(src, "§4Error: §cMissing stat or value on third argument. See below.");

                return CommandResult.empty();
            }

            // Every argument shifts up by one if we have a target. Run this to finalize args if we're shifted.
            if (target != null)
            {
                if (arg4Optional.isPresent() && !arg4Optional.get().equalsIgnoreCase("-f")) // Value or force flag.
                {
                    final String arg4String = arg4Optional.get();

                    if (arg4String.matches("-?[1-9]\\d*|0"))
                    {
                        longValue = Long.parseLong(arg4String);
                        valueIsNumeric = true;
                    }
                }
                else
                {
                    printLocalError(src, "§4Error: §cMissing or invalid value on fourth argument. See below.");
                    return CommandResult.empty();
                }
            }

            // Do in-battle checks. This first one is only hittable if we got called by an actual Player.
            if (target == null && BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                sendCheckedMessage(src, "§4Error: §cYou can't use this command while in a battle!");
                return CommandResult.empty();
            }
            else if (target != null && BattleRegistry.getBattle((EntityPlayerMP) target) != null)
            {
                sendCheckedMessage(src, "§4Error: §cTarget is battling, changes wouldn't stick. Exiting.");
                return CommandResult.empty();
            }

            // Did we pass ALL those checks? Go go go!
            // Start by getting the player's party, and then the Pokémon in the given slot.
            final Pokemon pokemon;
            if (target != null)
                pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) target).get(slot - 1);
            else
                pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot - 1);

            if (pokemon == null)
            {
                if (target != null)
                    sendCheckedMessage(src,"§4Error: §cYour target does not have anything in that slot!");
                else
                    sendCheckedMessage(src,"§4Error: §cYou don't have anything in that slot!");
            }
            else
            {
                if (!forceValue && !valueIsNumeric)
                    printLocalError(src, "§4Error: §cGot a non-numeric value, but no flag. Try a number.");
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
                            sendCheckedMessage(src,"§aForcing value! Old String value: §2" +
                                    pokemonNBT.getString(stat));
                        }
                        else if (pokemonNBT.getLong(stat) != 0)
                        {
                            sendCheckedMessage(src,"§aForcing value! Old numeric value: §2" +
                                    pokemonNBT.getLong(stat));
                        }
                        else
                        {
                            // Slightly awkward, we'll have to make an assumption here.
                            // getLong returns 0 both if something isn't present or if it is actually 0.
                            sendCheckedMessage(src, "§eForcing value! Tried to grab old value, but couldn't. Stuff may break.");
                        }

                        if (valueIsNumeric)
                            pokemonNBT.setLong(stat, longValue);
                        else
                        {
                            // All the arguments shift by one to accomodate for arg1 being a target, if it is one.
                            if (target == null)
                                pokemonNBT.setString(stat, arg3Optional.get());
                            else
                                pokemonNBT.setString(stat, arg4Optional.get());
                        }

                        // Write changed data to the Pokémon.
                        pokemon.writeToNBT(pokemonNBT);

                        // Update the player's sidebar with the new changes.
                        printBasicError("Yo, did it update? If not, TODO.");

                        sendCheckedMessage(src,"§aThe new value was written. You may have to reconnect.");
                        sendCheckedMessage(src,"§7-----------------------------------------------------");
                    }
                    else
                    {
                        if (stat.equals("Gender") && longValue > 2 || stat.equals("Gender") && longValue < 0)
                            sendCheckedMessage(src,"§4Error: §cSize value out of bounds. Valid range: 0 ~ 2");
                        else if (stat.equals("Growth") && longValue > 8 || stat.equals("Growth") && longValue < 0)
                            sendCheckedMessage(src,"§4Error: §cSize value out of bounds. Valid range: 0 ~ 8");
                        else if (stat.equals("IsShiny") && longValue != 0 && longValue != 1)
                            sendCheckedMessage(src,"§4Error: §cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)");
                        else if (stat.equals("Nature") && longValue > 24 || stat.equals("Nature") && longValue < 0)
                            sendCheckedMessage(src,"§4Error: §cNature value out of bounds. Valid range: 0 ~ 24");
                        else
                        {
                            sendCheckedMessage(src,"§7-----------------------------------------------------");

                            if (fixMessageString != null)
                            {
                                sendCheckedMessage(src,fixMessageString);
                                sendCheckedMessage(src,"");
                            }

                            // Write to player's chat, or console if running from command block.
                            sendCheckedMessage(src, "§bWriting value... Stat is §3" + stat + "§b, old value was §3" +
                                    pokemonNBT.getLong(stat) + "§b, new is §3" + longValue + "§b.");

                            // Also write to console specifically when called by a player. Let's make sure this is logged!
                            if (src instanceof Player)
                            {
                                printSourcedMessage(this.getClass().getSimpleName(), "Writing value... Stat is §3" + stat +
                                        "§b, old value was §3" + pokemonNBT.getLong(stat) + "§b, new is §3" + longValue + "§b.");
                            }

                            pokemonNBT.setLong(stat, longValue);

                            // Update the player's sidebar with the new changes.
                            printBasicError("Yo, did it update? If not, TODO.");

                            sendCheckedMessage(src,"§aExisting NBT value changed! You may have to reconnect.");
                            sendCheckedMessage(src,"§7-----------------------------------------------------");
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

	private void printLocalError(final CommandSource src, final String errorString)
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