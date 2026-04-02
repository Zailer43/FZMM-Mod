package fzmm.zailer.me.client;

import com.mojang.blaze3d.platform.InputConstants;
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
import fzmm.zailer.me.client.logic.minecraft_heads.MinecraftHeadsResources;
import fzmm.zailer.me.client.logic.mineskin.MineskinApi;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {

    public static final String MOD_ID = "fzmm";
    public static final Logger LOGGER = LoggerFactory.getLogger("FZMM");
    public static final FzmmConfig CONFIG = FzmmConfig.createAndLoad();
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
    public static final KeyMapping OPEN_MAIN_GUI_KEYBINDING = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fzmm.mainGui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, KEY_CATEGORY));
    public static final int CHAT_BASE_COLOR = 0x478e47;
    public static final int CHAT_WHITE_COLOR = 0xb7b7b7;
    public static final Identifier CUSTOM_HEAD_ENTITY = Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "custom_head");
    public static final ModelLayerLocation MODEL_CUSTOM_HEAD_LAYER = new ModelLayerLocation(CUSTOM_HEAD_ENTITY, "main");
    public static MineskinApi MINESKIN_API;
    public static MinecraftHeadsResources MCH_RESOURCES;


    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(FzmmCommand::registerCommands);
        FzmmItemGroup.register();
        registerKeys();
        HeadResourcesLoader.registerBuiltinResourcePack();

        CONFIG.history.subscribeToMaxItemHistory(integer -> FzmmHistory.onUpdateConfig());
        CONFIG.history.subscribeToMaxHeadHistory(integer -> FzmmHistory.onUpdateConfig());

        EntityRenderers.register(CustomHeadEntity.CUSTOM_HEAD_ENTITY_TYPE, CustomHeadEntityRenderer::new);
        FabricDefaultAttributeRegistry.register(CustomHeadEntity.CUSTOM_HEAD_ENTITY_TYPE, CustomHeadEntity.createMobAttributes());
        ModelLayerRegistry.registerModelLayer(MODEL_CUSTOM_HEAD_LAYER, CustomHeadEntityModel::getTexturedModelData);

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "head-resources-loader"), new HeadResourcesLoader());

        AutoPlacerHud.init();
        ItemTooltipAppend.init();

        MINESKIN_API = new MineskinApi();
        MCH_RESOURCES = new MinecraftHeadsResources();
    }

    private static void registerKeys() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!OPEN_MAIN_GUI_KEYBINDING.consumeClick()) return;

            if (ScreenshotSource.hasInstance()) {
                ScreenshotSource.getInstance().takeScreenshot();
            } else if (AutoPlacerHud.isHudActive) {
                AutoPlacerHud.removeHud();
            } else {
                FzmmUtils.setScreen(new MainScreen(client.screen));
            }
        });
    }
}