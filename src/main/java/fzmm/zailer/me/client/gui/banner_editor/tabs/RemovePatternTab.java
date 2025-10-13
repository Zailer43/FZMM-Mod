package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public class RemovePatternTab extends AbstractModifyPatternTab {

    @Override
    public String buttonId() {
        return "remove-pattern";
    }

    @Override
    public boolean shouldAddBase() {
        return false;
    }

    @Override
    protected void onItemComponentCreated(HistoryClipboard clipboard, ItemComponent itemComponent,
                                          @Nullable BannerPatternsComponent.Layer componentLayer,
                                          BannerBuilder currentBanner, DyeColor selectedColor) {
        if (componentLayer == null) {
            return;
        }

        itemComponent.mouseDown().subscribe((input, doubled) -> {
            UISounds.playButtonSound();
            clipboard.addUndo(currentBanner);

            currentBanner.removeLayer(componentLayer);

            clipboard.change(currentBanner);
            return true;
        });
    }
}
