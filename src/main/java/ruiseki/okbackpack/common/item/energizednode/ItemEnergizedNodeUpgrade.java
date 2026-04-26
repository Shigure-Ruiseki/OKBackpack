package ruiseki.okbackpack.common.item.energizednode;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.EnergizedNodeUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftHelpers;
import ruiseki.okcore.helper.LangHelpers;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileNodeEnergized;

public class ItemEnergizedNodeUpgrade extends ItemUpgrade<EnergizedNodeUpgradeWrapper> {

    public ItemEnergizedNodeUpgrade() {
        super("energized_node_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "energized_node_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public boolean hasSlotWidget() {
        return false;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.energized_node_upgrade"));

        if (!EnergizedNodeUpgradeWrapper.isConfigured(itemstack)) {
            list.add(
                EnumChatFormatting.DARK_GRAY + LangHelpers.localize("tooltip.backpack.energized_node_upgrade.empty"));
            return;
        }

        Map<Aspect, Integer> aspectRates = EnergizedNodeUpgradeWrapper.getStoredAspectRates(itemstack);
        if (GuiScreen.isShiftKeyDown()) {
            for (Map.Entry<Aspect, Integer> entry : aspectRates.entrySet()) {
                Aspect aspect = entry.getKey();
                if (aspect == null) continue;

                list.add(
                    " \u00a7" + aspect.getChatcolor() + aspect.getName() + "\u00a7r x " + entry.getValue() + " cv/t");
            }
            return;
        }

        list.add(buildCompactAspectTooltip(aspectRates));
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (stack == null || EnergizedNodeUpgradeWrapper.isConfigured(stack)) return false;

        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileNodeEnergized energizedNode)) return false;

        EnergizedNodeUpgradeWrapper
            .setStoredAspectRates(stack, ThaumcraftHelpers.getEnergizedNodeAspectValues(energizedNode));
        ThaumcraftHelpers.resetNodeConverterAbove(world, x, y, z);
        world.setBlockToAir(x, y, z);

        if (player instanceof EntityPlayerMP playerMP) {
            playerMP.updateHeldItem();
            playerMP.inventoryContainer.detectAndSendChanges();
        }
        return true;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemEnergizedNodeUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult.failOnlySingleAllowed(
                conflicts,
                LangHelpers.localize("item.energized_node_upgrade.name"),
                wrapper.getDisplayName());
        }
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public EnergizedNodeUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new EnergizedNodeUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(EnergizedNodeUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        // This upgrade no longer exposes any widget slots.
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, EnergizedNodeUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new EnergizedNodeUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }

    private String buildCompactAspectTooltip(Map<Aspect, Integer> aspectRates) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (Map.Entry<Aspect, Integer> entry : aspectRates.entrySet()) {
            Aspect aspect = entry.getKey();
            if (aspect == null) continue;

            if (!first) {
                builder.append(" | ");
            }

            builder.append('\u00a7')
                .append(aspect.getChatcolor())
                .append(entry.getValue())
                .append("\u00a7r");
            first = false;
        }

        return builder.toString();
    }
}
