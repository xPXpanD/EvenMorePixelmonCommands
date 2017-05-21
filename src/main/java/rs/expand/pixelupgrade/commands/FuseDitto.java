package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.world.World;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;

import java.util.Optional;

public class FuseDitto implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Integer slot1 = 0, slot2 = 0;
        Boolean commandConfirmed = false, canContinue = true;
        Player player = (Player) src;

        PixelUpgrade.log.info("\u00A7bFuseDitto: Called by player " + player.getName() + ", starting command.");

        if (!args.<String>getOne("target slot").isPresent())
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo slots were provided. Please provide two valid slots."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot> <sacrifice slot> (-c to confirm)"));

            canContinue = false;
        }
        else
        {
            String slotString = args.<String>getOne("target slot").get();

            if (slotString.matches("^[1-6]"))
                slot1 = Integer.parseInt(args.<String>getOne("target slot").get());
            else
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid value on target slot. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot> <sacrifice slot> (-c to confirm)"));

                canContinue = false;
            }
        }

        if (!args.<String>getOne("sacrifice slot").isPresent() && canContinue)
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo sacrifice slot provided. Please provide two valid slots."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot> <sacrifice slot> (-c to confirm)"));

            canContinue = false;
        }
        else if (canContinue)
        {
            String slotString = args.<String>getOne("sacrifice slot").get();

            if (slotString.matches("^[1-6]"))
                slot2 = Integer.parseInt(args.<String>getOne("sacrifice slot").get());
            else
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid value on sacrifice slot. Valid values are 1-6."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/fuseditto <target slot> <sacrifice slot> (-c to confirm)"));

                canContinue = false;
            }
        }

        if (args.hasAny("c"))
            commandConfirmed = true;

        if (canContinue)
        {
            Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
            PlayerStorage storageCompleted = (PlayerStorage) storage.get();
            NBTTagCompound nbt1 = storageCompleted.partyPokemon[slot1 - 1];
            NBTTagCompound nbt2 = storageCompleted.partyPokemon[slot2 - 1];

            if (nbt1 == null && nbt2 != null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cThe target Pok\u00E9mon does not seem to exist."));
            else if (nbt1 != null && nbt2 == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cThe sacrifice Pok\u00E9mon does not seem to exist."));
            else if (nbt1 == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cBoth the target and sacrifice do not seem to exist."));
            else
            {
                if (!nbt1.getString("Name").equals("Ditto") && nbt2.getString("Name").equals("Ditto"))
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cYour target Pok\u00E9mon is not a Ditto."));
                else if (nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cSorry, but the sacrifice needs to be a Ditto."));
                else if (!nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cThis command only works on Dittos."));
                else
                {
                    Integer HPUpgradeCount, ATKUpgradeCount, DEFUpgradeCount, SPATKUpgradeCount, SPDEFUpgradeCount, SPDUpgradeCount;
                    EntityPixelmon targetPokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt1, (World) player.getWorld());

                    Integer targetHP = nbt1.getInteger(NbtKeys.IV_HP);
                    Integer targetATK = nbt1.getInteger(NbtKeys.IV_ATTACK);
                    Integer targetDEF = nbt1.getInteger(NbtKeys.IV_DEFENCE);
                    Integer targetSPATK = nbt1.getInteger(NbtKeys.IV_SP_ATT);
                    Integer targetSPDEF = nbt1.getInteger(NbtKeys.IV_SP_DEF);
                    Integer targetSPD = nbt1.getInteger(NbtKeys.IV_SPEED);

                    Integer sacrificeHP = nbt2.getInteger(NbtKeys.IV_HP);
                    Integer sacrificeATK = nbt2.getInteger(NbtKeys.IV_ATTACK);
                    Integer sacrificeDEF = nbt2.getInteger(NbtKeys.IV_DEFENCE);
                    Integer sacrificeSPATK = nbt2.getInteger(NbtKeys.IV_SP_ATT);
                    Integer sacrificeSPDEF = nbt2.getInteger(NbtKeys.IV_SP_DEF);
                    Integer sacrificeSPD = nbt2.getInteger(NbtKeys.IV_SPEED);

                    switch (sacrificeHP/10)
                    {
                        case 0:
                            HPUpgradeCount = 0;
                            break;
                        case 1:
                            HPUpgradeCount = 1;
                            break;
                        case 2:
                            HPUpgradeCount = 2;
                            break;
                        default:
                            HPUpgradeCount = 3;
                            break;
                    }

                    if (targetHP == 31 && HPUpgradeCount == 3)
                        HPUpgradeCount = 1;
                    else if (targetHP >= 32)
                        HPUpgradeCount = 0;
                    else if (HPUpgradeCount + targetHP >= 31)
                        HPUpgradeCount = 31 - targetHP;

                    switch (sacrificeATK/10)
                    {
                        case 0:
                            ATKUpgradeCount = 0;
                            break;
                        case 1:
                            ATKUpgradeCount = 1;
                            break;
                        case 2:
                            ATKUpgradeCount = 2;
                            break;
                        default:
                            ATKUpgradeCount = 3;
                            break;
                    }

                    if (targetATK == 31 && ATKUpgradeCount == 3)
                        ATKUpgradeCount = 1;
                    else if (targetATK >= 32)
                        ATKUpgradeCount = 0;
                    else if (ATKUpgradeCount + targetATK >= 31)
                        ATKUpgradeCount = 31 - targetATK;

                    switch (sacrificeDEF/10)
                    {
                        case 0:
                            DEFUpgradeCount = 0;
                            break;
                        case 1:
                            DEFUpgradeCount = 1;
                            break;
                        case 2:
                            DEFUpgradeCount = 2;
                            break;
                        default:
                            DEFUpgradeCount = 3;
                            break;
                    }

                    if (targetDEF == 31 && DEFUpgradeCount == 3)
                        DEFUpgradeCount = 1;
                    else if (targetDEF >= 32)
                        DEFUpgradeCount = 0;
                    else if (DEFUpgradeCount + targetDEF >= 31)
                        DEFUpgradeCount = 31 - targetDEF;

                    switch (sacrificeSPATK/10)
                    {
                        case 0:
                            SPATKUpgradeCount = 0;
                            break;
                        case 1:
                            SPATKUpgradeCount = 1;
                            break;
                        case 2:
                            SPATKUpgradeCount = 2;
                            break;
                        default:
                            SPATKUpgradeCount = 3;
                            break;
                    }

                    if (targetSPATK == 31 && SPATKUpgradeCount == 3)
                        SPATKUpgradeCount = 1;
                    else if (targetSPATK >= 32)
                        SPATKUpgradeCount = 0;
                    else if (SPATKUpgradeCount + targetSPATK >= 31)
                        SPATKUpgradeCount = 31 - targetSPATK;

                    switch (sacrificeSPDEF/10)
                    {
                        case 0:
                            SPDEFUpgradeCount = 0;
                            break;
                        case 1:
                            SPDEFUpgradeCount = 1;
                            break;
                        case 2:
                            SPDEFUpgradeCount = 2;
                            break;
                        default:
                            SPDEFUpgradeCount = 3;
                            break;
                    }

                    if (targetSPDEF == 31 && SPDEFUpgradeCount == 3)
                        SPDEFUpgradeCount = 1;
                    else if (targetSPDEF >= 32)
                        SPDEFUpgradeCount = 0;
                    else if (SPDEFUpgradeCount + targetSPDEF >= 31)
                        SPDEFUpgradeCount = 31 - targetSPDEF;

                    switch (sacrificeSPD/10)
                    {
                        case 0:
                            SPDUpgradeCount = 0;
                            break;
                        case 1:
                            SPDUpgradeCount = 1;
                            break;
                        case 2:
                            SPDUpgradeCount = 2;
                            break;
                        default:
                            SPDUpgradeCount = 3;
                            break;
                    }

                    if (targetSPD == 31 && SPDUpgradeCount == 3)
                        SPDUpgradeCount = 1;
                    else if (targetSPD >= 32)
                        SPDUpgradeCount = 0;
                    else if (SPDUpgradeCount + targetSPD >= 31)
                        SPDUpgradeCount = 31 - targetSPD;

                    String IVs1 = String.valueOf(targetHP + " \u00A72HP \u00A7e| \u00A7a" + targetATK + " \u00A72DEF \u00A7e| \u00A7a" + targetDEF + " \u00A72DEF \u00A7e| \u00A7a");
                    String IVs2 = String.valueOf(targetSPATK + " \u00A72DEF \u00A7e| \u00A7a" + targetSPDEF + " \u00A72DEF \u00A7e| \u00A7a" + targetSPD + " \u00A72DEF");

                    //NONSENSICAL CODE BELOW HERE
                    String UpgradedIVs1 = String.valueOf(targetHP + " \u00A72HP \u00A7e| \u00A7a" + targetATK + " \u00A72DEF \u00A7e| \u00A7a" + targetDEF + " \u00A72DEF \u00A7e| \u00A7a");
                    String UpgradedIVs2 = String.valueOf(targetSPATK + " \u00A72DEF \u00A7e| \u00A7a" + targetSPDEF + " \u00A72DEF \u00A7e| \u00A7a" + targetSPD + " \u00A72DEF");

                    Integer totalUpgradeCount = HPUpgradeCount + ATKUpgradeCount + DEFUpgradeCount + SPATKUpgradeCount + SPDEFUpgradeCount + SPDUpgradeCount;
                    Double costToConfirm = totalUpgradeCount * 25.0;

                    player.sendMessage(Text.of("\u00A74Debug: \u00A7cTarget: " + targetHP + " " + targetATK + " " + targetDEF + " " + targetSPATK + " " + targetSPDEF + " " + targetSPD));
                    player.sendMessage(Text.of("\u00A74Debug: \u00A7cSacrifice: " + sacrificeHP + " " + sacrificeATK + " " + sacrificeDEF + " " + sacrificeSPATK + " " + sacrificeSPDEF + " " + sacrificeSPD));
                    player.sendMessage(Text.of("\u00A74Debug: \u00A7cCount: " + HPUpgradeCount + " " + ATKUpgradeCount + " " + DEFUpgradeCount + " " + SPATKUpgradeCount + " " + SPDEFUpgradeCount + " " + SPDUpgradeCount));
                    player.sendMessage(Text.of("\u00A74Debug: \u00A7cUpgrade count: " + totalUpgradeCount + ", Confirmation cost:" + costToConfirm));

                    if (!commandConfirmed)
                    {
                        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        player.sendMessage(Text.of("\u00A7bYou are about to upgrade the Ditto in slot \u00A73" + slot1 + "\u00A7b."));
                        player.sendMessage(Text.of("\u00A7bThe other Ditto in slot \u00A73" + slot2 + "\u00A7bwill be \u00A7ldeleted\u00A7r\u00A7b!"));
                        player.sendMessage(Text.of(""));
                        player.sendMessage(Text.of("\u00A7eCurrent IVs: \u00A7a" + IVs1 + IVs2));
                        player.sendMessage(Text.of("\u00A7eAfter upgrade: \u00A7a" + UpgradedIVs1 + UpgradedIVs2));
                        player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    }
                }
            }
        }

        return CommandResult.success();
    }
}
