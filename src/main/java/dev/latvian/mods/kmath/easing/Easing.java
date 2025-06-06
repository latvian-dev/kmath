package dev.latvian.mods.kmath.easing;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.KMath;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * <a href="https://easings.net/">Source</a>
 */
public enum Easing implements EasingFunction, StringRepresentable {
	MIN("min", x -> 0D),
	MAX("max", x -> 1D),
	HALF("half", x -> 0.5D),
	LINEAR("linear", x -> x),
	SMOOTHSTEP("smoothstep", KMath::smoothstep),
	ISMOOTHSTEP("ismoothstep", KMath::ismoothstep),
	SMOOTHERSTEP("smootherstep", KMath::smootherstep),

	SINE_IN("sine_in", x -> 1 - Math.cos((x * Math.PI) / 2)),
	SINE_OUT("sine_out", x -> Math.sin((x * Math.PI) / 2)),
	SINE_IN_OUT("sine_in_out", x -> -(Math.cos(Math.PI * x) - 1) / 2),

	QUAD_IN("quad_in", x -> x * x),
	QUAD_OUT("quad_out", x -> 1 - (1 - x) * (1 - x)),
	QUAD_IN_OUT("quad_in_out", x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2),

	CUBIC_IN("cubic_in", x -> x * x * x),
	CUBIC_OUT("cubic_out", x -> 1 - Math.pow(1 - x, 3)),
	CUBIC_IN_OUT("cubic_in_out", x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2D),

	QUART_IN("quart_in", x -> x * x * x * x),
	QUART_OUT("quart_out", x -> 1 - Math.pow(1 - x, 4)),
	QUART_IN_OUT("quart_in_out", x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2D),

	QUINT_IN("quint_in", x -> x * x * x * x * x),
	QUINT_OUT("quint_out", x -> 1 - Math.pow(1 - x, 5)),
	QUINT_IN_OUT("quint_in_out", x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2D),

	EXPO_IN("expo_in", x -> x == 0 ? 0 : Math.pow(2, 10 * x - 10)),
	EXPO_OUT("expo_out", x -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x)),
	EXPO_IN_OUT("expo_in_out", x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2),

	CIRC_IN("circ_in", x -> 1 - Math.sqrt(1 - x * x)),
	CIRC_OUT("circ_out", x -> Math.sqrt(1 - (x - 1) * (x - 1))),
	CIRC_IN_OUT("circ_in_out", x -> x < 0.5 ? (1 - Math.sqrt(1 - 4 * x * x)) / 2 : (Math.sqrt(1 - (-2 * x + 2) * (-2 * x + 2)) + 1) / 2),

	BACK_IN("back_in", x -> x * x * (2.70158 * x - 1.70158)),
	BACK_OUT("back_out", x -> 1 - (1 - x) * (1 - x) * (2.70158 * (1 - x) - 1.70158)),
	BACK_IN_OUT("back_in_out", x -> x < 0.5 ? Math.pow(2 * x, 2) * ((2.5949095 + 1) * 2 * x - 2.5949095) / 2 : (Math.pow(2 * x - 2, 2) * ((2.5949095 + 1) * (x * 2 - 2) + 2.5949095) + 2) / 2),

	ELASTIC_IN("elastic_in", x -> Math.sin(13 * Math.PI / 2 * x) * Math.pow(2, 10 * x - 10)),
	ELASTIC_OUT("elastic_out", x -> Math.sin(-13 * Math.PI / 2 * (x + 1)) * Math.pow(2, -10 * x) + 1),
	ELASTIC_IN_OUT("elastic_in_out", x -> x < 0.5 ? Math.sin(13 * Math.PI / 2 * (2 * x)) * Math.pow(2, 10 * (2 * x) - 10) / 2 : Math.sin(-13 * Math.PI / 2 * (2 * x - 1)) * Math.pow(2, -10 * (2 * x - 1)) / 2 + 1),

	BOUNCE_OUT("bounce_out", x -> {
		if (x < 1 / 2.75) {
			return 7.5625 * x * x;
		} else if (x < 2 / 2.75) {
			return 7.5625 * (x -= 1.5 / 2.75) * x + 0.75;
		} else if (x < 2.5 / 2.75) {
			return 7.5625 * (x -= 2.25 / 2.75) * x + 0.9375;
		} else {
			return 7.5625 * (x -= 2.625 / 2.75) * x + 0.984375;
		}
	}),

	BOUNCE_IN("bounce_in", x -> 1 - BOUNCE_OUT.ease(1 - x)),
	BOUNCE_IN_OUT("bounce_in_out", x -> x < 0.5 ? BOUNCE_IN.ease(x * 2) / 2 : BOUNCE_OUT.ease(x * 2 - 1) / 2 + 0.5),

	;

	public static final Easing[] VALUES = values();
	public static final Codec<Easing> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, Easing> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> VALUES[i], Easing::ordinal);

	public final String name;
	public final EasingFunction function;

	Easing(String name, EasingFunction function) {
		this.name = name;
		this.function = function;
	}

	@Override
	public double ease(double x) {
		return function.ease(x);
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
