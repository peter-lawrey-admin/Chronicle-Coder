package net.openhft.chronicle.coder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CharEncoderTest {

    @Test
    public void parseLatLon() {
        Coder coder = new CharCoderBuilder("0123456789ABCDEF").build();
        for (int x = -90; x <= 90; x++)
            for (int y = -180; y <= 180; y++) {
                StringBuilder sb = new StringBuilder();
                coder.appendLatLon(sb, x, y, 1e-4);
//                System.out.println("x: " + x + ", y: " + y + " " + sb);
                Coder.LatLon latLon = coder.parseLatLon(sb);
                assertEquals(x, latLon.latitude, 0.5e-4);
                assertEquals(y, latLon.longitude, 0.5e-4);
                assertTrue(latLon.precision >= 1e-4 / 16);
                assertTrue(latLon.precision <= 1e-4);
            }
    }

    @Test
    public void parseLongDecimal() {
        Coder coder = new CharCoderBuilder("0123456789").signed(true).build();
        StringBuilder sb = new StringBuilder();
        for (long l : new long[]{
                Long.MIN_VALUE, -Long.MAX_VALUE, Integer.MIN_VALUE, -1,
                0, 1, Integer.MAX_VALUE, Long.MAX_VALUE}) {
            assertEquals(Long.toString(l), coder.asString(l));
            sb.setLength(0);
            coder.appendLong(sb, l);
            assertEquals(l, coder.parseLong(sb));
        }
    }

    @Test
    public void parseLongHexadecimal() {
        Coder coder = new CharCoderBuilder("0123456789ABCDEF")
                .addAlias('a', 'A')
                .addAlias('b', 'B')
                .addAlias('c', 'C')
                .addAlias('d', 'D')
                .addAlias('e', 'E')
                .addAlias('f', 'F')
                .addAlias('O', '0')
                .addAlias('o', '0')
                .addAlias('l', '1')
                .addAlias('L', '1')
                .build();
        StringBuilder sb = new StringBuilder();
        for (long l : new long[]{
                Long.MIN_VALUE, -1234567890123456789L, -Long.MAX_VALUE, Integer.MIN_VALUE, -1,
                0, 1, Integer.MAX_VALUE, 1234567890123456789L, Long.MAX_VALUE}) {
            assertEquals(Long.toHexString(l).toUpperCase(), coder.asString(l));
            sb.setLength(0);
            coder.appendLong(sb, l);
            assertEquals(l, coder.parseLong(sb));
            assertEquals(l, coder.parseLong(Long.toHexString(l)));
        }
        assertEquals(0xf101, coder.parseLong("flol"));
    }

    @Test
    public void testBase85() {
        String symbols = "0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz\\\"#$%&'()*+,-./";
        Coder coder = new CharCoderBuilder(symbols).build();
        StringBuilder sb = new StringBuilder();
        for (long l : new long[]{
                Long.MIN_VALUE, -1234567890123456789L, -Long.MAX_VALUE, Integer.MIN_VALUE, -1,
                0, 1, Integer.MAX_VALUE, 1234567890123456789L, Long.MAX_VALUE}) {
            sb.setLength(0);
            coder.appendLong(sb, l);
            assertEquals(l, coder.parseLong(sb));
        }
    }

    @Test
    public void testBase64() {
        final char[] toBase64 = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        };
        String symbols = new String(toBase64);
        Coder coder = new CharCoderBuilder(symbols).build();
        StringBuilder sb = new StringBuilder();
        for (long l : new long[]{
                Long.MIN_VALUE, -1234567890123456789L, -Long.MAX_VALUE, Integer.MIN_VALUE, -1,
                0, 1, Integer.MAX_VALUE, 1234567890123456789L, Long.MAX_VALUE}) {
            sb.setLength(0);
            coder.appendLong(sb, l);
            assertEquals(l, coder.parseLong(sb));
        }
    }
}