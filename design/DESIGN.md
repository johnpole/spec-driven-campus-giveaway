# Campus Giveaway Design System (DESIGN.md)

Single source of truth for UI implementation. Use these tokens and the exported PNG screenshots in `design/stitch/<screen-name>/screenshot.png`. Only inspect raw HTML if a specific detail is ambiguous.

## 1. Color Tokens
| Token | Hex | Role / Application |
|---|---|---|
| `--color-primary` | `#D6001C` | RPI Cherry Red; primary CTAs, active nav tab, critical actions |
| `--color-primary-dark` | `#B30017` | Button active/pressed states |
| `--color-primary-soft` | `#FDE8EA` | Soft red container/hover tint |
| `--color-secondary` | `#1F2328` | Deep Charcoal; primary typography, priced pills (`$15`) |
| `--color-secondary-muted`| `#656D76` | Supporting body copy, timestamps, inactive nav |
| `--color-border-hairline`| `#E8E5DF` | Card borders, dividers, form input perimeters |
| `--color-tertiary-green` | `#2E9E5B` | Leaf Green; "FREE" pill, claimed state, eco indicators |
| `--color-tertiary-soft`  | `#EAF7EE` | Eco badge surface, approved state background |
| `--color-tertiary-border`| `#A7F3D0` | Eco badge perimeter |
| `--color-canvas`         | `#FAF8F5` | Warm off-white page background |
| `--color-surface`        | `#FFFFFF` | Pure white cards, bottom sheets, sticky bars |
| `--color-status-amber`   | `#D97706` | Pending review text (bg `#FEF3C7`, border `#FDE68A`) |
| `--color-status-red`     | `#D6001C` | Rejected/sold badge (bg `#FEE2E2`, border `#FECACA`) |

## 2. Typography
- **Headings / Display:** `Plus Jakarta Sans`, SemiBold/Bold (600/700).
  - Title XL (Mobile): `24px` / `30px`, `-0.015em` letter-spacing.
  - Title LG: `22px` / `28px`, `-0.01em` letter-spacing.
  - Title MD: `18px` / `24px`, `-0.005em` letter-spacing.
- **Body & Controls:** `Inter`, Regular/Medium (400/500).
  - Body Regular: `16px` / `24px` (minimum `16px` on form inputs to prevent mobile browser zoom).
  - Label Meta: `14px` / `20px`, `600` weight.
  - Micro: `12px` / `16px`, `500` weight (timestamps, secondary chips).

## 3. Spacing & Grid
- **Viewport:** Mobile-first fluid grid (360px–430px, calibrated at 390px). Safe area: `env(safe-area-inset-bottom, 16px)`.
- **Scale:** `xs: 4px` · `sm: 8px` · `md: 16px` · `lg: 24px` · `xl: 32px`.
- **Margins & Gutters:** `16px` (1rem) page margins and column gutters.
- **Touch Targets:** Minimum `48px × 48px` on all interactive touch elements.

## 4. Radii & Shadows
- **Corner Radii:** Base: `14px` (cards, inputs, primary buttons) · Pill: `9999px` (chips, badges, FAB) · Sheet: `24px` top corners.
- **Elevations:**
  - Card (L1): `0 2px 8px -2px rgba(31, 35, 40, 0.05)` + `1px solid var(--color-border-hairline)`.
  - Floating / Nav (L3): `0 12px 32px -4px rgba(31, 35, 40, 0.16)` + `backdrop-filter: blur(12px)`.
  - Toast / Sheet (L4): `0 16px 40px -6px rgba(31, 35, 40, 0.22)`.

## 5. Component Inventory
1. **Item Card:** White container (`14px` radius, hairline border), 1:1 image, top-left price pill (Green "FREE" or Charcoal `$price`), 2-line title, location pin chip, condition chip, relative timestamp.
2. **Primary Button:** Height `52px`, full-width, `14px` radius, `#D6001C` background with white 700 text. Active: `#B30017` with `scale(0.98)`.
3. **Floating Action Button (FAB):** `56px × 56px` circular `#D6001C` pill bottom-right, white `+` icon, L3 shadow.
4. **Bottom Navigation:** Fixed `64px` height + safe area, 3 tabs (Browse, Post, My Items) with icon + micro-label.
5. **Bottom Sheet:** White container with `24px` top radius, centered `36px × 4px` drag handle, dimmed `rgba(31,35,40,0.4)` backdrop.
6. **Form Inputs:** `50px` min height, `14px` radius, `16px` text size, `1px` border, transitions to `2px solid #D6001C` on focus.
7. **Status Badges / Chips:** Pill geometry (`9999px`). Semantics: FREE (green solid), Available (green soft), Pending (amber soft), Rejected (red soft).
8. **Toast Notification:** Floating pill top-center, `#1F2328` background, white text, contextual icon.
9. **Empty State:** Outlined moving box with leaf sprout, friendly copy, red primary CTA.

## 6. Screens & States Overview
- **S0 — Design System (`s0-design-system`):** Visual identity foundation ("Campus Circular"). Defines tactile minimalism, color hierarchy, typography scale, touch-ergonomics, and atomic UI tokens.
- **S1 — Home / Browse Feed (`s1-home-feed`):** Sticky top bar (logo + search field), hero impact banner ("Moving out? Pass it on · 1,240 items rehomed"), horizontal category scroll chips, 2-column compact listing grid, bottom-right "+ Post" FAB, and sticky bottom navigation.
  - *States:* Empty category state (moving box illustration), search filtering, infinite scroll skeleton feed.
- **S2 — Item Detail (`s2-item-detail`):** Full-bleed swipeable photo gallery with overlaid back/share controls, prominent price pill, title, metadata chips (condition, category, pickup location, expiry date), seller avatar row ("Priya S. · 2h ago"), description, interested peer indicator ("3 people are interested"), and sticky bottom "I'm interested" CTA.
  - *States:* Bottom sheet interest form modal (Name, Contact, pre-filled Message) and success toast overlay.
- **S3 — Post an Item (`s3-post-item`):** Single-column stepped form. Top photo capture tile (`camera` input), Item section (Title, Category bottom sheet picker, Condition segmented control, Price field with "It's free" toggle), Pickup location, Expiry date, Seller contact info, and legal consent checkbox.
  - *States:* Inline field validation error on empty Title/contact, dynamic disabling of price input when "It's free" is active.
- **S5 — Post Status (`s5-post-status`):** Private post tracking screen. Listing thumbnail + title, 3-step vertical progress timeline (`Submitted` → `AI Review` → `Approved` / `Pending Human` / `Rejected`), amber "Pending Review" advisory card, "Copy private link" button, and "Post another item" CTA.
  - *States:* `PENDING` (amber clock banner), `APPROVED` (green checkmark, live listing link), `REJECTED` (red alert with policy reason).

## 7. Implementation Notes
- **Styling Architecture:** The raw HTML files use the Tailwind CDN for Stitch preview purposes only. Implement all production UI using plain SCSS and the CSS custom properties defined in Section 1. Do not add Tailwind.
- **Image Assets:** Images embedded in the raw HTML are temporary Stitch cloud previews (`lh3.googleusercontent.com`). Never reference these external URLs; use local placeholder images located in `frontend/src/assets/placeholders/`.
- **Product Branding:** The product name displayed across the UI is always **"Campus Giveaway"** ("Campus Circular" refers strictly to the design style name).
- **Web Fonts:** Load `Plus Jakarta Sans` (600, 700) and `Inter` (400, 500) from Google Fonts with `display=swap`.
