# Custom Recipes

![bStats Servers](https://img.shields.io/bstats/servers/27998?refresh=true)
![bStats Players](https://img.shields.io/bstats/players/27998?refresh=true)
![Spiget Downloads](https://img.shields.io/spiget/downloads/130198?refresh=true)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21+-green)

Custom Recipes allows you to create custom recipes for wide selection of workstations including:
Crafting Table (Others in Future Updates)

Integrates with other plugins:
(In Future)

In addition, you can switch vanilla recipes at your discretion, as well as disable and override them (In Future)

## Support
For any questions or support join the [Discord](https://discord.gg/ChTjgTqw3T)

![Discord](https://img.shields.io/discord/1381214323220938924?color=5865F2&label=Discord&logo=discord&logoColor=white&style=for-the-badge&v=2)
---

## Features

### Recipe Types
- **Shaped Recipes** - Create custom 3x3 crafting grid patterns with exact positions
- **Shapeless Recipes** - Position-independent recipes (any arrangement works)
- **Bulk Crafting** - Configure recipes to produce multiple items (1-64)
- **Hidden Recipes** - Hide from recipe viewer mods until player discovers them
  
### Recipe Management
- **Recipe Creator GUI** - Create recipes in-game with drag-and-drop interface
- **Recipe Editor GUI** - Modify existing recipes visually
- **Item Editor** - Customize result items with:
  - GUI Name & Description (shown in recipe browser)
  - Crafted Name & Description (shown on crafted item)
  - CustomModelData support for resource packs
  - NBT data and custom tags
  - Enchantments with custom levels
- **Per-World Restrictions** - Enable/disable recipes per world (Overworld, Nether, End)
- **Hot Reload** - Reload recipes without restarting the server

### User Experience
- **Recipe Browser GUI** - View all recipes in an interactive interface
- **Recipe Viewer** - Inspect individual recipe patterns visually
- **Automatic Pagination** - Handles large recipe collections
- **MiniMessage Support** - Rich text formatting with gradients and colors
- **Permission System** - Granular permission control
- **Update Notifications** - Automatic alerts for new versions (for admins)

---

## Usage

### Commands

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/customrecipes help` | `/cr help` | Show help message | `customrecipes.use` |
| `/customrecipes reload` | `/cr reload`, `/cr rl` | Reload configuration | `customrecipes.reload` |
| `/customrecipes list` | `/cr list`, `/cr l` | List all recipes | `customrecipes.list` |
| `/customrecipes gui` | `/cr gui`, `/cr menu` | Open recipe GUI | `customrecipes.gui` |
| `/customrecipes enable <recipe>` | `/cr enable` | Enable a recipe | `customrecipes.manage` |
| `/customrecipes disable <recipe>` | `/cr disable` | Disable a recipe | `customrecipes.manage` |
| `/customrecipes delete <recipe>` | `/cr delete` | Delete a recipe | `customrecipes.manage` |

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `customrecipes.*` | All permissions | op |
| `customrecipes.use` | Basic command access | true |
| `customrecipes.reload` | Reload configuration | op |
| `customrecipes.list` | View recipe list | true |
| `customrecipes.gui` | Open GUI interface | true |
| `customrecipes.manage` | Manage recipes (enable/disable/delete) | op |
| `customrecipes.update.notify` | Receive update notifications | op |

---

## ⚙️ Configuration

### Basic Structure

```yaml
# Enable debug logging
debug: false

# Update checker settings
update-checker:
  enabled: true
  source: GITHUB
  github-repo: "hikarii-dev/Custom-Recipes"

# Use custom names and descriptions on crafted items
use-crafted-custom-names: true

# Keep custom names on spawn eggs when they spawn mobs
spawn-egg-keep-custom-name: false

# Ignore metadata/NBT when checking recipe ingredients
ignore-metadata: false

# World restrictions for recipes
world-restrictions:
  enabled: false
  disabled-worlds: []

# Recipe Format:
<RecipeKey>:
  gui-name: "Name in GUI"           # Always shown in recipe browser GUI
  gui-description:                  # Always shown in recipe browser GUI
    - "Line 1"
  crafted-name: "Name on item"      # (Optional) Name on crafted item
  crafted-description:              # (Optional) Lore on crafted item
    - "Line 1"
  type: SHAPED                      # SHAPED or SHAPELESS
  recipe:                           # For SHAPED recipes
    - ITEM ITEM ITEM
    - ITEM ITEM ITEM
    - ITEM ITEM ITEM
  ingredients:                      # For SHAPELESS recipes
    - MATERIAL:COUNT
  material: RESULT_ITEM
  amount: 1
  hidden: false                     # Hide from recipe mods until discovered
```

### Example Recipes

#### Spawn Egg Recipe
```yaml
BeeSpawnEgg:
  gui-name: "<yellow>Bee Spawn Egg"
  gui-description:
    - "<gray>Craft using honey bottles"
    - "<gold>Place to spawn a bee!"
  crafted-name: "<yellow>Bee Spawn Egg"
  crafted-description:
    - "<gray>Craft using honey bottles"
    - "<gold>Place to spawn a bee!"
  recipe:
    - HONEY_BOTTLE HONEY_BOTTLE HONEY_BOTTLE
    - HONEY_BOTTLE CLOCK HONEY_BOTTLE
    - HONEY_BOTTLE HONEY_BOTTLE HONEY_BOTTLE
  material: BEE_SPAWN_EGG
  amount: 1
```

#### Bulk Crafting Recipe
```yaml
GoldenAppleStack:
  gui-name: "<gold>Golden Apple Stack"
  gui-description:
    - "<gray>Bulk golden apple crafting"
    - "<yellow>Creates 8 golden apples"
  recipe:
    - GOLD_BLOCK GOLD_BLOCK GOLD_BLOCK
    - GOLD_BLOCK APPLE GOLD_BLOCK
    - GOLD_BLOCK GOLD_BLOCK GOLD_BLOCK
  material: GOLDEN_APPLE
  amount: 8
```

### Color Codes

This plugin supports **MiniMessage** format for text coloring:

- `<red>`, `<green>`, `<blue>`, etc. - Named colors
- `<#FF0000>` - Hex colors
- `<gradient:#FF0000:#00FF00>Text</gradient>` - Gradients
- `<rainbow>Text</rainbow>` - Rainbow effect
- `<bold>`, `<italic>`, `<underline>` - Formatting

[See MiniMessage Documentation](https://docs.advntr.dev/minimessage/format.html) for more options.

---

## Roadmap

### Completed
- [✨] **Shapeless Recipes** - Support for recipes without specific patterns
- [✨] **Recipe Editor GUI** - In-game recipe creation interface
- [✨] **Recipe Export/Import** - Recipes saved as individual files
- [✨] **Per-World Recipes** - Enable/disable recipes per world
- [✨] **Hidden Recipes** - Discoverable recipes system

### Planned Features
- [ ] **Furnace Recipes** - Custom smelting recipes
- [ ] **Brewing Recipes** - Custom potion recipes
- [ ] **Smithing Recipes** - Custom smithing table recipes
- [ ] **MySQL Support** - Store recipes in database
- [ ] **Per-Player Recipes** - Unlock recipes per player
- [ ] **Recipe Groups** - Organize recipes into categories
- [ ] **Recipe Dependencies** - Require other recipes first

---

## Installation

1. **Download** the latest `CustomRecipes-1.1.2.jar` from releases
2. **Place** the jar file in your server's `plugins` folder
3. **Start** your server to generate the default configuration
4. **Edit** `plugins/CustomRecipes/config.yml` to add your custom recipes or use `/customrecipes gui` with GUI-Create system.
5. **Reload** using `/cr reload` or restart your server

### Compatibility
- **Minecraft Version**: 1.21+
- **Server Software**: Paper/Spigot/Purpur (or compatible fork)
- **Java Version**: Java 21+

---

## Statistics
[![](https://bstats.org/signatures/bukkit/Custom%20Recipes.svg?refresh=true)](https://bstats.org/plugin/bukkit/CustomCrafting/27998)

---

**Made with ❤️ for the Minecraft community**
