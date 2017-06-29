package rs.expand.pixelupgrade.commands;

import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.ForceStatsConfig;
import rs.expand.pixelupgrade.configs.PixelUpgradeMainConfig;

public class ForceStats implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!ForceStatsConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = ForceStatsConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("§4ForceStats // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4ForceStats // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!ForceStatsConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ForceStatsConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("§4ForceStats // critical: §cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

	public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            Boolean useBritishSpelling = null;

            // Grab the useBritishSpelling value from the main config.
            if (!PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").isVirtual())
                useBritishSpelling = PixelUpgradeMainConfig.getInstance().getConfig().getNode("useBritishSpelling").getBoolean();

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4ForceStats // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else if (useBritishSpelling == null)
            {
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please check the console."));
                PixelUpgrade.log.info("§4CheckEgg // critical: §cCouldn't get value of \"useBritishSpelling\" from the main config.");
                PixelUpgrade.log.info("§4CheckEgg // critical: §cPlease check (or wipe and reload) your PixelUpgrade.conf file.");
            }
            else
            {
                printToLog(2, "Called by player §3" + src.getName() + "§b. Starting!");

                boolean canContinue = true, statWasFixed = true, forceValue = false, shinyFix = false, valueIsInt = false;
                int slot = 0, intValue = 0;
                String stat = null, fixedStat = null, value = null;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. See below."));
                    src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <stat> <value> {-f to force}"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
                    src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot provided. Aborting.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <stat> <value> {-f to force}"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
                        src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

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
                        printToLog(2, "Invalid stat provided, and force flag not passed. Abort.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid stat provided. See below for valid stats."));
                        src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <stat> <value> {-f to force}"));
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

                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
                        src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

                        canContinue = false;
                    }
                }
                else if (canContinue)
                {
                    printToLog(2, "No stat was provided. Let's abort.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo stat provided. See below for valid stats."));
                    src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <stat> <value> {-f to force}"));
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

                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
                    src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    canContinue = false;
                }

                if (!args.<String>getOne("value").isPresent() && canContinue)
                {
                    printToLog(2, "No value was provided. Let's abort.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo value or amount was provided."));
                    src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <stat> <value> {-f to force}"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
                    src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    canContinue = false;
                }
                else if (canContinue)
                {
                    String valueString = args.<String>getOne("value").get();

                    if (valueString.matches("^-?[0-9]*$") && !valueString.matches("-"))
                    {
                        printToLog(3, "Checked value, and found out it's an integer. Setting flag.");
                        intValue = Integer.parseInt(args.<String>getOne("value").get());
                        valueIsInt = true;
                    }
                    else
                    {
                        printToLog(3, "Value is not an integer, so treating it as a string.");
                        value = args.<String>getOne("value").get();
                    }
                }

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
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
                            printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (!forceValue && valueIsInt)
                        {
                            String[] validIVEV = new String[]
                            {
                                "IVHP", "IVAttack", "IVDefence", "IVSpAtt", "IVSpDef", "IVSpeed",
                                "EVHP", "EVAttack", "EVDefence", "EVSpecialAttack", "EVSpecialDefence", "EVSpeed"
                            };

                            printToLog(3, "Value is not forced, but is valid. Let's patch up the player's input.");
                            printToLog(3, "Found stat §2" + stat + "§a, trying adjustment... It is now §2" + fixedStat + "§a.");

                            if (Arrays.asList(validIVEV).contains(fixedStat) && intValue > 32767 || Arrays.asList(validIVEV).contains(fixedStat) && intValue < -32768)
                            {
                                printToLog(2, "Found an IV or EV so high that it'd roll over. Aborting.");
                                src.sendMessage(Text.of("§4Error: §cIV/EV value out of bounds. Valid range: -32768 ~ 32767"));
                            }
                            else if (fixedStat.equals("Growth") && intValue > 8 || fixedStat.equals("Growth") && intValue < 0)
                            {
                                printToLog(2, "Found a Growth value above 8 or below 0; out of bounds. Aborting.");
                                src.sendMessage(Text.of("§4Error: §cSize value out of bounds. Valid range: 0 ~ 8"));
                            }
                            else if (fixedStat.equals("Nature") && intValue > 24 || fixedStat.equals("Nature") && intValue < 0)
                            {
                                printToLog(2, "Found a Nature value above 24 or below 0; out of bounds. Aborting.");
                                src.sendMessage(Text.of("§4Error: §cNature value out of bounds. Valid range: 0 ~ 24"));
                            }
                            else if (fixedStat.equals("IsShiny") && intValue != 0 && intValue != 1)
                            {
                                printToLog(2, "Invalid shiny status value detected. Aborting.");
                                src.sendMessage(Text.of("§4Error: §cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)"));
                            }
                            else
                            {
                                printToLog(1, "Changing value. Stat: §6" + fixedStat + "§e. Old: §6" + nbt.getInteger(fixedStat) + "§e. New: §6" + intValue + "§e.");

                                nbt.setInteger(fixedStat, intValue);
                                if (shinyFix)
                                    nbt.setInteger("Shiny", intValue);

                                src.sendMessage(Text.of("§aExisting NBT value changed! You may have to reconnect."));
                                storageCompleted.sendUpdatedList();
                            }
                        }
                        else if (!forceValue)
                        {
                            printToLog(2, "Provided value was a string, but they're only supported in force mode. Abort.");

                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                            src.sendMessage(Text.of("§4Error: §cGot a non-integer value, but no flag. Try a number."));
                            src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot> <stat> <value> {-f to force}"));
                            src.sendMessage(Text.of(""));
                            src.sendMessage(Text.of("§5Please note: §dPassing the -f flag will disable safety checks."));
                            src.sendMessage(Text.of("§dThis may lead to crashes or even corruption. Handle with care!"));
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        }
                        else
                        {
                            try
                            {
                                printToLog(1, "Value is being forced! Old value: §6" + nbt.getInteger(stat) + "§e.");
                            }
                            catch (Exception F)
                            {
                                printToLog(1, "Value is being forced! Tried to grab old value, but couldn't read it...");
                            }

                            src.sendMessage(Text.of("§eForcing value..."));

                            if (statWasFixed)
                            {
                                printToLog(3, "Found a known stat in force mode. Checking and fixing, just in case...");

                                src.sendMessage(Text.of("§5Note: §dKnown stat found! Validating it."));
                                src.sendMessage(Text.of("§5Provided stat: §d" + stat + "§5 | Changed to: §d" + fixedStat));

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
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4ForceStats // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§6ForceStats // important: §e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("§3ForceStats // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2ForceStats // debug: §a" + inputString);
        }
    }
}