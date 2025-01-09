package fbanna.easyminigame.mixin;


import net.minecraft.resource.*;
import net.minecraft.util.path.SymlinkFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(VanillaDataPackProvider.class)
public class MixinDataPack {


    @Inject(method = "createManager(Ljava/nio/file/Path;Lnet/minecraft/util/path/SymlinkFinder;)Lnet/minecraft/resource/ResourcePackManager;", at = @At(value = "HEAD"), cancellable = true)
    private static void inject(Path dataPacksPath, SymlinkFinder symlinkFinder, CallbackInfoReturnable<ResourcePackManager> cir) {
        ResourcePackProvider provider = new FileResourcePackProvider(Path.of("./easyminigame/datapacktest/"), ResourceType.SERVER_DATA, ResourcePackSource.WORLD, symlinkFinder);

        cir.setReturnValue(

                new ResourcePackManager(
                        new VanillaDataPackProvider(symlinkFinder),
                        new FileResourcePackProvider(dataPacksPath, ResourceType.SERVER_DATA, ResourcePackSource.WORLD, symlinkFinder),
                        provider
                        )

        );
    }

}

