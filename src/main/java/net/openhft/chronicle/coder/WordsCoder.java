package net.openhft.chronicle.coder;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static net.openhft.chronicle.coder.AbstractCharEncoder.sign;
import static net.openhft.chronicle.coder.CharEncoder.TWO_2_64;

public class WordsCoder implements Coder {
    private final String[] symbols;
    private final String sep;
    private final Pattern sepPattern;
    private final Map<String, Integer> symbolMap = new LinkedHashMap<>();
    private final BigInteger base;
    private final int base2;

    public WordsCoder(String[] symbols, String sep, String sepRegex) {
        this.symbols = symbols;
        this.sep = sep;
        for (int i = 0; i < symbols.length; i++) {
            assert symbols[i] != null;
            Integer last = symbolMap.put(symbols[i], i);
            assert last == null : "Duplicate symbol '" + symbols[i] + "'";
        }
        sepPattern = Pattern.compile(sepRegex);
        this.base = BigInteger.valueOf(symbols.length);
        base2 = ((int) Math.sqrt(symbols.length)) & ~1;
    }

    /**
     * Parse words written in least significant to most significant order (Little Endian)
     *
     * @param cs     to parse
     * @param offset start of text
     * @param length of text
     * @return as a long
     */
    @Override
    public long parseLong(CharSequence cs, int offset, int length) {
        String[] split = sepPattern.split(cs.subSequence(offset, offset + length), 0);
        long value = 0;
        int base = symbols.length;
        long factor = 1;
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            int symbol = lookupSymbol(s);
            value += factor * symbol;
            factor *= base;
        }
        return value;
    }

    int lookupSymbol(String s) {
        Integer symbol = symbolMap.get(s);
        if (symbol == null) {
            symbol = symbolMap.get(s.toLowerCase());
            if (symbol == null)
                throw new IllegalArgumentException("Unknown words '" + s + "'");
        }
        return symbol;
    }

    @Override
    public byte[] parseBytes(CharSequence cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendLong(StringBuilder sb, long value) {
        String sep = "";
        if (value < 0) {
            BigInteger bi = TWO_2_64.add(BigInteger.valueOf(value));
            BigInteger[] divMod = bi.divideAndRemainder(base);
            value = divMod[0].longValue();
            sb.append(symbols[divMod[1].intValueExact()]);
            sep = this.sep;
        }
        int base = symbols.length;
        do {
            long val2 = value / base;
            int sym2 = (int) (value % base);
            sb.append(sep).append(symbols[sym2]);
            value = val2;
            sep = this.sep;
        } while (value > 0);
    }

    @Override
    public void appendBytes(StringBuilder sb, byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendNormalisedLatLon(StringBuilder sb, double latitude, double longitude, double precision) {
        int lats = sign(latitude);
        int lons = sign(longitude);
        double lata = Math.abs(latitude);
        double lona = Math.abs(longitude);
        final int factor = base2;
        {
            lata *= factor / 2;
            lona *= factor / 2;
            int lati = (int) Math.floor(lata);
            int loni = (int) Math.floor(lona);
            lata -= lati;
            lona -= loni;

            lati = lati * 2 + lats;
            loni = loni * 2 + lons;
            int index = loni * base2 + lati;
            sb.append(symbols[index]);
            precision *= factor / 2;
        }

        while (precision < 1) {
            lata *= factor;
            lona *= factor;
            int lati = (int) Math.floor(lata);
            int loni = (int) Math.floor(lona);
            lata -= lati;
            lona -= loni;
            int index = loni * base2 + lati;
            sb.append(sep).append(symbols[index]);
            precision *= factor;
        }

    }


    @Override
    public LatLon parseNormalisedLatLon(CharSequence cs, int offset, int length) {
        String[] split = sepPattern.split(cs.subSequence(offset, offset + length), 0);

        int symbol0 = lookupSymbol(split[0]);
        long lat = symbol0 % base2;
        long lon = symbol0 / base2;
        int lats = (int) (lat & 1);
        int lons = (int) (lon & 1);
        lat >>>= 1;
        lon >>>= 1;
        long precision = base2 / 2;

        for (int i = 1; i < split.length; i++) {
            lat *= base2;
            lon *= base2;
            int symbol = lookupSymbol(split[i]);
            lat += symbol % base2;
            lon += symbol / base2;
            precision *= base2;
        }
        lat *= 2;
        lat++;
        lon *= 2;
        lon++;
        if (lats != 0)
            lat = -lat;
        if (lons != 0)
            lon = -lon;
        double dPrecision = precision * 2;
        return new LatLon(lat / dPrecision, lon / dPrecision, 1 / dPrecision);
    }
}
