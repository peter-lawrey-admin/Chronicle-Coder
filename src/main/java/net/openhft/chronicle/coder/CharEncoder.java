package net.openhft.chronicle.coder;

public class CharEncoder extends AbstractCharEncoder {
    private final int base;
    private final boolean signed;

    public CharEncoder(char[] symbols, byte[] encoding, int min, boolean signed) {
        super(symbols, encoding, min);
        this.base = symbols.length;
        this.signed = signed;
    }

    @Override
    public long parseLong(CharSequence cs, int offset, int length) {
        boolean neg = false;
        if (signed && length > 0) {
            char ch = cs.charAt(offset);
            if (ch == '-') {
                neg = true;
                offset++;
                length--;
            } else if (ch == '+' && encoding['+' - min] < 0) {
                offset++;
                length--;
            }
        }
        long value = 0;
        for (int i = offset; i < offset + length; i++) {
            char ch = cs.charAt(i);
            byte code = encoding(ch);
            if (code == CharCoderBuilder.IGNORED)
                continue;
            value *= symbols.length;
            value += code;
        }
        return neg ? -value : value;
    }

    @Override
    public void appendLong(StringBuilder sb, long value) {
        if (signed && value < 0) {
            sb.append('-');
            value = -value; // -Long.MIN_VALUE == Long.MIN_VALUE
        }
        int start = sb.length();
        if (value < 0) {
            long v1 = value >>> 32;
            long d1 = v1 / base;
            long m1 = v1 % base;
            long v0 = (m1 << 32) + (value & 0xFFFF_FFFFL);
            long d0 = v0 / base;
            long m0 = v0 % base;
            value = (d1 << 32) + d0;
            sb.append(symbols[(int) m0]);
        }
        int base = symbols.length;
        do {
            long val2 = value / base;
            int sym2 = (int) (value % base);
            sb.append(symbols[sym2]);
            value = val2;
        } while (value > 0);
        reverse(sb, start);
    }

    @Override
    public boolean signed() {
        return signed;
    }
}
