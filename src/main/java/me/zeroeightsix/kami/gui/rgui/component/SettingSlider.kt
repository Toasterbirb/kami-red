package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.settings.impl.number.FloatSetting
import me.zeroeightsix.kami.setting.settings.impl.number.IntegerSetting
import me.zeroeightsix.kami.setting.settings.impl.number.NumberSetting
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2f
import org.kamiblue.commons.utils.MathUtils
import org.lwjgl.input.Keyboard
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

class SettingSlider(val setting: NumberSetting<*>) : Slider(setting.name, 0.0, setting.description, setting.visibility) {

    private val range = setting.range.endInclusive.toDouble() - setting.range.start.toDouble()
    private val settingValueDouble get() = setting.value.toDouble()
    private val settingStep = if (setting.step.toDouble() > 0.0) setting.step else getDefaultStep()
    private val stepDouble = settingStep.toDouble()
    private val places = when (setting) {
        is IntegerSetting -> 1
        is FloatSetting -> MathUtils.decimalPlaces(settingStep.toFloat())
        else -> MathUtils.decimalPlaces(settingStep.toDouble())
    }

    private var preDragMousePos = Vec2f(0.0f, 0.0f)

    private fun getDefaultStep() = when (setting) {
        is IntegerSetting -> range / 20
        is FloatSetting -> range / 20.0f
        else -> range / 20.0
    }

    override fun onStopListening(success: Boolean) {
        if (success) {
            name.toDoubleOrNull()?.let { setting.setValue(it.toString()) }
        }

        super.onStopListening(success)
        name = originalName
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        if (buttonId == 0) {
            preDragMousePos = mousePos
            updateValue(mousePos)
        }
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) {
            if (!listening) {
                listening = true
                name = setting.value.toString()
                value = 0.0
            } else {
                onStopListening(false)
            }
        } else if (buttonId == 0 && listening) {
            onStopListening(true)
        }
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        if (!listening && buttonId == 0) updateValue(mousePos)
    }

    private fun updateValue(mousePos: Vec2f) {
        value = if (!Keyboard.isKeyDown(Keyboard.KEY_LMENU)) mousePos.x.toDouble() / width.toDouble()
        else (preDragMousePos.x + (mousePos.x - preDragMousePos.x) * 0.1) / width.toDouble()

        var roundedValue = MathUtils.round(round((value * range + setting.range.start.toDouble()) / stepDouble) * stepDouble, places)
        if (abs(roundedValue) == 0.0) roundedValue = 0.0
        setting.setValue(roundedValue.toString())
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        val typedChar = Keyboard.getEventCharacter()
        if (keyState) {
            when (keyCode) {
                Keyboard.KEY_RETURN -> {
                    onStopListening(true)
                }
                Keyboard.KEY_BACK, Keyboard.KEY_DELETE -> {
                    name = name.substring(0, max(name.length - 1, 0))
                    if (name.isBlank()) name = "0"
                }
                else -> if (isNumber(typedChar)) {
                    if (name == "0" && (typedChar.isDigit() || typedChar == '-')) {
                        name = ""
                    }
                    name += typedChar
                }
            }
        }
    }

    private fun isNumber(char: Char) =
        char.isDigit()
            || char == '-'
            || char == '.'
            || char.equals('e', true)

    override fun onTick() {
        super.onTick()
        if (mouseState != MouseState.DRAG && !listening) {
            val min = setting.range.start.toDouble()
            val flooredSettingValue = floor((settingValueDouble - min) / stepDouble) * stepDouble
            if (value * range + min !in (flooredSettingValue - stepDouble)..flooredSettingValue) {
                value = (setting.value.toDouble() - min) / range
            }
        }
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        val valueText = setting.toString()
        protectedWidth = FontRenderAdapter.getStringWidth(valueText, 0.75f).toDouble()

        super.onRender(vertexHelper, absolutePos)
        if (!listening) {
            val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
            val posY = renderHeight - 2.0f - FontRenderAdapter.getFontHeight(0.75f)
            FontRenderAdapter.drawString(valueText, posX, posY, color = GuiColors.text, scale = 0.75f)
        }
    }

}