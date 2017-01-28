package com.rw.dencryp;


import com.rw.dencryp.crypt.DecryptionUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TextTest {
    @Test
    public void test() {
        String text = "hello, darkness   my old friend.\nI've   come to you again\r\nasd\n 123123";
        String[] words = text.split("\\s+");
        String[] separators = DecryptionUtil.getSeparators(text);
        System.out.println(Arrays.toString(words));
        System.out.println(Arrays.toString(separators));

        System.out.println(separators.length);
        Assert.assertNotEquals(words, separators);
    }
}
