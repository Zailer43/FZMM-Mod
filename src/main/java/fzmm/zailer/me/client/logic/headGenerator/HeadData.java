package fzmm.zailer.me.client.logic.headGenerator;

import java.awt.image.BufferedImage;

/**
 * @param headSkin the skin of the head, this is where the hat, glasses, beard, hair or whatever is,
 *                should not be confused with the base skin (the one to which this skin is applied on top)
 * @param displayName the name to display to the user
 */
public record HeadData(BufferedImage headSkin, String displayName) {
}
