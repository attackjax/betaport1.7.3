# Third-party notices

## The World Before (parity test vectors)

The Beta 1.7.3 reference values in `core/src/test/java/dev/b173/core/TerrainParityTest.java`
and `PopulationParityTest.java` (climate samples, noise hashes and chunk hashes) were
published by The World Before, which recorded them from the unmodified Beta 1.7.3
generator. They are used here only as test data. No code from that project is included.

```
MIT License

Copyright (c) 2026 The World Before contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Minecraft

No Minecraft code, textures or sounds are included in this repository or in the built jar.
The generator is an independent reimplementation of Beta 1.7.3's world generation
algorithms, checked against recorded output.

The hashes in `core/src/test/java/dev/b173/core/BetaOracleVectors.java` were recorded by
running an unmodified Beta 1.7.3 client jar through `tools/beta-oracle/BetaOracle.java`, which
calls into a jar the user supplies and ships none of it. They are checksums of generated
worlds, used only as test data.
