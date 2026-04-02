package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

/**
 * Upgrade that can void items according to rules.
 * Extends IInventoryModifiable so it can directly modify insert/extract logic.
 */
public interface IVoidUpgrade extends IInventoryModifiable {

    String VOID_TYPE_TAG = "VoidType";
    String VOID_INPUT_TAG = "VoidInput";

    boolean canVoid(ItemStack stack);

    VoidType getVoidType();

    void setVoidType(VoidType type);

    VoidInput getVoidInput();

    void setVoidInput(VoidInput type);

    enum VoidType {
        OVERFLOW,
        ANY
    }

    enum VoidInput {
        ALL,
        AUTOMATION
    }
}
