# Compound patches

These patches are not exactly patches themselves; rather, they contain patches.

Compound patches can be used to organize patches, or used in conjunction with [`test` patches](test.md) to add conditional patches.

## Format

A compound patch is defined by a set of square brackets: `[]`.

For example, this is a compound patch that contains an [`add`](add.md) patch:

```json
[
  {
    "op": "add",
    "path": "/somewhere",
    "value": null
  }
]
```

## Examples

### Organizing patches

If you have a lot of patches, it might be useful to organize them with compound patches:

```json
[
  [
    {
      "op": "add",
      "path": "/e1/modified",
      "value": true
    },
    {
      "op": "replace",
      "path": "/e1/count",
      "value": 3
    }
  ],

  [
    {
      "op": "add",
      "path": "/e2/modified",
      "value": true
    },
    {
      "op": "replace",
      "path": "/e2/count",
      "value": 7
    }
  ]
]
```

### Localizing tests

Normally, a `test` patch will affect all the patches in the file.
One can use compound patches to localize this effect:

```json
[
  [
    {
      "op": "test",
      "path": "/version",
      "value": "1.7.1"
    },
    {
      "op": "replace",
      "path": "/renderCompat",
      "value": true
    }
  ],

  {
    "op": "add",
    "path": "newContent",
    "value": true
  }
]
```

In this example, `newContent` will be added regardless of whatever `version` is, but `renderCompat` will only be replaced if `version` is exactly `"1.7.1"`.