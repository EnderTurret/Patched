package tests.util;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonPrimitive;

import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchUtil;

public final class SimpleFileAccess implements IFileAccess {

	@Override
	@Nullable
	public JsonPatch readIncludedPatch(String path) {
		if ("test".equals(path))
			return PatchUtil.add("/working", new JsonPrimitive(true));

		return null;
	}
}