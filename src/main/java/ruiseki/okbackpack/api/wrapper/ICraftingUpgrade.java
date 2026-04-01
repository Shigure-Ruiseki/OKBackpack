package ruiseki.okbackpack.api.wrapper;

public interface ICraftingUpgrade extends IStorageUpgrade {

    String CRAFTING_DEST_TAG = "CraftingDest";
    String USE_BACKPACK_TAG = "UseBackpack";

    CraftingDestination getCraftingDes();

    void setCraftingDes(CraftingDestination type);

    boolean isUseBackpack();

    void setUseBackpack(boolean used);

    enum CraftingDestination {
        BACKPACK,
        INVENTORY;
    }
}
