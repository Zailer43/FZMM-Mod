package fzmm.zailer.me.client.logic.player_statue;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.UpdatableSnackBarComponent;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.player_statue.tabs.PlayerStatueGenerateTab;
import fzmm.zailer.me.client.logic.player_statue.statue_head_skin.*;
import fzmm.zailer.me.utils.*;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerStatue {
    private final List<StatuePart> statueList;
    private final String name;
    private BufferedImage playerSkin;
    private final Vector3f pos;
    private final HorizontalDirectionOption direction;
    private UpdatableSnackBarComponent snackBar;
    private int partsGenerated;
    private int totalToGenerate;

    public PlayerStatue(BufferedImage playerSkin, String name, Vector3f pos, HorizontalDirectionOption direction) {
        this.playerSkin = playerSkin;
        this.name = name;
        this.statueList = new ArrayList<>();
        this.pos = pos;
        this.direction = direction;
    }

    public CompletableFuture<Void> generateStatues() {
        this.statueList.clear();

        int scale = this.getSkinScale();
        if (FzmmClient.CONFIG.playerStatue.convertSkinWithAlexModelInSteveModel() && ImageUtils.isSlimSimpleCheck(this.playerSkin, scale))
            this.playerSkin = ImageUtils.convertInSteveModel(this.playerSkin, scale);

        this.addStatueParts();

        this.totalToGenerate = this.statueList.size();
        this.partsGenerated = 0;
        AtomicBoolean isCancelled = new AtomicBoolean(false);

        Minecraft.getInstance().execute(() -> this.notifyStart(isCancelled));
        return this.generate(isCancelled)
                .whenComplete((unused, throwable) -> Minecraft.getInstance().execute(() -> {
                    this.snackBar.close();
                    if (throwable != null) return;
                    this.notifyComplete();
                }));
    }

    private void addStatueParts() {
        HeadModelSkin empty = new HeadModelSkin();
        HeadModelSkin bottom = new HeadModelSkin(HeadFace.HEAD_FACE.BOTTOM_FACE);
        HeadModelSkin top = new HeadModelSkin(HeadFace.HEAD_FACE.UP_FACE);
        HeadModelSkin left = new HeadModelSkin(HeadFace.HEAD_FACE.LEFT_FACE);
        HeadModelSkin right = new HeadModelSkin(HeadFace.HEAD_FACE.RIGHT_FACE);
        HeadModelSkin leftBottom = new HeadModelSkin(HeadFace.HEAD_FACE.BOTTOM_FACE, HeadFace.HEAD_FACE.LEFT_FACE);
        HeadModelSkin rightBottom = new HeadModelSkin(HeadFace.HEAD_FACE.BOTTOM_FACE, HeadFace.HEAD_FACE.RIGHT_FACE);

        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_LEG, "Right bottom leg", 0, leftBottom, 0, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_LEG, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_LEG, "Right middle leg", 1, left, -2, 0, 1, new ExtremitySkinManager(SkinPart.LEFT_LEG, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_LEG, "Right top leg", 2, left, 2, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_LEG, AbstractStatueSkinManager.Height.UPPER)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_BODY, "Right bottom body", 3, empty, -1, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.LOWER, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_BODY, "Right middle body", 4, empty, 1, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.MIDDLE, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_BODY, "Right top body", 5, top, -1, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.UPPER, true)));

        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_LEG, "Left bottom leg", 0, rightBottom, -1, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_LEG, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_LEG, "Left middle leg", 1, right, 1, 0, -1, new ExtremitySkinManager(SkinPart.RIGHT_LEG, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_LEG, "Left top leg", 2, right, -2, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_LEG, AbstractStatueSkinManager.Height.UPPER)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_BODY, "Left bottom body", 3, empty, 0, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.LOWER, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_BODY, "Left middle body", 4, empty, -2, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.MIDDLE, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_BODY, "Left top body", 5, top, 0, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.UPPER, false)));

        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_ARM, "Right bottom arm", 3, bottom, 0, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_ARM, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_ARM, "Right middle arm", 4, empty, -2, 0, 1, new ExtremitySkinManager(SkinPart.LEFT_ARM, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_ARM, "Right top arm", 5, top, 0, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_ARM, AbstractStatueSkinManager.Height.UPPER)));

        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_ARM, "Left bottom arm", 3, bottom, -1, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_ARM, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_ARM, "Left middle arm", 4, empty, 1, 0, -1, new ExtremitySkinManager(SkinPart.RIGHT_ARM, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_ARM, "Left top arm", 5, top, -1, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_ARM, AbstractStatueSkinManager.Height.UPPER)));

        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_FRONT, "Right bottom front head", 6, bottom, 1, 0, 0, new HeadSkinManager(false, true, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_FRONT, "Right top front head", 7, top, 2, 0, 2, new HeadSkinManager(false, false, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_BACK, "Right bottom back head", 6, bottom, 0, 0, 1, new HeadSkinManager(false, true, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_BACK, "Right top back head", 7, top, -3, 0, -3, new HeadSkinManager(false, false, true)));

        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_FRONT, "Left bottom front head", 6, bottom, -1, 0, 0, new HeadSkinManager(true, true, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_FRONT, "Left top front head", 7, top, 1, 0, -1, new HeadSkinManager(true, false, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_BACK, "Left bottom back head", 6, bottom, 1, 0, -2, new HeadSkinManager(true, true, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_BACK, "Left top back head", 7, top, -2, 0, 1, new HeadSkinManager(true, false, true)));
    }

    private void notifyStart(AtomicBoolean isCancelled) {
        this.snackBar = (UpdatableSnackBarComponent) UpdatableSnackBarComponent.builder(SnackBarManager.PLAYER_STATUE_ID)
                .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                .keepOnLimit()
                .title(Component.translatable("fzmm.snack_bar.playerStatue.loading.title"))
                .details(Component.translatable("fzmm.snack_bar.playerStatue.loading.details",
                        this.partsGenerated, 0, 0, 0, this.statueList.get(0).getName()))
                .sizing(Sizing.fixed(220), Sizing.content())
                .startTimer()
                .button(snackBar ->
                        EComponents.button(Component.translatable("fzmm.gui.button.cancel"))
                                .onPress(button -> {
                                    isCancelled.set(true);
                                    PlayerStatueGenerateTab.active = false;
                                    snackBar.close();
                                })
                ).build();

        SnackBarManager.getInstance().add(this.snackBar);
    }

    private void notifyComplete() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.PLAYER_STATUE_ID)
                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                .keepOnLimit()
                .title(Component.translatable("fzmm.snack_bar.playerStatue.successful.title"))
                .sizing(Sizing.fixed(220), Sizing.content())
                .mediumTimer()
                .startTimer()
                .build()
        );
    }

    public CompletableFuture<Void> generate(AtomicBoolean isCancelled) {
        List<BufferedImage> parts = this.statueList.stream()
                .map(statuePart -> statuePart.drawAndGet(this.playerSkin, this.getSkinScale()))
                .toList();

        return FzmmClient.MINESKIN_API.uploadSequentially(parts,
                (skin, response) -> {
                    this.partsGenerated++;
                    for (var part : this.statueList) {
                        if (part.isEquals(skin)) {
                            part.value(response.data().orElseThrow().skin().orElseThrow().toSkinValue());
                            this.updateStatus(part, FzmmClient.MINESKIN_API.getWaitMillis());
                            return;
                        }
                    }
                }, isCancelled
        );
    }

    public static ItemStack getStatueName(Vector3f pos, String name) {
        float x = pos.x() + 0.5f;
        float y = pos.y() - 0.1f;
        float z = pos.z() + 0.5f;

        Component nameText = Component.nullToEmpty(name);
        if (name != null && !name.isEmpty()) {
            try {
                // if serialization fails, it throws an exception
                nameText = ComponentSerialization.CODEC.decode(FzmmUtils.getRegistryOps(JsonOps.INSTANCE), JsonParser.parseString(name)).map(Pair::getFirst).getOrThrow();

                if (nameText == null) {
                    throw new IllegalArgumentException(String.format("[PlayerStatue] 'name' is not a valid JSON string: %s", name));
                }
            } catch (Exception ignored) {
                if (name.length() > 100) {
                    name = name.substring(0, 99);
                }
                nameText = Component.nullToEmpty(name);
            }
        }

        ItemStack nameTagStack = ArmorStandBuilder.builder()
                .setPos(x, y, z)
                .setAsHologram(nameText)
                .setTags(StatuePart.PLAYER_STATUE_TAG)
                .getItem("Name tag");

        nameTagStack.update(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()), component -> {
            CompoundTag result = component.copyTag();
            CompoundTag fzmmTag = new CompoundTag();
            CompoundTag playerStatueTag = new CompoundTag();

            playerStatueTag.putByte(StatuePart.PlayerStatueTags.NAME_TAG, (byte) 1);
            fzmmTag.put(TagsConstant.FZMM_PLAYER_STATUE, playerStatueTag);
            result.put(TagsConstant.FZMM, fzmmTag);

            return CustomData.of(result);
        });

        return nameTagStack;
    }

    public static boolean isNameTag(ItemStack stack) {
        CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        CompoundTag fzmmTag = customData.getCompoundOrEmpty(TagsConstant.FZMM);
        CompoundTag playerStatueTag = fzmmTag.getCompoundOrEmpty(TagsConstant.FZMM_PLAYER_STATUE);

        return playerStatueTag.getByte(StatuePart.PlayerStatueTags.NAME_TAG).isPresent();
    }

    public List<ItemStack> getStatueItems() {
        List<ItemStack> stackList = new ArrayList<>();

        for (StatuePart statue : this.statueList) {
            stackList.add(statue.get(this.pos, this.direction));
        }

        if (!this.name.isEmpty()) {
            stackList.add(getStatueName(this.pos, this.name));
        }

        return stackList;
    }

    public ItemStack getStatueInContainer() {
        return getStatueInContainer(this.getStatueItems(), this.pos);
    }

    public static ItemStack getStatueInContainer(List<ItemStack> statueList, Vector3f pos) {
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        String x = decimalFormat.format(pos.x());
        String y = decimalFormat.format(pos.y());
        String z = decimalFormat.format(pos.z());
        int color = FzmmClient.CONFIG.colors.playerStatue().rgb();
        Style colorStyle = Style.EMPTY.withColor(color);

        List<ItemStack> containerList = ContainerBuilder.builder()
                .containerItem(ItemUtils.from(FzmmClient.CONFIG.playerStatue.defaultContainer()))
                //.maxItemByContainer(FzmmClient.CONFIG.playerStatue.defaultContainer())//todo
                .addAll(statueList)
                .setNameStyleToItems(colorStyle)
                .addLoreToItems(Items.ARMOR_STAND, Component.translatable("fzmm.item.playerStatue.lore.1").getString(), color)
                .addLoreToItems(Items.ARMOR_STAND, Component.translatable("fzmm.item.playerStatue.lore.2").getString(), color)
                .getAsList();

        if (containerList.isEmpty()) return ItemStack.EMPTY;

        ItemStack container = containerList.get(0);
        container = DisplayBuilder.of(container)
                .setName(Component.literal(Component.translatable("fzmm.item.playerStatue.container.name").getString()).setStyle(colorStyle.withBold(true)))
                .addLore(Component.translatable("fzmm.item.playerStatue.container.lore.1", x, y, z), color)
                .get();

        return container;
    }

    public void updateStatus(StatuePart part, long delayMillis) {
        Minecraft.getInstance().execute(() -> {
            float delay = delayMillis / 1000f;
            this.snackBar.updateTitle(Component.translatable("fzmm.snack_bar.playerStatue.loading.title"));
            this.snackBar.updateDetails(Component.translatable("fzmm.snack_bar.playerStatue.loading.details",
                    part.getName(),
                    this.partsGenerated,
                    this.totalToGenerate,
                    new DecimalFormat("#,#0.0").format(delay)
            ));
            this.snackBar.updateTimerBar(this.partsGenerated / (float) this.totalToGenerate);

            if (!this.snackBar.hasParent()) {
                SnackBarManager.getInstance().add(this.snackBar);
            }
        });
    }

    public static ItemStack updateStatue(ItemStack container, Vector3f pos, HorizontalDirectionOption direction, String name) {
        List<ItemStack> containerItems = InventoryUtils.getItemsFromContainer(container);
        List<ItemStack> statueList = new ArrayList<>();

        for (ItemStack stack : containerItems) {
            if (StatuePart.isStatue(stack)) {
                statueList.add(StatuePart.ofItem(stack).get(pos, direction));
            } else if (isNameTag(stack)) {
                statueList.add(getStatueName(pos, name));
            }
        }

        return getStatueInContainer(statueList, pos);
    }

    public static boolean isPlayerStatue(ItemStack container) {
        List<ItemStack> containerItems = InventoryUtils.getItemsFromContainer(container);

        if (containerItems.isEmpty()) return false;

        for (ItemStack stack : containerItems) {
            if (!StatuePart.isStatue(stack) && !isNameTag(stack)) return false;
        }

        return true;
    }

    private int getSkinScale() {
        return this.playerSkin.getHeight() == 128 && this.playerSkin.getWidth() == 128 ? 2 : 1;
    }
}
