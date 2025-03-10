#  LinGUi - Ultimate GUI Library for Minecraft

<div align="center">
  
![LinGUi](https://img.shields.io/badge/LinGUi-v1.0--SNAPSHOT-brightgreen)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

**A powerful GUI library plugin for creating interactive chest-based GUIs with advanced scripting capabilities**

</div>

##  Features

- 📦 Create and customize **inventory GUIs** with interactive chest interfaces
- 🔌 **Easy-to-use API** for seamless integration with other plugins
- 📜 **Advanced scripting language** with variables, conditions, loops, and functions
- 🎮 **Visual effects** including particles, sounds, titles, and fireworks
- 🔗 **Command binding system** to map commands to specific GUIs
- 💾 **GUI persistence** with easy-to-edit YAML configuration
- 🔄 **Dynamic item updates** and animations
- 🧩 Support for modular script components with the `include` system
- 🌐 Multi-language support with advanced text formatting

##  Getting Started

### Installation

1. Download the latest release from [GitHub](https://github.com/c0rex86/LinGUi/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/LinGUi/`


##  Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/lingui reload` | Reload the plugin | `lingui.admin` |
| `/lingui help` | Show help information | Everyone |
| `/lingui info` | Show plugin information | Everyone |
| `/lingui list` | List all registered GUIs | `lingui.admin` |
| `/lingui bind <command> <guiId>` | Bind a command to a GUI | `lingui.admin` |
| `/lingui unbind <command>` | Unbind a command | `lingui.admin` |
| `/opengui <guiId> [player]` | Open a GUI for a player | `lingui.use` |

##  Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `lingui.admin` | Access to all LinGUi features | `op` |
| `lingui.use` | Permission to use basic GUI features | `true` |
| `lingui.create` | Permission to create and modify GUIs | `op` |
| `lingui.command` | Permission to use command bindings | `op` |
| `lingui.openother` | Permission to open GUIs for other players | `op` |
| `lingui.console` | Permission to execute console commands in scripts | `op` |

##  Developer API

Integrate LinGUi into your plugin:

```java
// Get the LinGUi instance
LinGUi linGui = (LinGUi) Bukkit.getPluginManager().getPlugin("LinGUi");
GuiManager guiManager = linGui.getGuiManager();

// Create a new GUI
GuiBuilder builder = guiManager.createGui("my_gui", "My Custom GUI", 3);

// Add items to the GUI
builder.setItem(13, new ItemStack(Material.DIAMOND_SWORD), (player, gui) -> {
    player.sendMessage("You clicked a diamond sword!");
    return true;
});

// Register the GUI
GuiPage gui = builder.build();
guiManager.registerGui(gui);

// Open the GUI for a player
guiManager.openGui(player, "my_gui");
```

Access the scripting engine:

```java
// Execute a script with variables
Map<String, Object> variables = new HashMap<>();
variables.put("custom_var", "Hello World");
ScriptResult result = linGui.getScriptManager().executeScript(scriptContent, player, variables);

// Register a custom script function
linGui.getScriptManager().registerScriptFunction("my_function", (context, args) -> {
    // Your function implementation
    return true;
});
```

##  Scripting Language

LinGUi features a powerful scripting language for creating interactive GUIs. Scripts can be defined directly in GUI configurations or saved as separate files in the `plugins/LinGUi/scripts/` directory.

### Basic Commands

```
# Execute a command as the player
cmd: say Hello, I'm using LinGUi!

# Execute a command as console
console: give {player} diamond 1

# Send a message to the player
msg: &aYou received a diamond!

# Open another GUI
open: another_gui

# Close the current GUI
close

# Check permission
perm: lingui.admin
```

### Advanced Features

```
# Variables and Data Types
set balance 100
set player_name {player}
set is_vip true

# Math Operations
math balance + 50     # Addition
math balance - 25     # Subtraction
math balance * 2      # Multiplication
math balance / 4      # Division

# Random Numbers
random number 1 100

# Delays (milliseconds)
delay: 1000

# Include other scripts
include: shop_functions

# Control Flow (if/else)
if {balance} >= 100
  msg: &aYou have enough money!
  math balance - 100
else
  msg: &cNot enough money!
endif

# Comparison Operators
if {value} == 100    # Equal to
if {value} != 200    # Not equal to
if {value} > 50      # Greater than
if {value} < 200     # Less than
if {value} >= 100    # Greater than or equal to
if {value} <= 200    # Less than or equal to

# Logic Operators
if {vip} == true and {balance} > 100
if {vip} == true or {balance} > 1000
if not {banned}

# Loops
for i in 1...10
  msg: Counter: {i}
endfor

# Functions
function process_payment
  math balance - 100
  msg: &aPayment processed!
endfunction

# Call a function
call process_payment

# Visual Effects
sound: ENTITY_EXPERIENCE_ORB_PICKUP 1.0 1.5
particle: HEART 10 0.5 0.5 0.5
title: &b&lWelcome!
subtitle: &eTo our server!
actionbar: &6This appears above your hotbar!
firework: random
```

### Example Script

```
# Check if player has VIP permission
if perm: lingui.vip
  # Welcome message with sound and particles
  sound: ENTITY_PLAYER_LEVELUP 1.0 1.0
  particle: VILLAGER_HAPPY 15 0.5 0.5 0.5
  
  title: &6&lVIP Shop
  subtitle: &eSpecial discounts for &b{player}
  
  # Set player variables
  set discount 25
  
  # Show special message
  msg: &aWelcome to the VIP Shop! You get a &e{discount}% &adiscount!
  
  # Healing effect
  heal
  feed
else
  # Regular welcome for non-VIP
  sound: ENTITY_EXPERIENCE_ORB_PICKUP 1.0 1.0
  title: &7&lShop
  subtitle: &eWelcome, &7{player}
  msg: &7Welcome to the shop. &cVIP members get special discounts!
endif
```

##  Configuration Files

### GUI Configuration (guis/example_gui.yml)

```yaml
title: "Example GUI"
rows: 3
items:
  13:
    material: DIAMOND
    name: "§bShiny Diamond"
    lore:
      - "§7Click to get a diamond"
    script: |
      if perm: lingui.admin
        console: give {player} diamond 1
        msg: &aYou received a diamond!
        sound: ENTITY_PLAYER_LEVELUP 1.0 1.0
      else
        msg: &cYou don't have permission!
        sound: ENTITY_VILLAGER_NO 1.0 1.0
      endif
```

### Command Bindings (command_bindings.yml)

```yaml
default_bindings: true
bindings:
  admin: admin_menu
  shop: shop_gui
  menu: advanced_menu
  game: game_gui
```

##  For Server Administrators

1. **Creating Custom GUIs**: Create YAML files in the `plugins/LinGUi/guis/` directory
2. **Writing Scripts**: Create script files in the `plugins/LinGUi/scripts/` directory
3. **Command Binding**: Use `/lingui bind mycommand gui_name` to create custom commands
4. **Permissions**: Assign permissions to control access to different GUIs

##  Frequently Asked Questions

<details>
<summary><b>How do I create a custom GUI?</b></summary>
Create a new YAML file in the <code>plugins/LinGUi/guis/</code> directory and define your GUI layout, items, and scripts. Use the examples provided as a starting point.
</details>

<details>
<summary><b>How do I use variables in scripts?</b></summary>
Variables are defined with the <code>set</code> command and accessed using curly braces: <code>{variable_name}</code>. For example: <code>set balance 100</code> and then <code>msg: Your balance is ${balance}</code>.
</details>

<details>
<summary><b>Can I use LinGUi with other plugins?</b></summary>
Yes! LinGUi provides a comprehensive API that other plugins can use to create and manage GUIs. See the Developer API section for examples.
</details>

##  Author

- [c0re](https://github.com/c0rex86/)
- Website: [https://c0rex86.ru](https://c0rex86.ru)

## 📜 License

This project is licensed under the MIT License. 