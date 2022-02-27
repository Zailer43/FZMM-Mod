package fzmm.zailer.me.client.guiLogic.playerStatue.statueHeadSkin;

import fzmm.zailer.me.utils.position.PosI;

public class ExtremitySkinManager extends AbstractStatueSkinManager {
    private final int extremityHeight;

    public ExtremitySkinManager(SkinPart skinPart, Height extremityHeight) {
        super(skinPart);
        this.extremityHeight = extremityHeight.get();
    }

    @Override
    protected void setLeft(PosI pos) {
        pos.add(0, this.extremityHeight);
    }

    @Override
    protected void setRight(PosI pos) {
        pos.add(8, this.extremityHeight);
    }

    @Override
    protected void setFront(PosI pos) {
        pos.add(4, this.extremityHeight);
    }

    @Override
    protected void setBack(PosI pos) {
        pos.add(12, this.extremityHeight);
    }

    @Override
    protected void setUp(PosI pos) {
        pos.add(4, 0);
    }

    @Override
    protected void setBottom(PosI pos) {
        pos.add(8, 0);
    }

}
