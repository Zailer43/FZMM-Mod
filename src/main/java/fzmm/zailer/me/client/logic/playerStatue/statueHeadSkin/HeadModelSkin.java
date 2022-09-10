package fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HeadModelSkin {
    private List<HeadFace.HEAD_FACE> headFaces;

    public HeadModelSkin() {
        this.headFaces = new ArrayList<>();
    }

    public HeadModelSkin(List<HeadFace.HEAD_FACE> headFaces) {
        this.headFaces = headFaces;
    }

    public HeadModelSkin(HeadFace.HEAD_FACE... headFaces) {
        this.headFaces = new ArrayList<>();

        this.add(headFaces);
    }

    public static HeadModelSkin of(HeadModelSkin headModelSkin, HeadModelSkin headModelSkin2) {
        List<HeadFace.HEAD_FACE> headFaces = new ArrayList<>(headModelSkin.headFaces);
        headFaces.addAll(headModelSkin2.headFaces);

        return new HeadModelSkin(headFaces);
    }

    public void add(HeadFace.HEAD_FACE... headFaces) {
        this.headFaces.addAll(Arrays.stream(headFaces).toList());

        this.distinct();
    }

    private void distinct() {
        this.headFaces = this.headFaces.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public void draw(AbstractStatueSkinManager skinManager, Graphics2D graphics, BufferedImage playerSkin, int scale) {
        for (HeadFace.HEAD_FACE headFace : this.headFaces) {
            skinManager.draw(headFace, graphics, playerSkin, scale);
        }
    }

}
