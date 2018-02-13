// CheckStats' sister command.
package rs.expand.pixelupgrade.commands;

// Remote imports.
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import rs.expand.pixelupgrade.utilities.PokemonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class CheckEgg implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Boolean showName, explicitReveal, recheckIsFree;
    public static Integer babyHintPercentage, commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("CheckEgg", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final ArrayList<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (showName == null)
                nativeErrorArray.add("showName");
            if (explicitReveal == null)
                nativeErrorArray.add("explicitReveal");
            if (babyHintPercentage == null)
                nativeErrorArray.add("babyHintPercentage");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");
            if (recheckIsFree == null)
                nativeErrorArray.add("recheckIsFree");

            // Also get some stuff from PixelUpgrade.conf.
            final ArrayList<String> mainConfigErrorArray = new ArrayList<>();
            if (shortenedHP == null)
                mainConfigErrorArray.add("shortenedHP");
            if (shortenedAttack == null)
                mainConfigErrorArray.add("shortenedAttack");
            if (shortenedDefense == null)
                mainConfigErrorArray.add("shortenedDefense");
            if (shortenedSpecialAttack == null)
                mainConfigErrorArray.add("shortenedSpecialAttack");
            if (shortenedSpecialDefense == null)
                mainConfigErrorArray.add("shortenedSpecialDefense");
            if (shortenedSpeed == null)
                mainConfigErrorArray.add("shortenedSpeed");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("CheckEgg", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (!mainConfigErrorArray.isEmpty())
            {
                PrintingMethods.printMainNodeError("CheckEgg", mainConfigErrorArray);
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                int slot = 0;
                boolean commandConfirmed = false, canContinue = true;
                final Player player = (Player) src;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    if (commandCost > 0)
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));
                    printSyntaxHelper(src);
                    PrintingMethods.checkAndAddFooter(commandCost, src);

                    canContinue = false;
                }
                else
                {
                    final String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        if (commandCost > 0)
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));

                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printSyntaxHelper(src);
                        PrintingMethods.checkAndAddFooter(commandCost, src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    final Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                    }
                    else
                    {
                        printToLog(2, "Found a Pixelmon storage, moving on.");

                        final PlayerStorage storageCompleted = storage.get();
                        final NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null || !nbt.getBoolean(NbtKeys.IS_EGG))
                        {
                            printToLog(1, "Provided slot was not an egg, or a proper Pokémon. Exit.");
                            src.sendMessage(Text.of("§4Error: §cCould not find an egg in the provided slot."));
                        }
                        else
                        {
                            printToLog(2, "Egg found. Let's do this!");

                            final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            final boolean wasEggChecked = pokemon.getEntityData().getBoolean("hadEggChecked");

                            if (commandCost == 0 || wasEggChecked && recheckIsFree)
                            {
                                printEggResults(nbt, pokemon, wasEggChecked, src);

                                // Keep this below the printEggResults call, or your debug message order will look weird.
                                if (commandCost == 0)
                                {
                                    printToLog(1, "Checking egg in slot §3" + slot +
                                            "§b. Config price is §30§b, taking nothing.");
                                }
                                else
                                {
                                    printToLog(1, "Checking egg, slot §3" + slot +
                                            "§b. Detected a recheck, taking nothing as per config.");
                                }
                            }
                            else
                            {
                                final BigDecimal costToConfirm = new BigDecimal(commandCost);

                                if (commandConfirmed)
                                {
                                    final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                    if (optionalAccount.isPresent())
                                    {
                                        final UniqueAccount uniqueAccount = optionalAccount.get();
                                        final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            printEggResults(nbt, pokemon, wasEggChecked, src);

                                            // Keep this below the printEggResults call, or your debug message order will look weird.
                                            printToLog(1, "Checking egg in slot §3" + slot +
                                                    "§b, and taking §3" + costToConfirm + "§b coins.");
                                        }
                                        else
                                        {
                                            final BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();

                                            printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                                    "§b, and we're lacking §3" + balanceNeeded);
                                            src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
                                        }
                                    }
                                    else
                                    {
                                        printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                                        src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                    }
                                }
                                else
                                {
                                    printToLog(1, "Showed cost, no confirmation was provided. Exit.");

                                    // Is cost to confirm exactly one coin?
                                    if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                        src.sendMessage(Text.of("§6Warning: §eChecking an egg's status costs §6one §ecoin."));
                                    else
                                    {
                                        src.sendMessage(Text.of("§6Warning: §eChecking an egg's status costs §6" +
                                                costToConfirm + "§e coins."));
                                    }

                                    src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                                }
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

    private void printSyntaxHelper(final CommandSource src)
    {
        if (commandCost != 0)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }

    private void printEggResults(final NBTTagCompound nbt, final EntityPixelmon pokemon, final boolean wasEggChecked, final CommandSource src)
    {
        printToLog(2, "We have entered the executing method. Checking stats now!");

        // Set up IVs and matching math.
        final int HPIV = nbt.getInteger(NbtKeys.IV_HP);
        final int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
        final int defenseIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
        final int spAttIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
        final int spDefIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
        final int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
        final int totalIVs = HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV;
        final int percentIVs = totalIVs * 100 / 186;
        final boolean isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
        if (showName)
        {
            src.sendMessage(Text.of("§eThere's a healthy §6" + nbt.getString("Name") + "§e inside of this egg!"));
            if (explicitReveal)
                src.sendMessage(Text.of(""));
        }

        if (explicitReveal)
        {
            printToLog(2, "Explicit reveal enabled. Printing full IVs, shiny-ness and other info.");

            // Format the IVs for use later, so we can print them.
            final String ivs1;
            final String ivs2;
            final String ivs3;
            final String ivs4;
            final String ivs5;
            final String ivs6;
            if (HPIV < 31)
                ivs1 = String.valueOf(HPIV + " §2" + shortenedHP + " §f|§a ");
            else
                ivs1 = String.valueOf("§l" + HPIV + " §2" + shortenedHP + " §r§f|§a ");

            if (attackIV < 31)
                ivs2 = String.valueOf(attackIV + " §2" + shortenedAttack + " §f|§a ");
            else
                ivs2 = String.valueOf("§l" + attackIV + " §2" + shortenedAttack + " §r§f|§a ");

            if (defenseIV < 31)
                ivs3 = String.valueOf(defenseIV + " §2" + shortenedDefense + " §f|§a ");
            else
                ivs3 = String.valueOf("§l" + defenseIV + " §2" + shortenedDefense + " §r§f|§a ");

            if (spAttIV < 31)
                ivs4 = String.valueOf(spAttIV + " §2" + shortenedSpecialAttack + " §f|§a ");
            else
                ivs4 = String.valueOf("§l" + spAttIV + " §2" + shortenedSpecialAttack + " §r§f|§a ");

            if (spDefIV < 31)
                ivs5 = String.valueOf(spDefIV + " §2" + shortenedSpecialDefense + " §f|§a ");
            else
                ivs5 = String.valueOf("§l" + spDefIV + " §2" + shortenedSpecialDefense + " §r§f|§a ");

            if (speedIV < 31)
                ivs6 = String.valueOf(speedIV + " §2" + shortenedSpeed + "");
            else
                ivs6 = String.valueOf("§l" + speedIV + " §2" + shortenedSpeed + "");

            src.sendMessage(Text.of("§bTotal IVs§f: §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)"));
            src.sendMessage(Text.of("§bIVs§f: §a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

            // Get a bunch of data from our PokemonMethods utility class.
            final ArrayList<String> natureArray = PokemonMethods.getNatureStrings(nbt.getInteger(NbtKeys.NATURE));
            final String natureName = natureArray.get(0);
            final String plusVal = natureArray.get(1);
            final String minusVal = natureArray.get(2);
            final String growthName = PokemonMethods.getGrowthName(nbt.getInteger(NbtKeys.GROWTH));

            // Set up a gender character. Console doesn't like Unicode genders, so if src is not a Player we'll use M/F/-.
            final char genderChar = PokemonMethods.getGenderCharacter(src, nbt.getInteger(NbtKeys.GENDER));

            // Show said data.
            final String extraInfo1 = String.valueOf("§bGender§f: " + genderChar +
                    "§f | §bSize§f: " + growthName + "§f | ");
            final String extraInfo2 = String.valueOf("§bNature§f: " + natureName +
                    "§f (§a" + plusVal + "§f/§c" + minusVal + "§f)");
            src.sendMessage(Text.of(extraInfo1 + extraInfo2));

            // Lucky!
            if (isShiny)
            {
                src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("§6§lCongratulations! §r§eThis baby is shiny!"));
            }
        }
        else
        {
            printToLog(2, "Explicit reveal disabled, printing vague status.");

            // Figure out whether the baby is anything special. Uses a config-set percentage for stat checks.
            if (percentIVs >= babyHintPercentage && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                src.sendMessage(Text.of("§6What's this? §eThis baby seems to be bursting with energy!"));
            else if (!(percentIVs >= babyHintPercentage) && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                src.sendMessage(Text.of("§6What's this? §eThis baby seems to have an odd sheen to it!"));
            else if (percentIVs >= babyHintPercentage && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                src.sendMessage(Text.of("§6What's this? §eSomething about this baby seems real special!"));
            else
                src.sendMessage(Text.of("§eThis baby seems to be fairly ordinary..."));
        }

        if (commandCost > 0 && recheckIsFree)
        {
            if (wasEggChecked)
            {
                if (!isShiny || !explicitReveal)
                    src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("§dThis egg was checked before, so this check was free!"));
            }
            else
            {
                printToLog(2, "First-time check, recheckIsFree enabled. Flagging for free rechecks.");
                pokemon.getEntityData().setBoolean("hadEggChecked", true);
            }
        }

        src.sendMessage(Text.of("§7-----------------------------------------------------"));
    }
}
