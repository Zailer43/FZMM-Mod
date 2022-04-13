package fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin;

import fzmm.zailer.me.utils.position.PosI;

public class HeadSkinManager extends AbstractStatueSkinManager {
    private static final short HALF_HEAD = 4;
    private final boolean isRight;
    private final boolean isBottom;
    private final boolean isBack;

    public HeadSkinManager(boolean isRight, boolean isBottom, boolean isBack) {
        super(SkinPart.HEAD);
        this.isRight = isRight;
        this.isBottom = isBottom;
        this.isBack = isBack;
    }

    @Override
    protected void setRight(PosI pos) {
        int addX = 16;
        int addY = 8;

        if (!this.isRight)
            return;

        if (this.isBack)
            addX += HALF_HEAD;

        if (this.isBottom)
            addY += HALF_HEAD;

        pos.add(addX, addY);
    }

    @Override
    protected void setLeft(PosI pos) {
        int addX = 0;
        int addY = 8;

        if (this.isRight)
            return;

        if (!this.isBack)
            addX += HALF_HEAD;

        if (this.isBottom)
            addY += HALF_HEAD;

        pos.add(addX, addY);
    }

    @Override
    protected void setFront(PosI pos) {
        int addX = 8;
        int addY = 8;

        if (this.isBack)
            return;

        if (this.isRight)
            addX += HALF_HEAD;

        if (this.isBottom)
            addY += HALF_HEAD;

        pos.add(addX, addY);
    }

    @Override
    protected void setBack(PosI pos) {
        int addX = 24;
        int addY = 8;

        if (!this.isBack)
            return;

        if (!this.isRight)
            addX += HALF_HEAD;

        if (this.isBottom)
            addY += HALF_HEAD;

        pos.add(addX, addY);
    }

    @Override
    protected void setUp(PosI pos) {
        int addX = 8;
        int addY = 0;

        if (this.isBottom)
            return;

        if (this.isRight)
            addX += HALF_HEAD;

        if (!this.isBack)
            addY += HALF_HEAD;

        pos.add(addX, addY);
    }

    @Override
    protected void setBottom(PosI pos) {
        int addX = 16;
        int addY = 0;

        if (!this.isBottom)
            return;

        if (this.isRight)
            addX += HALF_HEAD;

        if (!this.isBack)
            addY += HALF_HEAD;

        pos.add(addX, addY);
    }

}
