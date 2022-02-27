package fzmm.zailer.me.client.guiLogic.playerStatue.statueHeadSkin;

import fzmm.zailer.me.utils.position.PosI;

public class BodySkinManager extends AbstractStatueSkinManager {
    private final int bodyHeight;
    private final boolean isLeft;

    public BodySkinManager(Height bodyHeight, boolean isLeft) {
        super(SkinPart.BODY);
        this.bodyHeight = bodyHeight.get();
        this.isLeft = isLeft;
    }

    @Override
    protected void setLeft(PosI pos) {
        pos.add(20, this.bodyHeight);
    }

    @Override
    protected void setRight(PosI pos) {
        pos.add(0, this.bodyHeight);
    }

    @Override
    protected void setFront(PosI pos) {
        pos.add(this.getSide(4), this.bodyHeight);
    }

    @Override
    protected void setBack(PosI pos) {
        pos.add(this.getSide(!this.isLeft, 16), this.bodyHeight);
    }

    @Override
    protected void setUp(PosI pos) {
        pos.add(this.getSide(4), 0);
    }

    @Override
    protected void setBottom(PosI pos) {
        pos.add(this.getSide(12), 0);
    }

    private int getSide(int x) {
        return this.getSide(this.isLeft, x);
    }

    private int getSide(boolean isLeft, int x) {
        if (!isLeft)
            x += 4;
        return x;
    }

}
