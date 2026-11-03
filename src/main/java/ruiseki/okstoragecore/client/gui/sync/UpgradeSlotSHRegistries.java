package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.helper.StorageInventoryHelpers;
import ruiseki.okstoragecore.upgrades.ContentsFilterType;
import ruiseki.okstoragecore.upgrades.IContentsFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.PrimaryMatch;
import ruiseki.okstoragecore.upgrades.crafting.CraftingUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.feeding.FeedingUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.feeding.HungerLevel;
import ruiseki.okstoragecore.upgrades.magnet.MagnetUpgradeWrapper;

public class UpgradeSlotSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> REGISTRY = StorageSHRegistry.get(UpgradeSlotSH.class);

    public static final ResourceLocation ENABLE = new ResourceLocation(Reference.MOD_ID, "storage_enable");
    public static final ResourceLocation ALLOW_LIST = new ResourceLocation(Reference.MOD_ID, "storage_allow_list");
    public static final ResourceLocation PRIMAL_MATCH = new ResourceLocation(Reference.MOD_ID, "storage_primal_match");
    public static final ResourceLocation MATCH_DURABILITY = new ResourceLocation(
        Reference.MOD_ID,
        "storage_match_durability");
    public static final ResourceLocation MATCH_NBT = new ResourceLocation(Reference.MOD_ID, "storage_match_nbt");
    public static final ResourceLocation ORE_DICT_ADD = new ResourceLocation(Reference.MOD_ID, "storage_oredict_add");
    public static final ResourceLocation ORE_DICT_REMOVE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_oredict_remove");
    public static final ResourceLocation ORE_DICT_MATCH_TYPE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_oredict_match_type");
    public static final ResourceLocation CONTENTS_FILTER = new ResourceLocation(
        Reference.MOD_ID,
        "storage_contents_filter");

    public static final ResourceLocation FILL_CRAFTING = new ResourceLocation(
        Reference.MOD_ID,
        "storage_fill_crafting");
    public static final ResourceLocation SHIFT_INTO_STORAGE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_shift_into_storage");
    public static final ResourceLocation CRAFTING_ROTATED = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_rotated");
    public static final ResourceLocation CRAFTING_BALANCE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_balance");
    public static final ResourceLocation CRAFTING_SPREAD = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_spread");
    public static final ResourceLocation CRAFTING_CLEAR = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_clear");

    public static final ResourceLocation FEED_AT_HUNGER_LEVEL = new ResourceLocation(
        Reference.MOD_ID,
        "storage_feed_at_hunger_level");
    public static final ResourceLocation FEED_WHEN_HURT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_feed_when_hurt");

    public static final ResourceLocation PICKUP_XP = new ResourceLocation(Reference.MOD_ID, "storage_pickup_xp");
    public static final ResourceLocation PICKUP_ITEMS = new ResourceLocation(Reference.MOD_ID, "storage_pickup_item");

    public static void load() {

        REGISTRY.registerServer(ENABLE, (slot, buf) -> {
            boolean enable = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            wrapper.setEnabled(enable);
        });
        REGISTRY.registerServer(ALLOW_LIST, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setAllowList(buf.readBoolean());
        });
        REGISTRY.registerServer(PRIMAL_MATCH, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setPrimaryMatch(PrimaryMatch.fromOrdinal(buf.readInt()));
        });
        REGISTRY.registerServer(MATCH_DURABILITY, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setMatchDurability(buf.readBoolean());
        });
        REGISTRY.registerServer(MATCH_NBT, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setMatchNbt(buf.readBoolean());
        });
        REGISTRY.registerServer(ORE_DICT_ADD, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .addOreDictEntry(buf.readStringFromBuffer(255));
        });
        REGISTRY.registerServer(ORE_DICT_REMOVE, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .removeOreDictEntry(buf.readInt());
        });
        REGISTRY.registerServer(ORE_DICT_MATCH_TYPE, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setMatchAnyTag(buf.readBoolean());
        });
        REGISTRY.registerServer(CONTENTS_FILTER, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof IContentsFilteredUpgrade upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setDepositFilterType(ContentsFilterType.fromOrdinal(buf.readInt()));
        });

        REGISTRY.registerServer(FILL_CRAFTING, (slot, buf) -> {
            boolean fill = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setRefillCraftingGrid(fill);
        });
        REGISTRY.registerServer(SHIFT_INTO_STORAGE, (slot, buf) -> {
            boolean shiftIntoStorage = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setShiftClickIntoStorage(shiftIntoStorage);
        });
        REGISTRY.registerServer(CRAFTING_ROTATED, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers.rotated(upgradeWrapper.getStorage(), buf.readBoolean());
        });
        REGISTRY.registerServer(CRAFTING_BALANCE, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers.balance(upgradeWrapper.getStorage());
        });
        REGISTRY.registerServer(CRAFTING_SPREAD, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers.spread(upgradeWrapper.getStorage());
        });
        REGISTRY.registerServer(CRAFTING_CLEAR, (slot, buf) -> {
            boolean intoInventory = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers
                .clear(slot.getPanel(), slot.getWrapper(), upgradeWrapper.getStorage(), intoInventory);
        });

        REGISTRY.registerServer(FEED_AT_HUNGER_LEVEL, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof FeedingUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setFeedAtHungerLevel(HungerLevel.fromOrdinal(buf.readInt()));
        });
        REGISTRY.registerServer(FEED_WHEN_HURT, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof FeedingUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setFeedImmediatelyWhenHurt(buf.readBoolean());
        });

        REGISTRY.registerServer(PICKUP_XP, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof MagnetUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setPickupXp(buf.readBoolean());
        });
        REGISTRY.registerServer(PICKUP_ITEMS, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof MagnetUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setPickupItems(buf.readBoolean());
        });
    }
}
