package ruiseki.backpack.backpack;

import java.util.Objects;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
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

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okcore.capabilities.resolver.BasicCapabilityResolver;
import ruiseki.okcore.fluid.capability.CapabilityFluidHandler;
import ruiseki.okcore.fluid.capability.EmptyFluidHandler;
import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.item.IItemSharedTag;
import ruiseki.okcore.item.capability.CapabilityItemHandler;
import ruiseki.okcore.tileentity.TileEntityOK;

public class TileBackpack extends TileEntityOK implements IGuiHolder<PosGuiData> {

    private IBackpackWrapper backpackWrapper = IBackpackWrapper.Noop.INSTANCE;

    public TileBackpack() {
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
    }

    public void setBackpack(ItemStack stack) {
        this.backpackWrapper = CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .orElse(IBackpackWrapper.Noop.INSTANCE);
        this.backpackWrapper.setSaveHandler(() -> {
            markDirty();
            onSendUpdate();
        });
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.setBackpackFromNbt(tag);
    }

    private void setBackpackFromNbt(NBTTagCompound nbt) {
        setBackpack(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("backpackData")));
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
        ret.setTag("backpackData", backpackCopy.writeToNBT(new NBTTagCompound()));
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
        return new BackpackGuiHolder.TileEntityGuiHolder<>(backpackWrapper).createScreen(data, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new BackpackGuiHolder.TileEntityGuiHolder<>(backpackWrapper).buildUI(data, syncManager, settings);
    }
}
