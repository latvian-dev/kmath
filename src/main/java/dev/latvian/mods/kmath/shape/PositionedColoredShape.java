package dev.latvian.mods.kmath.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.codec.KMathCodecs;
import dev.latvian.mods.kmath.codec.KMathStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record PositionedColoredShape(Vec3 position, ColoredShape shape) {
	public static final Codec<PositionedColoredShape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KMathCodecs.VEC3.fieldOf("position").forGetter(PositionedColoredShape::position),
		ColoredShape.CODEC.fieldOf("shape").forGetter(PositionedColoredShape::shape)
	).apply(instance, PositionedColoredShape::new));

	public static final StreamCodec<ByteBuf, PositionedColoredShape> STREAM_CODEC = StreamCodec.composite(
		KMathStreamCodecs.VEC3, PositionedColoredShape::position,
		ColoredShape.STREAM_CODEC, PositionedColoredShape::shape,
		PositionedColoredShape::new
	);
}
