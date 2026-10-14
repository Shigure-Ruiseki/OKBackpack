package ruiseki.backpack.backpack;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ruiseki.backpack.Capabilities;
import ruiseki.backpack.backpack.wrapper.BackpackWrapper;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okbackpack.client.gui.container.BackpackModularScreen;
import ruiseki.okbackpack.common.init.TierRegistries;
import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.item.ItemBlockMetadata;

public class ItemBackpack extends ItemBlockMetadata implements IGuiHolder<PlayerInventoryGuiData> {

    private String tierId = TierRegistries.LEATHER;

    public ItemBackpack(Block block) {
        super(block);
        if (block instanceof BlockBackpack backpack) {
            this.tierId = backpack.getTierId();
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && stack != null) {
            GuiFactories.playerInventory()
                .openFromMainHand(player);
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean flag) {
        super.addInformation(stack, player, list, flag);
        if (stack.hasTagCompound()) {
            list.add(
                stack.getTagCompound()
                    .toString());
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {

            private IBackpackWrapper wrapper = null;

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                @Nullable ForgeDirection forgeDirection) {
                initCap();

                if (capability == Capabilities.BACKPACK_WRAPPER) {
                    return LazyOptional.of(() -> wrapper)
                        .cast();
                }
                return LazyOptional.empty();
            }

            private void initCap() {
                if (this.wrapper == null) {
                    this.wrapper = new BackpackWrapper(stack, tierId);
                    if (this.wrapper.getContentsUuid()
                        .isEmpty()) {
                        this.wrapper.getOrCreateContentsUuid();
                    }
                }
            }
        };
    }

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new BackpackModularScreen(mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IBackpackWrapper wrapper = CapabilityHelpers
            .getCapability(data.getUsedItemStack(), Capabilities.BACKPACK_WRAPPER)
            .orElse(IBackpackWrapper.Noop.INSTANCE);
        return new BackpackGuiHolder.ItemStackGuiHolder(wrapper).buildUI(data, syncManager, settings);
    }
}
