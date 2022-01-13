#!/bin/sh
./gradlew build
mv ./build/reobfJar/output.jar  ~/.local/share/polymc/instances/1.12.2/.minecraft/mods/kamired.jar
#rm -f ~/.minecraft/kamiblue/config/generic.bak
#rm -f ~/.minecraft/kamiblue/config/generic.json
