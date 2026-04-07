package ruiseki.okbackpack.common.item.refill;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okcore.helper.ItemNBTHelpers;

@Getter
public class AdvancedRefillUpgradeWrapper extends RefillUpgradeWrapper {

    private static final String TARGET_SLOTS_TAG = "TargetSlots";

    private final Map<Integer, TargetSlot> targetSlots;

    public AdvancedRefillUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer, 16);

        this.targetSlots = new HashMap<>();
        loadTargetSlots();
    }

    private void loadTargetSlots() {
        NBTTagCompound tag = ItemNBTHelpers.getCompound(upgrade, TARGET_SLOTS_TAG, false);
        if (tag != null) {
            for (String key : tag.func_150296_c()) {
                try {
                    int slot = Integer.parseInt(key);
                    int ordinal = tag.getInteger(key);
                    targetSlots.put(slot, TargetSlot.fromOrdinal(ordinal));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void saveTargetSlots() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<Integer, TargetSlot> entry : targetSlots.entrySet()) {
            tag.setInteger(
                String.valueOf(entry.getKey()),
                entry.getValue()
                    .ordinal());
        }
        ItemNBTHelpers.setCompound(upgrade, TARGET_SLOTS_TAG, tag);
        save();
    }

    @Override
    protected void onFilterSlotChanged(int slot) {
        ItemStack stack = filterHandler.getStackInSlot(slot);
        if (stack == null) {
            targetSlots.remove(slot);
            saveTargetSlots();
        } else if (!targetSlots.containsKey(slot)) {
            targetSlots.put(slot, TargetSlot.ANY);
            saveTargetSlots();
        }
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_refill_settings";
    }

    public TargetSlot getTargetSlot(int filterSlot) {
        return targetSlots.getOrDefault(filterSlot, TargetSlot.ANY);
    }

    public void setTargetSlot(int filterSlot, TargetSlot targetSlot) {
        targetSlots.put(filterSlot, targetSlot);
        saveTargetSlots();
    }

    @Override
    protected TargetSlot getTargetSlotForFilter(int filterSlot) {
        return targetSlots.getOrDefault(filterSlot, TargetSlot.ANY);
    }

    @Override
    public boolean allowsTargetSlotSelection() {
        return true;
    }

    @Override
    public boolean supportsBlockPick() {
        return true;
    }
}
