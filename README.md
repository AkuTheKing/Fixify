# Fixify

Fixify is a configurable client-side utility mod for Hypixel SkyBlock. It
combines dungeon tools, visual customization, HUD overlays, performance
improvements, reminders, and quality-of-life fixes in one themed interface.

The menu is divided into three columns: **Dungeons**, **Visuals**, and
**Misc**. Feature settings are collapsed by default, changes are saved
automatically, and the old global "Only in SkyBlock" restriction has been
removed. Features that depend on SkyBlock data still appear only when their
required data is available.

## Requirements

- Minecraft 26.1.2
- Fabric Loader 0.19.2 or newer
- Fabric API
- Java 25 or newer

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 26.1.2.
2. Place the Fixify jar in the Minecraft `mods` directory.
3. Launch the Fabric profile.

## Menu And Controls

- Press `Right Shift` to open or close the Fixify menu.
- Use `/fixify` to open the menu.
- Use `/fixify hudedit` to open the HUD editor.
- Expand a feature with its `+` control to view its settings.
- Feature states, settings, colors, keybinds, HUD positions, and menu layout
  are saved automatically to `config/fixify.json`.

## Dungeons

### Dungeon Score Meter

Displays the current dungeon score in a movable HUD widget.

- Configurable anchor and scale
- Rank or gradient theme
- Custom gradient colors and rotation
- Positioning through the HUD editor

### Etherwarp

Previews the destination of an Etherwarp before teleporting.

- Valid and failed destination colors
- Filled, outline, or filled-outline rendering
- Optional failed-position preview
- Server-position calculation option
- Full-block and depth-rendering controls
- Left-click Etherwarp modes
- Optional automatic sneaking for left-click Etherwarp
- Configurable keybind

### DungeonBreaker

Fixes client-side Dungeon Breaker behavior affected by latency and mining
fatigue.

- Prevents accidental mining of secret-related and protected blocks
- Locally removes valid blocks when mining fatigue would otherwise delay them
- Configurable keybind

### Teammate Highlight

Highlights dungeon teammates using their detected class role.

- Archer, Berserker, Tank, Mage, and Healer colors
- Filled and outlined player highlighting
- Configurable keybind

## Visuals

### Player Hider

Reduces visual obstruction caused by nearby players.

- Hide nearby players within a configurable distance
- Hide all remote players
- Ghost mode with configurable opacity
- Click through remote players

### Player Size

Changes the rendered size of the local player or all players.

- Separate X, Y, and Z scaling
- Optional scaling for all remote players
- Adjusted name-tag placement

### Hit Color

Replaces the vanilla red damage overlay with a configurable color and alpha.

### Fullbright

Removes client-side darkness and keeps the world fully visible.

### Performance HUD

Displays live performance and connection information.

- FPS, estimated TPS, and average ping
- Horizontal or vertical layout
- Configurable anchor and scale
- Custom label and value colors
- Positioning through the HUD editor

### Render Optimizer

Suppresses selected entities, effects, and overlays to reduce visual clutter
and unnecessary rendering.

- Falling blocks, lightning, and experience orbs
- Death animations and dying armor stands
- Explosion particles
- Archer passive effects
- Healer fairies, Soul Weavers, and tentacle heads
- Fire overlay

### Name Replace

Automatically detects the logged-in Minecraft username and replaces it in
rendered text.

- Custom replacement text
- Custom replacement color
- No manual original-name field required

### Zoom

Provides a configurable hold-to-zoom feature.

- FOV range from 10 to 110
- Configurable keybind, defaulting to `C`
- Optional scroll-wheel adjustment while zoomed
- Restores the previous FOV after releasing the key

### Pet Overlay

Displays active pet information in a customizable HUD widget.

- Bar and circular layouts
- Pet item display and icon-position controls
- Anchor, scale, and HUD editor positioning
- Rarity and custom themes
- Level, XP, and background colors
- Idle, hover, level-up, and value animations
- Optional rainbow level, XP, and background effects

### Pressure Display

Shows Great Sea pressure in a movable HUD widget.

- Configurable display threshold
- Anchor and scale controls
- Multiple themes
- Positioning through the HUD editor

### Low HP Indicator

Displays a screen warning when health is low.

- Configurable transparency
- Optional heartbeat-style pulse animation

### Drill Fuel Meter

Displays the current drill fuel amount in a movable HUD widget.

- Anchor and scale controls
- Biofuel and Mithril themes
- Positioning through the HUD editor

### Action Bar Cleanup

Removes selected duplicate information from the action bar.

- Hide pressure text
- Hide drill fuel text

## Misc

### Diana QoL

Ignores obstructive grass, flowers, bushes, and similar small block hitboxes
when interacting during Diana activities.

### Golden Fish CI

Allows fishing-rod use when interacting with a nearby Golden Fish would
otherwise block the action.

### Leap Frog

Detects an approaching fishing wake and performs one jump shortly before the
fish reaches the bobber.

### Smart Term AC

Queues controlled Terminator attack clicks while the use key is held, while
avoiding activation on Terminators with Rend.

### Infinite Chat

Expands the chat history limit to 10,000 messages and prevents automatic chat
history clearing while enabled.

### Reminder

Tracks the current SkyBlock calendar date and displays configurable reminders.

- Up to five reminder rules
- Custom reminder names, day lists, and commands
- Supports individual days and ranges such as `7, 14, 21` or `29-31`
- Configurable warning duration and scale
- Optional clickable command button in chat

### Missing Enchants

Compares an item's enchantments against the NotEnoughUpdates enchant data and
lists compatible missing enchantments. Hold `Shift` to expand the list.

### Compact Pet Level

Shortens pet level text in tooltips while preserving pet rarity coloring and
level ranges.

## Commands

- `/fixify` opens the Fixify menu.
- `/fixify hudedit` opens the HUD editor.
- `/fixify reminder` displays Reminder status.
- `/fixify reminder status` displays Reminder status.
- `/fixify reminder test` displays a test Reminder warning.
