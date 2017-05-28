package rs.expand.pixelupgrade.commands;

import java.math.BigDecimal;
import java.util.Optional;

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

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.FixEVsConfig;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class FixEVs implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
	    if (src instanceof Player)
        {
            Boolean commandConfirmed = false;
            Integer debugVerbosityMode, commandCost;
            Player player = (Player) src;

            debugVerbosityMode = checkConfigInt("debugVerbosityMode", false);
            commandCost = checkConfigInt("commandCost", false);

            if (debugVerbosityMode == null || commandCost == null)
            {
                printToLog(0, "Error parsing config! Make sure everything is valid, or regenerate it.");
                src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid config for command! Please report this to staff."));
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Boolean canContinue = true;
                Integer slot = 0;

                if (!args.<String>getOne("slot").isPresent())
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
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot provided. Aborting.");

                        checkAndAddHeader(commandCost, player);
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                        printCorrectHelper(commandCost, player);
                        checkAndAddFooter(commandCost, player);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + player.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Tried to fix EVs on an egg. Aborting...");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            Integer eHP = pokemon.stats.EVs.HP;
                            Integer eATK = pokemon.stats.EVs.Attack;
                            Integer eDEF = pokemon.stats.EVs.Defence;
                            Integer eSPATK = pokemon.stats.EVs.SpecialAttack;
                            Integer eSPDEF = pokemon.stats.EVs.SpecialDefence;
                            Integer eSPD = pokemon.stats.EVs.Speed;
                            Integer totalEVs = eHP + eATK + eDEF + eSPATK + eSPDEF + eSPD;
                            Boolean wasOptimized = false, allEVsGood = false;

                            if (eHP < 253 && eHP >= 0 && eATK < 253 && eATK >= 0 && eDEF < 253 && eDEF >= 0 && eSPATK < 253 && eSPATK >= 0 && eSPDEF < 253 && eSPDEF >= 0 && eSPD < 253 && eSPD >= 0)
                                allEVsGood = true;

                            if (eHP == 0 && eATK == 0 && eDEF == 0 && eSPATK == 0 && eSPDEF == 0 && eSPD == 0)
                            {
                                printToLog(2, "All EVs were at zero, no upgrades needed to be done. Abort.");
                                src.sendMessage(Text.of("\u00A7dNo EVs were found. Go faint some wild Pok\u00E9mon!"));
                                canContinue = false;
                            }
                            else if (eHP > 255 || eATK > 255 || eDEF > 255 || eSPATK > 255 || eSPDEF > 255 || eSPD > 255)
                            {
                                printToLog(2, "Found one or more EVs above 255. Probably set by staff, so abort.");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cOne or more EVs are above the limit. Please contact staff."));
                                canContinue = false;
                            }
                            else if (eHP < 0 || eATK < 0 || eDEF < 0 || eSPATK < 0 || eSPDEF < 0 || eSPD < 0)
                            {
                                printToLog(2, "Found one or more negative EVs. Let's let staff handle this -- abort.");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cOne or more EVs are negative. Please contact staff."));
                                canContinue = false;
                            }
                            else if (totalEVs < 510 && allEVsGood)
                            {
                                printToLog(2, "No wasted stats were detected. Abort.");
                                src.sendMessage(Text.of("\u00A7dNo issues found! Your Pok\u00E9mon is coming along nicely."));
                                canContinue = false;
                            }
                            else if (totalEVs == 510 && allEVsGood)
                            {
                                printToLog(2, "EV total of 510 hit, but no overleveled EVs found. Abort.");
                                src.sendMessage(Text.of("\u00A7dNo issues found! Not happy? Get some EV-reducing berries!"));
                                canContinue = false;
                            }
                            else if (commandCost > 0)
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
                                            printToLog(1, "Fixed EVs for slot " + slot + ", and took " + costToConfirm + " coins.");
                                            fixPlayerEVs(nbt, player, eHP, eATK, eDEF, eSPATK, eSPDEF, eSPD);
                                            wasOptimized = true;
                                        }
                                        else
                                        {
                                            BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                            printToLog(2, "Not enough coins! Cost: \u00A73" + costToConfirm + "\u00A7b, lacking: \u00A73" + balanceNeeded);

                                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                                            canContinue = false;
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

                                    src.sendMessage(Text.of("\u00A76Warning: \u00A7eFixing EVs will cost \u00A76" + costToConfirm + "\u00A7e coins."));
                                    src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a/fixevs " + slot + " -c"));

                                    canContinue = false;
                                }
                            }
                            else
                            {
                                printToLog(1, "Fixed EVs for slot " + slot + ". Config price is 0, taking nothing.");
                                fixPlayerEVs(nbt, player, eHP, eATK, eDEF, eSPATK, eSPDEF, eSPD);
                                wasOptimized = true;
                            }

                            if (canContinue)
                            {
                                if (wasOptimized)
                                {
                                    printToLog(2, "Succesfully optimized a Pok\u00E9mon!");

                                    if (nbt.getString("Nickname").equals(""))
                                        src.sendMessage(Text.of("\u00A76" + nbt.getString("Name") + "\u00A7e has been checked and optimized!"));
                                    else
                                        src.sendMessage(Text.of("\u00A7eYour \u00A76" + nbt.getString("Nickname") + "\u00A7e has been checked and optimized!"));
                                }
                                else
                                    src.sendMessage(Text.of("\u00A7aNo EV-related problems found! No changes were made."));
                            }
                        }
                    }
                }
            }
        }
	    else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

	private void fixPlayerEVs(NBTTagCompound nbt, Player player, Integer eHP, Integer eATK, Integer eDEF, Integer eSPATK, Integer eSPDEF, Integer eSPD)
    {
        if (eHP > 252)
        {
            player.sendMessage(Text.of("\u00A7aStat \u00A72HP \u00A7ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_HP, 252);
        }
        if (eATK > 252)
        {
            player.sendMessage(Text.of("\u00A7aStat \u00A72Attack \u00A7ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_ATTACK, 252);
        }
        if (eDEF > 252)
        {
            player.sendMessage(Text.of("\u00A7aStat \u00A72Defence \u00A7ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_DEFENCE, 252);
        }
        if (eSPATK > 252)
        {
            player.sendMessage(Text.of("\u00A7aStat \u00A72Special Attack \u00A7ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 252);
        }
        if (eSPDEF > 252)
        {
            player.sendMessage(Text.of("\u00A7aStat \u00A72Special Defence \u00A7ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 252);
        }
        if (eSPD > 252)
        {
            player.sendMessage(Text.of("\u00A7aStat \u00A72Speed \u00A7ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPEED, 252);
        }
    }

    private void checkAndAddHeader(Integer cost, Player player)
    {
        if (cost > 0)
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void checkAndAddFooter(Integer cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + cost + "\u00A7e coins."));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    private void printCorrectHelper(Integer cost, Player player)
    {
        if (cost != 0)
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fixevs <slot, 1-6> {-c to confirm}"));
        else
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fixevs <slot, 1-6>"));
    }

    private void printToLog(Integer debugNum, String inputString)
    {
        Integer debugVerbosityMode = checkConfigInt("debugVerbosityMode", true);

        if (debugVerbosityMode == null)
            debugVerbosityMode = 4;

        if (debugNum <= debugVerbosityMode)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74FixEVs // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76FixEVs // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73FixEVs // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72FixEVs // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt(String node, Boolean noMessageMode)
    {
        if (!FixEVsConfig.getInstance().getConfig().getNode(node).isVirtual())
            return FixEVsConfig.getInstance().getConfig().getNode(node).getInt();
        else if (noMessageMode)
            return null;
        else
        {
            PixelUpgrade.log.info("\u00A74FixEVs // critical: \u00A7cCould not parse config variable \"" + node + "\"!");
            return null;
        }
    }
}
