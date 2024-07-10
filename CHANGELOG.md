# Changelog

## v2.5.0

### Changed

- Armor stand markers now used as the seat entities (Geyser compatibility)
- Area effect cloud used as the _seat_ when riding other players to prevent view obstruction
- Dismount rider from you by sneaking (previously by attack)

### Fixed

- Config wasn't synced to the server after saving it in the Mod Menu screen
- Tools could be used on use interactions (shouldn't have tho)

### Removed

- `riding.hide_rider` option as not needed anymore

## v2.4.1

### Fixed

- Config wasn't saved to a file after saving it in the Mod Menu screen

## v2.4.0

Minecraft Tricky Trials Update 

### Fixed

- After dismounting a player, equipment was desynced for other clients

## v2.3.4

### Fixed

- Non-modded clients couldn't ride other players
- Minecraft client crashes during launch if `fsit.restrictions.json` doesn't exist

## v2.3.3

### Fixed

- Minecraft client crashes during launch

## v2.3.2 [yanked]

### Fixed

- Riding didn't work
- “Allow” button on the Social Interactions screen didn't work
- Incorrectly calculated seat velocity when yaw and movement direction differed
- Some migrated config options were missing

## v2.3.1

### Fixed

- Minecraft crash in 1.20.6
- Server crash because `fabric-key-binding-api-v1` is not available
- Double sneak didn't work

## v2.3.0

### Added

- Migrate v1 config `sneak` category
- Block slipperiness affects seats (if `sitting.apply_gravity`)
- Fluids affect seats (if `sitting.apply_gravity`)

### Changed

- Blocks and Tags options were merged in Mod Menu too

### Fixed

- The server crashed when player has non-player passenger
- Opening screens with toggled pose key on Hybrid mode reset it

### Removed

- Logs about migrated config options

## v2.2.0

### New Contributors

- @TheWhiteDog9487 made their first contribution in https://github.com/rvbsm/fsit/pull/35

### Added

- Simplified Chinese translation by @TheWhiteDog9487 at https://github.com/rvbsm/fsit/pull/35
- Support Minecraft 1.20.5
- Try to start riding on passed uses (totems in offhand, food, etc.)
- Start crawling by double sneaking near a one-block gap
- Preventing from sitting in places where players would suffocate
- Discard seat if `sitting.apply_gravity` and `sitting.allow_in_air` are disabled
- New config options:
  - `sitting.allow_in_air`
  - `riding.hide_rider`
  - `on_use.check_suffocation`
  - `on_double_sneak.crawling`
- New config command options:
  - `allowSittingInAir <bool>`
  - `hideRider <bool>`
  - `onUseCheckSuffocation <bool>`
  - `onSneakCrawl <bool>`
- Add config comments
- Use Machete Gradle plugin to minify JAR size

### Changed

- Rename `/fsit <allow|restrict>` to `/fsit:client <allow|restrict>`
- Rename config options:
  - `sitting.on_use` + `riding.on_use` → `on_use`
  - `sitting.on_double_sneak` → `on_double_sneak`
- Rename config command names:
  - `sitOnUse` → `onUseSit`
  - `rideOnUse` → `onUseRide`
  - `sitOnUseRange` + `rideOnUseRange` → `onUseRange`
  - `sitOnSneak` → `onSneakSit`
  - `sitOnSneakMinPitch` → `onSneakMinPitch`
  - `sitOnSneakDelay` → `onSneakDelay`
- Categorize Mod Menu screen
- Bump `fabric-commands-api` to v2

### Fixed

- Fake player interactions on player caused crash #34
- Client and server main command clash
- `/fsit reload` could be called by anyone
- Wrong config option name feedback
- Version semantics

### Removed

- `fabric-events-interaction-v0` dependency

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v2.1.0...v2.2.0

## v2.1.0

### Added

- Start sitting on double sneak
- Config migrator (for v1+ JSON and v2 YAML)
- Optionally apply gravity to seats (`sitting.seats_gravity`)
- Config command options:
  - `seatsGravity <bool>`
  - `sitOnSneak <bool>`
  - `sitOnSneakMinPitch <float>`
  - `sitOnSneakDelay <integer>`

### Changed

- Try to sit on passed uses (item wouldn't or can't be used)
- Rename config command names:
  - `sittableEnabled` → `sitOnUse`
  - `sittableRadius` → `sitOnUseRange`
  - `ridingEnabled` → `rideOnUse`
  - `ridingRadius` → `rideOnUseRange`

### Fixed

- After respawning, player config was lost
- Player's pose could be changed in the vehicle
- Player would swim in the air while crawling
- Clipping through a floor when using sit keybinding on servers

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v2.0.1...v2.1.0

## v2.0.1

### Added

- Restriction buttons in 1.20-1.20.1
- Check if connected server is compatible with FSit

### Changed

- Don't allow radius equal zero
- `/allow` and `/restrict` to `/fsit <allow|restrict>`

### Fixed

- Player stops crawling in places where he can start crouching
- Restrict buttons offset in 1.20.4

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v2.0.0...v2.0.1

## v2.0.0

**BREAKING CHANGE: Fabric Language Kotlin is now a required dependency**

### New Contributors

- @JustAlittleWolf made their first contribution in https://github.com/rvbsm/fsit/pull/30

### Added

- Optional configuration syncing (`use_server`)
- Seats now have gravity
- Seat entities are _actually_ no longer saved in the world
- Punch a rider to dismount him
- 1.20-1.20.4 compatibility (want to believe)
- Sit and crawl keybindings
- `/restrict` and `/allow` commands

### Changed

- Config now uses YAML
- Hide the rider for the player if he isn't looking at him #31
- `/fsit config`, `/fsit get`, and `/fsit set` were replaced by `/fsit <key> [value]`
- Cloth Config was replaced with YetAnotherConfigLib
- `/sit`, `/crawl` are also available in single-player worlds #32
- Player restrictions are handled by the client and no longer synced to the server

### Removed

- Sit by double sneaking

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.5.1-1.20.2...v2.0.0

## v1.5.1

### Added

- `/fsit config` command to show server config

### Removed

- `/fsit get` and `/fsit set` commands autocompletion #27

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.5.0-1.20.2...v1.5.1-1.20.2

## v1.5.0

### Added

- `/fsit get` and `/fsit set` commands to edit config options
- Discard empty seats if they exist after world reload

### Fixed

- The server crashed when the player tried to ride himself (e.g., free cam) #25

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.4.2...v1.5.0-1.20.2

## v1.4.2

### Fixed

- Was unable to dismount player if `sneak.enabled` is disabled

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.4.1...v1.4.2

## v1.4.1

### Fixed

- Client config wasn't sent to server :grin:

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.4.0...v1.4.1

## v1.4.0

**1.20.2 update**

**~~May~~ Should not be compatible with previous versions. Both server and client version must be the same.**

_P.S. Thanks for 2K downloads at Modrinth_ :heart:

### Added

- Support Minecraft 1.20.2
- Config migration

### Changed

- Keep players' riding restrictions in their server player-data
- Move to `toml4j`

### Removed

- `riding.height` was removed due to the correct position of players' passengers in Minecraft 1.20.2 :tada:

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.3.2...v1.4.0

## v1.3.2

### Fixed

- Config manager had been resetting some fields

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.3.1...v1.3.2

## v1.3.1

### Added

- Adjustable height between player head and a rider
- Stop crawling if a player starts flying
- Reset pose on pose commands (`/sit`, `/crawl`)
- Looking at the rider and sneaking dismounts him

### Fixed

- Game crashes without Mod Menu installed
- Player stops crawling if a block above is non-full #20
- Doubled messages after pose changing
- Crawling support entity didn't disappear
- Crawling with Speed II and Swift Sneak III
- Config was sent to the server even if the player wasn't connected to one

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.3.0...v1.3.1

## v1.3.0

_FSit-client is now a part of the FSit_

### Added

- Start crawling with `/crawl` or sneak twice looking down while already crawling
- Adjusting the radius to start riding
- Sitting on honey blocks works like standing on honey
- Send an overlay message to player on pose change
- Reload config with `/fsit reload`
- The server will respect players' configuration if they have a mod installed
- Player riding restrictions at Social Interactions screen

### Fixed

- The player immediately stopped sitting on the edge of a block
- Random crashes with invalid config

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.2.0...v1.3.0

## v1.2.0

### Added

- Support Minecraft 1.20

### Changed

- [revert] Do not start sitting by interaction if player is sneaking

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.1.3...v1.2.0

## v1.1.3

### Fixed

- The rider's position wasn't updated if the player didn't have a client-side mod installed

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.1.2...v1.1.3

## v1.1.2

### Fixed

- Interaction with entity swung hand all the times

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.1.1...v1.1.2

## v1.1.1

### Fixed

- Blocks weren't placed

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.1.0...v1.1.1

## v1.1.0

**WARNING: configuration will be reset to default after the first launch**

### Added

- Config version field
- Make sit on double sneak optional (`sneak_sit`)
- Swing hand on successful sit and ride interactions

### Changed

- Clear config without deleting config file
- Rename config options:
  - `min_angle` → `sneak.min_angle`
  - `shift_delay` → `sneak.sneak_delay`
  - `sittable_blocks` → `sittable.blocks`
  - `sittable_tags` → `sittable.tags`
  - `sit_on_players` → `misc.sit_players`

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.1...v1.1.0

## v1.0.1

### Added

- Start riding only if the main hand is empty
- Reduce rider's dimensions height
- Fake parent in Mod Menu for client-side mod

### Fixed

- Interaction with entity when player's pitch is greater than `min_angle` sits the player

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0...v1.0.1

## v1.0.0

### Added

- Create a client-side mod that fixes rider's position
- Include Fabric API modules to JAR

### Changed

- Make `/sit` server-only
- Try to ride, even if a player has a vehicle

### Fixed

- `/sit` didn't reset player's pose

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.8...v1.0.0

## v1.0.0-beta.8

another beta

### Added

- `/sit` command
- Dismount rider if player changes game-mode to spectator

### Fixed

- The player's position didn't change for other clients when he started riding another player

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.7...v1.0.0-beta.8

## v1.0.0-beta.7

### Added

- Player riding (disabled in config by default)

### Fixed

- Do not allow sitting in spectator mode

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.6...v1.0.0-beta.7

## v1.0.0-beta.6

### Added

- Add default options to be able to reset config

### Changed

- Use `night-config` instead of `toml4j`

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.5...v1.0.0-beta.6

## v1.0.0-beta.5

### Added

- Check if block is in sittable position
- Use Cloth Config for Mod Menu integration
- Include `toml4j`

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.4...v1.0.0-beta.5

## v1.0.0-beta.4

### Added

- Configuration file
- [WIP] Mod Menu integration (1.19.4 only)

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.3...v1.0.0-beta.4

## v1.0.0-beta.3

### Added

- Sit on horizontally placed logs by interacting with them

### Changes

- Start sitting by interacting with a block only if the player is sneaking

### Fixes

- A bucket of water was used, and the seat was called in while interacting with the water-loggable block

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.2...v1.0.0-beta.3

## v1.0.0-beta.2

### Added

- Don't allow sitting on occupied by another player block

### Changes

- Less delay between sneaks (1s → 600ms)

### Fixes

- Vehicle dismounting was counted as a sneak

**Full Changelog**: https://github.com/rvbsm/fsit/compare/v1.0.0-beta.1...v1.0.0-beta.2

## v1.0.0-beta.1

Initial release

### Added

- Sit on slabs or stairs
- Sit on double sneak

**Full Changelog**: https://github.com/rvbsm/fsit/commits/v1.0.0-beta.1
