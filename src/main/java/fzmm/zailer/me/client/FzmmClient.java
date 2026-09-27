package fzmm.zailer.me.client;

import fzmm.zailer.me.client.command.FzmmCommand;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntityModel;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntityRenderer;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxMenuManager;
import fzmm.zailer.me.client.logic.ItemTooltipAppend;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.client.logic.minecraft_heads.MinecraftHeadsResources;
import fzmm.zailer.me.client.logic.mineskin.MineskinApi;
import fzmm.zailer.me.config.FzmmConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {

    public static final String MOD_ID = "fzmm";
    public static final Logger LOGGER = LoggerFactory.getLogger("FZMM");
    public static final FzmmConfig CONFIG = FzmmConfig.createAndLoad();
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
        FzmmHotkeys.register();
        HeadResourcesLoader.registerBuiltinResourcePack();
        CtxMenuManager.registerUILayers();

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
}