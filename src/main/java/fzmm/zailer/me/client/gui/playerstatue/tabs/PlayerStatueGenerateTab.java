package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.mode.SkinMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ImageRows;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.playerstatue.IPlayerStatueTab;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatueScreen;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class PlayerStatueGenerateTab implements IPlayerStatueTab {
    private static final ImageStatus OLD_SKIN_FORMAT_NOT_SUPPORTED = new ImageStatus("playerStatue.oldSkinFormatNotSupported", ImageStatus.StatusType.ERROR);
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_ID = "skin-source";
    private static final String LAST_PLAYER_STATUE_GENERATED_ID = "lastGenerated";
    private static ItemStack lastPlayerStatueGenerated = null;
    private static Thread CREATE_PLAYER_STATUE_THREAD = null;
    private ImageButtonComponent skinButton;
    private ButtonWidget executeButton;
    private ButtonWidget lastGeneratedButton;

    @Override
    public String getId() {
        return "generate";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.skinButton = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_ID, SkinMode.NAME);
        this.lastGeneratedButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(LAST_PLAYER_STATUE_GENERATED_ID), lastPlayerStatueGenerated != null, this::lastGeneratedCallback);
        this.executeButton = rootComponent.childById(ButtonWidget.class, ButtonRow.getButtonId(PlayerStatueScreen.EXECUTE_ID));

        this.skinButton.setImageLoadedEvent(this::skinCallback);
        this.skinButton.setButtonCallback(bufferedImage -> this.executeButton.active = this.canExecute());
    }


    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        if (!this.canExecute())
            return;

        Optional<BufferedImage> image = this.skinButton.getImage();

        if (image.isEmpty())
            return;

        CREATE_PLAYER_STATUE_THREAD = new Thread(() -> {
            this.executeButton.active = false;


            Vector3f pos = new Vector3f(x, y, z);

            lastPlayerStatueGenerated = new PlayerStatue(image.get(), name, pos, direction)
                    .generateStatues()
                    .getStatueInContainer();

            FzmmUtils.giveItem(lastPlayerStatueGenerated);

            this.executeButton.active = true;
            this.lastGeneratedButton.active = true;
        });

        CREATE_PLAYER_STATUE_THREAD.start();
    }

    @Override
    public boolean canExecute() {
        return this.canExecute(this.skinButton.hasImage());
    }

    public boolean canExecute(boolean hasImage) {
        return hasImage && (CREATE_PLAYER_STATUE_THREAD == null || !CREATE_PLAYER_STATUE_THREAD.isAlive());
    }

    public ImageStatus skinCallback(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (!(width == 64 && height == 64) && !(width == 128 && height == 128))
            return OLD_SKIN_FORMAT_NOT_SUPPORTED;

        return ImageStatus.IMAGE_LOADED;
    }

    private void lastGeneratedCallback(ButtonWidget buttonWidget) {
        if (lastGeneratedButton != null)
            FzmmUtils.giveItem(lastPlayerStatueGenerated);
    }
}
