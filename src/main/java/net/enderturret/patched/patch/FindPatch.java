package net.enderturret.patched.patch;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

		obj.add("test", JsonPatch.Serializer.ENFORCING_GSON.toJsonTree(tests.size() == 1 ? tests.get(0) : tests));
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

				then.patch(parent.child(key, elem), context);

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

				then.patch(parent.child(i, elem), context);

				if (!multi)
					return;
			}
		}
	}
}