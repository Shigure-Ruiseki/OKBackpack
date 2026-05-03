package ruiseki.okbackpack.api.wrapper;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Interface for the experience pump upgrade. Transfers XP between an in-pack {@link ITankUpgrade}
 * (filled with the {@code xpjuice} fluid) and a nearby player.
 */
public interface IXpPumpUpgrade extends ITickable, IToggleable {

    enum XpPumpDirection {

        INPUT,
        OUTPUT,
        KEEP,
        OFF;

        public XpPumpDirection next() {
            XpPumpDirection[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    XpPumpDirection getDirection();

    void setDirection(XpPumpDirection direction);

    int getLevelTarget();

    void setLevelTarget(int level);

    int getLevelsToStore();

    void setLevelsToStore(int levels);

    int getLevelsToTake();

    void setLevelsToTake(int levels);

    boolean isMending();

    void setMending(boolean mending);

    boolean hasTankAvailable();

    void takeAllFromPlayer(EntityPlayer player);

    void giveAllToPlayer(EntityPlayer player);

    void takeLevelsFromPlayer(EntityPlayer player);

    void giveLevelsToPlayer(EntityPlayer player);
}
