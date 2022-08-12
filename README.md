# Server Mobs

## About
Server Mobs is a server-side mod for fabric 1.18.2 that creates new mobs, and provides a framework for other mods to do so.
At some point in the future, the non-API portion will be split into a separate mod. (Coming Soon™)

## Setup
Don't forget to configure polypack_host if you use it.
<br>
Example entity implementations are under [com.slimeist.server_mobs.entities](src/main/java/com/slimeist/server_mobs/entities).
The 'API' (not guaranteed to be stable yet) is under [com.slimeist.server_mobs.server_rendering](src/main/java/com/slimeist/server_mobs/server_rendering).

## Dependencies
### Required
* [polymer](https://github.com/Patbox/polymer/tree/dev/1.18.2)
* [hologram-api](https://github.com/Patbox/HologramAPI/tree/1.18)
* [nbtcrafting](https://github.com/Siphalor/nbt-crafting/tree/1.18-2.0)
### Recommended
* [polypack_host](https://github.com/aws404/polypack-host)
* [server_translations_api](https://github.com/NucleoidMC/Server-Translations/tree/1.18.2)

## License

This project is available under the MIT license. It is based on the fabric example mod template which is available under the CC0 license.<br>
Marked sections of code inside [com.slimeist.server_mobs.entities.MissileEntity](src/main/java/com/slimeist/server_mobs/entities/MissileEntity.java) are from [Team Pneumatic's Pnematicraft Repressurized](https://github.com/TeamPneumatic/pnc-repressurized), and are licensed under the GPL-3 license.<br>
Marked sections of code inside [com.slimeist.server_mobs.items.MissileItem](src/main/java/com/slimeist/server_mobs/items/MissileItem.java) are from [Team Pneumatic's Pnematicraft Repressurized](https://github.com/TeamPneumatic/pnc-repressurized), and are licensed under the GPL-3 license.