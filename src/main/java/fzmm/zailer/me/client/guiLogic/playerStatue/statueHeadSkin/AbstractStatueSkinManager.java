package fzmm.zailer.me.client.guiLogic.playerStatue.statueHeadSkin;

import fzmm.zailer.me.utils.position.PosI;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractStatueSkinManager {
    protected final SkinPart skinPart;

    protected AbstractStatueSkinManager(SkinPart skinPart) {
        this.skinPart = skinPart;
    }

    protected void draw(HeadFace.HEAD_FACE headFace, Graphics2D graphics, BufferedImage playerSkin) {
        PosI pos = this.skinPart.getNormalLayer();
        PosI hatPos = this.skinPart.getHatLayer();

        this.setPos(headFace, pos);
        headFace.get().draw(graphics, playerSkin, pos, false);
        this.setPos(headFace, hatPos);
        headFace.get().draw(graphics, playerSkin, hatPos, true);
    }

    private void setPos(HeadFace.HEAD_FACE headFace, PosI pos) {
        switch (headFace) {
            case LEFT_FACE -> this.setLeft(pos);
            case RIGHT_FACE -> this.setRight(pos);
            case FRONT_FACE -> this.setFront(pos);
            case BACK_FACE -> this.setBack(pos);
            case UP_FACE -> this.setUp(pos);
            case BOTTOM_FACE -> this.setBottom(pos);
        }
    }

    protected abstract void setLeft(PosI pos);

    protected abstract void setRight(PosI pos);

    protected abstract void setFront(PosI pos);

    protected abstract void setBack(PosI pos);

    protected abstract void setUp(PosI pos);

    protected abstract void setBottom(PosI pos);

    public enum Height {
        LOWER(12),
        MIDDLE(8),
        UPPER(4);

        final int height;

        Height(int height) {
            this.height = height;
        }

        int get() {
            return this.height;
        }
    }
}
