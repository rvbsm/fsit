# FSit (Fabric)

A server-side mod that allows players to sit anywhere.

__NOTE:__
- `sit_on_players`: ___by default, when a player sits on a player, it blocks the field of view. To prevent this, install client-side mod__
  (check additional files; bundled in full
  version)._

## Usage

- Sneak twice to sit down;  
- `/sit` command;  
- Right-click on stairs, slabs or horizontal logs (configurable).

### Configuration (stored in `config/fsit.toml`)

```toml
[sittable]
sittable_blocks = [] # List of block ids (e.g. "oak_log") available to sit.
sittable_tags = ["minecraft:slabs", "stairs", "logs"] # List of block tags.

[sneak]
sneak_sit = true # Toggles sit by sneak feature.
min_angle = 66.0 # degrees. Minimal pitch to sitting down.
shift_delay = 600 # milliseconds. Time between sneaks for sitting down.

[misc]
sit_on_players = true # Toggles sitting on other players
```

## Contributing

Pull requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the [MIT License][license].

[license]: ./LICENSE
