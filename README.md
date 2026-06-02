# CarryOnRevamped

A multiplatform fork of the classic **Carry On** mod for Minecraft 1.21.1, focused on networking robustness, modern mod compatibility, and general bug fixes.

CarryOnRevamped allows players to pick up, carry, and place down block entities (such as chests, furnaces, or spawners) and living mobs with their bare hands. It preserves all inventory, NBT data, and block states, allowing you to reorganise your base or transport animals easily without breaking them down.

---

## How to Use

* **Pick Up:** Press the **Carry Key** (which is **unbound by default** in your controls settings and must be assigned first) + Right Click with empty hands on any allowed block or entity.
* **Place Down:** Right Click on the ground or a surface.

---

## Core Features (Original Mod)

* **Block Entity Transport:** Pick up chests, furnaces, droppers, hoppers, spawners, and other block entities while retaining their full inventories and NBT data.
* **Entity Carrying:** Pick up and transport passive animals, hostile mobs, villagers, or even other players.
* **Gameplay Balancing:** Carrying heavy objects or mobs applies custom slowness, increased hunger, or other configurable effects based on the weight of the carried object.
* **Empty-Hand Requirement:** To ensure balanced gameplay, objects can only be picked up when both of the player's hands are empty.
* **Configurable Restrictions:** Fully customisable whitelists and blacklists to control exactly which blocks or entities can be picked up.

---

## Enhancements in CarryOnRevamped

This fork introduces critical stabilisations, gameplay improvements, and safety features for modern modded servers and modpacks:

### 1. Interaction & Safety Improvements

* **Carry Key Requirement:** Standardises pickup behaviour and resolves the original mod's issue where holding the Carry Key would still open a container's interface (such as chests, furnaces, and crafting tables) or trigger trading menus instead of actually picking them up. Requiring the Carry Key to be held down now successfully blocks these default interactions, ensuring consistent and reliable pickups.
* **Placement Cooldowns:** Prevents players from instantly re-picking up a player, mob, or block entity immediately after putting them down, avoiding annoying recursive pick-up/place loops if interaction keys are held.
* **Cycle-Safe Loop Guards:** Implements strict cyclic safety checks and loop guards to prevent recursive player passenger stack loops, avoiding instant stack overflows and server freezes.
* **Carried Data Recovery:** Hardens the mod's recovery systems to safely restore carried data and prevent crashes or item losses on world reload.
* **Carried Player Release Sync:** Centralises carried-player release handling so being put down or shift-dismounting clears the carrier state, passenger state, slowness effect, and multiplayer riding sync consistently without duplicate release packets.
* **Portal Carry Recovery:** Tracks carried player UUIDs across dimension changes and reattaches carried players after Nether portal travel when both players arrive in the destination dimension, while clearing stale invisible carry states if recovery fails.
* **Slowness Debuff Fixes:** Resolves persistent slowness debuff bugs that originally occurred during player dismounting, stacking, or manually clearing carried states.
* **Non-OP Recovery Command:** Allows non-operator players to execute the `/carryon place` command on themselves to safely place down a carried object if their state gets stuck.

### 2. Modpack Compatibility & Crash Prevention

* **Back-Items Safety Guards:** Adds support for a customisable `backItems` list (supporting wildcards and item tags) to natively recognise modded backpacks (e.g. `fxntstorage`, `create_jetpack`, and backtanks), preventing rendering conflicts or passenger loops, and blocking players from picking backpacks directly off another player's back.
* **Unsafe Placement Targets:** Adds configurable safety checks to block carrying blocks or entities onto dangerous placement targets (such as moving Create mod contraptions) that originally caused instant crashes.
* **Default Blacklists:** Ships with comprehensive out-of-the-box mod blacklists to ensure compatibility with major tech and magic mods.

### 3. Robust S2C Network Serialization & Null-Safety

* **The Original Mod Issue:** The original mod frequently crashed during network updates due to a lack of null-safety, inverted player-tracking mappings, and unhandled exceptions during capability serialization. Simple actions like picking up objects, placing, dismounting, or player tracking updates would throw `NullPointerException` or `ClassCastException` and crash the server or client. Furthermore, synchronising carried container blocks containing complex custom NBT data frequently exceeded standard network packet limits, throwing `EncoderException` and disconnecting players.
* **Our Solution:** Overhauled the network serialization, capability, and tracking layers to implement strict null-safety and cast guards, alongside Minecraft's native registry-free **`ByteBufCodecs.TRUSTED_COMPOUND_TAG`**.
  * Adds comprehensive null-safety checks during player tracking and capability serialization to eliminate persistent NPEs and cast exceptions.
  * Snapshots carried data before custom payload encoding and platform storage to prevent release-time state mutation from corrupting multiplayer sync packets.
  * Bypasses standard NBT size limit constraints (such as the default 2MB heap allocation tracker), allowing massive custom container payloads to synchronise safely in multiplayer.
  * Natively utilises Minecraft's network-level packet compression (Zlib), ensuring 100% thread safety, lightweight packet processing, and no event-loop freezes or memory leaks.

---

## Configuration

Configuration files are automatically generated under the `config/` directory:

* `config/carryon-common.toml` (Main gameplay settings, balancing parameters, blacklists, and whitelists)
* `config/carryon-client.toml` (Client-side rendering offsets and HUD positioning)

---

## Developer Setup (Multi-Loader Gradle)

This project compiles mods for **Fabric**, **Forge**, and **NeoForge** using a multi-project Gradle setup sharing a `Common` Java sourceset.

### Prerequisites

* **JDK 21** (Required for Minecraft 1.21.1 compilation)
* An IDE like **IntelliJ IDEA** (Recommended)

### Importing the Project

1. Clone this repository to your local machine:

   ```bash
   git clone https://github.com/TeamMangoMilk/CarryOnRevamped.git
   ```

2. Open the root folder in IntelliJ IDEA.
3. Verify your Gradle JVM is set to JDK 21 under `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`.
4. Open the Gradle tab on the right side of IntelliJ and run:
   * `Common > Tasks > vanilla gradle > decompile` (to decompile Minecraft).
   * `Forge > Tasks > forgegradle runs > genIntellijRuns` (to generate Forge runs).
5. You can now launch and debug the Fabric, Forge, or NeoForge client directly from your IDE's Run/Debug Configurations.

---

## Building Mod JARs

To compile and package release-ready JARs for all loaders:

1. Open your terminal in the root project folder.
2. Run the Gradle build command:

   ```powershell
   # Windows PowerShell
   $env:JAVA_HOME="C:\Path\To\Your\JDK-21"
   .\gradlew.bat clean build
   ```

3. The newly generated, version-stamped binaries will be packaged and stored in the central `build_jars/` folder:
   * `build_jars/carryon-revamped-fabric-1.21.1-[version].jar`
   * `build_jars/carryon-revamped-forge-1.21.1-[version].jar`
   * `build_jars/carryon-revamped-neoforge-1.21.1-[version].jar`

---

## Modpack Policy

You are free to include CarryOnRevamped in any public or private modpack under the terms of the LGPLv3 license. No explicit permission is required.

---

## Known Issues

* None! If you encounter a bug or crash, please open a detailed issue report on our repository.

---

## Planned Features

* **CarryOnExtended Integration:** Look into natively integrating *CarryOnExtended* features, such as the ability to throw carried blocks and entities.
* **Player Pickup Toggle:** Add a player-specific setting allowing individuals to enable or disable whether other players are allowed to pick them up.
* **Upstream Backports:** Investigate porting these networking, capability, and loop safety fixes to newer CarryOn and Minecraft versions as they release, keeping the primary focus on 1.21.1 for now.

---

## License & Credits

This project is licensed under the **GNU Lesser General Public License v3** (LGPLv3).

* **Original Creators:** Tschipp and Purplicious_Cow
* **Maintained & Revamped by:** TeamMangoMilk
