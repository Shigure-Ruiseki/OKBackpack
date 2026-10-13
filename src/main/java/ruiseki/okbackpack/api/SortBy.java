package ruiseki.okbackpack.api;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;

public enum SortBy {

    NAME,
    MOD_ID,
    COUNT,
    ORE_DICT;

    private static final ImmutableList<SortBy> values = ImmutableList.copyOf(values());

    public SortBy next(List<SortBy> allowed) {
        if (allowed == null || allowed.isEmpty()) return this;

        int i = allowed.indexOf(this);
        if (i < 0) return allowed.get(0);

        return allowed.get((i + 1) % allowed.size());
    }

    public SortBy prev(List<SortBy> allowed) {
        if (allowed == null || allowed.isEmpty()) return this;

        int i = allowed.indexOf(this);
        if (i < 0) return allowed.get(0);

        return allowed.get(Math.floorMod(i - 1, allowed.size()));
    }

    public static SortBy byIndex(int index) {
        if (index < 0 || index >= values.size()) return NAME;

        return values.get(index);
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static SortBy fromName(String name) {
        if (name == null || name.isEmpty()) {
            return NAME;
        }

        for (SortBy sortBy : values) {
            if (sortBy.name()
                .equalsIgnoreCase(name)) {
                return sortBy;
            }
        }

        return NAME;
    }

    public int getIndex() {
        return values.indexOf(this);
    }
}
