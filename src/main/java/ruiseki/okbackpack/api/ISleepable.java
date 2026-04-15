package ruiseki.okbackpack.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface ISleepable {

    String SLEEPING_BAG_DEPLOYED_TAG = "SleepingBagDeloyed";
    String SLEEPING_BAG_X = "SleepingBagX";
    String SLEEPING_BAG_Y = "SleepingBagY";
    String SLEEPING_BAG_Z = "SleepingBagZ";

    boolean deploySleepingBag(EntityPlayer player, World world, int meta, int cX, int cY, int cZ);

    void removeSleepingBag(World world);
}
