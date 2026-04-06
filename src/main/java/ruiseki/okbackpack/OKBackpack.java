package ruiseki.okbackpack;

import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.gtnewhorizon.gtnhlib.config.ConfigException;

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
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotGroupRegisters;
import ruiseki.okbackpack.client.renderer.JsonModelISBRH;
import ruiseki.okbackpack.common.command.CommandBackpack;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.common.init.ModOreDicts;
import ruiseki.okbackpack.common.recipe.CompactingRecipeCache;
import ruiseki.okbackpack.common.recipe.ModRecipes;
import ruiseki.okbackpack.compat.bauble.BaubleCompat;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.command.CommandMod;
import ruiseki.okcore.helper.MinecraftHelpers;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ICommonProxy;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    dependencies = Reference.DEPENDENCIES,
    guiFactory = Reference.GUI_FACTORY)
public class OKBackpack extends ModBase {

    static {
        try {
            ModConfig.registerConfig();
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static ICommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static OKBackpack instance;

    public OKBackpack() {
        super(Reference.MOD_ID, Reference.MOD_NAME);
        putGenericReference(REFKEY_MOD_VERSION, Reference.VERSION);
        addInitListeners(new BaubleCompat());
        addInitListeners(new ModRecipes());
        addInitListeners(new ModOreDicts());
        addInitListeners(new UpgradeSlotGroupRegisters());
        addInitListeners(new UpgradeSlotSHRegisters());
        addInitListeners(new DelegatedStackHandlerSHRegisters());
    }

    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ModBlocks.preInit();
        ModItems.preInit();
        if (MinecraftHelpers.isClientSide()) {
            ModelRegistry.registerModid(Reference.MOD_ID);
            RenderingRegistry.registerBlockHandler(JsonModelISBRH.INSTANCE);
        }
    }

    @Override
    protected CommandMod constructBaseCommand() {
        Map<String, ICommand> commands = Maps.newHashMap();
        CommandMod command = new CommandBackpack(this, commands);
        command.addAlias("okbackpack");
        return command;
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
        OKBackpack.instance.log(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     *
     * @param level   The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void okLog(Level level, String message) {
        OKBackpack.instance.log(level, message);
    }
}
