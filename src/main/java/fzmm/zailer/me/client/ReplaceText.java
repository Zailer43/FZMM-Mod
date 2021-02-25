package fzmm.zailer.me.client;

public class ReplaceText {

    public static String replace (String msg) {

        for (String[] text : texts) msg = msg.replaceAll(text[0], text[1]);
        return msg;
    }

    public static String[][] texts = {
            {"::shrug::", "¯\\\\_(ツ)_/¯"},
            {"::tableflip::", "(╯°□°）╯︵ ┻━┻"},
            {"::unflip::", "┬─┬ ノ( ゜-゜ノ)"},
            {"::magic::", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧"},
            {"::party::", "(⌐■_■)ノ♪"},
            {"::bye::", "(^▽^)┛"},
            {"::pico::", "⛏"},
            {"::pvp::", "⚔"},
            {"::<::", "«"},
            {"::>::", "»"},
            {"::arriba::", "↑"},
            {"::derecha::", "→"},
            {"::abajo::", "↓"},
            {"::izquierda::", "←"},
            {"::box0::", "☐"},
            {"::box1::", "☑"},
            {"::box2::", "☒"},
            {"::!::", "⚠"},
            {"::feliz::", "☻"}
    };
}