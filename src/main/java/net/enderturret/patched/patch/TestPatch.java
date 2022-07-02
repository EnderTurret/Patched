package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that performs an existence check before allowing other patches to proceed.
 * @see PatchUtil#test(String, JsonElement, boolean)
 * @author EnderTurret
 */
public class TestPatch extends JsonPatch {

	protected final JsonSelector path;
	protected final JsonElement test;
	protected final boolean inverse;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#test(String, JsonElement, boolean)} instead.
	 * @param path The path to the element to test.
	 * @param test The test element. May be {@code null}.
	 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
	 */
	protected TestPatch(String path, JsonElement test, boolean inverse) {
		super(null);
		this.path = JsonSelector.of(path);
		this.test = test;
		this.inverse = inverse;
	}

	/**
	 * <p>Attempts to test if this patch matches the given root element.</p>
	 * <p>The element passed in is unlikely to be the element that is tested.</p>
	 * @param root The root element.
	 * @param context The {@link PatchContext}. This customizes what features are available, among other things.
	 * @return {@code true} if the test passes.
	 * @throws PatchingException If {@link PatchContext#throwOnFailedTest()} is enabled.
	 */
	public boolean test(JsonElement root, PatchContext context) {
		final ElementContext ctx;

		try {
			ctx = path.select(root, context.throwOnFailedTest());
		} catch (TraversalException e) {
			throw new PatchingException("Test failed: " + e.getMessage());
		}

		final boolean inverse = context.sbExtensions() && this.inverse;

		final boolean result = _test(ctx, inverse);

		if (!result && context.throwOnFailedTest())
			throw new PatchingException("Test " + (ctx == null ? "null" : ctx.elem()) + " " + (inverse ? "!=" : "==") + " " + test + " failed.");

		return result;
	}

	private boolean _test(ElementContext ctx, boolean inverse) {
		if (ctx != null)
			if (test(ctx.elem()))
				return !inverse;

		return inverse;
	}

	private boolean test(JsonElement elem) {
		return test == null || test.equals(elem);
	}

	@Override
	public void patch(JsonElement root, PatchContext context) {}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) {}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		if (test != null)
			obj.add("value", test);
		if (inverse)
			obj.addProperty("inverse", inverse);
	}

	@Override
	protected JsonElement write(JsonSerializationContext context, String omitOperation) {
		final JsonObject obj = new JsonObject();

		if (!operation().equals(omitOperation))
			obj.addProperty("op", operation());

		obj.addProperty("path", path.toString());

		writeAdditional(obj, context);

		return obj;
	}

	@Override
	protected String operation() {
		return "test";
	}
}