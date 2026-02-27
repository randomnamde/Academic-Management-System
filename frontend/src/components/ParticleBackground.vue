<template>
  <div class="particle-background" aria-hidden="true">
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
    <div class="aurora aurora-left"></div>
    <div class="aurora aurora-right"></div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const canvasRef = ref(null)

const PARTICLE_COUNT = 72
const PARTICLE_SPEED = 0.24
const LINK_DISTANCE = 140
const POINTER_RADIUS = 190
const POINTER_FORCE = 0.012
const MAX_SPEED = 0.8

let ctx = null
let width = 0
let height = 0
let dpr = 1
let frameId = null
let particles = []
let reducedMotion = false
const pointer = {
  x: 0,
  y: 0,
  active: false
}

const createParticle = () => {
  const baseAlpha = Math.random() * 0.5 + 0.25
  return {
    x: Math.random() * width,
    y: Math.random() * height,
    vx: (Math.random() - 0.5) * PARTICLE_SPEED,
    vy: (Math.random() - 0.5) * PARTICLE_SPEED,
    radius: Math.random() * 1.8 + 0.5,
    baseAlpha,
    alpha: baseAlpha
  }
}

const applyPointerForce = (particle) => {
  if (!pointer.active) {
    particle.alpha = Math.max(particle.baseAlpha, particle.alpha - 0.01)
    return
  }

  const dx = pointer.x - particle.x
  const dy = pointer.y - particle.y
  const distance = Math.sqrt(dx * dx + dy * dy) || 1

  if (distance < POINTER_RADIUS) {
    const influence = 1 - distance / POINTER_RADIUS
    particle.vx += (dx / distance) * influence * POINTER_FORCE
    particle.vy += (dy / distance) * influence * POINTER_FORCE
    particle.alpha = Math.min(0.92, particle.baseAlpha + influence * 0.55)
  } else {
    particle.alpha = Math.max(particle.baseAlpha, particle.alpha - 0.01)
  }
}

const drawPointerEffect = () => {
  if (!pointer.active) return

  const pointerGradient = ctx.createRadialGradient(pointer.x, pointer.y, 0, pointer.x, pointer.y, POINTER_RADIUS * 0.85)
  pointerGradient.addColorStop(0, 'rgba(209, 194, 255, 0.18)')
  pointerGradient.addColorStop(1, 'rgba(209, 194, 255, 0)')

  ctx.beginPath()
  ctx.arc(pointer.x, pointer.y, POINTER_RADIUS * 0.85, 0, Math.PI * 2)
  ctx.fillStyle = pointerGradient
  ctx.fill()

  for (let i = 0; i < particles.length; i += 1) {
    const p = particles[i]
    const dx = pointer.x - p.x
    const dy = pointer.y - p.y
    const distance = Math.sqrt(dx * dx + dy * dy)

    if (distance < LINK_DISTANCE) {
      const opacity = (1 - distance / LINK_DISTANCE) * 0.22
      ctx.beginPath()
      ctx.moveTo(pointer.x, pointer.y)
      ctx.lineTo(p.x, p.y)
      ctx.strokeStyle = `rgba(203, 189, 255, ${opacity})`
      ctx.lineWidth = 0.8
      ctx.stroke()
    }
  }
}

const resizeCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  dpr = window.devicePixelRatio || 1
  width = window.innerWidth
  height = window.innerHeight

  canvas.width = Math.floor(width * dpr)
  canvas.height = Math.floor(height * dpr)
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`

  if (!ctx) {
    ctx = canvas.getContext('2d')
  }

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  particles = Array.from({ length: PARTICLE_COUNT }, createParticle)
}

const drawFrame = () => {
  if (!ctx) return

  ctx.clearRect(0, 0, width, height)

  for (let i = 0; i < particles.length; i += 1) {
    const p = particles[i]
    applyPointerForce(p)

    p.vx *= 0.994
    p.vy *= 0.994
    p.vx = Math.max(Math.min(p.vx, MAX_SPEED), -MAX_SPEED)
    p.vy = Math.max(Math.min(p.vy, MAX_SPEED), -MAX_SPEED)

    p.x += p.vx
    p.y += p.vy

    if (p.x < 0 || p.x > width) p.vx *= -1
    if (p.y < 0 || p.y > height) p.vy *= -1

    p.x = Math.min(Math.max(p.x, 0), width)
    p.y = Math.min(Math.max(p.y, 0), height)

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(197, 185, 255, ${p.alpha})`
    ctx.fill()
  }

  for (let i = 0; i < particles.length; i += 1) {
    for (let j = i + 1; j < particles.length; j += 1) {
      const p1 = particles[i]
      const p2 = particles[j]
      const dx = p1.x - p2.x
      const dy = p1.y - p2.y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < LINK_DISTANCE) {
        const opacity = (1 - distance / LINK_DISTANCE) * 0.22
        ctx.beginPath()
        ctx.moveTo(p1.x, p1.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.strokeStyle = `rgba(189, 173, 255, ${opacity})`
        ctx.lineWidth = 0.8
        ctx.stroke()
      }
    }
  }

  drawPointerEffect()

  frameId = window.requestAnimationFrame(drawFrame)
}

const drawStatic = () => {
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)
  for (let i = 0; i < particles.length; i += 1) {
    const p = particles[i]
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(197, 185, 255, ${p.alpha})`
    ctx.fill()
  }
}

const handleResize = () => {
  resizeCanvas()
  if (reducedMotion) {
    drawStatic()
  }
}

const handlePointerMove = (event) => {
  pointer.x = event.clientX
  pointer.y = event.clientY
  pointer.active = true
}

const handlePointerLeave = () => {
  pointer.active = false
}

onMounted(() => {
  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  resizeCanvas()

  if (reducedMotion) {
    drawStatic()
  } else {
    drawFrame()
  }

  window.addEventListener('resize', handleResize)
  window.addEventListener('pointermove', handlePointerMove, { passive: true })
  window.addEventListener('pointerleave', handlePointerLeave)
})

onUnmounted(() => {
  if (frameId) {
    window.cancelAnimationFrame(frameId)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerleave', handlePointerLeave)
})
</script>

<style scoped lang="scss">
.particle-background {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 20%, rgba(151, 124, 255, 0.25), transparent 45%),
    radial-gradient(circle at 88% 12%, rgba(190, 156, 255, 0.2), transparent 38%),
    linear-gradient(130deg, #080613 0%, #1b1535 55%, #2a1f4f 100%);
}

.particle-canvas {
  width: 100%;
  height: 100%;
  opacity: 0.88;
}

.aurora {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  animation: floatGlow 14s ease-in-out infinite;
}

.aurora-left {
  width: 36vw;
  height: 36vw;
  left: -10vw;
  bottom: -12vw;
  background: rgba(150, 120, 255, 0.45);
}

.aurora-right {
  width: 30vw;
  height: 30vw;
  right: -8vw;
  top: -6vw;
  background: rgba(202, 160, 255, 0.38);
  animation-delay: -6s;
}

@keyframes floatGlow {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(0, -14px, 0) scale(1.08);
  }
}

@media (max-width: 768px) {
  .aurora-left,
  .aurora-right {
    width: 48vw;
    height: 48vw;
  }
}
</style>
