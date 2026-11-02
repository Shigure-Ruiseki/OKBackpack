package ruiseki.okstoragecore.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;

public class FilterLogicBase {

    protected final ItemStack upgrade;
    protected final Consumer<ItemStack> saveHandler;

    protected final String parentTagKey;

    private boolean allowListDefault = false;

    @Nullable
    protected List<String> oreDictEntries = null;
    private final Map<String, Optional<Pattern>> oreDictPatternCache = new HashMap<>();

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
        }

        if (shouldMatchDurability() && stack.getItemDamage() != filter.getItemDamage()) {
            return false;
        }

        return !shouldMatchNbt() || ItemHelpers.areItemsEqualIgnoreDurability(stack, filter);
    }

    public List<String> getOreDictEntries() {
        if (oreDictEntries == null) {
            initOreDictEntries();
        }
        return Collections.unmodifiableList(oreDictEntries);
    }

    public boolean addOreDictEntry(String name) {
        if (name == null || name.trim()
            .isEmpty()) {
            return false;
        }
        name = name.trim();
        if (!isValidOreDictEntry(name)) {
            return false;
        }
        if (oreDictEntries == null) {
            initOreDictEntries();
        }

        if (oreDictEntries.contains(name)) {
            return false;
        }

        oreDictEntries.add(name);
        serializeOreDictEntries();
        save();

        return true;
    }

    public boolean removeOreDictEntry(int index) {
        if (oreDictEntries == null) {
            initOreDictEntries();
        }

        if (index < 0 || index >= oreDictEntries.size()) {
            return false;
        }

        String removed = oreDictEntries.remove(index);
        oreDictPatternCache.remove(removed);

        serializeOreDictEntries();
        save();

        return true;
    }

    public int indexOfOreDictEntry(String name) {
        return getOreDictEntries().indexOf(name);
    }

    protected void initOreDictEntries() {
        oreDictEntries = NBTHelpers
            .getCollection(
                upgrade,
                parentTagKey,
                "oreDictEntries",
                Constants.NBT.TAG_STRING,
                nbt -> Optional.of(((NBTTagString) nbt).func_150285_a_()),
                ArrayList::new)
            .orElseGet(ArrayList::new);
    }

    private void serializeOreDictEntries() {
        if (oreDictEntries == null) {
            return;
        }

        NBTHelpers.setList(upgrade, parentTagKey, "oreDictEntries", oreDictEntries, NBTTagString::new);
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

    public boolean shouldMatchAnyOreDict() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "matchAnyOreDict")
            .orElse(true);
    }

    public void setMatchAnyTag(boolean matchAnyTag) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "matchAnyOreDict", matchAnyTag);
        save();
    }

    @Nullable
    private Pattern getOreDictPattern(String expression) {
        return oreDictPatternCache.computeIfAbsent(expression, key -> {
            try {
                return Optional.of(Pattern.compile(key));
            } catch (PatternSyntaxException e) {
                return Optional.empty();
            }
        })
            .orElse(null);
    }

    private boolean isValidOreDictEntry(String expression) {
        return getOreDictPattern(expression) != null;
    }

    public boolean matchesOreDictPattern(String oreName, String expression) {
        Pattern pattern = getOreDictPattern(expression);
        return pattern != null && pattern.matcher(oreName)
            .matches();
    }
}
