package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.data.GuiEditManager;
import at.hannibal2.skyhanni.utils.shader.WorldSphereIntersectRender;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "updateCameraAndRender", at = @At("TAIL"))
    private void onLastRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        GuiEditManager.renderLast();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupFog(IF)V", ordinal = 2))
    private void renderWorldPassStart1(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        WorldSphereIntersectRender.INSTANCE.start();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;alphaFunc(IF)V", ordinal = 0))
    private void renderWorldPassEnd1(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        WorldSphereIntersectRender.INSTANCE.end();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;shadeModel(I)V", ordinal = 2))
    private void renderWorldPassStart2(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        WorldSphereIntersectRender.INSTANCE.start();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;shadeModel(I)V", ordinal = 3))
    private void renderWorldPassEnd2(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        WorldSphereIntersectRender.INSTANCE.end();
    }
}
