/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['IBM Plex Sans', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', 'sans-serif'],
        display: ['IBM Plex Serif', 'Noto Serif SC', 'Songti SC', 'serif']
      },
      colors: {
        white: 'var(--color-white)',
        black: 'var(--color-black)',
        primary: {
          900: 'var(--color-primary-900)',
          800: 'var(--color-primary-800)',
          700: 'var(--color-primary-700)',
          600: 'var(--color-primary-600)'
        },
        slatex: {
          900: 'var(--color-slate-900)',
          700: 'var(--color-slate-700)',
          600: 'var(--color-slate-600)',
          500: 'var(--color-slate-500)'
        },
        neutralx: {
          200: 'var(--color-neutral-200)',
          100: 'var(--color-neutral-100)',
          50: 'var(--bg-base)'
        },
        panel: 'var(--color-panel)',
        glass: 'var(--panel-glass)',
        state: {
          info: 'var(--color-info)',
          success: 'var(--color-success)',
          warn: 'var(--color-warn)',
          danger: 'var(--color-danger)'
        },
        semantic: {
          bg: 'var(--bg-base)',
          elevated: 'var(--bg-elevated)',
          text: 'var(--text-primary)',
          muted: 'var(--text-secondary)',
          accent: 'var(--accent-500)'
        }
      },
      borderRadius: {
        sm: 'var(--radius-control)',
        md: 'var(--radius-panel)',
        lg: 'var(--radius-pop)'
      },
      boxShadow: {
        soft: 'var(--shadow-soft)',
        panel: 'var(--shadow-panel)',
        pop: 'var(--shadow-overlay)'
      },
      transitionDuration: {
        180: '180ms',
        220: '220ms',
        320: '320ms'
      }
    }
  },
  plugins: []
}

