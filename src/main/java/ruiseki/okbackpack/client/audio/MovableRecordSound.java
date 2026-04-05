package ruiseki.okbackpack.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.util.ResourceLocation;

public class MovableRecordSound implements ITickableSound {

    private final ResourceLocation soundResource;
    private float xPos;
    private float yPos;
    private float zPos;
    private boolean donePlaying = false;
    private float volume = 1.0F;
    private float pitch = 1.0F;
    private final boolean local;

    public MovableRecordSound(ResourceLocation soundResource, float x, float y, float z, boolean local) {
        this.soundResource = soundResource;
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
        this.local = local;
    }

    public void updatePosition(float x, float y, float z) {
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
    }

    public void stopSound() {
        this.donePlaying = true;
    }

    @Override
    public void update() {}

    @Override
    public boolean isDonePlaying() {
        return donePlaying;
    }

    @Override
    public ResourceLocation getPositionedSoundLocation() {
        return soundResource;
    }

    @Override
    public boolean canRepeat() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getXPosF() {
        return xPos;
    }

    @Override
    public float getYPosF() {
        return yPos;
    }

    @Override
    public float getZPosF() {
        return zPos;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public ISound.AttenuationType getAttenuationType() {
        return local ? ISound.AttenuationType.NONE : ISound.AttenuationType.LINEAR;
    }
}
