package org.kamiblue.client.module.modules.misc

import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.modules.player.LagNotifier
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.module.Module

internal object LongJump : Module(
    name = "LongJump",
    category = Category.MISC,
    description = "Jump Longer",
) {
    private val mode by setting("Mode", Mode.PEAK)
    private val farSpeed by setting("Peak Speed", 1.0f, 0.0f..10.0f, 0.1f, { mode == Mode.PEAK }, description = "Speed When Falling")
    private val groundSpeed by setting("Ground Speed", 2.0f, 0.0f..10.0f, 0.1f, { mode == Mode.GROUND }, description = "Speed When Jumping")
    private val disableOnRubberband by setting("Rubberband disable", false)

    private var has = false

    private enum class Mode {
        PEAK,
        GROUND
    }

    init {
        onEnable {
            has = false
        }

        safeListener<PlayerTravelEvent> {
            if (LagNotifier.paused && disableOnRubberband) {
                disable()
            }

            when {
                mode == Mode.PEAK -> {
                    if (player.motionY <= 0 && !has) {
                        has = true
                        player.jumpMovementFactor = farSpeed
                    }
                    if (player.onGround) has = false
                }
                player.onGround -> {
                    player.jump()
                    player.motionX *= groundSpeed
                    player.motionY *= groundSpeed
                }
            }
        }
    }
}