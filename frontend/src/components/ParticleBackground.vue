<template>
  <div class="particle-background" aria-hidden="true">
    <div class="bg-layer bg-mesh"></div>
    <div class="bg-layer bg-paper"></div>
    <div class="bg-layer bg-orb orb-left"></div>
    <div class="bg-layer bg-orb orb-right"></div>
  </div>
</template>

<style scoped lang="scss">
.particle-background {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background: linear-gradient(140deg, #f4f9f8 0%, #eef5f4 48%, #e7f0ef 100%);
}

.bg-layer {
  position: absolute;
  inset: 0;
}

.bg-mesh {
  background-image:
    linear-gradient(rgba(21, 97, 101, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(21, 97, 101, 0.07) 1px, transparent 1px);
  background-size: 58px 58px;
  mask-image: radial-gradient(circle at 50% 45%, rgba(0, 0, 0, 0.72), transparent 82%);
  opacity: 0.48;
}

.bg-paper {
  background:
    radial-gradient(circle at 15% 16%, rgba(201, 231, 228, 0.5), transparent 34%),
    radial-gradient(circle at 84% 26%, rgba(164, 216, 211, 0.4), transparent 32%),
    radial-gradient(circle at 70% 84%, rgba(196, 229, 224, 0.34), transparent 34%);
  opacity: 0.8;
}

.bg-orb {
  border-radius: 999px;
  filter: blur(72px);
  opacity: 0.42;
  animation: floatGlow 18s ease-in-out infinite;
}

.orb-left {
  width: 42vw;
  height: 42vw;
  left: -12vw;
  bottom: -14vw;
  background: rgba(128, 191, 181, 0.54);
}

.orb-right {
  width: 34vw;
  height: 34vw;
  right: -10vw;
  top: -10vw;
  background: rgba(145, 178, 214, 0.34);
  animation-delay: -7s;
}

@keyframes floatGlow {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(0, -18px, 0) scale(1.06);
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg-orb {
    animation: none;
  }
}

@media (max-width: 768px) {
  .bg-mesh {
    background-size: 46px 46px;
    opacity: 0.4;
  }

  .orb-left,
  .orb-right {
    width: 56vw;
    height: 56vw;
  }
}
</style>
