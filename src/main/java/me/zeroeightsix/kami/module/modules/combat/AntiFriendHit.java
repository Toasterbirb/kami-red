package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.ClientPlayerAttackEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;

@Module.Info(name = "AntiFriendHit", description = "Don't hit your friends", category = Module.Category.COMBAT, alwaysListening = true)
public class AntiFriendHit extends Module {


    @EventHandler
    Listener<ClientPlayerAttackEvent> listener = new Listener<>(event -> {
        if (!this.isEnabled()) return;
        Entity e = mc.objectMouseOver.entityHit;
        if (e instanceof EntityOtherPlayerMP && Friends.isFriend(e.getName())) {
            event.cancel();
        }
    });


}
