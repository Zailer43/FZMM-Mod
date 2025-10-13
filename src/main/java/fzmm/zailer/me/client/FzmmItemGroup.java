package fzmm.zailer.me.client;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.BlockStateItemBuilder;
import fzmm.zailer.me.builders.CrossbowBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.raid.Raid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FzmmItemGroup {
    public static final String OPERATOR_BASE_TRANSLATION_KEY = "itemGroup.op";
    public static final String USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY = "itemGroup.fzmm.useful_block_states";
    public static final Identifier USEFUL_BLOCK_STATES_IDENTIFIER = Identifier.of(FzmmClient.MOD_ID, "useful_block_states");
    public static final Identifier LOOT_CHESTS_IDENTIFIER = Identifier.of(FzmmClient.MOD_ID, "loot_chests");

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) return;
            DynamicRegistryManager registryManager = MinecraftClient.getInstance().player.getRegistryManager();
            ArrayList<ItemStack> newEntries = new ArrayList<>();

            newEntries.add(new ItemStack(Items.FILLED_MAP));
            newEntries.add(new ItemStack(Items.WRITTEN_BOOK));
            newEntries.add(new ItemStack(Items.ENCHANTED_BOOK));
            newEntries.add(Items.KNOWLEDGE_BOOK.getDefaultStack());
            newEntries.add(new ItemStack(Items.SUSPICIOUS_STEW));
            newEntries.add(new ItemStack(Items.POTION));
            newEntries.add(new ItemStack(Items.SPLASH_POTION));
            newEntries.add(new ItemStack(Items.LINGERING_POTION));
            newEntries.add(new ItemStack(Items.TIPPED_ARROW));
            newEntries.add(Items.DRAGON_EGG.getDefaultStack());
            newEntries.add(Items.PETRIFIED_OAK_SLAB.getDefaultStack());

            addSpawnEggs(newEntries);
            addArmorStand(newEntries);
            addItemFrames(newEntries);
            addNameTags(newEntries);
            addCrossbows(newEntries);
            Optional<Registry<BannerPattern>> patternRegistry = registryManager.getOptional(RegistryKeys.BANNER_PATTERN);
            if (patternRegistry.isPresent()) {
                newEntries.add(Raid.createOminousBanner(patternRegistry.get()));
            } else {
                FzmmClient.LOGGER.warn("[FzmmItemGroup] Failed to add OminousBanner");
            }

            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.setDamage(elytra.getMaxDamage() - 1);
            newEntries.add(elytra);

            entries.addAfter(Items.DEBUG_STICK, newEntries);
        });
        // TODO: this need be sorted/organized
        ItemGroup usefulBlockStatesItemGroup = FabricItemGroup.builder()
                .displayName(Text.translatable(USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY))
                .icon(() -> new ItemStack(Items.REDSTONE_LAMP))
                .entries((displayContext, entries) -> {

                    entries.add(new BlockStateItemBuilder(Items.REDSTONE_LAMP, "litRedstoneLamp").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.FURNACE, "litFurnace").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SMOKER, "litSmoker").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.BLAST_FURNACE, "litBlastFurnace").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.CAMPFIRE, "offCampfire").add("lit", false).get());
                    entries.add(new BlockStateItemBuilder(Items.CAMPFIRE, "signalFireOfCampfire").add("signal_fire", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_CAMPFIRE, "offSoulCampfire").add("lit", false).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_CAMPFIRE, "signalFireOfSoulCampfire").add("signal_fire", true).get());
                    entries.add(new BlockStateItemBuilder(Items.GRASS_BLOCK, "snowyGrassBlock").add("snowy", true).get());
                    entries.add(new BlockStateItemBuilder(Items.MYCELIUM, "snowyMycelium").add("snowy", true).get());
                    entries.add(new BlockStateItemBuilder(Items.PODZOL, "snowyPodzol").add("snowy", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SNOW, "snowBlock").add("layers", 8).get());
                    entries.add(new BlockStateItemBuilder(Items.BARREL, "openBarrel").add("open", true).get());
                    entries.add(new BlockStateItemBuilder(Items.IRON_TRAPDOOR, "openIronTrapdoor").add("open", true).get());
                    entries.add(new BlockStateItemBuilder(Items.IRON_DOOR, "openIronDoor").add("open", true).get());
                    addItemTag(ItemTags.WOODEN_SHELVES, item -> entries.add(new BlockStateItemBuilder(item, "poweredShelf", item).add("powered", true).get()));
                    entries.add(new BlockStateItemBuilder(Items.END_PORTAL_FRAME, "endPortalFrameWithEye").add("eye", true).get());
                    addItemTag(ItemTags.LANTERNS, item -> {
                        entries.add(new BlockStateItemBuilder(item, "hangingLantern", item).add("hanging", true).get());
                        entries.add(new BlockStateItemBuilder(item, "lanternOnTheFloor", item).add("hanging", false).get());
                    });
                    entries.add(new BlockStateItemBuilder(Items.MANGROVE_PROPAGULE, "hangingMangrovePropagule").add("hanging", true).get());
                    // it is not possible to place it on faces of blocks other than the bottom one, it is useless
//                    stacks.add(new BlockStateTagItem(Items.MANGROVE_PROPAGULE, "Mangrove propagule on the floor").add("hanging", false).get());
                    entries.add(new BlockStateItemBuilder(Items.COMPOSTER, "fullComposter").add("level", 8).get());
                    entries.add(new BlockStateItemBuilder(Items.RESPAWN_ANCHOR, "fullRespawnAnchor").add("charges", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.BAMBOO, "bambooWithLeaves").add("leaves", "large").get());
                    entries.add(new BlockStateItemBuilder(Items.WHEAT_SEEDS, "fullGrownWheat").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.PUMPKIN_SEEDS, "fullGrownPumpkin").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.MELON_SEEDS, "fullGrownMelon").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.CARROT, "fullGrownCarrot").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.POTATO, "fullGrownPotatoes").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.BEETROOT_SEEDS, "fullGrownBeetroots").add("age", 3).get());
                    entries.add(new BlockStateItemBuilder(Items.COCOA_BEANS, "fullGrownCocoa").add("age", 2).get());
                    entries.add(new BlockStateItemBuilder(Items.GLOW_BERRIES, "glowBerries").add("berries", true).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.2").add("delay", 2).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.3").add("delay", 3).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.4").add("delay", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "lockedRepeater").add("locked", true).get());
                    entries.add(new BlockStateItemBuilder(Items.HOPPER, "disabledHopper").add("enabled", false).get());
                    entries.add(new BlockStateItemBuilder(Items.BEE_NEST, "beeNestFilledWithHoney").add("honey_level", 5).get());
                    entries.add(new BlockStateItemBuilder(Items.BEEHIVE, "beehiveFilledWithHoney").add("honey_level", 5).get());
                    entries.add(new BlockStateItemBuilder(Items.SEA_PICKLE, "seaPickle4").add("pickles", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.TURTLE_EGG, "turtleEgg4").add("eggs", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.CAKE, "sliceOfCake").add("bites", 6).get());
                    entries.add(new BlockStateItemBuilder(Items.TNT, "unstableTnt").add("unstable", true).get());
                    entries.add(new BlockStateItemBuilder(Items.REDSTONE, "poweredRedstone").add("power", 15).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_CATALYST, "sculkCatalystBloom").add("bloom", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_SHRIEKER, "sculkShriekerCanSummon").add("can_summon", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_SHRIEKER, "sculkShriekerLocked").add("shrieking", true).get());
                    entries.add(new BlockStateItemBuilder(Items.GLOW_LICHEN, "glowLichenBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_VEIN, "sculkVeinBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    entries.add(new BlockStateItemBuilder(Items.CHEST, "leftChest").add("type", "left").get());
                    entries.add(new BlockStateItemBuilder(Items.CHEST, "rightChest").add("type", "right").get());
                    entries.add(new BlockStateItemBuilder(Items.TRAPPED_CHEST, "leftTrappedChest").add("type", "left").get());
                    entries.add(new BlockStateItemBuilder(Items.TRAPPED_CHEST, "rightTrappedChest").add("type", "right").get());
                    addItemTag(ItemTags.DOORS, item -> addHalfUpper(entries, item, "halfDoor"));
                    addTallFlowers(entries);
                    addItemTag(ItemTags.LEAVES, item -> entries.add(new BlockStateItemBuilder(item, "nonPersistentLeaves", item).add("persistent", false).get()));
                    addItemTag(ItemTags.CANDLES, item -> entries.add(new BlockStateItemBuilder(item, "litCandle", item).add("lit", true).get()));
                    addItemTag(ItemTags.BEDS, item -> entries.add(new BlockStateItemBuilder(item, "bedHeadPart", item).add("part", "head").get()));
                    addItemTag(ItemTags.BEDS, item -> entries.add(new BlockStateItemBuilder(item, "lockedBed", item).add("occupied", true).get()));
                    entries.add(new BlockStateItemBuilder(Items.MANGROVE_ROOTS, "waterloggedMangroveRoots").add("waterlogged", true).get());
                    addItemTag(ItemTags.SLABS, item -> entries.add(new BlockStateItemBuilder(item, "waterloggedBlock", item).add("type", "double").add("waterlogged", true).get()));
                }).build();

        ItemGroup lootChestsItemGroup = FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.fzmm.loot_chests"))
                .icon(() -> new ItemStack(Items.CHEST))
                .entries((displayContext, entries) -> {
                    List<RegistryKey<LootTable>> lootTablesPath = LootTables.getAll().stream()
                            .sorted(Comparator.comparing(t -> t.getValue().getPath()))
                            .collect(Collectors.toList());

                    List<RegistryKey<LootTable>> archeologyLootTablesPath = LootTables.getAll().stream()
                            .sorted(Comparator.comparing(t -> t.getValue().getPath()))
                            .collect(Collectors.toList());

                    archeologyLootTablesPath.removeIf(lootTable -> !lootTable.getValue().getPath().startsWith("archaeology"));

                    lootTablesPath.removeIf(lootTable -> lootTable.getValue().getPath().startsWith("entities"));
                    lootTablesPath.removeIf(archeologyLootTablesPath::contains);

                    addLootChest(entries, Items.SUSPICIOUS_SAND, archeologyLootTablesPath, true);
                    addLootChest(entries, Items.SUSPICIOUS_GRAVEL, archeologyLootTablesPath, true);
                    addLootChest(entries, Items.CHEST, lootTablesPath, false);
                }).build();

        Registry.register(Registries.ITEM_GROUP, USEFUL_BLOCK_STATES_IDENTIFIER, usefulBlockStatesItemGroup);
        Registry.register(Registries.ITEM_GROUP, LOOT_CHESTS_IDENTIFIER, lootChestsItemGroup);
    }

    public static void populateItemGroups() {
        // since 1.21 the item groups and the search bar are initialized from the CreativeInventory constructor,
        // not initializing it here will cause that searching in the item groups will have no results if it was
        // initialized for the first time with ItemGroups#updateDisplayContext
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        new CreativeInventoryScreen(player, player.networkHandler.getEnabledFeatures(), true);
    }

    private static void addSpawnEggs(List<ItemStack> entries) {
        Predicate<ItemStack> hasGroup = stack -> {
            for (var group : Registries.ITEM_GROUP) {
                if (group.contains(stack)) {
                    return true;
                }
            }
            return false;
        };
        for (var item : Registries.ITEM) {
            if (item instanceof SpawnEggItem && !hasGroup.test(item.getDefaultStack())) {
                entries.add(item.getDefaultStack());
            }
        }
    }

    private static void addArmorStand(List<ItemStack> entries) {
        String baseTranslation = "armorStand.";
        ItemStack armorStandWithArms = ArmorStandBuilder.builder()
                .setShowArms()
                .getItem(FzmmUtils.disableItalicConfig(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "arms"), true));
        entries.add(armorStandWithArms);

        ItemStack smallArmorStand = ArmorStandBuilder.builder()
                .setSmall()
                .getItem(FzmmUtils.disableItalicConfig(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "small"), true));
        entries.add(smallArmorStand);

        ItemStack smallArmorStandWithArms = ArmorStandBuilder.builder()
                .setSmall()
                .setShowArms()
                .getItem(FzmmUtils.disableItalicConfig(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "smallWithArms"), true));
        entries.add(smallArmorStandWithArms);
    }

    private static void addItemFrames(List<ItemStack> entries) {
        ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
        ItemStack glowItemFrame = new ItemStack(Items.GLOW_ITEM_FRAME);
        NbtCompound entityTag = new NbtCompound();
        entityTag.putBoolean("Invisible", true);

        itemFrame.apply(DataComponentTypes.ENTITY_DATA, null, entityData -> {
            NbtCompound result = entityTag.copy();
            return TypedEntityData.create(EntityType.ITEM_FRAME, result);
        });
        glowItemFrame.apply(DataComponentTypes.ENTITY_DATA, null, entityData -> {
            NbtCompound result = entityTag.copy();
            return TypedEntityData.create(EntityType.ITEM_FRAME, result);
        });

        itemFrame.apply(DataComponentTypes.CUSTOM_NAME, null, component -> {
            Text translation = Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + ".invisibleItemFrame");
            return FzmmUtils.disableItalicConfig(translation.getString(), true);
        });
        glowItemFrame.apply(DataComponentTypes.CUSTOM_NAME, null, component -> {
            Text translation = Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + ".invisibleGlowItemFrame");
            return FzmmUtils.disableItalicConfig(translation.getString(), true);
        });

        entries.add(itemFrame);
        entries.add(glowItemFrame);
    }

    private static void addNameTags(List<ItemStack> entries) {
        final int LORE_COLOR = 0x1ecbe1;

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("")
                .addLore(getNameTagTranslation("empty", 1), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Dinnerbone")
                .addLore(getNameTagTranslation("dinnerbone", 1), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Grumm")
                .addLore(getNameTagTranslation("grumm", 1), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Toast")
                .addLore(getNameTagTranslation("toast", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("toast", 2), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("jeb_")
                .addLore(getNameTagTranslation("jeb_", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("jeb_", 2), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Johnny")
                .addLore(getNameTagTranslation("johnny", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("johnny", 2), LORE_COLOR)
                .addLore(getNameTagTranslation("johnny", 3), LORE_COLOR).get());
    }

    private static String getNameTagTranslation(String value, int line) {
        String baseTranslation = "nameTag.";
        String commentTranslation = ".comment.";

        return Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + value + commentTranslation + line).getString();
    }

    private static void addCrossbows(List<ItemStack> entries) {
        CrossbowBuilder crossbowArrow = CrossbowBuilder.builder().putProjectile(new ItemStack(Items.ARROW));

        entries.add(crossbowArrow.get());

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);

        firework.apply(DataComponentTypes.FIREWORKS, null,
                component -> new FireworksComponent(2, new ArrayList<>())
        );

        CrossbowBuilder crossbowFirework = CrossbowBuilder.builder().putProjectile(firework);

        entries.add(crossbowFirework.get());
    }

    private static void addTallFlowers(ItemGroup.Entries entries) {
        String suffix = "tallFlowerSelfDestructs";
        // TallFlowerBlock
        addHalfUpper(entries, Items.SUNFLOWER, suffix);
        addHalfUpper(entries, Items.LILAC, suffix);
        addHalfUpper(entries, Items.ROSE_BUSH, suffix);
        addHalfUpper(entries, Items.PEONY, suffix);
        // TallPlantBlock
        addHalfUpper(entries, Items.TALL_GRASS, suffix);
        addHalfUpper(entries, Items.LARGE_FERN, suffix);
        addHalfUpper(entries, Items.PITCHER_PLANT, suffix);
        // SmallDripleafBlock
        addHalfUpper(entries, Items.SMALL_DRIPLEAF, suffix);
    }

    private static void addHalfUpper(ItemGroup.Entries entries, Item item, String translation) {
        entries.add(new BlockStateItemBuilder(item, translation, item).add("half", "upper").get());
    }

    private static void addItemTag(TagKey<Item> tag, Consumer<Item> consumer) {
        Optional<ItemPredicate> predicate = itemPredicate(tag);
        if (predicate.isEmpty()) return;

        for (var item : Registries.ITEM) {
            if (predicate.get().test(new ItemStack(item))) {
                consumer.accept(item);
            }
        }
    }

    private static Optional<ItemPredicate> itemPredicate(TagKey<Item> tag) {
        try {
            return Optional.of(ItemPredicate.Builder.create().tag(Registries.ITEM, tag).build());
        } catch (Exception ignored) {
            FzmmClient.LOGGER.warn("[FzmmItemGroup] Missing tag '{}' (this can be ignored in multiplayer)", tag.id());
            return Optional.empty();
        }
    }

    private static void addLootChest(ItemGroup.Entries entries, Item item, List<RegistryKey<LootTable>> lootTableList, boolean isBrushable) {
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) return;
        for (var lootTable : lootTableList) {
            ItemStack stack = new ItemStack(item);

            String identifierString = lootTable.getValue().toString();

            // Brushable blocks (suspicious sand and suspicious gravel) do not use the
            // container_loot component like other lootable blocks in 1.20.5
            // https://bugs.mojang.com/browse/MC-271530
            if (isBrushable) {
                stack.apply(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.BRUSHABLE_BLOCK, new NbtCompound()), entityData -> {
                    NbtCompound result = entityData.copyNbtWithoutId();

                    result.putString("LootTable", identifierString);

                    return TypedEntityData.create(BlockEntityType.BRUSHABLE_BLOCK, result);
                });
            } else {
                stack.apply(DataComponentTypes.CONTAINER_LOOT, null,
                        containerLootComponent -> new ContainerLootComponent(lootTable, 0));
            }

            stack.apply(DataComponentTypes.CUSTOM_NAME, null, text -> Text.literal(identifierString));

            entries.add(stack);
        }
    }
}
