package fzmm.zailer.me.client.gui.headgenerator.category;

import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class HeadModelCategory implements IHeadCategory {
    public static final String CATEGORY_ID = "category";
    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.category.model";
    }

    @Override
    public boolean isCategory(AbstractHeadEntry entry, String categoryId) {
        return categoryId.equals(CATEGORY_ID);
    }

    @Override
    public Text getText() {
        return Text.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(0xC8375B));
    }
}
