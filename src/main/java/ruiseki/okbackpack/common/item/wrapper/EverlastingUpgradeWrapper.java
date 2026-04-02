package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IEntityApplicable;
import ruiseki.okbackpack.common.entity.EntityBackpack;

public class EverlastingUpgradeWrapper extends UpgradeWrapperBase implements IEntityApplicable {

    public EverlastingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public void applyContainerEntity(World world, Entity selfEntity) {
        if (!(selfEntity instanceof EntityBackpack backpack)) return;
        backpack.setImmortal(true);
    }
}
