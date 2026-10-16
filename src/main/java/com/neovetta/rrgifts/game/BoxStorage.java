package com.neovetta.rrgifts.game;

import com.mojang.serialization.Codec;
import com.neovetta.rrgifts.RussianRouletteGifts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Mobs held by filled boxes, keyed by box id. Lives in the overworld data storage, never on the item. */
public final class BoxStorage extends SavedData {
	private static final Codec<BoxStorage> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, CompoundTag.CODEC.listOf())
			.xmap(BoxStorage::new, s -> s.boxes);

	// One shared instance: SavedDataType is a record and the storage caches by equality.
	// A real DataFixTypes (not null): readTagFromDisk calls .update() on it unguarded when the file exists.
	public static final SavedDataType<BoxStorage> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(RussianRouletteGifts.MOD_ID, "boxes"),
			BoxStorage::new,
			CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Map<UUID, List<CompoundTag>> boxes = new HashMap<>();

	public BoxStorage() {}

	private BoxStorage(Map<UUID, List<CompoundTag>> loaded) {
		loaded.forEach((id, list) -> boxes.put(id, new ArrayList<>(list)));
	}

	public static BoxStorage get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public int count(UUID id) {
		List<CompoundTag> l = boxes.get(id);
		return l == null ? 0 : l.size();
	}

	public void add(UUID id, CompoundTag entity) {
		boxes.computeIfAbsent(id, k -> new ArrayList<>()).add(entity);
		setDirty();
	}

	public List<CompoundTag> take(UUID id) {
		List<CompoundTag> removed = boxes.remove(id);
		if (removed == null) return List.of();
		setDirty();
		return removed;
	}
}
