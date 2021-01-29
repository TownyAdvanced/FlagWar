# [Flag War for Towny Advanced](https://townyadvanced.github.io/wars)

Flag War is the first warfaring system for servers in the TownyAdvanced ecosystem.

Originally Flag War was called Cell War and was composed by [Shade]() as part of the
[Towny](https://github.com/TownyAdvanced/Towny) plugin.
It has since then been adapted over time to retain its functionality.

Flag War works similar to a strategy game, where players can capture regions of other Nations and
Towns quickly and with pin-point precision.

About mid-2020, an effort to split it off was made with work started towards necessary API changes
in Towny itself. The actual split of Flag War began January 2021.

___

## Current Recommended Versions
* [Latest Release](# "Pending Release") - Supports servers running Towny v0.96.6.0
* [Latest Pre-Release](# "Pending Release") - Supports servers running Towny v0.97.x.x

Note: Prior to Towny v0.9x.x.x - Flag War is included in Towny.

___

## Staying up to date

<p><img align=right src="https://user-images.githubusercontent.com/879756/65964779-3a067200-e423-11e9-9928-938b976af2c2.gif" height="155">

All Release builds and Development builds are being made available here on github's
[Releases](https://github.com/TownyAdvanced/FlagWar/releases) tab. We encourage server admins to
"watch" Flag War on github. Just click the watch button in the upper right and select "Releases Only".
</p>

___

## Connect/Support

The documentation found on [the Flag War Wiki](https://github.com/TownyAdvanced/FlagWar/wiki) is
updated every time a Release version of Flag War is put out.

<!--
- Some important pages to look over:
- [Installing Towny](https://github.com/TownyAdvanced/Towny/wiki/Installation)
- [How Towny Works](https://github.com/TownyAdvanced/Towny/wiki/How-Towny-Works)
-->

Here on github's [Issue Tracker](https://github.com/TownyAdvanced/FlagWar/issues) you can file
[bug reports](https://github.com/TownyAdvanced/FlagWar/issues/new?assignees=&labels=&template=bug_report.md&title=),
[feature requests](https://github.com/TownyAdvanced/FlagWar/issues/new?assignees=&labels=&template=feature_request.md&title=Suggestion%3A+),
or just ask [general questions](https://github.com/TownyAdvanced/Towny/discussions/new?category=Q-A)
on the Towny discussion board.

[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/TownyAdvanced/FlagWar.svg)](http://isitmaintained.com/project/TownyAdvanced/FlagWar "Average time to resolve an issue") [![Percentage of issues still open](http://isitmaintained.com/badge/open/TownyAdvanced/FlagWar.svg)](http://isitmaintained.com/project/TownyAdvanced/FlagWar "Percentage of issues still open")

If you still need help, join us on the [Discord server]( https://discord.gg/gnpVs5m ),
where you can find cutting edge updates and discussion on the development of the plugin.

If you want to support the developer consider becoming a Sponsor.

___

## Video Tutorials

Courtesy of Major_Graft, we have a new series of Tutorial Videos [available on the Towny website.](https://townyadvanced.github.io/tutorials.html).
These are geared more towards Towny in general, but are good resources for your review none the less.

___

## Contributing

If you'd like to contribute to the FlagWar code, familiarize yourself with the
[Contributing](https://github.com/TownyAdvanced/FlagWar/blob/master/.github/CONTRIBUTING.MD) guidelines.

___

## Licensing

Flag War is licensed under the
[Apache License, Version 2.0](https://github.com/TownyAdvanced/FlagWar/blob/master/LICENSE).

The decision to not use [Towny's license (CC BY-ND-NC 3.0)](http://creativecommons.org/licenses/by-nc-nd/3.0/)
was made for the following reason: We want you to be allowed to adapt, extend, or modify it to fit your needs.

Want to adapt it to Factions? How about PlotSquared? Be our guest! Just don't forget to read the license first.

We would still love for you to PR back when feasible. It's a great way to give back to the project.

___

## Building Flag War

If you would like to build from a specific branch yourself, you can do so with [Apache Maven](http://maven.apache.org/).

Please make sure you have Maven on your PATH before you proceed with this method.

- Open your terminal shell / command prompt and navigate to the `.../FlagWar` directory.

- Run `mvn clean package` to generate the plugin in the `.../FlagWar/target` directory.

- (Optional) Run `mvn install` to install Flag War to your local maven repository.

Alternatively, you can build it through your IDE provided that it bundles Maven with itself, or can
hook into it. See your IDE's documentation for details.