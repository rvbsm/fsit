# Changelog

## v2.2.0

### New Contributors

- @TheWhiteDog9487 made their first contribution in https://github.com/rvbsm/fsit/pull/35

### Added

- Simplified Chinese translation by @TheWhiteDog9487 at https://github.com/rvbsm/fsit/pull/35
- Support Minecraft 1.20.5
- Try to start riding on passed uses (totems in offhand, food, etc.)
- Start crawling by double sneaking near a one-block gap
- Preventing from sitting in places where player would suffocate
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
