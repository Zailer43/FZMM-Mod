package fzmm.zailer.me.client;

public class ToggleFont {
    //static final String[] NORMALFONT = ("wertyuiopasdfghjklzxcvbnm0123456789QWERTYUIOPASDFGHJKLZXCVBNM!$%&/()=?*-,.-")
    //                                   .split("");
    //static final String[] FINALFONT = ("ｑｗｅｒｔｙｕｉｏｐａｓｄｆｇｈｊｋｌｚｘｃｖｂｎｍ０１２３４５６７８９ＱＷＥＲＴＹＵＩＯＰＡＳＤＦＧＨＪＫＬＺＸＣＶＢＮＭ！＄％＆／（）＝？＊－，．－")
    //                                   .split("");

    public static String convert(String msg) {

        msg = msg.replaceAll("q", "ｑ");
        msg = msg.replaceAll("w", "ｗ");
        msg = msg.replaceAll("e", "ｅ");
        msg = msg.replaceAll("r", "ｒ");
        msg = msg.replaceAll("t", "ｔ");
        msg = msg.replaceAll("y", "ｙ");
        msg = msg.replaceAll("u", "ｕ");
        msg = msg.replaceAll("i", "ｉ");
        msg = msg.replaceAll("o", "ｏ");
        msg = msg.replaceAll("p", "ｐ");
        msg = msg.replaceAll("a", "ａ");
        msg = msg.replaceAll("s", "ｓ");
        msg = msg.replaceAll("d", "ｄ");
        msg = msg.replaceAll("f", "ｆ");
        msg = msg.replaceAll("g", "ｇ");
        msg = msg.replaceAll("h", "ｈ");
        msg = msg.replaceAll("j", "ｊ");
        msg = msg.replaceAll("k", "ｋ");
        msg = msg.replaceAll("l", "ｌ");
        msg = msg.replaceAll("z", "ｚ");
        msg = msg.replaceAll("x", "ｘ");
        msg = msg.replaceAll("c", "ｃ");
        msg = msg.replaceAll("v", "ｖ");
        msg = msg.replaceAll("b", "ｂ");
        msg = msg.replaceAll("n", "ｎ");
        msg = msg.replaceAll("m", "ｍ");
        msg = msg.replaceAll("0", "０");
        msg = msg.replaceAll("1", "１");
        msg = msg.replaceAll("2", "２");
        msg = msg.replaceAll("3", "３");
        msg = msg.replaceAll("4", "４");
        msg = msg.replaceAll("5", "５");
        msg = msg.replaceAll("6", "６");
        msg = msg.replaceAll("7", "７");
        msg = msg.replaceAll("8", "８");
        msg = msg.replaceAll("9", "９");
        msg = msg.replaceAll("Q", "Ｑ");
        msg = msg.replaceAll("W", "Ｗ");
        msg = msg.replaceAll("E", "Ｅ");
        msg = msg.replaceAll("R", "Ｒ");
        msg = msg.replaceAll("T", "Ｔ");
        msg = msg.replaceAll("Y", "Ｙ");
        msg = msg.replaceAll("U", "Ｕ");
        msg = msg.replaceAll("I", "Ｉ");
        msg = msg.replaceAll("O", "Ｏ");
        msg = msg.replaceAll("P", "Ｐ");
        msg = msg.replaceAll("A", "Ａ");
        msg = msg.replaceAll("S", "Ｓ");
        msg = msg.replaceAll("D", "Ｄ");
        msg = msg.replaceAll("F", "Ｆ");
        msg = msg.replaceAll("G", "Ｇ");
        msg = msg.replaceAll("H", "Ｈ");
        msg = msg.replaceAll("J", "Ｊ");
        msg = msg.replaceAll("K", "Ｋ");
        msg = msg.replaceAll("L", "Ｌ");
        msg = msg.replaceAll("Z", "Ｚ");
        msg = msg.replaceAll("X", "Ｘ");
        msg = msg.replaceAll("C", "Ｃ");
        msg = msg.replaceAll("V", "Ｖ");
        msg = msg.replaceAll("B", "Ｂ");
        msg = msg.replaceAll("N", "Ｎ");
        msg = msg.replaceAll("M", "Ｍ");
        msg = msg.replaceAll("!", "！");
        msg = msg.replaceAll("\\$", "＄");
        msg = msg.replaceAll("%", "％");
        msg = msg.replaceAll("&", "＆");
        msg = msg.replaceAll("/", "／");
        msg = msg.replaceAll("\\(", "（");
        msg = msg.replaceAll("\\)", "）");
        msg = msg.replaceAll("=", "＝");
        msg = msg.replaceAll("\\?", "？");
        msg = msg.replaceAll("\\*", "＊");
        msg = msg.replaceAll("-", "－");
        msg = msg.replaceAll(",", "，");
        msg = msg.replaceAll("\\.", "．");
        msg = msg.replaceAll("-", "－");

        return msg;
    }

}
