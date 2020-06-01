package dev.minemalox.advancedskyblock;

import dev.minemalox.advancedskyblock.commands.AdvancedSkyblockCommand;
import dev.minemalox.advancedskyblock.listeners.GuiScreenListener;
import dev.minemalox.advancedskyblock.listeners.NetworkListener;
import dev.minemalox.advancedskyblock.listeners.PlayerListener;
import dev.minemalox.advancedskyblock.listeners.RenderListener;
import dev.minemalox.advancedskyblock.utils.*;
import dev.minemalox.advancedskyblock.utils.discord.DiscordRPCManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.Timer;
import java.util.TimerTask;

@Mod(
        modid = AdvancedSkyblock.MOD_ID,
        name = AdvancedSkyblock.MOD_NAME,
        clientSideOnly = true,
        acceptedMinecraftVersions = "1.12.2",
        version = AdvancedSkyblock.VERSION,
        updateJSON = AdvancedSkyblock.UPDATE_JSON
)
public class AdvancedSkyblock {

    public static final String MOD_ID = "advancedskyblock";
    public static final String MOD_NAME = "Addons Skyblock";
    public static final String VERSION = "2.0.0-beta.2";
    public static final String UPDATE_JSON = "https://raw.githubusercontent.com/MineMalox/AdvancedSkyblock/1.12/.github/versions/dev-versions.json";
    /**
     * The main instance of the mod, used mainly by mixins who don't get it passed to them.
     */
    private static AdvancedSkyblock instance;
    /**
     * Get the scheduler that be can be used to easily execute tasks.
     */
    private final Scheduler scheduler = new Scheduler(this);
    private ConfigValues configValues;
    private Logger logger;
    private PersistentValues persistentValues;
    private PlayerListener playerListener;
    private GuiScreenListener guiScreenListener;
    private RenderListener renderListener;
    private InventoryUtils inventoryUtils;
    private Utils utils;
    private Updater updater;
    /**
     * Whether developer mode is enabled.
     */
    private boolean devMode = true;
    private KeyBinding[] keyBindings = new KeyBinding[4];
    private DiscordRPCManager discordRPCManager;

    public static AdvancedSkyblock getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        configValues = new ConfigValues(this, e.getSuggestedConfigurationFile());
        logger = LogManager.getLogger();
        persistentValues = new PersistentValues(e.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        // Initialize event listeners
        playerListener = new PlayerListener(this);
        guiScreenListener = new GuiScreenListener(this);
        renderListener = new RenderListener(this);
        discordRPCManager = new DiscordRPCManager(this);

        MinecraftForge.EVENT_BUS.register(new NetworkListener());
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(guiScreenListener);
        MinecraftForge.EVENT_BUS.register(renderListener);
        MinecraftForge.EVENT_BUS.register(scheduler);

        // Initialize utilities
        inventoryUtils = new InventoryUtils(this);
        utils = new Utils(this);
        updater = new Updater(this);

        ClientCommandHandler.instance.registerCommand(new AdvancedSkyblockCommand(this));

        keyBindings[0] = new KeyBinding("key.advancedskyblock.open_settings", Keyboard.KEY_NONE, MOD_NAME);
        keyBindings[1] = new KeyBinding("key.advancedskyblock.edit_gui", Keyboard.KEY_NONE, MOD_NAME);
        keyBindings[2] = new KeyBinding("key.advancedskyblock.lock_slot", Keyboard.KEY_L, MOD_NAME);
        keyBindings[3] = new KeyBinding("key.advancedskyblock.freeze_backpack", Keyboard.KEY_F, MOD_NAME);

        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }


    //private void changeKeyBindDescription(KeyBinding bind, String desc) {
    //    try {
    //        Field field = bind.getClass().getDeclaredField(SkyblockAddonsTransformer.isDeobfuscated() ? "keyDescription" : "field_74515_c");
    //        field.setAccessible(true);
    //        field.set(bind, desc);
    //    } catch (NoSuchFieldException | IllegalAccessException e) {
    //        logger.error("Could not change key description: " + bind.toString());
    //        logger.catching(e);
    //    }
    //}

    //public void loadKeyBindingDescriptions() {
    //    changeKeyBindDescription(keyBindings[0], Message.SETTING_SETTINGS.getMessage());
    //    changeKeyBindDescription(keyBindings[1], Message.SETTING_EDIT_LOCATIONS.getMessage());
    //    changeKeyBindDescription(keyBindings[2], Message.SETTING_LOCK_SLOT.getMessage());
    //    changeKeyBindDescription(keyBindings[3], Message.SETTING_SHOW_BACKPACK_PREVIEW.getMessage());
    //}

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        configValues.loadConfig();
        persistentValues.loadValues();
        // loadKeyBindingDescriptions();

        utils.checkDisabledFeatures();
        utils.getFeaturedURLOnline();
        updater.processUpdateCheckResult();
        scheduleMagmaCheck();

        for (Feature feature : Feature.values()) {
            if (feature.isGuiFeature()) {
                feature.getSettings().add(EnumUtils.FeatureSetting.GUI_SCALE);
            }
            if (feature.isColorFeature()) {
                feature.getSettings().add(EnumUtils.FeatureSetting.COLOR);
            }
        }
    }

    @Mod.EventHandler
    public void stop(FMLModDisabledEvent e) {
        discordRPCManager.stop();
    }

    private void scheduleMagmaCheck() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft() != null) {
                    utils.fetchEstimateFromServer();
                } else {
                    scheduleMagmaCheck();
                }
            }
        }, 5000);
    }

    public KeyBinding getOpenSettingsKey() {
        return keyBindings[0];
    }

    public KeyBinding getOpenEditLocationsKey() {
        return keyBindings[1];
    }

    public KeyBinding getLockSlotKey() {
        return keyBindings[2];
    }

    public KeyBinding getFreezeBackpackKey() {
        return keyBindings[3];
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public PersistentValues getPersistentValues() {
        return persistentValues;
    }

    public Utils getUtils() {
        return utils;
    }

    public Updater getUpdater() {
        return updater;
    }

    public DiscordRPCManager getDiscordRPCManager() {
        return discordRPCManager;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public GuiScreenListener getGuiScreenListener() {
        return guiScreenListener;
    }

    public RenderListener getRenderListener() {
        return renderListener;
    }

    public InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }

    public ConfigValues getConfigValues() {
        return configValues;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean b) {
        devMode = b;
    }

    private KeyBinding[] getKeyBindings() {
        return keyBindings;
    }
}
