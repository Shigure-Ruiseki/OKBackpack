package ruiseki.okbackpack.api.wrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import ruiseki.okcore.datastructure.BlockPos;

public interface ITickable {

    boolean tick(EntityPlayer player);

    boolean tick(World world, BlockPos pos);
}
