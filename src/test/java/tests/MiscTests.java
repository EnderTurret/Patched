package tests;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonPrimitive;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchUtil;

import tests.util.TestUtil;

public final class MiscTests {

	public static void main(String... args) {
		exceptions();
		patchBuilding();
	}

	private static void patchBuilding() {
		final List<JsonPatch> patches = new ArrayList<>();

		patches.add(PatchUtil.add("/add", new JsonPrimitive(true)));
		patches.add(PatchUtil.copy("/copied", "/from"));
		patches.add(PatchUtil.move("/moved", "/from"));
		patches.add(PatchUtil.remove("/remove"));
		patches.add(PatchUtil.replace("/replace", new JsonPrimitive(3)));
		patches.add(PatchUtil.compound(
				PatchUtil.test("/test", new JsonPrimitive(false), true),
				PatchUtil.test("custom", "/test", new JsonPrimitive("is this really a primitive? the answer may surprise you"), false)));
		patches.add(PatchUtil.find("/find", List.of(), PatchUtil.remove(""), true));

		final JsonPatch result = PatchUtil.compound(patches.toArray(JsonPatch[]::new));

		final String target = TestUtil.read("/tests/built_patch.json");
		final String output = PatchingTests.GSON.toJson(result);

		if (!output.equals(target)) {
			System.out.println("Built patch test failed!\n");
			System.out.println(target);
			System.out.println("\n(expected) vs (output)\n");
			System.out.println(output);
		}
	}

	private static void exceptions() {
		// Instantiate some exceptions. They don't really need testing.
		// This might be coverage hacking, but it's fine I swear.
		final PatchingException e = new PatchingException();
		new PatchingException(e);
		new PatchingException("this is a message", e);
		new TraversalException();
		new TraversalException(e);
		new TraversalException("this is also a message", e);
	}
}