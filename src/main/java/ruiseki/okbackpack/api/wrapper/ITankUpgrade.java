package ruiseki.okbackpack.api.wrapper;

import net.minecraftforge.fluids.FluidStack;

import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;

public interface ITankUpgrade extends IStorageUpgrade, ITickable {

    FluidStack getContents();

    int getTankCapacity();

    int fill(FluidStack resource, boolean doFill);

    FluidStack drain(int maxDrain, boolean doDrain);

    float getFillRatio();

    BaseItemStackHandler getStorage();

    void interactWithCursorStack(net.minecraft.entity.player.EntityPlayer player);
}
