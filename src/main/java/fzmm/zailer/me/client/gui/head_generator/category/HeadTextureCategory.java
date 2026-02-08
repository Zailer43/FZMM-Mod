package fzmm.zailer.me.client.gui.head_generator.category;

import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class HeadTextureCategory implements IHeadCategory {
    public static final String CATEGORY_ID = "texture";

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.category.texture";
    }

    @Override
    public boolean isCategory(AbstractHeadEntry entry, String categoryId) {
        return categoryId.equals(CATEGORY_ID);
    }

    @Override
    public Component getText() {
        return Component.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(0x5FD926));
    }

    @Override
    public boolean isModel() {
        return false;
    }

}
