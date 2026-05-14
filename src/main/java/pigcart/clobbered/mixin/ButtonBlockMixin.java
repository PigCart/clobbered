package pigcart.clobbered.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.clobbered.LobbedItem;

import static net.minecraft.world.level.block.ButtonBlock.POWERED;

@Mixin(ButtonBlock.class)
public abstract class ButtonBlockMixin {

    @Shadow
    @Final
    private BlockSetType type;

    @Shadow
    protected abstract void updateNeighbours(BlockState state, Level level, BlockPos pos);

    @Shadow
    protected abstract void playSound(@Nullable Player player, LevelAccessor level, BlockPos pos, boolean pressed);

    @Shadow
    @Final
    private int ticksToStayPressed;

    @Inject(method = "checkPressed", at = @At("HEAD"), cancellable = true)
    public void checkImpaledByItem(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        /*LobbedItem firstItem = this.type.canButtonBeActivatedByArrows()
                ? level.getEntitiesOfClass(LobbedItem.class, state.getShape(level, pos).bounds().move(pos)).stream().findFirst().orElse(null)
                : null;
        boolean shouldBePressed = firstItem != null && firstItem.isImpalingBlock();
        boolean wasPressed = state.getValue(POWERED);
        if (shouldBePressed != wasPressed) {
            level.setBlock(pos, state.setValue(POWERED, shouldBePressed), 3);
            this.updateNeighbours(state, level, pos);
            this.playSound(null, level, pos, shouldBePressed);
            level.gameEvent(firstItem, shouldBePressed ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        }

        if (shouldBePressed) {
            level.scheduleTick(new BlockPos(pos), (ButtonBlock)(Object)this, this.ticksToStayPressed);
            ci.cancel();
        }*/
    }
}
