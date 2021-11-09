import net.minecraft.entity.item.EntityFireworkRocket
import net.minecraft.network.play.server.SPacketPlayerPosLook
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener

internal object ElytraFix: Module(
    name = "ElytraFix",
    category = Category.MISC,
    description = "Remove Firework Rockets on Rubberband",
) {
    init {
        safeListener<PacketEvent.Receive> { event ->
            if (event.packet is SPacketPlayerPosLook && player.isElytraFlying) {
                world.getLoadedEntityList().filterIsInstance<EntityFireworkRocket>().forEach {
                    world.removeEntity(it)
                }
            }
        }
    }
}