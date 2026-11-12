package ruiseki.backpack.backpack;

import java.util.Objects;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.Delegate;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.block.BlockSleepingBagConfig;
import ruiseki.okcore.capabilities.resolver.BasicCapabilityResolver;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.energy.capability.CapabilityEnergy;
import ruiseki.okcore.energy.capability.EmptyEnergyStorage;
import ruiseki.okcore.energy.component.EnergyHandlerComponent;
import ruiseki.okcore.energy.component.IEnergyHandlerExclusion;
import ruiseki.okcore.fluid.capability.CapabilityFluidHandler;
import ruiseki.okcore.fluid.capability.EmptyFluidHandler;
import ruiseki.okcore.fluid.component.FluidHandlerComponent;
import ruiseki.okcore.fluid.component.IFluidHandlerExclusion;
import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.component.IInventoryExclusion;
import ruiseki.okcore.inventory.component.SidedInventoryComponent;
import ruiseki.okcore.item.IItemSharedTag;
import ruiseki.okcore.item.capability.CapabilityItemHandler;
import ruiseki.okcore.persist.nbt.NBTPersist;
import ruiseki.okcore.tileentity.TileTicking;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;

public class TileBackpack extends TileTicking
    implements IGuiHolder<PosGuiData>, ISidedInventory, IEnergyHandler, net.minecraftforge.fluids.IFluidHandler {

    private IBackpackWrapper backpackWrapper = IBackpackWrapper.NoopBackpackWrapper.INSTANCE;

    @Delegate(excludes = IInventoryExclusion.class)
    private final SidedInventoryComponent inventory = new SidedInventoryComponent(this) {

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return IntStream.range(0, getSizeInventory())
                .toArray();
        }
    };

    @Delegate(excludes = IFluidHandlerExclusion.class)
    private final FluidHandlerComponent tank = new FluidHandlerComponent(this);

    @Delegate(excludes = IEnergyHandlerExclusion.class)
    private final EnergyHandlerComponent energyStorage = new EnergyHandlerComponent(this);

    @NBTPersist
    private ForgeDirection facing = ForgeDirection.NORTH;

    @NBTPersist
    private boolean sleepingBagDeployed;
    @NBTPersist
    private BlockPos sleepingBagPos;

    public TileBackpack() {
        this.capabilityCache.addCapabilityResolver(
            BasicCapabilityResolver.create(CapabilityBackpackWrapperConfig.CAPABILITY, this::getBackpackWrapper));

        this.capabilityCache.addCapabilityResolver(
            BasicCapabilityResolver.create(
                CapabilityItemHandler.ITEM_HANDLER,
                () -> this.getBackpackWrapper()
                    .getInventoryForInputOutput()));

        this.capabilityCache.addCapabilityResolver(
            BasicCapabilityResolver.create(
                CapabilityFluidHandler.FLUID_HANDLER,
                () -> this.getBackpackWrapper()
                    .getFluidHandler()
                    .map(IFluidHandler.class::cast)
                    .orElse(EmptyFluidHandler.INSTANCE)));

        this.capabilityCache.addCapabilityResolver(
            BasicCapabilityResolver.create(
                CapabilityEnergy.ENERGY,
                () -> this.getBackpackWrapper()
                    .getEnergyStorage()
                    .orElse(EmptyEnergyStorage.INSTANCE)));
    }

    public void setBackpack(ItemStack stack) {
        this.backpackWrapper = CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .orElse(IBackpackWrapper.NoopBackpackWrapper.INSTANCE);
        this.backpackWrapper.setSaveHandler(() -> {
            markDirty();
            onSendUpdate();
        });
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (this.backpackWrapper != null && !this.getWorldObj().isRemote) {
            this.backpackWrapper.getUpgradeHandler()
                .getWrappersThatImplement(ITickableUpgrade.class)
                .forEach(upgrade -> upgrade.tick(null, this.getWorldObj(), this.getPos()));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.setBackpackFromNbt(tag);
    }

    private void setBackpackFromNbt(NBTTagCompound nbt) {
        setBackpack(ItemHelpers.readFromNBT(nbt.getCompoundTag("backpackData")));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        this.writeBackpack(tag);
    }

    private void writeBackpack(NBTTagCompound ret) {
        ItemStack backpackCopy = this.backpackWrapper.getBackpack()
            .copy();
        backpackCopy.setTagCompound(
            ((IItemSharedTag) Objects.requireNonNull(backpackCopy.getItem())).getNBTShareTag(backpackCopy));
        ret.setTag("backpackData", ItemHelpers.writeToNBT(backpackCopy, new NBTTagCompound()));
    }

    @Override
    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound ret = super.getNBTTagCompound();
        writeBackpack(ret);
        return ret;
    }

    public IBackpackWrapper getBackpackWrapper() {
        return backpackWrapper;
    }

    public boolean onBlockActivated(World world, EntityPlayer player, ForgeDirection side, float hitX, float hitY,
        float hitZ) {
        if (!worldObj.isRemote) {
            GuiFactories.tileEntity()
                .open(player, xCoord, yCoord, zCoord);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new BackpackGuiHolder.TileEntityGuiHolder().createScreen(data, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new BackpackGuiHolder.TileEntityGuiHolder().buildUI(data, syncManager, settings);
    }

    public ForgeDirection getFacing() {
        return this.facing;
    }

    public void setFacing(ForgeDirection facing) {
        this.facing = facing;
        this.markDirty();
        this.onSendUpdate();
    }

    public int getMainColor() {
        return this.backpackWrapper.getMainColor();
    }

    public int getAccentColor() {
        return this.backpackWrapper.getAccentColor();
    }

    public void setSleepingBagDeployed(boolean state) {
        this.sleepingBagDeployed = state;
        markDirty();
        onSendUpdate();
    }

    public boolean isSleepingBagDeployed() {
        return sleepingBagDeployed;
    }

    public boolean deploySleepingBag(EntityPlayer player, World world, BlockPos pos, int meta) {
        if (world.isRemote) return false;
        this.sleepingBagDeployed = BlockSleepingBag.spawnSleepingBag(player, world, meta, pos);
        if (this.sleepingBagDeployed) {
            this.sleepingBagPos = pos;
            this.markDirty();
            this.onSendUpdate();
        }
        return this.sleepingBagDeployed;
    }

    public void removeSleepingBag(World world) {
        if (this.sleepingBagDeployed && this.sleepingBagPos != null) {
            BlockPos pos = this.sleepingBagPos;
            if (world.blockExists(pos.getX(), pos.getY(), pos.getZ())
                && world.getBlock(pos.getX(), pos.getY(), pos.getZ())
                    == BlockSleepingBagConfig._instance.getInstance()) {
                world.func_147480_a(pos.getX(), pos.getY(), pos.getZ(), false);
            }
        }
        this.sleepingBagDeployed = false;
        this.sleepingBagPos = null;
        this.markDirty();
        this.onSendUpdate();
    }
}
