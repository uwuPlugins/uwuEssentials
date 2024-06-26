package me.mapetr.uwuEssentials.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.mapetr.uwuEssentials.Data
import me.mapetr.uwuEssentials.Database
import me.mapetr.uwuEssentials.Message
import org.bukkit.entity.Player

@CommandAlias("tphere")
class TeleportHere : BaseCommand() {
    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    @Description("Teleports a player to you")
    fun onCommand(player: Player, target: OnlinePlayer) {
        if (target.player == null) {
            player.sendMessage("Player not found")
            return
        }
        if (target.player === player) {
            player.sendMessage("You can't teleport to yourself")
            return
        }

        Data.back[target.player.uniqueId.toString()] = target.player.location
        Database.executeAsync("UPDATE back SET x = ${target.player.location.x}, y = ${target.player.location.y}, z = ${target.player.location.z}, yaw = ${target.player.location.yaw}, pitch = ${target.player.location.pitch}, world = '${target.player.world.name}' WHERE name = '${target.player.uniqueId.toString()}'")

        target.player.teleportAsync(player.location)
        Message.sendMessage(player, "<green>Teleported <white>${player.name}</white> to you")
        Message.sendMessage(target.player, "<green>Teleported <white>you</white> to <white>${player.name}")
    }
}
