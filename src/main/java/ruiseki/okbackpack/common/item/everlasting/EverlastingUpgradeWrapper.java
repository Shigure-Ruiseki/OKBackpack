package ruiseki.okbackpack.common.item.everlasting;

import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IEntityApplicable;
import ruiseki.okbackpack.common.entity.EntityBackpack;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class EverlastingUpgradeWrapper extends UpgradeWrapperBase implements IEntityApplicable {

    public EverlastingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public void applyContainerEntity(World world, Entity selfEntity) {
        if (!(selfEntity instanceof EntityBackpack backpack)) return;
        backpack.setImmortal(true);
    }
}
