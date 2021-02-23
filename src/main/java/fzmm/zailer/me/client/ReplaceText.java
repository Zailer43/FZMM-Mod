package fzmm.zailer.me.client;

public class ReplaceText {
    public static String replace(String msg) {
        msg = msg.replaceAll("::shrug::", "¯\\\\_(ツ)_/¯");
        msg = msg.replaceAll("::tableflip::", "(╯°□°）╯︵ ┻━┻");
        msg = msg.replaceAll("::unflip::", "┬─┬ ノ( ゜-゜ノ)");
        msg = msg.replaceAll("::magic::", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
        msg = msg.replaceAll("::pico::", "⛏");
        msg = msg.replaceAll("::pvp::", "⚔");
        msg = msg.replaceAll("::<::", "«");
        msg = msg.replaceAll("::>::", "»");
        msg = msg.replaceAll("::arriba::", "↑");
        msg = msg.replaceAll("::derecha::", "→");
        msg = msg.replaceAll("::abajo::", "↓");
        msg = msg.replaceAll("::izquierda::", "←");
        msg = msg.replaceAll("::box0::", "☐");
        msg = msg.replaceAll("::box1::", "☑");
        msg = msg.replaceAll("::box2::", "☒");
        msg = msg.replaceAll("::!::", "⚠");
        msg = msg.replaceAll(":::\\)::", "☻");

        return msg;
    }
}