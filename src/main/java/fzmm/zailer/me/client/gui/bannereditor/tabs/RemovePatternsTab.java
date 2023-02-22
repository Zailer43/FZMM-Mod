package fzmm.zailer.me.client.gui.bannereditor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.bannereditor.BannerEditorScreen;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;

public class RemovePatternsTab extends AbstractModifyPatternsTab {

    private static final String PATTERNS_LAYOUT = "remove-patterns-layout";
    @Override
    public String getId() {
        return "removePatterns";
    }

    @Override
    protected String getGridId() {
        return PATTERNS_LAYOUT;
    }

    @Override
    public boolean shouldAddBaseColor() {
        return false;
    }

    @Override
    protected void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent, NbtElement pattern, BannerBuilder currentBanner, DyeColor color) {
        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playButtonSound();
            currentBanner.removePattern(pattern);

            parent.updatePreview(currentBanner);
            return true;
        });
    }
}
