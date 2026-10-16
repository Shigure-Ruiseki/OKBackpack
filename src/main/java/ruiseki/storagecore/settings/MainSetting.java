package ruiseki.storagecore.settings;

import java.util.Optional;
import java.util.function.BiFunction;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okcore.datastructure.TriConsumer;

public class MainSetting<T> {

    private final String tagName;
    private final BiFunction<NBTTagCompound, String, Optional<T>> getValue;
    private final TriConsumer<NBTTagCompound, String, T> setValue;
    private final T defaultValue;

    public MainSetting(String tagName, BiFunction<NBTTagCompound, String, Optional<T>> getValue,
        TriConsumer<NBTTagCompound, String, T> setValue, T defaultValue) {
        this.tagName = tagName;
        this.getValue = getValue;
        this.setValue = setValue;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return tagName;
    }

    public void setValue(NBTTagCompound tag, T value) {
        setValue.accept(tag, tagName, value);
    }

    public void removeFrom(NBTTagCompound tag) {
        tag.removeTag(tagName);
    }

    public Optional<T> getValue(NBTTagCompound tag) {
        return getValue.apply(tag, tagName);
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}
