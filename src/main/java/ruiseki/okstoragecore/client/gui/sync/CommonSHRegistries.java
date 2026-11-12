package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okstoragecore.Reference;
import ruiseki.okstoragecore.client.gui.slot.CraftingSlotInfo;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidStackSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedValueSH;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IFluidFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.anvil.AnvilContainer;
import ruiseki.okstoragecore.upgrades.anvil.AnvilUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.battery.BatteryUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.crafting.CraftingContainer;
import ruiseki.okstoragecore.upgrades.tank.TankUpgradeWrapper;

public class CommonSHRegistries {

    public static final StorageSHRegistry.Entry<DelegatedFluidHandlerSH> FLUID_REGISTRY = StorageSHRegistry
        .get(DelegatedFluidHandlerSH.class);
    public static final StorageSHRegistry.Entry<DelegatedItemHandlerSH> ITEM_REGISTRY = StorageSHRegistry
        .get(DelegatedItemHandlerSH.class);
    public static final StorageSHRegistry.Entry<DelegatedValueSH> VALUE_REGISTRY = StorageSHRegistry
        .get(DelegatedValueSH.class);
    public static final StorageSHRegistry.Entry<UpgradeSlotSH> UPGRADE_REGISTRY = StorageSHRegistry
        .get(UpgradeSlotSH.class);

    public static final ResourceLocation STORAGE = new ResourceLocation(Reference.MOD_ID, "storage_storage");
    public static final ResourceLocation FILTER = new ResourceLocation(Reference.MOD_ID, "storage_filter");
    public static final ResourceLocation FLUID_FILTER = new ResourceLocation(Reference.MOD_ID, "storage_filter");

    public static final ResourceLocation ANVIL_CONTAINER = new ResourceLocation(
        Reference.MOD_ID,
        "storage_anvil_container");
    public static final ResourceLocation ANVIL_NAME = new ResourceLocation(Reference.MOD_ID, "storage_anvil_name");

    public static final ResourceLocation CRAFTING_CONTAINER = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_container");
    public static final ResourceLocation CRAFTING_INFO = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_info");

    public static final ResourceLocation TANK_FLUID = new ResourceLocation(Reference.MOD_ID, "storage_tank_fluid");
    public static final ResourceLocation TANK_CAPACITY = new ResourceLocation(
        Reference.MOD_ID,
        "storage_tank_capacity");

    public static final ResourceLocation BATTERY_ENERGY = new ResourceLocation(
        Reference.MOD_ID,
        "storage_battery_energy");
    public static final ResourceLocation BATTERY_CAPACITY = new ResourceLocation(
        Reference.MOD_ID,
        "storage_battery_capacity");

    public static void load() {
        ITEM_REGISTRY.registerServer(STORAGE, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof IStorageUpgrade upgradeWrapper)) return;
            handler.setItemHandler(upgradeWrapper::getStorage);
        });
        ITEM_REGISTRY.registerServer(FILTER, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            ((DelegatedFilterLogicSH) handler).setFilterLogic(upgradeWrapper::getFilterLogic);
        });

        FLUID_REGISTRY.registerServer(FLUID_FILTER, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof IFluidFilteredUpgrade upgradeWrapper)) return;
            ((DelegatedFluidFilterLogicSH) handler).setFilterLogic(upgradeWrapper::getFilterLogic);
        });

        UPGRADE_REGISTRY.registerServer(ANVIL_CONTAINER, (handler, buf) -> {
            UpgradeSlotUpdate slotUpdate = handler.getUpgradeUpdate();
            DelegatedItemHandlerSH handlerSH = slotUpdate.get("anvil_handler");
            AnvilContainer container = new AnvilContainer(
                slotUpdate.getWrapper(),
                slotUpdate.getPanel(),
                handlerSH.getContainerSupplier(),
                slotUpdate.getPlayer(),
                slotUpdate.getUpgradeIndex(),
                handlerSH.getItemHandler());
            slotUpdate.setUpgradeContainer(container);
        });
        VALUE_REGISTRY.registerServer(ANVIL_NAME, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof AnvilUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedStringSH) handler).setDelegated(upgradeWrapper::getItemName, s -> {
                upgradeWrapper.setItemName(s);
                ((AnvilContainer) handler.getUpgradeUpdate()
                    .getUpgradeContainer()).updateRepairOutput();
            });
        });

        UPGRADE_REGISTRY.registerServer(CRAFTING_CONTAINER, (handler, buf) -> {
            UpgradeSlotUpdate slotUpdate = handler.getUpgradeUpdate();
            DelegatedItemHandlerSH handlerSH = slotUpdate.get("crafting_handler");
            CraftingContainer container = new CraftingContainer(
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
        UPGRADE_REGISTRY.registerServer(CRAFTING_INFO, (handler, buf) -> {
            UpgradeSlotUpdate slotUpdate = handler.getUpgradeUpdate();
            IndexedModularSlot[] matrix = slotUpdate.get("crafting_matrix_slots");
            ModularCraftingSlot result = slotUpdate.get("crafting_result_slot");
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

        VALUE_REGISTRY.registerServer(TANK_CAPACITY, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof TankUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedIntSH) handler).setDelegated(upgradeWrapper::getTankCapacity);
        });
        VALUE_REGISTRY.registerServer(TANK_FLUID, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof TankUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedFluidStackSH) handler).setDelegated(upgradeWrapper::getContents);
        });

        VALUE_REGISTRY.registerServer(BATTERY_CAPACITY, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof BatteryUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedIntSH) handler).setDelegated(upgradeWrapper::getMaxEnergyStored);
        });
        VALUE_REGISTRY.registerServer(BATTERY_ENERGY, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof BatteryUpgradeWrapper upgradeWrapper)) return;
            ((DelegatedIntSH) handler).setDelegated(upgradeWrapper::getEnergyStored);
        });
    }
}
