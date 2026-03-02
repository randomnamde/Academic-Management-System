/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['IBM Plex Sans', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', 'sans-serif']
      },
      colors: {
        primary: {
          900: '#0F2742',
          800: '#163454',
          700: '#1E4266',
          600: '#2A527A'
        },
        slatex: {
          900: '#1E2938',
          700: '#334155',
          600: '#475569',
          500: '#64748B'
        },
        neutralx: {
          200: '#E2E8F0',
          100: '#F1F5F9',
          50: '#F8FAFC'
        },
        panel: '#FCFDFE',
        state: {
          info: '#2563EB',
          success: '#15803D',
          warn: '#B45309',
          danger: '#B91C1C'
        }
      },
      borderRadius: {
        sm: '6px',
        md: '8px'
      },
      boxShadow: {
        pop: '0 1px 2px rgba(15, 23, 42, 0.08)'
      },
      transitionDuration: {
        180: '180ms'
      }
    }
  },
  plugins: []
}
