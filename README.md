# Custom Recipes

**Create your own custom Crafting, Furnace, Anvil, and other recipes with tons of configuration option with Full GUI Support. You can customize any Vanilla Recipes too.**

![Servers](https://img.shields.io/bstats/servers/27998?refresh=true) ![Players](https://img.shields.io/bstats/players/27998?refresh=true) ![Downloads](https://img.shields.io/spiget/downloads/130198?refresh=true) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green)

## 🔴 You don't need to use configs to create, customize crafts or change settings, full GUI support.

**CustomRecipes** allows you to create, edit, and customize **CUSTOM** and **VANILLA** recipes for wide selection of workstations including:
Crafting Table (**Others in Future Updates**)

Integrates with other plugins:
(**In Future**)

In addition, you can switch vanilla recipes at your discretion, as well as disable and override them (**I made it, and now it is available to you**)

## 🔵 Support

For any questions or support join the [Discord](https://discord.gg/ChTjgTqw3T)

![Discord](https://img.shields.io/discord/1381214323220938924?color=5865F2&label=Discord&logo=discord&logoColor=white&style=for-the-badge)

## 🔵 Features

### 🟠 Recipe Types
- **Vanilla Recipe Editing** - Customize all 801 vanilla Minecraft recipes
- **Multi-Variant Recipes** - Support recipes with multiple ingredient options (different wood types, etc.)
- **Exact Ingredient Matching** - Recipes requiring specific durability, enchantments, or NBT data
- **Shaped Recipes** - Create custom 3x3 crafting grid patterns with exact positions
- **Shapeless Recipes** - Position-independent recipes (any arrangement works)
- **Bulk Crafting** - Configure recipes to produce multiple items (1-64)
- **Hidden Recipes** - Hide from recipe viewer mods until player discovers them

### 🟠 Recipe Management
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
- **Per-World Restrictions** - Enable/disable recipes per world (Overworld, Nether, End)
- **Hot Reload** - Reload recipes without restarting the server

### 🟠 Vanilla Recipe System
- **Vanilla Recipe Editor** - Modify any vanilla Minecraft recipe
- **Category Filtering** - Browse by Building, Decorations, Redstone, Food, Tools, Combat, etc.
- **Recipe Search** - Find recipes by name with partial matching
- **Bulk Management** - Enable/disable all vanilla recipes at once
- **Reset Functionality** - Restore recipes to original vanilla state

### 🟠 User Experience
- **Recipe Browser GUI** - View all recipes in an interactive interface
- **Recipe Viewer** - Inspect individual recipe patterns visually
- **Automatic Pagination** - Handles large recipe collections
- **MiniMessage Support** - Rich text formatting with gradients and colors
- **Permission System** - Granular permission control
- **Update Notifications** - Automatic alerts for new versions (for admins)

## 🔵 Commands

**`/customrecipes list`** - Browse all custom recipes  
Permission: `customrecipes.list`

**`/customrecipes help`** - Show admin help menu  
Permission: `customrecipes.manage (or reload/gui)`

**`/customrecipes reload`** - Reload configuration  
Permission: `customrecipes.reload`

**`/customrecipes gui`** - Open admin GUI menu  
Permission: `customrecipes.gui`

**Alias:** `/cr, /crecipes, /customrecipe`

## 🔵 Permissions

- `customrecipes.*` - All permissions **(op)**
- `customrecipes.list` - Browse custom recipes **(true)** - Available to all players
- `customrecipes.reload` - Reload configuration **(op)**
- `customrecipes.gui` - Open admin GUI interface **(op)**
- `customrecipes.manage` - Create, edit, and delete recipes **(op)**
- `customrecipes.update.notify` - Receive update notifications on join **(op)**

## 🔵 Color Codes

**This plugin supports MiniMessage format for text coloring:**
```
- <red>, <green>, <blue>, etc. - Named colors
- <#FF0000> - Hex colors
- <gradient:#FF0000:#00FF00>Text</gradient> - Gradients
- <rainbow>Text</rainbow> - Rainbow effect
- <bold>, <italic>, <underline> - Formatting
```

**[See MiniMessage Documentation](https://docs.advntr.dev/minimessage/format.html) for more options.**

## 🔵 Roadmap

### 🟢 Completed
- ✨ **Vanilla Recipe List** - List of all vanilla recipes with categories, stations and search system.
- ✨ **Vanilla Recipe Editor** - Any editing of vanilla recipes (except for the result item itself)
- ✨ **Multi-Variant Recipes** - Support recipes with multiple ingredient options (different wood types, etc.)
- ✨ **Exact Ingredient Matching** - Durability, enchantments, and NBT support
- ✨ **Enchant Editor** - Better control over enchanting items
- ✨ **Enchanted Book Crafting** - Full support for stored enchantments

### 🟠 Planned Features
- [ ] **Furnace Recipes** - Custom smelting recipes
- [ ] **Brewing Recipes** - Custom potion recipes
- [ ] **Smithing Recipes** - Custom smithing table recipes
- [ ] **MySQL Support** - Store recipes in database
- [ ] **Per-Player Recipes** - Unlock recipes per player
- [ ] **Recipe Dependencies** - Require other recipes first

## 🔵 Installation

1. **Download** the latest **CustomRecipes-1.5.0.jar** from releases
2. **Place** the jar file in your server's **plugins** folder
3. **Start** your server to generate the default configuration
4. **Use** **/customrecipes gui** or **/cr gui** with GUI-Create system.

## 🔵 Usage

**For Players:**
- Use **/cr** or **/cr list** to browse all available custom recipes
- Click on **any** recipe to view its crafting pattern

**For Administrators:**
- Use **/cr gui** to open the admin management interface
- Create, edit, enable/disable, and delete custom or vanilla recipes
- Configure plugin settings with **GUI**

## 🔵 Compatibility

- **Minecraft Version:** 1.21+
- **Server Software:** Paper/Spigot/Purpur (or compatible fork)
- **Java Version:** Java 21+

---

![bStats](https://bstats.org/signatures/bukkit/Custom%20Recipes.svg?refresh=true)

[![GitHub](https://i.imgur.com/9PtYwt2.png)](https://github.com/hikarii-dev/Custom-Recipes) [![Modrinth](https://i.imgur.com/0JP1E2s.png)](https://modrinth.com/project/hcustomrecipes) [![SpigotMC](https://i.imgur.com/G5QYXXl.png)](https://www.spigotmc.org/resources/custom-recipes.130198)

**Made with ❤️ for the Minecraft community**
