package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.CommonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class FixEVs implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.doPrint("FixEVs", false, debugNum, inputString); }

	@SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
	{
	    if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            ArrayList<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                CommonMethods.printNodeError("FixEVs", nativeErrorArray, 1);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (useBritishSpelling == null)
            {
                printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
                printToLog(0, "The main config contains invalid variables. Exiting.");
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §6" + src.getName() + "§e. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    checkAndAddHeader(commandCost, player);
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
                    printCorrectHelper(commandCost, player);
                    checkAndAddFooter(commandCost, player);

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

                        checkAndAddHeader(commandCost, player);
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printCorrectHelper(commandCost, player);
                        checkAndAddFooter(commandCost, player);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(2, "No error encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + player.getName() + "§c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to fix EVs on an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            int HPEV = pokemon.stats.EVs.HP;
                            int attackEV = pokemon.stats.EVs.Attack;
                            int defenceEV = pokemon.stats.EVs.Defence;
                            int spAttackEV = pokemon.stats.EVs.SpecialAttack;
                            int spDefenceEV = pokemon.stats.EVs.SpecialDefence;
                            int speedEV = pokemon.stats.EVs.Speed;
                            int totalEVs = HPEV + attackEV + defenceEV + spAttackEV + spDefenceEV + speedEV;
                            boolean allEVsGood = false;

                            if (HPEV < 253 && HPEV >= 0 && attackEV < 253 && attackEV >= 0 && defenceEV < 253 &&
                                    defenceEV >= 0 &&spAttackEV < 253 && spAttackEV >= 0 && spDefenceEV < 253 &&
                                    spDefenceEV >= 0 && speedEV < 253 && speedEV >= 0)
                                allEVsGood = true;

                            if (HPEV == 0 && attackEV == 0 && defenceEV == 0 && spAttackEV == 0 && spDefenceEV == 0 && speedEV == 0)
                            {
                                printToLog(1, "All EVs were at zero, no upgrades needed to be done. Exit.");
                                src.sendMessage(Text.of("§dNo EVs were found. Go faint some wild Pokémon!"));
                                canContinue = false;
                            }
                            else if (HPEV > 255 || attackEV > 255 || defenceEV > 255 || spAttackEV > 255 || spDefenceEV > 255 || speedEV > 255)
                            {
                                printToLog(1, "Found one or more EVs above 255. Probably set by staff, so exit.");
                                src.sendMessage(Text.of("§4Error: §cOne or more EVs are above the limit. Contact staff."));
                                canContinue = false;
                            }
                            else if (HPEV < 0 || attackEV < 0 || defenceEV < 0 || spAttackEV < 0 || spDefenceEV < 0 || speedEV < 0)
                            {
                                printToLog(1, "Found one or more negative EVs. Let's let staff handle this -- exit.");
                                src.sendMessage(Text.of("§4Error: §cOne or more EVs are negative. Please contact staff."));
                                canContinue = false;
                            }
                            else if (totalEVs < 510 && allEVsGood)
                            {
                                printToLog(1, "No wasted stats were detected. Exit.");
                                src.sendMessage(Text.of("§dNo issues found! Your Pokémon is coming along nicely."));
                                canContinue = false;
                            }
                            else if (totalEVs == 510 && allEVsGood)
                            {
                                printToLog(1, "EV total of 510 hit, but no overleveled EVs found. Exit.");
                                src.sendMessage(Text.of("§dNo issues found! Not happy? Get some EV-reducing berries!"));
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
                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            printToLog(1, "Fixed EVs for slot §6" + slot +
                                                    "§e, and took §6" + costToConfirm + "§e coins.");
                                            fixPlayerEVs(nbt, player, HPEV, attackEV, defenceEV, spAttackEV, spDefenceEV, speedEV);
                                        }
                                        else
                                        {
                                            BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                            printToLog(1, "Not enough coins! Cost: §6" +
                                                    costToConfirm + "§e, lacking: §6" + balanceNeeded);

                                            src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded +
                                                    "§c more coins to do this."));
                                            canContinue = false;
                                        }
                                    }
                                    else
                                    {
                                        printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. May be a bug?");
                                        src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                        canContinue = false;
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Got cost but no confirmation; end of the line. Exit.");

                                    src.sendMessage(Text.of("§6Warning: §eFixing EVs will cost §6" + costToConfirm + "§e coins."));
                                    src.sendMessage(Text.of("§2Ready? Type: §a" + commandAlias + " " + slot + " -c"));

                                    canContinue = false;
                                }
                            }
                            else
                            {
                                printToLog(1, "Fixed EVs for slot §6" + slot + "§e. Config price is §60§e, taking nothing.");
                                fixPlayerEVs(nbt, player, HPEV, attackEV, defenceEV, spAttackEV, spDefenceEV, speedEV);
                            }

                            if (canContinue)
                            {
                                if (nbt.getString("Nickname").equals(""))
                                    src.sendMessage(Text.of("§6" + nbt.getString("Name") + "§e has been checked and optimized!"));
                                else
                                    src.sendMessage(Text.of("§eYour §6" + nbt.getString("Nickname") + "§e has been checked and optimized!"));
                            }
                        }
                    }
                }
            }
        }
	    else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

	private void fixPlayerEVs(NBTTagCompound nbt, Player player, int HPEV, int attackEV, int defenceEV, int spAttackEV, int spDefenceEV, int speedEV)
    {
        if (HPEV > 252)
        {
            player.sendMessage(Text.of("§aStat §2HP §ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_HP, 252);
        }

        if (attackEV > 252)
        {
            player.sendMessage(Text.of("§aStat §2Attack §ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_ATTACK, 252);
        }

        if (defenceEV > 252)
        {
            if (useBritishSpelling)
                player.sendMessage(Text.of("§aStat §2Defence §ais above 252 and has been fixed!"));
            else
                player.sendMessage(Text.of("§aStat §2Defense §ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_DEFENCE, 252);
        }

        if (spAttackEV > 252)
        {
            player.sendMessage(Text.of("§aStat §2Special Attack §ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 252);
        }

        if (spDefenceEV > 252)
        {
            if (useBritishSpelling)
                player.sendMessage(Text.of("§aStat §2Special Defence §ais above 252 and has been fixed!"));
            else
                player.sendMessage(Text.of("§aStat §2Special Defense §ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 252);
        }

        if (speedEV > 252)
        {
            player.sendMessage(Text.of("§aStat §2Speed §ais above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPEED, 252);
        }
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
            player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            player.sendMessage(Text.of("§4Usage: §c" + commandAlias + " <slot, 1-6>"));
    }
}
