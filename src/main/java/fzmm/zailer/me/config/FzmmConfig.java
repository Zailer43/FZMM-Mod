package fzmm.zailer.me.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.Arrays;
import java.util.List;

@Config(name = "fzmm")
public class FzmmConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("replaceTexts")
    @ConfigEntry.Gui.TransitiveObject
    public ReplaceTexts replaceTexts = new ReplaceTexts();

    public static class General {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public boolean toggleFont = false;
        public boolean forceInvisibleItemFrame = false;
        public boolean disableNightVisionIfBlindness = false;
        public boolean textObfuscated = false;
    }

    public static class ReplaceTexts {
        public boolean enableReplaceText = true;
        public List<Pair> texts = Arrays.asList(
            new Pair("::shrug::", "¯\\\\_(ツ)_/¯"),
            new Pair("::tableflip::", "(╯°□°）╯︵ ┻━┻"),
            new Pair("::tableflipx2::", "┻━┻ ︵ ＼( °□° )／ ︵ ┻━┻"),
            new Pair("::unflip::", "┬─┬ ノ( ゜-゜ノ)"),
            new Pair("::magic::", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧"),
            new Pair("::party::", "(⌐■_■)ノ♪"),
            new Pair("::hi::", "(^Ｕ^)ノ ~Hi"),
            new Pair("::bye::", "(^▽^)┛"),
            new Pair("::zzz::", "(￣o￣) . z Z"),
            new Pair("::nuke::", "(Ｂ Ｏ Ｏ Ｏ Ｍ ！ ＼（〇_ｏ）／"),
            new Pair("::pico::", "⛏"),
            new Pair("::pvp::", "⚔"),
            new Pair("::<::", "«"),
            new Pair("::>::", "»"),
            new Pair("::arriba::", "↑"),
            new Pair("::derecha::", "→"),
            new Pair("::abajo::", "↓"),
            new Pair("::izquierda::", "←"),
            new Pair("::box0::", "☐"),
            new Pair("::box1::", "☑"),
            new Pair("::box2::", "☒"),
            new Pair("::!::", "⚠"),
            new Pair("::boom::", "✸"),
            new Pair("::heart::", "❤"),
            new Pair("::star::", "★"),
            new Pair("::happy::", "☻"),
            new Pair("::xz::", "::fzmm_x_round:: ::fzmm_z_round::"),
            new Pair("::xyz::", "::fzmm_x_round:: ::fzmm_y_round:: ::fzmm_z_round::"),
            new Pair("::xyzyp::", "::fzmm_x_round:: ::fzmm_y_round:: ::fzmm_z_round:: ::fzmm_yaw_round:: ::fzmm_pitch_round::"),
            new Pair("::uuid::", "::fzmm_uuid::"),
            new Pair("::item_name::", "::fzmm_item_name::")
        );
    }


    public static void init() {
        AutoConfig.register(FzmmConfig.class, GsonConfigSerializer::new);
    }

    public static FzmmConfig get() {
        return AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
    }

    public static class Pair {
        String original = "Original";
        String replace = "Replace";

        public Pair(String original, String replace) {
            this.original = original;
            this.replace = replace;
        }

        public Pair() {
        }

        public String getOriginal() {
            return original;
        }


        public String getReplace() {
            return replace;
        }
    }
}
