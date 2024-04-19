package md2html;

import java.util.HashMap;
import java.util.Map;

public class ParagraphParser {
    private record Insertion(String tag, int offset) {}

    private static final Map<String, String> TAGS = new HashMap<>(Map.ofEntries(
            Map.entry("_", "em"),
            Map.entry("*", "em"),
            Map.entry("__", "strong"),
            Map.entry("**", "strong"),
            Map.entry("`", "code"),
            Map.entry("--", "s"),
            Map.entry("![", "<img alt='"),
            Map.entry("](", "' src='"),
            Map.entry(")", "'>")
    ));
    
    private final Map<String, Integer> previousEntries;
    private final Map<Integer, Insertion> insertions = new HashMap<>();
    private String initialText;

    ParagraphParser(String text) {
        this.initialText = text;

        previousEntries = new HashMap<>();
        for (String el : TAGS.keySet()) {
            previousEntries.put(el, -1);
        }
    }

    public String parse() {
        StringBuilder sb = new StringBuilder();
        int lvlCounter = 0;
        while (initialText.charAt(lvlCounter) == '#') {
            lvlCounter++;
        }
        if (initialText.charAt(lvlCounter) != ' ') {
            lvlCounter = 0;
        }

        if (lvlCounter > 0) {
            initialText = initialText.substring(lvlCounter + 1);
            sb.append(getTag("h" + lvlCounter, false));
        } else {
            sb.append("<p>");
        }

        sb.append(parseBody(initialText));
        
        if (lvlCounter == 0) {
            sb.append("</p>");
        } else {
            sb.append(getTag("h" + lvlCounter, true));
        }

        return sb.toString();
    }

    private String getTag(String tag, boolean isClosing) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        if (isClosing) {
            sb.append("/");
        }
        sb.append(tag);
        sb.append(">");
        return sb.toString();
    }

    private void checkMap(String mdSymbol, int currentPosition) {
        if (previousEntries.get(mdSymbol) != -1) {
            insertions.put(previousEntries.get(mdSymbol), new Insertion(getTag(TAGS.get(mdSymbol), false), mdSymbol.length()));
            insertions.put(currentPosition, new Insertion(getTag(TAGS.get(mdSymbol), true), mdSymbol.length()));
            previousEntries.put(mdSymbol, -1);
        } else {
            previousEntries.put(mdSymbol, currentPosition);
        }
    }

    private String parseBody(String text) {
        boolean ignoreNext = false;
        boolean imageStarted = false;
        for (int i = 0; i < text.length(); i++) {
            String currentCharStringified = text.substring(i, i + 1);
            if (!ignoreNext && !imageStarted) {
                switch (text.charAt(i)) {
                    case '*':
                    case '_':
                        if (i < text.length() - 1 && text.charAt(i + 1) == text.charAt(i)) {
                            checkMap(text.substring(i, i + 2), i);
                            i++;
                        } else {
                            checkMap(currentCharStringified, i);
                        }
                        break;
                    case '`':
                        checkMap(currentCharStringified, i);
                        break;
                    case '-':
                        if (i < text.length() - 1 && text.charAt(i + 1) == text.charAt(i)) {
                            checkMap(text.substring(i, i + 2), i);
                            i++;
                        }
                        break;
                    case '\\':
                        ignoreNext = true;
                        break;
                    case '!':
                        if (i < text.length() - 1 && text.charAt(i + 1) == '[') {
                            imageStarted = true;
                            insertions.put(i, new Insertion(TAGS.get("!["), 2));
                            i++;
                        }
                        break;
                }
            } else if (imageStarted) {
                switch(text.charAt(i)) {
                    case ']':
                        insertions.put(i, new Insertion(TAGS.get("]("), 2));
                        i++;
                        break;
                    case ')':
                        imageStarted = false;
                        insertions.put(i, new Insertion(TAGS.get(")"), 1));
                        break;
                }
            } else if (ignoreNext) {
                ignoreNext = false;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            Insertion thisPosInseriton = insertions.get(i);
            if (thisPosInseriton != null) {
                sb.append(thisPosInseriton.tag());
                i += thisPosInseriton.offset() - 1;
                continue;
            }

            String strToAppend = switch (text.charAt(i)) {
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '&' -> "&amp;";
                case '\\' -> "";
                default -> text.substring(i, i + 1);
            };
            sb.append(strToAppend);
        }

        return sb.toString();
    }
}
