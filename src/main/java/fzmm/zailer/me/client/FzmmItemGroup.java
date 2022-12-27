package fzmm.zailer.me.client;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.BlockStateItemBuilder;
import fzmm.zailer.me.builders.CrossbowBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.LightBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.*;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.raid.Raid;

import java.util.Comparator;
import java.util.List;

public class FzmmItemGroup {
    public static final String OPERATOR_BASE_TRANSLATION_KEY = "itemGroup.op.";
    public static final String USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY = "itemGroup.fzmm.useful_block_states.";

    public static void register() {

        ItemGroup oldOperatorItemGroupReference = ItemGroups.OPERATOR;
        ItemGroups.OPERATOR = ItemGroup.create(ItemGroup.Row.BOTTOM, 5)
                .displayName(Text.translatable("itemGroup.op"))
                .icon(() -> new ItemStack(Items.COMMAND_BLOCK))
                .special()
                .entries((enabledFeatures, entries, operatorEnabled) -> {
                    entries.add(Items.COMMAND_BLOCK);
                    entries.add(Items.CHAIN_COMMAND_BLOCK);
                    entries.add(Items.REPEATING_COMMAND_BLOCK);
                    entries.add(Items.COMMAND_BLOCK_MINECART);
                    entries.add(Items.JIGSAW);
                    entries.add(Items.STRUCTURE_BLOCK);
                    entries.add(Items.STRUCTURE_VOID);
                    entries.add(Items.BARRIER);
                    entries.add(Items.DEBUG_STICK);

                    entries.add(Items.DRAGON_EGG);
                    entries.add(Items.FILLED_MAP);
                    entries.add(Items.WRITTEN_BOOK);
                    entries.add(Items.ENCHANTED_BOOK);
                    entries.add(Items.KNOWLEDGE_BOOK);
                    entries.add(Items.SUSPICIOUS_STEW);
                    entries.add(Items.POTION);
                    entries.add(Items.SPLASH_POTION);
                    entries.add(Items.LINGERING_POTION);
                    entries.add(Items.TIPPED_ARROW);

                    addArmorStand(entries);
                    addItemFrames(entries);
                    addNameTags(entries);
                    addCrossbows(entries);
                    addUnobtainablePaintings(entries);
                    entries.add(Raid.getOminousBanner());

                    ItemStack elytra = new ItemStack(Items.ELYTRA);
                    elytra.setDamage(elytra.getMaxDamage() - 1);
                    entries.add(elytra);

                    for (int i = 15; i >= 0; --i)
                        entries.add(LightBlock.addNbtForLevel(new ItemStack(Items.LIGHT), i));
                }).build();

        ItemGroup[] itemGroups = new ItemGroup[ItemGroups.GROUPS.size()];
        for (int i = 0; i != ItemGroups.GROUPS.size(); i++)
            itemGroups[i] = ItemGroups.GROUPS.get(i) == oldOperatorItemGroupReference ? ItemGroups.OPERATOR : ItemGroups.GROUPS.get(i);

        ItemGroups.GROUPS = ItemGroups.collect(itemGroups);

        FabricItemGroup.builder(new Identifier(FzmmClient.MOD_ID, "useful_block_states"))
                .icon(() -> new ItemStack(Items.REDSTONE_LAMP))
                .entries((enabledFeatures, entries, operatorEnabled) -> {

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
                    entries.add(new BlockStateItemBuilder(Items.END_PORTAL_FRAME, "endPortalFrameWithEye").add("eye", true).get());
                    entries.add(new BlockStateItemBuilder(Items.LANTERN, "hangingLantern").add("hanging", true).get());
                    entries.add(new BlockStateItemBuilder(Items.LANTERN, "lanternOnTheFloor").add("hanging", false).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_LANTERN, "hangingSoulLantern").add("hanging", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_LANTERN, "soulLanternOnTheFloor").add("hanging", false).get());
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
                    addHalfDoors(entries);
                    addTallFlowers(entries);
                    addLeaves(entries);
                    addLitCandles(entries);
                    addHalfBed(entries);
                    addLockedBed(entries);
                    entries.add(new BlockStateItemBuilder(Items.MANGROVE_ROOTS, "waterloggedMangroveRoots").add("waterlogged", true).get());
                    addWaterloggedBlocks(entries);
                }).build();

        FabricItemGroup.builder(new Identifier(FzmmClient.MOD_ID, "loot_chests"))
                .icon(() -> new ItemStack(Items.CHEST))
                .entries((enabledFeatures, entries, operatorEnabled) -> {
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
                        entries.add(chest);
                    }
                }).build();

        FabricItemGroup.builder(new Identifier(FzmmClient.MOD_ID, "player_heads"))
                .icon(() -> new ItemStack(Items.PLAYER_HEAD))
                .entries((enabledFeatures, entries, operatorEnabled) -> {

                    ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                    if (clientPlayer != null) {
                        List<GameProfile> profileList = clientPlayer.networkHandler.getPlayerList().stream()
                                .map(PlayerListEntry::getProfile)
                                .sorted(Comparator.comparing(GameProfile::getName))
                                .toList();

                        for (GameProfile profile : profileList) {
                            entries.add(HeadUtils.getPlayerHead(profile));
                        }
                    }

                }).build();

    }

    private static void addArmorStand(ItemGroup.Entries entries) {
        String baseTranslation = "armorStand.";
        ItemStack armorStandWithArms = ArmorStandBuilder.builder()
                .setShowArms()
                .getItem(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + baseTranslation + "arms"));
        entries.add(armorStandWithArms);

        ItemStack smallArmorStand = ArmorStandBuilder.builder()
                .setSmall()
                .getItem(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + baseTranslation + "small"));
        entries.add(smallArmorStand);

        ItemStack smallArmorStandWithArms = ArmorStandBuilder.builder()
                .setSmall()
                .setShowArms()
                .getItem(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + baseTranslation + "smallWithArms"));
        entries.add(smallArmorStandWithArms);
    }

    private static void addItemFrames(ItemGroup.Entries entries) {
        ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
        ItemStack glowItemFrame = new ItemStack(Items.GLOW_ITEM_FRAME);
        NbtCompound entityTag = new NbtCompound();

        entityTag.putBoolean("Invisible", true);
        itemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);
        glowItemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

        String itemFrameName = Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "invisibleItemFrame").getString();
        String glowItemFrameName = Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "invisibleGlowItemFrame").getString();

        itemFrame.setCustomName(Text.literal(itemFrameName).setStyle(Style.EMPTY.withItalic(false)));
        glowItemFrame.setCustomName(Text.literal(glowItemFrameName).setStyle(Style.EMPTY.withItalic(false)));

        entries.add(itemFrame);
        entries.add(glowItemFrame);
    }

    private static void addNameTags(ItemGroup.Entries entries) {
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

        return Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + baseTranslation + value + commentTranslation + line).getString();
    }

    private static void addCrossbows(ItemGroup.Entries entries) {
        CrossbowBuilder crossbowArrow = CrossbowBuilder.builder()
                .setCharged(true)
                .putProjectile(new ItemStack(Items.ARROW));

        entries.add(crossbowArrow.get());

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);

        NbtCompound fireworkTag = new NbtCompound();
        fireworkTag.putInt(FireworkRocketItem.FLIGHT_KEY, 2);
        firework.setSubNbt(FireworkRocketItem.FIREWORKS_KEY, fireworkTag);

        CrossbowBuilder crossbowFirework = CrossbowBuilder.builder()
                .setCharged(true)
                .putProjectile(firework);

        entries.add(crossbowFirework.get());
    }

    private static void addLeaves(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.LEAVES))
                entries.add(new BlockStateItemBuilder(item, "nonPersistentLeaves", item).add("persistent", false).get());
        }
    }

    private static void addHalfDoors(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.DOORS))
                addHalfUpper(entries, item, "halfDoor");
        }
    }

    private static void addTallFlowers(ItemGroup.Entries entries) {
        String suffix = "tallFlowerSelfDestructs";
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.TALL_FLOWERS))
                addHalfUpper(entries, item, suffix);
        }
        addHalfUpper(entries, Items.TALL_GRASS, suffix);
        addHalfUpper(entries, Items.LARGE_FERN, suffix);
        addHalfUpper(entries, Items.SMALL_DRIPLEAF, suffix);
    }

    private static void addHalfUpper(ItemGroup.Entries entries, Item item, String translation) {
        entries.add(new BlockStateItemBuilder(item, translation, item).add("half", "upper").get());
    }

    private static void addLitCandles(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.CANDLES))
                entries.add(new BlockStateItemBuilder(item, "litCandle", item).add("lit", true).get());
        }
    }

    private static void addHalfBed(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.BEDS))
                entries.add(new BlockStateItemBuilder(item, "bedHeadPart", item).add("part", "head").get());
        }
    }

    private static void addLockedBed(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.BEDS))
                entries.add(new BlockStateItemBuilder(item, "lockedBed", item).add("occupied", true).get());
        }
    }

    private static void addWaterloggedBlocks(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.SLABS))
                entries.add(new BlockStateItemBuilder(item, "waterloggedBlock", item).add("type", "double").add("waterlogged", true).get());
        }
    }

    private static void addUnobtainablePaintings(ItemGroup.Entries entries) {
        // the server is lower than 1.18, the tag does not exist
        if (!Registries.PAINTING_VARIANT.iterateEntries(PaintingVariantTags.PLACEABLE).iterator().hasNext())
            return;

        for (var painting : Registries.PAINTING_VARIANT) {
            if (!contains(painting)) {
                // if there is no translation in the mod of that painting, the id of the variant is used,
                // to prevent a translation key from appearing if a mod that adds non-placeable paintings is used
                String variantName = Registries.PAINTING_VARIANT.getId(painting).getPath();
                String translationKey = "entity.minecraft.painting." + variantName;
                String translation = Text.translatable(translationKey).getString();
                String name = variantName;
                if (!translation.equals(translationKey))
                    name = translation;

                ItemStack paintingStack = DisplayBuilder.builder().item(Items.PAINTING).setName(name).get();
                NbtCompound entityTag = new NbtCompound();
                entityTag.put("variant", NbtString.of(variantName));
                paintingStack.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

                entries.add(paintingStack);
            }
        }
    }

    private static boolean contains(PaintingVariant paintingVariant) {
        for (RegistryEntry<PaintingVariant> paintingVariantEntry : Registries.PAINTING_VARIANT.iterateEntries(PaintingVariantTags.PLACEABLE)) {
            if (paintingVariantEntry.value().equals(paintingVariant))
                return true;
        }
        return false;
    }

    private static boolean contains(Item item, TagKey<Item> tag) {
        return ItemPredicate.Builder.create().tag(tag).build().test(new ItemStack(item));
    }
}
