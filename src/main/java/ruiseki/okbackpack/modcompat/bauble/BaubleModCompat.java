package ruiseki.okbackpack.modcompat.bauble;

import ruiseki.okbackpack.helpers.BaublesHelpers;
import ruiseki.okbackpack.helpers.PlayerInventoryHandler;
import ruiseki.okbackpack.helpers.PlayerInventoryProvider;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class BaubleModCompat implements IModCompat {

    public static final String BAUBLES_INVENTORY = "baubles";

    @Override
    public String getModID() {
        return Mods.BaublesExpanded.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Bauble compatibility";
    }

    @Override
    public void onInit(Step step) {
        if (step == Step.PREINIT) {
            BaublesHelpers.assignSlotsUpToMinimum("body", 1);
        }

        if (step == Step.INIT) {
            PlayerInventoryProvider.get()
                .addPlayerInventoryHandler(
                    BAUBLES_INVENTORY,
                    gameTime -> PlayerInventoryHandler.SINGLE_IDENTIFIER,
                    (player, identifier) -> BaublesHelpers.getBaubles(player)
                        .getSizeInventory(),
                    (player, identifier, slot) -> BaublesHelpers.getBaubles(player)
                        .getStackInSlot(slot),
                    true,
                    false,
                    false,
                    true);
        }
    }
}
