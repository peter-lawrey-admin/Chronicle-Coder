package net.openhft.chronicle.coder;

public abstract class AbstractCharEncoder implements Coder {
    protected final char[] symbols;
    protected final byte[] encoding;
    protected final int min;

    protected AbstractCharEncoder(char[] symbols, byte[] encoding, int min) {
        this.symbols = symbols;
        this.encoding = encoding;
        this.min = min;
    }

    protected static void reverse(StringBuilder sb, int start) {
        int end = sb.length() - 1;
        for (; start < end; start++, end--) {
            char tmp = sb.charAt(start);
            sb.setCharAt(start, sb.charAt(end));
            sb.setCharAt(end, tmp);
        }
    }

    static int sign(double d) {
        return (int) (Double.doubleToRawLongBits(d) >>> 63);
    }

    @Override
    public void appendNormalisedLatLon(StringBuilder sb, double latitude, double longitude, double precision) {
        int lats = sign(latitude);
        int lons = sign(longitude);
        double lata = Math.abs(latitude);
        double lona = Math.abs(longitude);
        final int factor = symbols.length;
        {
            lata *= factor / 2;
            lona *= factor / 2;
            int lati = (int) Math.floor(lata);
            int loni = (int) Math.floor(lona);
            lata -= lati;
            lona -= loni;

            lati = lati * 2 + lats;
            loni = loni * 2 + lons;
            sb.append(symbols[lati]);
            sb.append(symbols[loni]);
            precision *= factor;
        }

        while (precision < 1) {
            lata *= factor;
            lona *= factor;
            int lati = (int) Math.floor(lata);
            int loni = (int) Math.floor(lona);
            lata -= lati;
            lona -= loni;
            sb.append(symbols[lati]);
            sb.append(symbols[loni]);
            precision *= factor;
        }
    }

    protected byte encoding(char ch) {
        byte code = ch < min || ch >= min + encoding.length
                ? CharCoderBuilder.UNSET
                : encoding[ch - min];
        if (code == CharCoderBuilder.UNSET)
            throw new IllegalArgumentException("Unexpected character '" + ch + "'");
        return code;
    }

    @Override
    public LatLon parseNormalisedLatLon(CharSequence cs, int offset, int length) {
        long lat = 0, lon = 0, precision = 1;
        int end = offset + length;
        int base = symbols.length;
        int lats = 0, lons = 0;
        boolean first = true;
        while (offset < end) {
            int code1;
            do {
                char ch1 = cs.charAt(offset++);
                code1 = encoding(ch1);
            } while (code1 < 0);
            int code2;
            do {
                if (offset >= end)
                    throw new IllegalArgumentException("not enough text");
                char ch2 = cs.charAt(offset++);
                code2 = encoding(ch2);
            } while (code2 < 0);

            if (first) {
                lat = code1 >> 1;
                lon = code2 >> 1;
                lats = code1 & 1;
                lons = code2 & 1;
                precision = base / 2;
                first = false;
            } else {
                lat *= base;
                lat += code1;
                lon *= base;
                lon += code2;
                precision *= base;
            }
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
