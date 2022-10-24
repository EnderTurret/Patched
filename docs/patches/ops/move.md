# The `move` operation

Moves an element elsewhere, failing if it doesn't exist.
It's rather like "renaming" an element.

Moving an element will also overwrite the element at its destination, if one is there.

## Format

A `move` patch looks like this:

```json
{
  "op": "move",
  "path": "/path/to/destination",
  "from": "/path/to/moved/element"
}
```

## Example

For example,

```json
{
  "op": "move",
  "path": "/extracted",
  "from": "/some data/0"
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
    47
  ],
  "extracted": 92
}
```