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
        public List<String> texts = Arrays.asList(
                "::shrug::==¯\\\\_(ツ)_/¯",
                "::tableflip::==(╯°□°）╯︵ ┻━┻",
                "::tableflipx2::==┻━┻ ︵ ＼( °□° )／ ︵ ┻━┻",
                "::unflip::==┬─┬ ノ( ゜-゜ノ)",
                "::magic::==(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
                "::party::==(⌐■_■)ノ♪",
                "::hi::==(^Ｕ^)ノ ~Hi",
                "::bye::==(^▽^)┛",
                "::zzz::==(￣o￣) . z Z",
                "::nuke::==(Ｂ Ｏ Ｏ Ｏ Ｍ ！ ＼（〇_ｏ）／",
                "::pico::==⛏",
                "::pvp::==⚔",
                "::<::==«",
                "::>::==»",
                "::arriba::==↑",
                "::derecha::==→",
                "::abajo::==↓",
                "::izquierda::==←",
                "::box0::==☐",
                "::box1::==☑",
                "::box2::==☒",
                "::!::==⚠",
                "::boom::==✸",
                "::heart::==❤",
                "::star::==★",
                "::happy::==☻",
                "::xz::==::fzmm_x_round:: ::fzmm_z_round::",
                "::xyz::==::fzmm_x_round:: ::fzmm_y_round:: ::fzmm_z_round::",
                "::xyzyp::==::fzmm_x_round:: ::fzmm_y_round:: ::fzmm_z_round:: ::fzmm_yaw_round:: ::fzmm_pitch_round::",
                "::uuid::==::fzmm_uuid::",
                "::item_name::==::fzmm_item_name::"
        );
    }


    public static void init() {
        AutoConfig.register(FzmmConfig.class, GsonConfigSerializer::new);
    }

    public static FzmmConfig get() {
        return AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
    }
}
