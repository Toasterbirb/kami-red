# KAMI

A minecraft 'hacked' client in the form of a forge mod.

See [forgehax](https://github.com/fr1kin/forgehax) for a more polished equivalent. Some features in KAMI may be based on those of forgehax, as I sometimes used it as reference.

This is by no means a finished project and isn't fully ready for release.

## Troubleshooting
Please reference the main troubleshooting page: [Troubleshooting](https://github.com/zeroeightysix/KAMI/blob/master/TROUBLESHOOTING.md)
If you experience an issue and it's not listed there, please [open a new issue](https://github.com/zeroeightysix/KAMI/issues/new) and a contributor will help you further.

## Contributing

You are free to clone, modify KAMI and make pull requests as you wish. To set up your development environment, make use of the following commands:

```
git clone https://github.com/zeroeightysix/KAMI/
cd KAMI
```

On linux, run `chmod +x gradlew` and for the following commands use `./gradlew` instead of `./gradlew.bat`

```
./gradlew.bat setupDecompWorkspace
```
Import KAMI into your IDE of choice. If you use intelliJ, import from the `build.gradle` file and run `./gradlew.bat genIntellijRuns`

If you do not wish to run from an IDE, use `./gradlew.bat runClient` to run KAMI.

### Building

```
./gradlew.bat build
cd build/libs
```
In `build/libs` you will find a file `KAMI-<minecraftVersion>-<kamiVersion>-full.jar` which you can copy to the `mods` folder of a minecraft instance that has forge installed.
