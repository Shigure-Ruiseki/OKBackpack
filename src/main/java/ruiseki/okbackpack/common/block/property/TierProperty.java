package ruiseki.okbackpack.common.block.property;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import java.lang.reflect.Type;

import com.gtnewhorizon.gtnhlib.blockstate.core.InvalidPropertyTextException;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okcore.block.property.IProperty;
import ruiseki.okcore.block.property.PropertyGetter;
import ruiseki.okcore.block.property.PropertySetter;

public interface TierProperty extends IProperty<BackpackTier> {

    @Override
    default Type getType() {
        return BackpackTier.class;
    }

    default JsonElement serialize(BackpackTier value) {
        return new JsonPrimitive(this.stringify(value));
    }

    default BackpackTier deserialize(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive()
            .isString() ? this.parse(element.getAsString()) : TierRegistry.getTier(LEATHER);
    }

    default String stringify(BackpackTier value) {
        return value.getId().toLowerCase();
    }

    default BackpackTier parse(String text) throws InvalidPropertyTextException {
        try {
            return TierRegistry.getTier(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPropertyTextException("Invalid ForgeDirection", e);
        }
    }

    static AbstractTierProperty tier(BackpackTier defaultValue, PropertyGetter<BackpackTier> getter,
        PropertySetter<BackpackTier> setter) {
        return construct("tier", defaultValue, getter, setter);
    }

    static AbstractTierProperty construct(String name, BackpackTier defaultValue,
        final PropertyGetter<BackpackTier> getter, final PropertySetter<BackpackTier> setter) {
        return new AbstractTierProperty(name, defaultValue) {

            public BackpackTier getValue(ItemStack stack) {
                return stack.getItem() instanceof BlockBackpack.ItemBackpack backpack ? backpack.getTier()
                    : this.getDefaultValue();
            }

            public BackpackTier getValue(IBlockAccess w, int x, int y, int z) {
                BackpackTier r = getter.get(w, x, y, z);
                return r != null ? r : this.getDefaultValue();
            }

            public void setValue(World w, int x, int y, int z, BackpackTier v) {
                setter.accept(w, x, y, z, v);
            }
        };
    }

    public abstract static class AbstractTierProperty implements TierProperty {

        private String name;
        private BackpackTier defaultValue;

        public AbstractTierProperty(String name, BackpackTier defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public AbstractTierProperty setName(String name) {
            this.name = name;
            return this;
        }

        public BackpackTier getDefaultValue() {
            return this.defaultValue;
        }

        public String getName() {
            return this.name;
        }
    }
}
