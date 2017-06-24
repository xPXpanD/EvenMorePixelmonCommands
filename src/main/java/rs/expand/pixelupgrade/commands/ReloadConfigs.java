package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReloadConfigs implements CommandExecutor
{
    public CommandResult execute(CommandSource src, CommandContext args)
    {
        boolean showError = false;

        if (args.<String>getOne("config").isPresent())
        {
            String configString = args.<String>getOne("config").get();
            String separator = FileSystems.getDefault().getSeparator();
            Path configPath = Paths.get("config" + separator + "PixelUpgrade" + separator);

            // Create a config directory if it doesn't exist.
            try
            {
                Files.createDirectory(configPath);
                PixelUpgrade.log.info("\u00A7dCould not find a PixelUpgrade config folder. Creating it!");
            }
            catch (IOException F)
            {   PixelUpgrade.log.info("\u00A7dFound a PixelUpgrade config folder. Trying to load!");   }

            switch (configString.toUpperCase())
            {
                // Special cases
                case "ALL":
                    loadEVERYTHING(); // Go go go!
                    break;
                case "MAINCONFIG": case "MAIN":
                    PixelUpgradeMainConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().primaryConfigPath, PixelUpgrade.getInstance().primaryConfigLoader);
                    break;

                // Commands
                case "CHECKEGG":
                    CheckEggConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckEggPath, PixelUpgrade.getInstance().cmdCheckEggLoader);
                    break;
                case "CHECKSTATS":
                    CheckStatsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckStatsPath, PixelUpgrade.getInstance().cmdCheckStatsLoader);
                    break;
                case "CHECKTYPES":
                    CheckTypesConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckTypesPath, PixelUpgrade.getInstance().cmdCheckTypesLoader);
                    break;
                case "DITTOFUSION":
                    DittoFusionConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdDittoFusionPath, PixelUpgrade.getInstance().cmdDittoFusionLoader);
                    break;
                case "FIXEVS":
                    FixEVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdFixEVsPath, PixelUpgrade.getInstance().cmdFixEVsLoader);
                    break;
                case "FIXLEVEL":
                    FixLevelConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdFixLevelPath, PixelUpgrade.getInstance().cmdFixLevelLoader);
                    break;
                case "FORCEHATCH":
                    ForceHatchConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdForceHatchPath, PixelUpgrade.getInstance().cmdForceHatchLoader);
                    break;
                case "FORCESTATS":
                    ForceStatsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdForceStatsPath, PixelUpgrade.getInstance().cmdForceStatsLoader);
                    break;
                case "PIXELUPGRADEINFO": case "INFO":
                    PixelUpgradeInfoConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdPixelUpgradeInfoPath, PixelUpgrade.getInstance().cmdPixelUpgradeInfoLoader);
                    break;
                case "RESETEVS":
                    ResetEVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdResetEVsPath, PixelUpgrade.getInstance().cmdResetEVsLoader);
                    break;
                case "UPGRADEIVS":
                    UpgradeIVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdUpgradeIVsPath, PixelUpgrade.getInstance().cmdUpgradeIVsLoader);
                    break;

                // Input did not match any of the above, abort.
                default:
                    showError = true;
            }

            PixelUpgrade.log.info("\u00A7dCommand reload finished!");
        }
        else
            showError = true;

        if (showError)
        {
            src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
            if (args.<String>getOne("config").isPresent())
                src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid config provided. See below for valid configs."));
            else
                src.sendMessage(Text.of("\u00A74Error: \u00A7cNo config provided. See below for valid configs."));
            src.sendMessage(Text.of("\u00A74Usage: \u00A7c/pureload <config>"));
            src.sendMessage(Text.of(""));
            src.sendMessage(Text.of("\u00A76Commands: \u00A7eCheckEgg, CheckStats, CheckTypes, DittoFusion"));
            src.sendMessage(Text.of("\u00A76Commands: \u00A7eFixEVs, FixLevel, ForceHatch, ForceStats"));
            src.sendMessage(Text.of("\u00A76Commands: \u00A7ePixelUpgradeInfo (or \"Info\"), ResetEVs, UpgradeIVs"));
            src.sendMessage(Text.of("\u00A76Other: \u00A7eAll (reloads ALL configs!), MainConfig (or \"Main\")"));
            src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
        else
        {
            src.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
            src.sendMessage(Text.of("\u00A73PU Reload // notice: \u00A7bReloaded the provided config(s)!"));
            src.sendMessage(Text.of("\u00A73PU Reload // notice: \u00A7bPlease check the console for any errors."));
            src.sendMessage(Text.of("\u00A77-----------------------------------------------------"));
        }

        return CommandResult.success();
    }

    private void loadEVERYTHING()
    {
        PixelUpgradeMainConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().primaryConfigPath, PixelUpgrade.getInstance().primaryConfigLoader);
        CheckEggConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckEggPath, PixelUpgrade.getInstance().cmdCheckEggLoader);
        CheckStatsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckStatsPath, PixelUpgrade.getInstance().cmdCheckStatsLoader);
        CheckTypesConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdCheckTypesPath, PixelUpgrade.getInstance().cmdCheckTypesLoader);
        DittoFusionConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdDittoFusionPath, PixelUpgrade.getInstance().cmdDittoFusionLoader);
        FixEVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdFixEVsPath, PixelUpgrade.getInstance().cmdFixEVsLoader);
        FixLevelConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdFixLevelPath, PixelUpgrade.getInstance().cmdFixLevelLoader);
        ForceHatchConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdForceHatchPath, PixelUpgrade.getInstance().cmdForceHatchLoader);
        ForceStatsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdForceStatsPath, PixelUpgrade.getInstance().cmdForceStatsLoader);
        PixelUpgradeInfoConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdPixelUpgradeInfoPath, PixelUpgrade.getInstance().cmdPixelUpgradeInfoLoader);
        ResetEVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdResetEVsPath, PixelUpgrade.getInstance().cmdResetEVsLoader);
        UpgradeIVsConfig.getInstance().loadOrCreateConfig(PixelUpgrade.getInstance().cmdUpgradeIVsPath, PixelUpgrade.getInstance().cmdUpgradeIVsLoader);
    }
}
