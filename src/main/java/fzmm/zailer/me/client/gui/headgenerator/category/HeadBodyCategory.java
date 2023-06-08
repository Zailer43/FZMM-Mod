package fzmm.zailer.me.client.gui.headgenerator.category;

import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class HeadBodyCategory implements IHeadCategory {
//    public static final String CATEGORY_ID = "body";

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.category.body";
    }

    @Override
    public boolean isCategory(AbstractHeadEntry entry, String categoryId) {
        return entry.isEditingSkinBody();
    }

    @Override
    public Text getText() {
        return Text.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(0xC209F6));
    }

}
