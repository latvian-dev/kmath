package dev.latvian.mods.klib.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.SimilarityCheck;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public record Rotation(float yaw, float pitch, float roll, Type type) {
	public enum Type {
		DEG(KMath.TO_DEG, 360D),
		RAD(KMath.TO_RAD, Math.PI * 2D);

		public final double scaled;
		public final float scalef;
		public final double fulld;
		public final float fullf;
		public final Rotation none;

		Type(double scaled, double fulld) {
			this.scaled = scaled;
			this.scalef = (float) scaled;
			this.fulld = fulld;
			this.fullf = (float) fulld;
			this.none = new Rotation(0F, 0F, 0F, this);
		}

		public Rotation of(float yaw, float pitch, float roll) {
			return yaw == 0F && pitch == 0F && roll == 0F ? none : new Rotation(yaw, pitch, roll, this);
		}

		public Rotation of(float yaw, float pitch) {
			return yaw == 0F && pitch == 0F ? none : new Rotation(yaw, pitch, 0F, this);
		}

		public Rotation of(float yaw) {
			return yaw == 0F ? none : new Rotation(yaw, 0F, 0F, this);
		}
	}

	public static final Codec<Rotation> CODEC = Codec.either(Codec.FLOAT, Codec.FLOAT.listOf()).xmap(
		either -> either.map(Rotation::deg, list -> switch (list.size()) {
			case 1 -> deg(list.get(0));
			case 2 -> deg(list.get(0), list.get(1));
			case 3 -> deg(list.get(0), list.get(1), list.get(2));
			default -> throw new IllegalArgumentException("Invalid Rotation list size: " + list.size());
		}),
		r -> {
			if (r.pitch == 0F && r.roll == 0F) {
				return Either.left(r.yawDeg());
			} else if (r.roll == 0F) {
				return Either.right(List.of(r.yawDeg(), r.pitchDeg()));
			} else {
				return Either.right(List.of(r.yawDeg(), r.pitchDeg(), r.rollDeg()));
			}
		}
	);

	public static final StreamCodec<ByteBuf, Rotation> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, Rotation::yawDeg,
		ByteBufCodecs.FLOAT, Rotation::pitchDeg,
		ByteBufCodecs.FLOAT, Rotation::rollDeg,
		Rotation::deg
	);

	public static final StreamCodec<ByteBuf, Rotation> STREAM_CODEC_NO_ROLL = StreamCodec.composite(
		ByteBufCodecs.FLOAT, Rotation::yawDeg,
		ByteBufCodecs.FLOAT, Rotation::pitchDeg,
		Rotation::deg
	);

	public static final DataType<Rotation> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Rotation.class);

	public static final Rotation NONE = new Rotation(0F, 0F, 0F, Type.RAD);

	public static Rotation deg(float yaw, float pitch, float roll) {
		return yaw == 0F && pitch == 0F && roll == 0F ? NONE : new Rotation(yaw, pitch, roll, Type.DEG);
	}

	public static Rotation deg(float yaw, float pitch) {
		return yaw == 0F && pitch == 0F ? NONE : new Rotation(yaw, pitch, 0F, Type.DEG);
	}

	public static Rotation deg(float yaw) {
		return yaw == 0F ? NONE : new Rotation(yaw, 0F, 0F, Type.DEG);
	}

	public static Rotation deg(Vec2 rotation) {
		return deg(rotation.y, rotation.x);
	}

	public static Rotation rad(float yaw, float pitch, float roll) {
		return yaw == 0F && pitch == 0F && roll == 0F ? NONE : new Rotation(yaw, pitch, roll, Type.RAD);
	}

	public static Rotation rad(float yaw, float pitch) {
		return yaw == 0F && pitch == 0F ? NONE : new Rotation(yaw, pitch, 0F, Type.RAD);
	}

	public static Rotation rad(float yaw) {
		return yaw == 0F ? NONE : new Rotation(yaw, 0F, 0F, Type.RAD);
	}

	public static Rotation of(@Nullable Entity entity, float delta) {
		return entity == null ? NONE : deg(entity.getViewYRot(delta), entity.getViewXRot(delta));
	}

	public static Rotation compute(Vec3 source, Vec3 target, float roll) {
		double dx = target.x() - source.x();
		double dy = target.y() - source.y();
		double dz = target.z() - source.z();

		double yaw = Math.atan2(dz, dx) - KMath.HALF_PI;
		double pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));

		return rad((float) yaw, (float) pitch, roll);
	}

	public static Rotation compute(Vec3 source, Vec3 target) {
		return compute(source, target, 0F);
	}

	public float yawDeg() {
		return type == Type.DEG ? yaw : yaw * KMath.F_TO_DEG;
	}

	public float pitchDeg() {
		return type == Type.DEG ? pitch : pitch * KMath.F_TO_DEG;
	}

	public float rollDeg() {
		return type == Type.DEG ? roll : roll * KMath.F_TO_DEG;
	}

	public float yawRad() {
		return type == Type.RAD ? yaw : yaw * KMath.F_TO_RAD;
	}

	public float pitchRad() {
		return type == Type.RAD ? pitch : pitch * KMath.F_TO_RAD;
	}

	public float rollRad() {
		return type == Type.RAD ? roll : roll * KMath.F_TO_RAD;
	}

	public Rotation toDeg() {
		return this == NONE || type == Type.DEG ? this : new Rotation(yaw * KMath.F_TO_DEG, pitch * KMath.F_TO_DEG, roll * KMath.F_TO_DEG, Type.DEG);
	}

	public Rotation toRad() {
		return this == NONE || type == Type.RAD ? this : new Rotation(yaw * KMath.F_TO_RAD, pitch * KMath.F_TO_RAD, roll * KMath.F_TO_RAD, Type.RAD);
	}

	public Rotation lerp(float delta, Rotation to) {
		if (type == to.type) {
			return type.of(
				KMath.lerp(delta, yaw, to.yaw),
				KMath.lerp(delta, pitch, to.pitch),
				KMath.lerp(delta, roll, to.roll)
			);
		} else {
			return deg(
				KMath.lerp(delta, yawDeg(), to.yawDeg()),
				KMath.lerp(delta, pitchDeg(), to.pitchDeg()),
				KMath.lerp(delta, rollDeg(), to.rollDeg())
			);
		}
	}

	public Vec3 lookVec3(double dist) {
		float p = pitchRad();
		float y = -yawRad();
		float yc = Mth.cos(y);
		float ys = Mth.sin(y);
		float pc = Mth.cos(p);
		float ps = Mth.sin(p);
		return new Vec3(ys * pc * dist, -ps * dist, yc * pc * dist);
	}

	public Vec3f lookVec3f(float dist) {
		float p = pitchRad();
		float y = -yawRad();
		float yc = Mth.cos(y);
		float ys = Mth.sin(y);
		float pc = Mth.cos(p);
		float ps = Mth.sin(p);
		return new Vec3f(ys * pc * dist, -ps * dist, yc * pc * dist);
	}

	@Override
	public String toString() {
		if (roll == 0F) {
			if (pitch == 0F) {
				return "Rotation[" + yawDeg() + "]";
			}

			return "Rotation[" + yawDeg() + ", " + pitchDeg() + "]";
		}

		return "Rotation[" + yawDeg() + ", " + pitchDeg() + ", " + rollDeg() + "]";
	}

	public boolean isNone() {
		return yaw == 0F && pitch == 0F && roll == 0F;
	}

	public boolean isYawOnly() {
		return yaw != 0F && pitch == 0F && roll == 0F;
	}

	public Matrix4f rotateYXZ(Matrix4f mat) {
		if (isYawOnly()) {
			return mat.rotateY(yawRad());
		} else {
			return mat.rotateYXZ(yawRad(), pitchRad(), rollRad());
		}
	}

	public Matrix3f rotateYXZ(Matrix3f mat) {
		if (isYawOnly()) {
			return mat.rotateY(yawRad());
		} else {
			return mat.rotateYXZ(yawRad(), pitchRad(), rollRad());
		}
	}

	public Matrix4f rotateZXY(Matrix4f mat) {
		if (isYawOnly()) {
			return mat.rotateY(yawRad());
		} else {
			mat.rotateZ(rollRad());
			mat.rotateX(pitchRad());
			mat.rotateY(yawRad());
			return mat;
		}
	}

	public Matrix3f rotateZXY(Matrix3f mat) {
		if (isYawOnly()) {
			return mat.rotateY(yawRad());
		} else {
			mat.rotateZ(rollRad());
			mat.rotateX(pitchRad());
			mat.rotateY(yawRad());
			return mat;
		}
	}

	public boolean isSimilar(Rotation other) {
		return SimilarityCheck.areFloatsSimilar(yawDeg(), other.yawDeg())
			&& SimilarityCheck.areFloatsSimilar(pitchDeg(), other.pitchDeg())
			&& SimilarityCheck.areFloatsSimilar(rollDeg(), other.rollDeg());
	}
}
