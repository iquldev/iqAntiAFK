# iqAntiAFK

![License](https://img.shields.io/badge/license-MIT-blue.svg) ![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Platform](https://img.shields.io/badge/Platform-Paper%20%2F%20Spigot-green)

**iqAntiAFK** is a professional, high-performance, and feature-rich Anti-AFK plugin for Minecraft servers. It goes beyond simple timer checks by implementing heuristic analysis to detect macro/bot evasions.

## Features

- **Smart Detection**:
  - **Activity Analysis**: Tracking of movement, rotation, inventory clicks, block breaking/placing, and chat.
  - **Anti-Bot Heuristics**: Detects "Anti-AFK" mods that spin, shake head, or jump in place using variance analysis.
  - **Strafe Detection**: Identifies bots walking back and forth (inefficient movement) to bypass standard checks.
- **Dynamic Roles**:
  - Define unlimited custom roles in `config.yml` (e.g., `vip`, `mvp`, `admin`).
  - Assign different AFK timers and actions (Kick, Warn, Notify Admins) per role.
  - Automatic priority handling (longest timer wins).
- **Suspicion System**:
  - Tracks suspicious repetitive behavior.
  - Alerts admins or auto-kicks macros that try to bypass AFK checks.
- **Localization**:
  - Built-in support for English (`en_US`) and Russian (`ru_RU`).
- **Beautiful Design**:
  - Hex color support and gradients for all messages.

## Installation

1.  Download `iqAntiAFK.jar`.
2.  Place it in your server's `plugins` folder.
3.  Restart the server.
4.  Edit `plugins/iqAntiAFK/config.yml` to customize settings.

## Configuration

```yaml
language: en_US # or ru_RU

roles:
  default:
    afk-time: 300
    action: KICK
  vip:
    afk-time: 600
    action: WARN
  admin:
    afk-time: -1 # Bypass
    action: NONE

activity-thresholds:
  min-rotation: 10.0
  min-movement: 0.1
  ignore-jumping-in-place: true
```

## Commands

- `/iqaa reload` - Reload configuration.
- `/whoafk` - List currently AFK players.
- `/activity <player>` - View detailed activity stats and suspicion level.

## Permissions

- `iqantiafk.reload`
- `iqantiafk.whoafk`
- `iqantiafk.activity`
- `iqantiafk.notify` (Receive admin alerts)
- `iqantiafk.bypass` (Bypass all checks)
- `iqantiafk.role.<name>` (Assign custom roles)
