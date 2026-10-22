package ruiseki.backpack.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.tileentity.TileEntityOK;

public class TileBackpack extends TileEntityOK implements IGuiHolder<PosGuiData> {

    private IBackpackWrapper backpackWrapper = IBackpackWrapper.Noop.INSTANCE;

    public TileBackpack() {

    }

    public void setBackpack(ItemStack stack) {
        backpackWrapper = CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .orElse(IBackpackWrapper.Noop.INSTANCE);
        backpackWrapper.setSaveHandler(() -> {
            markDirty();
            onSendUpdate();
        });
    }

    public boolean onBlockActivated(World world, EntityPlayer player, ForgeDirection side, float hitX, float hitY,
        float hitZ) {
        if (!worldObj.isRemote) {
            GuiFactories.tileEntity()
                .open(player, xCoord, yCoord, zCoord);
        }
        return true;
    }

    public IBackpackWrapper getWrapper() {
        return backpackWrapper;
    }

    @Override
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new BackpackGuiHolder.TileEntityGuiHolder<>(backpackWrapper).createScreen(data, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new BackpackGuiHolder.TileEntityGuiHolder<>(backpackWrapper).buildUI(data, syncManager, settings);
    }
}
