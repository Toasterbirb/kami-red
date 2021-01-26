package me.zeroeightsix.kami.module.modules.combat

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.extension.id
import me.zeroeightsix.kami.mixin.extension.packetAction
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.WorldUtils.getHitSide
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CombatUtils.equipBestWeapon
import me.zeroeightsix.kami.util.combat.CrystalUtils.calcCrystalDamage
import me.zeroeightsix.kami.util.combat.CrystalUtils.canPlaceCollide
import me.zeroeightsix.kami.util.combat.CrystalUtils.getCrystalBB
import me.zeroeightsix.kami.util.combat.CrystalUtils.getCrystalList
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.RotationUtils.getRotationTo
import me.zeroeightsix.kami.util.math.RotationUtils.getRotationToEntity
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3d
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3dCenter
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.runSafeR
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@CombatManager.CombatModule
internal object CrystalAura : Module(
    name = "CrystalAura",
    alias = arrayOf("CA", "AC", "AutoCrystal"),
    description = "Places End Crystals to kill enemies",
    category = Category.COMBAT,
    modulePriority = 80
) {
    /* Settings */
    private val page by setting("Page", Page.GENERAL)

    /* General */
    private val noSuicideThreshold by setting("NoSuicide", 8.0f, 0.0f..20.0f, 0.5f, { page == Page.GENERAL })
    private val rotationTolerance by setting("RotationTolerance", 10, 5..50, 5, { page == Page.GENERAL })
    private val maxYawSpeed by setting("MaxYawSpeed", 50, 10..100, 5, { page == Page.GENERAL })
    private val swingMode by setting("SwingMode", SwingMode.CLIENT, { page == Page.GENERAL })

    /* Force place */
    private val bindForcePlace by setting("BindForcePlace", Bind(), { page == Page.FORCE_PLACE })
    private val forcePlaceHealth by setting("ForcePlaceHealth", 6.0f, 0.0f..20.0f, 0.5f, { page == Page.FORCE_PLACE })
    private val forcePlaceArmorDura by setting("ForcePlaceArmorDura", 5, 0..50, 1, { page == Page.FORCE_PLACE })
    private val minDamageForcePlace by setting("MinDamageForcePlace", 1.5f, 0.0f..10.0f, 0.25f, { page == Page.FORCE_PLACE })

    /* Place page one */
    private val doPlace by setting("Place", true, { page == Page.PLACE_ONE })
    private val autoSwap by setting("AutoSwap", true, { page == Page.PLACE_ONE })
    private val spoofHotbar by setting("SpoofHotbar", false, { page == Page.PLACE_ONE && autoSwap })
    private val placeSwing by setting("PlaceSwing", true, { page == Page.PLACE_ONE })
    private val placeSync by setting("PlaceSync", false, { page == Page.PLACE_ONE })
    private val extraPlacePacket by setting("ExtraPlacePacket", false, { page == Page.PLACE_ONE })

    /* Place page two */
    private val minDamageP by setting("MinDamagePlace", 2.0f, 0.0f..10.0f, 0.25f, { page == Page.PLACE_TWO })
    private val maxSelfDamageP by setting("MaxSelfDamagePlace", 2.0f, 0.0f..10.0f, 0.25f, { page == Page.PLACE_TWO })
    private val placeOffset by setting("PlaceOffset", 1.0f, 0f..1f, 0.05f, { page == Page.PLACE_TWO })
    private val maxCrystal by setting("MaxCrystal", 2, 1..5, 1, { page == Page.PLACE_TWO })
    private val placeDelay by setting("PlaceDelay", 1, 1..10, 1, { page == Page.PLACE_TWO })
    private val placeRange by setting("PlaceRange", 4.0f, 0.0f..5.0f, 0.25f, { page == Page.PLACE_TWO })
    private val wallPlaceRange by setting("WallPlaceRange", 2.0f, 0.0f..5.0f, 0.25f, { page == Page.PLACE_TWO })

    /* Explode page one */
    private val doExplode by setting("Explode", true, { page == Page.EXPLODE_ONE })
    private val autoForceExplode by setting("AutoForceExplode", true, { page == Page.EXPLODE_ONE })
    private val antiWeakness by setting("AntiWeakness", true, { page == Page.EXPLODE_ONE })
    private val packetExplode by setting("PacketExplode", true, { page == Page.EXPLODE_ONE })
    private val predictExplode by setting("PredictExplode", false, { page == Page.EXPLODE_ONE })
    private val predictDelay by setting("PredictDelay", 10, 0..200, 1, { page == Page.EXPLODE_ONE && predictExplode })

    /* Explode page two */
    private val minDamageE by setting("MinDamageExplode", 6.0f, 0.0f..10.0f, 0.25f, { page == Page.EXPLODE_TWO })
    private val maxSelfDamageE by setting("MaxSelfDamageExplode", 3.0f, 0.0f..10.0f, 0.25f, { page == Page.EXPLODE_TWO })
    private val swapDelay by setting("SwapDelay", 10, 1..50, 1, { page == Page.EXPLODE_TWO })
    private val hitDelay by setting("HitDelay", 1, 1..10, 1, { page == Page.EXPLODE_TWO })
    private val hitAttempts by setting("HitAttempts", 4, 0..8, 1, { page == Page.EXPLODE_TWO })
    private val explodeRange by setting("ExplodeRange", 4.0f, 0.0f..5.0f, 0.25f, { page == Page.EXPLODE_TWO })
    private val wallExplodeRange by setting("WallExplodeRange", 2.0f, 0.0f..5.0f, 0.25f, { page == Page.EXPLODE_TWO })
    /* End of settings */

    private enum class Page {
        GENERAL, FORCE_PLACE, PLACE_ONE, PLACE_TWO, EXPLODE_ONE, EXPLODE_TWO
    }

    @Suppress("UNUSED")
    private enum class SwingMode {
        CLIENT, PACKET
    }

    /* Variables */
    private val placedBBMap = Collections.synchronizedMap(HashMap<BlockPos, Pair<AxisAlignedBB, Long>>()) // <CrystalBoundingBox, Added Time>
    private val ignoredList = HashSet<EntityEnderCrystal>()
    private val packetList = ArrayList<Packet<*>>(3)
    private val yawDiffList = FloatArray(20)
    private val lockObject = Any()

    private var placeMap = emptyMap<BlockPos, Triple<Float, Float, Double>>() // <BlockPos, Target Damage, Self Damage>
    private var crystalMap = emptyMap<EntityEnderCrystal, Triple<Float, Float, Double>>() // <Crystal, <Target Damage, Self Damage>>
    private var lastCrystal: EntityEnderCrystal? = null
    private var lastLookAt = Vec3d.ZERO
    private var forcePlacing = false
    private var placeTimer = 0
    private var hitTimer = 0
    private var hitCount = 0
    private var yawDiffIndex = 0
    private var lastEntityID = 0

    var inactiveTicks = 20; private set
    val minDamage get() = max(minDamageP, minDamageE)
    val maxSelfDamage get() = min(maxSelfDamageP, maxSelfDamageE)

    override fun isActive() = isEnabled && inactiveTicks <= 20

    init {
        onEnable {
            runSafeR {
                resetRotation()
            } ?: disable()
        }

        onDisable {
            lastCrystal = null
            forcePlacing = false
            placeTimer = 0
            hitTimer = 0
            hitCount = 0
            inactiveTicks = 10
            PlayerPacketManager.resetHotbar()
        }

        listener<InputEvent.KeyInputEvent> {
            if (bindForcePlace.isDown(Keyboard.getEventKey())) {
                forcePlacing = !forcePlacing
                MessageSendHelper.sendChatMessage("$chatName Force placing" + if (forcePlacing) " &aenabled" else " &cdisabled")
            }
        }

        safeListener<PacketEvent.Receive> { event ->
            when (event.packet) {
                is SPacketSpawnObject -> {
                    lastEntityID = event.packet.entityID

                    if (event.packet.type == 51) {
                        val vec3d = Vec3d(event.packet.x, event.packet.y, event.packet.z)
                        val pos = vec3d.toBlockPos()

                        placedBBMap.remove(pos)?.let {
                            if (packetExplode) {
                                packetExplode(event.packet.entityID, pos.down(), vec3d)
                            }
                        }
                    }
                }
                is SPacketSoundEffect -> {
                    // Minecraft sends sounds packets a tick before removing the crystal lol
                    if (event.packet.category == SoundCategory.BLOCKS && event.packet.sound == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                        val crystalList = getCrystalList(Vec3d(event.packet.x, event.packet.y, event.packet.z), 6.0f)

                        for (crystal in crystalList) {
                            crystal.setDead()
                        }

                        ignoredList.clear()
                        hitCount = 0
                    }
                }
            }
        }

        safeListener<OnUpdateWalkingPlayerEvent> {
            if (!CombatManager.isOnTopPriority(this@CrystalAura) || CombatSetting.pause) return@safeListener

            if (it.phase == Phase.PRE && inactiveTicks <= 20 && lastLookAt != Vec3d.ZERO) {
                val packet = PlayerPacketManager.PlayerPacket(rotating = true, rotation = getLastRotation())
                PlayerPacketManager.addPacket(this@CrystalAura, packet)
            }

            if (it.phase == Phase.POST) {
                synchronized(lockObject) {
                    for (packet in packetList) sendPacketDirect(packet)
                    packetList.clear()
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent>(2000) {
            if (it.phase == TickEvent.Phase.START) {
                inactiveTicks++
                hitTimer++
                placeTimer++
                updateYawSpeed()
            }

            if (CombatManager.isOnTopPriority(CrystalAura) && !CombatSetting.pause && packetList.size == 0) {
                updateMap()
                if (canExplode()) explode()
                if (canPlace()) place()
            }

            if (it.phase == TickEvent.Phase.END) {
                if (inactiveTicks > 5 || getHand() == EnumHand.OFF_HAND) PlayerPacketManager.resetHotbar()
                if (inactiveTicks > 20) resetRotation()
            }
        }
    }

    private fun updateYawSpeed() {
        val yawDiff = abs(RotationUtils.normalizeAngle(PlayerPacketManager.prevServerSideRotation.x - PlayerPacketManager.serverSideRotation.x))
        yawDiffList[yawDiffIndex] = yawDiff
        yawDiffIndex = (yawDiffIndex + 1) % 20
    }

    private fun SafeClientEvent.updateMap() {
        placeMap = CombatManager.placeMap
        crystalMap = CombatManager.crystalMap

        placedBBMap.values.removeIf { System.currentTimeMillis() - it.second > max(InfoCalculator.ping(), 100) }

        if (inactiveTicks > 20) {
            if (getPlacingPos() == null && placedBBMap.isNotEmpty()) {
                placedBBMap.clear()
            }

            if (getExplodingCrystal() == null && ignoredList.isNotEmpty()) {
                ignoredList.clear()
                hitCount = 0
            }
        }
    }

    private fun SafeClientEvent.place() {
        getPlacingPos()?.let { pos ->
            val hand = getHand()

            if (hand == null) {
                if (autoSwap) {
                    player.hotbarSlots.firstItem(Items.END_CRYSTAL)?.let {
                        if (spoofHotbar) PlayerPacketManager.spoofHotbar(it.hotbarSlot)
                        else swapToSlot(it)
                    }
                }
                return
            }

            placeTimer = 0
            inactiveTicks = 0
            lastLookAt = Vec3d(pos).add(0.5, placeOffset.toDouble(), 0.5)

            sendOrQueuePacket(getPlacePacket(pos, hand))
            if (extraPlacePacket) sendOrQueuePacket(getPlacePacket(pos, hand))
            if (placeSwing) sendOrQueuePacket(CPacketAnimation(hand))

            val crystalPos = pos.up()
            placedBBMap[crystalPos] = getCrystalBB(crystalPos) to System.currentTimeMillis()

            if (predictExplode) {
                defaultScope.launch {
                    delay(predictDelay.toLong())

                    synchronized(lockObject) {
                        if (!placedBBMap.containsKey(crystalPos)) return@synchronized
                        packetExplode(lastEntityID + 1, pos, crystalPos.toVec3d(0.5, 0.0, 0.5))
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.preExplode(): Boolean {
        if (antiWeakness && player.isPotionActive(MobEffects.WEAKNESS) && !isHoldingTool()) {
            equipBestWeapon()
            PlayerPacketManager.resetHotbar()
            return false
        }

        // Anticheat doesn't allow you attack right after changing item
        if (System.currentTimeMillis() - PlayerPacketManager.lastSwapTime < swapDelay * 50) {
            return false
        }

        return true
    }

    private fun SafeClientEvent.packetExplode(entityID: Int, pos: BlockPos, vec3d: Vec3d) {
        if (!preExplode()) return

        val triple = placeMap[pos] ?: return

        if (!noSuicideCheck(triple.second)) return
        if (!checkDamageExplode(triple.first, triple.second)) return
        if (triple.third > explodeRange) return

        val attackPacket = CPacketUseEntity().apply {
            id = entityID
            packetAction = CPacketUseEntity.Action.ATTACK
        }

        synchronized(lockObject) {
            explodeDirect(attackPacket, vec3d)
        }
    }

    private fun SafeClientEvent.explode() {
        getExplodingCrystal()?.let {
            if (!preExplode()) return

            if (hitAttempts != 0 && it == lastCrystal) {
                hitCount++
                if (hitCount >= hitAttempts) ignoredList.add(it)
            } else {
                hitCount = 0
            }

            CombatManager.target?.let { target -> player.setLastAttackedEntity(target) }
            lastCrystal = it

            explodeDirect(CPacketUseEntity(it), it.positionVector)
        }
    }

    private fun SafeClientEvent.explodeDirect(packet: CPacketUseEntity, pos: Vec3d) {
        hitTimer = 0
        inactiveTicks = 0
        lastLookAt = pos

        sendOrQueuePacket(packet)
        sendOrQueuePacket(CPacketAnimation(getHand() ?: EnumHand.OFF_HAND))
    }

    private fun SafeClientEvent.getPlacePacket(pos: BlockPos, hand: EnumHand) =
        CPacketPlayerTryUseItemOnBlock(pos, getHitSide(pos), hand, 0.5f, placeOffset, 0.5f)

    private fun SafeClientEvent.sendOrQueuePacket(packet: Packet<*>) {
        val yawDiff = abs(RotationUtils.normalizeAngle(PlayerPacketManager.serverSideRotation.x - getLastRotation().x))
        if (yawDiff < rotationTolerance) sendPacketDirect(packet)
        else packetList.add(packet)
    }

    private fun SafeClientEvent.sendPacketDirect(packet: Packet<*>) {
        if (packet is CPacketAnimation && swingMode == SwingMode.CLIENT) player.swingArm(packet.hand)
        else connection.sendPacket(packet)
    }
    /* End of main functions */

    /* Placing */
    private fun SafeClientEvent.canPlace() =
        doPlace
            && placeTimer > placeDelay
            && player.allSlots.countItem(Items.END_CRYSTAL) > 0
            && countValidCrystal() < maxCrystal

    @Suppress("UnconditionalJumpStatementInLoop") // The linter is wrong here, it will continue until it's supposed to return
    private fun SafeClientEvent.getPlacingPos(): BlockPos? {
        if (placeMap.isEmpty()) return null

        val eyePos = player.getPositionEyes(1f)

        for ((pos, triple) in placeMap) {
            // Damage check
            if (!noSuicideCheck(triple.second)) continue
            if (!checkDamagePlace(triple.first, triple.second)) continue

            // Distance check
            if (triple.third > placeRange) continue

            // Wall distance check
            val rayTraceResult = world.rayTraceBlocks(eyePos, pos.toVec3dCenter())
            val hitBlockPos = rayTraceResult?.blockPos ?: pos
            if (hitBlockPos.distanceTo(pos) > 1.0 && triple.third > wallPlaceRange) continue

            // Collide check
            if (!canPlaceCollide(pos)) continue

            // Place sync
            if (placeSync) {
                val bb = getCrystalBB(pos.up())
                if (placedBBMap.values.any { it.first.intersects(bb) }) continue
            }

            // Yaw speed check
            val hitVec = pos.toVec3d().add(0.5, placeOffset.toDouble(), 0.5)
            if (!checkYawSpeed(getRotationTo(hitVec).x)) continue

            return pos
        }
        return null
    }

    /**
     * @return True if passed placing damage check
     */
    private fun checkDamagePlace(damage: Float, selfDamage: Float) =
        (shouldFacePlace(damage) || damage >= minDamageP) && (selfDamage <= maxSelfDamageP)
    /* End of placing */

    /* Exploding */
    private fun canExplode() = doExplode && hitTimer > hitDelay

    private fun SafeClientEvent.getExplodingCrystal(): EntityEnderCrystal? {
        val filteredCrystal = crystalMap.entries.filter { (crystal, triple) ->
            !ignoredList.contains(crystal)
                && !crystal.isDead
                && checkDamageExplode(triple.first, triple.second)
                && checkYawSpeed(getRotationToEntity(crystal).x)
        }

        return (filteredCrystal.firstOrNull { (crystal, triple) ->
            triple.third <= explodeRange
                && (player.canEntityBeSeen(crystal) || EntityUtils.canEntityFeetBeSeen(crystal))
        } ?: filteredCrystal.firstOrNull { (_, triple) ->
            triple.third <= wallExplodeRange
        })?.key
    }


    private fun checkDamageExplode(damage: Float, selfDamage: Float) =
        (shouldFacePlace(damage) || shouldForceExplode() || damage >= minDamageE) && selfDamage <= maxSelfDamageE

    private fun shouldForceExplode() = autoForceExplode
        && placeMap.values.any {
        it.first > minDamage && it.second <= maxSelfDamage && it.third <= placeRange
    }
    /* End of exploding */

    /* General */
    private fun SafeClientEvent.getHand(): EnumHand? {
        val serverSideItem = if (spoofHotbar) player.inventory.getStackInSlot(PlayerPacketManager.serverSideHotbar).item else null

        return when (Items.END_CRYSTAL) {
            player.heldItemOffhand.item -> EnumHand.OFF_HAND
            player.heldItemMainhand.item -> EnumHand.MAIN_HAND
            serverSideItem -> EnumHand.MAIN_HAND
            else -> null
        }
    }

    private fun SafeClientEvent.noSuicideCheck(selfDamage: Float) = CombatUtils.getHealthSmart(player) - selfDamage > noSuicideThreshold

    private fun SafeClientEvent.isHoldingTool(): Boolean {
        val item = player.heldItemMainhand.item
        return item is ItemTool || item is ItemSword
    }

    private fun shouldFacePlace(damage: Float) =
        damage >= minDamageForcePlace
            && (forcePlacing
            || forcePlaceHealth > 0.0f && CombatManager.target?.let { CombatUtils.getHealthSmart(it) <= forcePlaceHealth } ?: false
            || forcePlaceArmorDura > 0.0f && getMinArmorDura() <= forcePlaceArmorDura)

    private fun getMinArmorDura() =
        (CombatManager.target?.let { target ->
            target.armorInventoryList
                .filter { !it.isEmpty && it.isItemStackDamageable }
                .maxByOrNull { it.itemDamage }
                ?.let {
                    (it.maxDamage - it.itemDamage) * 100 / it.maxDamage
                }
        }) ?: 100

    private fun SafeClientEvent.countValidCrystal(): Int {
        var count = 0
        CombatManager.target?.let {
            val eyePos = player.getPositionEyes(1f)

            if (placeSync) {
                for ((_, pair) in placedBBMap) {
                    val pos = pair.first.center.subtract(0.0, 1.0, 0.0)
                    if (pos.distanceTo(eyePos) > placeRange) continue
                    val damage = calcCrystalDamage(pos, it)
                    val selfDamage = calcCrystalDamage(pos, player)
                    if (!checkDamagePlace(damage, selfDamage)) continue
                    count++
                }
            }

            for ((crystal, pair) in crystalMap) {
                if (ignoredList.contains(crystal)) continue
                if (!checkDamagePlace(pair.first, pair.second)) continue
                if (crystal.positionVector.distanceTo(eyePos) > placeRange) continue
                if (!checkYawSpeed(getRotationToEntity(crystal).x)) continue
                count++
            }
        }
        return count
    }
    /* End of general */

    /* Rotation */
    private fun checkYawSpeed(yaw: Float): Boolean {
        val yawDiff = abs(RotationUtils.normalizeAngle(yaw - PlayerPacketManager.serverSideRotation.x))
        return yawDiffList.sum() + yawDiff <= maxYawSpeed
    }

    private fun SafeClientEvent.getLastRotation() =
        getRotationTo(lastLookAt)

    private fun resetRotation() {
        lastLookAt = CombatManager.target?.positionVector ?: Vec3d.ZERO
    }
    /* End of rotation */
}