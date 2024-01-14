package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class PlayerStatueGenerateTab implements IPlayerStatueTab {
    private static final ImageStatus INVALID_SKIN_SIZE = new ImageStatus("playerStatue.invalidSkinSize", ImageStatus.StatusType.ERROR);
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_ID = "skin-source";
    private static Thread CREATE_PLAYER_STATUE_THREAD = null;
    private ImageRowsElements skinElements;
    private ButtonWidget executeButton;

    @Override
    public String getId() {
        return "generate";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.skinElements = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_ID, ImageMode.NAME);
        this.executeButton = rootComponent.childById(ButtonWidget.class, ButtonRow.getButtonId(PlayerStatueScreen.EXECUTE_ID));

        ImageButtonComponent skinButton = this.skinElements.imageButton();
        skinButton.setImageLoadedEvent(this::skinCallback);
        skinButton.setButtonCallback(skin -> {
            this.executeButton.active = this.canExecute();
            if (skin.getWidth() == 64 && skin.getHeight() == 32)
                skinButton.setImage(ImageUtils.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skin));
        });
    }


    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        if (!this.canExecute())
            return;

        Optional<BufferedImage> image = this.skinElements.imageButton().getImage();

        if (image.isEmpty())
            return;

        CREATE_PLAYER_STATUE_THREAD = new Thread(() -> {
            this.executeButton.active = false;


            Vector3f pos = new Vector3f(x, y, z);

            ItemStack statueGenerated = new PlayerStatue(image.get(), name, pos, direction)
                    .generateStatues()
                    .getStatueInContainer();

            FzmmUtils.giveItem(statueGenerated);

            this.executeButton.active = true;
        });

        CREATE_PLAYER_STATUE_THREAD.start();
    }

    @Override
    public boolean canExecute() {
        return this.canExecute(this.skinElements.imageButton().hasImage());
    }

    public boolean canExecute(boolean hasImage) {
        return hasImage && (CREATE_PLAYER_STATUE_THREAD == null || !CREATE_PLAYER_STATUE_THREAD.isAlive());
    }

    public ImageStatus skinCallback(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (!(width == 64 && height == 32) && !(width == 64 && height == 64) && !(width == 128 && height == 128))
            return INVALID_SKIN_SIZE;

        return ImageStatus.IMAGE_LOADED;
    }

    @Override
    public IMementoObject createMemento() {
        return new GenerateMementoTab(this.skinElements.valueField().getText(), this.skinElements.mode().get());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        GenerateMementoTab memento = (GenerateMementoTab) mementoTab;
        this.skinElements.valueField().setText(memento.skinRowValue);
        this.skinElements.valueField().setCursorToStart(false);
        this.skinElements.imageModeButtons().get(memento.sourceType).onPress();
    }

    private record GenerateMementoTab(String skinRowValue, ImageMode sourceType) implements IMementoObject {
    }
}
