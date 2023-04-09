package fzmm.zailer.me.client.gui.headgenerator.category;

import net.minecraft.text.Text;

public interface IHeadCategory {
    IHeadCategory[] NATURAL_CATEGORIES = new IHeadCategory[] {
            new HeadAllCategory(),
            new HeadTextureCategory(),
            new HeadModelCategory(),
            new HeadPaintableCategory()
    };

    IHeadCategory COMPOUND_CATEGORY = new HeadCompoundCategory();

    String getTranslationKey();

    boolean isCategory(String id);

    Text getText();

    static IHeadCategory getCategory(String id) {
        for (int i = 1; i != NATURAL_CATEGORIES.length; i++) {
            if (NATURAL_CATEGORIES[i].isCategory(id)) {
                return NATURAL_CATEGORIES[i];
            }
        }

        return NATURAL_CATEGORIES[0];
    }
}
