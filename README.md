[FlagWar for Towny Advanced](https://townyadvanced.github.io/wars)
==================================================================

----
### Important Notices

<details><summary><b>Transitioning from the Integrated FlagWar system.</b></summary>

FlagWar has it's own methods for configuration and localization, independent of Towny. Please be advised that we
currently only provide an English translation. There will likely be other teething issues, but we are here to help with
them when required. See [Getting Support][get-support] if you have issues.

If you would like to help out with development, translations, or other efforts: please see the 
<a href="#developer-resources">Developer Resources</a> section.
</details>

<details><summary><b>Metrics / Telemetry</b></summary>

FlagWar, makes use of the [bStats](https://bstats.org/) metrics library. For an idea of what is collected, you can view
the telemetry reports [here](https://bstats.org/plugin/bukkit/FlagWar/10325).
While we would appreciate it if you were to keep bStats reporting enabled, you can opt-out of sending telemetry
by modifying the bStats config found at `yourServer/plugins/bStats/`.
</details>

----
Summary
----

FlagWar is one of the first official war systems in the Towny Advanced ecosystem, dating back
around 2011. FlagWar works similar to a strategy game, where players can capture regions of other Nations and
Towns quickly and with pin-point precision.

> I started getting re-involved with Towny’s development, and got some inspiration from users about
> how to do warring Nations. I also took a gander at some of the mechanics of a similar plugin that
> did PvP stuff right, Factions. I coded up some threaded tasks that created a huge beacon in the
> sky overtop the area under attack. The game mechanic was to attack and hold the area. Towny’s war
> event is the same thing, but there’s nothing physical. So I created a focus for the defenders.
> Attacking would have the attacker place a flag, which the defenders would need to break / take
> down. I eventually merged this into the Towny plugin.&nbsp;&mdash;&nbsp;[@Zren](https://github.com/Zren)
> &ndash; [from his blog][zren-blog]

It has since then been adapted over time to retain its core functionality, while it continues to
provide server communities with a simple, yet fast and effective, way to capture territory.

About mid-2020, an effort to split it back off from Towny began. FlagWar was finally fully split off in 2021, marking
its 10th anniversary.

Sections
--------
1) [Licensing 📜][licensing]
2) [Administrator Resources 👨‍💻][admin-resources]
   - [Supported Releases 📦][supported-releases]
   - [Staying up to Date 📨][staying-updated]
   - [Getting Support ⚕][get-support]
3) [Developer Resources 🧰][developer-resources]
   - [Contributing Code 💻][contrib-code]
   - [Contributing Documentation 🗒][contrib-docs] - WIP
   - [Localizing FlagWar 🗺][contrib-localize]    - WIP
   - [Building FlagWar 🏗][building]
4) [Supporting the Project 🦸][supporting-flagwar]    

Licensing
---------

<img align="right" height="155" src="https://opensource.org/files/OSI_Approved_License.png">

FlagWar is licensed under the [Apache License, Version 2.0][apache-v2], a license approved by the
[Open Source Initiative][osi] and is [GPL Compatible][gpl-apache].
Some portions, such as shaded libraries, may be alternatively licensed. See the [NOTICE][notice] for any alternative
licensing or copyright limitations.

Documentation found on the [FlagWar Wiki][wiki] is licensed under [CC BY 4.0][cc by].

Additionally, please respect [Towny's license][cc by-nc-nd 3.0] when using FlagWar with Towny.
Use of FlagWar does not constitute a way to bypass Towny's license restrictions.
When in doubt, [ask for clarification][get-support]. 

Administrator Resources
-----------------------

### Supported Releases

| FlagWar Release | 📅 Released On   | Requirements                                                                |
| :-------------: | :--------------: | :-------------------------------------------------------------------------: |
| [v0.5.1][0.5.1] | October 27, 2021 | Towny (_0.97.2.0_+), Bukkit API-based&ast; server (_1.16.5_, _1.17.1_)      |
| [v0.4.0][0.4.0] | August 12, 2021  | Towny (_0.97.0.17+_), Bukkit API-based&ast; server (_1.16.5_, _1.17.1_)     |
| _Pre-History_   | _2011 - 2021_    | Deprecated & Removed in Towny versions 0.97.0.0 and 0.97.1.0, respectively. | 

> FlagWar has adopted the use of Paper as an API provider. Spigot should however still be viable. Please
> submit a ticket if you experience any issues.

[0.5.1]: https://github.com/TownyAdvanced/FlagWar/releases/tag/0.5.1b "FlagWar Release 0.5.1"
[0.4.0]: https://github.com/TownyAdvanced/FlagWar/releases/tag/0.4.0 "FlagWar Release 0.4.0"

### Staying up to Date

<img align=right src="https://user-images.githubusercontent.com/879756/65964779-3a067200-e423-11e9-9928-938b976af2c2.gif" height="155">

All Release builds and Development builds have been made available here on GitHub's [Releases][releases] page.  
We encourage server admins to "watch" FlagWar on GitHub in order to receive update notifications.  
Just click the watch button in the upper right and select "Releases Only".

Sometimes, releases can be incubating for extended periods of time.   
You can build from main if you want to test on the bleeding edge.

### Getting Support

The documentation found on the [FlagWar Wiki][wiki] will be updated semi-regularly, usually after a tagged release.
If you find the documentation insufficient, please open an issue so that we can address it.

On the [Issue Tracker][issue-tracker] you can file [bug reports][bug],
[feature requests][feature], or review / submit a [QA discussion][discuss-towny] on the Towny discussion board.

[![Average Issue Resolution Time][iim-time-badge]][iim-time] [![Percentage of Issues 'Open'][iim-percent-badge]][iim-percent]

If you still need help, come and join us on the [TownyAdvanced Discord server][discord]. There, you can also find
support, get notified on the latest updates for Towny, chat with other admins, and have discussions over plugin
development with developers from multiple projects.

Developer Resources
-------------------

### Contributing Code

If you would like to contribute to the FlagWar code, first please read the [Contributing Guidelines][contributing].

You will also want to ensure that your working environment is in order. You can check out
[Environment Setup][env-setup] over on our wiki to help you get started.

### Contributing Documentation

Documentation could also use a fair bit of work. Help in this department would be much appreciated.

We document both in plain-text (LICENSE, NOTICE) and in GitHub-Flavored Markdown (everywhere else).

If you are interested in writing for the [FlagWar Wiki][wiki], feel free to ask on the [Towny Discord][discord]
(ping FlagCourier), or chime in on the [Towny Discussion Board][discuss-towny] to be given write access. 

### Localizing FlagWar

The localization files for FlagWar are built directly into the jar using ResourceBundles.

To localize FlagWar to your language, copy the 
[Translation_en_US.properties](src/main/resources/Translation_en_US.properties) file from the
[`resources` directory](src/main/resources) as `Translation_{LOCALE}.properties`.

If a locale already exists, please refrain from creating a duplicate. Some locales may be stubs that were added for
debugging purposes (en_GB, en_US[-_]POSIX, es_MX.) Feel free to modify them, but ensure they have the same key values
as the master file (en_US).

The `LOCALE`, or the locale id compatible with the Java Locale class,
is a 1-to-3 segment string representing the _language_, the _region_, and the _variant_ (if desired);
delimited primarily by underscores, with variants optionally being hyphenated. 

Examples: en, en_US, en_US-POSIX.

See the Java [Locale](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Locale.html) documentation 
for well-formed naming. 

> &ast; FlagWar's Locale IDs should stick to the `lang(_REGION([_\-]VARIANT))` format.  
> PRs to Locale processing are welcome. It's pretty messy in there...

After translation is complete, save your changes. You will need to [Build FlagWar](README.md#Building-FlagWar) to ensure
that all strings are accounted for, and that they render properly for end users.

### Building FlagWar

Assuming you've got your [environment properly set up][env-setup], building FlagWar is relatively straight forward.

Steps:
1) Clone FlagWar from GitHub.
    - _See the [GitHub Docs][github-docs], if you are unfamiliar with cloning projects._
2) Navigate into the cloned `FlagWar` directory in your terminal.
3) Run `mvn clean package` to build FlagWar. This will generate files to the `FlagWar/target/` directory.
    - We also supply several maven profiles for convenience. Append `-P profileX,profileY,profileZ` to use them,
      obviously replacing the ridiculous placeholder profiles listed here.
    - The `checkstyle` profile will ensure that code edits are in-line with the code style guidelines.
    - The `javadocs` profile will generate a `FlagWar-version-javadoc.jar` file, as well as the
      `FlagWar/target/apidocs/` folder.

You can alternatively build FlagWar through your IDE, provided that it includes Maven, or that
it can at least find it. Check your IDE's documentation regarding Maven support.

Supporting the Project
----------------------

If you've found FlagWar to be of use and would like to support the project, then thank you!

You can support the project in multiple ways:

- **Contribute to the project 📝**  
  Be it through [adding or expanding a localization][contrib-localize], endorsing the project,
  hunting for bugs, [writing code][contrib-code], or [writing documentation][contrib-docs], any help is appreciated.
  

- **Sponsor a Developer 💗**  
  Sponsoring a developer gives back to those who have spent time to keep the project going.  
  See the sidebar for open sponsorships and learn about GitHub's [Sponsors Program][gh-sponsors].


- **Use FlagWar ⛳**  
  Using FlagWar lets us know that people still love the war system.  
  Keeping metrics on, while optional, also gives us a rough idea of the adoption of FlagWar in the
  TownyAdvanced ecosystem.

<!-- Links -->
[admin-resources]: README.md#administrator-resources "Administrator Resources"
[apache-v2]: LICENSE "Apache License, Version 2.0"
[atom]: https://atom.io/ "A hackable text editor for the 21st Century (Free, Cross Platform)"
[boards-1]: https://github.com/TownyAdvanced/FlagWar/projects/1 "FlagWar: Feature Parity Checklist"
[bug]: https://github.com/TownyAdvanced/FlagWar/issues/new?assignees=&labels=&template=bug_report.md&title= "Report a FlagWar bug"
[building]: README.md#building-flagwar "Building FlagWar"
[cc by-nc-nd 3.0]: https://creativecommons.org/licenses/by-nc-nd/3.0/legalcode "Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported"
[cc by]: https://creativecommons.org/licenses/by/4.0/ "Creative Commons Attribution 4.0 International"
[contrib-code]: README.md#contributing-code "Contributing Code"
[contrib-docs]: README.md#contributing-documentation "Contributing Documentation"
[contrib-localize]: README.md#localizing-FlagWar "Localizing FlagWar"
[contributing]: ./.github/CONTRIBUTING.MD "FlagWar Contributing Guidelines"
[corretto]: https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/patches.html "Corretto OpenJDK 8 Patches"
[developer-resources]: README.md#developer-resources "Developer Resources"
[discord]: https://discord.gg/gnpVs5m "Join the TownyAdvanced Discord server"
[discuss-towny]: https://github.com/TownyAdvanced/Towny/discussions/categories/q-a "View Towny's Q&A Discussion Board"
[eclipse]: https://www.eclipse.org/eclipseide/ "The Leading Open Platform for Professional Developers"
[env-setup]: https://github.com/TownyAdvanced/FlagWar/wiki/Environment-Setup "FlagWar Docs: Environment Setup"
[feature]: https://github.com/TownyAdvanced/FlagWar/issues/new?assignees=&labels=&template=feature_request.md&title=Suggestion%3A+ "Request a new feature or tweak"
[get-support]: README.md#getting-support "Getting Support"
[gh-sponsors]: https://github.com/sponsors "Invest in the software that powers your world"
[git-docs]: https://git-scm.com/doc "Git Documentation"
[git]: https://git-scm.org/ "Git Version Control Software"
[github-docs]: https://docs.github.com/ "GitHub Documentation"
[gpl-apache]: https://www.gnu.org/licenses/license-list.html#apache2 "GPL Compatible Free Software Licenses"
[idea]: https://www.jetbrains.com/idea/ "The Capable and Ergonomic Java IDE by JetBrains"
[iim-percent-badge]: http://isitmaintained.com/badge/open/TownyAdvanced/FlagWar.svg
[iim-percent]: http://isitmaintained.com/project/TownyAdvanced/FlagWar "Percentage of Issues 'Open'"
[iim-time-badge]: http://isitmaintained.com/badge/resolution/TownyAdvanced/FlagWar.svg
[iim-time]: http://isitmaintained.com/project/TownyAdvanced/FlagWar "Average Issue Resolution Time"
[issue-tracker]: https://github.com/TownyAdvanced/FlagWar/issues "FlagWar Issue Tracker"
[jdk]: https://sdkman.io/jdks "JDK Distributions | SDKMAN!"
[licensing]: README.md#licensing "Licensing"
[maven]: https://maven.apache.org/ "Apache Maven Software Project Management and Comprehension Tool"
[netbeans]: https://netbeans.apache.org/ "Fits the Pieces Together"
[notice]: NOTICE "Legal Notices for FlagWar"
[npp]: https://notepad-plus-plus.org/ "A free (as in speech, and beer) source code editor and Notepad replacement (Free, Windows)"
[osi]: https://opensource.org/licenses "Licenses & Standards | Open Source Initiative"
[releases]: https://github.com/TownyAdvanced/FlagWar/releases "FlagWar Tagged Releases"
[sponsor-LlmDl]: https://github.com/sponsors/LlmDl "Sponsor LlmDl, the current TownyAdvanced lead developer and maintainer"
[staying-updated]: README.md#staying-up-to-date "Staying up to Date"
[sublime]: https://www.sublimetext.com/ "A sophisticated text editor for code, markup and prose (Trialware, Cross Platform)"
[supported-releases]: README.md#supported-releases "Supported Releases"
[supporting-flagwar]: README.md#supporting-the-project "Supporting the Project"
[vim]: https://www.vim.org "The ubiquitous text editor"
[vscode]: https://code.visualstudio.com "Code editing. Redefined. (Free, Cross Platform)"
[wiki]: https://github.com/TownyAdvanced/FlagWar/wiki "Official FlagWar Documentation"
[zren-blog]:https://zren.github.io/timeline/#bukkit-plugin--cellwar "Timeline / Projects | zren.github.io"
