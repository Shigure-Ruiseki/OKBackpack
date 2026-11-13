package ruiseki.backpack.modcompat.thaumcraft.upgrades.energizednode;

import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;
import thaumcraft.api.aspects.Aspect;

public class ItemEnergizedNodeUpgrade extends ItemUpgradeBase<EnergizedNodeUpgradeWrapper> {

    public static final UpgradeType<EnergizedNodeUpgradeWrapper> TYPE = new UpgradeType<>(
        EnergizedNodeUpgradeWrapper::new);
    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemEnergizedNodeUpgrade.class::isInstance,
            0,
            LangHelpers.localize("gui.okbackpack.error.add.energizednode_exists")));

    public ItemEnergizedNodeUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.tooltipBuilder(builder -> {
            builder.addLine("item.items.okbackpack.energized_node_upgrade.tooltip");
            builder.addLine((stack, player, tooltip, advanced) -> {
                if (!EnergizedNodeUpgradeWrapper.hasAspect(stack)) {
                    tooltip.add(
                        EnumChatFormatting.DARK_GRAY
                            + LangHelpers.localize("item.items.okbackpack.energized_node_upgrade.empty"));
                    return;
                }

                Map<Aspect, Integer> aspectRates = EnergizedNodeUpgradeWrapper.getStoredAspectRates(stack);
                if (GuiScreen.isShiftKeyDown()) {
                    for (Map.Entry<Aspect, Integer> entry : aspectRates.entrySet()) {
                        Aspect aspect = entry.getKey();
                        if (aspect == null) continue;
                        tooltip.add(
                            " §" + aspect.getChatcolor() + aspect.getName() + "§r x " + entry.getValue() + " cv/t");
                    }
                } else {
                    tooltip.add(buildCompactAspectTooltip(aspectRates));
                }
            });
        });
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

            builder.append('§')
                .append(aspect.getChatcolor())
                .append(entry.getValue())
                .append("§r");
            first = false;
        }

        return builder.toString();
    }

    @Override
    public UpgradeType<EnergizedNodeUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }
}
