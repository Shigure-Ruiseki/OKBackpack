package ruiseki.backpack.upgrades.anvil;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.slot.ModularResultSlot;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.inventory.InventoryCraftingWrapper;

public class AnvilContainer extends UpgradeContainer {

    private final InventoryCraftingWrapper inventory;
    private ModularResultSlot resultSlot;

    private int maximumCost;
    private int stackSizeToBeUsedInRepair;
    private DelegatedStringSH nameField;
    private DelegatedStringSH requiredField;

    public AnvilContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        Supplier<StorageContainer> containerSupplier, EntityPlayer player, int upgradeIndex,
        IItemHandlerModifiable anvilInventory) {
        super(storageWrapper, storagePanel, player, upgradeIndex);
        this.inventory = new InventoryCraftingWrapper(containerSupplier, 2, 1, anvilInventory, 0);
    }

    @Override
    public void detectAndSendChanges() {
        inventory.detectChanges();
        this.updateCostText();
    }

    @Override
    public void onAdded(UpgradeSlotUpdate update) {
        ModularResultSlot slot = update.get("anvil_result_slot");
        if (this.resultSlot != null && this.resultSlot != slot) {
            throw new IllegalStateException("Only one crafting output slot is supported with AnvilContainer!");
        }
        this.resultSlot = slot;
        slot.setContainer(this);

        this.nameField = update.get("anvil_name_field");
        this.requiredField = update.get("anvil_required_field");
    }

    @Override
    public void onCraftMatrixChanged(@NotNull IInventory inventory) {
        if (!getPlayer().worldObj.isRemote) {
            updateRepairOutput();
        }
    }

    public void updateRepairOutput() {
        ItemStack left = inventory.getStackInSlot(0);

        maximumCost = 1;
        stackSizeToBeUsedInRepair = 0;

        if (ItemHelpers.isEmpty(left)) {
            clearResult();
            return;
        }

        ItemStack result = ItemHelpers.copy(left);
        ItemStack right = inventory.getStackInSlot(1);

        int cost = left.getRepairCost() + (right == null ? 0 : right.getRepairCost());
        int enchantmentCost = 0;

        Map<Integer, Integer> enchantments = EnchantmentHelper.getEnchantments(result);

        boolean enchantedBook = right != null && right.getItem() == Items.enchanted_book
            && Items.enchanted_book.func_92110_g(right)
                .tagCount() > 0;

        if (!ItemHelpers.isEmpty(right)) {
            if (left.isItemStackDamageable() && left.getItem()
                .getIsRepairable(left, right)) {

                int repairAmount = Math.min(result.getItemDamage(), result.getMaxDamage() / 4);

                if (repairAmount <= 0) {
                    clearResult();
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
                    clearResult();
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

                for (Integer id : rightEnchantments.keySet()) {

                    Enchantment enchantment = Enchantment.enchantmentsList[id];
                    if (enchantment == null) continue;

                    int leftLevel = enchantments.getOrDefault(id, 0);
                    int rightLevel = rightEnchantments.get(id);

                    int level = leftLevel == rightLevel ? rightLevel + 1 : Math.max(rightLevel, leftLevel);

                    boolean canApply = enchantment.canApply(left);

                    if (getPlayer().capabilities.isCreativeMode
                        || left.getItem() == net.minecraft.init.Items.enchanted_book) {
                        canApply = true;
                    }

                    for (Integer existing : enchantments.keySet()) {
                        Enchantment existingEnchantment = Enchantment.enchantmentsList[existing];
                        if (!existing.equals(id) && !(enchantment.canApplyTogether(existingEnchantment)
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
            result = ItemHelpers.EMPTY;
        }

        boolean wasRepaired = enchantmentCost > 0;
        boolean wasRenamed = false;

        if (!ItemHelpers.isEmpty(result)) {
            if (StringUtils.isBlank(nameField.getValue())) {
                if (left.hasDisplayName()) {
                    result.func_135074_t();
                }
            } else if (!nameField.getStringValue()
                .equals(left.getDisplayName())) {
                    result.setStackDisplayName(nameField.getValue());
                    enchantmentCost++;
                    wasRenamed = true;
                }
        }

        maximumCost = wasRenamed && !wasRepaired ? 1 : cost + enchantmentCost;

        if (enchantmentCost <= 0) {
            result = ItemHelpers.EMPTY;
        }

        if (maximumCost >= 40 && !getPlayer().capabilities.isCreativeMode) {
            result = ItemHelpers.EMPTY;
        }

        if (!ItemHelpers.isEmpty(result) && wasRepaired) {
            int repairCost = result.getRepairCost();

            if (!ItemHelpers.isEmpty(right) && repairCost < right.getRepairCost()) {
                repairCost = right.getRepairCost();
            }

            result.setRepairCost(repairCost * 2 + 1);

            EnchantmentHelper.setEnchantments(enchantments, result);
        }

        this.resultSlot.updateResult(result);
        this.updateCostText();
    }

    private void updateCostText() {
        String costText = "";

        if (maximumCost > 0 && !ItemHelpers.isEmpty(resultSlot.getStack())) {
            boolean creative = getPlayer().capabilities.isCreativeMode;

            if (maximumCost >= 40 && !creative) {
                costText = "§c" + StatCollector.translateToLocal("container.repair.expensive");
            } else {
                String text = StatCollector.translateToLocalFormatted("container.repair.cost", maximumCost);

                boolean canAfford = creative || getPlayer().experienceLevel >= maximumCost;
                costText = (canAfford ? "§a" : "§c") + text;
            }
        }

        if (!costText.equals(requiredField.getStringValue())) {
            requiredField.setValue(costText);
        }
    }

    private void clearResult() {
        maximumCost = 0;
        stackSizeToBeUsedInRepair = 0;
        resultSlot.updateResult(ItemHelpers.EMPTY);
    }

    @Override
    public void onTakeResult(EntityPlayer player, ItemStack stack) {
        int cost = maximumCost;

        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-cost);
        }

        // Consume left input.
        this.inventory.setSlot(0, ItemHelpers.EMPTY, false);

        // Consume right input.
        ItemStack right = this.inventory.getStackInSlot(1);

        if (!ItemHelpers.isEmpty(right)) {
            int consume = Math.max(1, stackSizeToBeUsedInRepair);

            ItemStack remaining = ItemHelpers.copy(right);
            ItemHelpers.shrink(remaining, consume);

            this.inventory.setSlot(1, ItemHelpers.isEmpty(remaining) ? ItemHelpers.EMPTY : remaining, false);
        }

        // Recalculate output.
        this.inventory.notifyContainer();
    }
}
