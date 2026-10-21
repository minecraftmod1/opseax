package com.opsec.mixin;

import com.opsec.OpsecConfig;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(TranslatableTextContent.class)
public abstract class TranslatableTextContentMixin {
    @Shadow @Final private String key;
    @Shadow @Final private String fallback;

    @Inject(method = "visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true)
    private <T> void opsec$hide(StringVisitable.Visitor<T> visitor, CallbackInfoReturnable<Optional<T>> cir) {
        OpsecConfig cfg = OpsecConfig.get();
        if (cfg.hideModTranslations && cfg.shouldHideKey(key)) {
            cir.setReturnValue(visitor.accept(fallback != null ? fallback : key));
        }
    }
}
