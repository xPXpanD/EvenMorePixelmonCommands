package rs.expand.pixelupgrade.commands;

import java.math.BigDecimal;
import java.util.Optional;

import com.pixelmonmod.pixelmon.config.PixelmonConfig;
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
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.FixLevelConfig;

import static rs.expand.pixelupgrade.PixelUpgrade.economyService;

public class FixLevel implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!FixLevelConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = FixLevelConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("§4FixLevel // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("§4FixLevel // critical: §cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    private static String alias;
    private void getCommandAlias()
    {
        if (!FixLevelConfig.getInstance().getConfig().getNode("commandAlias").isVirtual())
            alias = "/" + FixLevelConfig.getInstance().getConfig().getNode("commandAlias").getString();
        else
        {
            PixelUpgrade.log.info("§4FixLevel // critical: §cConfig variable \"commandAlias\" could not be found!");
            alias = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args)
    {
        if (src instanceof Player)
        {
            Integer commandCost = null;
            if (!FixLevelConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                commandCost = FixLevelConfig.getInstance().getConfig().getNode("commandCost").getInt();
            else
                PixelUpgrade.log.info("§4FixLevel // critical: §cCould not parse config variable \"commandCost\"!");

            // Set up the command's debug verbosity mode and preferred alias.
            getVerbosityMode();
            getCommandAlias();

            if (commandCost == null || alias == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("§4FixLevel // critical: §cCheck your config. If need be, wipe and §4/pureload§c.");
            }
            else
            {
                printToLog(2, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

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
                        printToLog(3, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot provided. Aborting.");

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
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
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
                            printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(2, "Tried to fix level on an egg. Aborting...");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            int pokemonLevel = pokemon.getLvl().getLevel(), configLevel = PixelmonConfig.maxLevel;

                            if (pokemonLevel != configLevel)
                            {
                                printToLog(2, "Config max level and provided Pokémon's level did not match. Abort.");
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
                                            TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(), costToConfirm, Cause.source(this).build());

                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                printToLog(1, "Fixed level for slot " + slot + ", and took " + costToConfirm + " coins.");
                                                pokemon.getLvl().setLevel(configLevel - 1);
                                            }
                                            else
                                            {
                                                BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                                printToLog(2, "Not enough coins! Cost: §3" + costToConfirm + "§b, lacking: §3" + balanceNeeded);

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
                                        printToLog(2, "Succesfully fixed the level of a Pokémon!");

                                        if (nbt.getString("Nickname").equals(""))
                                            src.sendMessage(Text.of("§6" + nbt.getString("Name") + "§e has had its level fixed!"));
                                        else
                                            src.sendMessage(Text.of("§eYour §6" + nbt.getString("Nickname") + "§e has had its level fixed!"));
                                        src.sendMessage(Text.of("§2Note: §aPlease reconnect for changes to save and show."));
                                    }
                                }
                                else
                                {
                                    printToLog(2, "Got cost but no confirmation; end of the line.");

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
            printToLog(0, "This command cannot run from the console or command blocks.");

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
                PixelUpgrade.log.info("§6FixLevel // important: §e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("§3FixLevel // start/end: §b" + inputString);
            else
                PixelUpgrade.log.info("§2FixLevel // debug: §a" + inputString);
        }
    }
}
