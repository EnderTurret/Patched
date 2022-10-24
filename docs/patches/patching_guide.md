# Patching Guide

This is a semi-basic guide for creating patches.
It explains the basics of patching files, some of the stranger things in patches (like escaping paths), and a few other things.

### Deciding What to Patch

Obviously, before you can patch something you have to first have an idea of what you're changing.

Examples would be modifying a field in the file `myoverengineeredconfig.json`, adding an object to `list_of_objects.json`, etc.
Minecraft-specific examples might be adding a feature to a biome, adding or changing loot in a loot table, etc.

The main thing is you need to figure out what changes you're planning to make to the file, as those are what your patches will need to perform.

### Creating a Patch

Patches have a filename of `<name of file to patch>.patch`.
If you were patching `myfile.json`, the patch would be called `myfile.json.patch`.

Before you can write the contents of a patch, you have to create the patch file.
After, you need to figure out what parts of the file you're changing.

### Anatomy of a Patch

All patches take a similar "shape."
They all have an `op` field that describes what operation is being performed, usually a `path` field that identifies the element being modified, and sometimes a `value` field for adding or replacing things.

This is what a simple patch looks like:

```json
[
  {
    "op": "add",
    "path": "/myelement",
    "value": "this element was added!"
  }
]
```

In this example:
* the `op` is `add`, meaning we're adding an element
* the path is `/myelement`, meaning we're adding an element called "myelement" to the root object
* the value is `"this element was added!"`, which is the element that will be placed at `myelement`

If this patch were to be applied to an empty document, such as `{}`, we'd get:

```json
{
  "myelement": "this element was added!"
}
```

#### Compound patches

Before we go deeper, we should talk about compound patches.

These are not exactly an operation of any kind.
Rather, they group patches together.

They can be used to organize patches, or for scope-limiting (this will be important later).

You might have noticed the square brackets in the earlier example.
This is a compound patch.

They can also be nested, like so:

```json
[
  [
    {
      // ...
    },
    {
      // ...
    }
  ],
  [
    // ...
  ]
]
```

### Paths

The `path` is one of the most important parts of a patch, so it's important to understand how they work.

The `path` is a slash (`/`) delimited list of "subpaths", which can be "traversed" to find an element.
Each subpath may be a string (like `myelement`), a number (like `3`), or an end-of-array marker (`-`).

These subpaths have different meanings depending on the element being traversed.
For example, `3` may indicate the index 3 in an array, or the name "3" in an object.
Similarly, the end-of-array marker either points to the end of an array, or the name "-" in an object.

#### Using reserved characters

Rarely, you might encounter a field you want to change that contains a slash in its name.
The problem is that because slashes are path delimiters, it might point to a completely different element!

Fortunately, there is a way to "escape" the slash.
Specifically, `~1` becomes `/` when the path is parsed,
and now because `~` has a special meaning, there's also `~0` for `~`.

#### Traversal Exceptions

Something you will quickly run into when writing paths is that you will run into these so-called `TraversalExceptions`.
These are errors that are thrown when a path cannot be traversed for whatever reason, such as an element not existing.

This is a list of most of the circumstances in which a `TraversalException` is thrown:
* traversing into an element that doesn't exist
* traversing a string subpath in an array
* traversing a negative index in an array
* replacing or removing a non-existent element
* creating a path that does not begin with a slash and is not empty

As you can see, this is a rather large list of error states.
What if you wanted to remove an element that may not exist, without causing an error?
There is a way to avoid these errors which will be explained in a following section.

### Other Operations

In the first example, the `add` operation was demonstrated.
Of course, `add` isn't the only operation.
There are several others, and [the list of operations](operations.md) details all of them.
You should look at each operation in the list, as some of them have special attributes.

### Test Patches

Test patches allow one to perform checks on the state of an element before performing a modification.

Going back to the earlier example, how would one remove an element without causing an error?
Doing such is actually quite simple:

```json
[
  {
    "op": "test",
    "path": "/myelem"
  },
  {
    "op": "remove",
    "path": "/myelem"
  }
]
```

Test patches work by preventing other patches in the same *compound patch* from applying.
Notice the square brackets in this example, as these indicate that both of these patches share a compound patch.
If the test operation evaluates to `false`, the remove patch will not apply.

Since you can nest compound patches, you can have patches apply in different scenarios in the same patch.
For example, maybe you want to change something only if it hasn't already been changed:

```json
[
  [
    {
      "op": "test",
      "path": "/materials/0/name",
      "value": "aluminium"
    },
    {
      "op": "replace",
      "path": "/materials/0/name",
      "value": "aluminum"
    }
  ],
  [
    {
      "op": "add",
      "path": "/materials/-",
      "value": {
        "name": "my material"
      }
    }
  ]
]
```

In this example, the name of the "aluminium" material is changed to "aluminum" if and only if its name is currently "aluminium."
If another patch were to change the name of the material first, it wouldn't be changed to "aluminum."
In addition to this, the material "my material" is added regardless of the state of the "aluminium" material.

There are actually a few different kinds of test patches.
The first one shown here is an existence test, which tests for the existence of an element.
There are also equality tests, which were shown in the previous example.
Lastly, there are "custom" tests, which will not be covered here.
Additionally, test patches can be inverted (meaning you could check if an element doesn't exist).

I would highly recommend reading [the document on test patches](ops/test.md) for more information.