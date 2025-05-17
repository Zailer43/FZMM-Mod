package fzmm.zailer.me.client.logic.head_generator.texture;

import fzmm.zailer.me.client.gui.head_generator.category.HeadTextureCategory;
import fzmm.zailer.me.client.gui.head_generator.options.ISkinPreEdit;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SkinPart;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HeadTextureEntry extends AbstractHeadEntry {

    private final BufferedImage headSkin;
    private final boolean isEditingSkinBody;

    /**
     * @param headSkin the skin of the head, this is where the hat, glasses, beard, hair or whatever is,
     *                 should not be confused with the base skin (the one to which this skin is applied on top)
     */
    public HeadTextureEntry(BufferedImage headSkin, String path) {
        super(path);
        this.headSkin = headSkin;
        this.isEditingSkinBody = this.calculateIsEditingSkinBody();
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin, boolean hasUnusedPixels) {
        ISkinPreEdit noneEdit = SkinPreEditOption.NONE.getPreEdit();
        BufferedImage result = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        noneEdit.apply(graphics, baseSkin);

        // fixes skin formatting inconsistencies
        if (this.isEditingSkinBody() && (ImageUtils.isSlimSimpleCheck(baseSkin) != ImageUtils.isSlimFullCheck(this.headSkin))) {
            HeadModelEntry formatUpdater = ImageUtils.isSlimSimpleCheck(baseSkin) ?
                    InternalModels.WIDE_TO_SLIM : InternalModels.SLIM_TO_WIDE;

            BufferedImage adaptedHeadSkin = formatUpdater.getHeadSkin(this.headSkin, false);
            noneEdit.apply(graphics, adaptedHeadSkin);
            adaptedHeadSkin.flush();
        } else {
            noneEdit.apply(graphics, this.headSkin);
        }

        if (hasUnusedPixels) {
            ImageUtils.copyUnusedPixels(baseSkin, graphics);
        }

        graphics.dispose();

        return result;
    }

    @Override
    public String getCategoryId() {
        return HeadTextureCategory.CATEGORY_ID;
    }

    @Override
    public boolean isEditingSkinBody() {
        return this.isEditingSkinBody;
    }

    @Override
    public boolean isFirstResult() {
        return false;
    }

    private boolean calculateIsEditingSkinBody() {
        if (this.headSkin.getWidth() != SkinPart.MAX_WIDTH || this.headSkin.getHeight() != SkinPart.MAX_HEIGHT) {
            return false;
        }

        for (SkinPart part : SkinPart.BODY_PARTS) {
            byte[][] usedAreas = part.usedAreas();
            for (var rect : usedAreas) {
                if (this.calculateIsEditingSkinBody(rect[0], rect[1], rect[2], rect[3])) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean calculateIsEditingSkinBody(int x, int y, int x2, int y2) {
        for (int i = x; i < x2; i++) {
            for (int j = y; j < y2; j++) {
                if (ImageUtils.hasPixel(j, i, this.headSkin)) {
                    return true;
                }
            }
        }
        return false;
    }
}
