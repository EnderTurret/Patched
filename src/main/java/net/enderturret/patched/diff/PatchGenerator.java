package net.enderturret.patched.diff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchUtil;

/**
 * <p>Attempts to generate patches given source Json and target Json.</p>
 * <p>Note: this is not currently fully functional; it can fail spectacularly. Use with caution.</p>
 * @author EnderTurret
 */
public class PatchGenerator {

	/**
	 * Using the given original Json and target Json, attempts to create a patch that when applied to the original, creates an output equal to the target.
	 * @param orig The original or "source" Json.
	 * @param target The target Json.
	 * @param insertExactTests Whether to insert {@code test} patches before inserting {@code replace} patches.
	 * @param compressPatch If the output is a single patch, this determines whether to return that patch or wrap it in a compound patch anyway.
	 * @return The patch.
	 */
	public static JsonPatch diff(JsonElement orig, JsonElement target, boolean insertExactTests, boolean compressPatch) {
		final List<JsonPatch> patches = new ArrayList<>();
		final Deque<String> path = new ArrayDeque<>();
		final Set<String> keys = new HashSet<>();

		diffOne(orig, target, insertExactTests, patches, path);

		return compressPatch && patches.size() == 1 ? patches.get(0) : PatchUtil.compound(patches.toArray(JsonPatch[]::new));
	}

	private static void diffOne(JsonElement orig, JsonElement target, boolean insertExactTests, List<JsonPatch> patches, Deque<String> path) {
		if (orig instanceof JsonObject o && target instanceof JsonObject t) {
			final Set<String> keys = new HashSet<>();

			keys.addAll(o.keySet());
			keys.addAll(t.keySet());

			for (String k : keys) {
				path.add(k);

				if (!o.has(k))
					patches.add(PatchUtil.add(join(path), t.get(k)));
				else if (!t.has(k))
					patches.add(PatchUtil.remove(join(path)));
				else {
					diffOne(o.get(k), t.get(k), insertExactTests, patches, path);
				}

				path.removeLast();
			}

			return;
		}

		else if (orig instanceof JsonArray o && target instanceof JsonArray t) {
			/*
			 * Arrays are difficult. Consider:
			 *
			 * [1, 2, 3] => [1, 2, 4, 3]
			 *
			 * We can tell this is "add" because the size increases.
			 *
			 * [1, 2, 3] => [1, 2, 4]
			 *
			 * We can tell this is "replace" because the size is still 3.
			 *
			 * [1, 2, 3] => [1, 2, 4, 5]
			 *
			 * This is ambiguous: it could be that 4 was added and 3 was replaced, or
			 * it could be that 5 was added and 3 was replaced.
			 *
			 * These are equivalent:
			 * [
			 *   {
			 *     "op": "replace",
			 *     "path": "/2",
			 *     "value": 4
			 *   },
			 *   {
			 *     "op": "add",
			 *     "path": "/-",
			 *     "value": 5
			 *   }
			 * ]
			 *
			 * [
			 *   {
			 *     "op": "replace",
			 *     "path": "/2",
			 *     "value": 5
			 *   },
			 *   {
			 *     "op": "add",
			 *     "path": "/2",
			 *     "value": 4
			 *   }
			 * ]
			 */

			// If the arrays share no common elements, prefer replacing it completely.
			// If the target array is empty, prefer removing elements instead.
			boolean replaceArray = !t.isEmpty();

			for (JsonElement e : t)
				if (o.contains(e)) {
					replaceArray = false;
					break;
				}

			if (replaceArray) {
				patches.add(PatchUtil.replace(join(path), t));
				return;
			}

			final int sizeO = o.size();
			final int sizeT = t.size();
			final int maxLen = Math.max(sizeO, sizeT);

			for (int i = 0; i < maxLen; i++) {
				if (i >= sizeO) {
					// It's all add ops from here on out.
					patches.add(PatchUtil.add(join(path) + "/-", t.get(i)));
				} else if (i >= sizeT) {
					// It's all remove ops from here on out.
					patches.add(PatchUtil.remove(join(path) + "/" + sizeT));
				} else {
					path.add(Integer.toString(i));
					diffOne(o.get(i), t.get(i), insertExactTests, patches, path);
					path.removeLast();
				}
			}

			return;
		}

		if (!orig.equals(target)) {
			JsonPatch replace = PatchUtil.replace(join(path), target);

			if (insertExactTests)
				replace = PatchUtil.compound(PatchUtil.test(join(path), orig, false), replace);

			patches.add(replace);
		}
	}

	private static String join(Collection<String> path) {
		return "/" + String.join("/", path);
	}

	private static record Pair(JsonElement orig, JsonElement target) {}
}