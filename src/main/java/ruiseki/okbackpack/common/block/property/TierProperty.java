package ruiseki.okbackpack.common.block.property;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import java.lang.reflect.Type;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.gtnewhorizon.gtnhlib.blockstate.core.InvalidPropertyTextException;

import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okcore.block.property.IProperty;
import ruiseki.okcore.block.property.PropertyGetter;
import ruiseki.okcore.block.property.PropertySetter;

public interface TierProperty extends IProperty<String> {

    @Override
    default Type getType() {
        return String.class;
    }

    default JsonElement serialize(String value) {
        return new JsonPrimitive(this.stringify(value));
    }

    default String deserialize(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive()
            .isString() ? this.parse(element.getAsString()) : LEATHER;
    }

    default String stringify(String value) {
        return value == null ? LEATHER : value.toLowerCase();
    }

    default String parse(String text) throws InvalidPropertyTextException {
        if (text == null || text.isEmpty()) {
            throw new InvalidPropertyTextException("Empty tier id");
        }

        // Tier ids are stored lowercase, so the lookup must not upper case the text or it always misses.
        String id = text.toLowerCase();
        if (!TierRegistry.isRegistered(id)) {
            throw new InvalidPropertyTextException("Unknown backpack tier: " + text);
        }

        return id;
    }

    static AbstractTierProperty tier(String defaultValue, PropertyGetter<String> getter,
        PropertySetter<String> setter) {
        return construct("tier", defaultValue, getter, setter);
    }

    static AbstractTierProperty construct(String name, String defaultValue, final PropertyGetter<String> getter,
        final PropertySetter<String> setter) {
        return new AbstractTierProperty(name, defaultValue) {

            public String getValue(ItemStack stack) {
                return stack.getItem() instanceof BlockBackpack.ItemBackpack backpack ? backpack.getTierId()
                    : this.getDefaultValue();
            }

            public String getValue(IBlockAccess w, int x, int y, int z) {
                String r = getter.get(w, x, y, z);
                return r != null ? r : this.getDefaultValue();
            }

            public void setValue(World w, int x, int y, int z, String v) {
                setter.accept(w, x, y, z, v);
            }
        };
    }

    public abstract static class AbstractTierProperty implements TierProperty {

        private String name;
        private String defaultValue;

        public AbstractTierProperty(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public AbstractTierProperty setName(String name) {
            this.name = name;
            return this;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }

        public String getName() {
            return this.name;
        }
    }
}
