import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { debounce, throttle } from '@/utils/debounce'

describe('debounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should debounce function calls', () => {
    const fn = vi.fn()
    const debouncedFn = debounce(fn, 300)

    debouncedFn()
    debouncedFn()
    debouncedFn()

    expect(fn).not.toHaveBeenCalled()

    vi.advanceTimersByTime(300)

    expect(fn).toHaveBeenCalledTimes(1)
  })

  it('should pass arguments to debounced function', () => {
    const fn = vi.fn()
    const debouncedFn = debounce(fn, 300)

    debouncedFn('arg1', 'arg2')

    vi.advanceTimersByTime(300)

    expect(fn).toHaveBeenCalledWith('arg1', 'arg2')
  })

  it('should cancel debounced function', () => {
    const fn = vi.fn()
    const debouncedFn = debounce(fn, 300)

    debouncedFn()
    debouncedFn.cancel()

    vi.advanceTimersByTime(300)

    expect(fn).not.toHaveBeenCalled()
  })
})

describe('throttle', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should throttle function calls', () => {
    const fn = vi.fn()
    const throttledFn = throttle(fn, 300)

    throttledFn()
    throttledFn()
    throttledFn()

    // First call should execute immediately
    expect(fn).toHaveBeenCalledTimes(1)

    // Subsequent calls should be throttled
    vi.advanceTimersByTime(100)
    throttledFn()
    expect(fn).toHaveBeenCalledTimes(1)

    vi.advanceTimersByTime(200)
    throttledFn()
    expect(fn).toHaveBeenCalledTimes(2)
  })

  it('should cancel throttled function', () => {
    const fn = vi.fn()
    const throttledFn = throttle(fn, 300)

    throttledFn()
    throttledFn.cancel()

    vi.advanceTimersByTime(300)

    expect(fn).toHaveBeenCalledTimes(1)
  })
})
