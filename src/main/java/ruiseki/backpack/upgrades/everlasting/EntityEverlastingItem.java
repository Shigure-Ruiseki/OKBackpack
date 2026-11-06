package ruiseki.backpack.upgrades.everlasting;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.entity.EntityImmortalItem;

public class EntityEverlastingItem extends EntityImmortalItem {

    public EntityEverlastingItem(World world, Entity original, ItemStack stack) {
        super(world, original, stack);
        this.setImmortal(true);
    }
}
