# The `add` operation

Adds a value to an element, possibly overwriting an existing value.

## Format

An `add` patch looks like this:

```json
{
  "op": "add",
  "path": "/path/to/added/element",
  "value": "some element to add"
}
```

## Example

For example,

```json
{
  "op": "add",
  "path": "/added value",
  "value": true
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
    92,
    47
  ],
  "added value": true // added by the patch
}
```