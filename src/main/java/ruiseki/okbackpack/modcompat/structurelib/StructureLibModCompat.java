package ruiseki.okbackpack.modcompat.structurelib;

import java.util.function.Function;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import ruiseki.okbackpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.capability.wrapper.InventoryAdapter;
import ruiseki.okcore.modcompat.IModCompat;

public class StructureLibModCompat implements IModCompat {

    public static final String REGISTRY_KEY = "0999-okbackpack";

    @Override
    public String getModID() {
        return Mods.StructureLib.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "StructureLib compatibility";
    }

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            InventoryUtility.registerStackExtractor(REGISTRY_KEY, new BackpackExtractor());
        }
    }

    public static final class BackpackExtractor implements Function<ItemStack, IInventory> {

        @Override
        public IInventory apply(ItemStack stack) {
            if (ItemHelpers.isEmpty(stack)) return null;
            return CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
                .map(wrapper -> new InventoryAdapter(wrapper.getInventoryHandler()))
                .orElse(null);
        }
    }
}
