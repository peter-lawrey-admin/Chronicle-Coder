package net.openhft.chronicle.coder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class WordsCoderBuilder {

    private final String[] symbols;
    private String seperator = ".";
    private String sepRegex = "\\.";

    private WordsCoderBuilder(String[] symbols) {
        this.symbols = symbols;
    }

    public static WordsCoderBuilder fromFile(String fileName, int wordCount) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is == null)
            is = new FileInputStream(fileName);
        return from(fileName, is, wordCount);
    }

    public static WordsCoderBuilder from(String description, InputStream is, int wordCount) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String[] words = new String[wordCount];
            for (int i = 0; i < wordCount; i++) {
                if ((words[i] = br.readLine()) == null)
                    throw new IOException("Only found " + i + " of " + wordCount + " words in " + description);
            }
            return new WordsCoderBuilder(words);
        }
    }

    public WordsCoderBuilder seperator(String seperator) {
        this.seperator = seperator;
        this.sepRegex = Pattern.quote(seperator);
        return this;
    }

    public WordsCoder build() {
        return new WordsCoder(symbols, seperator, sepRegex);
    }
}
