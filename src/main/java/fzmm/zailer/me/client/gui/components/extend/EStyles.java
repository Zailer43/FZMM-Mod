package fzmm.zailer.me.client.gui.components.extend;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;

public class EStyles {

    public static final Color TEXT_ERROR_COLOR = Color.ofRgb(0xD83F27);
    public static final Color TEXT_SUCCESS_COLOR = Color.ofRgb(0x4CD827);

    //TODO: high-contrast version?
    // with green-blind/deuteranopia or red-blind/protanopia there seems to be difficulty in distinguishing
    public static final Color ALERT_SUCCESS_COLOR = Color.ofArgb(0xC86ABE30);
    public static final Color ALERT_ERROR_COLOR = Color.ofArgb(0xC87C2828);
    public static final Color ALERT_LOADING_COLOR = Color.ofArgb(0xC89BADB7);
    public static final Color ALERT_WARNING_COLOR = Color.ofArgb(0xC8DF7126);
    public static final Color ALERT_TIP_COLOR = Color.ofArgb(0xC82687DF);

    public static final int UNSELECTED_COLOR = 0x40000000;
    public static final int SELECTED_COLOR = 0x70000000;

    public static final Surface DEFAULT_HOVERED = (context, component) -> context.fill(component.x(), component.y(),
            component.x() + component.width(),
            component.y() + component.height(),
            component.zIndex(), 0x40000000
    );

    public static final ButtonComponent.Renderer DEFAULT_FLAT_BUTTON = ButtonComponent.Renderer
            .flat(0x00000000, UNSELECTED_COLOR, SELECTED_COLOR);
}
