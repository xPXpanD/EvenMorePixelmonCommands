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

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

//TODO: Add reveal mode to config.
//TODO: Quick check for whether an egg has been previously checked.

public class CheckEgg implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Player player = (Player) src;
        Boolean canContinue = true, commandConfirmed = false;
        Integer slot = 0;
        BigDecimal costToConfirm = new BigDecimal(25);

        PixelUpgrade.log.info("\u00A7bCheckEgg: Called by player " + player.getName() + ", starting command.");

        if (!args.<String>getOne("slot").isPresent())
        {
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/checkegg <slot, 1-6> (-c to confirm)"));
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + costToConfirm + "\u00A7e coins."));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

            canContinue = false;
        }
        else
        {
            String slotString = args.<String>getOne("slot").get();

            if (slotString.matches("^[1-6]"))
                slot = Integer.parseInt(args.<String>getOne("slot").get());
            else
            {
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/checkegg <slot, 1-6> (-c to confirm)"));
                player.sendMessage(Text.of(""));
                player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
                player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + costToConfirm + "\u00A7e coins."));
                player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                canContinue = false;
            }
        }

        if (args.hasAny("c"))
            commandConfirmed = true;

        if (canContinue)
        {
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
            PlayerStorage storageCompleted = storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (nbt == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
            else if (!nbt.getBoolean("isEgg"))
                player.sendMessage(Text.of("\u00A74Error: \u00A7cThis command only works on eggs! Check out \u00A74/getstats\u00A7c."));
            else if (commandConfirmed)
            {
                Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                if (optionalAccount.isPresent())
                {
                    UniqueAccount uniqueAccount = optionalAccount.get();
                    TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());

                    if (transactionResult.getResult() == ResultType.SUCCESS)
                    {
                        Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
                        Integer IVATT = nbt.getInteger(NbtKeys.IV_ATTACK);
                        Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                        Integer IVSPATT = nbt.getInteger(NbtKeys.IV_SP_ATT);
                        Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                        Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                        Integer totalIVs = IVHP + IVATT + IVDEF + IVSPATT + IVSPDEF + IVSPD;
                        Integer percentIVs = totalIVs * 100 / 186;
                        /*String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;

                        if (IVHP == 31)
                            ivs1 = String.valueOf("\u00A7o" + IVHP + " \u00A72HP \u00A7r\u00A7e|\u00A7a ");
                        else
                            ivs1 = String.valueOf(IVHP + " \u00A72HP \u00A7e|\u00A7a ");

                        if (IVATT == 31)
                            ivs2 = String.valueOf("\u00A7o" + IVATT + " \u00A72ATK \u00A7r\u00A7e|\u00A7a ");
                        else
                            ivs2 = String.valueOf(IVATT + " \u00A72ATK \u00A7e|\u00A7a ");

                        if (IVDEF == 31)
                            ivs3 = String.valueOf("\u00A7o" + IVDEF + " \u00A72DEF \u00A7r\u00A7e|\u00A7a ");
                        else
                            ivs3 = String.valueOf(IVDEF + " \u00A72DEF \u00A7e|\u00A7a ");

                        if (IVSPATT == 31)
                            ivs4 = String.valueOf("\u00A7o" + IVSPATT + " \u00A72Sp. ATK \u00A7r\u00A7e|\u00A7a ");
                        else
                            ivs4 = String.valueOf(IVSPATT + " \u00A72Sp. ATK \u00A7e|\u00A7a ");

                        if (IVSPDEF == 31)
                            ivs5 = String.valueOf("\u00A7o" + IVSPDEF + " \u00A72Sp. DEF \u00A7r\u00A7e|\u00A7a ");
                        else
                            ivs5 = String.valueOf(IVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a ");

                        if (IVSPD == 31)
                            ivs6 = String.valueOf("\u00A7o" + IVSPD + " \u00A72SPD");
                        else
                            ivs6 = String.valueOf(IVSPD + " \u00A72SPD");*/

                        player.sendMessage(Text.of("\u00A76There's a healthy \u00A7c" + nbt.getString("Name") + "\u00A76 inside of this egg!"));
                        if (percentIVs >= 90 && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                            player.sendMessage(Text.of("\u00A76What's this? \u00A7eThis baby seems to be bursting with energy..."));
                        else if (!(percentIVs >= 90) && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                            player.sendMessage(Text.of("\u00A76What's this? \u00A7eThis baby seems to have an odd sheen to it..."));
                        else if (percentIVs >= 90 && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                            player.sendMessage(Text.of("\u00A76What's this? \u00A7eSomething about this baby seems real special!"));

                        PixelUpgrade.log.info("\u00A7aCheckEgg debug: Checked status of egg in slot " + slot + ", and took " + costToConfirm + " coins.");
                    }
                    else
                    {
                        BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cYou need \u00A74" + balanceNeeded + "\u00A7c more coins to do this."));
                    }
                }
                else
                {
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cNo economy account found. Please contact staff!"));

                    PixelUpgrade.log.info("\u00A74CheckEgg debug:" + player.getName() + "\u00A7c does not have an economy account, aborting. May be a bug?");
                }
            }
            else
            {
                player.sendMessage(Text.of("\u00A76Warning: \u00A7eChecking an egg's status costs \u00A76" + costToConfirm + "\u00A7e coins."));
                player.sendMessage(Text.of("\u00A7bIf you want to continue, type: \u00A7a/checkegg " + slot + " -c"));
            }
        }
        return CommandResult.success();
    }
}