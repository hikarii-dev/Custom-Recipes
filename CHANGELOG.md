# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
