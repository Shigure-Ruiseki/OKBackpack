package ruiseki.okstoragecore.upgrades.magnet;

import net.minecraft.entity.Entity;

public interface IMagnetPreventionChecker {

    boolean isBlocked(Entity entity);
}
