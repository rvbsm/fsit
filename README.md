# FSit (Fabric)

A server-side mod that allows players to sit anywhere.

## Usage

To sit down, look down and sneak twice.
To get up, just sneak once.  
Also, right-click with Shift on stairs, slabs or horizontal logs works.

### Configuration (stored in `config/fsit.toml`)

```toml
sit_on_players = false # Ability to sit on other players
min_angle = 66.0 # degrees. Minimal pitch to sitting down.
shift_delay = 600 # milliseconds. Time between sneaks for sitting down.
sittable_blocks = [] # List of block ids (e.g. "oak_log") available to sit.
sittable_tags = ["minecraft:slabs", "stairs", "logs"] # List of block tags.
```

## Contributing

Pull requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the [MIT License][license].

[license]: ./LICENSE
