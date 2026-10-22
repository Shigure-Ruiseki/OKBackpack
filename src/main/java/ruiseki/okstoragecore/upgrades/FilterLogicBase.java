package ruiseki.okstoragecore.upgrades;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.tag.Registries;
import ruiseki.okcore.tag.TagKey;

public class FilterLogicBase {

    protected final ItemStack upgrade;
    protected final Consumer<ItemStack> saveHandler;

    protected final String parentTagKey;

    private boolean allowListDefault = false;

    @Nullable
    protected Set<TagKey<Item>> tagKeys = null;

    public FilterLogicBase(ItemStack upgrade, Consumer<ItemStack> saveHandler, String parentTagKey) {
        this.upgrade = upgrade;
        this.saveHandler = saveHandler;
        this.parentTagKey = parentTagKey;
    }

    public String getParentTagKey() {
        return parentTagKey;
    }

    public void setAllowByDefault(boolean allowListDefault) {
        this.allowListDefault = allowListDefault;
    }

    protected void save() {
        saveHandler.accept(upgrade);
    }

    public boolean stackMatchesFilter(ItemStack stack, ItemStack filter) {
        if (ItemHelpers.isEmpty(filter)) {
            return false;
        }

        PrimaryMatch primaryMatch = getPrimaryMatch();
        if (primaryMatch == PrimaryMatch.MOD) {
            // noinspection ConstantConditions
            if (!Item.itemRegistry.getNameForObject(stack.getItem())
                .equals(Item.itemRegistry.getNameForObject(filter.getItem()))) {
                return false;
            }
        } else if (primaryMatch == PrimaryMatch.ITEM) {
            if (!ItemHelpers.areItemsEqual(stack, filter)) {
                return false;
            }
        } else if (primaryMatch == PrimaryMatch.ORE_DICT) {
            if (!areOreDictionaryItemsEqual(stack, filter)) {
                return false;
            }
        }

        if (shouldMatchDurability() && stack.getItemDamage() != filter.getItemDamage()) {
            return false;
        }

        return !shouldMatchNbt() || ItemHelpers.areItemsEqualIgnoreDurability(stack, filter);
    }

    public Set<TagKey<Item>> getTagKeys() {
        if (tagKeys == null) {
            initTags();
        }
        return Collections.unmodifiableSet(tagKeys);
    }

    public void addTag(TagKey<Item> tagName) {
        if (tagKeys == null) {
            initTags();
        }
        tagKeys.add(tagName);
        serializeTags();
        save();
    }

    private void serializeTags() {
        if (tagKeys == null) {
            return;
        }
        NBTHelpers.setList(
            upgrade,
            parentTagKey,
            "tags",
            tagKeys,
            t -> new NBTTagString(
                t.location()
                    .toString()));
    }

    public void removeTagName(TagKey<Item> tagName) {
        if (tagKeys == null) {
            initTags();
        }
        tagKeys.remove(tagName);
        serializeTags();
        save();
    }

    protected void initTags() {
        Comparator<TagKey<Item>> comparator = Comparator.comparing(
            tag -> tag.location()
                .toString());
        tagKeys = NBTHelpers
            .getCollection(
                upgrade,
                parentTagKey,
                "tags",
                Constants.NBT.TAG_STRING,
                elementNbt -> Optional.of(
                    TagKey.create(Registries.ITEM, new ResourceLocation(((NBTTagString) elementNbt).func_150285_a_()))),
                () -> new TreeSet<>(comparator))
            .orElseGet(() -> new TreeSet<>(comparator));
    }

    public void setAllowList(boolean isAllowList) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "isAllowList", isAllowList);
        save();
    }

    public boolean isAllowList() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "isAllowList")
            .orElse(allowListDefault);
    }

    public boolean shouldMatchDurability() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "matchDurability")
            .orElse(false);
    }

    public void setMatchDurability(boolean matchDurability) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "matchDurability", matchDurability);
        save();
    }

    public void setMatchNbt(boolean matchNbt) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "matchNbt", matchNbt);
        save();
    }

    public boolean shouldMatchNbt() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "matchNbt")
            .orElse(false);
    }

    public void setPrimaryMatch(PrimaryMatch primaryMatch) {
        NBTHelpers.setEnumConstant(upgrade, parentTagKey, "primaryMatch", primaryMatch);
        save();
    }

    public PrimaryMatch getPrimaryMatch() {
        return NBTHelpers.getEnumConstant(upgrade, parentTagKey, "primaryMatch", PrimaryMatch::fromName)
            .orElse(PrimaryMatch.ITEM);
    }

    public boolean shouldMatchAnyTag() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "matchAnyTag")
            .orElse(true);
    }

    public void setMatchAnyTag(boolean matchAnyTag) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "matchAnyTag", matchAnyTag);
        save();
    }

    private boolean areOreDictionaryItemsEqual(ItemStack stack, ItemStack filter) {
        int[] stackOreIds = OreDictionary.getOreIDs(stack);
        int[] filterOreIds = OreDictionary.getOreIDs(filter);

        for (int stackOreId : stackOreIds) {
            for (int filterOreId : filterOreIds) {
                if (stackOreId == filterOreId) {
                    return true;
                }
            }
        }

        return false;
    }
}
