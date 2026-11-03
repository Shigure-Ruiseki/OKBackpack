package ruiseki.backpack.upgrades.anvil;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import ruiseki.backpack.client.gui.slot.AnvilResultSlot;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.inventory.InventoryCraftingWrapper;

public class AnvilContainer extends UpgradeContainer {

    private final InventoryCraftingWrapper inventoryAnvil;
    private AnvilResultSlot resultSlot;

    private int maximumCost;
    private int stackSizeToBeUsedInRepair;
    private String repairedItemName = "";

    public AnvilContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        Supplier<StorageContainer> containerSupplier, EntityPlayer player, int upgradeIndex,
        IItemHandlerModifiable anvilInventory) {
        super(storageWrapper, storagePanel, player, upgradeIndex);
        this.inventoryAnvil = new InventoryCraftingWrapper(containerSupplier, 2, 1, anvilInventory, 0);
    }

    public InventoryCraftingWrapper getInventoryAnvil() {
        return inventoryAnvil;
    }

    public int getMaximumCost() {
        return maximumCost;
    }

    public int getStackSizeToBeUsedInRepair() {
        return stackSizeToBeUsedInRepair;
    }

    public String getRepairedItemName() {
        return repairedItemName;
    }

    public void setRepairedItemName(String name) {
        this.repairedItemName = name == null ? "" : name;
        updateRepairOutput();
    }

    @Override
    public void detectAndSendChanges() {
        inventoryAnvil.detectChanges();
    }

    @Override
    public void onAdded(UpgradeSlotUpdate update) {
        AnvilResultSlot slot = update.get("anvil_result_slot");

        if (resultSlot != null && resultSlot != slot) {
            throw new IllegalStateException("Only one Anvil result slot is supported with AnvilContainer!");
        }

        resultSlot = slot;
    }

    @Override
    public void onCraftMatrixChanged(@NotNull IInventory inventory) {
        if (!getPlayer().worldObj.isRemote) {
            updateRepairOutput();
        }
    }

    public void updateRepairOutput() {
        ItemStack left = inventoryAnvil.getStackInSlot(0);

        maximumCost = 1;
        stackSizeToBeUsedInRepair = 0;

        if (left == null) {
            setResult(null);
            maximumCost = 0;
            return;
        }

        ItemStack result = left.copy();
        ItemStack right = inventoryAnvil.getStackInSlot(1);

        int cost = left.getRepairCost() + (right == null ? 0 : right.getRepairCost());
        int enchantmentCost = 0;

        Map<Integer, Integer> enchantments = EnchantmentHelper.getEnchantments(result);

        boolean enchantedBook = right != null && right.getItem() == Items.enchanted_book
            && Items.enchanted_book.func_92110_g(right)
                .tagCount() > 0;

        if (right != null) {
            if (left.isItemStackDamageable() && left.getItem()
                .getIsRepairable(left, right)) {

                int repairAmount = Math.min(result.getItemDamage(), result.getMaxDamage() / 4);

                if (repairAmount <= 0) {
                    setResult(null);
                    maximumCost = 0;
                    return;
                }

                int used = 0;

                for (; repairAmount > 0 && used < right.stackSize; used++) {
                    result.setItemDamage(result.getItemDamage() - repairAmount);

                    enchantmentCost++;

                    repairAmount = Math.min(result.getItemDamage(), result.getMaxDamage() / 4);
                }

                stackSizeToBeUsedInRepair = used;

            } else {
                if (!enchantedBook && (result.getItem() != right.getItem() || !result.isItemStackDamageable()
                    || result.getHasSubtypes())) {

                    setResult(null);
                    maximumCost = 0;
                    return;
                }

                if (result.isItemStackDamageable() && !enchantedBook) {
                    int leftDurability = left.getMaxDamage() - left.getItemDamage();
                    int rightDurability = right.getMaxDamage() - right.getItemDamage();

                    int addedDurability = rightDurability + result.getMaxDamage() * 12 / 100;
                    int combinedDurability = leftDurability + addedDurability;

                    int newDamage = result.getMaxDamage() - combinedDurability;

                    if (newDamage < 0) {
                        newDamage = 0;
                    }

                    if (newDamage < result.getItemDamage()) {
                        result.setItemDamage(newDamage);
                        enchantmentCost += 2;
                    }
                }

                Map<Integer, Integer> rightEnchantments = EnchantmentHelper.getEnchantments(right);

                for (Integer enchantmentId : rightEnchantments.keySet()) {
                    int id = enchantmentId.intValue();

                    Enchantment enchantment = Enchantment.enchantmentsList[id];

                    if (enchantment == null) {
                        continue;
                    }

                    int leftLevel = enchantments.containsKey(id) ? enchantments.get(id)
                        .intValue() : 0;

                    int rightLevel = rightEnchantments.get(id)
                        .intValue();

                    int level = leftLevel == rightLevel ? rightLevel + 1 : Math.max(rightLevel, leftLevel);

                    boolean canApply = enchantment.canApply(left);

                    if (getPlayer().capabilities.isCreativeMode
                        || left.getItem() == net.minecraft.init.Items.enchanted_book) {
                        canApply = true;
                    }

                    for (Integer existingId : enchantments.keySet()) {
                        int existing = existingId.intValue();

                        Enchantment existingEnchantment = Enchantment.enchantmentsList[existing];

                        if (existing != id && !(enchantment.canApplyTogether(existingEnchantment)
                            && existingEnchantment.canApplyTogether(enchantment))) {
                            canApply = false;
                            enchantmentCost++;
                        }
                    }

                    if (!canApply) {
                        continue;
                    }

                    if (level > enchantment.getMaxLevel()) {
                        level = enchantment.getMaxLevel();
                    }

                    enchantments.put(id, level);

                    int weightCost = 0;

                    switch (enchantment.getWeight()) {
                        case 1:
                            weightCost = 8;
                            break;
                        case 2:
                            weightCost = 4;
                            break;
                        case 5:
                            weightCost = 2;
                            break;
                        case 10:
                            weightCost = 1;
                            break;
                        default:
                            break;
                    }

                    if (enchantedBook) {
                        weightCost = Math.max(1, weightCost / 2);
                    }

                    enchantmentCost += weightCost * level;
                }
            }
        }

        if (enchantedBook && !result.getItem()
            .isBookEnchantable(result, right)) {
            result = null;
        }

        boolean wasRepaired = enchantmentCost > 0;
        boolean wasRenamed = false;

        if (result != null) {
            if (StringUtils.isBlank(repairedItemName)) {
                if (left.hasDisplayName()) {
                    result.func_135074_t();
                }
            } else if (!repairedItemName.equals(left.getDisplayName())) {
                result.setStackDisplayName(repairedItemName);
                enchantmentCost++;
                wasRenamed = true;
            }
        }

        maximumCost = wasRenamed && !wasRepaired ? 1 : cost + enchantmentCost;

        if (enchantmentCost <= 0) {
            result = null;
        }

        if (maximumCost >= 40 && !getPlayer().capabilities.isCreativeMode) {
            result = null;
        }

        if (result != null && wasRepaired) {
            int repairCost = result.getRepairCost();

            if (right != null && repairCost < right.getRepairCost()) {
                repairCost = right.getRepairCost();
            }

            result.setRepairCost(repairCost * 2 + 1);

            EnchantmentHelper.setEnchantments(enchantments, result);
        }

        setResult(result);
    }

    private void setResult(ItemStack stack) {
        if (resultSlot != null) {
            resultSlot.updateResult(stack);
        }
    }
}
