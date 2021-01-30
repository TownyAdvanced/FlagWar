# [FlagWar for Towny Advanced](https://townyadvanced.github.io/wars)

FlagWar is a variation on the first official war system in the Towny Advanced ecosystem, dating back
around 2011.

> I started getting re-involved with Towny’s development, and got some inspiration from users about
> how to do warring Nations. I also took a gander at some of the mechanics of a similar plugin that
> did PvP stuff right, Factions. I coded up some threaded tasks that created a huge beacon in the
> sky overtop the area under attack. The game mechanic was to attack and hold the area. Towny’s war
> event is the same thing, but there’s nothing physical. So I created a focus for the defenders.
> Attacking would have the attacker place a flag, which the defenders would need to break / take
> down. I eventually merged this into the Towny plugin.&nbsp;&mdash;-&nbsp;[@Zren](https://github.com/Zren)
> &ndash; [from his blog](https://zren.github.io/timeline/#bukkit-plugin--cellwar)

It has since then been adapted over time to retain its core functionality, while it continues to
provide server communities with a simple, yet effective, way to capture territory.

FlagWar works similar to a strategy game, where players can capture regions of other Nations and
Towns quickly and with pin-point precision.

About mid-2020, an effort to split it back off from Towny began. Necessary API changes in Towny
itself took priority for a time...

FlagWar was finally fully split off in ________________.

___

## Supported Releases

| FlagWar Release | Release Date | Minimum Requirements |
|:--------------: | :----------: | :------------------: |
| 0.1.0 (Latest)  | xxxxx        | Towny 0.96.7.1, Spigot 1.14.4 |
| _Pre-History_   | _2011_       | _Included in Towny until v0.9x.x.x; no support._ |

> We recommend administrators look into [PaperMC](https://papermc.io/), or one of its forks.
> 
> While FlagWar does not use the PaperAPI, Towny does make use of the PaperLib library to provide
> some optimizations. The PaperMC server software also offers greater flexibility for a server
> administrator to take advantage of when compared to Spigot.

___

## Staying up to date

<p><img align=right src="https://user-images.githubusercontent.com/879756/65964779-3a067200-e423-11e9-9928-938b976af2c2.gif" height="155">

All Release builds and Development builds are being made available here on GitHub's
[Releases](https://github.com/TownyAdvanced/FlagWar/releases) page. We encourage server admins to
"watch" FlagWar on GitHub in order to receive update notifications.
Just click the watch button in the upper right and select "Releases Only".
</p>

___

## Connect/Support

The documentation found on [the FlagWar Wiki](https://github.com/TownyAdvanced/FlagWar/wiki) will be
updated every time a Release version of FlagWar is tagged. If you find the documentation
insufficient, open an issue, and we will assess the need.

<!--
- Some important pages to look over:
- [Installing Towny](https://github.com/TownyAdvanced/Towny/wiki/Installation)
- [How Towny Works](https://github.com/TownyAdvanced/Towny/wiki/How-Towny-Works)
-->

Here on GitHub's [Issue Tracker](https://github.com/TownyAdvanced/FlagWar/issues) you can file
[bug reports](https://github.com/TownyAdvanced/FlagWar/issues/new?assignees=&labels=&template=bug_report.md&title=),
[feature requests](https://github.com/TownyAdvanced/FlagWar/issues/new?assignees=&labels=&template=feature_request.md&title=Suggestion%3A+),
or just ask [general questions](https://github.com/TownyAdvanced/Towny/discussions/new?category=Q-A)
on the Towny discussion board.

[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/TownyAdvanced/FlagWar.svg)](http://isitmaintained.com/project/TownyAdvanced/FlagWar "Average time to resolve an issue") [![Percentage of issues still open](http://isitmaintained.com/badge/open/TownyAdvanced/FlagWar.svg)](http://isitmaintained.com/project/TownyAdvanced/FlagWar "Percentage of issues still open")

If you still need help, join us on the [Discord server]( https://discord.gg/gnpVs5m ),
where you can find cutting edge updates and discussion on the development of FlagWar, as well as
the rest of the plugins in the Towny Advanced family.

If you want to support the development of FlagWar, or the Towny Advanced project, consider becoming
a Sponsor. We also accept code contributions and review.

___

## Video Tutorials

Courtesy of Major_Graft, we have a new series of Tutorial Videos
[available on the Towny website.](https://townyadvanced.github.io/tutorials.html).
These are geared more towards Towny in general, but are good resources for your review nonetheless.

___

## Contributing

If you would like to contribute to the FlagWar code then you need to read the
[Contributing](https://github.com/TownyAdvanced/FlagWar/blob/master/.github/CONTRIBUTING.MD)
guidelines.

If you already have, then you will also want to get your tools ready.

For basic work, involving only minor changes (1-2 lines), you can use GitHub's built-in editor after
forking the project.

For anything more involved, you will need to fulfill the following requirements:
- A Java Development Kit, for Java SE 8 or higher
- A competent Editor or an Integrated Development Environment (IDE)
    - Competent Editors: [Atom][1], [Notepad++][4], [Sublime Text][3], [Vim][5], [Visual Studio Code][2]
      - No, Windows Notepad is not a competent editor.
      - Vim is like smoking, it's hard to quit.
    - Recommendable IDEs: [Apache NetBeans][8], [Eclipse IDE][6], [IntelliJ IDEA][7]
- Apache Maven (See [Building FlagWar](#building-flagwar))

> Note that some IDEs bundle a JDK and Apache Maven. And for the love of all that is code, do not use Microsoft Notepad.

    
[1]: https://atom.io/ "A hackable text editor for the 21st Century (Free, Cross Platform)"
[2]: https://code.visualstudio.com "Code editing. Redefined. (Free, Cross Platform)"
[3]: https://www.sublimetext.com/ "A sophisticated text editor for code, markup and prose (Trialware, Cross Platform)"
[4]: https://notepad-plus-plus.org/ "A free (as in speech, and beer) source code editor and Notepad replacement (Free, Windows)"
[5]: https://www.vim.org "The ubiquitous text editor"
[6]: https://www.eclipse.org/eclipseide/ "The Leading Open Platform for Professional Developers"
[7]: https://www.jetbrains.com/idea/ "The Capable and Ergonomic Java IDE by JetBrains"
[8]: https://netbeans.apache.org/ "Fits the Pieces Together"
___

## Licensing
<img align="right" height="155" src="https://opensource.org/files/OSI_Approved_License.png">
FlagWar is licensed under the
[Apache License, Version 2.0](https://github.com/TownyAdvanced/FlagWar/blob/master/LICENSE), an
Open Source Initiative approved license.

Some portions, such as shaded libraries, may be alternatively licensed. See the
[NOTICE file](https://github.com/TownyAdvanced/FlagWar/blob/master/NOTICE) for any alternative
licensing.

The decision to not use [Towny's license (CC BY-ND-NC 3.0)](http://creativecommons.org/licenses/by-nc-nd/3.0/)
was made for the following reason: We want you to be allowed to adapt, extend, or modify it to fit your needs.

Want to adapt it to Factions? How about PlotSquared? Be our guest! Just do not forget to read the license first.

We would still love for you to PR back when feasible. It is a great way to give back to the project.

___

## Building FlagWar

If you would like to build from a specific branch yourself, you can do so with [Apache Maven](http://maven.apache.org/).

Please make sure you have Maven on your PATH before you proceed with this method.

- Open your terminal shell / command prompt and navigate to the `.../FlagWar` directory.

- Run `mvn clean package` to generate the plugin in the `.../FlagWar/target` directory.

- (Optional) Run `mvn install` to install FlagWar to your local maven repository.

Alternatively, you can build it through your IDE provided that it bundles Maven with itself, or that
it can at the very least hook into it. Check the documentation for your specific IDE for support.
