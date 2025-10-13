package baritone.utils.pathing;

import baritone.Baritone;
import baritone.api.utils.BetterBlockPos;
import baritone.pathing.movement.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Evaluates terrain safety for pathfinding and mining decisions.
 */
public final class HazardAnalyzer {

    private HazardAnalyzer() {
    }

    public static double calculateHazardPenalty(CalculationContext ctx, BlockPos pos) {
        int radius = Math.max(1, Baritone.settings().hazardScanRadius.value);
        double multiplier = Math.max(0D, Baritone.settings().hazardWeightMultiplier.value);
        double penalty = 0D;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    mutable.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = ctx.get(mutable);
                    penalty += hazardValue(state, mutable, ctx);
                }
            }
        }
        return penalty * multiplier;
    }

    public static boolean isUnsafeToMine(CalculationContext ctx, BetterBlockPos target) {
        int radius = Math.max(1, Baritone.settings().hazardScanRadius.value);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(target.x + dx, target.y + dy, target.z + dz);
                    BlockState state = ctx.get(mutable);
                    if (isFluidHazard(state) || isThermalHazard(state)) {
                        return true;
                    }
                    if (dy >= 0 && isCascadingHazard(ctx, mutable, state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static double hazardValue(BlockState state, BlockPos pos, CalculationContext ctx) {
        if (isFluidHazard(state)) {
            return 3.0D;
        }
        if (isThermalHazard(state)) {
            return 2.0D;
        }
        if (isCascadingHazard(ctx, pos, state)) {
            return 1.5D;
        }
        return 0D;
    }

    private static boolean isFluidHazard(BlockState state) {
        FluidState fluidState = state.getFluidState();
        return fluidState.isSource() && (fluidState.is(FluidTags.LAVA) || fluidState.is(FluidTags.WATER));
    }

    private static boolean isThermalHazard(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.LAVA || block == Blocks.MAGMA_BLOCK || block == Blocks.FIRE || block == Blocks.SOUL_FIRE
            || block == Blocks.CAMPFIRE || block == Blocks.SOUL_CAMPFIRE;
    }

    private static boolean isCascadingHazard(CalculationContext ctx, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof FallingBlock && isAirOrFluid(ctx, pos.below())) {
            return true;
        }
        return false;
    }

    private static boolean isAirOrFluid(CalculationContext ctx, BlockPos pos) {
        BlockState below = ctx.get(pos);
        return below.isAir() || !below.getFluidState().isEmpty();
    }
}
