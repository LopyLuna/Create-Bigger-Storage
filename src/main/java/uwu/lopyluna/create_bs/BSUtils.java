package uwu.lopyluna.create_bs;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BSUtils {

    public static  <T>void returnMixin(T value, CallbackInfoReturnable<T> cir) {
        cir.setReturnValue(value);
        cir.cancel();
    }
}
