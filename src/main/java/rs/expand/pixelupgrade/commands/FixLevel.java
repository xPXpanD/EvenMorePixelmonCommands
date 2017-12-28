package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import java.math.BigDecimal;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.configs.FixLevelConfig;
import rs.expand.pixelupgrade.PixelUpgrade;

import static rs.expand.pixelupgrade.PixelUpgrade.debugLevel;
import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class FixLevel implements CommandExecutor
{
    // Not sure how this works yet, but nicked it from TotalEconomy.
    // Will try to figure this out later, just glad to have this working for now.
    private PixelUpgrade pixelUpgrade;
    public FixLevel(PixelUpgrade pixelUpgrade) { this.pixelUpgrade = pixelUpgrade; }

    // Grab the command's alias.
    private static String alias = null;
    private void getCommandAlias()
    {
        if (!FixLevelConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + FixLevelConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
            PixelUpgrade.log.info("§4FixLevel // critical: §cConfig variable \"commandAlias\" could not be found!");
    }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            Integer commandCost = null;
            if (!FixLevelConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                commandCost = FixLevelConfig.getInstance().getConfig().getNode("commandCost").getInt();
            else
                PixelUpgrade.log.info("§4FixLevel // critical: §cCould not parse config variable \"commandCost\"!");

            // Set up the command's preferred alias.
            getCommandAlias();

            if (commandCost == null || alias == null)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4FixLevel // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    player.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
                    src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6> {-c to confirm}"));
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

                        player.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        src.sendMessage(Text.of("§4Usage: §c" + alias + " <slot, 1-6> {-c to confirm}"));
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
                            printToLog(1, "Tried to fix level on an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            int pokemonLevel = pokemon.getLvl().getLevel(), configLevel = PixelmonConfig.maxLevel;

                            if (pokemonLevel != configLevel)
                            {
                                printToLog(1, "Config max level and provided Pokémon's level did not match. Exit.");
                                src.sendMessage(Text.of("§4Error: §cYour Pokémon is not at level §4" + configLevel + "§c, yet."));
                            }
                            else
                            {
                                if (commandConfirmed)
                                {
                                    if (commandCost > 0)
                                    {
                                        BigDecimal costToConfirm = new BigDecimal(commandCost);

                                        Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                        if (optionalAccount.isPresent())
                                        {
                                            UniqueAccount uniqueAccount = optionalAccount.get();
                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.of(EventContext.empty(), pixelUpgrade.getPluginContainer()));

                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                printToLog(1, "Fixed level for slot " + slot + ", and took " + costToConfirm + " coins.");
                                                pokemon.getLvl().setLevel(configLevel - 1);
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                                printToLog(1, "Not enough coins! Cost: §3" + costToConfirm + "§b, lacking: §3" + balanceNeeded);

                                                src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
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
                                        printToLog(1, "Fixed level for slot " + slot + ". Config price is 0, taking nothing.");
                                        pokemon.getLvl().setLevel(configLevel - 1);
                                    }

                                    if (canContinue)
                                    {
                                        storageCompleted.sendUpdatedList();
                                        printToLog(1, "Succesfully fixed the level of a Pokémon!");

                                        if (nbt.getString("Nickname").equals(""))
                                            src.sendMessage(Text.of("§6" + nbt.getString("Name") + "§e had its level fixed!"));
                                        else
                                            src.sendMessage(Text.of("§eYour §6" + nbt.getString("Nickname") + "§e had its level fixed!"));
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Got cost but no confirmation; end of the line. Exit.");

                                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                                    src.sendMessage(Text.of("§6Warning: §eYou're about to lower this Pokémon's level to §6" + (configLevel - 1) + "§e."));
                                    if (commandCost > 0)
                                        src.sendMessage(Text.of("§6Doing this will cost you §c" + commandCost + "§6 coins."));
                                    src.sendMessage(Text.of("§2Ready? Type: §a" + alias + " " + slot + " -c"));
                                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                                }
                            }
                        }
                    }
                }
            }
        }
        else
            PixelUpgrade.log.info("§cThis command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're sure!"));
        if (cost > 0)
            player.sendMessage(Text.of("§eConfirming will cost you §6" + cost + "§e coins."));
        player.sendMessage(Text.of("§5-----------------------------------------------------"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("§4FixLevel // critical: §c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("§3FixLevel // notice: §b" + inputString);
            else
                PixelUpgrade.log.info("§2FixLevel // debug: §a" + inputString);
        }
    }
}
