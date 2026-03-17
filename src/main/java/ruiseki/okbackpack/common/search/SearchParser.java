package ruiseki.okbackpack.common.search;

import java.util.ArrayList;
import java.util.List;

public class SearchParser {

    private SearchParser() {}

    public static SearchNode parse(String input) {
        if (input == null || input.trim()
            .isEmpty()) {
            return null;
        }

        String[] orGroups = input.toLowerCase()
            .split("\\|");
        List<SearchNode> orNodes = new ArrayList<>();

        for (String group : orGroups) {
            group = group.trim();
            if (group.isEmpty()) continue;

            List<String> terms = splitTerms(group);
            List<SearchNode> andNodes = new ArrayList<>();

            for (String raw : terms) {
                if (raw.isEmpty()) continue;

                boolean neg = raw.startsWith("-");
                String term = neg ? raw.substring(1) : raw;

                SearchNode node = parseTerm(term);
                if (node == null) continue;

                if (neg) node = new NotNode(node);
                andNodes.add(node);
            }

            if (!andNodes.isEmpty()) {
                orNodes.add(andNodes.size() == 1 ? andNodes.get(0) : new AndNode(andNodes));
            }
        }

        if (orNodes.isEmpty()) return null;
        return orNodes.size() == 1 ? orNodes.get(0) : new OrNode(orNodes);
    }

    private static SearchNode parseTerm(String term) {
        if (term.isEmpty()) return null;

        char c = term.charAt(0);
        String body = term.substring(1);

        switch (c) {
            case '@':
                return new ModNode(body);
            case '#':
                return new TooltipNode(body);
            case '$':
                return new OreNode(body);
            case '%':
                return new CreativeTabNode(body);
            default:
                return new TextNode(term);
        }
    }

    private static List<String> splitTerms(String s) {
        List<String> result = new ArrayList<>();
        boolean quote = false;
        StringBuilder cur = new StringBuilder();

        for (char c : s.toCharArray()) {
            if (c == '"') {
                quote = !quote;
                continue;
            }
            if (c == ' ' && !quote) {
                if (cur.length() > 0) {
                    result.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }

        if (cur.length() > 0) result.add(cur.toString());
        return result;
    }
}
