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

import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.ResetEVsConfig;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class ResetEVs implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!ResetEVsConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = ResetEVsConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!ResetEVsConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + ResetEVsConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
        if (src instanceof Player)
        {
            Integer commandCost = null;
            if (!ResetEVsConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                commandCost = ResetEVsConfig.getInstance().getConfig().getNode("commandCost").getInt();
            else
                PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7cCould not parse config variable \"commandCost\"!");

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (commandCost == null || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7cCheck your config. If need be, wipe and \u00A74/pixelupgrade reload\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

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
                            printToLog(2, "Tried to reset EVs on an egg. Aborting...");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            if (commandConfirmed)
                            {
                                printToLog(3, "Command was confirmed, checking balances.");

                                if (commandCost > 0)
                                {
                                    BigDecimal costToConfirm = new BigDecimal(commandCost);
                                    Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                    if (optionalAccount.isPresent())
                                    {
                                        UniqueAccount uniqueAccount = optionalAccount.get();
                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());

                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            resetPlayerEVs(nbt, player);
                                            printToLog(1, "Reset EVs for slot " + slot + ", and took " + costToConfirm + " coins.");
                                            src.sendMessage(Text.of("\u00A76Your " + nbt.getString("Name") + "\u00A7e had their EVs successfully wiped!"));
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
                                    resetPlayerEVs(nbt, player);
                                    printToLog(1, "Reset EVs for slot " + slot + ". Config price is 0, taking nothing.");
                                    src.sendMessage(Text.of("\u00A76Your " + nbt.getString("Name") + "\u00A7e had their EVs successfully wiped!"));
                                }
                            }
                            else
                            {
                                printToLog(2, "No confirmation provided, printing warning and aborting.");

                                src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                                src.sendMessage(Text.of("\u00A76Warning: \u00A7eYou are about to reset this Pok\u00E9mon's EVs to zero!"));
                                if (commandCost > 0)
                                    src.sendMessage(Text.of("\u00A7eResetting will cost \u00A76" + commandCost + "\u00A7e coins!"));
                                src.sendMessage(Text.of("\u00A72Ready? Type: \u00A7a" + alias + " " + slot + " -c"));
                                src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
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

	private void resetPlayerEVs(NBTTagCompound nbt, Player player)
    {
        int EVHP = nbt.getInteger(NbtKeys.EV_HP);
        int EVATT = nbt.getInteger(NbtKeys.EV_ATTACK);
        int EVDEF = nbt.getInteger(NbtKeys.EV_DEFENCE);
        int EVSPATT = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
        int EVSPDEF = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
        int EVSPD = nbt.getInteger(NbtKeys.EV_SPEED);

        printToLog(1, "Command has been confirmed, printing old EVs...");
        printToLog(1, "HP: " + EVHP + " | ATK: " + EVATT + " | DEF: " + EVDEF + " | SPATK: " + EVSPATT + " | SPDEF: " + EVSPDEF + " | SPD: " + EVSPD);

        nbt.setInteger(NbtKeys.EV_HP, 0);
        nbt.setInteger(NbtKeys.EV_ATTACK, 0);
        nbt.setInteger(NbtKeys.EV_DEFENCE, 0);
        nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 0);
        nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 0);
        nbt.setInteger(NbtKeys.EV_SPEED, 0);

        if (nbt.getString("Nickname").equals(""))
            player.sendMessage(Text.of("\u00A76" + nbt.getString("Name") + "\u00A7e had its EVs wiped."));
        else
            player.sendMessage(Text.of("\u00A7eYour \u00A76" + nbt.getString("Nickname") + "\u00A7e had its EVs wiped."));
    }

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
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6> {-c to confirm}"));
        else
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c" + alias + " <slot, 1-6>"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74ResetEVs // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76ResetEVs // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73ResetEVs // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72ResetEVs // debug: \u00A7a" + inputString);
        }
    }
}