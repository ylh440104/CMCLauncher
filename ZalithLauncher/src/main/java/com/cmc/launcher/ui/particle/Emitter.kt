package com.cmc.launcher.ui.particle

import androidx.compose.ui.graphics.drawscope.DrawScope

internal abstract class Emitter(
    private val particleConfigData: ParticleConfigData
) {

    val particlePool = mutableListOf<Particle>()

    abstract fun generateParticles(numberOfParticles: Int)

    fun addParticle() {
        val particle = createFreshParticle()
        particlePool.add(particle)
    }

    private fun createFreshParticle(): Particle {
        return Particle(
            initialX = particleConfigData.x,
            initialY = particleConfigData.y,
            color = particleConfigData.particleColor.getExactColor(),
            size = particleConfigData.particleSize.getExactSize(),
            velocity = particleConfigData.velocity.createVelocityVector(),
            acceleration = particleConfigData.acceleration.createAccelerationVector(),
            lifetime = particleConfigData.lifeTime.maxLife,
            agingFactor = particleConfigData.lifeTime.agingFactor,
        )
    }

    fun applyForce(force: Vector2D) {
        for (particle in particlePool) {
            particle.applyForce(force)
        }
    }

    abstract fun update(dt: Float)

    fun render(drawScope: DrawScope) {
        for (particle in particlePool) {
            particle.show(drawScope)
        }
    }
}

internal class ParticleExplodeEmitter(
    numberOfParticles: Int,
    particleConfigData: ParticleConfigData
) : Emitter(particleConfigData) {
    init {
        generateParticles(numberOfParticles)
    }

    override fun generateParticles(numberOfParticles: Int) {
        repeat(numberOfParticles) { addParticle() }
    }

    override fun update(dt: Float) {
        for (particle in particlePool) {
            particle.update(dt)
        }
        particlePool.removeAll { it.finished() }
    }
}