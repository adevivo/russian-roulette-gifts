package com.neovetta.rrgifts;

import com.neovetta.rrgifts.game.BoxCapture;
import com.neovetta.rrgifts.game.ConfigLoader;
import com.neovetta.rrgifts.game.EmptyGiftCondition;
import com.neovetta.rrgifts.game.GiftCommand;
import com.neovetta.rrgifts.game.GiftItems;
import com.neovetta.rrgifts.game.GiftOpenHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RussianRouletteGifts implements ModInitializer {
	public static final String MOD_ID = "russianroulettegifts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		EmptyGiftCondition.register();
		ConfigLoader.register();
		GiftOpenHandler.register();
		BoxCapture.register();
		GiftCommand.register();
		GiftItems.registerLoreRestore();
		LOGGER.info("Russian Roulette Gifts initialized");
	}
}
