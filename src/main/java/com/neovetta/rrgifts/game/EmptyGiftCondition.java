package com.neovetta.rrgifts.game;

import com.mojang.serialization.MapCodec;
import com.neovetta.rrgifts.RussianRouletteGifts;
import com.neovetta.rrgifts.core.EmptyGiftFlag;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

/** "russianroulettegifts:empty_gift_enabled": gates the empty-box recipe on the config flag. */
public final class EmptyGiftCondition implements ResourceCondition {
	public static final MapCodec<EmptyGiftCondition> CODEC = MapCodec.unit(EmptyGiftCondition::new);
	public static final ResourceConditionType<EmptyGiftCondition> TYPE = ResourceConditionType.create(
			Identifier.fromNamespaceAndPath(RussianRouletteGifts.MOD_ID, "empty_gift_enabled"), CODEC);

	public static void register() {
		ResourceConditions.register(TYPE);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(ConfigLoader.FILE_NAME);
		if (!Files.exists(path)) return true;
		try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			return EmptyGiftFlag.read(r);
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.warn("Could not read {} for the empty gift recipe condition; assuming enabled", path);
			return true;
		}
	}
}
