package org.bladecoder.engine.util;

import org.bladecoder.engine.actions.Action;
import org.bladecoder.engine.actions.ActionCallback;
import org.bladecoder.engine.model.BaseActor;
import org.bladecoder.engine.model.Scene;
import org.bladecoder.engine.model.Verb;
import org.bladecoder.engine.model.World;

public class ActionCallbackSerialization {
	public static final String SEPARATION_SYMBOL = "#";

	private static String find(ActionCallback cb, Verb v) {
		String id = v.getId();

		int pos = 0;

		for (Action a : v.getActions()) {
			if (cb == a) {
				StringBuilder stringBuilder = new StringBuilder(id);
				stringBuilder.append(SEPARATION_SYMBOL).append(pos);

				return stringBuilder.toString();
			}

			pos++;
		}

		return null;
	}

	private static String find(ActionCallback cb, BaseActor a) {
		if(a == null) return null;
		
		String id = a.getId();

		for (Verb v : a.getVerbs().values()) {
			String result = find(cb, v);

			if (result != null) {
				StringBuilder stringBuilder = new StringBuilder(id);
				stringBuilder.append(SEPARATION_SYMBOL).append(result);

				return stringBuilder.toString();
			}
		}

		return null;
	}

	public static String find(ActionCallback cb) {
		String id = null;

		if (cb == null)
			return null;

		// search in scene verbs
		Scene s = World.getInstance().getCurrentScene();

		id = find(cb, s);

		if (id != null)
			return id;

		id = find(cb, s.getPlayer());
		if (id != null)
			return id;

		// search in actors
		for (BaseActor a : s.getActors().values()) {
			id = find(cb, a);
			if (id != null)
				return id;
		}

		// search in defaultVerbs
		for (Verb v : BaseActor.getDefaultVerbs().values()) {
			id = find(cb, v);
			if (id != null) {
				StringBuilder stringBuilder = new StringBuilder("DEFAULT_VERB");
				stringBuilder.append(SEPARATION_SYMBOL).append(id);

				return stringBuilder.toString();
			}
		}

		return null;
	}

	public static ActionCallback find(String id) {
		Scene s = World.getInstance().getCurrentScene();

		String[] split = id.split(SEPARATION_SYMBOL);

		if (split.length < 3)
			return null;

		String actorId = split[0];
		String verbId = split[1];
		int actionPos = Integer.parseInt(split[2]);

		
		Verb v = null;
		
		if (actorId.equals("DEFAULT_VERB")) {

			v = BaseActor.getDefaultVerbs().get(verbId);
		} else {

			BaseActor a;

			if (actorId.equals(s.getId()))
				a = s;
			else
				a = s.getActor(actorId);

			if (a == null)
				return null;

			v = a.getVerbs().get(verbId);
		}

		if (v == null)
			return null;

		Action action = v.getActions().get(actionPos);

		if (action instanceof ActionCallback)
			return (ActionCallback) action;

		return null;
	}
}