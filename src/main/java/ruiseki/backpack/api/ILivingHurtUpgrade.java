package ruiseki.backpack.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

/**
 * Implement this interface to allow an upgrade to respond to {@link
 * net.minecraftforge.event.entity.living.LivingHurtEvent}.
 */
public interface ILivingHurtUpgrade {

    /**
     * Modifies the damage dealt to a living entity.
     *
     * @param entity the entity being hurt
     * @param source the source of the damage
     * @param amount the current damage amount
     * @return the modified damage amount
     */
    float onLivingHurt(EntityLivingBase entity, DamageSource source, float amount);
}
