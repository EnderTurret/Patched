# API Documentation

## Installation

There is no maven hosting this library (yet).
Currently, your best option is using [JitPack](https://jitpack.io):

```gradle
repositories {
    maven {
        url = 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.github.EnderTurret:Patched:1.1.1'
}
```

## Reading Patches

You will need to have a `Gson` instance setup with the patch (de)serializer.
This can be done using `Patches.patchGson(boolean, boolean)`.

Next, you can use `Patches.readPatch(Gson, String)` or any of the other varieties to read a patch file.

## Applying Patches

You will need a `JsonElement` representing the Json you will be patching.
You will also need a patch read via the [previous step](#reading-patches).

To apply a patch, you use `JsonPatch.patch(JsonElement, PatchContext)`.
This method will modify the `JsonElement` you give it, so make a copy if you need one.
You can obtain a `PatchContext` using `PatchContext.newContext()` and customize it using the provided methods.

### Customizing the `PatchContext`

`PatchContext` comes with a number of settings:

* `sbExtensions`
	This specifies whether existence tests and inverse tests are allowed in patches.
* `patchedExtensions`
	This specifies whether the `find` operation and "custom" test types are allowed in patches.
* `throwOnFailedTest`
	Whether to treat `test` operation failures as errors.
* `testEvaluator`
	An evaluator for the aforementioned custom test types. Only useful if `patchedExtensions` are on.
* `audit`
	A `PatchAudit` instance to collect changes from patches.
	This is mainly useful for its `toString(JsonElement)` method, which creates a "pretty-printed" string representing the passed in element with comments indicating the changes made.