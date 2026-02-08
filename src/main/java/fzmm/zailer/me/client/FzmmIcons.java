package fzmm.zailer.me.client;

import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.resources.Identifier;

@SuppressWarnings("UnusedAssignment")
public class FzmmIcons {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "textures/gui/widgets.png");

    // Main Screen, Icon don't have U and V getter :(
    // U and V are needed for texture in 32x32 instead of 16x16
    public static final int[] IMAGETEXT;
    public static final int[] TEXT_FORMAT;
    public static final int[] PLAYER_STATUE;
    public static final int[] ENCRYPT_BOOK;
    public static final int[] HEAD_GENERATOR;
    public static final int[] CONVERTERS;
    public static final int[] HISTORY;
    public static final int[] HEAD_GALLERY;
    public static final int[] BANNER_EDITOR;
    public static final int[] NBT_EDITOR;

    // General
    public static final Icon PINNED;

    // Head Generator
    public static final Icon ROTATE_IN_X_POS;
    public static final Icon ROTATE_IN_Y_POS;
    public static final Icon ROTATE_IN_Z_POS;
    public static final Icon ROTATE_IN_X_NEG;
    public static final Icon ROTATE_IN_Y_NEG;
    public static final Icon ROTATE_IN_Z_NEG;

    public static final Icon MODEL_SLIM;
    public static final Icon MODEL_WIDE;

    public static final Icon PRE_EDIT_NONE;
    public static final Icon PRE_EDIT_OVERLAP;
    public static final Icon PRE_EDIT_REMOVE;

    private static Icon of(int u, int v) {
        return Icon.of(TEXTURE, u, v, 256, 256);
    }

    static {
        int u = 16;
        int v = -16;
        // second row are main screen icons
        IMAGETEXT = new int[]{u, v += 16};
        TEXT_FORMAT = new int[]{u, v += 16};
        PLAYER_STATUE = new int[]{u, v += 16};
        ENCRYPT_BOOK = new int[]{u, v += 16};
        HEAD_GENERATOR = new int[]{u, v += 16};
        CONVERTERS = new int[]{u, v += 16};
        HISTORY = new int[]{u, v += 16};
        HEAD_GALLERY = new int[]{u, v += 16};
        BANNER_EDITOR = new int[]{u, v += 16};
        NBT_EDITOR = new int[]{u, v += 16};

        u += 16;
        v = -16;

        // third row are general icons
        PINNED = of(u, v += 16);

        u += 16;
        v = -16;

        // fourth row are head generator icons
        PRE_EDIT_NONE = of(u, v += 16);
        PRE_EDIT_OVERLAP = of(u, v += 16);
        PRE_EDIT_REMOVE = of(u, v += 16);

        u += 16;
        v = -16;

        // fifth row are head generator icons x2
        ROTATE_IN_X_POS = of(u, v += 16);
        ROTATE_IN_Y_POS = of(u, v += 16);
        ROTATE_IN_Z_POS = of(u, v += 16);
        ROTATE_IN_X_NEG = of(u, v += 16);
        ROTATE_IN_Y_NEG = of(u, v += 16);
        ROTATE_IN_Z_NEG = of(u, v += 16);

        MODEL_SLIM = of(u, v += 16);
        MODEL_WIDE = of(u, v += 16);
    }
}