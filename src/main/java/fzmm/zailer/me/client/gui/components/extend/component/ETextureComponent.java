package fzmm.zailer.me.client.gui.components.extend.component;

import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.util.Identifier;
import org.w3c.dom.Element;

public class ETextureComponent extends TextureComponent {

    public ETextureComponent(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        super(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    /**
     * Removes the bounding box to avoid the component to:<br>
     *  - being clickable<br>
     *  - interfering with component under it<br>
     *  - having cursor style<br>
     *  - being hovered<br>
     *  - having tooltip<br>
     *  And maybe add some unknown cursed behavior<br>
     */
    @Override
    public boolean isInBoundingBox(double x, double y) {
        return false;
    }

    /**
     * Copy from {@link TextureComponent#parse(Element)}
     */
    public static ETextureComponent parse(Element element) {
        UIParsing.expectAttributes(element, "texture");
        var textureId = UIParsing.parseIdentifier(element.getAttributeNode("texture"));

        int u = 0, v = 0, regionWidth = 0, regionHeight = 0, textureWidth = 256, textureHeight = 256;
        if (element.hasAttribute("u")) {
            u = UIParsing.parseSignedInt(element.getAttributeNode("u"));
        }

        if (element.hasAttribute("v")) {
            v = UIParsing.parseSignedInt(element.getAttributeNode("v"));
        }

        if (element.hasAttribute("region-width")) {
            regionWidth = UIParsing.parseSignedInt(element.getAttributeNode("region-width"));
        }

        if (element.hasAttribute("region-height")) {
            regionHeight = UIParsing.parseSignedInt(element.getAttributeNode("region-height"));
        }

        if (element.hasAttribute("texture-width")) {
            textureWidth = UIParsing.parseSignedInt(element.getAttributeNode("texture-width"));
        }

        if (element.hasAttribute("texture-height")) {
            textureHeight = UIParsing.parseSignedInt(element.getAttributeNode("texture-height"));
        }

        return new ETextureComponent(textureId, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
