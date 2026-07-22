package dev.bluerotor.sentocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SteamParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private final float initialSize;

    protected SteamParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(
                level,
                x,
                y,
                z
        );

        this.sprites = sprites;

        /*
         * サーバー側から指定された速度を初速度として使用します。
         */
        this.xd = xSpeed;
        this.yd = Math.max(
                0.012D,
                ySpeed
        );
        this.zd = zSpeed;

        /*
         * 重力の影響を受けないようにします。
         */
        this.gravity = 0.0F;

        /*
         * ブロックなどとの衝突処理は行いません。
         * 湯気が縁に引っ掛かったり下へ押し戻されたりするのを防ぎます。
         */
        this.hasPhysics = false;

        /*
         * 湯気が存在する時間です。
         * 40～59tick、約2～3秒です。
         */
        this.lifetime =
                40 + level.random.nextInt(20);

        /*
         * 初期サイズを少しランダム化します。
         */
        this.initialSize =
                0.18F
                        + level.random.nextFloat() * 0.08F;

        this.quadSize = initialSize;

        /*
         * テクスチャの色を白のまま表示します。
         */
        this.setColor(
                1.0F,
                1.0F,
                1.0F
        );

        this.alpha = 0.75F;

        /*
         * 初回描画前にテクスチャを設定します。
         */
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        /*
         * 前フレームの位置を保存します。
         */
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        /*
         * 寿命に達したら削除します。
         */
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        /*
         * 常にわずかな上向き加速を加えます。
         */
        this.yd += 0.00035D;

        /*
         * 自然な横揺れを加えます。
         */
        this.xd +=
                (this.random.nextDouble() - 0.5D)
                        * 0.00045D;

        this.zd +=
                (this.random.nextDouble() - 0.5D)
                        * 0.00045D;

        /*
         * 速度に従って移動します。
         */
        this.move(
                this.xd,
                this.yd,
                this.zd
        );

        /*
         * 横方向の速度を徐々に弱めます。
         * 上方向は維持し、下向きにならないようにします。
         */
        this.xd *= 0.96D;
        this.yd *= 0.985D;
        this.zd *= 0.96D;

        if (this.yd < 0.006D) {
            this.yd = 0.006D;
        }

        float progress =
                (float) this.age
                        / (float) this.lifetime;

        /*
         * 時間経過に伴って少しずつ膨らませます。
         */
        this.quadSize =
                initialSize
                        * (1.0F + progress * 1.4F);

        /*
         * 最初は柔らかく現れ、
         * 後半で徐々に透明になります。
         */
        if (progress < 0.15F) {
            this.alpha =
                    0.75F
                            * (progress / 0.15F);
        } else {
            float fadeProgress =
                    (progress - 0.15F) / 0.85F;

            this.alpha =
                    0.75F
                            * (1.0F - fadeProgress);
        }

        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
     * SteamParticleを生成するクライアント側Providerです。
     */
    public static class Provider
            implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(
                SpriteSet sprites
        ) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new SteamParticle(
                    level,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    sprites
            );
        }
    }
}