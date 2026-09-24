package com.ultrascore.core;

import com.ultrascore.announcements.AnnouncementManager;
import com.ultrascore.announcements.AnnouncementScheduler;
import com.ultrascore.announcements.BroadcastCommand;
import com.ultrascore.backup.BackupManager;
import com.ultrascore.commands.UltrasCoreCommand;
import com.ultrascore.config.ConfigManager;
import com.ultrascore.gui.GuiManager;
import com.ultrascore.homes.HomeCommand;
import com.ultrascore.homes.HomeManager;
import com.ultrascore.homes.HomesCommand;
import com.ultrascore.homes.SetHomeCommand;
import com.ultrascore.homes.DelHomeCommand;
import com.ultrascore.homes.HomeAdminCommand;
import com.ultrascore.homes.UcHomeCommand;
import com.ultrascore.homes.HomeTabCompleter;
import com.ultrascore.hub.HubCommand;
import com.ultrascore.hub.HubJoinListener;
import com.ultrascore.hub.HubManager;
import com.ultrascore.hub.SetHubCommand;
import com.ultrascore.language.LanguageManager;
import com.ultrascore.logs.LogManager;
import com.ultrascore.messagesystem.ChatCommand;
import com.ultrascore.messagesystem.ChatListener;
import com.ultrascore.messagesystem.MessageCommand;
import com.ultrascore.nightvision.NightVisionCommand;
import com.ultrascore.nightvision.NightVisionListener;
import com.ultrascore.nightvision.NightVisionManager;
import com.ultrascore.playernames.JoinLeaveDeathListener;
import com.ultrascore.playernames.NamesCommand;
import com.ultrascore.playernames.NamesListener;
import com.ultrascore.playernames.NamesManager;
import com.ultrascore.playernames.RanksCommand;
import com.ultrascore.placeholders.UltrasCorePlaceholders;
import com.ultrascore.proxy.ProxyManager;
import com.ultrascore.rtp.RTPCommand;
import com.ultrascore.rtp.RTPManager;
import com.ultrascore.spawn.SetSpawnCommand;
import com.ultrascore.spawn.SpawnCommand;
import com.ultrascore.spawn.SpawnListener;
import com.ultrascore.spawn.SpawnManager;
import com.ultrascore.settings.CustomGuiManager;
import com.ultrascore.settings.SettingsCommand;
import com.ultrascore.settings.SettingsManager;
import com.ultrascore.storage.StorageManager;
import com.ultrascore.utils.TeleportCountdown;
import com.ultrascore.tpa.TPACommand;
import com.ultrascore.tpa.TPAHereCommand;
import com.ultrascore.tpa.TPAListener;
import com.ultrascore.tpa.TPAManager;
import com.ultrascore.tpa.TPATabCompleter;
import com.ultrascore.compatibility.ScoreboardTabGuard;
import com.ultrascore.visibility.HideCommand;
import com.ultrascore.visibility.VisibilityListener;
import com.ultrascore.visibility.VisibilityManager;
import com.ultrascore.warps.WarpCommand;
import com.ultrascore.warps.WarpManager;
import com.ultrascore.warps.WarpPasswordListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * UltrasCore bootstrap.
 *
 * Design: every feature system is owned by a single Manager. The plugin class
 * only wires managers together and (de)registers commands/listeners based on
 * config flags, so that a disabled system truly has zero footprint at runtime.
 */
public final class UltrasCore extends JavaPlugin {

    private static UltrasCore instance;

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private LogManager logManager;
    private StorageManager storageManager;
    private GuiManager guiManager;
    private TeleportCountdown teleportCountdown;

    private TPAManager tpaManager;
    private HomeManager homeManager;
    private RTPManager rtpManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private SettingsManager settingsManager;
    private CustomGuiManager customGuiManager;
    private AnnouncementManager announcementManager;
    private AnnouncementScheduler announcementScheduler;
    private VisibilityManager visibilityManager;
    private NamesManager namesManager;
    private NightVisionManager nightVisionManager;
    private HubManager hubManager;
    private ProxyManager proxyManager;
    private BackupManager backupManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.logManager = new LogManager(this);
        this.storageManager = new StorageManager(this);
        this.guiManager = new GuiManager(this);
        this.teleportCountdown = new TeleportCountdown(this);
        this.settingsManager = new SettingsManager(this, storageManager);
        this.proxyManager = new ProxyManager(this);
        this.backupManager = new BackupManager(this);

        configManager.validate();

        registerSystems();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UltrasCorePlaceholders(this).register();
            logManager.info("core", "PlaceholderAPI detected — %ultrascore_*% placeholders registered.");
        }

        backupManager.backupNow();

        ScoreboardTabGuard.check(this);

        logManager.info("core", "UltrasCore enabled (" + getPluginMeta().getVersion() + ").");
    }

    @Override
    public void onDisable() {
        if (announcementScheduler != null) {
            announcementScheduler.stop();
        }
        if (proxyManager != null) {
            proxyManager.unregisterChannel();
        }
        if (storageManager != null) {
            storageManager.saveAllSync();
        }
        if (logManager != null) {
            logManager.info("core", "UltrasCore disabled.");
        }
    }

    /** (Re)registers systems according to the current config. Safe to call on reload. */
    public void registerSystems() {
        // Reload can call this repeatedly, so drop every previously-registered
        // listener first (including GuiManager's and TeleportCountdown's) and
        // re-add the ones that should still be active — otherwise each reload
        // would stack duplicate listeners (e.g. quit events firing N times).
        org.bukkit.event.HandlerList.unregisterAll(this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        getServer().getPluginManager().registerEvents(teleportCountdown, this);

        // TPA
        if (configManager.isSystemEnabled("tpa")) {
            this.tpaManager = new TPAManager(this);
            registerCommand("tpa", new TPACommand(this, tpaManager), new TPATabCompleter(this));
            registerCommand("tpahere", new TPAHereCommand(this, tpaManager), new TPATabCompleter(this));
            registerCommand("uc_tpa", new com.ultrascore.tpa.TPAAdminCommand(this));
            getServer().getPluginManager().registerEvents(new TPAListener(this, tpaManager), this);
        } else {
            this.tpaManager = null;
        }

        // Homes
        if (configManager.isSystemEnabled("homes")) {
            this.homeManager = new HomeManager(this, storageManager);
            registerCommand("home", new HomeCommand(this, homeManager), new HomeTabCompleter(homeManager));
            registerCommand("homes", new HomesCommand(this, homeManager));
            registerCommand("sethome", new SetHomeCommand(this, homeManager));
            registerCommand("delhome", new DelHomeCommand(this, homeManager), new HomeTabCompleter(homeManager));
            registerCommand("home_admin", new HomeAdminCommand(this, homeManager));
            registerCommand("uc_home", new UcHomeCommand(this, homeManager));
        } else {
            this.homeManager = null;
        }

        // RTP
        if (configManager.isSystemEnabled("rtp")) {
            this.rtpManager = new RTPManager(this);
            registerCommand("rtp", new RTPCommand(this, rtpManager));
        } else {
            this.rtpManager = null;
        }

        // Warps
        if (configManager.isSystemEnabled("warps")) {
            this.warpManager = new WarpManager(this);
            registerCommand("warp", new WarpCommand(this, warpManager));
            getServer().getPluginManager().registerEvents(new WarpPasswordListener(this, warpManager), this);
        } else {
            this.warpManager = null;
        }

        // Spawn
        if (configManager.isSystemEnabled("spawn")) {
            this.spawnManager = new SpawnManager(this);
            registerCommand("setspawn", new SetSpawnCommand(this, spawnManager));
            registerCommand("spawn", new SpawnCommand(this, spawnManager));
            getServer().getPluginManager().registerEvents(new SpawnListener(this, spawnManager), this);
        } else {
            this.spawnManager = null;
        }

        // Settings (built-in toggle GUI + YAML-defined custom GUIs)
        if (configManager.isSystemEnabled("settings")) {
            this.customGuiManager = new CustomGuiManager(this);
            registerCommand("setting", new SettingsCommand(this, settingsManager, customGuiManager));
        } else {
            this.customGuiManager = null;
        }

        // Chat toggle
        if (configManager.isSystemEnabled("chat")) {
            registerCommand("chat", new ChatCommand(this, settingsManager));
            getServer().getPluginManager().registerEvents(new ChatListener(this, settingsManager), this);
        }

        // Private messages
        if (configManager.isSystemEnabled("pm")) {
            registerCommand("msg", new MessageCommand(this, settingsManager));
        }

        // Broadcast + optional automatic announcements
        if (announcementScheduler != null) {
            announcementScheduler.stop();
            announcementScheduler = null;
        }
        if (configManager.isSystemEnabled("broadcast")) {
            this.announcementManager = new AnnouncementManager(this, settingsManager);
            registerCommand("bc", new BroadcastCommand(this, announcementManager));
            this.announcementScheduler = new AnnouncementScheduler(this, announcementManager);
        } else {
            this.announcementManager = null;
        }

        // Join/Leave/Death messages (always active if the underlying config block is enabled;
        // per-viewer filtering happens inside the listener via Settings toggles)
        getServer().getPluginManager().registerEvents(new JoinLeaveDeathListener(this, settingsManager), this);

        // Visibility (/hide)
        if (configManager.isSystemEnabled("visibility")) {
            this.visibilityManager = new VisibilityManager(this, settingsManager);
            registerCommand("hide", new HideCommand(this, visibilityManager));
            getServer().getPluginManager().registerEvents(new VisibilityListener(visibilityManager), this);
        } else {
            this.visibilityManager = null;
        }

        // Names (real nametag hiding)
        if (configManager.isSystemEnabled("names")) {
            this.namesManager = new NamesManager(this, settingsManager);
            registerCommand("names", new NamesCommand(this, namesManager));
            getServer().getPluginManager().registerEvents(new NamesListener(namesManager), this);
        } else {
            this.namesManager = null;
        }

        // Ranks (stored preference; depends on Names)
        if (configManager.isSystemEnabled("ranks") && namesManager != null) {
            registerCommand("ranks", new RanksCommand(this, namesManager));
        }

        // Night Vision
        if (configManager.isSystemEnabled("nightvision")) {
            this.nightVisionManager = new NightVisionManager(this, settingsManager);
            registerCommand("nv", new NightVisionCommand(this, nightVisionManager));
            getServer().getPluginManager().registerEvents(new NightVisionListener(nightVisionManager), this);
        } else {
            this.nightVisionManager = null;
        }

        // Hub (+ Proxy bridge for PROXY mode)
        if (configManager.isSystemEnabled("hub")) {
            this.hubManager = new HubManager(this);
            registerCommand("hub", new HubCommand(this, hubManager, proxyManager));
            registerCommand("sethub", new SetHubCommand(this, hubManager));
            getServer().getPluginManager().registerEvents(new HubJoinListener(this, hubManager), this);
            if (hubManager.isProxyMode() || proxyManager.isEnabled()) {
                proxyManager.registerChannel();
            }
        } else {
            this.hubManager = null;
        }

        registerCommand("ultrascore", new UltrasCoreCommand(this));
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        registerCommand(name, executor, null);
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor,
                                  org.bukkit.command.TabCompleter completer) {
        org.bukkit.command.PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            logManager.warn("core", "Command '" + name + "' is missing from plugin.yml — skipping.");
            return;
        }
        cmd.setExecutor(executor);
        if (completer != null) {
            cmd.setTabCompleter(completer);
        }
    }

    /** Reloads config, language and all system data without a server restart. */
    public void reloadUltrasCore() {
        if (backupManager != null) {
            backupManager.backupNow();
        }
        reloadConfig();
        configManager.reload();
        configManager.validate();
        languageManager.reload();
        registerSystems();
    }

    public static UltrasCore getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public TeleportCountdown getTeleportCountdown() {
        return teleportCountdown;
    }

    public TPAManager getTpaManager() {
        return tpaManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public RTPManager getRtpManager() {
        return rtpManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public CustomGuiManager getCustomGuiManager() {
        return customGuiManager;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    public NamesManager getNamesManager() {
        return namesManager;
    }

    public NightVisionManager getNightVisionManager() {
        return nightVisionManager;
    }

    public HubManager getHubManager() {
        return hubManager;
    }

    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }
}
