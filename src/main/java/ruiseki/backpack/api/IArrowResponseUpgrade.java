package ruiseki.backpack.api;

import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

public interface IArrowResponseUpgrade {

    /**
     * Called when a player begins using a bow.
     *
     * @param event the bow nock event
     */
    boolean onArrowNock(ArrowNockEvent event);

    /**
     * Called when a player stops using a bow.
     *
     * @param event the bow loose event
     */
    boolean onArrowLoose(ArrowLooseEvent event);
}
