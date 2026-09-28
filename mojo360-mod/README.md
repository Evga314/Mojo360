# Mojo joystick 360

Fabric mod for Minecraft 26.3 that brings 360° movement from the MojoLauncher on-screen joystick.
The vanilla launcher joystick sends W/A/S/D, so you can only walk in 8 directions.
With this mod you can walk in any direction, and the walking speed follows how far you tilt the stick.

## Requirements

- Launcher: [Mojo360](https://github.com/Evga314/Mojo360), a MojoLauncher fork, until the bridge is merged into MojoLauncher.
- Minecraft 26.3, Fabric Loader 0.19.5+, Java 25.
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional) for the settings screen.

On any other launcher, or on PC, the mod does nothing and the game plays as usual.

## Settings (Mod Menu)

- **360° movement:** on/off, applies instantly without a restart. When off, the joystick sends W/A/S/D again.
- **Speed from stick tilt:** off means the stick only sets the direction and you always walk at full speed.
- **Debug log:** logs the handshake, joystick values and the movement vector, at most every 500 ms.

## For other mod authors

The launcher side is an open API. Any mod can use it, and several mods can use it at the same time.
See [analog-movement.md](https://github.com/Evga314/Mojo360/blob/v360_openjdk/docs/analog-movement.md).

## Build

```
./gradlew build
```

The jar is written to `build/libs/`.

## License

LGPL-3.0, the same as MojoLauncher.
