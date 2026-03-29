---
name: acg-frontend-optimization
description: Expertly optimize and refine existing Vue 3 frontend code for an ACG illustration & commission marketplace. Focus heavily on improving DOM structure, elevating CSS/UI aesthetics, and polishing user experience, while STRICTLY PRESERVING all existing business logic, API calls, and data bindings.
---

This skill acts as an expert UI/UX refactoring guide. Your objective is NOT to design new features from scratch, but to take existing, functional-but-basic Vue 3 components and elevate them into a production-grade, immersive ACG platform (similar to Pixiv, Skeb, or ArtStation).

## 1. Optimization Mindset
- **Refine, Don't Replace**: Read the existing `<template>` and `<script setup>`. Understand its functional purpose before changing the UI. 
- **Structure First, Style Second**: Clean up "div soup". Reorganize existing elements using modern CSS Grid or Flexbox for better spatial composition before applying decorative styles.

## 2. Structural & Layout Optimization
- **Information Hierarchy**: Reorganize existing data points. Ensure the most important content (e.g., Artwork image, Commission price, Artist name) grabs immediate attention.
- **Card-Based UI**: Wrap existing list items or loose data into elegant, high-density content cards.
- **Whitespace & Breathing Room**: drastically improve `padding`, `margin`, and `gap`. Professional ACG platforms use deliberate negative space to make colorful artworks pop. 

## 3. Style & Aesthetic Refinement (ACG Vibe)
- **Theme & Colors**: Replace hardcoded generic colors with a cohesive ACG palette. Use deep dark modes or ultra-clean light modes (`#F7F9FA` backgrounds with pure `#FFFFFF` foreground cards).
- **Depth & Polish**: Upgrade flat designs by adding subtle features: 
  - `backdrop-filter: blur()` for glassmorphism effects on floating elements.
  - Extremely soft, diffuse `box-shadow` to create depth.
- **Micro-interactions**: Enhance existing buttons and cards with smooth `transition` classes, `hover:` states (e.g., slight scaling, gradient overlays), and active states.
- **Typography**: Optimize existing text. Use font-weight and opacity (e.g., `text-gray-500` for secondary info) to create contrast, rather than just changing font sizes.

## 4. Specific Component Enhancements
- **Galleries**: Transform basic `v-for` image lists into visually pleasing masonry grids or balanced flex layouts. Ensure `object-fit: cover` prevents image distortion.
- **Commission Forms/Flows**: Restructure existing forms. Group related inputs into visually distinct sections or cards. Enhance existing status text into visually distinct Badges/Tags.

## 🚫 5. THE ABSOLUTE RED LINES (Logic Preservation)
You are optimizing the VIEW layer, NOT the MODEL or CONTROLLER.
1. **Zero API Changes**: NEVER alter Axios/Fetch URLs, methods, or payloads. The backend Spring Cloud contract is locked.
2. **Preserve Vue Reactivity**: All `ref`, `reactive`, `v-model`, `@click`, and lifecycle hooks (`onMounted`) must remain exactly as they are. You can change the HTML tag wrapping them, but the directive and function call MUST survive.
3. **Keep Conditional Logic**: If an element has `v-if` or `v-show` based on user roles or commission status, that logic must be strictly maintained in the new optimized structure.