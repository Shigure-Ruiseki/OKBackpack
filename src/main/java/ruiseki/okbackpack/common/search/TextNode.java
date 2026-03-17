package ruiseki.okbackpack.common.search;

final class TextNode implements SearchNode {

    private final String text;

    TextNode(String text) {
        this.text = text;
    }

    @Override
    public boolean matches(ItemStackKey k) {
        if (k.getDisplayName()
            .contains(text)) return true;
        for (String line : k.getTooltipLower()) {
            if (line.contains(text)) return true;
        }
        return false;
    }
}
