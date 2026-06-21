# Custom Recipes

**Create your own custom Crafting and Furnace recipes with tons of configuration options and Full GUI Support. Customize vanilla recipes, add random results, craft events, and a lot of functions.**

![Servers](https://img.shields.io/bstats/servers/27998?refresh=true) ![Players](https://img.shields.io/bstats/players/27998?refresh=true) ![Downloads](https://img.shields.io/spiget/downloads/130198?refresh=true) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green)

## You don't need to use configs to create, customize crafts or change settings, full GUI support.

**CustomRecipes** allows you to create, edit, and customize **CUSTOM** and **VANILLA** recipes for wide selection of workstations including:
- **Crafting Table** - Shaped and shapeless recipes with full customization
- **Furnace** - Custom smelting recipes with custom fuels and conditions
- **More stations coming soon:** Blast Furnace, Smoker, Brewing Stand, Smithing Table, and more!

Integrates with other plugins:
- **Vault** - Economy integration for recipe costs
- **More integrations coming soon**

In addition, you can switch vanilla recipes at your discretion, as well as disable and override them (**I made it, and now it is available to you**)

## Support

For any questions or support join the [Discord](https://discord.gg/acpkr9EUvQ)

![Discord](https://img.shields.io/discord/1381214323220938924?color=5865F2&label=Discord&logo=discord&logoColor=white&style=for-the-badge)

## Features

### Recipe Types
- **Vanilla Recipe Editing** - Customize all 801 vanilla Minecraft recipes
- **Multi-Variant Recipes** - Support recipes with multiple ingredient options (different wood types, etc.)
- **Exact Ingredient Matching** - Recipes requiring specific durability, enchantments, or NBT data
- **Shaped Recipes** - Create custom 3x3 crafting grid patterns with exact positions
- **Shapeless Recipes** - Position-independent recipes (any arrangement works)
- **Bulk Crafting** - Configure recipes to produce multiple items (1-64)
- **Hidden Recipes** - Hide from recipe viewer mods until player discovers them
- **Random Results** - Weighted outcome system with multiple possible results
- **Craft Events** - Trigger sounds, particles, and commands when recipes are used
- **Failure Chance** - Configure recipes with chance to fail and produce nothing

### Recipe Management
- **Recipe Creator GUI** - Create recipes in-game with drag-and-drop interface
- **Recipe Editor GUI** - Modify existing recipes visually and edit recipe patterns
- **Recipe Pattern Editor** - Click-to-modify ingredient grid for existing recipes
- **Item Editor** - Customize result items with:
  - GUI Name & Description (shown in recipe browser)
  - Crafted Name & Description (shown on crafted item)
  - CustomModelData support for resource packs
  - NBT data and custom tags
  - Enchantments with custom levels via Enchantment Selector GUI
  - Enchantment visibility toggle  
  - Item Flags (HIDE_ENCHANTS, HIDE_ATTRIBUTES, etc.)
  - Variant Switcher for editing each random result independently
- **Per-World Restrictions** - Enable/disable recipes per world (Overworld, Nether, End)
- **Hot Reload** - Reload recipes without restarting the server

### Furnace Recipe System
- **Furnace Recipe Creator** - Create custom smelting recipes in-game
- **Custom Fuel Support** - Use any item as fuel for your recipes
- **Smelting Conditions** - XP requirements, permissions, money costs, cooldowns, and usage limits
- **Experience Rewards** - Grant XP when recipes are smelted
- **Burn Time Configuration** - Adjust cooking time per recipe
- **Random Results** - Furnace recipes support weighted outcomes

### Random Results & Craft Events
- **Random Results GUI** - Configure weighted probability outcomes (up to 27 variants)
- **Failure Chance** - Set 0-100% chance for recipes to fail completely
- **Craft Events** - Execute actions when recipes are crafted or smelted:
  - Play sounds with custom pitch and volume
  - Spawn particle effects with full customization
  - Run console commands with placeholder support
- **Event Presets** - Save and reuse event configurations across recipes

### Multi-Language Support
- **4 Languages Available** - English, Russian, Ukrainian, German
- **Language Selector GUI** - Switch language in-game per player
- **Fully Translated** - All menus, messages, and tooltips in all languages
- **Automatic Fallback** - Missing translations default to English

### Vanilla Recipe System
- **Vanilla Recipe Editor** - Modify any vanilla Minecraft recipe
- **Category Filtering** - Browse by Building, Decorations, Redstone, Food, Tools, Combat, etc.
- **Recipe Search** - Find recipes by name with partial matching
- **Bulk Management** - Enable/disable all vanilla recipes at once
- **Reset Functionality** - Restore recipes to original vanilla state

### User Experience
- **Recipe Browser GUI** - View all recipes in an interactive interface
- **Recipe Viewer** - Inspect individual recipe patterns visually
- **Automatic Pagination** - Handles large recipe collections
- **MiniMessage Support** - Rich text formatting with gradients and colors
- **Permission System** - Granular permission control
- **Update Notifications** - Automatic alerts for new versions (for admins)

## Commands

**`/customrecipes list`** - Browse all custom recipes  
Permission: `customrecipes.list`

**`/customrecipes help`** - Show admin help menu  
Permission: `customrecipes.manage (or reload/gui)`

**`/customrecipes reload`** - Reload configuration  
Permission: `customrecipes.reload`

**`/customrecipes gui`** - Open admin GUI menu  
Permission: `customrecipes.gui`

**Alias:** `/cr, /crecipes, /customrecipe`

## Permissions

- `customrecipes.*` - All permissions **(op)**
- `customrecipes.list` - Browse custom recipes **(true)** - Allows viewing custom recipes list only available/unlocked recipes
- `customrecipes.list` - Browse custom recipes **(op)** - Allows viewing ALL recipes 
- `customrecipes.reload` - Reload configuration **(op)**
- `customrecipes.gui` - Open admin GUI interface **(op)**
- `customrecipes.manage` - Create, edit, and delete recipes **(op)**
- `customrecipes.update.notify` - Receive update notifications on join **(op)**

## Color Codes

**This plugin supports MiniMessage format for text coloring:**
```
- <red>, <green>, <blue>, etc. - Named colors
- <#FF0000> - Hex colors
- <gradient:#FF0000:#00FF00>Text</gradient> - Gradients
- <rainbow>Text</rainbow> - Rainbow effect
- <bold>, <italic>, <underline> - Formatting
```

**[See MiniMessage Documentation](https://docs.advntr.dev/minimessage/format.html) for more options.**

## Roadmap

### Completed
- [✨] **Furnace Recipes** - Complete custom smelting system with conditions and custom fuels
- [✨] **Random Results** - Weighted outcome system with failure chance
- [✨] **Craft Events** - Sounds, particles, and commands on recipe use
- [✨] **Multi-Language** - 4 languages (English, Russian, Ukrainian, German)
- [✨] **Item Flags** - Full ItemFlag support with dedicated GUI

### Planned Features
- [ ] **Blast Furnace Recipes** - Faster ore smelting recipes
- [ ] **Smoker Recipes** - Custom food cooking recipes
- [ ] **Campfire Recipes** - Custom campfire cooking
- [ ] **Brewing Recipes** - Custom potion recipes
- [ ] **Smithing Recipes** - Custom smithing table recipes
- [ ] **MySQL Support** - Store recipes in database
- [ ] **Per-Player Recipes** - Unlock recipes per player
- [ ] **Recipe Dependencies** - Require other recipes first

## Installation

1. **Download** the latest **CustomRecipes-2.0.0.jar** from releases
2. **Place** the jar file in your server's **plugins** folder
3. **Start** your server to generate the default configuration
4. **Use** **/customrecipes gui** or **/cr gui** with GUI-Create system.

## Usage

**For Players:**
- Use **/cr list** to browse all available custom recipes
- Click on **any** recipe to view its crafting pattern

**For Administrators:**
- Use **/cr gui** to open the admin management interface
- Create, edit, enable/disable, and delete custom or vanilla recipes
- Configure plugin settings with **GUI**

## Compatibility

- **Minecraft Version:** 1.18.2 - 1.21.10
- **Server Software:** Paper/Spigot/Purpur (or compatible fork)
- **Java Version:** Java 17+

---

![bStats](https://bstats.org/signatures/bukkit/Custom%20Recipes.svg?refresh=true)

[![GitHub](https://i.imgur.com/9PtYwt2.png)](https://github.com/hikarii-dev/Custom-Recipes) [![Modrinth](https://i.imgur.com/0JP1E2s.png)](https://modrinth.com/project/hcustomrecipes) [![SpigotMC](https://i.imgur.com/G5QYXXl.png)](https://www.spigotmc.org/resources/custom-recipes.130198)

**Made with ❤️ for the Minecraft community**
