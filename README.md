# FSit (Fabric)

A _server-side_ mod that allows players to sit anywhere!

_Partially [GSit][gsit] rework for Fabric._

__NOTE:__

Player Riding:
___By default, when a player rides another player, his hit-box is blocking player's view.
Install this mod on the client to prevent sight blocking.___

## Usage

* To sit or crawl, you need to sneak twice while looking down;
* Use stairs, slabs, or horizontal logs (configurable);
* Ride other players;
* By commands `/sit` and `/crawl`;

### Client

The server will respect players' configuration if this mod is installed on the client.
Additionally,
they can restrict other players from riding them using [Social Interactions screen][social-interactions].

For other players, server configuration will be used.

### Configuration (`config/fsit.toml`)

The config can be modified using [Mod Menu][modmenu] and [Cloth Config API][cloth-config] installed.

Use `/fsit get <key>` and `/fsit set <key> <value` to access or modify config fields.
Reload from the file using `/fsit reload`.

```toml
config_version = 4

[sneak]
enabled = true
angle = 66.0
delay = 600

[sittable]
enabled = true
radius = 2
blocks = []
tags = ["minecraft:slabs", "minecraft:stairs", "minecraft:logs"]

[riding]
enabled = true
radius = 3
```

## Contributing

Pull requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the [MIT License][license].

[license]: ./LICENSE

[gsit]: https://github.com/Gecolay/GSit

[social-interactions]: https://minecraft.wiki/w/Social_interactions

[modmenu]: [https://modrinth.com/mod/modmenu]
[cloth-config]: [https://modrinth.com/mod/cloth-config]
