<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script setup lang="ts">
// app.vue - 应用入口
</script>

<style>
/* Google Fonts: Nunito Sans (Friendly UI) + Fira Code (Mono) */
@import url('https://fonts.googleapis.com/css2?family=Nunito+Sans:wght@300;400;500;600;700;800&family=Fira+Code:wght@400;500;600;700&display=swap');

/* CSS Variables - Soft UI Design System Tokens */
:root {
  /* Primary Colors - Friendly Blue */
  --color-primary: #4F46E5;
  --color-primary-light: #818CF8;
  --color-primary-lighter: #C7D2FE;
  --color-primary-lightest: #EEF2FF;
  --color-on-primary: #FFFFFF;

  /* Secondary - Warm Purple */
  --color-secondary: #7C3AED;
  --color-secondary-light: #A78BFA;

  /* Accent - Friendly Green */
  --color-accent: #10B981;
  --color-accent-light: #6EE7B7;
  --color-accent-lightest: #D1FAE5;

  /* Warm Amber for CTAs */
  --color-warm: #F59E0B;
  --color-warm-light: #FCD34D;

  /* Surface Colors - Soft Neutrals */
  --color-background: #F3F4F6;
  --color-surface: #FFFFFF;
  --color-surface-elevated: #FFFFFF;
  --color-surface-muted: #F9FAFB;

  /* Text Colors */
  --color-foreground: #1F2937;
  --color-foreground-muted: #6B7280;
  --color-foreground-subtle: #9CA3AF;

  /* Border Colors */
  --color-border: #E5E7EB;
  --color-border-light: #F3F4F6;

  /* Status Colors - Soft & Approachable */
  --color-success: #10B981;
  --color-success-light: #D1FAE5;
  --color-warning: #F59E0B;
  --color-warning-light: #FEF3C7;
  --color-error: #EF4444;
  --color-error-light: #FEE2E2;
  --color-info: #3B82F6;
  --color-info-light: #DBEAFE;

  /* Soft UI Shadows - The key to Soft UI */
  --shadow-soft-sm: 3px 3px 6px #D1D5DB, -3px -3px 6px #FFFFFF;
  --shadow-soft-md: 6px 6px 12px #D1D5DB, -6px -6px 12px #FFFFFF;
  --shadow-soft-lg: 8px 8px 16px #D1D5DB, -8px -8px 16px #FFFFFF;
  --shadow-soft-xl: 12px 12px 24px #D1D5DB, -12px -12px 24px #FFFFFF;

  /* Inset shadows for pressed state */
  --shadow-inset-sm: inset 2px 2px 4px #D1D5DB, inset -2px -2px 4px #FFFFFF;
  --shadow-inset-md: inset 4px 4px 8px #D1D5DB, inset -4px -4px 8px #FFFFFF;

  /* Subtle drop shadow for depth */
  --shadow-drop-sm: 0 2px 4px rgba(0, 0, 0, 0.05);
  --shadow-drop-md: 0 4px 8px rgba(0, 0, 0, 0.08);
  --shadow-drop-lg: 0 8px 16px rgba(0, 0, 0, 0.1);

  /* Spacing & Radius */
  --radius-sm: 12px;
  --radius-md: 16px;
  --radius-lg: 20px;
  --radius-xl: 24px;
  --radius-full: 9999px;

  /* Transitions */
  --transition-fast: 150ms ease;
  --transition-normal: 250ms ease;
  --transition-slow: 350ms ease;
}

/* Dark Mode */
.dark {
  --color-background: #1F2937;
  --color-surface: #374151;
  --color-surface-elevated: #4B5563;
  --color-surface-muted: #374151;

  --color-foreground: #F9FAFB;
  --color-foreground-muted: #D1D5DB;
  --color-foreground-subtle: #9CA3AF;

  --color-border: #4B5563;
  --color-border-light: #374151;

  --color-primary-light: #818CF8;
  --color-primary-lighter: #6366F1;

  /* Soft UI Shadows for Dark Mode */
  --shadow-soft-sm: 3px 3px 6px #1F2937, -3px -3px 6px #4B5563;
  --shadow-soft-md: 6px 6px 12px #1F2937, -6px -6px 12px #4B5563;
  --shadow-soft-lg: 8px 8px 16px #1F2937, -8px -8px 16px #4B5563;
  --shadow-soft-xl: 12px 12px 24px #1F2937, -12px -12px 24px #4B5563;

  --shadow-inset-sm: inset 2px 2px 4px #1F2937, inset -2px -2px 4px #4B5563;
  --shadow-inset-md: inset 4px 4px 8px #1F2937, inset -4px -4px 8px #4B5563;

  --color-success-light: rgba(16, 185, 129, 0.15);
  --color-warning-light: rgba(245, 158, 11, 0.15);
  --color-error-light: rgba(239, 68, 68, 0.15);
  --color-info-light: rgba(59, 130, 246, 0.15);
}

/* Global Base Styles */
html,
body,
#__nuxt {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* Typography - Friendly & Approachable */
body {
  font-family: 'Nunito Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

button,
input,
textarea,
select {
  font-family: inherit;
}

/* Typography Scale */
h1, h2, h3, h4, h5, h6 {
  font-family: 'Nunito Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  color: var(--color-foreground);
}

code, pre, .mono {
  font-family: 'Fira Code', 'Consolas', monospace;
}

/* Soft UI Utility Classes */
.soft-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft-md);
  border: 1px solid var(--color-border-light);
  transition: all var(--transition-normal);
}

.soft-card:hover {
  box-shadow: var(--shadow-soft-lg);
  transform: translateY(-2px);
}

.soft-card-flat {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-drop-md);
  border: 1px solid var(--color-border-light);
}

.soft-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  font-size: 0.9375rem;
  font-weight: 600;
  border-radius: var(--radius-full);
  background: var(--color-surface);
  color: var(--color-foreground);
  box-shadow: var(--shadow-soft-sm);
  border: none;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.soft-button:hover {
  box-shadow: var(--shadow-soft-md);
  transform: translateY(-1px);
}

.soft-button:active {
  box-shadow: var(--shadow-inset-sm);
  transform: translateY(0);
}

.soft-button-primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  color: var(--color-on-primary);
  box-shadow: var(--shadow-drop-md), 0 4px 12px rgba(79, 70, 229, 0.3);
}

.soft-button-primary:hover {
  box-shadow: var(--shadow-drop-lg), 0 8px 20px rgba(79, 70, 229, 0.4);
}

.soft-button-accent {
  background: linear-gradient(135deg, var(--color-accent) 0%, #059669 100%);
  color: white;
  box-shadow: var(--shadow-drop-md), 0 4px 12px rgba(16, 185, 129, 0.3);
}

.soft-input {
  background: var(--color-surface);
  border: none;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-inset-sm);
  padding: 0.75rem 1rem;
  font-size: 0.9375rem;
  color: var(--color-foreground);
  transition: all var(--transition-normal);
}

.soft-input:focus {
  outline: none;
  box-shadow: var(--shadow-inset-sm), 0 0 0 3px var(--color-primary-lighter);
}

/* Stat Cards */
.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-soft-md);
  border: 1px solid var(--color-border-light);
  padding: 1.5rem;
  transition: all var(--transition-normal);
}

.stat-card:hover {
  box-shadow: var(--shadow-soft-lg);
  transform: translateY(-4px);
}

/* Icon Containers - Soft & Friendly */
.icon-soft {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-soft-sm);
  transition: all var(--transition-normal);
}

.icon-soft-primary {
  background: linear-gradient(135deg, var(--color-primary-lightest) 0%, var(--color-primary-lighter) 100%);
  color: var(--color-primary);
}

.icon-soft-success {
  background: linear-gradient(135deg, var(--color-success-light) 0%, var(--color-accent-lightest) 100%);
  color: var(--color-success);
}

.icon-soft-warning {
  background: linear-gradient(135deg, var(--color-warning-light) 0%, #FEF3C7 100%);
  color: var(--color-warning);
}

.icon-soft-error {
  background: linear-gradient(135deg, var(--color-error-light) 0%, #FECACA 100%);
  color: var(--color-error);
}

.icon-soft-info {
  background: linear-gradient(135deg, var(--color-info-light) 0%, #BFDBFE 100%);
  color: var(--color-info);
}

/* Badges - Soft & Rounded */
.badge-soft {
  display: inline-flex;
  align-items: center;
  padding: 0.375rem 0.875rem;
  font-size: 0.8125rem;
  font-weight: 600;
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-drop-sm);
}

.badge-soft-primary {
  background: var(--color-primary-lightest);
  color: var(--color-primary);
}

.badge-soft-success {
  background: var(--color-success-light);
  color: var(--color-success);
}

.badge-soft-warning {
  background: var(--color-warning-light);
  color: var(--color-warning);
}

.badge-soft-error {
  background: var(--color-error-light);
  color: var(--color-error);
}

.badge-soft-info {
  background: var(--color-info-light);
  color: var(--color-info);
}

.badge-soft-neutral {
  background: var(--color-surface-muted);
  color: var(--color-foreground-muted);
}

/* Progress Bars - Soft */
.progress-soft {
  height: 10px;
  background: var(--color-border);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-inset-sm);
  overflow: hidden;
}

.progress-soft-fill {
  height: 100%;
  border-radius: var(--radius-full);
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  transition: width var(--transition-slow);
}

.progress-soft-fill-success {
  background: linear-gradient(90deg, var(--color-accent) 0%, var(--color-accent-light) 100%);
}

.progress-soft-fill-warning {
  background: linear-gradient(90deg, var(--color-warning) 0%, var(--color-warm-light) 100%);
}

/* Panels */
.panel-soft {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-soft-md);
  border: 1px solid var(--color-border-light);
}

/* Background Pattern */
.bg-soft-gradient {
  background: linear-gradient(135deg, var(--color-primary-lightest) 0%, var(--color-background) 50%, var(--color-accent-lightest) 100%);
}

/* Decorative Elements */
.blob-soft {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  animation: float 20s ease-in-out infinite;
}

.blob-soft-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, var(--color-secondary-light) 100%);
  top: -100px;
  left: -100px;
}

.blob-soft-2 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, var(--color-accent-light) 0%, var(--color-warm-light) 100%);
  bottom: -100px;
  right: -100px;
  animation-delay: -10s;
}

.blob-soft-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, var(--color-info-light) 0%, var(--color-primary-lighter) 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(20px, -20px) scale(1.05); }
  66% { transform: translate(-15px, 15px) scale(0.95); }
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}

.animate-slide-up {
  animation: slideUp 0.4s ease-out forwards;
}

.stagger-1 { animation-delay: 0.05s; }
.stagger-2 { animation-delay: 0.1s; }
.stagger-3 { animation-delay: 0.15s; }
.stagger-4 { animation-delay: 0.2s; }
.stagger-5 { animation-delay: 0.25s; }

/* Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--color-surface-muted);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--color-foreground-subtle);
}

/* Focus States */
*:focus-visible {
  outline: 3px solid var(--color-primary-lighter);
  outline-offset: 2px;
}

/* Reduced Motion */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
