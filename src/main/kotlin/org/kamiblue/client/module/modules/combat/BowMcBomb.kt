package org.kamiblue.client.module.modules.combat

import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEgg
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemSnowball
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand


internal object BowMcBomb : Module(
    name = "Glock 19",
    category = Category.COMBAT,
    description = "NOBODY MOVE!",
) {
    private val Bows by setting("Bows", true, description = "Bomb bows")
    private val pearls by setting("Pearls", true, description = "Bomb pearls")
    private val eggs by setting("Eggs", true, description = "Bomb eggs")
    private val snowballs by setting("SnowBalls", true, description = "Bomb snowballs")
    private val Timeout by setting("Timeout", 5000, 100..20000, 1, description = "Bomb timeout")
    private val spoofs by setting("Spoofs", 10, 1..300, 1, description = "Packet per bomb")
    private val bypass by setting("Bypass", false, description = "Bypasses some servers")
    private val debug by setting("Debug", false, description = "De-Insectify")

    private var shooting = false
    private var lastShootTime: Long = 0

    init {
        onEnable {
            shooting = false
            lastShootTime = System.currentTimeMillis()
        }
        safeListener<PacketEvent.Send> { event ->
            if (event.packet is CPacketPlayerDigging) {
                val packet = event.packet as CPacketPlayerDigging
                if (packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    val handStack = player.getHeldItem(EnumHand.MAIN_HAND)
                    if (!handStack.isEmpty && handStack.item is ItemBow && Bows) {
                        doSpoofs()
                        if (debug) MessageSendHelper.sendChatMessage("$chatName trying to spoof")
                    }
                }
            } else if (event.packet is CPacketPlayerTryUseItem) {
                val packet2 = event.packet as CPacketPlayerTryUseItem
                if (packet2.hand == EnumHand.MAIN_HAND) {
                    val handStack = player.getHeldItem(EnumHand.MAIN_HAND)
                    if (!handStack.isEmpty && handStack.item != null) {
                        if (handStack.item is ItemEgg && eggs) {
                            doSpoofs()
                        } else if (handStack.item is ItemEnderPearl && pearls) {
                            doSpoofs()
                        } else if (handStack.item is ItemSnowball && snowballs) {
                            doSpoofs()
                        }
                    }
                }
            }
        }
    }

    private fun doSpoofs() {
        if (System.currentTimeMillis() - lastShootTime >= Timeout) {
            shooting = true
            lastShootTime = System.currentTimeMillis()
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING))
            for (index in 0 until spoofs) {
                if (bypass) {
                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false))
                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true))
                } else {
                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true))
                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false))
                }
            }
            if (debug) MessageSendHelper.sendChatMessage("$chatName Spoofed")
            shooting = false
        }
    }
}
