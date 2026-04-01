package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IVoidUpgrade {

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
