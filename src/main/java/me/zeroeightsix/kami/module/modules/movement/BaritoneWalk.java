package me.zeroeightsix.kami.module.modules.movement;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import me.zeroeightsix.kami.module.Module;

import static me.zeroeightsix.kami.util.MathsUtils.normalizeAngle;

/**
 * Created by Dewy on the 17th of April, 2020
 */
@Module.Info(name = "BaritoneWalk", description = "AutoWalk with Baritone pathfinding.", category = Module.Category.MOVEMENT)
public class BaritoneWalk extends Module {
    
    private String direction;

    // Very shittily done, but this check is not that taxing on performance cos it is NOT performed every tick.
    @Override
    protected void onEnable() {
        if (normalizeAngle(mc.player.rotationYaw) >= -22.5 && normalizeAngle(mc.player.rotationYaw) <= 22.5) { // +Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX, (int) mc.player.posZ + 1068));

            direction = "+Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 22.6 && normalizeAngle(mc.player.rotationYaw) <= 67.5) { // -X / +Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - 1068, (int) mc.player.posZ + 1068));

            direction = "-X / +Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 67.6 && normalizeAngle(mc.player.rotationYaw) <= 112.5) { // -X
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - 1068, (int) mc.player.posZ));

            direction = "-X";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 112.6 && normalizeAngle(mc.player.rotationYaw) <= 157.5) { // -X / -Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - 1068, (int) mc.player.posZ - 1068));

            direction = "-X / -Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 157.6 || normalizeAngle(mc.player.rotationYaw) <= -157.5) { // -Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX, (int) mc.player.posZ - 1068));

            direction = "-Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= -157.6 && normalizeAngle(mc.player.rotationYaw) <= -112.5) { // +X / -Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + 1068, (int) mc.player.posZ - 1068));

            direction = "+X / -Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= -112.6 && normalizeAngle(mc.player.rotationYaw) <= -67.5) { // +X
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + 1068, (int) mc.player.posZ));

            direction = "+X";
        } else if (normalizeAngle(mc.player.rotationYaw) >= -67.6 && normalizeAngle(mc.player.rotationYaw) <= -22.6) { // +X / +Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + 1068, (int) mc.player.posZ + 1068));

            direction = "+X / +Z";
        }
    }

    @Override
    public String getHudInfo() {
        return direction;
    }

    @Override
    protected void onDisable() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
    }
}
