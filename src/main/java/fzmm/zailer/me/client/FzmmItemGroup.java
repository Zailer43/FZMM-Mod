package fzmm.zailer.me.client;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.utils.*;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.PaintingVariantTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.raid.Raid;

import java.util.Comparator;
import java.util.List;

public class FzmmItemGroup {
    public static final String UNOBTAINABLE_BASE_TRANSLATION_KEY = "itemGroup.fzmm.unobtainable_items.";
    public static final String USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY = "itemGroup.fzmm.useful_block_states.";

    public static void register() {

        FabricItemGroupBuilder.create(new Identifier(FzmmClient.MOD_ID, "unobtainable_items"))
                .icon(() -> new ItemStack(Items.JIGSAW))
                .appendItems((stacks, itemGroup) -> {
                    for (Item item : Registry.ITEM) {
                        if (item.getGroup() == null && item != Items.AIR && item != Items.LIGHT) {
                            stacks.add(new ItemStack(item));
                        }
                    }
                    stacks.add(new ItemStack(Items.POTION));
                    stacks.add(new ItemStack(Items.SPLASH_POTION));
                    stacks.add(new ItemStack(Items.LINGERING_POTION));
                    stacks.add(new ItemStack(Items.TIPPED_ARROW));

                    addArmorStand(stacks);
                    addItemFrames(stacks);
                    addNameTags(stacks);
                    addCrossbows(stacks);
                    addUnobtainablePaintings(stacks);
                    stacks.add(Raid.getOminousBanner());

                    ItemStack elytra = new ItemStack(Items.ELYTRA);
                    elytra.setDamage(431);
                    stacks.add(elytra);

                    addLightBlock(stacks);

                }).build();

        FabricItemGroupBuilder.create(new Identifier(FzmmClient.MOD_ID, "useful_block_states"))
                .icon(() -> new ItemStack(Items.REDSTONE_LAMP))
                .appendItems((stacks, itemGroup) -> {

                    stacks.add(new BlockStateTagItem(Items.REDSTONE_LAMP, "litRedstoneLamp").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.FURNACE, "litFurnace").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.SMOKER, "litSmoker").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.BLAST_FURNACE, "litBlastFurnace").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.CAMPFIRE, "offCampfire").add("lit", false).get());
                    stacks.add(new BlockStateTagItem(Items.CAMPFIRE, "signalFireOfCampfire").add("signal_fire", true).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_CAMPFIRE, "offSoulCampfire").add("lit", false).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_CAMPFIRE, "signalFireOfSoulCampfire").add("signal_fire", true).get());
                    stacks.add(new BlockStateTagItem(Items.GRASS_BLOCK, "snowyGrassBlock").add("snowy", true).get());
                    stacks.add(new BlockStateTagItem(Items.MYCELIUM, "snowyMycelium").add("snowy", true).get());
                    stacks.add(new BlockStateTagItem(Items.PODZOL, "snowyPodzol").add("snowy", true).get());
                    stacks.add(new BlockStateTagItem(Items.SNOW, "snowBlock").add("layers", 8).get());
                    stacks.add(new BlockStateTagItem(Items.BARREL, "openBarrel").add("open", true).get());
                    stacks.add(new BlockStateTagItem(Items.IRON_TRAPDOOR, "openIronTrapdoor").add("open", true).get());
                    stacks.add(new BlockStateTagItem(Items.IRON_DOOR, "openIronDoor").add("open", true).get());
                    stacks.add(new BlockStateTagItem(Items.END_PORTAL_FRAME, "endPortalFrameWithEye").add("eye", true).get());
                    stacks.add(new BlockStateTagItem(Items.LANTERN, "hangingLantern").add("hanging", true).get());
                    stacks.add(new BlockStateTagItem(Items.LANTERN, "lanternOnTheFloor").add("hanging", false).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_LANTERN, "hangingSoulLantern").add("hanging", true).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_LANTERN, "soulLanternOnTheFloor").add("hanging", false).get());
                    stacks.add(new BlockStateTagItem(Items.MANGROVE_PROPAGULE, "hangingMangrovePropagule").add("hanging", true).get());
                    // it is not possible to place it on faces of blocks other than the bottom one, it is useless
//                    stacks.add(new BlockStateTagItem(Items.MANGROVE_PROPAGULE, "Mangrove propagule on the floor").add("hanging", false).get());
                    stacks.add(new BlockStateTagItem(Items.COMPOSTER, "fullComposter").add("level", 8).get());
                    stacks.add(new BlockStateTagItem(Items.RESPAWN_ANCHOR, "fullRespawnAnchor").add("charges", 4).get());
                    stacks.add(new BlockStateTagItem(Items.BAMBOO, "bambooWithLeaves").add("leaves", "large").get());
                    stacks.add(new BlockStateTagItem(Items.WHEAT_SEEDS, "fullGrownWheat").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.PUMPKIN_SEEDS, "fullGrownPumpkin").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.MELON_SEEDS, "fullGrownMelon").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.CARROT, "fullGrownCarrot").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.POTATO, "fullGrownPotatoes").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.BEETROOT_SEEDS, "fullGrownBeetroots").add("age", 3).get());
                    stacks.add(new BlockStateTagItem(Items.COCOA_BEANS, "fullGrownCocoa").add("age", 2).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "repeaterTicks.2").add("delay", 2).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "repeaterTicks.3").add("delay", 3).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "repeaterTicks.4").add("delay", 4).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "lockedRepeater").add("locked", true).get());
                    stacks.add(new BlockStateTagItem(Items.HOPPER, "disabledHopper").add("enabled", false).get());
                    stacks.add(new BlockStateTagItem(Items.BEE_NEST, "beeNestFilledWithHoney").add("honey_level", 5).get());
                    stacks.add(new BlockStateTagItem(Items.BEEHIVE, "beehiveFilledWithHoney").add("honey_level", 5).get());
                    stacks.add(new BlockStateTagItem(Items.SEA_PICKLE, "seaPickle4").add("pickles", 4).get());
                    stacks.add(new BlockStateTagItem(Items.TURTLE_EGG, "turtleEgg4").add("eggs", 4).get());
                    stacks.add(new BlockStateTagItem(Items.CAKE, "sliceOfCake").add("bites", 6).get());
                    stacks.add(new BlockStateTagItem(Items.REDSTONE, "poweredRedstone").add("power", 15).get());
                    stacks.add(new BlockStateTagItem(Items.SCULK_CATALYST, "sculkCatalystBloom").add("bloom", true).get());
                    stacks.add(new BlockStateTagItem(Items.SCULK_SHRIEKER, "sculkShriekerCanSummon").add("can_summon", true).get());
                    stacks.add(new BlockStateTagItem(Items.SCULK_SHRIEKER, "sculkShriekerLocked").add("shrieking", true).get());
                    stacks.add(new BlockStateTagItem(Items.GLOW_LICHEN, "glowLichenBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    stacks.add(new BlockStateTagItem(Items.SCULK_VEIN, "sculkVeinBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    addHalfDoors(stacks);
                    addTallFlowers(stacks);
                    addLeaves(stacks);
                    addLitCandles(stacks);
                    addHalfBed(stacks);
                    addLockedBed(stacks);
                }).build();

        FabricItemGroupBuilder.create(new Identifier(FzmmClient.MOD_ID, "loot_chests"))
                .icon(() -> new ItemStack(Items.CHEST))
                .appendItems((stacks, itemGroup) -> {
                    List<String> lootTablesPath = LootTables.getAll().stream()
                            .map(Identifier::getPath)
                            .sorted()
                            .toList();

                    for (String path : lootTablesPath) {
                        if (path.startsWith("entities"))
                            continue;

                        ItemStack chest = new ItemStack(Items.CHEST);
                        NbtCompound blockEntityTag = new NbtCompound();

                        blockEntityTag.putString("LootTable", path);

                        chest.setCustomName(Text.literal("LootChest: " + path));
                        chest.setSubNbt(TagsConstant.BLOCK_ENTITY, blockEntityTag);
                        stacks.add(chest);
                    }
                }).build();

        FabricItemGroupBuilder.create(new Identifier(FzmmClient.MOD_ID, "player_heads"))
                .icon(() -> new ItemStack(Items.PLAYER_HEAD))
                .appendItems((stacks, itemGroup) -> {
                    stacks.clear();

                    ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                    if (clientPlayer != null) {
                        List<GameProfile> profileList = clientPlayer.networkHandler.getPlayerList().stream()
                                .map(PlayerListEntry::getProfile)
                                .sorted(Comparator.comparing(GameProfile::getName))
                                .toList();

                        for (GameProfile profile : profileList) {
                            stacks.add(HeadUtils.getPlayerHead(profile));
                        }
                    }

                }).build();

    }

    private static void addArmorStand(List<ItemStack> stacks) {
        String baseTranslation = "armorStand.";
        ItemStack armorStandWithArms = new ArmorStandUtils().setShowArms()
                .getItem(Text.translatable(UNOBTAINABLE_BASE_TRANSLATION_KEY + baseTranslation + "arms"));
        stacks.add(armorStandWithArms);

        ItemStack smallArmorStand = new ArmorStandUtils().setSmall()
                .getItem(Text.translatable(UNOBTAINABLE_BASE_TRANSLATION_KEY + baseTranslation + "small"));
        stacks.add(smallArmorStand);

        ItemStack smallArmorStandWithArms = new ArmorStandUtils().setSmall().setShowArms()
                .getItem(Text.translatable(UNOBTAINABLE_BASE_TRANSLATION_KEY + baseTranslation + "smallWithArms"));
        stacks.add(smallArmorStandWithArms);
    }

    private static void addItemFrames(List<ItemStack> stacks) {
        ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
        ItemStack glowItemFrame = new ItemStack(Items.GLOW_ITEM_FRAME);
        NbtCompound entityTag = new NbtCompound();

        entityTag.putBoolean("Invisible", true);
        itemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);
        glowItemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

        itemFrame.setCustomName(Text.translatable(UNOBTAINABLE_BASE_TRANSLATION_KEY + "invisibleItemFrame").setStyle(Style.EMPTY.withItalic(false)));
        glowItemFrame.setCustomName(Text.translatable(UNOBTAINABLE_BASE_TRANSLATION_KEY + "invisibleGlowItemFrame").setStyle(Style.EMPTY.withItalic(false)));

        stacks.add(itemFrame);
        stacks.add(glowItemFrame);
    }

    private static void addNameTags(List<ItemStack> stacks) {
        final int LORE_COLOR = 0x1ecbe1;

        stacks.add(new DisplayUtils(Items.NAME_TAG).setName("")
                .addLore(getNameTagTranslation("empty", 1), LORE_COLOR).get());

        stacks.add(new DisplayUtils(Items.NAME_TAG).setName("Dinnerbone")
                .addLore(getNameTagTranslation("dinnerbone", 1), LORE_COLOR).get());

        stacks.add(new DisplayUtils(Items.NAME_TAG).setName("Grumm")
                .addLore(getNameTagTranslation("grumm", 1), LORE_COLOR).get());

        stacks.add(new DisplayUtils(Items.NAME_TAG).setName("Toast")
                .addLore(getNameTagTranslation("toast", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("toast", 2), LORE_COLOR).get());

        stacks.add(new DisplayUtils(Items.NAME_TAG).setName("jeb_")
                .addLore(getNameTagTranslation("jeb_", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("jeb_", 2), LORE_COLOR).get());

        stacks.add(new DisplayUtils(Items.NAME_TAG).setName("Johnny")
                .addLore(getNameTagTranslation("johnny", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("johnny", 2), LORE_COLOR)
                .addLore(getNameTagTranslation("johnny", 3), LORE_COLOR).get());
    }

    private static Text getNameTagTranslation(String value, int line) {
        String baseTranslation = "nameTag.";
        String commentTranslation = ".comment.";

        return Text.translatable(UNOBTAINABLE_BASE_TRANSLATION_KEY + baseTranslation + value + commentTranslation + line);
    }

    private static void addCrossbows(List<ItemStack> stacks) {
        CrossbowUtils crossbowArrow = new CrossbowUtils();
        CrossbowUtils crossbowFirework = new CrossbowUtils();
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        NbtCompound fireworkTag = new NbtCompound();

        stacks.add(crossbowArrow.setCharged(true).putProjectile(new ItemStack(Items.ARROW)).get());

        fireworkTag.putInt(FireworkRocketItem.FLIGHT_KEY, 2);
        firework.setSubNbt(FireworkRocketItem.FIREWORKS_KEY, fireworkTag);
        stacks.add(crossbowFirework.setCharged(true).putProjectile(firework).get());

    }

    private static void addLightBlock(List<ItemStack> stacks) {
        for (int i = 0; i != 16; i++) {
            stacks.add(new BlockStateTagItem(Items.LIGHT).add("level", i).get());
        }
    }

    private static void addLeaves(List<ItemStack> stacks) {
        for (var item : Registry.ITEM) {
            if (contains(item, ItemTags.LEAVES))
                stacks.add(new BlockStateTagItem(item, "leavesPersistent", item).add("persistent", false).get());
        }
    }

    private static void addHalfDoors(List<ItemStack> stacks) {
        for (var item : Registry.ITEM) {
            if (contains(item, ItemTags.DOORS))
                addHalfUpper(stacks, item, "halfDoor");
        }
    }

    private static void addTallFlowers(List<ItemStack> stacks) {
        String suffix = "tallFlowerSelfDestructs";
        for (var item : Registry.ITEM) {
            if (contains(item, ItemTags.TALL_FLOWERS))
                addHalfUpper(stacks, item, suffix);
        }
        addHalfUpper(stacks, Items.TALL_GRASS, suffix);
        addHalfUpper(stacks, Items.LARGE_FERN, suffix);
        addHalfUpper(stacks, Items.SMALL_DRIPLEAF, suffix);
    }

    private static void addHalfUpper(List<ItemStack> stacks, Item item, String translation) {
        stacks.add(new BlockStateTagItem(item, translation, item).add("half", "upper").get());
    }

    private static void addLitCandles(List<ItemStack> stacks) {
        for (var item : Registry.ITEM) {
            if (contains(item, ItemTags.CANDLES))
                stacks.add(new BlockStateTagItem(item, "litCandle", item).add("lit", true).get());
        }
    }

    private static void addHalfBed(List<ItemStack> stacks) {
        for (var item : Registry.ITEM) {
            if (contains(item, ItemTags.BEDS))
                stacks.add(new BlockStateTagItem(item, "bedHeadPart", item).add("part", "head").get());
        }
    }

    private static void addLockedBed(List<ItemStack> stacks) {
        for (var item : Registry.ITEM) {
            if (contains(item, ItemTags.BEDS))
                stacks.add(new BlockStateTagItem(item, "lockedBed", item).add("occupied", true).get());
        }
    }

    private static void addUnobtainablePaintings(List<ItemStack> stacks) {
        for (var painting : Registry.PAINTING_VARIANT) {
            if (!contains(painting)) {
                String variantName = Registry.PAINTING_VARIANT.getId(painting).getPath();

                ItemStack paintingStack = new DisplayUtils(Items.PAINTING).setName(variantName).get();
                NbtCompound entityTag = new NbtCompound();
                entityTag.put("variant", NbtString.of(variantName));
                paintingStack.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

                stacks.add(paintingStack);
            }
        }
    }

    private static boolean contains(PaintingVariant paintingVariant) {
        for (RegistryEntry<PaintingVariant> paintingVariantEntry : Registry.PAINTING_VARIANT.iterateEntries(PaintingVariantTags.PLACEABLE)) {
            if (paintingVariantEntry.value().equals(paintingVariant))
                return true;
        }
        return false;
    }

    private static boolean contains(Item item, TagKey<Item> tag) {
        return ItemPredicate.Builder.create().tag(tag).build().test(new ItemStack(item));
    }
}
