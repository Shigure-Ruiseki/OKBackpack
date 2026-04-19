package ruiseki.okbackpack.client.gui.interaction;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.client.renderer.BackpackContentHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.helper.LangHelpers;

public final class BackpackInventoryInteractionTooltipHelpers {

    public static final String RIGHT_CLICK_ADD_KEY = "tooltip.backpack.inventory_interaction.right_click_add";

    private BackpackInventoryInteractionTooltipHelpers() {}

    public static List<String> prependInteractionLine(List<String> lines) {
        List<String> result = new ArrayList<>();
        result.add(LangHelpers.localize(RIGHT_CLICK_ADD_KEY));
        if (lines != null) {
            result.addAll(lines);
        }
        return result;
    }

    public static List<String> buildExpandedTooltipLines(ItemStack backpackStack, BackpackWrapper wrapper) {
        List<String> lines = new ArrayList<>();
        if (backpackStack == null || wrapper == null) {
            return lines;
        }

        if (Mods.CodeChickenCore.isModLoaded()) {
            BackpackContentHandler.prepareContents(wrapper);

            boolean hasUpgrades = !BackpackContentHandler.upgradeInfos.isEmpty();
            boolean hasContents = !BackpackContentHandler.sortedContents.isEmpty();

            if (!hasUpgrades && !hasContents) {
                lines.add("\u00a7e" + LangHelpers.localize("tooltip.backpack.contents.empty"));
            } else {
                double multiplier = wrapper.applyStackLimitModifiers();
                if (multiplier > 1 && multiplier != Integer.MAX_VALUE) {
                    lines.add(
                        "\u00a7a" + LangHelpers
                            .localize("tooltip.backpack.contents.stack_multiplier", String.format("%.1f", multiplier)));
                } else if (multiplier == Integer.MAX_VALUE) {
                    lines.add("\u00a7a" + LangHelpers.localize("tooltip.backpack.contents.stack_multiplier", "\u221E"));
                }

                if (hasUpgrades) {
                    lines.addAll(BackpackContentHandler.upgradeTooltipLines);
                    lines.add("\u00a7e" + LangHelpers.localize("tooltip.backpack.contents.upgrades"));
                    lines.add(BackpackContentHandler.getUpgradeHandlerLine());
                }

                if (hasContents) {
                    lines.add("\u00a7e" + LangHelpers.localize("tooltip.backpack.contents.inventory"));
                    lines.add(BackpackContentHandler.getContentsHandlerLine());
                }
            }
        }

        lines.add(LangHelpers.localize("tooltip.backpack.inventory_size", wrapper.backpackSlots));
        lines.add(LangHelpers.localize("tooltip.backpack.upgrade_slots_size", wrapper.upgradeSlots));
        return lines;
    }

    public static List<String> buildInteractionTooltipLines(ItemStack backpackStack, BackpackWrapper wrapper) {
        List<String> lines = buildExpandedTooltipLines(backpackStack, wrapper);
        List<String> result = new ArrayList<>();
        result.add(LangHelpers.localize(RIGHT_CLICK_ADD_KEY));
        result.addAll(lines);
        return result;
    }
}
