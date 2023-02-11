package fzmm.zailer.me.client.gui.bannereditor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.bannereditor.BannerEditorScreen;
import io.wispforest.owo.ui.component.ItemComponent;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;

public class RemovePatternsTab extends AbstractModifyPatternsTab {

    private static final String PATTERNS_GRID = "remove-patterns-grid";
    @Override
    public String getId() {
        return "removePatterns";
    }

    @Override
    protected String getGridId() {
        return PATTERNS_GRID;
    }

    @Override
    public boolean shouldAddBaseColor() {
        return false;
    }

    @Override
    protected void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent, NbtElement pattern, BannerBuilder currentBanner, DyeColor color) {
        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            currentBanner.removePattern(pattern);

            parent.updatePreview(currentBanner);
            return true;
        });
    }
}
