package ruiseki.okbackpack.common.item.anvil;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.StringUtils;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IAnvilUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AnvilUpgradeWrapper extends UpgradeWrapperBase implements IAnvilUpgrade {

    protected final BaseItemStackHandler anvilInventory;
    protected int maximumCost;
    protected int stackSizeToBeUsedInRepair;
    protected String repairedItemName;
    protected boolean suppressUpdate = false;

    public AnvilUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);

        this.anvilInventory = new BaseItemStackHandler(3) {

            @Override
            protected void onContentsChanged(int slot) {
                if (suppressUpdate) return;
                if (slot == 0) {
                    ItemStack leftInput = getStackInSlot(0);
                    if (leftInput == null) {
                        repairedItemName = "";
                    } else {
                        repairedItemName = leftInput.getDisplayName();
                    }
                }
                if (slot == 0 || slot == 1) {
                    updateRepairOutput();
                }
                saveInventory();
            }
        };

        NBTTagCompound invTag = ItemNBTHelpers.getCompound(upgrade, ANVIL_INV_TAG, false);
        if (invTag != null) anvilInventory.deserializeNBT(invTag);

        this.maximumCost = ItemNBTHelpers.getInt(upgrade, MAXIMUM_COST_TAG, 0);
        this.stackSizeToBeUsedInRepair = ItemNBTHelpers.getInt(upgrade, STACK_SIZE_TO_USE_TAG, 0);
        NBTTagCompound nbt = ItemNBTHelpers.getNBT(upgrade);
        this.repairedItemName = nbt.hasKey(REPAIRED_ITEM_NAME_TAG) ? nbt.getString(REPAIRED_ITEM_NAME_TAG) : "";
    }

    private void saveInventory() {
        NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
        tag.setTag(ANVIL_INV_TAG, anvilInventory.serializeNBT());
        save();
    }

    private void saveState() {
        ItemNBTHelpers.setInt(upgrade, MAXIMUM_COST_TAG, maximumCost);
        ItemNBTHelpers.setInt(upgrade, STACK_SIZE_TO_USE_TAG, stackSizeToBeUsedInRepair);
        NBTTagCompound nbt = ItemNBTHelpers.getNBT(upgrade);
        nbt.setString(REPAIRED_ITEM_NAME_TAG, repairedItemName != null ? repairedItemName : "");
        NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
        tag.setTag(ANVIL_INV_TAG, anvilInventory.serializeNBT());
        save();
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.anvil_settings";
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return anvilInventory;
    }

    @Override
    public int getMaximumCost() {
        return maximumCost;
    }

    @Override
    public String getRepairedItemName() {
        return repairedItemName;
    }

    @Override
    public void setRepairedItemName(String name) {
        this.repairedItemName = name;
    }

    @Override
    public int getStackSizeToBeUsedInRepair() {
        return stackSizeToBeUsedInRepair;
    }

    @Override
    public void updateRepairOutput() {
        ItemStack itemstack = anvilInventory.getStackInSlot(0);
        this.maximumCost = 0;
        int i = 0;
        byte b0 = 0;
        int j = 0;

        if (itemstack == null) {
            setOutputDirect(null);
            this.maximumCost = 0;
            saveState();
            return;
        }

        ItemStack itemstack1 = itemstack.copy();
        ItemStack itemstack2 = anvilInventory.getStackInSlot(1);
        Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
        boolean flag = false;
        int k2 = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
        this.stackSizeToBeUsedInRepair = 0;
        int k;
        int l;
        int i1;
        int k1;
        int l1;
        Iterator<Integer> iterator1;
        Enchantment enchantment;

        if (itemstack2 != null) {
            flag = itemstack2.getItem() == Items.enchanted_book && Items.enchanted_book.func_92110_g(itemstack2)
                .tagCount() > 0;

            if (itemstack1.isItemStackDamageable() && itemstack1.getItem()
                .getIsRepairable(itemstack, itemstack2)) {
                k = Math.min(itemstack1.getItemDamageForDisplay(), itemstack1.getMaxDamage() / 4);

                if (k <= 0) {
                    setOutputDirect(null);
                    this.maximumCost = 0;
                    saveState();
                    return;
                }

                for (l = 0; k > 0 && l < itemstack2.stackSize; ++l) {
                    i1 = itemstack1.getItemDamageForDisplay() - k;
                    itemstack1.setItemDamage(i1);
                    i += Math.max(1, k / 100) + map.size();
                    k = Math.min(itemstack1.getItemDamageForDisplay(), itemstack1.getMaxDamage() / 4);
                }

                this.stackSizeToBeUsedInRepair = l;
            } else {
                if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable())) {
                    setOutputDirect(null);
                    this.maximumCost = 0;
                    saveState();
                    return;
                }

                if (itemstack1.isItemStackDamageable() && !flag) {
                    k = itemstack.getMaxDamage() - itemstack.getItemDamageForDisplay();
                    l = itemstack2.getMaxDamage() - itemstack2.getItemDamageForDisplay();
                    i1 = l + itemstack1.getMaxDamage() * 12 / 100;
                    int j1 = k + i1;
                    k1 = itemstack1.getMaxDamage() - j1;

                    if (k1 < 0) {
                        k1 = 0;
                    }

                    if (k1 < itemstack1.getItemDamage()) {
                        itemstack1.setItemDamage(k1);
                        i += Math.max(1, i1 / 100);
                    }
                }

                Map<Integer, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                iterator1 = map1.keySet()
                    .iterator();

                while (iterator1.hasNext()) {
                    i1 = iterator1.next();
                    enchantment = Enchantment.enchantmentsList[i1];
                    if (enchantment == null) continue;

                    k1 = map.getOrDefault(i1, 0);
                    l1 = map1.get(i1);
                    int i3;

                    if (k1 == l1) {
                        ++l1;
                        i3 = l1;
                    } else {
                        i3 = Math.max(l1, k1);
                    }

                    l1 = i3;
                    int i2 = l1 - k1;
                    boolean flag1 = enchantment.canApply(itemstack);

                    if (itemstack.getItem() == Items.enchanted_book) {
                        flag1 = true;
                    }

                    for (int j2 : map.keySet()) {
                        Enchantment e2 = Enchantment.enchantmentsList[j2];
                        if (e2 != null && j2 != i1
                            && !(enchantment.canApplyTogether(e2) && e2.canApplyTogether(enchantment))) {
                            flag1 = false;
                            i += i2;
                        }
                    }

                    if (flag1) {
                        if (l1 > enchantment.getMaxLevel()) {
                            l1 = enchantment.getMaxLevel();
                        }

                        map.put(i1, l1);
                        int l2 = switch (enchantment.getWeight()) {
                            case 1 -> 8;
                            case 2 -> 4;
                            case 5 -> 2;
                            case 10 -> 1;
                            default -> 0;
                        };

                        if (flag) {
                            l2 = Math.max(1, l2 / 2);
                        }

                        i += l2 * i2;
                    }
                }
            }
        }

        if (StringUtils.isBlank(this.repairedItemName)) {
            if (itemstack.hasDisplayName()) {
                j = itemstack.isItemStackDamageable() ? 7 : itemstack.stackSize * 5;
                i += j;
                itemstack1.func_135074_t();
            }
        } else if (!this.repairedItemName.equals(itemstack.getDisplayName())) {
            j = itemstack.isItemStackDamageable() ? 7 : itemstack.stackSize * 5;
            i += j;

            if (itemstack.hasDisplayName()) {
                k2 += j / 2;
            }

            itemstack1.setStackDisplayName(this.repairedItemName);
        }

        k = 0;

        for (iterator1 = map.keySet()
            .iterator(); iterator1.hasNext(); k2 += k + k1 * l1) {
            i1 = iterator1.next();
            enchantment = Enchantment.enchantmentsList[i1];
            k1 = map.get(i1);
            l1 = 0;
            ++k;

            if (enchantment != null) {
                switch (enchantment.getWeight()) {
                    case 1:
                        l1 = 8;
                        break;
                    case 2:
                        l1 = 4;
                        break;
                    case 5:
                        l1 = 2;
                        break;
                    case 10:
                        l1 = 1;
                        break;
                }

                if (flag) {
                    l1 = Math.max(1, l1 / 2);
                }
            }
        }

        if (flag) {
            k2 = Math.max(1, k2 / 2);
        }

        if (flag && !itemstack1.getItem()
            .isBookEnchantable(itemstack1, itemstack2)) {
            itemstack1 = null;
        }

        this.maximumCost = k2 + i;

        if (i <= 0) {
            itemstack1 = null;
        }

        if (j == i && j > 0 && this.maximumCost >= 40) {
            this.maximumCost = 39;
        }

        if (itemstack1 != null) {
            l = itemstack1.getRepairCost();

            if (itemstack2 != null && l < itemstack2.getRepairCost()) {
                l = itemstack2.getRepairCost();
            }

            if (itemstack1.hasDisplayName()) {
                l -= 9;
            }

            if (l < 0) {
                l = 0;
            }

            l += 2;
            itemstack1.setRepairCost(l);
            EnchantmentHelper.setEnchantments(map, itemstack1);
        }

        setOutputDirect(itemstack1);
        saveState();
    }

    private void setOutputDirect(ItemStack stack) {
        suppressUpdate = true;
        try {
            anvilInventory.setStackInSlot(2, stack);
        } finally {
            suppressUpdate = false;
        }
    }

    @Override
    public void onTakeOutput(EntityPlayer player) {
        if (player == null) return;

        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-this.maximumCost);
        }

        suppressUpdate = true;
        try {
            anvilInventory.setStackInSlot(0, null);

            if (this.stackSizeToBeUsedInRepair > 0) {
                ItemStack rightInput = anvilInventory.getStackInSlot(1);

                if (rightInput != null && rightInput.stackSize > this.stackSizeToBeUsedInRepair) {
                    rightInput.stackSize -= this.stackSizeToBeUsedInRepair;
                    anvilInventory.setStackInSlot(1, rightInput);
                } else {
                    anvilInventory.setStackInSlot(1, null);
                }
            } else {
                anvilInventory.setStackInSlot(1, null);
            }

            this.maximumCost = 0;
            this.stackSizeToBeUsedInRepair = 0;
            this.repairedItemName = "";
        } finally {
            suppressUpdate = false;
        }

        saveState();
    }
}
