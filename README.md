# HologramStats

A powerful Minecraft plugin that creates dynamic holograms displaying player statistics from SQL databases.
Perfect for showing player achievements, game stats, or any custom data in beautiful, interactive hologram displays — with full PlaceholderAPI support.


## ✅ Features

- 🔗 **Dynamic Hologram System** - Create custom holograms with configurable locations and content
- 🗄️ **MySQL Integration** - Connect to multiple databases and execute custom SQL queries
- 📊 **Real-time Statistics** - Display live player data that updates automatically
- 🎨 **Customizable Content** - Support for hex colors, player placeholders, and dynamic text
- 🖱️ **Interactive Commands** - Clickable buttons for easy hologram management
- 🔄 **PlaceholderAPI Support** - Custom placeholders for database queries
- 🧹 **Auto-cleanup** - Automatic removal of old holograms on server restart
- ⌨️ **Tab Completion** - Smart command suggestions with permission-based filtering
- 🎭 **Player-specific Holograms** - Individual hologram instances for each player


## 🛠️ Requirements

- **Minecraft 1.20.5 and above**
- **Java 21**
- **MySQL/MariaDB database**
- **PlaceholderAPI** (soft dependency)


## 💬 Commands & 🔐 Permissions

- `/hs reload` (hologramstats.reload) – reloads the plugin configuration
- `/hs spawn <name>` (hologramstats.spawn) – spawns a hologram for the player
- `/hs setlocation <name>` (hologramstats.setlocation) – sets hologram location to player's position
- `/hs list` (hologramstats.list) – displays all available holograms with clickable buttons
- `hologramstats.help` – access to help command


## 🎯 Hologram Features

- **Multi-database support** - Connect to different databases for various data sources
- **Custom SQL queries** - Execute personalized database queries with player placeholders
- **Dynamic content** - Use placeholders like `%player%`, `%player_uuid%`, and custom query results
- **Hex color support** - Beautiful, colorful hologram displays
- **Location persistence** - Save and restore hologram positions across server restarts
- **Player-specific instances** - Each player sees their own personalized hologram data

## 📊 PlaceholderAPI Examples

After setting up your database queries in `config.yml`, you can use the generated placeholders in any plugin that supports PlaceholderAPI:

### Example Placeholders:
- `%hologramstats_speedbuilders-wins%` - Returns the number of wins for the player
- `%hologramstats_speedbuilders-played%` - Returns the number of games played
- `%hologramstats_cosmetics-owned%` - Returns the number of owned cosmetics

### How It Works:
1. Configure your queries in `config.yml` under the `queries` section
2. The plugin automatically generates placeholders in format: `%hologramstats_[query-name]%`
3. When the placeholder is used, it executes the corresponding SQL query
4. Returns the result directly from your database

This makes it easy to display real-time player statistics anywhere on your server using the standard PlaceholderAPI syntax!


## 📥 Installation

1. Download the plugin `.jar` file
2. Ensure you have **MySQL/MariaDB** running
3. Place the plugin into your server's `/plugins` folder
4. Start the server and configure `config.yml`
5. Set up your database connections and hologram configurations


## ⚖️ License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0) license.  
You are free to use, share, and modify the code for non-commercial purposes. Commercial use is not allowed.

🔗 Full license: [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/)
