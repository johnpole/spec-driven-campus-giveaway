---
name: Campus Circular
colors:
  surface: '#fbf9f6'
  surface-dim: '#dbdad7'
  surface-bright: '#fbf9f6'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f0'
  surface-container: '#efeeeb'
  surface-container-high: '#eae8e5'
  surface-container-highest: '#e4e2df'
  on-surface: '#1b1c1a'
  on-surface-variant: '#5e3f3c'
  inverse-surface: '#30312f'
  inverse-on-surface: '#f2f0ed'
  outline: '#926e6b'
  outline-variant: '#e7bcb8'
  surface-tint: '#c00018'
  primary: '#a80013'
  on-primary: '#ffffff'
  primary-container: '#d6001c'
  on-primary-container: '#ffe6e3'
  inverse-primary: '#ffb3ac'
  secondary: '#5b5f64'
  on-secondary: '#ffffff'
  secondary-container: '#dde0e6'
  on-secondary-container: '#5f6369'
  tertiary: '#005f30'
  on-tertiary: '#ffffff'
  tertiary-container: '#007a3f'
  on-tertiary-container: '#a1ffb9'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad6'
  primary-fixed-dim: '#ffb3ac'
  on-primary-fixed: '#410003'
  on-primary-fixed-variant: '#930010'
  secondary-fixed: '#e0e2e9'
  secondary-fixed-dim: '#c3c6cd'
  on-secondary-fixed: '#181c21'
  on-secondary-fixed-variant: '#43474d'
  tertiary-fixed: '#8df9ac'
  tertiary-fixed-dim: '#71dc92'
  on-tertiary-fixed: '#00210d'
  on-tertiary-fixed-variant: '#005229'
  background: '#fbf9f6'
  on-background: '#1b1c1a'
  surface-variant: '#e4e2df'
typography:
  title-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  title-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 30px
    letterSpacing: -0.015em
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.01em
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-regular:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  body-medium:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0em
  label-meta:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-regular:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  micro:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

The design system embodies collegiate community, circular economy, and frictionless mobility. It addresses the hectic, transient nature of move-out and graduation seasons—where students must offload furniture, textbooks, mini-fridges, and gear rapidly, cleanly, and reliably.

### Personality & Demographics
- **Target Audience:** College students, campus residents, graduating seniors, and incoming freshmen navigating tight move-in/out windows.
- **Brand Attributes:** Friendly, trustworthy, sustainable, energetic, and subtly playful without sacrificing transaction speed or clarity.
- **Emotional Response:** High confidence in physical handoffs, neighborhood safety, and clear-eyed satisfaction in preventing usable goods from hitting campus dumpsters.

### Design Style
The system employs a contemporary **Tactile Minimalist** style tailored for mobile web. It balances crisp, warm-tinted surfaces with rich micro-elevations, high-contrast tap targets, and pill-shaped semantic chips. Whitespace is generous but calibrated for high-density scanning on small screens under varied outdoor lighting conditions (e.g., bright sunlight on the quad). Visual hierarchy is driven by saturated brand accents (Cherry Red) paired with distinct eco-indicators (Leaf Green) grounded on warm paper-like backdrops.

## Colors

The palette establishes an immediate sense of school spirit, collegiate vibrancy, and clear functional utility while sustaining high outdoor readability.

### Core Roles
- **Primary (`#D6001C` - RPI Cherry Red):** Directs primary user intent. Drives primary action buttons, active navigation states, unread message badges, and critical progress calls.
  - *Primary Dark/Hover:* `#B30017`
  - *Primary Soft/Container:* `#FDE8EA`
- **Secondary (`#1F2328` - Deep Charcoal):** Delivers optimal typographic contrast against pale surfaces. Avoids harsh pure blacks in favor of a warm, ink-dense tone.
  - *Secondary Muted / Supporting:* `#656D76`
  - *Subtle Border / Hairline Divider:* `#E8E5DF`
- **Tertiary / Eco Accent (`#2E9E5B` - Leaf Green):** Dedicated strictly to positive circularity metrics, verified peer tags, and the defining zero-cost indicator ("FREE" badges).
  - *Tertiary Soft/Container:* `#EAF7EE`
  - *Tertiary Border:* `#A7F3D0`
- **Canvas (`#FAF8F5` - Warm Off-White):** Reduces eye strain and outdoor glare while lending a tactile, physical paper foundation.
- **Surface (`#FFFFFF` - Pure White):** Used for elevated cards, bottom sheets, sticky action decks, and modals to pop against the canvas.

### Semantic Tiers
- **Pending / In Review:** Amber `#D97706` (Surface: `#FEF3C7`, Border: `#FDE68A`)
- **Available / Claimed:** Leaf Green `#2E9E5B` (Surface: `#EAF7EE`, Border: `#A7F3D0`)
- **Sold / Reserved / Ended:** Cherry Red `#D6001C` (Surface: `#FEE2E2`, Border: `#FECACA`)

## Typography

The typography scale balances geometric friendliness with rigorous structural readability.

### Pairing Strategy
- **Display & Headings (Plus Jakarta Sans):** Chosen for its rounded geometric forms, open counters, and confident modern air. It lends titles approachable authority.
- **Body & Functional Text (Inter):** High x-height and neutral, machined precision ensure dense listing details, chat threads, and location instructions remain razor-sharp at compact sizes.

### Usage Rules
- **Base Body Target:** Never set operational text below `16px` on form fields or descriptions to avoid unwanted iOS viewport zooming and ensure readability during active campus walks.
- **Micro Roles:** `12px` is reserved strictly for timestamps, secondary chip metadata, and status micro-labels.

## Layout & Spacing

A mobile-first, fluid grid designed for seamless single-handed navigation within the physical thumb zone.

### Grid & Breakpoints
- **Compact (Mobile: 0 - 599px):** 4-column fluid layout. Margins: `1rem` (16px), Gutters: `1rem` (16px). All key functional triggers are aligned to the lower two-thirds of the screen.
- **Medium (Tablet: 600 - 1023px):** 8-column fluid layout with an enclosed canvas container of max-width `720px`. Margins: `1.5rem` (24px).
- **Expanded (Desktop/Web View: ≥ 1024px):** Fixed application shell bounded at `480px` max-width centered on an off-white canvas backdrop, maintaining native app proportions on larger displays.

### Layout Geometry
- **Safe Area Inset:** A mandatory bottom margin of `env(safe-area-inset-bottom, 16px)` guarantees fixed navigation elements float clear of device home bars.
- **Touch Target Integrity:** Every interactive hit state enforces a minimum height and width of `48px`.

## Elevation & Depth

Visual hierarchy uses physical layering, warm diffuse shadows, and backdrop blurs to preserve contextual awareness during item discovery.

### Tonal Stratification
- **Level 0 (Canvas):** `#FAF8F5` base background; zero elevation.
- **Level 1 (Card & Section Surfaces):** Pure White `#FFFFFF` with a subtle hairline perimeter (`1px solid #E8E5DF`) and a gentle ambient lift: `0 2px 8px -2px rgba(31, 35, 40, 0.05)`.
- **Level 2 (Active Sheets & Cards on Drag):** Pure White `#FFFFFF` with soft, deep diffusion: `0 4px 20px -2px rgba(31, 35, 40, 0.08)`.
- **Level 3 (Floating Actions & Sticky Bottom Navigation):** `0 12px 32px -4px rgba(31, 35, 40, 0.16)`. Sticky bottom controls combine this shadow with `backdrop-filter: blur(12px)` and a translucent `#FFFFFFEE` fill.
- **Level 4 (Toasts & Overlays):** `0 16px 40px -6px rgba(31, 35, 40, 0.22)`.

## Shapes

The shape vocabulary communicates approachability and ergonomics via cohesive rounded geometries.

- **Base Radius (14px):** Applied to primary surfaces, input containers, item thumbnail frames, and standard callout banners.
- **Pill Geometry (9999px):** Applied to status badges, condition indicators, location tags, floating action buttons, and segmented pill filters.
- **Sheet Radius (24px):** Bottom sheets feature top-only radiuses (`rounded-t-[24px]`) paired with a centered `36px × 4px` rounded-pill drag handle.

## Components

### 1. Item Card
- **Structure:** Vertical flex container housed in `#FFFFFF` with a `1px solid #E8E5DF` boundary and a `14px` border radius.
- **Media Asset:** 1:1 or 4:3 aspect ratio photo container with `12px` inner radius, featuring an absolute-positioned top-left Price Pill.
- **Price Treatment:** Leaf Green `#2E9E5B` with bold white text for "FREE"; Deep Charcoal `#1F2328` with white text for priced goods (e.g., "$10").
- **Meta Row:** Inline horizontal flex of Location Chip (with location pin icon) and Condition Chip ("Like New", "Good", "Fair"), capped by an aligned relative timestamp (`micro` typography, `#656D76`).

### 2. Full-Width Primary Button
- **Dimensions:** Minimum `52px` height, full container width, `14px` border radius.
- **Styling:** Primary Cherry Red `#D6001C` background, `#FFFFFF` text in Plus Jakarta Sans SemiBold (`16px`).
- **Interactive State:** Transitions to `#B30017` on active press with a subtle `scale(0.98)` tactile transformation.

### 3. Floating Action Button (FAB)
- **Dimensions:** `56px × 56px` circle (or dynamic pill expanding on scroll up), anchored at the bottom-right (`16px` right, `80px` bottom to clear navigation).
- **Styling:** `#D6001C` background, pure white bold glyph (`+`), Level 3 drop shadow.

### 4. Bottom Navigation Bar
- **Dimensions:** Fixed full-width container, `64px` height + safe area inset.
- **Layout:** 4 equal tabs (Explore, Search, My Listings, Messages). Each target encompasses an icon and micro-label with a minimum `48px` tap box.
- **Active State:** Cherry Red icon fill and label text; inactive state rendered in `#656D76`.

### 5. Bottom Sheet
- **Anatomy:** `#FFFFFF` surface with `24px` top radiuses, overlaid upon a `rgba(31, 35, 40, 0.4)` dimmed backdrop.
- **Handle:** `36px × 4px` pill bar in `#E8E5DF` centered `8px` from the top edge. All primary controls reside within thumb-reach (lower 40% of the viewport).

### 6. Status Badges & Pills
- **Available:** `#2E9E5B` text on `#EAF7EE` fill with `#A7F3D0` outline.
- **Pending/Hold:** `#D97706` text on `#FEF3C7` fill with `#FDE68A` outline.
- **Reserved/Sold:** `#D6001C` text on `#FEE2E2` fill with `#FECACA` outline.

### 7. Form Inputs
- **Dimensions:** `50px` minimum height, `14px` radius, `#FFFFFF` fill with `1px solid #E8E5DF`.
- **Text:** Minimum `16px` font size (preventing mobile browser zoom triggers). Focused state transitions to a `2px solid #D6001C` stroke without layout shift.

### 8. Toast Notifications
- **Design:** Compact floating pill anchored at the top center, `16px` below status bar.
- **Styling:** Level 4 elevation, `#1F2328` high-contrast background with white typography and contextual status icon.

### 9. Empty States
- **Visual:** Outlined cardboard moving box paired with a leafy green sprout emerging from the seam.
- **Copy:** Friendly, actionable microcopy (e.g., *"No listings here yet! Be the first to share something with your dorm."*) followed by a compact Cherry Red CTA button.