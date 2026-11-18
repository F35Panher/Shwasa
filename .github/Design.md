# Shwasa — Design System & UX Guidelines

## 1. Brand Identity
- **App Name**: **Shwasa** (Sanskrit for *breath*)
- **Tagline**: *Breathe Better. Live Better.*
- **Visual Metaphor**: Calm botanical sanctuary—warm cream surfaces, deep eucalyptus teal, sage greens, and unhurried breathing motion.
- **App Icon**: Minimalist alabaster-cream lotus embraced by sage-mint concentric breath arcs and orbital dots on a deep botanical teal (`#1C4E43`) adaptive background.

## 2. Color Palette (`ui/theme/Color.kt`)

| Token | Light Mode | Dark Mode | Purpose |
| :--- | :--- | :--- | :--- |
| `primary` | `#3E6F63` (Sage) | `#93C9B9` (Soft Mint) | Primary actions, active progress, focus badges |
| `secondary` | `#4D6A6E` (Muted Teal) | `#A9C8CC` (Pale Teal) | Secondary accents and calming highlights |
| `tertiary` | `#9C6B4B` (Warm Terracotta) | `#D9B296` (Warm Sand) | Streak badges, goal lines, Kapalbhati cues |
| `background` | `#F7F5EF` (Warm Cream) | `#101816` (Forest Night) | Main screen canvas |
| `surface` | `#FBF9F4` (Alabaster) | `#16201D` (Deep Moss) | Cards, bottom bars, and sheets |

Stage indicators use distinct calming hues (`#3E7B6D` Inhale, `#547AA5` Hold, `#8B6D9C` Exhale, `#2E7D87` Humming, `#C0734F` Active Kapalbhati, `#B38867` Rest).

## 3. Typography (`ui/theme/Type.kt`)
Google Fonts are bundled locally in `app/src/main/res/font/` for reliable offline rendering:
- **Display & Headings**: `Cormorant Garamond` — editorial serenity for screen headers.
- **Body & UI Controls**: `Plus Jakarta Sans` — clean legibility for instructions, cards, and settings.
- **Countdown Numerals**: `Space Grotesk` (`64sp`) — crisp, high-contrast digits for breathing timers.

## 4. Adaptive & Responsive Layout
- **Edge-to-Edge**: Enabled in `MainActivity` via `enableEdgeToEdge()`, with `WindowInsets.safeDrawing` applied at the root `Scaffold`.
- **Compact Screens (`< 640dp`)**: 5-tab Material 3 `NavigationBar` (`Home`, `Explore`, `Custom`, `Progress`, `Settings`), with 1-tap access to `Practice History` from both Home and Progress.
- **Medium / Expanded Screens (`>= 640dp`)**: Left `NavigationRail` showing all 6 top-level destinations, responsive 2–3 column module grids on Home, and `widthIn(max = 720–960.dp)` centered content containers.

## 5. Custom Visualizers (`BreathingVisualizer.kt`)
- **Circular Breathing Clock**: Radial gradient breath halo that expands on Inhale and contracts on Exhale with an outer arc indicator.
- **Box Square**: Rounded four-edge path illuminating each side sequentially with a glowing traveler dot.
- **Anulom Vilom Nostrils**: Dual left/right cards showing active nostril and animated vertical airflow.
- **Bhramari Wave**: Concentric pulsing resonance rings plus an animated acoustic wave during humming.
- **Kapalbhati Pulse**: Active exhalation counter (`current / target`) paired with a calm pulsing ring.
- **Minimalist Mode**: Large distraction-free countdown numbers selectable in Settings.

## 6. Accessibility & Safety Standards
- **Touch Targets**: Interactive buttons and controls meet or exceed `48dp` height/size.
- **Reduced Motion**: Toggle in Settings suppresses expanding/oscillating animations while preserving timer clarity.
- **Non-Visual Practice**: Optional audio chimes and spoken instructions let users close their eyes during practice.
- **Mindful Safety**: Every technique includes clear beginner instructions and explicit reminders never to force breath retention or continue through dizziness or breathlessness.
