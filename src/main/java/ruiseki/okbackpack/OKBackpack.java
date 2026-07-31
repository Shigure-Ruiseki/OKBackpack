package ruiseki.okbackpack;

import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.Level;

import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

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
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedValueSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotGroupRegisters;
import ruiseki.okbackpack.client.renderer.JsonModelISBRH;
import ruiseki.okbackpack.common.command.CommandBackpack;
import ruiseki.okbackpack.common.init.ModOreDicts;
import ruiseki.okbackpack.common.init.OKBackpackBlocks;
import ruiseki.okbackpack.common.init.OKBackpackItems;
import ruiseki.okbackpack.common.init.TierRegistries;
import ruiseki.okbackpack.common.recipe.CompactingRecipeCache;
import ruiseki.okbackpack.common.recipe.ModRecipes;
import ruiseki.okbackpack.compat.bauble.BaubleCompat;
import ruiseki.okbackpack.compat.findit.FindItCompat;
import ruiseki.okbackpack.compat.structurelib.StructureLibCompat;
import ruiseki.okbackpack.compat.tic.TConstructTabCompat;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.data.condition.ConfigCondition;
import ruiseki.okcore.helper.MinecraftHelpers;
import ruiseki.okcore.init.ModBaseVersionable;
import ruiseki.okcore.proxy.ICommonProxy;
import ruiseki.okcore.tracking.Versions;

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

        ConfigCondition.registerConfig(
            new ResourceLocation(Reference.MOD_ID, "enableTravelersUpgrades"),
            ModConfig.enableTravelersUpgrades);

        addInitListeners(new BaubleCompat());
        addInitListeners(new StructureLibCompat());
        addInitListeners(new ModRecipes());
        addInitListeners(new ModOreDicts());
        addInitListeners(new UpgradeSlotGroupRegisters());
        addInitListeners(new UpgradeSlotSHRegisters());
        addInitListeners(new BackpackSlotSHRegisters());
        addInitListeners(new BackpackSHRegisters());
        addInitListeners(new DelegatedStackHandlerSHRegisters());
        addInitListeners(new DelegatedValueSHRegisters());
        addInitListeners(new TierRegistries());
    }

    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        OKBackpackBlocks.register();
        OKBackpackItems.register();
        if (MinecraftHelpers.isClientSide()) {
            ModelRegistry.registerModid(Reference.MOD_ID);
            RenderingRegistry.registerBlockHandler(JsonModelISBRH.INSTANCE);
        }
        if (ModConfig.useVersionChecker) {
            Versions.registerMod(this, this, Reference.UPDATE_URL);
        }
    }

    @Override
    protected LiteralArgumentBuilder<ICommandSender> constructBaseCommand(MinecraftServer server) {
        LiteralArgumentBuilder<ICommandSender> builder = super.constructBaseCommand(server);
        builder.then(new CommandBackpack(this, server).make());
        return builder;
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
        FindItCompat.registerProvider();
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
        CompactingRecipeCache.getInstance()
            .initialize(
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
        CompactingRecipeCache.getInstance()
            .invalidate();
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return OKBCreativeTab.INSTANCE;
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
