package nl.pim16aap2.animatedarchitecture.spigot.v1_19_R2;

import nl.pim16aap2.animatedarchitecture.core.api.IExecutor;
import nl.pim16aap2.animatedarchitecture.core.api.ILocation;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.AnimationContext;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlockFactory;
import nl.pim16aap2.animatedarchitecture.core.managers.AnimatedBlockHookManager;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.Animator;
import nl.pim16aap2.animatedarchitecture.core.util.Constants;
import nl.pim16aap2.animatedarchitecture.core.util.Util;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Dd;
import nl.pim16aap2.animatedarchitecture.spigot.util.SpigotAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * v1_19_R2 implementation of {@link IAnimatedBlockFactory}.
 *
 * @author Pim
 * @see IAnimatedBlockFactory
 */
@Singleton
public final class AnimatedBlockFactory implements IAnimatedBlockFactory
{
    private final AnimatedBlockHookManager animatedBlockHookManager;
    private final IExecutor executor;

    AnimatedBlockFactory(AnimatedBlockHookManager animatedBlockHookManager, IExecutor executor)
    {
        this.animatedBlockHookManager = animatedBlockHookManager;
        this.executor = executor;
    }

    @Override
    public Optional<IAnimatedBlock> create(
        ILocation loc, float radius, float startAngle, boolean bottom, boolean onEdge, AnimationContext context,
        Vector3Dd finalPosition, Animator.MovementMethod movementMethod)
        throws Exception
    {
        final Location spigotLocation = SpigotAdapter.getBukkitLocation(loc);
        final World bukkitWorld = Util.requireNonNull(spigotLocation.getWorld(), "Spigot world from location: " + loc);
        final Material material = spigotLocation.getBlock().getType();

        if (!BlockAnalyzer.isAllowedBlockStatic(material))
            return Optional.empty();

        final double offset = bottom ? 0.010_001 : 0;
        final ILocation spawnLoc = loc.add(0, offset, 0);

        final var animatedBlock = new CustomEntityFallingBlock(
            executor, loc.getWorld(), bukkitWorld, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), radius,
            startAngle, movementMethod, onEdge, context, animatedBlockHookManager, finalPosition);

        animatedBlock.b(CraftChatMessage.fromStringOrNull(Constants.ANIMATED_ARCHITECTURE_ENTITY_NAME));
        animatedBlock.n(false);
        return Optional.of(animatedBlock);
    }
}
