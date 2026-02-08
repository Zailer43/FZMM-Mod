package fzmm.zailer.me.client.gui.head_generator.category;

import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class HeadBodyCategory implements IHeadCategory {

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.category.body";
    }

    @Override
    public boolean isCategory(AbstractHeadEntry entry, String categoryId) {
        return entry.isEditingSkinBody();
    }

    @Override
    public Component getText() {
        return Component.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(0xC209F6));
    }

    @Override
    public boolean isModel() {
        return false;
    }

}
