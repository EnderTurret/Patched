# The `include` operation

This operation allows including the contents of a patch from another.
It's intended to ease organization and reduce duplication by moving repetitive patches into single files which are then included.

This operation is only available if `patchedExtensions` is enabled *and* an `IFileAccess` instance has been installed into the `PatchContext`.

## Format

An `include` patch looks like this:

```json
{
  "op": "include",
  "path": "path/to/patch" // the path to the patch file to include -- the format of the path is implementation specific
}
```

## Examples

For example, given the following patch:

```json
{
  "op": "include",
  "path": "test.json.patch"
}
```

with the following `test.json.patch`:

```json
{
  "op": "add",
  "path": "/added",
  "value": true
}
```

applied to the following document:

```json
{
  "some_value": 1
}
```

results in:

```json
{
  "some_value": 1,
  "added": true
}
```