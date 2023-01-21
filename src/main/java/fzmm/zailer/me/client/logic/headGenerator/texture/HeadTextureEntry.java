package fzmm.zailer.me.client.logic.headGenerator.texture;

import fzmm.zailer.me.client.gui.headgenerator.HeadGenerationMethod;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;

import java.awt.image.BufferedImage;

public class HeadTextureEntry extends AbstractHeadEntry {

    private final BufferedImage headSkin;

    /**
     * @param headSkin the skin of the head, this is where the hat, glasses, beard, hair or whatever is,
     *                should not be confused with the base skin (the one to which this skin is applied on top)
     */
    public HeadTextureEntry(BufferedImage headSkin, String displayName, String key) {
        super(displayName, key);
        this.headSkin = headSkin;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin, boolean overlapHatLayer) {
        return new HeadGenerator(baseSkin, overlapHatLayer).addTexture(this.headSkin).getHeadTexture();
    }

    @Override
    public boolean canOverlap() {
        return true;
    }

    @Override
    public HeadGenerationMethod getGenerationMethod() {
        return HeadGenerationMethod.TEXTURE;
    }

    public BufferedImage getHeadSkin() {
        return this.headSkin;
    }
}
