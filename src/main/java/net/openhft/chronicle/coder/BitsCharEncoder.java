package net.openhft.chronicle.coder;

public class BitsCharEncoder extends AbstractCharEncoder {
    private final int base, shift;

    public BitsCharEncoder(char[] symbols, byte[] encoding, int min) {
        super(symbols, encoding, min);
        this.base = symbols.length;
        this.shift = 31 - Integer.numberOfLeadingZeros(base);
    }

    @Override
    public long parseLong(CharSequence cs, int offset, int length) {
        long value = 0;
        for (int i = offset; i < offset + length; i++) {
            char ch = cs.charAt(i);
            byte code = encoding(ch);
            if (code == CharCoderBuilder.IGNORED)
                continue;
            value <<= shift;
            value += code;
        }
        return value;
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
}
