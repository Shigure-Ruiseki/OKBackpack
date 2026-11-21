package ruiseki.okstoragecore.upgrades;

import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

public interface IPickupXpUpgrade {

    boolean onPickupXp(PlayerPickupXpEvent event);
}
