package fzmm.zailer.me.client.gui.playerStatue;

import fzmm.zailer.me.client.gui.playerStatue.statueHeadSkin.HeadFace;
import fzmm.zailer.me.client.gui.playerStatue.statueHeadSkin.HeadModelSkin;
import fzmm.zailer.me.utils.position.PosF;

public enum StatuePartEnum {
    LEFT_ARM(StatuePartType.ARM, true, false),
    LEFT_BODY(StatuePartType.BODY, true, false),
    LEFT_HEAD_BACK(StatuePartType.HEAD, true, false),
    LEFT_HEAD_FRONT(StatuePartType.HEAD, true, true),
    LEFT_LEG(true),

    RIGHT_ARM(StatuePartType.ARM, false, false),
    RIGHT_BODY(StatuePartType.BODY, false, false),
    RIGHT_HEAD_BACK(StatuePartType.HEAD, false, false),
    RIGHT_HEAD_FRONT(StatuePartType.HEAD, false, true),
    RIGHT_LEG(false);

    private static final float STATUE_NORMAL_DISTANCE = 0.125f;
    private static final float STATUE_ARM_DISTANCE = STATUE_NORMAL_DISTANCE * 3;

    private final boolean isArm;
    private final boolean isLeft;
    private final boolean isFront;
    private final boolean isHead;
    private final HeadModelSkin defaultHeadModel;

    StatuePartEnum(StatuePartType type, boolean isLeft, boolean isFront) {
        this.isArm = type == StatuePartType.ARM;
        this.isLeft = isLeft;
        this.isFront = isFront;
        this.isHead = type == StatuePartType.HEAD;

        HeadModelSkin headModel = new HeadModelSkin();
        if (type != StatuePartType.BODY)
            headModel.add(this.isLeft ? HeadFace.HEAD_FACE.LEFT_FACE : HeadFace.HEAD_FACE.RIGHT_FACE);

        if (this.isHead)
            headModel.add(this.isFront ? HeadFace.HEAD_FACE.FRONT_FACE : HeadFace.HEAD_FACE.BACK_FACE);
        else
            headModel.add(HeadFace.HEAD_FACE.BACK_FACE, HeadFace.HEAD_FACE.FRONT_FACE);

        this.defaultHeadModel = headModel;
    }

    StatuePartEnum(boolean isLeft) {
        this(StatuePartType.LEG, isLeft, false);
    }

    public HeadModelSkin getDefaultHeadModel() {
        return this.defaultHeadModel;
    }

    private PosF getDistance() {
        float x;
        float y = 0f;

        if (this.isArm) {
            x = STATUE_ARM_DISTANCE;
        } else {
            x = STATUE_NORMAL_DISTANCE;
            if (this.isHead)
                y = STATUE_NORMAL_DISTANCE;
        }

        return new PosF(x, y);
    }

    public PosF getNorth() {
        PosF pos = this.getDistance();
        if (!this.isLeft)
            pos.invertX();

        if (this.isHead && this.isFront) {
            pos.invertY();
        }

        return pos;
    }

    public PosF getEast() {
        PosF pos = this.getDistance().swapValues();
        if (this.isHead && !this.isFront)
            pos.invertX();

        if (!this.isLeft)
            pos.invertY();

        return pos;
    }

    public PosF getSouth() {
        PosF pos = this.getDistance();
        if (this.isLeft)
            pos.invertX();

        if (this.isHead && !this.isFront)
            pos.invertY();

        return pos;
    }

    public PosF getWest() {
        PosF pos = this.getDistance().swapValues();
        if (this.isHead && this.isFront)
            pos.invertX();

        if (this.isLeft)
            pos.invertY();

        return pos;
    }

    private enum StatuePartType {
        ARM,
        BODY,
        HEAD,
        LEG
    }

    public static StatuePartEnum get(String statuePart) {
        try {
            return StatuePartEnum.valueOf(statuePart);
        } catch (Exception ignored) {
            return StatuePartEnum.LEFT_ARM;
        }
    }

}