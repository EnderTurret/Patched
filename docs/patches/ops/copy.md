# The `copy` operation

Copies an element and places it somewhere, failing if the source element doesn't exist.
This is similar in behavior to `move`

Copying an element will also overwrite the element at its destination, if one is there.

## Format

A `copy` patch looks like this:

```json
{
  "op": "copy",
  "path": "/path/to/destination",
  "from": "/path/to/source/element"
}
```

## Example

For example,

```json
{
  "op": "copy",
  "path": "/copied data",
  "from": "/some data/1"
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
  "copied data": 47
}
```