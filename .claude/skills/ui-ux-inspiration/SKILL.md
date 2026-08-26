---
name: ui-ux-inspiration
description: >
  Curated catalog of UI/UX design-inspiration websites (Aceternity, ShaderGradient,
  HeroUI, dialedweb, supahero, motionsites, Spline 3D, and others) plus a playbook
  for translating their web-native patterns (gradients, glassmorphism, scroll/hover
  motion, bento grids, 3D scenes) into Jetpack Compose for this Android app. Use this
  skill whenever the user wants design inspiration, asks to redesign or restyle a
  screen, mentions "design references", "UI inspo", 3D UI elements, or names any of
  the cataloged sites (Aceternity, ShaderGradient, HeroUI, Supahero, Motionsites,
  dialedweb, 21st.dev, Spline, etc.) — even if they don't explicitly say "use the
  design skill". Also use it when
  deciding how a Compose screen (SolverScreen, SettingsScreen, HistoryScreen,
  StatsScreen, Theme) should look or feel, or when picking a visual direction
  (gradients, cards, motion, typography) for this app.
---

# UI/UX Inspiration Catalog

A personal reference list of design/UI showcase sites, kept for pulling visual and
motion ideas into this app's Jetpack Compose UI (`app/src/main/java/com/leetcodeautomation/ui/`).

Almost none of these sites are npm packages or things you "install" — they're mostly
React/Tailwind component galleries and design portfolios. The value here is visual
and interaction *patterns*, not code you can drop in. One exception: **ShaderGradient**
is a real npm/React library (`@shadergradient/react`) — not usable directly in Kotlin,
but its shader-gradient *look* is worth recreating with Compose's `Brush`/`AGSL`
runtime shaders (Android 13+) or a plain animated multi-stop gradient as a fallback.

When the user asks for design inspiration or a restyle, open the relevant site(s)
with WebFetch/browser tooling to look at current examples (site contents change
over time — treat categorization below as a starting point, not gospel), then
translate what's useful using the "Translating to Compose" section.

## Catalog

### New picks (current favorites — check these first)
- **https://ui.aceternity.com/** — big library of copy-paste React components:
  spotlight cards, animated beams, 3D card hover, bento grids. Best source for
  "how do I make this card feel alive" patterns.
- **https://shadergradient.co/** — animated shader-based gradient backgrounds.
  Real npm package (`@shadergradient/react`), not Kotlin-usable directly — mine it
  for color/motion combos to rebuild with Compose `Brush.linearGradient` /
  `Brush.radialGradient` + `animateFloat`, or AGSL runtime shaders on API 33+.
- **https://supahero.io/** (user-flagged "special") — worth a close look for
  hero-section layout and typography-led design.
- **https://motionsites.ai/** (user-flagged "special") — AI-assembled sites with
  strong scroll/entrance motion; good source for animation timing/easing ideas.
- **https://www.dialedweb.com/** (user-flagged "cool design") — overall polish and
  layout reference.

### Older picks (still useful, lower priority than "New")
- **https://www.durves.com/**
- **https://www.dark.design/** — dark-theme design portfolio.
- **https://getdesign.md/** (user-flagged "contains something good" — re-check
  when browsing, the good bits aren't obviously labeled on the site).
- **https://dotmatrix.zzzzshawn.cloud/**
- **https://21st.dev/** — component gallery; still good but the "New picks" above
  are the current reference point, so prefer them when the two disagree.

### Different elements (specific components/interactions, not full sites)
- **https://calltoinspiration.com/** — CTA/button design inspiration specifically.
- **https://heroui.com/** — a real, documented React component library (used to be
  NextUI). Good for looking up how a component's *states* (hover/focus/disabled/
  loading) are typically designed, even though it's not installable in Compose.
- **https://animmasterlib.dev/** — animation-focused component library; good
  reference for micro-interaction timing.

### 3D
- **https://spline.design/** — browser-based 3D design tool for building
  interactive 3D scenes/objects (exported as `.splinecode`/embeds for web, or
  GLTF/GLB models). Not a component gallery — it's where you'd *author* a 3D
  asset. Not directly embeddable in Compose (no Spline runtime for Android),
  but two practical paths: (1) export the scene as a GLB/GLTF model and render
  it natively with a Compose-compatible 3D engine (e.g. Filament/SceneView,
  or Google's `filament-android`), or (2) treat it purely as visual reference
  for lighting/material/shape ideas and rebuild simpler versions procedurally
  (e.g. a 3D-ish rotating card via `graphicsLayer { rotationX/rotationY }` +
  gradient shading, no real 3D engine needed for lightweight effects).

### Uncategorized (not yet reviewed — check before relying on these)
- **https://www.rocket.new/**
- **https://www.vengenceui.com/**
- **https://skiper-ui.com/**

These three haven't been evaluated yet. If the user references one, browse it,
form an opinion on what category it belongs in (New/Old/Different elements), and
update this file's categorization accordingly — don't just leave it uncategorized
forever.

## Translating web patterns to Jetpack Compose

This app is Kotlin + Jetpack Compose (Material 3) — see `app/.../ui/Theme.kt` for
the existing color scheme/typography before introducing a new visual direction.
None of the reference sites' code ports over; only the visual/motion *idea* does.

| Web pattern | Compose equivalent |
|---|---|
| CSS gradient background | `Brush.linearGradient(...)` / `Brush.radialGradient(...)` as a `Modifier.background(brush)` |
| Animated/shader gradient (ShaderGradient) | Animate gradient stop colors/offsets with `rememberInfiniteTransition` + `animateFloat`, or an `AGSL` `RuntimeShader` on API 33+ for a true shader look |
| Glassmorphism / frosted card | `Modifier.blur()` (API 31+) on a background layer, or a semi-transparent `Surface`/`Box` with a subtle border + `Modifier.shadow` |
| Hover/spotlight card effects (Aceternity) | Compose has no mouse hover on touch devices — reinterpret as **press/ripple state** via `Modifier.pointerInput`/`interactionSource`, or a subtle scale+elevation animation on tap using `animateFloatAsState` |
| Scroll-triggered entrance animation (motionsites) | `LazyColumn`/`LazyRow` items animated in with `animateItemPlacement()` plus an `AnimatedVisibility` (`fadeIn() + slideInVertically()`) keyed on visibility |
| Bento-grid layouts | `LazyVerticalGrid` with `GridItemSpan` to vary tile sizes |
| Micro-interactions (animmasterlib) | `animateFloatAsState`/`animateColorAsState`/`Animatable` driven off button press or state change; keep durations short (100-250ms) to match typical web micro-interaction timing |
| Component state variants (HeroUI docs) | Model explicitly as Compose state (`enabled`, `isLoading`, `isError`) and branch the `Modifier`/content per state, same idea as CSS variants |

## Workflow when asked for design inspiration or a restyle

1. Ask (or infer from context) which screen/component is being redesigned.
2. Pick 1-3 relevant sites from **New picks** first, falling back to **Older
   picks**/**Different elements** for a specific interaction detail.
3. Fetch the live site if you need to see current examples — this list only has
   short descriptions, not the actual visual designs.
4. Use the translation table above to propose a concrete Compose implementation
   (composable structure + which Modifier/animation APIs), not just a vague
   "make it look like X".
5. If the user names one of the **Uncategorized** sites, review it and slot it
   into a category in this file as part of the same turn.
