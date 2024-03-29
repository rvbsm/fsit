# FSit (Fabric)

A _server-side_ mod that allows players to sit anywhere!

_Inspired by [GSit][gsit]._

## Usage

* Interact with blocks like stairs, slabs, and horizontal logs
* Hop on and ride other players!
* `/sit` and `/crawl` commands for sitting and crawling actions

### Client

* Manageable riders through the Social Interactions screen (1.20.2+)
* Keybindings

### Configuration (`config/fsit.yaml`)

If mod is installed on both the server and client,
player configuration will be automatically synced (only if `use_server` is `false`).

The config can be modified with [Mod Menu][modmenu] and [YetAnotherConfigLib][yacl] installed.

```yaml
use_server: false
sittable:
  enabled: true
  radius: 2
  materials:
    - "#minecraft:slabs"
    - "#minecraft:stairs"
    - "#minecraft:logs"
riding:
  enabled: true
  radius: 3
```

## Contributing

Pull requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the [MIT License](./LICENSE).

[gsit]: https://github.com/Gecolay/GSit

[social-interactions]: https://minecraft.wiki/w/Social_interactions

[modmenu]: [https://modrinth.com/mod/modmenu]
[yacl]: [https://modrinth.com/mod/yacl]
