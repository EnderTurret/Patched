# The `remove` operation

Removes an element, failing if it doesn't exist.

## Format

A `remove` patch looks like this:

```json
{
  "op": "remove",
  "path": "/path/to/removed/element"
}
```

## Example

For example,

```json
{
  "op": "remove",
  "path": "/some data/1"
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
    92
  ]
}
```