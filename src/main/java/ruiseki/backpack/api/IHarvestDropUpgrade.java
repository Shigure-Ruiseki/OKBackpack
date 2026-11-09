package ruiseki.backpack.api;

import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

/**
 * Handles block harvest drop modifications.
 */
public interface IHarvestDropUpgrade {

    void onHarvestDrops(HarvestDropsEvent event);
}
