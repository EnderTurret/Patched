# Patched

A library for reading, writing, and applying [Json patches](https://jsonpatch.com/).

It handles everything the [original RFC](https://datatracker.ietf.org/doc/html/rfc6902) describes,
the [Starbound extensions](https://community.playstarbound.com/threads/april-21st-%E2%80%93-stable-update-notes.95106/page-5#post-2561028) to it,
and even introduces [its own extensions](#the-find-operation). These extensions are by default disabled.

Specifically, the Starbound extensions add [existence tests](#existence-tests) and the ability to [invert tests](#inverse-tests),
and the other extension adds [custom test conditions](#custom-tests) and a new ["find" operation](#the-find-operation).

## Limitations and Differences

Replacing the root document is not possible.
This is because of the way the library was originally designed.
I can't imagine that this is a common use-case, so I haven't done the required work to support this.

Additionally, the `add` operation lets you add elements past the end of an array (they're treated like adding at `-`).
This is for patch compatibility reasons (see: [the Minecraft mod](https://github.com/EnderTurret/PatchedMod)) and because it doesn't seem like a huge problem to allow this.
If things explode, an option may be added to disable this functionality in the future.

When all extensions are disabled, these are the only differences between this library and most other patching libraries.

## Extensions

### Existence tests

Normally, `test` only lets you test if something is equal to a specific value.
This extension lets you omit the `value` field, allowing you to test if an element exists.

For example:

```json
[
  {
    "op": "test",
    "path": "/foo"
  },
  {
    "op": "add",
    "path": "/bar",
    "value": 1
  }
]
```

applied to this document:

```json
{
  "foo": "yes"
}
```

results in:

```json
{
  "foo": "yes",
  "bar": 1
}
```

### Inverse tests

Another extension is an `inverse` field to the `test` operation.
This lets you negate the operation; instead of testing if something is equal to something, you are testing if something is *not* equal to something.

For example:

```json
[
  {
    "op": "test",
    "path": "/foo",
    "inverse": true,
    "value": 1
  },
  {
    "op": "add",
    "path": "/bar",
    "value": 1
  }
]
```

applied to this document:

```json
{
  "foo": "yes"
}
```

results in:

```json
{
  "foo": "yes",
  "bar": 1
}
```

This might not seem very useful as is, but when combined with existence tests, it can be very helpful.

### Custom tests

Sometimes, you'll find that you want the `test` operation to support testing against other things.

The best example for this is in [the mod](https://github.com/EnderTurret/PatchedMod) where you might want a patch to apply only when a certain mod is loaded.
This might be achieved like so:

```json
[
  {
    "op": "test",
    "type": "patched:mod_loaded", // a custom condition that checks if a mod is loaded
    "value": "patched" // the mod in question
  },
  { // this is only applied when the mod "patched" is loaded
    "op": "add",
    "path": "/added",
    "value": "Patched is loaded"
  }
]
```

When the `type` field is specified, the `path` field may be omitted (same with `value`).
It is up to the `ITestEvaluator` to decide if this is legal for the condition used.

### The "find" operation

The find operation is a sort of fuzzy-matching operation.

For example, applying the following patch:

```json
[
  {
    "op": "find",
    "path": "/array",
    "test": {
      "path": "/a",
      "value": 7
    },
    "then": {
      "op": "add",
      "path": "/c",
      "value": 1
    }
  }
]
```

to the following document:

```json
{
  "array": [
    {
      "a": 1,
      "b": 3
    },
    {
      "a": 7,
      "b": 0
    }
  ]
}
```

yields the following:

```json
{
  "array": [
    {
      "a": 1,
      "b": 3
    },
    {
      "a": 7,
      "b": 0,
      "c": 1
    }
  ]
}
```

Only the `test` operation is valid in the `test` part of the patch.
This means that the operation field is unnecessary and may be omitted from each `test` patch, as is seen here.

The find operation by default only patches the first matching element.
In order to have it patch all matching elements, `multi` must be set to `true`, like so:

```json
[
  {
    "op": "find",
    "path": "/array",
    "multi": true,
    "test": {
      "path": "/a",
      "value": 7
    },
    "then": {
      "op": "add",
      "path": "/c",
      "value": 1
    }
  }
]
```

Additionally, you may have multiple tests by defining an array:

```json
[
  {
    "op": "find",
    "path": "/array",
    "test": [
      {
        "path": "/a",
        "value": 7
      },
      {
        "path": "/b",
        "value": 0
      }
    ],
    "then": {
      "op": "add",
      "path": "/c",
      "value": 1
    }
  }
]
```

You can also do the same with `then`.

## Installing

### For users

This project is mainly intended to be an API for other projects to use.
However, there are [built versions](https://github.com/EnderTurret/Patched/releases) of the library available that include a basic CLI for patching things.

Not only that, but there is a [Minecraft mod](https://github.com/EnderTurret/PatchedMod) that implements patching functionality there.

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
    implementation 'com.github.EnderTurret:Patched:1.1.1'
}
```

## Usage (for developers)

First make sure you have the library on the build path.
See the prior section for details.

### Reading patches

You will need to have a `Gson` instance setup with the patch (de)serializer.
This can be done using `Patches.patchGson(boolean, boolean)`.

Next, you can use `Patches.readPatch(Gson, String)` or any of the other varieties to read a patch file.

### Applying patches

You will need a `JsonElement` representing the Json you will be patching.
You will also need a patch read via the [previous step](#reading-patches).

To apply a patch, you use `JsonPatch.patch(JsonElement, PatchContext)`.
This method will modify the `JsonElement` you give it, so make a copy if you need one.
You can obtain a `PatchContext` using `PatchContext.newContext()` and customize it using the provided methods.