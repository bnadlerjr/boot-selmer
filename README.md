# boot-selmer

Boot task to compile [Selmer](https://github.com/yogthos/Selmer) templates.

[![Clojars Project][1]][2]

## Options

To see a list of options accepted by this task:

```
$ boot selmer -h
```

## Development

To install a local snapshot:

```
$ boot build-jar
```

To push a new release to Clojars:

1. Update the version in `build.boot`
2. `CLOJARS_USER=foo CLOJARS_PASS=bar boot build-jar push-release`

## License

Copyright © 2017 Bob Nadler, Jr.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: https://img.shields.io/clojars/v/bnadlerjr/boot-selmer.svg
[2]: http://clojars.org/bnadlerjr/boot-selmer
