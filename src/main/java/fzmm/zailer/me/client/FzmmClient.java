package fzmm.zailer.me.client;

import fzmm.zailer.me.client.command.FzmmCommand;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntityModel;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntityRenderer;
import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import fzmm.zailer.me.client.gui.main.MainScreen;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.client.logic.ItemTooltipAppend;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Environment(EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {

    public static final String MOD_ID = "fzmm";
    public static final Logger LOGGER = LoggerFactory.getLogger("FZMM");
    public static final FzmmConfig CONFIG = FzmmConfig.createAndLoad();
    public static final KeyBinding OPEN_MAIN_GUI_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fzmm.mainGui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.fzmm"));
    public static final int CHAT_BASE_COLOR = 0x478e47;
    public static final int CHAT_WHITE_COLOR = 0xb7b7b7;
    public static final Identifier CUSTOM_HEAD_ENTITY = Identifier.of(FzmmClient.MOD_ID, "custom_head");
    public static final EntityModelLayer MODEL_CUSTOM_HEAD_LAYER = new EntityModelLayer(CUSTOM_HEAD_ENTITY, "main");
    public static final String HTTP_USER_AGENT = "FZMM/1.0";


    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(FzmmCommand::registerCommands);
        FzmmItemGroup.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!OPEN_MAIN_GUI_KEYBINDING.wasPressed())
                return;

            if (ScreenshotSource.hasInstance()) {
                ScreenshotSource.getInstance().takeScreenshot();
            } else if (AutoPlacerHud.isHudActive) {
                AutoPlacerHud.removeHud();
            } else {
                FzmmUtils.setScreen(new MainScreen(client.currentScreen));
            }
        });


        FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(container -> ResourceManagerHelper.registerBuiltinResourcePack(
                        Identifier.of(MOD_ID, "fzmm_default_heads"),
                        container,
                        Text.literal("FZMM: Head generator"),
                        ResourcePackActivationType.DEFAULT_ENABLED
                )).filter(success -> !success).ifPresent(success -> LOGGER.warn("[FzmmClient] Failed to register default heads resource pack"));

        CONFIG.history.subscribeToMaxItemHistory(integer -> FzmmHistory.onUpdateConfig());
        CONFIG.history.subscribeToMaxHeadHistory(integer -> FzmmHistory.onUpdateConfig());

        EntityRendererRegistry.register(CustomHeadEntity.CUSTOM_HEAD_ENTITY_TYPE, CustomHeadEntityRenderer::new);
        FabricDefaultAttributeRegistry.register(CustomHeadEntity.CUSTOM_HEAD_ENTITY_TYPE, CustomHeadEntity.createMobAttributes());
        EntityModelLayerRegistry.registerModelLayer(MODEL_CUSTOM_HEAD_LAYER, CustomHeadEntityModel::getTexturedModelData);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new HeadResourcesLoader());

        AutoPlacerHud.init();
        ItemTooltipAppend.init();
    }
}