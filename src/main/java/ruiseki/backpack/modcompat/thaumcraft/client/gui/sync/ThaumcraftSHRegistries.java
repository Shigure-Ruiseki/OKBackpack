package ruiseki.backpack.modcompat.thaumcraft.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane.ArcaneContainer;
import ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane.ArcaneCraftingUpgradeWrapper;
import ruiseki.okstoragecore.Reference;
import ruiseki.okstoragecore.client.gui.slot.CraftingSlotInfo;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedValueSH;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class ThaumcraftSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> UPGRADE_REGISTRY = StorageSHRegistry
        .get(UpgradeSlotSH.class);
    public static final StorageSHRegistry.Entry<DelegatedValueSH> VALUE_REGISTRY = StorageSHRegistry
        .get(DelegatedValueSH.class);

    public static final ResourceLocation ARCANE_CONTAINER = new ResourceLocation(
        Reference.MOD_ID,
        "thaumcraft_arcane_container");
    public static final ResourceLocation ARCANE_INFO = new ResourceLocation(Reference.MOD_ID, "thaumcraft_arcane_info");
    public static final ResourceLocation HAS_WAND = new ResourceLocation(
        Reference.MOD_ID,
        "thaumcraft_arcane_has_wand");
    public static final ResourceLocation MISSING_RESEARCH = new ResourceLocation(
        Reference.MOD_ID,
        "thaumcraft_arcane_missing_research");
    public static final ResourceLocation MISSING_RESEARCH_NAME = new ResourceLocation(
        Reference.MOD_ID,
        "thaumcraft_arcane_missing_research_name");

    public static void load() {

        UPGRADE_REGISTRY.registerServer(ARCANE_CONTAINER, (handler, buf) -> {
            UpgradeSlotUpdate slotUpdate = handler.getUpgradeUpdate();
            DelegatedItemHandlerSH handlerSH = slotUpdate.get("arcane_handler");
            ArcaneContainer container = new ArcaneContainer(
                slotUpdate.getWrapper(),
                slotUpdate.getPanel(),
                handlerSH.getContainerSupplier(),
                slotUpdate.getPlayer(),
                slotUpdate.getUpgradeIndex(),
                3,
                3,
                handlerSH.getItemHandler());
            slotUpdate.setUpgradeContainer(container);
        });
        UPGRADE_REGISTRY.registerServer(ARCANE_INFO, (handler, buf) -> {
            UpgradeSlotUpdate slotUpdate = handler.getUpgradeUpdate();
            IndexedModularSlot[] matrix = slotUpdate.get("arcane_matrix_slots");
            ModularCraftingSlot result = slotUpdate.get("arcane_result_slot");
            CraftingSlotInfo slotInfo = new CraftingSlotInfo(
                slotUpdate.getUpgradeIndex(),
                matrix,
                result,
                3,
                3,
                25,
                6,
                18,
                18);
            slotUpdate.setCraftingInfo(slotInfo);
        });

        VALUE_REGISTRY.registerServer(HAS_WAND, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof ArcaneCraftingUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedBooleanSH) handler).setDelegated(upgradeWrapper::hasWand);
        });
        VALUE_REGISTRY.registerServer(MISSING_RESEARCH, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof ArcaneCraftingUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedStringSH) handler).setDelegated(upgradeWrapper::getMissingResearch);
        });
        VALUE_REGISTRY.registerServer(MISSING_RESEARCH_NAME, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof ArcaneCraftingUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedStringSH) handler).setDelegated(upgradeWrapper::getMissingResearchName);
        });
    }
}
