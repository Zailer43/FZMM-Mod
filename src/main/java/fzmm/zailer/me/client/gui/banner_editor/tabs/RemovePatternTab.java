package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.nbt.NbtElement;
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
                                          @Nullable NbtElement pattern,
                                          BannerBuilder currentBanner, DyeColor selectedColor) {
        if (pattern == null) {
            return;
        }

        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playButtonSound();
            clipboard.addUndo(currentBanner);

            currentBanner.removePattern(pattern);

            clipboard.change(currentBanner);
            return true;
        });
    }
}
