/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./public/index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['IBM Plex Sans', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', 'sans-serif'],
        display: ['IBM Plex Serif', 'Source Han Serif SC', 'Noto Serif SC', 'serif']
      },
      colors: {
        brand: {
          50: '#f1f9f8',
          100: '#dff0ee',
          200: '#bfe1dd',
          300: '#8cc8c1',
          400: '#54a9a0',
          500: '#2f8f86',
          600: '#24736d',
          700: '#1f5d59',
          800: '#1d4b48',
          900: '#1a3f3d'
        },
        ink: {
          50: '#f4f7f9',
          100: '#e9eef2',
          200: '#cdd9e2',
          300: '#a5b9c8',
          400: '#7894a9',
          500: '#5b788e',
          600: '#476076',
          700: '#3a4e61',
          800: '#334252',
          900: '#2e3947'
        }
      },
      boxShadow: {
        soft: '0 10px 26px rgba(20, 68, 80, 0.12)',
        panel: '0 16px 32px rgba(18, 52, 66, 0.16)'
      },
      borderRadius: {
        xl2: '1.1rem'
      }
    }
  },
  plugins: []
}
