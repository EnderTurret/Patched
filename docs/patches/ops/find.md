# The `find` operation

This operation is rather like a search-and-apply type of operation.
It lets you apply any number of patches to elements matching specified conditions.

This operation is only available if `patchedExtensions` is enabled.

## Format

A `find` patch looks like this:

```json
{
  "op": "find",
  "path": "/path/to/root",
  "multi": false, // whether to continue after the first match
  "test": [] // a list of one or more test patches -- the operation may be omitted from them
  "then": [] // a list of one or more patches. These are applied when a match is found
}
```

## Examples

### Basic Usage

For example,

```json
{
  "op": "find",
  "path": "/my data",
  "multi": true,
  "test": [
    {
      "path": "/enabled",
      "value": true
    }
  ],
  "then": [
    {
      "op": "add",
      "path": "/found",
      "value": true
    }
  ]
}
```

applied to this document:

```json
{
  "my data": {
    "beans": {
      "enabled": false
    },
    "fancyMode": {
      "enabled": true
    },
    "northIsDown": {
      "enabled": true
    },
    "performance": {
      "enabled": false
    }
  }
}
```

results in:

```json
{
  "my data": {
    "beans": {
      "enabled": false
    },
    "fancyMode": {
      "enabled": true,
      "found": true
    },
    "northIsDown": {
      "enabled": true,
      "found": true
    },
    "performance": {
      "enabled": false
    }
  }
}
```

This example demonstrated `find` in an object, but it can also be used in arrays.

### Removing elements

Another use of `find` is removing elements (notice the empty path):

```json
{
  "op": "find",
  "path": "/inventory",
  "multi": true,
  "test": [
    {
      "path": "/count",
      "value": 2
    }
  ],
  "then": [
    {
      "op": "remove",
      "path": ""
    }
  ]
}
```

applied to this:

```json
{
  "inventory": [
    {
      "name": "pomegranate",
      "count": 2
    },
    {
      "name": "flattened screwdriver",
      "count": 1
    },
    {
      "name": "bespoke firmware",
      "count": 2
    }
  ]
}
```

results in:

```json
{
  "inventory": [
    {
      "name": "flattened screwdriver",
      "count": 1
    }
  ]
}
```