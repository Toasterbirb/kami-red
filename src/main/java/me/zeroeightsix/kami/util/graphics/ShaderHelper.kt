package me.zeroeightsix.kami.util.graphics


import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ResolutionUpdateEvent
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.client.shader.ShaderLinkHelper
import net.minecraft.util.ResourceLocation

class ShaderHelper(shaderIn: ResourceLocation, vararg frameBufferNames: String) {
    private val mc = Wrapper.minecraft

    val shader: ShaderGroup?
    private val frameBufferMap = HashMap<String, Framebuffer>()

    init {
        shader = if (OpenGlHelper.shadersSupported) {
            try {
                ShaderLinkHelper.setNewStaticShaderLinkHelper()
                ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, shaderIn).also {
                    it.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                }
            } catch (e: Exception) {
                KamiMod.log.warn("Failed loading Shader")
                e.printStackTrace()
                null
            }?.also {
                for (name in frameBufferNames) {
                    frameBufferMap[name] = it.getFramebufferRaw(name)
                }
            }
        } else {
            null
        }
    }

    @EventHandler
    private val listener = Listener(EventHook { event: ResolutionUpdateEvent ->
        shader?.createBindFramebuffers(event.width, event.height)
    })

    fun getFrameBuffer(name: String) = frameBufferMap[name]

    // We are putting it here so it can find the listener above
    init {
        KamiMod.EVENT_BUS.subscribe(this)
    }
}