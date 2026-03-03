# Patched

A library for reading, writing, and applying [Json patches](https://jsonpatch.com/).

It is a completely specification-compliant implementation, although it does include [its own extensions](#extensions-to-the-specification).

This library does not come with documentation on the patches themselves, however the Minecraft mod [has documentation on this subject](https://github.com/EnderTurret/PatchedMod/wiki).
For documentation on using the library, there is fairly extensive Javadocs in the source code, or one can [look at the Minecraft mod for examples](https://github.com/EnderTurret/PatchedMod/tree/multiversion/common/src/main/java/net/enderturret/patchedmod/common).

## Installing

### For users

This project is mainly intended to be an API for other projects to use.
However, there is a [Minecraft mod](https://github.com/EnderTurret/PatchedMod) that implements patching functionality there.

### For developers

There is no maven hosting this library (yet).
Currently, your best option is using [JitPack](https://jitpack.io):

```gradle
repositories {
    maven {
        url = 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.github.EnderTurret:Patched:<version>'
}
```

## Extensions to the specification

Patched includes some optional extensions to [the original specification](https://datatracker.ietf.org/doc/html/rfc6902), which are as follows:
* [Test Extensions](https://community.playstarbound.com/threads/april-21st-%E2%80%93-stable-update-notes.95106/page-5#post-2561028) (`testExtensions`)
	* [Existence test patch type](https://github.com/EnderTurret/PatchedMod/wiki/Test-Operation)
	* [Inverse test patches](https://github.com/EnderTurret/PatchedMod/wiki/Test-Operation)
* Patched Extensions (`patchedExtensions`)
	* ["Custom" test patch type](https://github.com/EnderTurret/PatchedMod/wiki/Test-Operation)
	* [`find` operation](https://github.com/EnderTurret/PatchedMod/wiki/Find-Operation)
	* [`include` operation](https://github.com/EnderTurret/PatchedMod/wiki/Include-Operation)
	* [`paste` operation](https://github.com/EnderTurret/PatchedMod/wiki/Paste-Operation)
	* Absolute paths (intended for `find`)
	* Json path placeholders (intended for `find`)