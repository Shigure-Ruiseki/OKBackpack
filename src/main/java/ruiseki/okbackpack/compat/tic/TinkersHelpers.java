package ruiseki.okbackpack.compat.tic;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.helper.MinecraftHelpers;

public class TinkersHelpers {

    private static final String CLASS_CRAFTING_LOGIC = "tconstruct.tools.logic.CraftingStationLogic";
    private static final String CLASS_CRAFTING_STATION = "tconstruct.tools.inventory.CraftingStationContainer";
    private static final String METHOD_ON_CRAFT_CHANGED = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")
        ? "onCraftMatrixChanged"
        : "func_75130_a";
    private static final String FIELD_CRAFT_MATRIX = "craftMatrix";
    private static final String FIELD_CRAFT_RESULT = "craftResult";

    private static Class<?> craftingStation;
    private static Object craftingStationInstance;

    private TinkersHelpers() {}

    static {
        if (Mods.TConstruct.isLoaded()) {
            createCraftingStationInstance();
        }
    }

    private static void createCraftingStationInstance() {
        try {
            Class<?> craftingLogic = Class.forName(CLASS_CRAFTING_LOGIC);
            Object craftingLogicInstance = craftingLogic.getConstructor()
                .newInstance();
            InventoryPlayer invPlayer = getInventoryPlayer();

            craftingStation = Class.forName(CLASS_CRAFTING_STATION);
            craftingStationInstance = craftingStation
                .getConstructor(InventoryPlayer.class, craftingLogic, int.class, int.class, int.class)
                .newInstance(invPlayer, craftingLogicInstance, 0, 0, 0);
        } catch (Exception e) {
            OKBackpack.okLog(Level.ERROR, "Error getting instance of Tinkers Crafting Station: " + e);
        }
    }

    private static InventoryPlayer getInventoryPlayer() {
        InventoryPlayer invPlayer;
        if (MinecraftHelpers.isServerSide()) {
            WorldServer world = FMLCommonHandler.instance()
                .getMinecraftServerInstance().worldServers[0];
            UUID fakeUuid = UUID.fromString("521e749d-2ac0-3459-af7a-160b4be5c62b");
            GameProfile fakeProfile = new GameProfile(fakeUuid, "[Adventurer]");
            invPlayer = new InventoryPlayer(new FakePlayer(world, fakeProfile));
        } else {
            invPlayer = Minecraft.getMinecraft().thePlayer.inventory;
        }
        return invPlayer;
    }

    @Nullable
    public static synchronized ItemStack getTinkersRecipe(InventoryCrafting craftMatrix) {
        if (craftingStationInstance == null) return null;

        try {
            craftingStation.getField(FIELD_CRAFT_MATRIX)
                .set(craftingStationInstance, craftMatrix);

            craftingStation.getMethod(METHOD_ON_CRAFT_CHANGED, IInventory.class)
                .invoke(craftingStationInstance, craftMatrix);

            return ((IInventory) craftingStation.getField(FIELD_CRAFT_RESULT)
                .get(craftingStationInstance)).getStackInSlot(0);
        } catch (Exception e) {
            OKBackpack.okLog(Level.ERROR, "Error during reflection in getTinkersRecipe: " + e);
            return null;
        }
    }
}
