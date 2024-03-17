# Specification Differences

## Extensions

Patched adds quite a few "extensions" to [the original specification](https://datatracker.ietf.org/doc/html/rfc6902).

These extensions are the following:
* [Test Extensions](https://community.playstarbound.com/threads/april-21st-%E2%80%93-stable-update-notes.95106/page-5#post-2561028) (`testExtensions`)
	* [Existence test patch type](ops/test.md#existence)
	* [Inverse test patches](ops/test.md#inverse-tests)
* Patched Extensions (`patchedExtensions`)
	* ["Custom" test patch type](ops/test.md#custom)
	* [`find` operation](ops/find.md)
	* [`include` operation](ops/include.md)

All of these extensions are off by default, which brings Patched more in line with the original specification.