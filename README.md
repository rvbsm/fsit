# FSit (Fabric)

A _server-side_ mod that allows players to sit anywhere!

_Partially [GSit][gsit] rework for Fabric._

__NOTE:__

Player Riding:
___By default, when a player rides another player, his hit-box is blocking player's view.
Install this mod also on the client to prevent view blocking___

## Usage

* Sneak twice looking down to sit down (or to force crawling while crawling);
* Right-click on stairs, slabs or horizontal logs (lists are configurable);
* Right-click on a player to start riding him (disabled by default);
* `/sit`, `/crawl` commands;

### Client

If the mod is installed on the client, the server will respect the player's configuration.
Additionally,
players will be able to restrict players from riding them using [Social Interactions screen][social-interactions].

For other players without the mod installed, the server configuration will be used.

### Configuration (`config/fsit.toml`)

Can be modified from the client with [Mod Menu][modmenu] and [Cloth Config API][cloth-config] installed.

Reload config on server with `/fsit reload` command.

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

[social-interactions]: https://minecraft.fandom.com/wiki/Social_interactions

[modmenu]: [https://modrinth.com/mod/modmenu]
[cloth-config]: [https://modrinth.com/mod/cloth-config]
