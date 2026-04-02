package ruiseki.okbackpack.api.wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * An upgrade that can apply special behavior to the backpack container
 * and/or entities nearby.
 */
public interface IEntityApplicable {

    /**
     * Called when the upgrade should apply behavior directly to the backpack container.
     *
     * @param world      The current world
     * @param selfEntity The entity representing the backpack container
     *                   (e.g., ItemEntity in the world, or a TileEntity)
     */
    default void applyContainerEntity(World world, Entity selfEntity) {}

    /**
     * Called when the upgrade should affect entities near the backpack container.
     *
     * @param nearbyEntity The nearby entity that the upgrade can affect
     */
    default void applyToNearbyEntity(Entity nearbyEntity) {}
}
