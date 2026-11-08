package ruiseki.backpack.api;

import net.minecraft.entity.EntityLivingBase;

/**
 * Handles fall damage modifications for a living entity.
 */
public interface ILivingFallUpgrade {

    /**
     * Modifies the fall damage dealt to an entity.
     *
     * @param entity   the entity taking fall damage
     * @param distance the fall distance
     * @return the modified damage multiplier
     */
    float onLivingFall(EntityLivingBase entity, float distance);
}
