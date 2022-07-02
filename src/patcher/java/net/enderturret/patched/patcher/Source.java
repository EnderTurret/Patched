package net.enderturret.patched.patcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a query-able source folder that can contain both Json files and patches.
 * @author EnderTurret
 *
 * @param <P> The path type.
 * @see SourceAdapter
 */
public class Source<P> implements Comparable<Source<P>> {

	private final Map<String, Boolean> existence = new HashMap<>();

	private final SourceAdapter<P> adapter;

	private final int priority;

	public Source(SourceAdapter<P> adapter, int priority) {
		this.adapter = adapter;
		this.priority = priority;
	}

	public int priority() {
		return priority;
	}

	public Boolean exists(P path) {
		return existence.get(path.toString());
	}

	public byte[] read(P path) {
		final String str = path.toString();
		final Boolean exists = existence.get(str);

		if (exists != null)
			return exists ? adapter.read(path) : null;

		final byte[] ret = adapter.read(path);
		existence.put(str, ret != null);

		return ret;
	}

	@Override
	public int compareTo(Source<P> o) {
		final int temp = Integer.compare(priority(), o.priority());
		if (temp != 0) return temp;
		return adapter.namespace().compareTo(o.adapter.namespace());
	}

	@Override
	public String toString() {
		return "{" + priority() + ", " + adapter.namespace() + "}";
	}
}