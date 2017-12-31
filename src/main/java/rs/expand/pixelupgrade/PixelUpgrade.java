package rs.expand.pixelupgrade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.commands.*;
import rs.expand.pixelupgrade.configs.*;
import rs.expand.pixelupgrade.utilities.ConfigOperations;

import javax.inject.Inject;

// New things:
//TODO: Make a Pokémon transfer command.
//TODO: Make a token redeeming command for shinies. Maybe make it a starter picker command, even.
//TODO: Maybe make a heal command with a hour-long cooldown?
//TODO: Make a /pokesell, maybe one that sells based on ball worth.
//TODO: Check public static final String PC_RAVE = "rave";
//TODO: See if recoloring Pokémon is possible.
//TODO: Look into name colors?
//TODO: Make a Pokéball changing command, get it to write the old ball to the Pokémon for ball sale purposes.
//TODO: Do something with setPixelmonScale. Maybe a /spawnboss for super big high HP IV bosses with custom loot?
//TODO: Make a /devolve, or something along those lines.

// Improvements to existing things:
//TODO: Tab completion on player names.
//TODO: Maybe turn /dittofusion into a generic /fuse, with a Ditto-only config option.
//TODO: Add a Mew clone count check to /checkstats and /showstats.

@Plugin
(
        id = "pixelupgrade",
        name = "PixelUpgrade",
        version = "3.1",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds a whole bunch of utility commands to Pixelmon, and some economy-integrated commands, too.",
        authors = "XpanD"

        // Not listed but certainly appreciated:

        // NickImpact (helping me understand how to manipulate NBTs)
        // Proxying (writing to entities in a copy-persistent manner)
        // Karanum (fancy paginated command lists)
        // Hiroku (tip + snippet for setting up UTF-8 encoding; made § work)
        // Xenoyia (a LOT of early help, and some serious later stuff too)

        // ...and everybody else who contributed ideas and reported issues.
        // Thanks for helping make PU what it is now, people!
)

public class PixelUpgrade
{
    // Primary setup.
    private static final String name = "PixelUpgrade";
    public static final Logger log = LoggerFactory.getLogger(name);
    public static EconomyService economyService;

    // Create an instance that other classes can access.
    private static PixelUpgrade instance = new PixelUpgrade();
    public static PixelUpgrade getInstance()
    { return instance; }

    // This is all magic to me, right now. One day I'll learn what this means! Thanks, Google.
    @Inject
    private PluginContainer pluginContainer;
    public PluginContainer getPluginContainer() { return pluginContainer; }

    // Set up a nice compact private logger specifically for showing command loading.
    private static final String pName = "PU";
    private static final Logger pLog = LoggerFactory.getLogger(pName);

    // Config-related setup.
    private static String separator = FileSystems.getDefault().getSeparator();
    private static String privatePath = "config" + separator;
    public static String path = "config" + separator + "PixelUpgrade" + separator;

    // Set up the debug logger variable. If we can read the debug level from the configs, we'll overwrite this later.
    public static Integer debugLevel = 3;

    // Load up a ton of variables for use by other commands. We'll fill these in during Forge pre-init.
    public String shortenedHP;
    public String shortenedAttack;
    public String shortenedDefense;
    public String shortenedSpAtt;
    public String shortenedSpDef;
    public String shortenedSpeed;

    // Create the config paths.
    public static Path primaryConfigPath = Paths.get(privatePath, "PixelUpgrade.conf");
    public static Path checkEggPath = Paths.get(path, "CheckEgg.conf");
    public static Path checkStatsPath = Paths.get(path, "CheckStats.conf");
    public static Path checkTypesPath = Paths.get(path, "CheckTypes.conf");
    public static Path dittoFusionPath = Paths.get(path, "DittoFusion.conf");
    public static Path fixEVsPath = Paths.get(path, "FixEVs.conf");
    public static Path fixLevelPath = Paths.get(path, "FixLevel.conf");
    public static Path forceHatchPath = Paths.get(path, "ForceHatch.conf");
    public static Path forceStatsPath = Paths.get(path, "ForceStats.conf");
    public static Path puInfoPath = Paths.get(path, "PixelUpgradeInfo.conf");
    public static Path resetCountPath = Paths.get(path, "ResetCount.conf");
    public static Path resetEVsPath = Paths.get(path, "ResetEVs.conf");
    public static Path switchGenderPath = Paths.get(path, "SwitchGender.conf");
    public static Path showStatsPath = Paths.get(path, "ShowStats.conf");
    public static Path upgradeIVsPath = Paths.get(path, "UpgradeIVs.conf");

    // Set up said paths.
    public static ConfigurationLoader<CommentedConfigurationNode> primaryConfigLoader =
            HoconConfigurationLoader.builder().setPath(primaryConfigPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkEggLoader =
            HoconConfigurationLoader.builder().setPath(checkEggPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkStatsLoader =
            HoconConfigurationLoader.builder().setPath(checkStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> checkTypesLoader =
            HoconConfigurationLoader.builder().setPath(checkTypesPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> dittoFusionLoader =
            HoconConfigurationLoader.builder().setPath(dittoFusionPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> fixEVsLoader =
            HoconConfigurationLoader.builder().setPath(fixEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> fixLevelLoader =
            HoconConfigurationLoader.builder().setPath(fixLevelPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> forceHatchLoader =
            HoconConfigurationLoader.builder().setPath(forceHatchPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> forceStatsLoader =
            HoconConfigurationLoader.builder().setPath(forceStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> puInfoLoader =
            HoconConfigurationLoader.builder().setPath(puInfoPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetCountLoader =
            HoconConfigurationLoader.builder().setPath(resetCountPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> resetEVsLoader =
            HoconConfigurationLoader.builder().setPath(resetEVsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> switchGenderLoader =
            HoconConfigurationLoader.builder().setPath(switchGenderPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> showStatsLoader =
            HoconConfigurationLoader.builder().setPath(showStatsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> upgradeIVsLoader =
            HoconConfigurationLoader.builder().setPath(upgradeIVsPath).build();

    @Listener // Needed for economy support.
    public void onChangeServiceProvider(ChangeServiceProviderEvent event)
    {
        if (event.getService().equals(EconomyService.class))
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
    }

    //@Listener // Needed for the reload command.
    //public void shareInstance(GameConstructionEvent event)
    //{ instance = this; }

    /*                       *\
         Utility commands.
    \*                       */
    private CommandSpec reloadconfigs = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.reload")
            .executor(new ReloadConfigs())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("config"))))
            .build();

    private CommandSpec pixelupgradeinfo = CommandSpec.builder()
            .executor(new PixelUpgradeInfo())
            .arguments( // Ignore all arguments, don't error on anything. Command doesn't use them, anyways.
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of(""))))
            .build();

    /*                    *\
         Main commands.
    \*                    */
    private CommandSpec checkegg = CommandSpec.builder()
            .permission("pixelupgrade.command.checkegg")
            .executor(new CheckEgg())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    private CommandSpec checkstats = CommandSpec.builder()
            .permission("pixelupgrade.command.checkstats")
            .executor(new CheckStats(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("confirmation"))))
            .build();

    private CommandSpec checktypes = CommandSpec.builder()
            .permission("pixelupgrade.command.checktypes")
            .executor(new CheckTypes(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("pokemon"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec dittofusion = CommandSpec.builder()
            .permission("pixelupgrade.command.dittofusion")
            .executor(new DittoFusion(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("sacrifice slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec fixevs = CommandSpec.builder()
            .permission("pixelupgrade.command.fixevs")
            .executor(new FixEVs(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec fixlevel = CommandSpec.builder()
            .permission("pixelupgrade.command.fixlevel")
            .executor(new FixLevel(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec forcehatch = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcehatch")
            .executor(new ForceHatch())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("target or slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))))
            .build();

    private CommandSpec forcestats = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.forcestats")
            .executor(new ForceStats())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("value"))),
                    GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec resetcount = CommandSpec.builder()
            .permission("pixelupgrade.command.staff.resetcount")
            .executor(new ResetCount())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("count"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec resetevs = CommandSpec.builder()
            .permission("pixelupgrade.command.resetevs")
            .executor(new ResetEVs(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec switchgender = CommandSpec.builder()
            .permission("pixelupgrade.command.switchgender")
            .executor(new SwitchGender(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec showstats = CommandSpec.builder()
            .permission("pixelupgrade.command.showstats")
            .executor(new ShowStats(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    private CommandSpec upgradeivs = CommandSpec.builder()
            .permission("pixelupgrade.command.upgradeivs")
            .executor(new UpgradeIVs(this))
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("slot"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("stat"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("quantity"))),
                    GenericArguments.flags().flag("c").buildWith(GenericArguments.none()))
            .build();

    @Listener
    public void onPreInitializationEvent(GamePreInitializationEvent event)
    {
        // Create a config directory if it doesn't exist. Silently catch an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(path));
            log.info("§dCould not find a PixelUpgrade config folder. Creating it!");
        }
        catch (IOException ignored) {} // We don't need to show a message if the folder already exists.

        // Load up the primary config and the info command config, and figure out the info alias.
        // We start printing stuff, here. If any warnings/errors pop up they'll be shown here.
        // Note: We run an overloaded method for the primary config. That's why it knows where to go.
        pLog.info("===========================================================================");
        pLog.info("--> §aLoading global settings and §2/pixelupgrade§a command listing...");
        ConfigOperations.getInstance().setupConfig(primaryConfigPath, privatePath, primaryConfigLoader);
        String puInfoAlias = PixelUpgradeInfoConfig.getInstance().setupConfig(puInfoPath, path, puInfoLoader);

        // Register other aliases and get some configs. Similar to the above, any errors/warnings will be printed.
        pLog.info("--> §aLoading command-specific configs...");
        String checkEggAlias = ConfigOperations.getInstance().setupConfig(
                "CheckEgg", "egg", checkEggPath, path, checkEggLoader);
        String checkStatsAlias = ConfigOperations.getInstance().setupConfig(
                "CheckStats", "cs", checkStatsPath, path, checkStatsLoader);
        String checkTypesAlias = ConfigOperations.getInstance().setupConfig(
                "CheckTypes", "type", checkTypesPath, path, checkTypesLoader);
        String dittoFusionAlias = ConfigOperations.getInstance().setupConfig(
                "DittoFusion", "fuse", dittoFusionPath, path, dittoFusionLoader);
        String fixEVsAlias = ConfigOperations.getInstance().setupConfig(
                "FixEVs", "fixevs", fixEVsPath, path, fixEVsLoader);
        String fixLevelAlias = ConfigOperations.getInstance().setupConfig(
                "FixLevel", "fixlevel", fixLevelPath, path, fixLevelLoader);
        String forceHatchAlias = ConfigOperations.getInstance().setupConfig(
                "ForceHatch", "fhatch", forceHatchPath, path, forceHatchLoader);
        String forceStatsAlias = ConfigOperations.getInstance().setupConfig(
                "ForceStats", "fstats", forceStatsPath, path, forceStatsLoader);
        String resetCountAlias = ConfigOperations.getInstance().setupConfig(
                "ResetCount", "delcount", resetCountPath, path, resetCountLoader);
        String resetEVsAlias = ConfigOperations.getInstance().setupConfig(
                "ResetEVs", "delevs", resetEVsPath, path, resetEVsLoader);
        String showStatsAlias = ConfigOperations.getInstance().setupConfig(
                "ShowStats", "show", showStatsPath, path, showStatsLoader);
        String switchGenderAlias = ConfigOperations.getInstance().setupConfig(
                "SwitchGender", "bend", switchGenderPath, path, switchGenderLoader);
        String upgradeIVsAlias = ConfigOperations.getInstance().setupConfig(
                "UpgradeIVs", "upgrade", upgradeIVsPath, path, upgradeIVsLoader);

        // Read the debug logging level and apply it. All commands will refer to this.
        String modeString = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "debugVerbosityMode", false);
        if (modeString != null)
        {
            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("§4PixelUpgrade // critical: §cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
            PixelUpgrade.log.info("§4PixelUpgrade // critical: §cConfig variable \"debugVerbosityMode\" could not be read!");

        // Initialize the variables that we want other commands to have access to.
        shortenedHP = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "shortenedHealth", false);
        shortenedAttack = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "shortenedAttack", false);
        shortenedDefense = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "shortenedDefense", false);
        shortenedSpAtt = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "shortenedSpecialAttack", false);
        shortenedSpDef = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "shortenedSpecialDefense", false);
        shortenedSpeed = ConfigOperations.getInstance().updateConfigs("PixelUpgrade", "shortenedSpeed", false);

        // Do some initial setup for our formatted messages later on. We'll show three commands per line.
        ArrayList<String> commandList = new ArrayList<>();
        StringBuilder formattedCommand = new StringBuilder(), printableList = new StringBuilder();
        String commandAlias = null, commandString = null;

        // Format our commands and aliases and add them to the lists that we'll print in a bit.
        for (int i = 1; i <= 15; i++)
        {
            switch (i)
            {
                // Normal commands. If the alias is null (error returned), we pass the base command again instead.
                // This prevents NPEs while also letting us hide commands by checking whether they've returned null.
                case 1:
                    commandAlias = checkEggAlias;
                    if (checkEggAlias == null)
                        checkEggAlias = "/checkegg";
                    commandString = "/checkegg";
                    break;
                case 2:
                    commandAlias = checkStatsAlias;
                    if (checkStatsAlias == null)
                        checkStatsAlias = "/checkstats";
                    commandString = "/checkstats";
                    break;
                case 3:
                    commandAlias = checkTypesAlias;
                    if (checkTypesAlias == null)
                        checkTypesAlias = "/checktypes";
                    commandString = "/checktypes";
                    break;
                case 4:
                    commandAlias = dittoFusionAlias;
                    if (dittoFusionAlias == null)
                        dittoFusionAlias = "/dittofusion";
                    commandString = "/dittofusion";
                    break;
                case 5:
                    commandAlias = fixEVsAlias;
                    if (fixEVsAlias == null)
                        fixEVsAlias = "/fixevs";
                    commandString = "/fixevs";
                    break;
                case 6:
                    commandAlias = fixLevelAlias;
                    if (fixLevelAlias == null)
                        fixLevelAlias = "/fixlevel";
                    commandString = "/fixlevel";
                    break;
                case 7:
                    commandAlias = forceHatchAlias;
                    if (forceHatchAlias == null)
                        forceHatchAlias = "/forcehatch";
                    commandString = "/forcehatch";
                    break;
                case 8:
                    commandAlias = forceStatsAlias;
                    if (forceStatsAlias == null)
                        forceStatsAlias = "/forcestats";
                    commandString = "/forcestats";
                    break;
                case 9:
                    commandAlias = puInfoAlias;
                    if (puInfoAlias == null)
                        puInfoAlias = "/pixelupgrade";
                    commandString = "/pixelupgrade";
                    break;
                case 10:
                    commandAlias = "no alias";
                    commandString = "/pureload";
                    break;
                case 11:
                    commandAlias = resetCountAlias;
                    if (resetCountAlias == null)
                        resetCountAlias = "/resetcount";
                    commandString = "/resetcount";
                    break;
                case 12:
                    commandAlias = resetEVsAlias;
                    if (resetEVsAlias == null)
                        resetEVsAlias = "/resetevs";
                    commandString = "/resetevs";
                    break;
                case 13:
                    commandAlias = switchGenderAlias;
                    if (switchGenderAlias == null)
                        switchGenderAlias = "/switchgender";
                    commandString = "/switchgender";
                    break;
                case 14:
                    commandAlias = showStatsAlias;
                    if (showStatsAlias == null)
                        showStatsAlias = "/showstats";
                    commandString = "/showstats";
                    break;
                case 15:
                    commandAlias = upgradeIVsAlias;
                    if (upgradeIVsAlias == null)
                        upgradeIVsAlias = "/upgradeivs";
                    commandString = "/upgradeivs";
                    break;
            }

            if (commandAlias != null)
            {
                // Format the command.
                formattedCommand.append("§2");
                formattedCommand.append(commandString);

                if (commandAlias.equals("no alias") || commandString.equals("/" + commandAlias))
                    formattedCommand.append("§a, ");
                else
                {
                    formattedCommand.append("§a (§2/");
                    formattedCommand.append(commandAlias.toLowerCase());
                    formattedCommand.append("§a), ");
                }

                // If we're at the last command, shank the trailing comma for a clean end.
                if (i == 15)
                    formattedCommand.setLength(formattedCommand.length() - 2);

                // Add the formatted command to the list, and then clear the StringBuilder so we can re-use it.
                commandList.add(formattedCommand.toString());
                formattedCommand.setLength(0);
            }
        }

        // Print the formatted commands + aliases.
        int listSize = commandList.size();
        pLog.info("--> §aSuccessfully registered a bunch of commands! See below.");

        for (int q = 1; q < listSize + 1; q++)
        {
            printableList.append(commandList.get(q - 1));

            if (q == listSize) // Are we on the last entry of the list? Exit.
                pLog.info("    " + printableList);
            else if (q % 3 == 0) // Is the loop number a multiple of 3? If so, we have three commands stocked up. Print!
            {
                pLog.info("    " + printableList);
                printableList.setLength(0); // Wipe the list so we can re-use it for the next three commands.
            }
        }

        pLog.info("===========================================================================");

        // And finally, register the aliases we grabbed earlier.
        Sponge.getCommandManager().register(this, checkegg, "checkegg", "eggcheck", checkEggAlias);
        Sponge.getCommandManager().register(this, checkstats, "checkstats", "getstats", checkStatsAlias);
        Sponge.getCommandManager().register(this, checktypes, "checktypes", "checktype", "weakness", checkTypesAlias);
        Sponge.getCommandManager().register(this, dittofusion, "dittofusion", "fuseditto", dittoFusionAlias);
        Sponge.getCommandManager().register(this, fixevs, "fixevs", "fixev", fixEVsAlias);
        Sponge.getCommandManager().register(this, fixlevel, "fixlevel", "fixlevels", fixLevelAlias);
        Sponge.getCommandManager().register(this, forcehatch, "forcehatch", forceHatchAlias);
        Sponge.getCommandManager().register(this, forcestats, "forcestats", "forcestat", forceStatsAlias);
        Sponge.getCommandManager().register(this, pixelupgradeinfo, "pixelupgrade", "pixelupgradeinfo", puInfoAlias);
        Sponge.getCommandManager().register(this, reloadconfigs, "pureload", "pixelupgradereload");
        Sponge.getCommandManager().register(this, resetcount, "resetcount", "resetcounts", resetCountAlias);
        Sponge.getCommandManager().register(this, resetevs, "resetevs", "resetev", resetEVsAlias);
        Sponge.getCommandManager().register(this, switchgender, "switchgender", switchGenderAlias);
        Sponge.getCommandManager().register(this, showstats, "showstats", showStatsAlias);
        Sponge.getCommandManager().register(this, upgradeivs, "upgradeivs", "upgradeiv", upgradeIVsAlias);

        CheckEgg.alias = ConfigOperations.getInstance().updateConfigs("CheckEgg", "commandAlias", false);
        PixelUpgrade.log.info("§4PixelUpgrade // DEBUG: §cEnd of pre-init event. Alias: " + CheckEgg.alias);
        PixelUpgrade.log.info("§4PixelUpgrade // DEBUG: §cNode contents: " + ConfigOperations.getInstance().updateConfigs("CheckEgg", "commandAlias", false));
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    { log.info("§bAll systems nominal."); }
}