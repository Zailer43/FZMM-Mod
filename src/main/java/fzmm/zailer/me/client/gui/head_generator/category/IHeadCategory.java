package fzmm.zailer.me.client.gui.head_generator.category;

import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import net.minecraft.network.chat.Component;

import java.io.Serializable;

public interface IHeadCategory extends Serializable {
    IHeadCategory[] NATURAL_CATEGORIES = new IHeadCategory[] {
            new HeadAllCategory(),
            new HeadTextureCategory(),
            new HeadModelCategory(),
            new HeadPaintableCategory(),
            new HeadBodyCategory()
    };

    IHeadCategory COMPOUND_CATEGORY = new HeadCompoundCategory();

    String getTranslationKey();

    boolean isCategory(AbstractHeadEntry entry, String categoryId);

    Component getText();

    boolean isModel();

    static IHeadCategory getCategory(AbstractHeadEntry entry, String id) {
        for (int i = 1; i != NATURAL_CATEGORIES.length; i++) {
            if (NATURAL_CATEGORIES[i].isCategory(entry, id)) {
                return NATURAL_CATEGORIES[i];
            }
        }

        return NATURAL_CATEGORIES[0];
    }
}
