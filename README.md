LibUpd8
===

A small *mod* to update Mojang libraries.

**This is no longer relevant as of Minecraft Launcher 1.4.6.
It added version inheritance so it is no longer necessary to
keep up with all the changes Mojang does to their json.**

In a Minecraft modded environment, the libraries are not updated
like they do in a vanilla version. This has become an issue recently
as Mojang has been pushing frequent updates to Realms, causing Realms
users to miss out on all the mods out there. This mod aims to
automatically update the libraries to at least the versions in the
official vanilla json downloaded directly from Mojang.

The updater will make sure to only update outdated libraries.
For example: Default Minecraft ships with Guava 15.0, but FML
requires Guava 16.0. This library, since it has a more recent
 version, will not be *updated* to 15.0.

Any updates that are applied will take effect the next time the
Minecraft Launcher is opened.
