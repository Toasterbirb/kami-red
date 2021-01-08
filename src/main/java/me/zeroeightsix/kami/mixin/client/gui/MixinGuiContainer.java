package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.gui.mc.KamiGuiStealButton;
import me.zeroeightsix.kami.module.modules.player.ChestStealer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiContainer.class)
public class MixinGuiContainer extends GuiScreen {

    @Shadow protected int guiLeft;
    @Shadow protected int guiTop;
    @Shadow protected int xSize;

    private final GuiButton stealButton = new KamiGuiStealButton(this.guiLeft + this.xSize + 2, this.guiTop + 2);

    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo ci) {
        if (ChestStealer.INSTANCE.isValidGui()) {
            this.buttonList.add(stealButton);
            ChestStealer.updateButton(stealButton, this.guiLeft, this.xSize, this.guiTop);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 696969) {
            ChestStealer.INSTANCE.setStealing(!ChestStealer.INSTANCE.getStealing());
        } else {
            super.actionPerformed(button);
        }
    }

    @Inject(method = "updateScreen", at = @At("HEAD"))
    public void updateScreen(CallbackInfo ci) {
        ChestStealer.updateButton(stealButton, this.guiLeft, this.xSize, this.guiTop);
    }

}
