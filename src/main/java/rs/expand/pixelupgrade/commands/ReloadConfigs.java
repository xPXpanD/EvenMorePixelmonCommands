package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.*;

public class ReloadConfigs implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        src.sendMessage(Text.of("\u00A73PU Reload // notice: \u00A7bStarting reload of all configs..."));

        CheckEggConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckEggPath, PixelUpgrade.getInstance().cmdCheckEggLoader);
        DittoFusionConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdDittoFusionPath, PixelUpgrade.getInstance().cmdDittoFusionLoader);
        FixEVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdFixEVsPath, PixelUpgrade.getInstance().cmdFixEVsLoader);
        ForceStatsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdForceStatsPath, PixelUpgrade.getInstance().cmdForceStatsLoader);
        ForceHatchConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdForceHatchPath, PixelUpgrade.getInstance().cmdForceHatchLoader);
        GetStatsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdGetStatsPath, PixelUpgrade.getInstance().cmdGetStatsLoader);
        PixelUpgradeInfoConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdPixelUpgradeInfoPath, PixelUpgrade.getInstance().cmdPixelUpgradeInfoLoader);
        ResetEVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdResetEVsPath, PixelUpgrade.getInstance().cmdResetEVsLoader);
        UpgradeConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdUpgradePath, PixelUpgrade.getInstance().cmdUpgradeLoader);
        WeaknessConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdWeaknessPath, PixelUpgrade.getInstance().cmdWeaknessLoader);

        src.sendMessage(Text.of("\u00A73PU Reload // notice: \u00A7bReloading done. Check the console for any errors."));

        return CommandResult.success();
    }
}
