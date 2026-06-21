package ruiseki.okbackpack.compat.structurelib;

import java.util.function.Function;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import cpw.mods.fml.common.Optional;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.inventory.BackpackWrapperInventoryAdapter;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.init.IInitListener;

public class StructureLibCompat implements IInitListener {

    public static final String REGISTRY_KEY = "0999-okbackpack";

    public static boolean registered;

    @Override
    public void onInit(Step step) {
        if (step != Step.POSTINIT || registered || !Mods.StructureLib.isModLoaded()) return;
        register();
    }

    @Optional.Method(modid = "structurelib")
    public static void register() {
        if (registered) return;
        InventoryUtility.registerStackExtractor(REGISTRY_KEY, new StructureLibBackpackExtractor());
        registered = true;
        OKBackpack.okLog("Registered StructureLib backpack extractor");
    }

    public static final class StructureLibBackpackExtractor implements Function<ItemStack, IInventory> {

        @Override
        public IInventory apply(ItemStack stack) {
            if (!BackpackEntityHelpers.isBackpackStack(stack, false)) {
                return null;
            }
            BackpackWrapper wrapper = new BackpackWrapper(stack, (BlockBackpack.ItemBackpack) stack.getItem());
            return new BackpackWrapperInventoryAdapter(wrapper);
        }
    }
}
