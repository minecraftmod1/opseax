package com.opsec.mixin;

import com.opsec.OpsecConfig;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLoginNetworkHandler.class, priority = 500)
public abstract class LoginQueryMixin {
    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onQueryRequest", at = @At("HEAD"), cancellable = true, require = 0)
    private void opsec$emptyAnswer(LoginQueryRequestS2CPacket packet, CallbackInfo ci) {
        if (!OpsecConfig.get().blockLoginQueries) return;
        this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId(), null));
        ci.cancel();
    }
}
