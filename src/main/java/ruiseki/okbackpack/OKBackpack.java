package ruiseki.okbackpack;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.Level;

import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import ruiseki.okbackpack.backpack.BackpackStorage;
import ruiseki.okbackpack.client.gui.sync.BackpackSHRegistries;
import ruiseki.okbackpack.client.gui.sync.OBUpgradeSlotSHRegistries;
import ruiseki.okbackpack.client.renderer.JsonModelISBRH;
import ruiseki.okbackpack.init.ModOreDicts;
import ruiseki.okbackpack.init.ModRecipes;
import ruiseki.okbackpack.init.TierRegistries;
import ruiseki.okbackpack.init.block.BlockLeatherBackpackConfig;
import ruiseki.okbackpack.modcompat.bauble.BaubleModCompat;
import ruiseki.okbackpack.modcompat.efr.EFRModCompat;
import ruiseki.okbackpack.modcompat.jfmuy.JFMUYModCompat;
import ruiseki.okbackpack.modcompat.nei.NEIModCompat;
import ruiseki.okbackpack.modcompat.structurelib.StructureLibModCompat;
import ruiseki.okbackpack.modcompat.thaumcraft.ThaumcraftModCompat;
import ruiseki.okbackpack.modcompat.tic.TConstructTabCompat;
import ruiseki.okcore.config.ConfigHandler;
import ruiseki.okcore.config.extendedconfig.BlockItemConfigReference;
import ruiseki.okcore.data.condition.ConfigCondition;
import ruiseki.okcore.helper.MinecraftHelpers;
import ruiseki.okcore.init.ItemCreativeTab;
import ruiseki.okcore.init.ModBaseVersionable;
import ruiseki.okcore.modcompat.ModCompatLoader;
import ruiseki.okcore.proxy.ICommonProxy;
import ruiseki.okstoragecore.helper.RecipeHelpers;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.MOD_VERSION,
    dependencies = Reference.MOD_DEPENDENCIES,
    guiFactory = Reference.GUI_FACTORY)
public class OKBackpack extends ModBaseVersionable {

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static ICommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static OKBackpack _instance;

    public OKBackpack() {
        super(Reference.MOD_ID, Reference.MOD_NAME, Reference.MOD_VERSION);

        registerWorldStorage(BackpackStorage.getInstance(this));

        ConfigCondition.registerConfig(
            new ResourceLocation(Reference.MOD_ID, "enableTravelersUpgrades"),
            GeneralConfig.enableTravelersUpgrades);

        addInitListeners(new ModRecipes());
        addInitListeners(new ModOreDicts());
        addInitListeners(new TierRegistries());
    }

    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        BackpackSHRegistries.load();
        OBUpgradeSlotSHRegistries.load();

        if (MinecraftHelpers.isClientSide()) {
            ModelRegistry.registerModid(Reference.MOD_ID);
            RenderingRegistry.registerBlockHandler(JsonModelISBRH.INSTANCE);
        }
    }

    @Override
    protected void loadModCompats(ModCompatLoader modCompatLoader) {
        super.loadModCompats(modCompatLoader);
        modCompatLoader.addModCompat(new ThaumcraftModCompat());
        modCompatLoader.addModCompat(new NEIModCompat());
        modCompatLoader.addModCompat(new JFMUYModCompat());
        modCompatLoader.addModCompat(new EFRModCompat());
        modCompatLoader.addModCompat(new StructureLibModCompat());
        modCompatLoader.addModCompat(new BaubleModCompat());
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
        if (MinecraftHelpers.isClientSide()) {
            TConstructTabCompat.registerClientTabs();
        }
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
        RecipeHelpers.setWorld(
            MinecraftServer.getServer()
                .getEntityWorld());
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
        RecipeHelpers.clearCache();
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return new ItemCreativeTab(this, new BlockItemConfigReference(BlockLeatherBackpackConfig.class));
    }

    @Override
    public void onGeneralConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new GeneralConfig());
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configHandler) {
        Configs.registerBlocks(configHandler);
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
        OKBackpack._instance.log(Level.INFO, message);
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
        OKBackpack._instance.log(level, message, params);
    }
}
