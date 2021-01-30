# Contributing to Flag War

## Contributing Code

Code contributions are welcome and are made via GitHub's Pull Request.
You can ask for any information/help on your idea/contribution in the
[TownyAdvanced Discord](https://discord.gg/gnpVs5m).

* FlagWar is licensed under the
  [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt). Please read it to
  familiarize yourself with the license.
  - If you disagree with the license, do not submit your code.
* By opening a Pull Request on GitHub, you represent that you have read and agree with
  the terms of the project's license; and that you are the author of your changes; or that the code
  you add is either available in the public domain, or by permission of the original author
  (with you providing proof.)

## Contributing Localizations

Community Translations are greatly appreciated, and allow FlagWar to be used by many others that
may not speak the language used by the developers.

A few ground rules for translations:
- No machine-generated text translations. You cannot trust some files spat out by some bot running
  on Amazon, Google, or Microsoft servers.
- Adhere to the Java Locale language tag when naming your localization.
  - [List of Locales supported in Java 8](https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html)
  - Example: es_MX, en_US, de_DE

# Pull & Code Requirements

## Pull-Request Requirements

1. Keep your commit message(s) self-explanatory. The description should be a brief explanation and,
   if applicable, include a hash-tagged reference to any directly related issues or pull requests.
2. Pull Requests should be simple and straight-forward. One pull request for one feature. Changed
   Files should only show a difference where code has been removed, added, or refactored &mdash;
   no re-formatting of entire files for formatting's sake.
3. Test everything you've touched before requesting changes to be merged. Untested code will be
   rejected, and you may end up on our naughty list.

## General Code Requirements

> FlagWar makes use of an [EditorConfig](https://editorconfig.org) file to help ensure consistent
> formatting among contributors.
>
> [Editors that support it](https://editorconfig.org/#pre-installed) should automatically follow
> part 1 of this section, and partially assist with part 4.
>
> Some editors do not include native support, but may have
> [plugins available](https://editorconfig.org/#download).

1. Spaces, Not Tabs
   - The source should be indented using spaces rather than tabs. Standard indentation consists of
     4 spaces per every indent, but this can vary at times. Please respect the original.
   - Please disable any automated source formatting in your IDE if applicable, or set it to use 4
     spaces.
2. Keep your Java compiler target set to language level 8.
   - A good portion of servers are not capable of running on top of newer runtimes, yet.
3. Avoid using Wildcard imports.
   - The OpenJDK reference compiler _should_ be able to import only what it needs, but we cannot
     guarantee this for all vendors / variations.
4. Don't run IDE Formatters over entire class files.
   - This is done to avoid us changing the lines everytime, each IDE is different, so we don't have
     a constant here and there.
   - This also keeps file compares on github simpler for maintainers to parse through.
5. Do not use NMS (Net Minecraft Server) or CraftBukkit (server-specific) code. FlagWar uses the
   Spigot API only.
6. Reflection should be a _last resort_ reserved for special circumstances.
   - Reflection among our internal classes is **not acceptable**. This is just bad design.
7. No Packet-Handling, period.
   - Packet-handling can lead to additional maintenance tasks down the road where we may not be
     equipped for it.
   - Includes using Libraries like Netty and ProtocolLib.

## Dependency Inclusion

For the sanity of the rest of us, please adhere to the following guidelines when attempting to
include a dependency or 3rd-party library.

1. Libraries need to be approved by the current maintainer before being merged.
   Preferably, you should ask before committing.

2. Prevent ClassNotFound errors - Do not reference a plugin in a loaded class, unless it is enabled.

3. Do not embed your own plugins within FlagWar
   - Dependencies should be within the 'Provided' scope.
   - They will also need peer-review.

4. No "Black Box" or Obfuscated dependencies.
   - All dependencies should have their source open and auditable.

5. Libraries and code snippets need to be of a compatible license with ours.
   - Do not violate either project's license in implementing your changes.
   - 3rd Party code without a license will be denied outright under the understanding that the
     original author is reserving all rights. Please ask them to issue a software license.

6. Any additional libraries or files under copyright of someone else need to be added to the
   `NOTICE` file. Please list the author, the copyright, the license, and path to the file(s) if it
   is not imported through maven.

## Updating Language Files

>   **CAUTION**
>   This section is in dire need of being rewritten still. It may not be accurate to the final
>   specifications yet to be laid out. We may duplicate old code, or switch to a new system here...

The language files are stored in the resources section of the project. (`resources/"language".yml`)
To update / change an existing language file, fork this repo and then edit the "language".yml in your fork.
Pull-Request your updated version. Please try to keep the title simple and include the version number:
e.g "German Translation v0.42"

If you are creating a new language file, feel-free to use either another translation (e.g "es-mx")
as a template.
Please run your final translation through a YAML validator. Certain formatting marks are out of
specification in yaml, or require being escaped. As an example, you have to double up the `''`
(apostrophe) when you want one `'` to appear.
