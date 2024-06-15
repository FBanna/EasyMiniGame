This entirely server side mod allows for highly customizable mini games to quickly make games within Minecraft. Ideal for SMP as it saves the players data (inventory, position, world, xp etc.) before the start of a game and restores it at the end. The minigames exist in a custom dimension so does not interfere with the world. At the moment it is developed for making survival games but I will expand it to adopt new games like, dropper, bedwars, bridge, and a parkour game. Every map is tied to a game and the maps config is within easyminigame/(gamename)/(mapname)/config.json

**Commands**

Play:

	/emg play (game name) (optional map name) (starts the specified map or takes a random one from the game)*

Start:

	/emg start *(starts the game)*

Stop:

	/emg stop *(stops the game)*


Debug:

	/emg debug enter *(enters the custom dimension)*
	/emg debug exist *(exits the custom dimension)*

Games:

	/emg game create (game name) (player count)
	/emg game (game name) lives get / set *(how many lives before elimination)*
	/emg game (game name) gamemode get / set *(gamemode for players in the game)*
	/emg game (game name) chestregen  list / remove / clear / add *(ticks between regeneration of chests in the map)*
	/emg game ( game name) reload true / false *(if the map is regenerated between games may cause lag spikes for large maps)*

Create a map with:

	/emg game (game name) map create (map name) (team count)
	/emg game (game name) map (map name) boundaries get / set *(2 block positions that the map will be saved from)*
	/emg game (game name) map (map name) save *(saves the map to nbt file in world/generated)*
	/emg game (game name) map (map name) load *(loads map)*
	/emg game (game name) map (map name) list / remove / add / auto *(add a block position of a chest and its loot table to be generated at start of game, or auto add all chests in boundaries with same loot table)*
	/emg game (game name) map (map name) spawnPoint get / set *(index of team from 0 to amount of teams and a block position of the team spawn point, takes the command senders orientation or set a specific yaw)*

