package net.openhft.chronicle.coder;

import java.math.BigInteger;

public class CharEncoder extends AbstractCharEncoder {
    static final BigInteger TWO_2_64 = BigInteger.valueOf(1).shiftLeft(64);
    private final BigInteger base;
    private final boolean signed;

    public CharEncoder(char[] symbols, byte[] encoding, int min, boolean signed) {
        super(symbols, encoding, min);
        this.base = BigInteger.valueOf(symbols.length);
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
    public byte[] parseBytes(CharSequence cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendLong(StringBuilder sb, long value) {
        if (signed && value < 0) {
            sb.append('-');
            value = -value; // -Long.MIN_VALUE == Long.MIN_VALUE
        }
        int start = sb.length();
        if (value < 0) {
            BigInteger bi = TWO_2_64.add(BigInteger.valueOf(value));
            BigInteger[] divMod = bi.divideAndRemainder(base);
            value = divMod[0].longValue();
            sb.append(symbols[divMod[1].intValueExact()]);
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
    public void appendBytes(StringBuilder sb, byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean signed() {
        return signed;
    }
}
