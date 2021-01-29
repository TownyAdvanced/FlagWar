# Contributing to Flag War

## Contributing Code

Code contributions are welcome and are made via GitHub's Pull Request.
You can ask for any information/help on your idea/contribution in the [TownyAdvanced Discord](https://discord.gg/gnpVs5m).

* Please make sure to read our [License](https://github.com/TownyAdvanced/FlagWar/blob/master/LICENSE). If you do not agree with the terms and conditions of our license, please do not contribute any code.
* By making a pull request, you represent that you have the right to waive copyright and related rights to your contribution, and agree that all copyright and related rights in your contributions are waived, and you acknowledge that the TownyAdvanced organization has the copyright to use and modify your contribution under our [License](https://github.com/TownyAdvanced/FlagWar/blob/master/LICENSE) for perpetuity.

# Pull & Code Requirements

## Pull-Request Requirements

1. Keep your commit message(s) self-explanitory, description should be a brief explanation and include an issue reference (if applicable.).
2. Pull Requests should be simple and straight-forward. One pull request for one feature. Changed Files should only show a difference where code has been removed or added, no re-formatting of entire files.
3. Test everything you've touched before pull-requesting. (Exception being language files, we don't expect you to check all lang strings.)

## General Code Requirements

> With the inclusion of the Maven build system, FlagWar also makes use of a [EditorConfig](https://editorconfig.org) file.
> IDEs that support it should automatically follow parts of this section.

1. Spaces, Not Tabs
   - The source should be indented using spaces rather than tabs. Standard indentation consists of
     4 spaces per every indent, but this can vary at times. Please respect the original.
   - Please disable any automated source formatting in your IDE if applicable, or set it to use 4 spaces.
2. Keep your Java compiler target set to language level 8.
   - A good portion of servers are not capable of running on top of newer runtimes, yet.
3. Avoid using Wildcard imports.
   - The OpenJDK reference compiler _should_ be able to import only what it needs, but we cannot
     guarantee this for all vendors / variations.
4. Don't run IDE Formatters over entire class files.
   - This is done to avoid us changing the lines everytime, each IDE is different, so we don't have
     a constant here and there.
   - This also keeps file compares on github simpler for maintainers to parse through.
5. Do not use NMS (Net Minecraft Server) or CraftBukkit (server-specific) code. FlagWar uses the Spigot API only.
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

<!-- TODO: Rewrite this section after Flag War implements it's own language files. -->

The language files are stored in the resources section of the project. (`resources/"language".yml`)
To update / change an existing language file, fork this repo and then edit the "language".yml in your fork.
Pull-Request your updated version. Please try to keep the title simple and include the version number:
e.g "German Translation v0.42"

If you are creating a new language file, feel-free to use either another translation (e.g "es-mx")
as a template.
Please run your final translation through a YAML validator. Certain formatting marks are out of
specification in yaml, or require being escaped. As an example, you have to double up the `''`
(apostrophe) when you want one `'` to appear.
