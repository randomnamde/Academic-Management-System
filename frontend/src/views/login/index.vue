<template>
  <div class="login-entry min-h-screen px-4 py-10 md:px-6">
    <div class="aurora-layer" aria-hidden="true"></div>

    <main class="entry-shell mx-auto w-full max-w-4xl">
      <h1 class="entry-title" aria-label="教务管理平台">
        <span class="sr-only">教务管理平台</span>
        <span class="typing-text" aria-hidden="true">{{ displayTitle }}</span>
        <span class="typing-caret" aria-hidden="true">|</span>
      </h1>
      <p class="entry-subtitle">学而不思则罔，思而不学则殆</p>
      <AppButton ref="entryButtonRef" class="entry-cta" tone="accent" @click="openLoginCard">登录系统</AppButton>
    </main>

    <Transition name="login-overlay-fade">
      <div
        v-if="showLoginCard"
        ref="overlayRef"
        class="login-overlay fixed inset-0 z-50 flex items-center justify-center p-4"
        role="presentation"
        tabindex="-1"
        @click.self="closeLoginCard"
        @keydown="onOverlayKeydown"
      >
        <section
          ref="loginCardRef"
          class="login-card w-full max-w-md"
          role="dialog"
          aria-modal="true"
          aria-labelledby="login-dialog-title"
        >
          <button class="close-btn touch-target" aria-label="关闭登录弹窗" @click="closeLoginCard">×</button>
          <h2 id="login-dialog-title" class="login-title">登录系统</h2>
          <p class="login-tip">请输入账号和密码</p>

          <form class="mt-4 space-y-3" @submit.prevent="handleLogin">
            <div>
              <label for="login-username" class="mb-1 block text-[13px] font-medium text-slatex-700">用户名</label>
              <AppInput
                id="login-username"
                v-model="loginForm.username"
                name="username"
                autocomplete="username"
                placeholder="请输入用户名"
              />
            </div>
            <div>
              <label for="login-password" class="mb-1 block text-[13px] font-medium text-slatex-700">密码</label>
              <AppInput
                id="login-password"
                v-model="loginForm.password"
                name="password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
              />
            </div>
            <AppButton class="submit-btn" tone="accent" block :loading="loading" native-type="submit">登录</AppButton>
          </form>
        </section>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppButton from '@/components/ui/AppButton.vue'
import AppInput from '@/components/ui/AppInput.vue'

const store = useStore()
const router = useRouter()

const fullTitle = '教务管理平台'
const typingInterval = 110
const deletingInterval = 80
const completePause = 1500
const restartPause = 500

const loading = ref(false)
const showLoginCard = ref(false)
const overlayRef = ref(null)
const loginCardRef = ref(null)
const entryButtonRef = ref(null)
const previousActiveElement = ref(null)
const loginForm = reactive({ username: '', password: '' })

const typedText = ref('')
const isDeleting = ref(false)
const reduceMotion = ref(false)
let typingTimer = null
let motionMediaQuery = null
let motionMediaHandler = null

const displayTitle = computed(() => (reduceMotion.value ? fullTitle : typedText.value))

function getElementFromRef(target) {
  if (!target) return null
  return target.$el || target
}

function focusUsernameInput() {
  const card = getElementFromRef(loginCardRef.value)
  const usernameInput = card?.querySelector?.('#login-username')
  usernameInput?.focus?.()
}

function lockBodyScroll(lock) {
  document.body.style.overflow = lock ? 'hidden' : ''
}

function clearTypingTimer() {
  if (!typingTimer) return
  window.clearTimeout(typingTimer)
  typingTimer = null
}

function scheduleTyping(delay) {
  clearTypingTimer()
  typingTimer = window.setTimeout(stepTyping, delay)
}

function stepTyping() {
  if (reduceMotion.value) {
    typedText.value = fullTitle
    clearTypingTimer()
    return
  }

  if (!isDeleting.value) {
    const nextLength = typedText.value.length + 1
    typedText.value = fullTitle.slice(0, nextLength)
    if (typedText.value === fullTitle) {
      isDeleting.value = true
      scheduleTyping(completePause)
      return
    }
    scheduleTyping(typingInterval)
    return
  }

  const nextLength = Math.max(typedText.value.length - 1, 0)
  typedText.value = fullTitle.slice(0, nextLength)
  if (!typedText.value.length) {
    isDeleting.value = false
    scheduleTyping(restartPause)
    return
  }
  scheduleTyping(deletingInterval)
}

function initTyping() {
  if (reduceMotion.value) {
    typedText.value = fullTitle
    return
  }
  typedText.value = ''
  isDeleting.value = false
  scheduleTyping(typingInterval)
}

function openLoginCard() {
  previousActiveElement.value = document.activeElement
  showLoginCard.value = true
}

function closeLoginCard() {
  showLoginCard.value = false
}

function onOverlayKeydown(event) {
  if (event.key !== 'Escape') return
  event.preventDefault()
  closeLoginCard()
}

function validateLogin() {
  if (!loginForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return false
  }
  if (!loginForm.password.trim()) {
    ElMessage.error('请输入密码')
    return false
  }
  return true
}

async function handleLogin() {
  if (!validateLogin()) return

  loading.value = true
  try {
    await store.dispatch('login', loginForm)
    ElMessage.success('登录成功')
    showLoginCard.value = false
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

watch(showLoginCard, (open) => {
  if (open) {
    lockBodyScroll(true)
    nextTick(() => {
      overlayRef.value?.focus?.()
      focusUsernameInput()
    })
    return
  }

  lockBodyScroll(false)
  const target = getElementFromRef(previousActiveElement.value) || getElementFromRef(entryButtonRef.value)
  target?.focus?.()
})

onMounted(() => {
  motionMediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  motionMediaHandler = () => {
    reduceMotion.value = motionMediaQuery.matches
    clearTypingTimer()
    initTyping()
  }
  motionMediaHandler()
  motionMediaQuery.addEventListener('change', motionMediaHandler)
})

onBeforeUnmount(() => {
  lockBodyScroll(false)
  clearTypingTimer()
  if (motionMediaQuery && motionMediaHandler) {
    motionMediaQuery.removeEventListener('change', motionMediaHandler)
  }
  motionMediaQuery = null
  motionMediaHandler = null
})
</script>

<style scoped>
.login-entry {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  background: linear-gradient(148deg, color-mix(in srgb, var(--bg-base) 94%, transparent), var(--bg-elevated));
}

.login-entry::before,
.login-entry::after,
.aurora-layer {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  z-index: 0;
}

.login-entry::before,
.login-entry::after {
  content: '';
}

.login-entry::before {
  width: min(48vw, 560px);
  height: min(48vw, 560px);
  left: -12%;
  top: -16%;
  background: radial-gradient(circle, color-mix(in srgb, var(--accent-500) 42%, transparent) 0%, transparent 68%);
  filter: blur(1px);
  animation: glow-drift-a 9s ease-in-out infinite;
}

.login-entry::after {
  width: min(40vw, 470px);
  height: min(40vw, 470px);
  right: -10%;
  bottom: -18%;
  background: radial-gradient(circle, color-mix(in srgb, var(--success) 34%, transparent) 0%, transparent 68%);
  filter: blur(2px);
  animation: glow-drift-b 13s ease-in-out infinite;
}

.aurora-layer {
  width: min(44vw, 520px);
  height: min(44vw, 520px);
  left: 42%;
  top: 20%;
  transform: translateX(-50%);
  background: radial-gradient(
    ellipse at center,
    color-mix(in srgb, var(--accent-600) 36%, transparent) 0%,
    color-mix(in srgb, var(--success) 26%, transparent) 42%,
    transparent 74%
  );
  mix-blend-mode: screen;
  filter: blur(2px);
  animation: glow-drift-c 17s ease-in-out infinite;
}

.entry-shell {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  text-align: center;
}

.entry-shell > .entry-title,
.entry-shell > .entry-subtitle,
.entry-shell > .entry-cta {
  opacity: 0;
  transform: translateY(20px);
  filter: blur(6px);
  animation: entry-reveal 640ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.entry-shell > .entry-title {
  animation-delay: 0ms;
}

.entry-shell > .entry-subtitle {
  animation-delay: 120ms;
}

.entry-shell > .entry-cta {
  animation-delay: 240ms;
}

.entry-title {
  margin: 0;
  width: min(92vw, 900px);
  min-height: 1.3em;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  font-family: var(--font-display), var(--font-sans);
  font-size: clamp(38px, 8vw, 78px);
  letter-spacing: 0.08em;
  color: color-mix(in srgb, var(--text-primary) 96%, transparent);
  text-shadow:
    0 0 24px color-mix(in srgb, var(--accent-500) 30%, transparent),
    0 8px 30px color-mix(in srgb, var(--accent-700) 16%, transparent);
}

.typing-text {
  display: inline-block;
}

.typing-caret {
  display: inline-block;
  color: color-mix(in srgb, var(--accent-500) 100%, var(--color-white));
  text-shadow: 0 0 12px color-mix(in srgb, var(--accent-500) 58%, transparent);
  animation: caret-blink 560ms step-end infinite;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  margin: -1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

.entry-subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.entry-cta {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  min-width: 180px;
  min-height: 44px;
  border-radius: 999px;
  box-shadow: 0 14px 30px color-mix(in srgb, var(--accent-600) 36%, transparent);
  animation: cta-breathe 2.4s ease-in-out infinite;
  transition: transform 180ms ease, box-shadow 200ms ease, filter 180ms ease;
}

.entry-cta::after {
  content: '';
  position: absolute;
  top: -40%;
  left: -110%;
  width: 56%;
  height: 180%;
  background: linear-gradient(
    110deg,
    transparent 0%,
    color-mix(in srgb, var(--color-white) 62%, transparent) 48%,
    transparent 100%
  );
  transform: rotate(14deg);
  pointer-events: none;
  animation: cta-shimmer 3.6s ease-in-out infinite;
}

.entry-cta:hover,
.entry-cta:focus-visible {
  transform: translateY(-3px);
  filter: brightness(1.03);
  box-shadow:
    0 20px 40px color-mix(in srgb, var(--accent-600) 44%, transparent),
    0 0 0 4px color-mix(in srgb, var(--accent-500) 16%, transparent);
}

.entry-cta:active {
  transform: translateY(0);
}

.login-overlay {
  background: color-mix(in srgb, var(--color-black) 52%, transparent);
  backdrop-filter: blur(8px);
}

.login-card {
  position: relative;
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background: color-mix(in srgb, var(--surface-base) 92%, transparent);
  box-shadow: var(--shadow-overlay);
  padding: 22px 18px 18px;
  transform: translateY(0) scale(1);
  opacity: 1;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 76%, transparent);
  color: var(--text-secondary);
  font-size: 19px;
  line-height: 1;
  cursor: pointer;
  transition: background-color 180ms ease, transform 180ms ease, box-shadow 200ms ease, border-color 200ms ease;
}

.close-btn:hover {
  border-color: color-mix(in srgb, var(--accent-500) 34%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
  transform: translateY(-1px);
  box-shadow: 0 8px 18px color-mix(in srgb, var(--accent-500) 18%, transparent);
}

.close-btn:focus-visible {
  border-color: color-mix(in srgb, var(--accent-500) 46%, transparent);
  transform: translateY(-1px);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--accent-500) 18%, transparent);
}

.login-title {
  margin: 0;
  text-align: center;
  font-size: 25px;
  color: var(--text-primary);
}

.login-tip {
  margin: 6px 0 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.login-card :deep(.app-input) {
  min-height: 44px;
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.login-card :deep(.app-input:hover),
.login-card :deep(.app-input:focus-visible) {
  transform: translateY(-1px);
}

.submit-btn {
  min-height: 44px;
}

.login-overlay-fade-enter-active,
.login-overlay-fade-leave-active {
  transition: opacity 260ms ease;
}

.login-overlay-fade-enter-active .login-card,
.login-overlay-fade-leave-active .login-card {
  transition:
    transform 320ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 320ms cubic-bezier(0.22, 1, 0.36, 1);
}

.login-overlay-fade-enter-from,
.login-overlay-fade-leave-to {
  opacity: 0;
}

.login-overlay-fade-enter-from .login-card,
.login-overlay-fade-leave-to .login-card {
  opacity: 0;
  transform: translateY(20px) scale(0.92);
}

.login-overlay-fade-enter-to .login-card,
.login-overlay-fade-leave-from .login-card {
  opacity: 1;
  transform: translateY(0) scale(1);
}

@keyframes caret-blink {
  0%,
  45% {
    opacity: 1;
  }

  46%,
  100% {
    opacity: 0;
  }
}

@keyframes glow-drift-a {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.84;
  }

  50% {
    transform: translate3d(16%, 14%, 0) scale(1.25);
    opacity: 0.38;
  }
}

@keyframes glow-drift-b {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.76;
  }

  50% {
    transform: translate3d(-15%, -13%, 0) scale(1.2);
    opacity: 0.3;
  }
}

@keyframes glow-drift-c {
  0%,
  100% {
    transform: translate3d(-50%, 0, 0) scale(1);
    opacity: 0.62;
  }

  50% {
    transform: translate3d(-44%, -11%, 0) scale(1.26);
    opacity: 0.34;
  }
}

@keyframes entry-reveal {
  from {
    opacity: 0;
    transform: translateY(20px);
    filter: blur(6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}

@keyframes cta-breathe {
  0%,
  100% {
    box-shadow: 0 14px 30px color-mix(in srgb, var(--accent-600) 36%, transparent);
    filter: brightness(1);
  }

  50% {
    box-shadow: 0 22px 44px color-mix(in srgb, var(--accent-600) 52%, transparent);
    filter: brightness(1.08);
  }
}

@keyframes cta-shimmer {
  0%,
  72% {
    left: -115%;
    opacity: 0;
  }

  78% {
    opacity: 0.92;
  }

  100% {
    left: 132%;
    opacity: 0;
  }
}

@media (max-width: 640px) {
  .entry-shell {
    gap: 16px;
  }

  .entry-title {
    font-size: clamp(32px, 11vw, 52px);
    letter-spacing: 0.06em;
  }
}

@media (prefers-reduced-motion: reduce) {
  .typing-caret,
  .entry-cta,
  .login-entry::before,
  .login-entry::after,
  .aurora-layer,
  .entry-shell > .entry-title,
  .entry-shell > .entry-subtitle,
  .entry-shell > .entry-cta,
  .entry-cta::after {
    animation: none;
  }

  .typing-caret {
    opacity: 1;
    text-shadow: none;
  }

  .entry-shell > .entry-title,
  .entry-shell > .entry-subtitle,
  .entry-shell > .entry-cta {
    opacity: 1;
    transform: none;
    filter: none;
  }

  .login-entry::before,
  .login-entry::after,
  .aurora-layer {
    transform: none;
    opacity: 0.5;
  }

  .login-overlay-fade-enter-active,
  .login-overlay-fade-leave-active,
  .login-overlay-fade-enter-active .login-card,
  .login-overlay-fade-leave-active .login-card {
    transition-duration: 0.01ms !important;
  }

  .login-overlay-fade-enter-from .login-card,
  .login-overlay-fade-leave-to .login-card {
    transform: none;
    opacity: 1;
  }
}
</style>
