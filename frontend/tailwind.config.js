/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#202820',
        moss: '#2f6848',
        canvas: '#f7f8f5',
        line: '#e7ebe4',
      },
      boxShadow: {
        card: '0 8px 28px rgba(32, 40, 32, 0.045)',
      },
    },
  },
  plugins: [],
}
