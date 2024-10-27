# The `paste` operation

This operation allows 'pasting' a value from an `IDataSource`.
It is not to be confused with `copy`, as the two do not perform the same action.
The operation's purpose is to allow inserting code-provided data into a json file, enabling one to (for example) insert configuration file values into an unrelated json document.
It can be thought of as the write-only twin to [custom tests](test.md#custom).

This operation is only available if `patchedExtensions` is enabled *and* an `IDataSource` instance has been installed into the `PatchContext`.

## Format

The simplest `paste` patch looks like this:

```json
{
  "op": "paste",
  "type": "an identifier for the data source",
  "path": "path/to/insert/data"
}
```

However, `paste` patches can be as complex as the following:

```json5
{
  "op": "paste",
  "type": "an identifier for the data source",
  "from": "/path/to/input/element", // an input element to pass to the data source
  "path": "/path/to/output",
  "value": { // may also be a primitive, such as a string or number
    // ...
  }
}
```

## Examples

Since data sources are implementation-specific, there are no examples of this patch.