package ruiseki.okbackpack.common.item.energizednode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IEnergizedNodeUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftChargeHelper;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;
import thaumcraft.api.aspects.Aspect;

public class EnergizedNodeUpgradeWrapper extends UpgradeWrapperBase implements IEnergizedNodeUpgrade {

    public static final String NODE_CV_TAG = "NodeCv";

    private final BaseItemStackHandler storageHandler;
    private int cachedUpgradeSlotIndex = -1;

    public EnergizedNodeUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
        this.storageHandler = new BaseItemStackHandler(0);
        this.storageHandler.setVisualSize(0);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.energized_node_settings";
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return storageHandler;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        return doTick();
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        return doTick();
    }

    public boolean doTick() {
        if (!isConfigured(upgrade)) return false;

        if (!(storage instanceof BackpackWrapper backpackWrapper)) {
            return false;
        }

        ThaumcraftChargeHelper.AspectBudget budget = ThaumcraftChargeHelper.createBudget(getStoredAspectRates(upgrade));
        if (budget.isExhausted()) return false;

        return ThaumcraftChargeHelper
            .chargeStacks(backpackWrapper.getVisChargeableStacksExcluding(getUpgradeSlotIndex()), budget);
    }

    @Override
    public Iterable<ItemStack> getVisChargeableStacks() {
        return Collections.emptyList();
    }

    public static boolean isConfigured(ItemStack stack) {
        return stack != null && ItemNBTHelpers.getNBT(stack)
            .hasKey(NODE_CV_TAG, 10);
    }

    public static LinkedHashMap<Aspect, Integer> getStoredAspectRates(ItemStack stack) {
        LinkedHashMap<Aspect, Integer> result = new LinkedHashMap<>();
        var tag = ItemNBTHelpers.getCompound(stack, NODE_CV_TAG, false);

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (aspect != null) {
                result.put(aspect, tag == null ? 0 : Math.max(0, tag.getInteger(aspect.getTag())));
            }
        }

        return result;
    }

    public static void setStoredAspectRates(ItemStack stack, Map<Aspect, Integer> aspectRates) {
        var root = ItemNBTHelpers.getNBT(stack);
        var nodeTag = new net.minecraft.nbt.NBTTagCompound();

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (aspect != null) {
                int amount = aspectRates == null ? 0 : Math.max(0, aspectRates.getOrDefault(aspect, 0));
                nodeTag.setInteger(aspect.getTag(), amount);
            }
        }

        root.setTag(NODE_CV_TAG, nodeTag);
    }

    private int getUpgradeSlotIndex() {
        if (cachedUpgradeSlotIndex >= 0) {
            return cachedUpgradeSlotIndex;
        }

        for (int i = 0; i < storage.getUpgradeHandler()
            .getSlots(); i++) {
            if (storage.getUpgradeHandler()
                .getStackInSlot(i) == upgrade) {
                cachedUpgradeSlotIndex = i;
                break;
            }
        }

        return cachedUpgradeSlotIndex;
    }
}
