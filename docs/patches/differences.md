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

All of these extensions are off by default, which brings Patched more in line with the original specification.

## Differences

There is currently only one difference that prevents Patched from being a 1:1 implementation of the specification.

The difference is that Patched does not throw an error when trying to add an element at an index past the end of an array.
This was a choice made to make the library "friendlier" for its intended purpose -- patching game assets.