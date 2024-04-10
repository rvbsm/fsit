# FSit (Fabric)

A _server-side_ mod that allows players to sit anywhere!

_Inspired by [GSit][gsit]._

![2024-04-10_20 17 24](https://github.com/rvbsm/fsit/assets/39232658/b500c9d5-7f50-4afd-b293-85f2772d393c)

![2024-04-10_20 08 41](https://github.com/rvbsm/fsit/assets/39232658/a5f2914f-afaa-4c4e-8870-7ada65fa9cdd)

![2023-09-10_14 03 01](https://github.com/rvbsm/fsit/assets/39232658/344aba3c-d7f1-40d7-a9bb-500ccacfdfcb)

## Usage

* Interact with blocks like stairs, slabs, and horizontal logs
* Hop on and ride other players!
* `/sit` and `/crawl` commands for sitting and crawling actions

### Client

* Manageable riders through the Social Interactions screen (1.20.2+)
* Keybindings

![2024-04-10_21 53 42](https://github.com/rvbsm/fsit/assets/39232658/5c9187d3-1faf-4f94-868c-887bc40a1761)

![2024-04-10_21 54 32](https://github.com/rvbsm/fsit/assets/39232658/cfa90e8e-4790-4030-8ae0-36d5ed3d9354)

### Configuration (`config/fsit.yaml`)

If mod is installed on both the server and client,
player configuration will be automatically synced (only if `use_server` is `false`).

The config can be modified with [Mod Menu][modmenu] and [YetAnotherConfigLib][yacl] installed.

```yaml
use_server: false
sitting:
  seats_gravity: true
  on_use:
    enabled: true
    range: 2
    blocks:
      - "#slabs"
      - "#stairs"
      - "#logs"
  on_double_sneak:
    enabled: false
    min_pitch: 66.6
    delay: 600
riding:
  on_use:
    enabled: true
    range: 3
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
