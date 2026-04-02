/*
package me.zailer.testmod.client.unit_test;

import fzmm.zailer.me.utils.TextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class TextUtilsTest {

    @Test
    void split() {
        // test string, number of multibyte characters
        var testStr = Map.of(
                "This is a test string", 0,
                "ñ", 0,
                "|@#~½¬{[]}\\¸æßðđŋħ̉ĸł~{}«»¢„“”µ•·̣", 0,
                "Test \uD83D\uDC7B", 1,
                "🗡🎣🔱", 3,
                "123  🗡🎣🔱 abc", 3,
                "invalid pair \uD83D", 0,
                "invalid pair \uD83D\uD83D abc", 0
        );

        for (var test : testStr.keySet()) {
            Assertions.assertEquals(test, String.join("", (TextUtils.splitMessage(test))));
            Assertions.assertEquals(test.length() - testStr.get(test), TextUtils.splitMessage(test).size());
        }
    }

    @Test
    void unpairedMultibyte() {
        var testStr = List.of(
                "\uD83D\uD83D",
                "\uD8FF",
                "\uD800",
                "string\uD83C",
                "\uD83Dstring",
                "🗡".split("")[0]
        );

        for (var test : testStr) {
            Assertions.assertNotEquals(test, TextUtils.removeUnpairedMultibyte(test));
        }
    }

    @Test
    void pairedMultibyte() {
        var testStr = List.of(
                "I'm lazy to search more characters 🗡🎣🔱",
                "I don't know what write →← \uD83D\uDDE1\uD83C\uDFA3[asd]\uD83D\uDD31",
                "\uD8FF\uDCFF",
                "\uD880\uDC80",
                "🗡",
                "|@#~½¬{[]}\\¸æßðđŋħ̉ĸł~{}«»¢„“”µ•·̣",
                "abc123",
                "\uD83D\uDC7BMultibyte or not multibyte, that's the question\uD83D\uDC7B"
        );

        for (var test : testStr) {
            Assertions.assertEquals(test, TextUtils.removeUnpairedMultibyte(test));
        }
    }
}
*/
