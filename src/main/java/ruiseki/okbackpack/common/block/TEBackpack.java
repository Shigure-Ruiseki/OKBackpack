package ruiseki.okbackpack.common.block;

import java.util.Collection;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cpw.mods.fml.common.Optional;
import lombok.experimental.Delegate;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.wrapper.IBatteryUpgrade;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftHelpers;
import ruiseki.okcore.energy.IOKEnergyIO;
import ruiseki.okcore.persist.nbt.NBTPersist;
import ruiseki.okcore.tileentity.TileEntityOK;
import ruiseki.okcore.tileentity.TileSideCapability;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

@Optional.Interface(iface = "thaumcraft.api.aspects.IAspectContainer", modid = "Thaumcraft")
public class TEBackpack extends TileSideCapability implements ISidedInventory, IGuiHolder<SidedPosGuiData>,
    TileEntityOK.ITickingTile, IOKEnergyIO, IFluidHandler, IAspectContainer {

    private int[] allSlots;

    @NBTPersist
    private ForgeDirection facing = ForgeDirection.NORTH;

    @NBTPersist(BackpackWrapper.BACKPACK_NBT)
    private BackpackWrapper wrapper;

    @NBTPersist
    private boolean sleepingBagDeployed;
    @NBTPersist
    private int sbx;
    @NBTPersist
    private int sby;
    @NBTPersist
    private int sbz;

    @Delegate
    protected final TileEntityOK.ITickingTile tickingTileComponent = new TileEntityOK.TickingTileComponent(this);

    public TEBackpack() {
        wrapper = new BackpackWrapper();
        this.wrapper.setInventorySlotChangeHandler(new Runnable() {

            @Override
            public void run() {
                markDirty();
            }
        });
        allSlots = new int[wrapper.getSlots()];
        for (int i = 0; i < allSlots.length; i++) {
            allSlots[i] = i;
        }
    }

    public void setWrapper(BackpackWrapper wrapper) {
        this.wrapper = wrapper;
        this.wrapper.setInventorySlotChangeHandler(new Runnable() {

            @Override
            public void run() {
                markDirty();
            }
        });
        allSlots = new int[wrapper.getSlots()];
        for (int i = 0; i < allSlots.length; i++) {
            allSlots[i] = i;
        }
    }

    public BackpackWrapper getWrapper() {
        return wrapper;
    }

    public boolean onBlockActivated(World world, EntityPlayer player, ForgeDirection side, float hitX, float hitY,
        float hitZ) {
        if (wrapper.canPlayerAccess(player.getUniqueID())) {
            if (!worldObj.isRemote) {
                GuiFactories.sidedTileEntity()
                    .open(player, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN);
            }
        }
        return true;
    }

    @Override
    protected void doUpdate() {
        super.doUpdate();

        if (wrapper.tick(worldObj, getPos())) {
            markDirty();
        }
    }

    @Override
    public ModularScreen createScreen(SidedPosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Reference.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new BackpackGuiHolder.TileEntityGuiHolder(wrapper).buildUI(data, syncManager, settings);
    }

    public ForgeDirection getFacing() {
        return facing;
    }

    public void setFacing(ForgeDirection facing) {
        this.facing = facing;
        markDirty();
        onSendUpdate();
    }

    @Override
    public void invalidate() {
        if (worldObj != null && !worldObj.isRemote) {
            wrapper.forceStopAllJukeboxes(worldObj, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f);
        }
        super.invalidate();
    }

    @Override
    public void onChunkLoad() {
        super.onChunkLoad();
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return allSlots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        if (slot < 0 || slot >= getSizeInventory()) {
            return false;
        }
        if (!wrapper.canInsert(slot, stack)) {
            return false;
        }
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        if (slot < 0 || slot >= getSizeInventory()) {
            return false;
        }
        ItemStack existing = wrapper.getStackInSlot(slot);
        if (existing == null || existing.stackSize < stack.stackSize) {
            return false;
        }
        if (!wrapper.canExtract(slot, stack)) {
            return false;
        }
        return stack.getItem() == existing.getItem();
    }

    @Override
    public int getSizeInventory() {
        return wrapper.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= getSizeInventory()) {
            return null;
        }
        return wrapper.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= getSizeInventory()) {
            return null;
        }
        ItemStack fromStack = wrapper.getStackInSlot(slot);
        if (fromStack == null) {
            return null;
        }
        if (fromStack.stackSize <= amount) {
            wrapper.setStackInSlot(slot, null);
            return fromStack;
        }
        ItemStack result = fromStack.splitStack(amount);
        wrapper.setStackInSlot(slot, fromStack.stackSize > 0 ? fromStack : null);
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getSizeInventory()) {
            return;
        }

        if (stack == null) {
            wrapper.setStackInSlot(slot, null);
        }

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        wrapper.setStackInSlot(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return wrapper.getDisplayName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return wrapper.hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        double mod = wrapper.applySlotLimitModifiers();
        double raw = 64.0 * mod;
        if (raw >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) Math.ceil(raw);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canInteractWith(player);
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    public int getMainColor() {
        return wrapper.getMainColor();
    }

    public int getAccentColor() {
        return wrapper.getAccentColor();
    }

    public boolean isSleepingBagDeployed() {
        return this.sleepingBagDeployed;
    }

    public void setSleepingBagDeployed(boolean state) {
        this.sleepingBagDeployed = state;
        markDirty();
        onSendUpdate();
    }

    public boolean deploySleepingBag(EntityPlayer player, World world, int meta, int cX, int cY, int cZ) {
        if (world.isRemote) return false;

        sleepingBagDeployed = BlockSleepingBag.spawnSleepingBag(player, world, meta, cX, cY, cZ);
        if (sleepingBagDeployed) {
            sbx = cX;
            sby = cY;
            sbz = cZ;
            markDirty();
            onSendUpdate();
        }
        return sleepingBagDeployed;
    }

    public void removeSleepingBag(World world) {
        if (sleepingBagDeployed && world.getBlock(sbx, sby, sbz) == ModBlocks.SLEEPING_BAG.getBlock())
            world.func_147480_a(sbx, sby, sbz, false);

        sleepingBagDeployed = false;
        markDirty();
    }

    private IBatteryUpgrade getBatteryUpgrade() {
        Map<Integer, IBatteryUpgrade> batteries = wrapper.gatherCapabilityUpgrades(IBatteryUpgrade.class);
        if (batteries.isEmpty()) return null;
        return batteries.values()
            .iterator()
            .next();
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        IBatteryUpgrade battery = getBatteryUpgrade();
        if (battery == null) return 0;
        int received = battery.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            markDirty();
        }
        return received;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        IBatteryUpgrade battery = getBatteryUpgrade();
        if (battery == null) return 0;
        int extracted = battery.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            markDirty();
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        IBatteryUpgrade battery = getBatteryUpgrade();
        return battery != null ? battery.getEnergyStored() : 0;
    }

    @Override
    public int getMaxEnergyStored() {
        IBatteryUpgrade battery = getBatteryUpgrade();
        return battery != null ? battery.getMaxEnergyStored() : 0;
    }

    @Override
    public void setEnergyStored(int energy) {
        // Energy is managed by the battery upgrade internally
    }

    @Override
    public int getEnergyTransfer() {
        IBatteryUpgrade battery = getBatteryUpgrade();
        return battery != null ? battery.getMaxTransfer() : 0;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return getBatteryUpgrade() != null;
    }

    private ITankUpgrade getTankForFill(Fluid fluid) {
        ITankUpgrade emptyTank = null;
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values()) {
            FluidStack contents = tank.getContents();
            if (contents != null && contents.getFluid() == fluid) {
                if (contents.amount < tank.getTankCapacity()) return tank;
            } else if (contents == null && emptyTank == null) {
                emptyTank = tank;
            }
        }
        return emptyTank;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) return 0;
        ITankUpgrade tank = getTankForFill(resource.getFluid());
        if (tank == null) return 0;
        int filled = tank.fill(resource, doFill);
        if (doFill && filled > 0) markDirty();
        return filled;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) return null;
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values()) {
            FluidStack contents = tank.getContents();
            if (contents != null && contents.getFluid() == resource.getFluid()) {
                FluidStack drained = tank.drain(resource.amount, doDrain);
                if (drained != null && doDrain) markDirty();
                return drained;
            }
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) return null;
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values()) {
            FluidStack contents = tank.getContents();
            if (contents != null && contents.amount > 0) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && doDrain) markDirty();
                return drained;
            }
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        if (fluid == null) return false;
        return getTankForFill(fluid) != null;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values()) {
            FluidStack contents = tank.getContents();
            if (contents != null && (fluid == null || contents.getFluid() == fluid)) return true;
        }
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        Collection<ITankUpgrade> tanks = wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values();
        FluidTankInfo[] info = new FluidTankInfo[tanks.size()];
        int i = 0;
        for (ITankUpgrade tank : tanks) {
            info[i++] = new FluidTankInfo(tank.getContents(), tank.getTankCapacity());
        }
        return info;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public AspectList getAspects() {
        return ThaumcraftHelpers.getWandAspects(wrapper);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public void setAspects(AspectList aspects) {}

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean doesContainerAccept(Aspect aspect) {
        return Mods.Thaumcraft.isLoaded() && ThaumcraftHelpers.doesWandAcceptAspect(wrapper, aspect);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int addToContainer(Aspect aspect, int amount) {
        int leftover = ThaumcraftHelpers.addAspectToWands(wrapper, aspect, amount);
        if (leftover < amount) markDirty();
        return leftover;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    @Deprecated
    @Optional.Method(modid = "Thaumcraft")
    public boolean takeFromContainer(AspectList aspects) {
        return false;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return ThaumcraftHelpers.getWandAspectAmount(wrapper, aspect) >= amount;
    }

    @Override
    @Deprecated
    @Optional.Method(modid = "Thaumcraft")
    public boolean doesContainerContain(AspectList aspects) {
        if (aspects == null) return false;
        for (Aspect a : aspects.getAspects()) {
            if (a != null && !doesContainerContainAmount(a, aspects.getAmount(a))) return false;
        }
        return true;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int containerContains(Aspect aspect) {
        return ThaumcraftHelpers.getWandAspectAmount(wrapper, aspect);
    }
}
