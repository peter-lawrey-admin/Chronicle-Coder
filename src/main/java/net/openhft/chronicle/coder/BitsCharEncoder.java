package net.openhft.chronicle.coder;

public class BitsCharEncoder implements Coder {
    private final char[] symbols;
    private final byte[] encoding;
    private final int base, shift;
    private final int min;

    public BitsCharEncoder(char[] symbols, byte[] encoding, int min) {
        this.symbols = symbols;
        this.encoding = encoding;
        this.base = symbols.length;
        this.shift = 31 - Integer.numberOfLeadingZeros(base);
        this.min = min;
    }

    static void reverse(StringBuilder sb, int start) {
        int end = sb.length() - 1;
        for (; start < end; start++, end--) {
            char tmp = sb.charAt(start);
            sb.setCharAt(start, sb.charAt(end));
            sb.setCharAt(end, tmp);
        }
    }

    @Override
    public long parseLong(CharSequence cs, int offset, int length) {
        long value = 0;
        for (int i = offset; i < offset + length; i++) {
            char ch = cs.charAt(i);
            byte code = ch < min || ch >= min + encoding.length
                    ? CharCoderBuilder.UNSET
                    : encoding[ch - min];
            if (code == CharCoderBuilder.UNSET)
                throw new IllegalArgumentException("Unexpected character '" + ch + "'");
            if (code == CharCoderBuilder.IGNORED)
                continue;
            value <<= shift;
            value += code;
        }
        return value;
    }

    @Override
    public byte[] parseBytes(CharSequence cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendLong(StringBuilder sb, long value) {
        int start = sb.length();
        int base = symbols.length;
        do {
            long val2 = value >>> shift;
            int sym2 = (int) (value & (base - 1));
            sb.append(symbols[sym2]);
            value = val2;
        } while (value > 0);
        reverse(sb, start);
    }

    @Override
    public void appendBytes(StringBuilder sb, byte[] bytes) {
        throw new UnsupportedOperationException();
    }
}
