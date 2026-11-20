Azure - Fabric client mod for Minecraft 26.2

Build
1. Install JDK 25 and Gradle 9.x.
2. In this folder run: gradle wrapper
3. Run: ./gradlew build
4. Take build/libs/azure-gui-1.0.0.jar and put it, plus Fabric API for 26.2, in your mods folder.

If Gradle can't resolve a version, bump loader_version, loom_version or fabric_api_version in gradle.properties
(latest numbers: https://fabricmc.net/develop). Alternative: copy src/ into the official Fabric example mod for 26.2.

Use
Right Shift opens the menu (rebindable under Controls > Azure). Right Shift again closes it.
Flags are heuristics from chunk data your client already received. Expect false positives near villages and mineshafts.
Only use finders where the server allows them.
