package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.misc.AutoTool;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 * Updated by S-B99 on 06/04/20
 */
@Module.Info(name = "Aura", category = Module.Category.COMBAT, description = "Hits entities around you")
public class Aura extends Module {
    private Setting<Boolean> attackPlayers = register(Settings.b("Players", true));
    private Setting<Boolean> attackMobs = register(Settings.b("Mobs", false));
    private Setting<Boolean> attackAnimals = register(Settings.b("Animals", false));
    private Setting<Double> hitRange = register(Settings.d("Hit Range", 5.5d));
    private Setting<Boolean> ignoreWalls = register(Settings.b("Ignore Walls", true));
    private Setting<HitMode> prefer = register(Settings.e("Prefer", HitMode.SWORD));
    private Setting<Boolean> autoTool = register(Settings.b("Auto Tool", true));
    private Setting<WaitMode> delayMode = register(Settings.e("Mode", WaitMode.DELAY));
    private Setting<Boolean> autoSpamDelay = register(Settings.booleanBuilder("Auto Spam Delay").withValue(true).withVisibility(v -> delayMode.getValue().equals(WaitMode.SPAM)).build());
    private Setting<Double> waitTick = register(Settings.doubleBuilder("Spam Delay").withMinimum(0.1).withValue(2.0).withMaximum(20.0).withVisibility(v -> !autoSpamDelay.getValue() && delayMode.getValue().equals(WaitMode.SPAM)).build());
    private Setting<Boolean> infoMsg = register(Settings.booleanBuilder("Info Message").withValue(true).withVisibility(v -> false));

    private int waitCounter;

    public enum HitMode { SWORD, AXE, NONE }
    private enum WaitMode { DELAY, SPAM }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        if (autoSpamDelay.getValue() && infoMsg.getValue()) {
            infoMsg.setValue(false);
            Command.sendWarningMessage("[Aura] When Auto Tick Delay is turned on whatever you give Tick Delay doesn't matter, it uses the current TPS instead");
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.player.isDead) return;

        double autoWaitTick = 0;

        if (autoSpamDelay.getValue()) {
            autoWaitTick = 20 - (Math.round(LagCompensator.INSTANCE.getTickRate() * 10) / 10.0);
        }

        boolean shield = mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == EnumHand.OFF_HAND;

        if (mc.player.isHandActive() && !shield) {
            return;
        }

        if (delayMode.getValue().equals(WaitMode.DELAY)) {
            if (mc.player.getCooledAttackStrength(getLagComp()) < 1) {
                return;
            } else if (mc.player.ticksExisted % 2 != 0) {
                return;
            }
        }

        if (autoSpamDelay.getValue()) {
            if (delayMode.getValue().equals(WaitMode.SPAM) && autoWaitTick > 0) {
                if (waitCounter < autoWaitTick) {
                    waitCounter++;
                    return;
                } else {
                    waitCounter = 0;
                }
            }
        } else {
            if (delayMode.getValue().equals(WaitMode.SPAM) && waitTick.getValue() > 0) {
                if (waitCounter < waitTick.getValue()) {
                    waitCounter++;
                    return;
                } else {
                    waitCounter = 0;
                }
            }
        }

        for (Entity target : mc.world.loadedEntityList) {
            if (!EntityUtil.isLiving(target))
                continue;
            if (target == mc.player)
                continue;
            if (mc.player.getDistance(target) > hitRange.getValue())
                continue;
            if (((EntityLivingBase) target).getHealth() <= 0)
                continue;
            if (delayMode.getValue().equals(WaitMode.DELAY) && ((EntityLivingBase) target).hurtTime != 0)
                continue;
            if (!ignoreWalls.getValue() && (!mc.player.canEntityBeSeen(target) && !canEntityFeetBeSeen(target)))
                continue; // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed

            if (attackPlayers.getValue() && target instanceof EntityPlayer && !Friends.isFriend(target.getName())) {
                attack(target);
                return;
            } else {
                if (EntityUtil.isPassive(target) ? attackAnimals.getValue() : (EntityUtil.isMobAggressive(target) && attackMobs.getValue())) {
                    if ((autoTool.getValue())) {
                        AutoTool.equipBestWeapon(prefer.getValue());
                    }
                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe) {
                        attack(target);
                    }
                    return;
                }
            }
        }
    }

    private void attack(Entity e) {
        mc.playerController.attackEntity(mc.player, e);
        mc.player.swingArm(EnumHand.MAIN_HAND);

    }

    private float getLagComp() {
        if (delayMode.getValue().equals(WaitMode.DELAY)) {
            return -(20 - LagCompensator.INSTANCE.getTickRate());
        }
        return 0.0F;
    }

    private boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }
}
