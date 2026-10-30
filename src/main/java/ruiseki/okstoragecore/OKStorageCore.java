package ruiseki.okstoragecore;

import net.minecraft.creativetab.CreativeTabs;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigHandler;
import ruiseki.okcore.init.ModBaseVersionable;
import ruiseki.okcore.proxy.ICommonProxy;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSHRegisters;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSHRegisters;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.sync.StorageSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedValueSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistries;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.MOD_VERSION,
    dependencies = Reference.MOD_DEPENDENCIES,
    guiFactory = Reference.GUI_FACTORY)
public class OKStorageCore extends ModBaseVersionable {

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static ICommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static OKStorageCore _instance;

    public OKStorageCore() {
        super(Reference.MOD_ID, Reference.MOD_NAME, Reference.MOD_VERSION);
    }

    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        StorageSHRegisters.load();
        StorageSlotSHRegistries.load();
        UpgradeSlotSHRegistries.load();
        UpgradeSlotUpdateRegistries.load();
        DelegatedItemHandlerSHRegisters.load();
        DelegatedFilterLogicSHRegisters.load();
        DelegatedValueSHRegisters.load();
    }

    @Override
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        super.onServerStarting(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        super.onServerStarted(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        super.onServerStopping(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        super.onServerStopped(event);
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return null;
    }

    @Override
    public void onGeneralConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new GeneralConfig());
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configHandler) {
        Configs.register(configHandler);
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    /**
     * Log a new info message for this mod.
     *
     * @param message The message to show.
     */
    public static void okLog(String message) {
        OKStorageCore._instance.log(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     *
     * @param level   The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void okLog(Level level, String message) {
        OKBackpack._instance.log(level, message);
    }

    public static void okLog(Level level, String message, Object... params) {
        OKStorageCore._instance.log(level, message, params);
    }
}
