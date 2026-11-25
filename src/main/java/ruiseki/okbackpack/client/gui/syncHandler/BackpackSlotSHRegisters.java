package ruiseki.okbackpack.client.gui.syncHandler;

import ruiseki.okbackpack.api.upgrade.BackpackSlotSHRegistry;
import ruiseki.okcore.init.IInitListener;

public class BackpackSlotSHRegisters implements IInitListener {

    public static final String UPDATE_SET_MEMORY_STACK = "update_set_memory_stack";
    public static final String UPDATE_UNSET_MEMORY_STACK = "update_unset_memory_stack";
    public static final String UPDATE_SET_SLOT_LOCK = "update_set_slot_lock";
    public static final String UPDATE_UNSET_SLOT_LOCK = "update_unset_slot_lock";

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            BackpackSlotSHRegistry.registerServer(
                UPDATE_SET_MEMORY_STACK,
                (slot, buf) -> {
                    slot.wrapper.setMemoryStack(
                        slot.getSlot()
                            .getSlotIndex(),
                        buf.readBoolean());
                });

            BackpackSlotSHRegistry.registerServer(
                UPDATE_UNSET_MEMORY_STACK,
                (slot, buf) -> {
                    slot.wrapper.unsetMemoryStack(
                        slot.getSlot()
                            .getSlotIndex());
                });

            BackpackSlotSHRegistry.registerServer(
                UPDATE_SET_SLOT_LOCK,
                (slot, buf) -> {
                    slot.wrapper.setSlotLocked(
                        slot.getSlot()
                            .getSlotIndex(),
                        true);
                });

            BackpackSlotSHRegistry.registerServer(
                UPDATE_UNSET_SLOT_LOCK,
                (slot, buf) -> {
                    slot.wrapper.setSlotLocked(
                        slot.getSlot()
                            .getSlotIndex(),
                        false);
                });

        }
    }

}
