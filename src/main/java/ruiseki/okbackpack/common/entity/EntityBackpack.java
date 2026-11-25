package ruiseki.okbackpack.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okcore.entity.EntityImmortalItem;

public class EntityBackpack extends EntityImmortalItem {

    public EntityBackpack(World world, Entity original, ItemStack stack, BackpackWrapper wrapper) {
        super(world, original, stack);
        wrapper.applyContainerEntity(world, this);
    }
}
