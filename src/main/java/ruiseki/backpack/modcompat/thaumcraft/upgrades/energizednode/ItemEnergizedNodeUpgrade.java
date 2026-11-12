package ruiseki.backpack.modcompat.thaumcraft.upgrades.energizednode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;
import thaumcraft.api.aspects.Aspect;

public class ItemEnergizedNodeUpgrade extends ItemUpgradeBase<EnergizedNodeUpgradeWrapper> {

    public static final UpgradeType<EnergizedNodeUpgradeWrapper> TYPE = new UpgradeType<>(
        EnergizedNodeUpgradeWrapper::new);

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
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack) {
        UpgradeSlotChangeResult result = super.canAddUpgradeTo(storageWrapper, upgradeStack);
        if (!result.isSuccessful()) return result;

        Set<Integer> errorUpgradeSlots = new HashSet<>();
        this.findUpgrades(errorUpgradeSlots, storageWrapper, upgrade -> upgrade instanceof EnergizedNodeUpgradeWrapper);

        if (!errorUpgradeSlots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.energizednode_exists"),
                errorUpgradeSlots,
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }
}
