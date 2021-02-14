# Contributing Guidelines for FlagWar

## Contributing Code

Code contributions are welcome and are made via GitHub's [Pull Request][pull request] system. You can ask for any information
or help with your idea or contribution in the [TownyAdvanced Discord](https://discord.gg/gnpVs5m).

* FlagWar is licensed under the [Apache License, Version 2.0][apache-v2]. ([Local Copy][license])  
  Please read it to familiarize yourself with the license terms.
    
* By opening a Pull Request on GitHub, you represent that you have read and agree with the terms of the project's
  license; and that you are the author of your changes, or that the code you add is either available in the public
  domain or licensed under the MIT or Apache licenses and has been appropriately attributed to its creators in both it's
  implementation and within the [NOTICE][notice] file.
  
[notice]: ../NOTICE "FlagWar License Notices"
[apache-v2]: https://www.apache.org/licenses/LICENSE-2.0.txt "Apache License, Version 2.0"
[license]: ../LICENSE "FlagWar License (Local Copy)"
[pull request]: https://github.com/TownyAdvanced/FlagWar/pulls "FlagWar Pull Requests"

## Contributing Localizations

> ⚠ **IMPLEMENTATION NOTICE**  
> Localization features have not yet been implemented within FlagWar, and localization is currently dependent
> on Towny. Some strings are currently hard-coded into English.

Community submitted localizations are greatly appreciated, and allow FlagWar to be used by many others that may not
speak the language(s) used by the developers.

A few ground rules for translations:
- No machine-generated text translations. You cannot simply trust some files spat out by some bot running on Amazon,
  Google, or Microsoft servers.
- Adhere to the [Java Locale][java-locales] language tag when naming your localization, unless it's a fantasy locale
  emulating a work of fiction. (e.g. Languages by J.R.R. Tolkien, _Pirate Speak_, or _Klingon_)
    
[java-locales]: https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html
    "JDK8: JRE8 Supported Locales | Oracle"

# Pull & Code Requirements

## Git Etiquette

1. Everything you do should first be ticketed on the [issue tracker][issues] explaining the problem, or feature request.
    - Good reference specification for everyone. Others can review the issue and provide input.
    - Some features may be out of scope or issues closed for working as intended.
    
2. Never target `main`. There is a `development` branch for a reason. 
    - Major features may have their own distinct branches, such as `feature/warflag_as_sheep`. When working on these
    branches, you should ensure that your PRs point to them.
     
3. Utilize the commit messages to explain what you're doing. [Stop writing bad commit messages!][stop-soni]
   
   > ![Source: XKCD #1296](https://imgs.xkcd.com/comics/git_commit.png)  
   > 🖼 _[XKCD #1296, "Git Commit"][xkcd1296]_

4. Single-subject rule: Pull requests should be limited in scope to the related ticket(s).
    - See something else to tweak that is outside the PR's scope? Create a new branch derived from `development` and
      start the PR process for it separately. 
      
5. Don't PR incomplete work &mdash; if it doesn't compile, or isn't hooked up, keep it as a Draft PR until it is ready
   and has been tested.   

6. Test *everything* you have touched in at least a minimum playable environment on OpenJDK 8, with the API-targeted
   version of Spigot.
   
[issues]: https://github.com/TownyAdvanced/FlagWar/issues "FlagWar Issue Tracker"
[stop-soni]: https://medium.com/better-programming/stop-writing-bad-commit-messages-8df79517177d
    "Stop Writing Bad Commit Messages by Devin Soni"
[xkcd1296]: https://xkcd.com/1296/ "Git Commit | XKCD"

## General Code Requirements

> ℹ **Portability**  
> FlagWar makes use of `.gitattributes` and an [EditorConfig](https://editorconfig.org) files to help ensure consistent
> file portability among contributors. [Editors that support EditorConfig](https://editorconfig.org/#pre-installed)
> should automatically follow parts of this section. Some editors may require a
> [plugin](https://editorconfig.org/#download) to automate compliance.

> ⚠ **NOTICE**  
> _Maven will refuse to compile FlagWar if any files are out of the `.editorconfig` specifications. You can
> force compliance by running `mvn editorconfig:format` on a terminal, or through your IDE if it supports executing
> Maven plugins directly._

1. **Indentation: 4 Spaces**
    - No file should use tabs for indentation.
    - If your IDE is insane or doesn't respect the `.editorconfig`, please disable any automated source formatting in
      your IDE, or set it to use 4 spaces.
     
2. **Use Java Language Level 8**
    - Ensure your IDE is configured to use language level 8. If your IDE does not support setting the language level,
      ensure that you use a copy of OpenJDK 8 to avoid issues, or find a different IDE.
    - A good portion of servers are not yet capable of running on top of newer runtimes.
    
3. **No Wildcard Imports**
    - Either directly import for each class (preferred),
      or specify the full path to a method (`io.townyadvanced.flagwar.FlagWar.someMethod()`) if there are fewer than two
      occurrences in a file (import still preferred).
    - If your IDE combines imports for you, disable the respective feature or bump its threshold up.
    
4. **Line Endings and New Lines**
    - Use Unix-style (LF) file line endings, and platform-neutral new lines in Java Strings (`%n` > `\n`)
        - Exception: Any Windows scripts (.bat, .cmd, .ps1) _should_ be CRLF formatted.
        - All modern editors support LF line endings on Windows, [including Notepad][notepad-lf]
          (Don't use notepad... 🤦)
    - Use Platform-Neutral new-line characters when manipulating Strings.
        - `%n` is *always* a newline, regardless of the platform or JDK vendor, whereas `\n` is not guaranteed to be so.
    
5. **Use of Server-specific Code**
    - Do not use NMS (`net.minecraft.server`) or server implementation-specific code.
    - If you wish to incorporate Paper features, consider using PaperLib. We will continue to target the SpigotAPI as a
      base target for compatibility reason.
   
6. **Use of Reflection**
    - Reflection should be a _last resort_ reserved for special circumstances.
    - Reflection among our internal classes is **not acceptable**. This is of poor design, and should be refactored.
    
7. **Packet Handling**
    - The TownyAdvanced group is not interested in being held responsible for poor packet handling implementations. We 
      have chosen to leave these tasks up to the server implementation.
    - This also means no use of packet-manipulating libraries such as Netty and ProtocolLib.
     
8. **Deprecating Code** 
   When deprecating methods, the deprecated method should implement the replacement method to fallback gracefully. These
   methods should also be appropriately annotated in the Javadocs if public.

   ```java
   /* Example */
   public class FlagWar {
        /**
         * Gets the FlagWar plugin instance.
         * @return the FlagWar instance.
         * @deprecated Use {@link io.github.townyadvanced.flagwar.FlagWar#getInstance()} instead.
         */
        @Deprecated
        public static FlagWar getPlugin() {
	        return getInstance();
	    }
   }
   ```
   
9. **Code Wrapping** 
    - There is no formal line limit at this time, though keeping it between 100 and 120 is preferred.
    - Soft-wrapping is preferred over manual wrapping: try not to hard wrap at less than 100 characters.
    - That being said, make your lines *readable*. If your arguments are extra long, consider manually wrapping them
      like any other method or statement to improve readability.
      
10. **Linting and Proactive Bug Smashing**
    - Plugins and tools like [CheckStyle][checkstyle], [SonarLint][sonarlint], and [SpotBugs][spotbugs] are highly
      beneficial, and can help you improve the readability of your code as well as detect bugs, code smells, and
      vulnerabilities.
      - We currently don't implement any CheckStyle style guidelines, but it is under consideration.
    - We may implement automation of these in the future, similarly to our implementation of automated EditorConfig
      verification.
      
11. **Comments**
    - Avoid using inappropriate Javadoc comments
        ```java
        /**
         * ⛔ Don't do this, unless you are writing Javadocs.
         */
        ```
    - Avoid superfluous comments. Let your code be self-documenting.
    - See a "walking" or otherwise inaccurate comment? Nuke it or modify it. A bad source of documentation hurts
      everyone.
      
12. **Refactoring**
    - Do not be afraid of refactoring code. Ownership of clean code is responsibility of the whole team.
    - If you have trouble reading a method, others likely have the same issue. Extract methods as required to improve
      high level readability, and keep your own methods as small as you can.
    - Scout's rule: Always leave the repository cleaner than you found it.
    
[notepad-lf]: https://devblogs.microsoft.com/commandline/extended-eol-in-notepad/
    "Introducing extended line endings support in Notepad | Windows Commandline DevBlog"
[checkstyle]: https://checkstyle.sourceforge.io/ "CheckStyle | Automated Code Conventions"
[sonarlint]: https://www.sonarlint.org/ "SonarLint | Fix issues before they exist"
[spotbugs]: https://spotbugs.github.io/ "SpotBugs | Spiritual Successor of Findbugs | A Static Code Analysis Tool"

## Dependency Inclusion

For the sanity of the rest of us, please adhere to the following guidelines when attempting to
include a dependency or 3rd-party library.

1. Libraries need to be approved by the current maintainer before being merged.
   If you are adding a dependency, make your case for it in your PR, or in a separate ticket.

2. Prevent ClassNotFound errors - Do not reference a plugin in a loaded class, unless it is enabled.

3. No "Black Box" or Obfuscated dependencies.
   - All dependencies should have their source open and auditable.

4. Libraries and code snippets need to be of a compatible license with ours.
   - Do not violate either project's license in implementing your changes.
   - 3rd Party code without a license will be denied outright under the understanding that the original author is
     reserving all rights. Please ask them to offer an open source software license, and to grant license to any patents
     as required.

5. Any additional libraries or files under copyright of someone else need to be added to the
   `NOTICE` file. Please list the author, the copyright, the license, and path to the file(s) if it
   is not imported through maven.
   
----

Copyright © 2021 Towny Advanced.  

For the purpose of providing open documentation, this file has been licensed under a
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/">
    Creative Commons Attribution 4.0 International License
</a>.
<br/>
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/">
    <img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" />
</a>
