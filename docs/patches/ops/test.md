# The `test` operation

This operation performs a check of some kind and aborts processing patches in the same [compound patch](compound.md) if the check fails.

## Equality

An equality test patch checks to see if a particular element is equal to its `value` element.

### Format

An equality `test` patch looks like this:

```json
{
  "op": "test",
  "path": "/path/to/target/element",
  "value": "some value"
}
```

### Example

For example,

```json
[
  {
    "op": "test",
    "path": "/my value",
    "value": true
  },
  {
    "op": "add",
    "path": "/added",
    "value": "yep, it's true"
  }
]
```

checks to see if `my value` is equal to `true` before adding an element. When applied to this document:

```json
{
  "my value": true
}
```

it results in:

```json
{
  "my value": true,
  "added": "yep, it's true"
}
```

If `my value` was anything other than `true`, `added` would not have been added to the document.

## Existence

Existence `test` patches check to see if a particular element exists.

These patches can only be used if `testExtensions` is enabled.

### Format

An existence `test` patch looks like this:

```json
{
  "op": "test",
  "path": "/path/to/target/element"
}
```

Basically, you just omit the `value` field.

### Example

For example,

```json
[
  {
    "op": "test",
    "path": "/my value"
  },
  {
    "op": "add",
    "path": "/added",
    "value": "it exists alright"
  }
]
```

checks to see if `my value` exists before adding an element. When applied to this document:

```json
{
  "my value": 3
}
```

it results in:

```json
{
  "my value": true,
  "added": "it exists alright"
}
```

If `my value` did not exist, `added` would not have been added to the document.

## Custom

These `test` patches perform checks defined by a "test evaluator."

This feature is only enabled if `patchedExtensions` is enabled.

By default, Patched does not add any of these.
If you are a user, the program exposing patch support should have documentation telling you if there are any defined.

### Format

A custom `test` patch looks like this:

```json
{
  "op": "test",
  "type": "the name of the custom test condition",
  "path": "/path/to/target/element", // optional
  "value": "some value" // optional
}
```

## Inverse tests

Allows checking to see if a condition fails.
This is most useful when checking the existence of something; instead of checking if something exists, you can check if something *doesn't* exist.

This feature is only available if `testExtensions` is enabled.

### Format

Simply add the following:

```json
"inverse": true
```

to any test patch.

For example:

```json
{
  "op": "test",
  "path": "/my/element",
  "inverse": true
}
```

### Example

For example,

```json
[
  {
    "op": "test",
    "path": "/added",
    "inverse": true
  },
  {
    "op": "add",
    "path": "/added",
    "value": "it's alive!"
  }
]
```

checks to make sure `added` doesn't exist before adding it.
When applied to this document:

```json
{
  "something": 1.4159e-3
}
```

it results in:

```json
{
  "something": 1.4159e-3
  "added": "it's alive!"
}
```

This happened because `added` did not previously exist.
If it did, it would not be changed to `"it's alive!"`.