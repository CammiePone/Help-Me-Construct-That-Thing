package dev.cammiescorner.hmctt;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class HmcttModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> HmcttConfig.getScreen(parent, HmcttClient.MOD_ID);
	}
}
