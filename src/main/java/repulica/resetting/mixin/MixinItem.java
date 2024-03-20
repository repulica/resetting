package repulica.resetting.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import repulica.resetting.ComponentManager;

@Mixin(Item.class)
public class MixinItem {

	@Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
	private void overrideComponents(CallbackInfoReturnable<ComponentMap> info) {
		if (ComponentManager.INSTANCE.hasOverrides((Item) (Object) this))
			info.setReturnValue(ComponentManager.INSTANCE.getComponents((Item) (Object) this));
	}

	@Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
	private void overrideMaxCount(CallbackInfoReturnable<Integer> info) {
		if (ComponentManager.INSTANCE.hasOverrides((Item) (Object) this))
			info.setReturnValue(ComponentManager.INSTANCE.getComponents((Item) (Object) this).getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1));
	}

}
