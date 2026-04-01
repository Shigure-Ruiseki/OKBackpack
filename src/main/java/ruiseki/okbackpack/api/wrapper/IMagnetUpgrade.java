package ruiseki.okbackpack.api.wrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.joml.Vector3d;

public interface IMagnetUpgrade extends ITickable {

    String MAG_ITEM_TAG = "CollectItem";
    String MAG_EXP_TAG = "CollectExp";

    boolean isCollectItem();

    void setCollectItem(boolean enabled);

    boolean isCollectExp();

    void setCollectExp(boolean collectExp);

    boolean canCollectItem(ItemStack stack);

    default void toggleItem() {
        setCollectItem(!isCollectItem());
    }

    default void toggleExp() {
        setCollectExp(!isCollectExp());
    }

    default List<Entity> getMagnetEntities(World world, AxisAlignedBB aabb) {
        Set<Entity> result = new HashSet<>();

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, aabb);
        List<EntityXPOrb> xps = world.getEntitiesWithinAABB(EntityXPOrb.class, aabb);

        if (isCollectItem()) {
            for (EntityItem item : items) {
                if (canCollectItem(item.getEntityItem())) {
                    result.add(item);
                }
            }
        }
        if (isCollectExp()) {
            result.addAll(xps);
        }

        return new ArrayList<>(result);
    }

    default void setEntityMotionFromVector(Entity entity, Vector3d target, float modifier) {
        Vector3d current = fromEntityCenter(entity);

        Vector3d motion = new Vector3d(target.x - current.x, target.y - current.y, target.z - current.z);

        if (motion.length() > 1.0) {
            motion.normalize();
        }

        entity.motionX = motion.x * modifier;
        entity.motionY = motion.y * modifier;
        entity.motionZ = motion.z * modifier;
    }

    default Vector3d fromEntityCenter(Entity e) {
        return new Vector3d(e.posX, e.posY - e.yOffset + e.height / 2.0, e.posZ);
    }
}
