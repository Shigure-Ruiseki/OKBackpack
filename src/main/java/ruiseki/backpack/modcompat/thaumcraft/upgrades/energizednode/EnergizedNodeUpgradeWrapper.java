package ruiseki.backpack.modcompat.thaumcraft.upgrades.energizednode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.modcompat.thaumcraft.helper.ThaumcraftChargeHelper;
import ruiseki.backpack.modcompat.thaumcraft.helper.ThaumcraftChargeHelper.AspectBudget;
import ruiseki.backpack.modcompat.thaumcraft.upgrades.IVisChargeUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;
import thaumcraft.api.aspects.Aspect;

public class EnergizedNodeUpgradeWrapper
    extends UpgradeWrapperBase<EnergizedNodeUpgradeWrapper, ItemEnergizedNodeUpgrade> implements ITickableUpgrade {

    public static final String ASPECTS_TAG = "aspects_inventory";

    protected EnergizedNodeUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!hasAspect(upgrade)) return;
        AspectBudget budget = ThaumcraftChargeHelper.createBudget(getStoredAspectRates(upgrade));
        if (budget.isExhausted()) return;
        ThaumcraftChargeHelper.chargeStacks(this.getVisChargeableStacks(), budget);
    }

    public static boolean hasAspect(ItemStack stack) {
        return stack != null && NBTHelpers.getCompoundTag(stack, ASPECTS_TAG)
            .isPresent();
    }

    public static LinkedHashMap<Aspect, Integer> getStoredAspectRates(ItemStack stack) {
        LinkedHashMap<Aspect, Integer> result = new LinkedHashMap<>();
        NBTHelpers.getCompoundTag(stack, ASPECTS_TAG)
            .ifPresent(tag -> {
                for (Aspect aspect : Aspect.getPrimalAspects()) {
                    if (aspect != null) {
                        result.put(aspect, Math.max(0, tag.getInteger(aspect.getTag())));
                    }
                }
            });
        return result;
    }

    public static void setStoredAspectRates(ItemStack stack, Map<Aspect, Integer> aspectRates) {
        NBTTagCompound nodeTag = new NBTTagCompound();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (aspect != null) {
                int amount = aspectRates == null ? 0 : Math.max(0, aspectRates.getOrDefault(aspect, 0));
                nodeTag.setInteger(aspect.getTag(), amount);
            }
        }
        NBTHelpers.setCompoundTag(stack, ASPECTS_TAG, nodeTag);
    }

    public Iterable<ItemStack> getVisChargeableStacks() {
        List<ItemStack> entries = new ArrayList<>();
        this.storageWrapper.getUpgradeHandler()
            .getWrappersThatImplement(IVisChargeUpgrade.class)
            .forEach(upgrade -> {
                for (ItemStack stack : upgrade.getVisChargeableStacks()) {
                    entries.add(stack);
                }
            });
        return entries;
    }
}
