package fzmm.zailer.me.client.gui.playerStatue.statueHeadSkin;

import fzmm.zailer.me.utils.position.PosI;

import java.awt.*;
import java.awt.image.BufferedImage;

public record HeadFace(int x, int y) {
    private static final short HEAD_SIZE = 8;
    private static final short HAT_LAYER_X_DISTANCE = 32;

    public void draw(Graphics2D graphics, BufferedImage playerSkin, PosI source, boolean hatLayer) {
        int source2Distance = 4;
        int sourceX = source.getX();
        int sourceY = source.getY();
        int destinationX = this.x;

        if (hatLayer)
            destinationX += HAT_LAYER_X_DISTANCE;

        if (playerSkin.getWidth() == 128 && playerSkin.getHeight() == 128) {
            source2Distance *= 2;
            sourceX *= 2;
            sourceY *= 2;
        }

        graphics.drawImage(playerSkin, destinationX, this.y, destinationX + HEAD_SIZE, this.y + HEAD_SIZE, sourceX, sourceY, sourceX + source2Distance, sourceY + source2Distance, null);
    }

    public enum HEAD_FACE {
        RIGHT_FACE(16, 8),
        LEFT_FACE(0, 8),
        FRONT_FACE(8, 8),
        BACK_FACE(24, 8),
        UP_FACE(8, 0),
        BOTTOM_FACE(16, 0);

        private final int x;
        private final int y;

        HEAD_FACE(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public HeadFace get() {
            return new HeadFace(this.x, this.y);
        }
    }
}
