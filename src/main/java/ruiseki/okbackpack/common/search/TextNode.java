package ruiseki.okbackpack.common.search;

final class TextNode implements SearchNode {

    private final String text;

    TextNode(String text) {
        this.text = text;
    }

    @Override
    public boolean matches(ItemStackKey k) {
        if (k.getStack() == null || k.getStack()
            .getItem() == null) return false;
        return k.getStack()
            .getDisplayName()
            .contains(text);
    }
}
