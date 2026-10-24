# Stitch Prompts (stitch.withgoogle.com)

**Mobile-first.** Most attendees open the app by scanning a QR code on their phones, so design every screen
in **Mobile** mode first. The app is still a web app (Angular, served from Cloud Run), so nothing has to be installed.
It just needs to look and feel native on a phone. Generate one screen per prompt, then use short
"Edit" follow-ups instead of re-prompting. When you're done, export the **HTML/CSS** and screenshots into `design/stitch/`.
Optionally, at the end, generate a desktop variant of S1 (prompt D1) so you have a version for the projector.

---

## S0 — Design system (run first)

```
Mobile-first design system for "Campus Giveaway", a mobile web app where college students who are
graduating or moving out give away or sell their stuff cheaply to other students. Users arrive by
scanning a QR code on their phone. Mood: friendly, trustworthy, sustainable, a little playful.
Primary color: RPI cherry red #D6001C. Secondary: deep charcoal #1F2328. Accent: leaf green #2E9E5B
for "Free" badges and sustainability cues. Background: warm off-white #FAF8F5.
Typography: rounded geometric sans for headings (like "Plus Jakarta Sans"), "Inter" for body,
16px minimum body size. Corner radius 14px, soft shadows. Touch targets at least 48px, primary actions
reachable by thumb at the bottom of the screen. Components: item card with photo, price/"FREE" pill,
condition chip, pickup-location chip, time posted; full-width primary button; bottom navigation bar;
floating action button; bottom sheet; toast; status badges (Pending = amber, Approved = green,
Rejected = red); form inputs with clear labels; empty state with a cardboard moving box with a leaf.
```

## S1 — Home / browse feed (the first screen after scanning the QR)

```
Mobile home screen for Campus Giveaway (390px wide). Sticky top bar: logo (moving box with a leaf)
and a search field "Search desks, mini-fridges, textbooks…". Small hero card: "Moving out? Pass it on."
with "1,240 items rehomed · 3.1 tons kept out of landfill". Horizontally scrolling category chips:
All, Free, Furniture, Electronics, Textbooks, Kitchen, Dorm essentials, Clothing, Bikes.
Two-column grid of compact item cards: square photo, title (2 lines max), price or green "FREE" pill,
pickup location, "2h ago". Floating red "+ Post" button bottom-right above a bottom nav bar with
Browse, Post, My items. Infinite scroll feel.
```

## S2 — Item detail + "I'm interested"

```
Mobile item detail screen for Campus Giveaway. Full-width swipeable photo at top with back arrow and
share icon overlaid. Below: title "IKEA desk + chair", big price "$15" (or green FREE pill), chips for
condition, category, pickup location, "available until May 20". Seller row: avatar initial, "Priya S.",
posted 2h ago. Description text. "3 people are interested" with first names.
Sticky bottom bar with a full-width primary button "I'm interested". Tapping it opens a bottom sheet
with: Your name, Your email or phone, Message (prefilled "Hi! Can I pick this up Friday after 5pm?"),
and "Send interest". Show the success toast: "Priya will get your note. Be kind and show up on time!"
```

## S3 — Post an item (with camera)

```
Mobile "Post an item" screen for Campus Giveaway, single column, step feel. Top: large photo tile
"Take a photo or choose from gallery" (camera icon) with preview after capture. Section "The item":
Title, Category (bottom-sheet picker), Condition segmented control (Like new / Good / Fair),
Price with an "It's free" toggle that disables the price field, Description, Pickup location,
Available until (native date picker). Section "About you": Name, Email or phone, helper text
"Only shown to people who express interest." Checkbox "This item is legal, safe, and mine to give."
Sticky bottom full-width button "Post item". Show an inline validation error on an empty Title.
Inputs use the right mobile keyboards (email, tel, numeric for price).
```

## S4 — Moderator queue (the live-demo screen)

```
Mobile moderator screen for Campus Giveaway. Header "Moderation" with a badge "AI pre-screen: Gemini"
and segmented tabs Pending (12) / Approved / Rejected. A vertical list of cards: thumbnail, title,
seller name, time, and an "AI verdict" strip with a colored badge (APPROVE / REJECT / NEEDS HUMAN),
a confidence bar, and a one-line reason (e.g. "Looks like hazardous chemicals; violates campus policy.").
Each card has two large buttons side by side: green "Approve" and red "Reject" (Reject opens a
bottom sheet with reason options). Filter chip at top: "Needs human only".
```

## S5 — Post status (private link the poster gets after posting)

```
Mobile screen "Your post" for Campus Giveaway: item thumbnail and title, a status timeline:
Submitted → AI review → Approved (or Pending human review / Rejected with reason).
Amber "Pending review" banner with text "We check every post to keep campus safe. Usually under a minute."
Button "Copy private link" and "Post another item".
```

## D1 — Desktop variant (optional, for the projector)

```
Desktop (1440px) version of the Campus Giveaway home feed using the same design system:
top nav with search and "+ Post an item" button, hero strip, category chips, 4-column item grid.
```

## Follow-up edit prompts

- `Make the FREE pill more prominent and use the leaf green.`
- `Increase contrast; ensure all text meets WCAG AA.`
- `Move the primary action into a sticky bottom bar so it's reachable with a thumb.`
- `Show this screen in dark mode.`
