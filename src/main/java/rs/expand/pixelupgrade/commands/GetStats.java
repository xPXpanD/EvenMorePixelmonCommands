package rs.expand.pixelupgrade.commands;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import rs.expand.pixelupgrade.PixelUpgrade;

public class GetStats implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        Player player = (Player) src;
        Optional<Player> target = player.getPlayer();
        String targetString;
        Boolean targetAcquired = false, canContinue = true;
        Integer slot = 0;

        PixelUpgrade.log.info("\u00A7bGetStats: Called by player " + player.getName() + ", starting command.");

        if (args.<String>getOne("target or slot").isPresent())
        {
            targetString = args.<String>getOne("target or slot").get();
            target = Sponge.getServer().getPlayer(targetString);
            if (!args.<String>getOne("slot").isPresent())
            {
                if (targetString.matches("^[1-6]"))
                    slot = Integer.parseInt(targetString);
                else
                {
                    if (target.isPresent())
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cFound a target, but no slot was provided."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));
                    }
                    else if (targetString.matches("^[0-9].*"))
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds! Valid values are 1-6."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));
                    }
                    else
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));
                    }

                    canContinue = false;
                }
            }
            else if (!target.isPresent())
            {
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYour target does not exist, or is offline."));
                player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));

                canContinue = false;
            }
            else
            {
                try
                {
                    slot = Integer.parseInt(args.<String>getOne("slot").get());

                    if (!(slot < 7 && slot > 0))
                    {
                        player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot value out of bounds. Valid values are 1-6."));
                        player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));

                        canContinue = false;
                    }
                    else
                        targetAcquired = true;
                }
                catch (Exception F)
                {
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                    player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));

                    canContinue = false;
                }
            }
        }
        else
        {
            player.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide at least a slot."));
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/gs (optional target) <slot, 1-6>"));

            canContinue = false;
        }

        if (canContinue)
        {
            Optional<PlayerStorage> storage;
            if (targetAcquired)
                storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) target.get()));
            else
                storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));

            PlayerStorage storageCompleted = storage.get();
            NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

            if (!targetAcquired && nbt == null)
                player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
            else if (nbt != null)
            {
                if (nbt.getBoolean("isEgg"))
                    player.sendMessage(Text.of("\u00A74Error: \u00A7cI cannot peer into an egg. Check out \u00A74/checkegg\u00A7c."));
                else
                {
                    EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());

                    Integer IVHP = nbt.getInteger(NbtKeys.IV_HP);
                    Integer IVATK = nbt.getInteger(NbtKeys.IV_ATTACK);
                    Integer IVDEF = nbt.getInteger(NbtKeys.IV_DEFENCE);
                    Integer IVSPATK = nbt.getInteger(NbtKeys.IV_SP_ATT);
                    Integer IVSPDEF = nbt.getInteger(NbtKeys.IV_SP_DEF);
                    Integer IVSPD = nbt.getInteger(NbtKeys.IV_SPEED);
                    Integer totalIVs = IVHP + IVATK + IVDEF + IVSPATK + IVSPDEF + IVSPD;
                    Integer percentIVs = totalIVs * 100 / 186;

                    Integer EVHP = nbt.getInteger(NbtKeys.EV_HP);
                    Integer EVATK = nbt.getInteger(NbtKeys.EV_ATTACK);
                    Integer EVDEF = nbt.getInteger(NbtKeys.EV_DEFENCE);
                    Integer EVSPATK = nbt.getInteger(NbtKeys.EV_SPECIAL_ATTACK);
                    Integer EVSPDEF = nbt.getInteger(NbtKeys.EV_SPECIAL_DEFENCE);
                    Integer EVSPD = nbt.getInteger(NbtKeys.EV_SPEED);
                    Integer totalEVs = EVHP + EVATK + EVDEF + EVSPATK + EVSPDEF + EVSPD;
                    Integer percentEVs = totalEVs * 100 / 510;

                    Integer natureNum = nbt.getInteger(NbtKeys.NATURE);
                    Integer growthNum = nbt.getInteger(NbtKeys.GROWTH);
                    Integer genderNum = nbt.getInteger(NbtKeys.GENDER);
                    Integer fuseCount = pokemon.getEntityData().getInteger("fuseCount");
                    Integer upgradeCount = pokemon.getEntityData().getInteger("upgradeCount");
                    String natureName, plusVal, minusVal, growthName, genderName;
                    String ivs1, ivs2, ivs3, ivs4, ivs5, ivs6;
                    String evs1, evs2, evs3, evs4, evs5, evs6;

                    Boolean isShiny, isLegendary, isBaby = false;
                    isShiny = nbt.getInteger(NbtKeys.IS_SHINY) == 1;
                    if (nbt.getString("Name").equals("Riolu") || nbt.getString("Name").equals("Mime Jr.") || nbt.getString("Name").equals("Happiny"))
                        isBaby = true;
                    isLegendary = EnumPokemon.legendaries.contains(nbt.getString("Name"));

                    if (IVHP == 31)
                        ivs1 = String.valueOf("\u00A7o" + IVHP + " \u00A72HP \u00A7r\u00A7e|\u00A7a ");
                    else
                        ivs1 = String.valueOf(IVHP + " \u00A72HP \u00A7e|\u00A7a ");

                    if (IVATK == 31)
                        ivs2 = String.valueOf("\u00A7o" + IVATK + " \u00A72ATK \u00A7r\u00A7e|\u00A7a ");
                    else
                        ivs2 = String.valueOf(IVATK + " \u00A72ATK \u00A7e|\u00A7a ");

                    if (IVDEF == 31)
                        ivs3 = String.valueOf("\u00A7o" + IVDEF + " \u00A72DEF \u00A7r\u00A7e|\u00A7a ");
                    else
                        ivs3 = String.valueOf(IVDEF + " \u00A72DEF \u00A7e|\u00A7a ");

                    if (IVSPATK == 31)
                        ivs4 = String.valueOf("\u00A7o" + IVSPATK + " \u00A72Sp. ATK \u00A7r\u00A7e|\u00A7a ");
                    else
                        ivs4 = String.valueOf(IVSPATK + " \u00A72Sp. ATK \u00A7e|\u00A7a ");

                    if (IVSPDEF == 31)
                        ivs5 = String.valueOf("\u00A7o" + IVSPDEF + " \u00A72Sp. DEF \u00A7r\u00A7e|\u00A7a ");
                    else
                        ivs5 = String.valueOf(IVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a ");

                    if (IVSPD == 31)
                        ivs6 = String.valueOf("\u00A7o" + IVSPD + " \u00A72SPD");
                    else
                        ivs6 = String.valueOf(IVSPD + " \u00A72SPD");

                    if (EVHP == 252)
                        evs1 = String.valueOf("\u00A7o" + EVHP + " \u00A72HP \u00A7r\u00A7e|\u00A7a ");
                    else if (EVHP > 252 && EVHP < 256)
                        evs1 = String.valueOf("\u00A7c" + EVHP + " \u00A74HP \u00A7e|\u00A7a ");
                    else
                        evs1 = String.valueOf(EVHP + " \u00A72HP \u00A7e|\u00A7a ");

                    if (EVATK == 252)
                        evs2 = String.valueOf("\u00A7o" + EVATK + " \u00A72ATK \u00A7r\u00A7e|\u00A7a ");
                    else if (EVATK > 252 && EVATK < 256)
                        evs2 = String.valueOf("\u00A7c" + EVATK + " \u00A74ATK \u00A7e|\u00A7a ");
                    else
                        evs2 = String.valueOf(EVATK + " \u00A72ATK \u00A7e|\u00A7a ");

                    if (EVDEF == 252)
                        evs3 = String.valueOf("\u00A7o" + EVDEF + " \u00A72DEF \u00A7r\u00A7e|\u00A7a ");
                    else if (EVDEF > 252 && EVDEF < 256)
                        evs3 = String.valueOf("\u00A7c" + EVDEF + " \u00A74DEF \u00A7e|\u00A7a ");
                    else
                        evs3 = String.valueOf(EVDEF + " \u00A72DEF \u00A7e|\u00A7a ");

                    if (EVSPATK == 252)
                        evs4 = String.valueOf("\u00A7o" + EVSPATK + " \u00A72Sp. ATK \u00A7r\u00A7e|\u00A7a ");
                    else if (EVSPATK > 252 && EVSPATK < 256)
                        evs4 = String.valueOf("\u00A7c" + EVSPATK + " \u00A74Sp. ATK \u00A7e|\u00A7a ");
                    else
                        evs4 = String.valueOf(EVSPATK + " \u00A72Sp. ATK \u00A7e|\u00A7a ");

                    if (EVSPDEF == 252)
                        evs5 = String.valueOf("\u00A7o" + EVSPDEF + " \u00A72Sp. DEF \u00A7r\u00A7e|\u00A7a ");
                    else if (EVSPDEF > 252 && EVSPDEF < 256)
                        evs5 = String.valueOf("\u00A7c" + EVSPDEF + " \u00A74Sp. DEF \u00A7e|\u00A7a ");
                    else
                        evs5 = String.valueOf(EVSPDEF + " \u00A72Sp. DEF \u00A7e|\u00A7a ");

                    if (EVSPD == 252)
                        evs6 = String.valueOf("\u00A7o" + EVSPD + " \u00A72SPD");
                    else if (EVSPD > 252 && EVSPD < 256)
                        evs6 = String.valueOf("\u00A7m" + EVSPD + " \u00A72SPD");
                    else
                        evs6 = String.valueOf(EVSPD + " \u00A72SPD");

                    switch (natureNum)
                    {
                        case 0:
                            natureName = "Hardy";
                            plusVal = "+NONE";
                            minusVal = "-NONE";
                            break;
                        case 1:
                            natureName = "Serious";
                            plusVal = "+NONE";
                            minusVal = "-NONE";
                            break;
                        case 2:
                            natureName = "Docile";
                            plusVal = "+NONE";
                            minusVal = "-NONE";
                            break;
                        case 3:
                            natureName = "Bashful";
                            plusVal = "+NONE";
                            minusVal = "-NONE";
                            break;
                        case 4:
                            natureName = "Quirky";
                            plusVal = "+NONE";
                            minusVal = "-NONE";
                            break;
                        case 5:
                            natureName = "Lonely";
                            plusVal = "+ATK";
                            minusVal = "-DEF";
                            break;
                        case 6:
                            natureName = "Brave";
                            plusVal = "+ATK";
                            minusVal = "-SPD";
                            break;
                        case 7:
                            natureName = "Adamant";
                            plusVal = "+ATK";
                            minusVal = "-SP. ATK";
                            break;
                        case 8:
                            natureName = "Naughty";
                            plusVal = "+ATK";
                            minusVal = "-SP. DEF";
                            break;
                        case 9:
                            natureName = "Bold";
                            plusVal = "+DEF";
                            minusVal = "-ATK";
                            break;
                        case 10:
                            natureName = "Relaxed";
                            plusVal = "+DEF";
                            minusVal = "-SPD";
                            break;
                        case 11:
                            natureName = "Impish";
                            plusVal = "+DEF";
                            minusVal = "-SP. ATK";
                            break;
                        case 12:
                            natureName = "Lax";
                            plusVal = "+DEF";
                            minusVal = "-SP. DEF";
                            break;
                        case 13:
                            natureName = "Timid";
                            plusVal = "+SPD";
                            minusVal = "-ATK";
                            break;
                        case 14:
                            natureName = "Hasty";
                            plusVal = "+SPD";
                            minusVal = "-DEF";
                            break;
                        case 15:
                            natureName = "Jolly";
                            plusVal = "+SPD";
                            minusVal = "-SP. ATK";
                            break;
                        case 16:
                            natureName = "Naive";
                            plusVal = "+SPD";
                            minusVal = "-SP. DEF";
                            break;
                        case 17:
                            natureName = "Modest";
                            plusVal = "+SP. ATK";
                            minusVal = "-ATK";
                            break;
                        case 18:
                            natureName = "Mild";
                            plusVal = "+SP. ATK";
                            minusVal = "-DEF";
                            break;
                        case 19:
                            natureName = "Quiet";
                            plusVal = "+SP. ATK";
                            minusVal = "-SPD";
                            break;
                        case 20:
                            natureName = "Rash";
                            plusVal = "+SP. ATK";
                            minusVal = "-SP. DEF";
                            break;
                        case 21:
                            natureName = "Calm";
                            plusVal = "+SP. DEF";
                            minusVal = "-ATK";
                            break;
                        case 22:
                            natureName = "Gentle";
                            plusVal = "+SP. DEF";
                            minusVal = "-DEF";
                            break;
                        case 23:
                            natureName = "Sassy";
                            plusVal = "+SP. DEF";
                            minusVal = "-SPD";
                            break;
                        case 24:
                            natureName = "Careful";
                            plusVal = "+SP. DEF";
                            minusVal = "-SP. ATK";
                            break;
                        default:
                            natureName = "Not found? Please report this.";
                            plusVal = "+N/A";
                            minusVal = "-N/A";
                            break;
                    }

                    switch (growthNum)
                    {
                        case 0:
                            growthName = "Pygmy";
                            break;
                        case 1:
                            growthName = "Runt";
                            break;
                        case 2:
                            growthName = "Small";
                            break;
                        case 3:
                            growthName = "Ordinary";
                            break;
                        case 4:
                            growthName = "Huge";
                            break;
                        case 5:
                            growthName = "Giant";
                            break;
                        case 6:
                            growthName = "Enormous";
                            break;
                        case 7:
                            growthName = "\u00A7cGinormous";
                            break;
                        case 8:
                            growthName = "\u00A7aMicroscopic";
                            break;
                        default:
                            growthName = "Not found? Please report this.";
                            break;
                    }

                    switch (genderNum)
                    {
                        case 0:
                            genderName = "\u2642";
                            break;
                        case 1:
                            genderName = "\u2640";
                            break;
                        case 2:
                            genderName = "\u26A5";
                            break;
                        default:
                            genderName = "Not found? Please report this.";
                            break;
                    }

                    if (targetAcquired)
                    {
                        if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.get().getName() + "\u00A76's \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname")));
                        else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.get().getName() + "\u00A76's \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname") + "\u00A7e (\u00A7fshiny\u00A7e)"));
                        else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.get().getName() + "\u00A76's \u00A7c" + nbt.getString("Name") + "\u00A7e (\u00A7fshiny\u00A7e)"));
                        else
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + target.get().getName() + "\u00A76's \u00A7c" + nbt.getString("Name")));

                        player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e)"));
                        player.sendMessage(Text.of("\u00A7eIVs: \u00A7a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

                        player.sendMessage(Text.of("\u00A7eTotal EVs: \u00A7a" + totalEVs + "\u00A7e/\u00A7a510\u00A7e (\u00A7a" + percentEVs + "%\u00A7e)"));
                        player.sendMessage(Text.of("\u00A7eEVs: \u00A7a" + evs1 + "" + evs2 + "" + evs3 + "" + evs4 + "" + evs5 + "" + evs6));

                        player.sendMessage(Text.of("\u00A7eGender: \u00A7f" + genderName + "\u00A7f | \u00A7eSize: \u00A7f" + growthName + "\u00A7f | \u00A7eNature: \u00A7f" + natureName + "\u00A7e (\u00A7a" + plusVal + "\u00A7e / \u00A7c" + minusVal + "\u00A7e)"));

                        if (isShiny && nbt.getString("Name").equals("Ditto") && fuseCount != 0 && fuseCount < 10)
                            player.sendMessage(Text.of("\u00A76This shiny Ditto has been fused \u00A7c" + pokemon.getEntityData().getInteger("fuseCount") + "\u00A76/\u00A7c10 \u00A76times."));
                        else if (isShiny && nbt.getString("Name").equals("Ditto") && fuseCount == 0)
                            player.sendMessage(Text.of("\u00A76This shiny Ditto can be fused \u00A7c10 \u00A76more times!"));
                        else if (isShiny && nbt.getString("Name").equals("Ditto"))
                            player.sendMessage(Text.of("\u00A76This shiny Ditto cannot be fused any further!"));
                        else if (!isShiny && nbt.getString("Name").equals("Ditto") && fuseCount != 0 && fuseCount < 10)
                            player.sendMessage(Text.of("\u00A76This Ditto has been fused \u00A7c" + pokemon.getEntityData().getInteger("fuseCount") + "\u00A76/\u00A7c5 \u00A76times."));
                        else if (!isShiny && nbt.getString("Name").equals("Ditto") && fuseCount == 0)
                            player.sendMessage(Text.of("\u00A76This Ditto can be fused \u00A7c5 \u00A76more times!"));
                        else if (!isShiny && nbt.getString("Name").equals("Ditto"))
                            player.sendMessage(Text.of("\u00A76This Ditto cannot be fused any further!"));
                        else if (isShiny && upgradeCount != 0 && upgradeCount < 60)
                            player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c60 \u00A76times."));
                        else if (isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon can be upgraded \u00A7c60 \u00A76more times!"));
                        else if (isShiny)
                            player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon has been fully upgraded!"));
                        else if (isLegendary && !isShiny && upgradeCount != 0 && upgradeCount < 20)
                            player.sendMessage(Text.of("\u00A76This legendary Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c20 \u00A76times."));
                        else if (isLegendary && !isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This legendary Pok\u00E9mon can be upgraded \u00A7c20 \u00A76more times!"));
                        else if (isLegendary && !isShiny)
                            player.sendMessage(Text.of("\u00A76This legendary Pok\u00E9mon has been fully upgraded!"));
                        else if (isBaby && !isShiny && upgradeCount != 0 && upgradeCount < 25)
                            player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c25 \u00A76times."));
                        else if (isBaby && !isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon can be upgraded \u00A7c25 \u00A76more times!"));
                        else if (isBaby && !isShiny)
                            player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon has been fully upgraded!"));
                        else if (!isShiny && upgradeCount != 0 && upgradeCount < 35)
                            player.sendMessage(Text.of("\u00A76This Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c35 \u00A76times."));
                        else if (!isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This Pok\u00E9mon can be upgraded \u00A7c35 \u00A76more times!"));
                        else if (!isShiny)
                            player.sendMessage(Text.of("\u00A76This Pok\u00E9mon has been fully upgraded!"));
                        else
                            player.sendMessage(Text.of("\u00A7cCould not figure out upgrade status. Please report this."));
                    }
                    else
                    {
                        if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) != 1)
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname")));
                        else if (!nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name") + "\u00A76, also known as \u00A7c" + nbt.getString("Nickname") + "\u00A7e (\u00A7fshiny\u00A7e)"));
                        else if (nbt.getString("Nickname").equals("") && nbt.getInteger(NbtKeys.IS_SHINY) == 1)
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name") + "\u00A7e (\u00A7fshiny\u00A7e)"));
                        else
                            player.sendMessage(Text.of("\u00A76Stats of \u00A7c" + nbt.getString("Name")));

                        player.sendMessage(Text.of("\u00A7eTotal IVs: \u00A7a" + totalIVs + "\u00A7e/\u00A7a186\u00A7e (\u00A7a" + percentIVs + "%\u00A7e)"));
                        player.sendMessage(Text.of("\u00A7eIVs: \u00A7a" + ivs1 + "" + ivs2 + "" + ivs3 + "" + ivs4 + "" + ivs5 + "" + ivs6));

                        player.sendMessage(Text.of("\u00A7eTotal EVs: \u00A7a" + totalEVs + "\u00A7e/\u00A7a510\u00A7e (\u00A7a" + percentEVs + "%\u00A7e)"));
                        player.sendMessage(Text.of("\u00A7eEVs: \u00A7a" + evs1 + "" + evs2 + "" + evs3 + "" + evs4 + "" + evs5 + "" + evs6));

                        player.sendMessage(Text.of("\u00A7eGender: \u00A7f" + genderName + "\u00A7f | \u00A7eSize: \u00A7f" + growthName + "\u00A7f | \u00A7eNature: \u00A7f" + natureName + "\u00A7e (\u00A7a" + plusVal + "\u00A7e / \u00A7c" + minusVal + "\u00A7e)"));

                        if (isShiny && nbt.getString("Name").equals("Ditto") && fuseCount != 0 && fuseCount < 10)
                            player.sendMessage(Text.of("\u00A76This shiny Ditto has been fused \u00A7c" + pokemon.getEntityData().getInteger("fuseCount") + "\u00A76/\u00A7c10 \u00A76times."));
                        else if (isShiny && nbt.getString("Name").equals("Ditto") && fuseCount == 0)
                            player.sendMessage(Text.of("\u00A76This shiny Ditto can be fused \u00A7c10 \u00A76more times!"));
                        else if (isShiny && nbt.getString("Name").equals("Ditto"))
                            player.sendMessage(Text.of("\u00A76This shiny Ditto cannot be fused any further!"));
                        else if (!isShiny && nbt.getString("Name").equals("Ditto") && fuseCount != 0 && fuseCount < 10)
                            player.sendMessage(Text.of("\u00A76This Ditto has been fused \u00A7c" + pokemon.getEntityData().getInteger("fuseCount") + "\u00A76/\u00A7c5 \u00A76times."));
                        else if (!isShiny && nbt.getString("Name").equals("Ditto") && fuseCount == 0)
                            player.sendMessage(Text.of("\u00A76This Ditto can be fused \u00A7c5 \u00A76more times!"));
                        else if (!isShiny && nbt.getString("Name").equals("Ditto"))
                            player.sendMessage(Text.of("\u00A76This Ditto cannot be fused any further!"));
                        else if (isShiny && upgradeCount != 0 && upgradeCount < 60)
                            player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c60 \u00A76times."));
                        else if (isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon can be upgraded \u00A7c60 \u00A76more times!"));
                        else if (isShiny)
                            player.sendMessage(Text.of("\u00A76This shiny Pok\u00E9mon has been fully upgraded!"));
                        else if (isLegendary && !isShiny && upgradeCount != 0 && upgradeCount < 20)
                            player.sendMessage(Text.of("\u00A76This legendary Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c20 \u00A76times."));
                        else if (isLegendary && !isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This legendary Pok\u00E9mon can be upgraded \u00A7c20 \u00A76more times!"));
                        else if (isLegendary && !isShiny)
                            player.sendMessage(Text.of("\u00A76This legendary Pok\u00E9mon has been fully upgraded!"));
                        else if (isBaby && !isShiny && upgradeCount != 0 && upgradeCount < 25)
                            player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c25 \u00A76times."));
                        else if (isBaby && !isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon can be upgraded \u00A7c25 \u00A76more times!"));
                        else if (isBaby && !isShiny)
                            player.sendMessage(Text.of("\u00A76This baby Pok\u00E9mon has been fully upgraded!"));
                        else if (!isShiny && upgradeCount != 0 && upgradeCount < 35)
                            player.sendMessage(Text.of("\u00A76This Pok\u00E9mon has been upgraded \u00A7c" + pokemon.getEntityData().getInteger("upgradeCount") + "\u00A76/\u00A7c35 \u00A76times."));
                        else if (!isShiny && upgradeCount == 0)
                            player.sendMessage(Text.of("\u00A76This Pok\u00E9mon can be upgraded \u00A7c35 \u00A76more times!"));
                        else if (!isShiny)
                            player.sendMessage(Text.of("\u00A76This Pok\u00E9mon has been fully upgraded!"));
                        else
                            player.sendMessage(Text.of("\u00A7cCould not figure out upgrade status. Please report this."));

                        if (EVHP < 256 && EVHP > 252 || EVATK < 256 && EVATK > 252 || EVDEF < 256 && EVDEF > 252 || EVSPATK < 256 && EVSPATK > 252 || EVSPDEF < 256 && EVSPDEF > 252 || EVSPD < 256 && EVSPD > 252)
                            player.sendMessage(Text.of("\u00A75Warning: \u00A7dEVs above 252 do nothing. Try using \u00A75/fixevs\u00A7d."));
                    }
                }
            }
            else
            {
                Integer slotTicker = 0;
                player.sendMessage(Text.of("\u00A75Info: \u00A7dThat slot is empty, showing whole team!"));

                for (NBTTagCompound loopValue : storageCompleted.partyPokemon)
                {
                    if (slotTicker > 5)
                        break;

                    if (loopValue == null)
                        player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A72Empty\u00A7a."));
                    else
                    {
                        if (!loopValue.getString("Nickname").equals(""))
                            player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A7aA level " + loopValue.getInteger("Level") + "\u00A72 " + loopValue.getString("Name") + "\u00A7a, also known as \u00A72" + loopValue.getString("Nickname") + "\u00A7a."));
                        else
                            player.sendMessage(Text.of("\u00A73Slot " + (slotTicker + 1) + ": \u00A7aA level " + loopValue.getInteger("Level") + "\u00A72 " + loopValue.getString("Name") + "\u00A7a."));
                    }

                    slotTicker++;
                }

                player.sendMessage(Text.of("\u00A75Info: \u00A7dWant to know more? Use: \u00A75/getstats (player) <slot>"));
            }
        }
        return CommandResult.success();
	}
}