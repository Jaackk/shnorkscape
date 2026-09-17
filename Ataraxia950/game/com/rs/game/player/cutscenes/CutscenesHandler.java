package com.rs.game.player.cutscenes;

import com.rs.game.player.content.araxxor.AraxxiStartCS;
import com.rs.game.player.content.araxxor.LightPathCutscene;
import com.rs.utils.Logger;

import java.util.HashMap;

public class CutscenesHandler {

	private static final HashMap<Object, Class<Cutscene>> handledCutscenes = new HashMap<Object, Class<Cutscene>>();

	public static final Cutscene getCutscene(Object key) {
		Class<Cutscene> classC = handledCutscenes.get(key);
		if (classC == null)
			return null;
		try {
			return classC.newInstance();
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static final void init() {
		try {
			Class<Cutscene> value1 = (Class<Cutscene>) Class.forName(EdgeWilderness.class.getCanonicalName());
			handledCutscenes.put("EdgeWilderness", value1);
			Class<Cutscene> value2 = (Class<Cutscene>) Class.forName(DTPreview.class.getCanonicalName());
			handledCutscenes.put("DTPreview", value2);
			Class<Cutscene> value3 = (Class<Cutscene>) Class.forName(NexCutScene.class.getCanonicalName());
			handledCutscenes.put("NexCutScene", value3);
			Class<Cutscene> value4 = (Class<Cutscene>) Class.forName(TowersPkCutscene.class.getCanonicalName());
			handledCutscenes.put("TowersPkCutscene", value4);
			Class<Cutscene> value5 = (Class<Cutscene>) Class.forName(CorporealBeastScene.class.getCanonicalName());
			handledCutscenes.put("CorporealBeastScene", value5);
			Class<Cutscene> value6 = (Class<Cutscene>) Class.forName(PortsNoticeboard.class.getCanonicalName());
			handledCutscenes.put("PortsNoticeboard", value6);
			Class<Cutscene> value7 = (Class<Cutscene>) Class.forName(RiseOfTheSixCutscene.class.getCanonicalName());
			handledCutscenes.put("RiseOfTheSixCutscene", value7);
			Class<Cutscene> value8 = (Class<Cutscene>) Class.forName(TestCutScene.class.getCanonicalName());
			handledCutscenes.put("TestCutScene", value8);
			Class<Cutscene> raxCS = (Class<Cutscene>) Class.forName(LightPathCutscene.class.getCanonicalName());
			handledCutscenes.put("LightPathCutscene", raxCS);
			Class<Cutscene> raxiCS = (Class<Cutscene>) Class.forName(AraxxiStartCS.class.getCanonicalName());
			handledCutscenes.put("AraxxiStartCS", raxiCS);
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static final void reload() {
		handledCutscenes.clear();
		init();
	}
}
