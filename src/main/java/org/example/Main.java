package org.example;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Main {

    //TESTABLE SAMPLES
    public static final String WORD = "In similique voluptatem est consequatur cupiditate aut veniam veritatis aut autem suscipit in corrupti placeat et suscipit";
    public static final String WORD_0 = "abracadabra";
    public static final String WORD_1 = "AABCBBABC";
    public static final String WORD_2 = "abacabacabadaca";

    public static final int BUFFER_SIZE = 25;
    public static final char OPEN_SYMBOL = '(';
    public static final char CLOSE_SYMBOL = ')';
    public static final String EMPTY = "";
    public static final String COMMA = ",";

    record Pair(int offset, int length, boolean hasNextSymbol) {
    }

    record Node(Pair pair, char symbol) {
        @Override
        public String toString() {
            if (this.symbol == '\0')
                return String.format("(%d,%d)", pair.offset, pair.length);
            else if (this.pair.length == 0 && this.pair.offset == 0)
                return String.valueOf(this.symbol());
            return String.format("(%d,%d,%s)", pair.offset, pair.length, this.symbol);
        }
    }

    public static Pair findMaxPrefix(String buffer, int pos) {
        var pref = String.valueOf(WORD.charAt(pos));
        if (buffer.contains(pref)) {
            var entryIndex = buffer.lastIndexOf(pref);
            return getString(buffer, pos, 1, entryIndex);
        } else {
            return new Pair(0, 0, true);
        }
    }

    private static Pair getString(String buffer, int pos, int length, int entryIndex) {
        var offset = Math.abs(entryIndex - buffer.length());
        if (pos + length >= WORD.length()) {
            return new Pair(offset, WORD.length() - pos, false);
        }

        var pref = WORD.substring(pos, pos + length + 1);

        if (buffer.equals(pref)) {
            var start = pos + length + 1;
            var prefAfterWord = "";
            var i = 1;
            do {
                prefAfterWord = WORD.substring(start, start + i + 1);
                i++;
            } while (buffer.contains(prefAfterWord));

            return new Pair(offset, length + i, true);
        }

        if (buffer.contains(pref)) {
            var entryIndexNew = buffer.lastIndexOf(pref);
            return getString(buffer, pos, length + 1, entryIndexNew);
        } else {
            return new Pair(offset, length, true);
        }
    }

    private static List<Node> encodeWithLZ77() {
        List<Node> nodes = new ArrayList<>();
        var buffer = EMPTY;
        var pos = 0;
        var index = 0;

        while (pos < WORD.length()) {
            var pair = findMaxPrefix(buffer, pos);
            var calculatedLength = pair.length() + 1;
            var symbol = pair.hasNextSymbol ? WORD.charAt(pos + calculatedLength - 1) : '\0';
            var node = new Node(pair, symbol);
            nodes.add(node);
            index = pos + calculatedLength;
            if (index >= WORD.length()) {
                break;
            }
            if (index > BUFFER_SIZE) {
                buffer = WORD.substring(index - BUFFER_SIZE, index);
            } else {
                var appendBuffer = WORD.substring(pos, index <= WORD.length() ? index : WORD.length());
                buffer += appendBuffer;
            }
//            System.out.println(buffer);
            pos += calculatedLength;
        }

        return nodes;
    }

    private static String decodeLZ77(StringBuilder message) {
        var msg = message.toString();
        while (msg.indexOf(OPEN_SYMBOL) != -1) {
            var start = msg.indexOf(OPEN_SYMBOL);
            var end = msg.indexOf(CLOSE_SYMBOL);
            var node = msg.substring(start, end + 1);
            var data = node
                    .replace("(", EMPTY)
                    .replace(")", EMPTY)
                    .split(COMMA);

            var offset = Integer.valueOf(data[0]);
            var length = Integer.valueOf(data[1]);
            var suffix = "";
            if (length > offset) {
                var diff = length - offset;
                suffix = msg.substring(start - offset, start) + msg.substring(start - diff - 1, diff) + (data.length == 3 ? data[2] : EMPTY);
            } else {
                suffix = msg.substring(start - offset, start - offset + length) + (data.length == 3 ? data[2] : EMPTY);
            }
            var symb = (data.length == 3) ? "," + data[2] : EMPTY;
            msg = msg.replaceFirst("\\(" + offset + "," + length + symb + "\\)", suffix);
        }
        return msg;
    }

    private static StringBuilder nodesToString(List<Node> nodes) {
        var message = new StringBuilder();
        nodes.forEach(message::append);
        return message;
    }

    public static void main(String[] args) {
        System.out.println(WORD);
        var encoded = nodesToString(encodeWithLZ77());
        System.out.println(encoded);
        var msg = decodeLZ77(encoded);
        System.out.println(msg);
        assertEquals(WORD, msg);
    }

}