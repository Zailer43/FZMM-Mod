package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.*;

public class FilledMapBuilder {
    private ItemStack stack;

    private FilledMapBuilder() {
        this.stack = new ItemStack(Items.FILLED_MAP);
    }

    public static FilledMapBuilder builder() {
        return new FilledMapBuilder();
    }

    public FilledMapBuilder of(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemStack get() {
        return this.stack;
    }

    public Optional<Integer> id() {
        if (!this.stack.hasNbt())
            return Optional.empty();

        NbtCompound nbtCompound = this.stack.getNbt();
        assert nbtCompound != null;

        return nbtCompound.contains(TagsConstant.FILLED_MAP_ID, NbtElement.INT_TYPE) ? Optional.of(nbtCompound.getInt(TagsConstant.FILLED_MAP_ID)) : Optional.empty();
    }

    public FilledMapBuilder id(int id) {
        this.stack.getOrCreateNbt().putInt(TagsConstant.FILLED_MAP_ID, id);
        return this;
    }

    public Optional<FilledMapBuilder.FilledMapData> data() {
        Optional<Integer> id = this.id();
        Optional<MapState> mapState = this.mapState();

        if (id.isEmpty() || mapState.isEmpty())
            return Optional.empty();

        return Optional.of(new FilledMapBuilder.FilledMapData(id.get(), mapState.get()));
    }

    public Optional<MapState> mapState() {
        Optional<Integer> mapId = this.id();
        if (mapId.isEmpty())
            return Optional.empty();

        return FilledMapBuilder.mapState(mapId.get());
    }

    public static Optional<MapState> mapState(Integer id) {
        MapState mapState = FilledMapItem.getMapState(id, MinecraftClient.getInstance().world);
        if (mapState == null || mapState.colors == null)
            return Optional.empty();

        return Optional.of(mapState);
    }

    public static Map<String, MapState> getAllMapStates() {
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;

        return world.getMapStates();
    }

    public static Map<String, MapState> getAllNoEmptyMapStates() {
        Map<String, MapState> mapStates = new HashMap<>(FilledMapBuilder.getAllMapStates());

        // Removes all maps that are empty and completely transparent
        mapStates.entrySet().removeIf(entry -> {
            for (var color : entry.getValue().colors) {
                if (color != 0)
                    return false;
            }

            return true;
        });

        return mapStates;
    }

    public static List<FilledMapData> getSortedMapStates() {
        Map<String, MapState> mapStates = FilledMapBuilder.getAllNoEmptyMapStates();
        String mapName = FilledMapItem.getMapName(0);
        mapName = mapName.replace("0", "");

        List<FilledMapData> result = new ArrayList<>();
        for (var entry : mapStates.entrySet()) {
            String mapIdStr = entry.getKey().replace(mapName, "");
            int mapId;

            try {
                mapId = Integer.parseInt(mapIdStr);
            } catch (NumberFormatException e) {
                FzmmClient.LOGGER.warn("[FilledMapBuilder] Failed to parse map id: " + mapIdStr);
                continue;
            }

            result.add(new FilledMapData(mapId, entry.getValue()));
        }

        result.sort(Comparator.comparingInt(FilledMapData::id));

        return result;
    }

    public record FilledMapData(Integer id, MapState mapState) {

    }
}
