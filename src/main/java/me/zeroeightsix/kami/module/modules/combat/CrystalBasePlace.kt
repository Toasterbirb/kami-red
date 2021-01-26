package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.WorldUtils.getNeighbour
import me.zeroeightsix.kami.util.WorldUtils.hasNeighbour
import me.zeroeightsix.kami.util.WorldUtils.isPlaceable
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.CrystalUtils.calcCrystalDamage
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.items.block
import me.zeroeightsix.kami.util.items.firstBlock
import me.zeroeightsix.kami.util.items.hotbarSlots
import me.zeroeightsix.kami.util.math.RotationUtils.getRotationTo
import me.zeroeightsix.kami.util.math.VectorUtils
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.util.*

@CombatManager.CombatModule
internal object CrystalBasePlace : Module(
    name = "CrystalBasePlace",
    description = "Places obby for placing crystal on",
    category = Category.COMBAT,
    modulePriority = 90
) {
    private val manualPlaceBind = setting("BindManualPlace", Bind())
    private val minDamageInc = setting("MinDamageInc", 2.0f, 0.0f..10.0f, 0.25f)
    private val range = setting("Range", 4.0f, 0.0f..8.0f, 0.5f)
    private val delay = setting("Delay", 20, 0..50, 5)

    private val timer = TickTimer()
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }
    private var inactiveTicks = 0
    private var rotationTo: Vec3d? = null
    private var placePacket: CPacketPlayerTryUseItemOnBlock? = null

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 3
    }

    init {
        onDisable {
            inactiveTicks = 0
            placePacket = null
            PlayerPacketManager.resetHotbar()
        }

        listener<RenderWorldEvent> {
            val clear = inactiveTicks >= 30
            renderer.render(clear)
        }

        safeListener<InputEvent.KeyInputEvent> {
            if (!CombatManager.isOnTopPriority(this@CrystalBasePlace) || CombatSetting.pause) return@safeListener
            val target = CombatManager.target ?: return@safeListener

            if (manualPlaceBind.value.isDown(Keyboard.getEventKey())) prePlace(target)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener
            inactiveTicks++

            if (!CombatManager.isOnTopPriority(CrystalBasePlace) || CombatSetting.pause) return@safeListener

            val slot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) ?: return@safeListener
            val target = CombatManager.target ?: return@safeListener

            placePacket?.let { packet ->
                if (inactiveTicks > 1) {
                    if (!isHoldingObby) PlayerPacketManager.spoofHotbar(slot.hotbarSlot)
                    player.swingArm(EnumHand.MAIN_HAND)
                    connection.sendPacket(packet)
                    PlayerPacketManager.resetHotbar()
                    placePacket = null
                }
            }

            if (placePacket == null && CrystalAura.isEnabled && CrystalAura.inactiveTicks > 15) prePlace(target)

            if (isActive()) {
                rotationTo?.let { hitVec ->
                    val rotation = getRotationTo(hitVec)
                    PlayerPacketManager.addPacket(CrystalBasePlace, PlayerPacketManager.PlayerPacket(rotating = true, rotation = rotation))
                }
            } else {
                rotationTo = null
            }
        }
    }

    private val SafeClientEvent.isHoldingObby
        get() = isObby(player.heldItemMainhand)
                || isObby(player.inventory.getStackInSlot(PlayerPacketManager.serverSideHotbar))

    private fun isObby(itemStack: ItemStack) = itemStack.item.block == Blocks.OBSIDIAN

    private fun SafeClientEvent.prePlace(entity: EntityLivingBase) {
        if (rotationTo != null || !timer.tick((delay.value * 50.0f).toLong(), false)) return
        val placeInfo = getPlaceInfo(entity)
        if (placeInfo != null) {
            val offset = WorldUtils.getHitVecOffset(placeInfo.first)
            val hitVec = Vec3d(placeInfo.second).add(offset)
            rotationTo = hitVec
            placePacket = CPacketPlayerTryUseItemOnBlock(placeInfo.second, placeInfo.first, EnumHand.MAIN_HAND, offset.x.toFloat(), offset.y.toFloat(), offset.z.toFloat())
            renderer.clear()
            renderer.add(placeInfo.second.offset(placeInfo.first), ColorHolder(255, 255, 255))
            inactiveTicks = 0
            timer.reset()
        } else {
            timer.reset((delay.value * -25.0f).toLong())
        }
    }

    private fun SafeClientEvent.getPlaceInfo(entity: EntityLivingBase): Pair<EnumFacing, BlockPos>? {
        val cacheMap = TreeMap<Float, BlockPos>(compareByDescending { it })
        val prediction = CombatSetting.getPrediction(entity)
        val eyePos = player.getPositionEyes(1.0f)
        val posList = VectorUtils.getBlockPosInSphere(eyePos, range.value)
        val maxCurrentDamage = CombatManager.placeMap.entries
            .filter { eyePos.distanceTo(it.key) < range.value }
            .map { it.value.first }
            .maxOrNull() ?: 0.0f

        for (pos in posList) {
            // Placeable check
            if (!isPlaceable(pos, false)) continue

            // Neighbour blocks check
            if (!hasNeighbour(pos)) continue

            // Damage check
            val damage = calcPlaceDamage(pos, entity, prediction.first, prediction.second)
            if (!checkDamage(damage.first, damage.second, maxCurrentDamage)) continue

            cacheMap[damage.first] = pos
        }

        for (pos in cacheMap.values) {
            return getNeighbour(pos, 1) ?: continue
        }
        return null
    }

    private fun SafeClientEvent.calcPlaceDamage(pos: BlockPos, entity: EntityLivingBase, entityPos: Vec3d, entityBB: AxisAlignedBB): Pair<Float, Float> {
        // Set up a fake obsidian here for proper damage calculation
        val prevState = world.getBlockState(pos)
        world.setBlockState(pos, Blocks.OBSIDIAN.defaultState)

        // Checks damage
        val damage = calcCrystalDamage(pos, entity, entityPos, entityBB)
        val selfDamage = calcCrystalDamage(pos, player)

        // Revert the block state before return
        world.setBlockState(pos, prevState)

        return damage to selfDamage
    }

    private fun checkDamage(damage: Float, selfDamage: Float, maxCurrentDamage: Float) =
        selfDamage < CrystalAura.maxSelfDamage && damage > CrystalAura.minDamage && (maxCurrentDamage < CrystalAura.minDamage || damage - maxCurrentDamage >= minDamageInc.value)
}