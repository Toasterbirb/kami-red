package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.client.ActiveModules;
import me.zeroeightsix.kami.util.Wrapper;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.util.color.ColorConverter.toF;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 4/08/2017.
 * Updated by dominikaaaa on 20/03/19
 */
public class KamiActiveModulesUI extends AbstractComponentUI<me.zeroeightsix.kami.gui.kami.component.ActiveModules> {

    @Override
    public void renderComponent(me.zeroeightsix.kami.gui.kami.component.ActiveModules component, FontRenderer f) {
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);

        FontRenderer renderer = Wrapper.getFontRenderer();

        List<Module> mods = Arrays.stream(ModuleManager.getModules())
                .filter(Module::isEnabled)
                .filter(Module -> (ActiveModules.INSTANCE.getHidden().getValue() || Module.isOnArray()))
                .sorted(Comparator.comparing(module -> renderer.getStringWidth(module.name.getValue() + (module.getHudInfo() == null ? "" : module.getHudInfo() + " ")) * (component.sort_up ? -1 : 1)))
                .collect(Collectors.toList());


        final int[] y = {2};

        if (Wrapper.getPlayer() != null) {
            if (ActiveModules.INSTANCE.getPotion().getValue() && component.getParent().getY() < 26 && Wrapper.getPlayer().getActivePotionEffects().size() > 0 && component.getParent().getOpacity() == 0)
                y[0] = Math.max(component.getParent().getY(), 26 - component.getParent().getY());
        }

        final float[] hue = {(System.currentTimeMillis() % (360 * ActiveModules.INSTANCE.getRainbowSpeed())) / (360f * ActiveModules.INSTANCE.getRainbowSpeed())};

        Function<Integer, Integer> xFunc;
        switch (component.getAlignment()) {
            case RIGHT:
                xFunc = i -> component.getWidth() - i;
                break;
            case CENTER:
                xFunc = i -> component.getWidth() / 2 - i / 2;
                break;
            case LEFT:
            default:
                xFunc = i -> 0;
                break;
        }

        for (int i = 0; i < mods.size(); i++) {
            Module module = mods.get(i);
            int rgb;

            switch (ActiveModules.INSTANCE.getMode().getValue()) {
                case RAINBOW:
                    rgb = Color.HSBtoRGB(hue[0], toF(ActiveModules.INSTANCE.getSaturationR().getValue()), toF(ActiveModules.INSTANCE.getBrightnessR().getValue()));
                    break;
                case CATEGORY:
                    rgb = ActiveModules.INSTANCE.getCategoryColour(module);
                    break;
                case CUSTOM:
                    rgb = Color.HSBtoRGB(toF(ActiveModules.INSTANCE.getHueC().getValue()), toF(ActiveModules.INSTANCE.getSaturationC().getValue()), toF(ActiveModules.INSTANCE.getBrightnessC().getValue()));
                    break;
                case INFO_OVERLAY:
                    rgb = ActiveModules.INSTANCE.getInfoColour(i);
                    break;
                default:
                    rgb = 0;
            }

            String hudInfo = module.getHudInfo();
            String text = ActiveModules.INSTANCE.getAlignedText(module.name.getValue(), (hudInfo == null ? "" : KamiMod.colour + "7" + hudInfo + KamiMod.colour + "r"), component.getAlignment().equals(AlignedComponent.Alignment.RIGHT));
            int textWidth = renderer.getStringWidth(text);
            int textHeight = renderer.getFontHeight() + 1;
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            renderer.drawStringWithShadow(xFunc.apply(textWidth), y[0], red, green, blue, text);
            hue[0] += .02f;
            y[0] += textHeight;
        }

        component.setHeight(y[0]);

        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
    }

    @Override
    public void handleSizeComponent(me.zeroeightsix.kami.gui.kami.component.ActiveModules component) {
        component.setWidth(100);
        component.setHeight(100);
    }
}
