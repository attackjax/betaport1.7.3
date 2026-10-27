# Beta oracle

Small programs that run the real Beta 1.7.3 code from a Beta 1.7.3 client jar you
already own, so the mod's generator and colours can be compared with the original. Nothing
from that jar is copied into this project; these files only call into it.

Beta's classes sit in the unnamed package under obfuscated names, and the jar is signed, so
first make an unsigned copy (Java will not let unsigned classes share a signed package):

```
mkdir beta && cd beta && unzip -q /path/to/minecraft-b1.7.3-client.jar
rm META-INF/MOJANG_C.SF META-INF/MOJANG_C.DSA
jar --create --file ../beta-unsigned.jar --no-manifest . && cd ..
javac -cp beta-unsigned.jar -d out tools/beta-oracle/*.java
```

`BetaOracle` generates base terrain for an area plus a two-chunk margin, decorates every chunk
in row order (or in the order of a server log written with `-Db173.tracePopulation=true`),
and dumps block IDs, metadata, stored sky and block light, chests and spawners:

```
java -cp beta-unsigned.jar:out BetaOracle <seed> <minChunkX> <minChunkZ> <maxChunkX> <maxChunkZ> out.bin [server.log]
./gradlew oracleCheck --args="out.bin [server.log]"
```

`ColourOracle` asks Beta's grass, oak leaf and tall grass blocks for their colours at random
positions, with the colour maps replaced by an identity table, and prints a checksum that
`ColourParityTest` reproduces:

```
java -cp beta-unsigned.jar:out ColourOracle <seed> <positions> <positionSeed>
```

`SkyOracle` walks a Beta world through random times of day, rain, thunder, lightning and
viewer positions, asking for the sky, fog, sunrise and cloud colours and the star brightness,
then runs Beta's star builder up to the point where it would draw. It prints the three
checksums that `SkyParityTest` reproduces:

```
java -cp beta-unsigned.jar:out SkyOracle <seed>
```

On Windows, use `;` instead of `:` between classpath entries.

The obfuscated names (`fd` World, `yf` ChunkProviderGenerate and so on) are those of the
Beta 1.7.3 client jar with SHA-1 `43db9b498cb67058d2e12d394e6507722e71bb45`.

`VehicleOracle` builds a Beta world with a flat stone floor, writes rails and water straight into
its chunks (so no rail reshapes itself), and runs Beta's own `EntityMinecart` and `EntityBoat`
through `World.updateEntity`, printing position, speed and yaw each tick. Riders are plain living
things whose own mind holds fixed keys and, if asked, a fixed yaw; one sits like a player. The
names of the runs match the `probe carts` and `probe boat...` runs of the test server:

```
java -cp beta-unsigned.jar:out VehicleOracle cart_flat cart_rider cart_slope cart_powered cart_brake cart_curve furnace cart_bump_ew cart_bump_ns cart_pickup boat_float boat_row boat_crash boat_wall
```

`NetherOracle` does the same for the Nether: it builds a Beta `World` with the Nether's
`WorldProvider` and runs `ChunkProviderHell`, dumping an area's base terrain and its blocks after
decoration. Beta never reseeded the Nether's generator for decoration, so by default every chunk
is decorated right after its diagonal neighbour has been provided again, which leaves the
generator where Beta left it when a world filled its starting area row by row; that is the state
the mod replays. `--arrival` instead lets chunks arrive in row order and decorates each as soon
as its four chunks exist, and `--light` empties Beta's light queue after every arrival, as its
loading screen did. `NetherOracleCheck` compares a dump with the core:

```
java -cp beta-unsigned.jar:out NetherOracle <seed> <minChunkX> <minChunkZ> <maxChunkX> <maxChunkZ> nether.bin [--arrival] [--light]
./gradlew netherOracleCheck --args="nether.bin"
```

`RecipeOracle` lists every recipe Beta knew, in Beta's own IDs: the crafting recipes in the order
`CraftingManager` tried them, the furnace's smelting table and every item's burn time. The core
keeps the same lines (`BetaRecipes`), and the parity suite checks them against this list's hash:

```
java -cp beta-unsigned.jar:out RecipeOracle recipes.txt
```

`DropOracle` lists what every Beta block drops when broken: for each block (and each
metadata value that changes the drop: wood kinds, wool colours, slabs, crop ages, door and bed
halves), how many items fell over 20,000 breaks through Beta's own `quantityDropped`,
`idDropped` and `damageDropped`, and the share of each item; then which of a bare hand and
Beta's 21 tools get a drop from the block at all (`EntityPlayer.canHarvestBlock`). The core
keeps the same lines (`BetaBlockDrops`), and the parity suite checks them against this list's
hash and checks the mod's drop rules against them:

```
java -cp beta-unsigned.jar:out DropOracle drops.txt
```

`FireOracle` prints `BlockFire`'s two tables (how readily each block feeds fire, and how
readily it catches) and which blocks' materials burn, the test still lava makes before setting
the air beside a block alight. The core keeps the same values (`BetaFire`), checked by hash in
`FireParityTest`:

```
java -cp beta-unsigned.jar:out FireOracle
```

`PortalOracle` runs Beta's own `Teleporter` in stand-in worlds (flat, uneven, solid to the top)
from fixed random seeds and prints every obsidian and portal block it builds and where it puts
the traveller, then has travellers choose between two portals. The probe's `portals` run of
`BetaTeleporter` on a server prints the same lines, relative to each run's origin:

```
java -cp beta-unsigned.jar:out PortalOracle
```

`AoOracle` runs Beta's own smooth lighting (`RenderBlocks.renderStandardBlockWithAmbientOcclusion`)
on random 5 by 5 by 5 patches of blocks and light levels, drawing the middle block into Beta's
`Tessellator` and reading every corner's position and colour back from its buffer. It first
prints Beta's `canBlockGrass` table, its light values and every block's render type, which the
core keeps in `BetaSmoothLight`. With `check` it prints the compact record (which faces were
drawn, and a hash of every corner colour) that `SmoothLightParityTest` reproduces:

```
java -cp beta-unsigned.jar:out AoOracle 8000 2026 check
```

`FlatOracle` finds out how Beta lit, with smooth lighting off, the faces of each block it did
not draw as a plain cube. It draws each block through `RenderBlocks.renderBlockByRenderType`
in random patches of light and metadata and, for every face, looks for the block (itself or a
neighbour) and shade whose brightness gives all four corners' colours, then prints how often
each answer fitted, by block and by the way the face points. Beta's `Minecraft` class needs
`org.lwjgl.LWJGLException` to load; an empty class of that name on the class path will do:

```
java -cp beta-unsigned.jar:lwjgl-stub:out FlatOracle 300
```

`PathOracle` runs Beta's `PathFinder` on random 32 by 16 by 32 worlds of uneven stone with
water, lava, fences and doors (open and shut), for mobs of each of Beta's sizes, asking for a
way to a block or to another mob's feet, and prints a hash of every point of every path that
`PathParityTest` reproduces. It makes its mobs without running their constructors, which
needs `java.lang` opened to it:

```
java --add-opens java.base/java.lang=ALL-UNNAMED -cp beta-unsigned.jar:lwjgl-stub:out PathOracle 3000 26
```
