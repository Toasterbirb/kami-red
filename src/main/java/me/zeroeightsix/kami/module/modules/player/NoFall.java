package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

//import java.util.concurrent.TimeUnit;

/**
 * Created by 086 on 19/11/2017.
 * Updated by S-B99 on 24/10/2019
 */
@Module.Info(category = Module.Category.PLAYER, description = "Prevents fall damage", name = "NoFall")
public class NoFall extends Module {

	private Setting<Boolean> packet = register(Settings.b("Packet", false));
	private Setting<Boolean> bucket = register(Settings.b("Bucket", true));
	private Setting<Integer> distance = register(Settings.i("Distance", 1));

	private long last = 0;

	@EventHandler
	public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
		if (event.getPacket() instanceof CPacketPlayer && packet.getValue()) {
			((CPacketPlayer) event.getPacket()).onGround = true;
		}
	});

	@Override
	public void onUpdate() {
		if (bucket.getValue() && mc.player.fallDistance >= distance.getValue() && !EntityUtil.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
			Vec3d posVec = mc.player.getPositionVector();
			RayTraceResult result = mc.world.rayTraceBlocks(posVec, posVec.add(0, -5.33f, 0), true, true, false);
			if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
				EnumHand hand = EnumHand.MAIN_HAND;
				if (mc.player.getHeldItemOffhand().getItem() == Items.WATER_BUCKET) hand = EnumHand.OFF_HAND;
				else if (mc.player.getHeldItemMainhand().getItem() != Items.WATER_BUCKET) {
					for (int i = 0; i < 9; i++)
						if (mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
							mc.player.inventory.currentItem = i;
							mc.player.rotationPitch = 90;
							last = System.currentTimeMillis();
							return;
						}
					return;
				}

				mc.player.rotationPitch = 90;
				mc.playerController.processRightClick(mc.player, mc.world, hand);

				// this is where i want to run the above 2 lines again after 300 milliseconds 

				// this was tried individually
				// result: forgot but it was either a crash or lag
				//TimeUnit.MILLISECONDS.sleep(400);

				// result: lag thread
				//long lastNanoTime = System.nanoTime();
				//long nowTime = System.nanoTime();
				//while(nowTime/1000000 - lastNanoTime /1000000 < 300 )
				//{
				//	nowTime = System.nanoTime();
				//	System.out.println("KAMI: Tried to pick up bucket");
				//	mc.player.rotationPitch = 90;
				//	mc.playerController.processRightClick(mc.player, mc.world, hand);

				//}  

				// this was tried individually
				// result: freeze
				//Thread.sleep(300);

				// this was tried individually
				// result: clean exit
				//wait(300);

				//mc.player.rotationPitch = 90;
				//mc.playerController.processRightClick(mc.player, mc.world, hand);
			}
		}
	}
}
