package net.openhft.chronicle.coder;

import java.util.Arrays;

public class CharCoderBuilder {
    public static final byte UNSET = Byte.MIN_VALUE;
    public static final byte IGNORED = -1;
    private byte[] encoding;
    private int min;
    private final char[] symbols;

    private boolean signed;

    public CharCoderBuilder(String symbols) {
        this.symbols = symbols.toCharArray();
        assert this.symbols.length < 128;
        int min = Character.MAX_VALUE;
        int max = Character.MIN_VALUE;
        for (char ch : this.symbols) {
            min = Math.min(ch, min);
            max = Math.max(ch, max);
        }
        this.min = min;
        encoding = new byte[max - min + 1];
        Arrays.fill(encoding, UNSET);
        for (int i = 0; i < this.symbols.length; i++) {
            char ch = this.symbols[i];
            if (encoding(ch) >= 0)
                throw new IllegalArgumentException("Symbol '" + ch + "' appears more than once");
            encoding(ch, (byte) i);
        }
    }

    public CharCoderBuilder addAlias(char from, char to) {
        if (encoding(from) >= 0)
            throw new IllegalStateException("Cannot alias symbol '" + from + "'");
        if (encoding(to) < 0)
            throw new IllegalStateException("Cannot alias symbol '" + from + "' to '" + to + "'");
        encoding(from, encoding(to));
        return this;
    }

    public CharCoderBuilder ignored(String ignored) {
        for (int i = 0; i < ignored.length(); i++) {
            char ch = ignored.charAt(i);
            if (encoding(ch) >= 0)
                throw new IllegalStateException("Cannot ignore symbol '" + ch + "'");
            encoding(ch, IGNORED);
        }
        return this;
    }

    public CharCoderBuilder signed(boolean signed) {
        this.signed = signed;
        return this;
    }

    public Coder build() {
        if (!signed && Integer.bitCount(symbols.length) == 1)
            return new BitsCharEncoder(symbols, encoding, min);
        return new CharEncoder(symbols, encoding, min, signed);
    }

    private byte encoding(char ch) {
        return ch >= min && ch < min + encoding.length ? encoding[ch - min] : UNSET;
    }

    private void encoding(char set, byte code) {
        if (set < min) {
            int shift = min - set;
            byte[] encoding2 = new byte[encoding.length + shift];
            System.arraycopy(encoding, 0, encoding2, shift, encoding.length);
            encoding = encoding2;
            for (int i = 1; i < shift; i++)
                encoding[i] = UNSET;
            min = set;
        } else if (set >= min + encoding.length) {
            int oldLength = encoding.length;
            encoding = Arrays.copyOf(encoding, set + 1 - min);
            for (int i = oldLength; i < encoding.length; i++)
                encoding[i] = UNSET;
        }
        encoding[set - min] = code;
    }
}
