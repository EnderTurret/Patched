# The `replace` operation

Replaces an element with some other value, failing if the element doesn't exist.
In practice `replace` is rather similar to `add`, but with the guarantee that the value is only added if its target exists.

## Format

A `replace` patch looks like this:

```json
{
  "op": "replace",
  "path": "/path/to/replaced/element",
  "value": "the value to replace the existing one with"
}
```

## Example

For example,

```json
{
  "op": "replace",
  "path": "/some data/0",
  "value": "Wait, this isn't a number!"
}
```

applied to this document:

```json
{
  "some data": [
    92,
    47
  ]
}
```

results in:

```json
{
  "some data": [
    "Wait, this isn't a number!",
    47
  ]
}
```