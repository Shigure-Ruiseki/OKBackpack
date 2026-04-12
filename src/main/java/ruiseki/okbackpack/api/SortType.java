package ruiseki.okbackpack.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

public enum SortType {

    BY_NAME,
    BY_MOD_ID,
    BY_COUNT,
    BY_ORE_DICT;

    private static final ImmutableList<SortType> values = ImmutableList.copyOf(values());

    public SortType next(List<SortType> allowed) {
        if (allowed == null || allowed.isEmpty()) return this;

        int i = allowed.indexOf(this);
        if (i < 0) return allowed.get(0);

        return allowed.get((i + 1) % allowed.size());
    }

    public SortType prev(List<SortType> allowed) {
        if (allowed == null || allowed.isEmpty()) return this;

        int i = allowed.indexOf(this);
        if (i < 0) return allowed.get(0);

        return allowed.get(Math.floorMod(i - 1, allowed.size()));
    }

    public static SortType byIndex(int index) {
        if (index < 0 || index >= values.size()) return BY_NAME;

        return values.get(index);
    }

    public int getIndex() {
        return values.indexOf(this);
    }
}
