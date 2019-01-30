# Badlion Client CPS API

This repository explains how to limit the clicks per second (cps) of a Badlion Client user via a simple Bungee/Bukkit plugin.

By default there is no limitation to how fast you can click with the Badlion Client.

### Installation

How to install the Badlion Client CPS API on your server.

#### Quick Installation (for non-programmers)

1. Download **either** the latest bukkit or bungee plugin from our releases (you don't need both, we recommend using the BungeeCord plugin if you are running BungeeCord): https://github.com/BadlionNetwork/BadlionClientCPSAPI/releases
2. Place the downloaded plugin into your `plugins` directory on your server.
3. Turn on the BungeeCord or Bukkit server and a default config will be automatically made in `plugins/BadlionClientCPSAPI/config.json`
4. Edit the config as you see fit and reboot the server after you have finished editing the config (see below for more information).

#### Do it yourself (for programmers)

1. Clone this repository
2. Compile the plugin(s) you want to use (you only need one per Minecraft network).
2. Place the compiled plugins from the `target` directories into your `plugins` directory on your server.
3. Turn on the BungeeCord or Bukkit server and a default config will be automatically made in `plugins/BadlionClientCPSAPI/config.json`
4. Edit the config as you see fit and reboot the server after you have finished editing the config (see below for more information).

### Example Config

To make a config place the information below example into `plugins/BadlionClientCPSAPI/config.json`. If you have any JSON errors it will not load the plugin properly. A quick and easy way to test that your JSON config is valid is to use this tool: https://jsonformatter.curiousconcept.com/

This example config will limit a user to 20 clicks per second (cps) when playing on your server with the Badlion Client.

```json
{
	"clicksPerSecondLimit": 20,
	"clicksPerSecondLimitRight": 20
}
```

The first value `clicksPerSecondLimit` is for left click and the second value `clicksPerSecondLimitRight` is for right click.
