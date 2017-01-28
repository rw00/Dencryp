package com.rw.dencryp.crypt;

import com.rw.dencryp.DencrypApplication;

public class DecryptionUtil {
    public static String decrypt(String text) {
        text = text.trim();
        String[] words = text.split("\\s+");
        String[] separators = getSeparators(text);
        String decrypted = "";

        int k = 0;
        for (String word : words) {
            try {
                String s = DencrypApplication.getCryptor().decrypt(word);
                if (s == null) {
                    decrypted += word;
                } else {
                    decrypted += s;
                }
            } catch (Exception ex) {
                decrypted += word;
            }
            if (k < separators.length) {
                decrypted += separators[k++];
            }
        }
        return decrypted;
    }

    public static String[] getSeparators(String text) {
        return text.split("[\\w=-]+");
    }
}
