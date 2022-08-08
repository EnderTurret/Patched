package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that performs a check of some kind before allowing other patches to proceed.
 * @see PatchUtil#test(String, JsonElement, boolean)
 * @author EnderTurret
 */
public class TestPatch extends JsonPatch {

	protected final String type;
	protected final JsonSelector path;
	protected final JsonElement test;
	protected final boolean inverse;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#test(String, JsonElement, boolean)} instead.
	 * @param type If non-{@code null}, specifies a custom type for {@link ITestEvaluator}.
	 * @param path The path to the element to test. May be {@code null} only if {@code type} is not.
	 * @param test The test element. May be {@code null}.
	 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
	 */
	protected TestPatch(String type, String path, JsonElement test, boolean inverse) {
		super(null);
		if (type == null && path == null) throw new IllegalArgumentException("path may only be null when type is not null");
		this.type = type;
		this.path = path == null ? null : JsonSelector.of(path);
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
		ElementContext ctx = null;

		if (path != null)
			try {
				ctx = path.select(root, context.throwOnFailedTest());
			} catch (TraversalException e) {
				throw new PatchingException("Test failed: " + e.getMessage());
			}

		final boolean inverse = context.sbExtensions() && this.inverse;

		final boolean result = _test(root, ctx, inverse, context);

		if (!result && context.throwOnFailedTest())
			throw new PatchingException("Test " + (ctx == null ? "null" : ctx.elem()) + " " + (inverse ? "!=" : "==") + " " + test + " failed.");

		return result;
	}

	private boolean _test(JsonElement root, ElementContext ctx, boolean inverse, PatchContext context) {
		if (ctx != null)
			if (_test(root, ctx.elem(), context))
				return !inverse;

		return inverse;
	}

	private boolean _test(JsonElement root, JsonElement target, PatchContext context) {
		if (type != null) {
			if (context.testEvaluator() == null)
				throw new PatchingException("Cannot handle custom test type '" + type + "' as no evaluator is installed!");
			return context.testEvaluator().test(root, type, target, test, context);
		}

		return test == null || test.equals(target);
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

		if (type != null)
			obj.addProperty("type", type);

		if (path != null)
			obj.addProperty("path", path.toString());

		writeAdditional(obj, context);

		return obj;
	}

	@Override
	protected String operation() {
		return "test";
	}
}