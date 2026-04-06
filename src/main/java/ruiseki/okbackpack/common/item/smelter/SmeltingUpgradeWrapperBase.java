package ruiseki.okbackpack.common.item.smelter;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public abstract class SmeltingUpgradeWrapperBase extends UpgradeWrapperBase
    implements ISmeltingUpgrade, ISlotModifiable {

    protected final IStorageWrapper storage;
    protected BaseItemStackHandler smeltingInventory;

    public SmeltingUpgradeWrapperBase(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
        this.storage = storage;

        this.smeltingInventory = new BaseItemStackHandler(3) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag("SmeltingInv", this.serializeNBT());
                save();
            }
        };
        NBTTagCompound invTag = ItemNBTHelpers.getCompound(upgrade, "SmeltingInv", false);
        if (invTag != null) smeltingInventory.deserializeNBT(invTag);
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return smeltingInventory;
    }

    @Override
    public int getTotalCookTime() {
        return 200;
    }

    @Override
    public int getCookTime() {
        return ItemNBTHelpers.getInt(upgrade, COOK_TIME_TAG, 0);
    }

    @Override
    public void setCookTime(int progress) {
        ItemNBTHelpers.setInt(upgrade, COOK_TIME_TAG, progress);
        save();
    }

    @Override
    public int getBurnTime() {
        return ItemNBTHelpers.getInt(upgrade, BURN_TIME_TAG, 0);
    }

    @Override
    public void setBurnTime(int progress) {
        ItemNBTHelpers.setInt(upgrade, BURN_TIME_TAG, progress);
        save();
    }

    @Override
    public int getTotalBurnTime() {
        return ItemNBTHelpers.getInt(upgrade, BURN_TIME_TOTAL_TAG, 0);
    }

    @Override
    public void setTotalBurnTime(int total) {
        ItemNBTHelpers.setInt(upgrade, BURN_TIME_TOTAL_TAG, total);
        save();
    }

    @Override
    public boolean isBurning() {
        return getBurnTime() > 0;
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IUpgradeItem<?>item)) return true;

        IUpgradeWrapper candidate = item.createWrapper(stack, storage, null);
        return !(candidate instanceof ISmeltingUpgrade);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.smelting_settings";
    }

    protected ItemStack getInput() {
        return smeltingInventory.getStackInSlot(0);
    }

    protected void setInput(ItemStack stack) {
        smeltingInventory.setStackInSlot(0, stack);
    }

    protected ItemStack getFuel() {
        return smeltingInventory.getStackInSlot(1);
    }

    protected void setFuel(ItemStack stack) {
        smeltingInventory.setStackInSlot(1, stack);
    }

    protected ItemStack getOutput() {
        return smeltingInventory.getStackInSlot(2);
    }

    protected void setOutput(ItemStack stack) {
        smeltingInventory.setStackInSlot(2, stack);
    }

    protected void doSmeltTick() {
        if (!isEnabled()) return;

        boolean dirty = false;
        int fuelProgress = getBurnTime();
        int smeltProgress = getCookTime();
        int fuelTotal = getTotalBurnTime();

        if (fuelProgress > 0) {
            fuelProgress--;
            setBurnTime(fuelProgress);
        }

        ItemStack input = getInput();
        if (input != null) {
            ItemStack result = getSmeltingResult(input);
            if (result != null) {
                ItemStack output = getOutput();
                boolean canOutput = output == null || (ItemHandlerHelper.canItemStacksStack(output, result)
                    && output.stackSize + result.stackSize <= output.getMaxStackSize());

                if (canOutput) {
                    if (fuelProgress <= 0) {
                        ItemStack fuel = getFuel();
                        if (fuel != null) {
                            int burnTime = TileEntityFurnace.getItemBurnTime(fuel);
                            if (burnTime > 0) {
                                fuelProgress = burnTime;
                                fuelTotal = burnTime;
                                setBurnTime(fuelProgress);
                                setTotalBurnTime(fuelTotal);

                                fuel.stackSize--;
                                if (fuel.stackSize <= 0 && fuel.getItem() != null) {
                                    ItemStack containerItem = fuel.getItem()
                                        .getContainerItem(fuel);
                                    setFuel(containerItem);
                                } else {
                                    setFuel(fuel);
                                }
                                dirty = true;
                            }
                        }
                    }

                    if (fuelProgress > 0) {
                        smeltProgress++;
                        if (smeltProgress >= getTotalCookTime()) {
                            smeltProgress = 0;
                            if (output == null) {
                                setOutput(result.copy());
                            } else {
                                output.stackSize += result.stackSize;
                                setOutput(output);
                            }
                            input.stackSize--;
                            if (input.stackSize <= 0) {
                                setInput(null);
                            } else {
                                setInput(input);
                            }
                            dirty = true;
                        }
                        setCookTime(smeltProgress);
                    } else if (smeltProgress > 0) {
                        smeltProgress = Math.max(smeltProgress - 2, 0);
                        setCookTime(smeltProgress);
                    }
                }
            } else if (smeltProgress > 0) {
                smeltProgress = Math.max(smeltProgress - 2, 0);
                setCookTime(smeltProgress);
            }
        } else if (smeltProgress > 0) {
            smeltProgress = Math.max(smeltProgress - 2, 0);
            setCookTime(smeltProgress);
        }

        if (dirty) {
            markDirty();
        }
    }

    @Override
    public float getProgress() {
        int total = getTotalCookTime();
        int current = getCookTime();
        return total > 0 ? (float) current / total : 0.0f;
    }

    @Override
    public float getBurnProgress() {
        int total = getTotalBurnTime();
        int current = getBurnTime();
        return total > 0 ? (float) current / total : 0.0f;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        doSmeltTick();
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        doSmeltTick();
        return false;
    }
}
