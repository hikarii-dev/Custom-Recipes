# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.5.0] MAJOR UPDATE - 2025-11-21

### Added
- **Vanilla Recipe Editor** - Complete vanilla Minecraft recipe customization system
  - 801 vanilla recipes fully integrated (Crafting Table recipes)
  - Recipe pattern modification with preview
  - Support for multi-ingredient variants (e.g. different wood types)
  - Per-variant result customization (e.g. Suspicious Stew effects)
  - Disable/enable individual vanilla recipes
  - Recipe search system with partial name matching
  - Category filtering (Building, Decorations, Redstone, Transportation, Food, Tools, Combat, Brewing, Misc)
  - Station filtering (more stations coming soon)
  - Ingredient variant cycling for recipes with multiple options with visual numbered options
  - Result item amount adjustment (left/right click)
  - Reset to original vanilla recipe functionality

- **Advanced Recipe Pattern Editor**
  - Click-to-modify ingredient grid for shaped recipes
  - Type toggle between Shaped/Shapeless for vanilla recipes
  - Result item amount adjustment (left/right click)
  - Pattern preview
  - Multiple variant support per recipe (up to 10+ variants)

- **Enhanced Item Editor** - Expanded customization options
  - Enchantment Selector GUI - Dedicated menu for enchantment management
    - All enchantments in searchable grid layout
    - Visual level adjustment (left/right click)
    - Max level enforcement per enchantment
    - Quick max/reset with shift-click
    - Enchantment compatibility + filter toggle/enable
    - Enchantment descriptions and effects
    - Visual indicators
    - Enchantment visibility toggle (HIDE_ENCHANTS flag)
  - NBT data editor with key-value pairs
  - Custom Model Data configuration

- **Recipe Pattern Editing for Custom Recipes**
  - Edit existing custom recipe patterns
  - Support for shaped and shapeless pattern changes
  - Ingredient modification via crafting grid
  - Result item amount adjustment (left/right click in edit mode)
  - Save changes without recreating recipe
  - Preserves recipe metadata and settings
  - Visual indicators for edit mode

- **Recipe List Enhancements**
  - Edit Item Button - Quick result editing from list
  - Direct access to item editor from recipe list
  - Modify names, descriptions, enchantments without opening full editor
  - Right-click context menu for quick actions

- **Recipe Editor Improvements**
  - Get Result Item button - Receive configured item + Sound feedback on item receive
  - Items creating with all properties (NBT, enchantments, custom data)

- **Global Recipe Management**
  - Disable All Custom Recipes button in Settings 
  - Enable All Custom Recipes button in Settings
  - Disable All Vanilla Recipes button in Settings (801 recipes)
  - Enable All Vanilla Recipes button in Settings (801 recipes)
  - Recipe count display (enabled/disabled/total)

- **Vanilla Recipe Migration System**
  - Automatic version detection and migration
  - Backup creation before updates
  - Fallback for corrupted data
  - Version tracking

- **Update System Improvements**
  - Periodic background update checks (every 30 minutes)
  - Real-time notifications for admins whos online
  - Automatic notification on new version discovery
  - No server restart needed to detect updates

- **Station Selector GUI** - Menu for choosing crafting station before recipe creation
  - Visual station selection with icons: Crafting Table, Furnace, Blast Furnace, Smoker, Stonecutter, Brewing Stand, Smithing Table, Anvil, Loom
  - "Coming soon" indicators for disabled stations
  - Grid size display per station
  - Glow effect on available stations
  - Station descriptions and status messages

- **Exact Item Matching for Recipe Ingredients** - Advanced crafting requirements
  - Durability/Damage support for tools and armor in recipes
  - Full enchantment matching for weapons and tools as ingredients
  - Stored enchantments support for enchanted books as ingredients
  - NBT/metadata matching in crafting grid
  - CustomModelData matching for resource pack items

### Changed
- **Recipe Editor GUI** - Complete redesign
  - Added "Edit Recipe Pattern" button for custom recipes
  - New edit mode with separate save/cancel buttons
  - Live grid editing with amount adjustment
  - Result item editing moved to dedicated button
  - Toggle system between view/edit modes
  - Better visual feedback for editing state
  - Hidden/Visible recipe toggle

- **Vanilla Recipe Storage**
  - Individual YAML+JSON files per modified vanilla recipe
  - Variant-based pattern storage structure
  - Multi-variant support in vanilla-recipes.yml

- **Recipe List GUI** - Enhanced display
  - Plain text names without formatting artifacts
  - MiniMessage color preservation for custom names
  - Proper name/description extraction from ItemMeta
  - Status indicators (Enabled/Modified/Disabled)
  - Color-coded recipe names (Green/Yellow/Red)
  - Enhanced special properties display (enchantments, NBT, model data)
  - World restrictions shown in lore

- **Settings GUI** - Enhanced organization
  - Dedicated buttons for custom recipe management
  - Dedicated buttons for vanilla recipe management
  - World restriction status properly reflects active restrictions
  - Dimension-based world toggles (Overworld/Nether/End)
  - Visual counters for enabled/disabled recipes
  - Warning messages for bulk operations

- **Recipe Amount System**
  - Multi-ingredient amount support in shaped recipes
  - Visual amount display in recipe grids
  - Proper ingredient consumption for amounts > 1
  - Amount preservation during recipe editing

- **Command System Simplification**
  - Removed /cr list command (use GUI instead)
  - Removed /cr enable command (use GUI instead)
  - Removed /cr disable command (use GUI instead)
  - Removed /cr delete command (use GUI instead)
  - Kept only: /cr gui, /cr reload, /cr help

- **Item Property Handling**
  - ItemFlags preserved across all operations
  - HIDE_ENCHANTS flag properly applied and saved
  - Full ItemMeta cloning in all editors
  - Enchantment visibility controllable per item

## [1.1.2] - 2025-11-17

### Added
- **Per-World Recipe System** - Control recipe availability by dimension
  - Enable/disable recipes per world (Overworld, Nether, End)
  - Default world restrictions in settings GUI
  - Individual world configuration per recipe
  - Visual world selector with dimension icons
  - World list stored in recipe config files
- **Runtime Update Notifications** - Real-time alerts for online admins
  - Background update checking while server is running
  - Automatic notifications when new version is released
  - No server rejoin required to see updates
  - Same notification format as login messages
- **Enhanced Result Item Customization**
  - CustomModelData button in Edit Result Item menu
  - NBT data support button with chat input
  - Add custom tags and metadata to crafted items
  - Full item property configuration with live preview
  - Clear All button to reset customization
  - Right-click to clear and restart descriptions
- **Edit Item Button in Recipe List** - Quick result editing
  - Direct access to item editor from recipe list
  - Modify GUI/Crafted names, descriptions, CustomModelData, NBT, Enchantments
  - Convenient shortcut for result item changes
  - Located in each recipe's list entry

### Changed
- **Recipe Ingredient Matching** - Enhanced metadata support
  - CustomModelData now affects ingredient matching
  - NBT tag comparison for custom items
  - Support for anvil-renamed items in recipes
  - Better item property awareness
- **Recipe Information Display** - Now shows complete item data
  - Displays GUI Name and GUI Description
  - Displays Crafted Name and Crafted Description
  - Shows CustomModelData if present
  - Lists all Enchantments with levels
  - Shows NBT data tags
  - Better organized information layout
- **Item Editor GUI Layout** - Improved organization
  - Separate sections for Crafted and GUI properties
  - Clear labeling of what appears where (crafted item vs recipe browser)
  - Better button descriptions with usage hints
  - Right-click functionality for clearing descriptions

## [1.1.0] - 2025-11-16

### Added
- **Recipe Creator GUI** - In-game recipe creation interface
  - Drag-and-drop 3x3 crafting grid
  - Recipe type toggle (Shaped/Shapeless)
  - Visual result preview
- **Item Editor GUI** - Customize item names and descriptions
  - MiniMessage color support
  - Multi-line description editor
  - Live preview system
- **Shapeless Recipes** - Position-independent recipe type
  - Support for multiple ingredients of same type
  - Flexible ingredient counts (1-9 items)
- **Hidden Recipes System** - Hide recipes from recipe viewer mods
  - Per-player discovery tracking
  - Unlock on first craft
  - Integration with PrepareItemCraftEvent
- **Advanced Item Storage**
  - Full ItemStack serialization (NBT, PDC, CustomModelData)
  - JSON recipe format alongside YAML
  - Individual recipe files in `recipes/` folder
- **Update Checker** - Automatic update notifications
  - GitHub and Spigot source support
  - Configurable via `config.yml`
  - In-game notifications for ops
- **bStats Integration** - Anonymous usage statistics
  - Server count tracking
  - Recipe count metrics

### Changed
- **Recipe Storage** - Recipes now save to separate files
  - Each recipe in `plugins/CustomRecipes/recipes/<name>.yml`
  - JSON format also created for each recipe
  - Legacy config.yml format still supported
- **Recipe Deletion** - Now permanently removes recipe files
  - Two-click confirmation system
  - Deletes both YAML and JSON files
  - Visual confirmation with color change
- **GUI Design** - Improved visual appearance
  - Light blue glass panes instead of gray
  - Better button layouts (6-row inventories)
  - Empty slots filled in recipe list
  - Clearer button descriptions
- **Recipe Editor GUI** - Expanded layout
  - Now uses 6-row inventory (54 slots)
  - Recipe info book added
  - Better button positioning
  - Type indicator display

### Fixed
- Recipe patterns with empty edges now work correctly
- AIR ingredients no longer cause NullPointerException
- Recipe enable/disable properly syncs with config files
- GUI event listeners properly cleanup on inventory close
- Chat input for item editor works reliably
- Recipe loading handles both Map and ConfigurationSection
- Shaped recipes with 1-2 rows now load correctly
- Recipe keys with different cases handled properly

## [1.0.0] - 2025-11-15

### Added
- Initial release
- Shaped recipe support
- Custom names and lore for crafted items
- Recipe management GUI
- MiniMessage formatting support
- Permission system
- Hot reload functionality
- Bulk crafting (1-64 items per recipe)
- Spawn egg custom name support

[1.1.0]: https://github.com/hikarii-dev/Custom-Recipes/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/hikarii-dev/Custom-Recipes/releases/tag/v1.0.0
