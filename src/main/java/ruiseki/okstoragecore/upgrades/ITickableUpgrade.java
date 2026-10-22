package ruiseki.okstoragecore.upgrades;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;

public interface ITickableUpgrade {

    void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos);
}
