# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
  
### Technical
- Added `WorldRestrictionManager` for dimension-based recipe control
- Enhanced `ItemStackSerializer` with CustomModelData methods
- Added NBT comparison utilities for ingredient validation
- Improved `RecipeListGUI` with edit item functionality
- Background scheduler task for runtime update checking
- World restriction data in recipe YAML/JSON files
- Added `guiName` and `guiDescription` fields to `ItemEditorGUI`
- Enhanced `RecipeManager.updateRecipeResult()` to save all text fields
- Improved `saveRecipeToFile()` to persist GUI and Crafted properties
- Added static `lastEditors` map for accessing editor state

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

### Technical
- Added `ItemStackSerializer` utility class
- Added `JsonRecipeFileManager` for JSON handling
- Added `RecipeDataManager` for hidden recipe tracking
- Added `ItemEditorGUI` for item customization
- Added `RecipeCreatorGUI` for in-game recipe creation
- Improved `RecipeConfigLoader` with fallback support
- Added `ShapelessRecipeData` record class
- Enhanced `CustomRecipe` to store full ItemStack
- Better error handling and validation throughout

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
