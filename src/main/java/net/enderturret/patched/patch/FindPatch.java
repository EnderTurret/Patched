package net.enderturret.patched.patch;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.exception.PatchingException;

/**
 * A patch that finds an element matching some criteria and applies a patch to it.
 * @see PatchUtil#find(String, List, JsonPatch, boolean)
 * @author EnderTurret
 * @since 1.0.0
 */
public final class FindPatch extends JsonPatch {

	private final List<TestPatch> tests;
	private final JsonPatch then;
	private final boolean multi;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#find(String, List, JsonPatch, boolean)} instead.
	 * @param path The path to the element to find things in.
	 * @param tests A list of tests that an element must pass to have {@code then} applied to it.
	 * @param then A patch to apply to elements passing the tests.
	 * @param multi Whether to continue searching for matching elements after the first one is found.
	 * @since 1.0.0
	 */
	protected FindPatch(String path, List<TestPatch> tests, JsonPatch then, boolean multi) {
		super(path);
		this.tests = tests;
		this.then = then;
		this.multi = multi;
	}

	@Override
	protected String operation() {
		return "find";
	}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		if (multi)
			obj.addProperty("multi", multi);

		if (tests.size() == 1)
			obj.add("test", tests.get(0).write(context, "test"));
		else {
			final JsonArray list = new JsonArray();

			for (TestPatch test : tests)
				list.add(test.write(context, "test"));

			obj.add("test", list);
		}

		obj.add("then", context.serialize(then));
	}

	private boolean testAll(JsonElement elem, PatchContext context) {
		for (TestPatch tp : tests)
			if (!tp.test(elem, context))
				return false;

		return true;
	}

	@Override
	protected void patchJson(ElementContext parent, PatchContext context) throws PatchingException {
		if (!context.patchedExtensions())
			throw new PatchingException("find: Patched extensions are not enabled.");

		parent = last.select(parent, true);

		String strPath = null;

		if (parent.elem() instanceof JsonObject o) {
			// Copy the set first, so we don't encounter CMEs.
			// We're using LinkedHashSet instead of Set.of to avoid element re-shuffling.
			// This is necessary as otherwise you will encounter inconsistent element modification.
			// See PatchingTests "find/remove_unspecific" for more information.
			for (String key : new LinkedHashSet<>(o.keySet())) {
				final JsonElement elem = o.get(key);

				if (!testAll(elem, context))
					continue;

				// Tests succeeded, apply patch.

				if (context.audit() != null) context.audit().beginPrefix((strPath == null ? strPath = path.toString() + "/" + last.toString() : strPath), key);
				then.patch(parent.child(key, elem), context);
				if (context.audit() != null) context.audit().endPrefix();

				if (!multi)
					return;
			}
		}
		else if (parent.elem() instanceof JsonArray a) {
			// Use traditional iteration so we don't encounter surprise CMEs.
			for (int i = 0; i < a.size(); i++) {
				final JsonElement elem = a.get(i);

				if (!testAll(elem, context))
					continue;

				// Tests succeeded, apply patch.

				if (context.audit() != null) context.audit().beginPrefix((strPath == null ? strPath = path.toString() + "/" + last.toString() : strPath), Integer.toString(i));
				then.patch(parent.child(i, elem), context);
				if (context.audit() != null) context.audit().endPrefix();

				if (!multi)
					return;
			}
		}
	}
}