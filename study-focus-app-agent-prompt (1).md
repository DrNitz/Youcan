## Role
You are a senior Android engineer and product-minded UI designer. Build an Android app from the requirements below. Decide the architecture, file structure, and implementation details yourself. Make sensible decisions yourself and note your assumptions in the assumptions.md. 
## Goal
One app that helps students study consistently by combining a focus timer, countdowns, streaks, motivation, study logging, phone-usage awareness, and reminders. The user should be able to start a session, stay focused, see where their time went, and keep a streak going.

## Platform and stack
- Android only (no iOS, no cross-platform).
- Kotlin, Jetpack Compose, Material 3.
- Offline-first. All data stays on the device. No account or login.
- Works across a wide range of Android versions and devices.
- Adaptive layout for every screen size: narrow and tall phones, foldables, and tablets, in portrait and landscape, split-screen or multi-window, and when resized. Base layouts on window size classes (compact, medium, expanded), not device types. On wider screens use the space well (for example two panes such as sessions beside analytics, or a navigation rail) and cap content widths so nothing looks stretched. Preserve state across rotation, fold and unfold, and resizing. Widgets, the floating dock, and the island-style pill must also behave on every size.
- Use current, stable, well-maintained libraries and verify versions before adding them.

## Core features (must have)

### 1. Pomodoro / focus timer
- Configurable focus, short-break, and long-break durations, plus number of cycles.
- Keeps running when the app is backgrounded or the screen is off, and survives the app being killed.
- Persistent notification with the live timer and quick actions (pause, resume, stop).
- Each session can be tagged with a subject or there activity and an optional note about what was studied/done.
- Clear alert (sound and/or vibration) when a phase ends.
- The timer screen has a user-selectable background (see section 8).

### 2. Countdown
- Countdowns to exams or deadlines (title, date and time, optional subject).
- Home screen shows the nearest countdown prominently (days, hours, minutes).
- Optional home-screen widget for the nearest countdown. (days, hours, minutes) with daily motivation below fully constamizable day,minitu hours monts / toggle quotes

### 3. Streaks
- A day counts toward the streak when the user reaches their daily study goal (user-set minutes for a particular app/group of apps).
- Show current streak, longest streak, and a calendar or heatmap view of past days with most used app.
- Handle edge cases cleanly: time zones, midnight rollover, missed days, and an optional streak-freeze.
- Also create a fance sharable socialmediacard 

### 4. Study logging and analytics
- Automatic log from completed sessions, and manual entry for study done outside the timer.
- Track total time per day, week, and month, broken down by subject.
- Charts: daily/weekly trends, subject distribution, best study hours, goal completion rate.
- Search or filter the history by subject and date range.

### 5. Phone and app usage awareness
- Show which apps the user spent time on, and for how long, per day and per week.
- Show how much of that time fell inside vs. outside study sessions.
- Let the user mark apps as "distracting" and show their total distracting time.
- Use the Android-sanctioned usage-access mechanism. Do not read screen content and do not use accessibility services. Explain the permission clearly in an onboarding screen, and degrade gracefully if the user declines.

### 6. Reminders
- Recurring study/task  reminders (days of week and time) and one-off reminders.
- Reminders must survive reboot and battery optimization as far as Android allows, and handle the exact-alarm and notification permission rules on current Android versions.
- Reminder before a countdown deadline (user-chosen offsets).
- Also can add todo list plan today with beautiful UI and reminder and timing and can copy todays plan to next day and soo on with a widget option to place on homescreen 

### 7. Motivation
- Daily motivational message or quote on the home screen.
- Encouraging feedback after sessions, streak milestones, and goal completion (subtle celebration animation).
- Keep the tone positive, never guilt-inducing.

### 8. Timer backgrounds
- The user chooses the focus-screen background in settings, with a live preview:
  - Built-in looping animated backgrounds (for example slow-flowing gradients, aurora, gentle waves, drifting stars, soft rain) that loop seamlessly and never feel busy.
  - Static options: solid colors, gradients, and true-black AMOLED.
  - Custom wallpaper: the user uploads an image from their gallery, can crop or zoom it, and can adjust blur and dim so the timer stays readable.
- The timer text and controls must stay readable on any background (automatic contrast or scrim).
- Animated backgrounds must be battery-friendly: stop when the screen is off or the app is backgrounded, and reduce or stop on battery saver or when the user has system "reduce motion" enabled.
- Use the system photo picker so no broad storage permission is needed.

### 9. Home-screen widgets
- The user can add multiple widgets, each configured independently, including several of the same type.
- Widget types (at least): focus timer with start/pause, today's study time and goal progress, streak, nearest countdown, weekly mini-chart, and motivational quote.
- Customization per widget through a configuration screen when it is added, and editable later:
  - Background: solid color, gradient, translucent, the user's own image, or dynamic system colors.
  - Corner radius, text color, and accent color.
  - Which items and info are shown (for example time, subject, streak, countdown, goal progress, quote).
- Resizable: the layout adapts from small to large, showing fewer items when reduced and more when expanded.
- Tap actions: start a session straight from the widget, or open the relevant screen.
- Widgets stay accurate (a running timer ticks live) without draining battery.
- Show a live preview in the widget picker.
- Android widgets cannot run continuous animations, so keep animation to the in-app screens and make widgets clean and static.

### 10. Island-style display and floating dock (optional, user-enabled)
The Pomodoro timer, reminders, and countdowns can each be shown outside the app, but only if the user turns it on, separately for each feature.

**Dynamic-island-style display.** Android has no native "Dynamic Island", so use the best mechanism the device offers and fall back gracefully:
1. Where the OS supports it (live or promoted ongoing notifications on recent Android versions, which can appear as a status-bar chip and on the lock screen, plus OEM features built on them), use that. It is the preferred, policy-safe route.
2. Otherwise, optionally offer a compact pill-style overlay near the top of the screen or camera cutout. Handle notches, cutouts, rotation, and status-bar insets.
3. Otherwise, fall back to a standard ongoing notification.
- Detect capability at runtime, tell the user which mode is active, and never promise the island style on phones that can't do it.
- The pill is compact by default (time remaining and a small progress ring) and expands on tap (pause/resume/stop, subject, next reminder or countdown).

**Floating dock (overlay).**
- A small draggable dock that snaps to screen edges, collapses into a bubble, and expands on tap into quick controls: start/pause/stop timer, current countdown, and next reminder.
- Remembers its position. The user can set size, opacity, theme, and which items it shows, and can choose to show it only while a session is running.
- One-tap dismiss, and easy to turn off from settings.

**Overlay rules.**
- Requires the "Display over other apps" permission. Request it only when the user enables the dock or pill, explain why, deep-link to the setting, and keep the app fully working if it is denied.
- Run it through a foreground service with a visible notification.
- It must never block touches outside its own bounds, cover system dialogs, or get in the way of the keyboard.
- Clean it up automatically when the session ends or the feature is turned off.
- Do not use accessibility services for this.

### 11. Voice assistant control (Gemini / "Hey Google")
The user should be able to control the app by voice through the phone's assistant. Target phrases (the app name is part of the request):
- "Hey Google, open [App]"
- "Set a 25-minute focus timer in [App]" (start, pause, and stop a session)
- "Start a countdown to my exam in [App]"
- "Set a study reminder for 6 PM in [App]"
- "Open the Pomodoro screen in [App]"
- "Show my weekly stats in [App]" (opens the stats screen, with a short summary where the assistant supports it)

Google is moving phones from Google Assistant to Gemini, and how third-party apps plug into Gemini is still evolving, so build in layers and check what actually works on a real device today:
1. **Baseline that always works:** every feature above is reachable through a deep link or intent, plus static and dynamic app shortcuts (start focus session, add countdown, add reminder, weekly stats).
2. **App Actions:** map built-in intents to those shortcuts or deep links so the assistant can open specific features. Test whether they still work with Gemini.
3. **AppFunctions (Android 16+):** expose complete operations (start a timer, create a countdown, create a reminder, read weekly stats) as functions the assistant can call. Expose business operations, never UI clicks. Check current availability, because Gemini access for third-party apps may be limited or in preview.
4. **Fallbacks:** if a voice path isn't supported on a phone, the same actions stay one tap away through shortcuts, widgets, and the Quick Settings tile.

Rules: use sensible defaults when a detail is missing (for example a default focus length), validate inputs, give clear confirmation of what was done, expose only safe low-risk operations, and keep all data on the device.
Do not test the voice paths yourself. My older phone has no Gemini, and I will test voice on my newer phone. Build them, then give me a checklist of commands to try.

## UX and UI direction
- Clean, clear, calm, modern, and less distracting. The timer screen is the hero.
- During a focus session show only the essentials (timer, subject, controls). Everything else stays hidden until the user taps to reveal it.
- Fast to start: starting a focus session takes one or two taps from the home screen.
- Smooth and purposeful animation throughout: shared-element and screen transitions, animated timer progress, number and chart transitions, subtle haptics, and a restrained celebration at milestones. Animation must support the experience and never compete with the content.
- Hold a steady 60fps and do not let animation hurt battery. Respect the system "reduce motion" setting.
- Thoughtful empty states, loading states, and first-run onboarding.
- Anti-"AI slop" design: the app must look handcrafted, not templated. Avoid the generic look: default purple-to-blue gradients, stock-style illustrations, emoji used as icons, identical card grids, overused glassmorphism, and decoration with no purpose. Pick a distinct visual identity (palette, typography, spacing, iconography) and apply it consistently.
- Wording: short, specific, human, and in one consistent voice. Avoid hype and filler such as "unlock your potential", "supercharge", "seamless", or "elevate". Use plain words that say exactly what a button or message does.
- Accessible: readable contrast, scalable text, TalkBack-friendly.
- Look for current Compose-friendly open-source libraries and design resources that raise UI quality. Candidates to evaluate (verify they are maintained and fit): Vico for charts, Lottie for animations, Konfetti for celebrations, Material 3 components and theming. Choose what you judge best, and keep dependencies minimal.
- If you have access to agent skills or reference repos for Android/Compose UI design, use them, and tell me which ones you used.

## Privacy and policy
- No data leaves the device.
- Keep the app compliant with Google Play policies for usage-access, notification, overlay, and foreground-service permissions.

## Out of scope for v1 we can implemen in V2
- Cloud sync, social features, leaderboards, accounts, iOS, web.
- peer syn, app overlay when using distacting app with a quote

## How to work
Work in three phases. Do not skip ahead.

### Phase 1: Research my system and tools (before the plan)
1. Inspect my system: operating system, CPU, RAM, free disk space, virtualization or hardware-acceleration support, GPU, and what is already installed.
2. Find the best open-source GitHub repositories and resources other then whech are not present in my pc Check that each one is actively maintained, free to use, license-compatible, and works with current Android tooling:
   - **Anti-AI-slop UI and wording:** Compose component libraries, design references, UX-writing and copywriting guides, and any agent skills that help produce distinctive, human-feeling UI and copy instead of generic template output.
   - 

### Phase 2: Plan, then stop
Write a concise plan covering: architecture and main libraries, the researched tools and chosen emulator setup (with expected RAM use), design direction, screen list, the build increments in order, how each increment will be verified, and risks. Then stop and wait. Do not write app code yet. I will read the plan and then tell you to implement.

### Phase 3: Implementation (only after I say go)
1. Do not ask me questions, except to ask me to connect my device (item 5). Decide yourself and record assumptions and decisions in the README.
2. Build in working increments: timer, sessions, and backgrounds, then streaks and logging, then countdowns and reminders, then widgets, then the island-style display and floating dock, then voice assistant integration, then usage tracking, then analytics and polish.
3. After each increment: Do compaction or creat a Compaction or Agent help.md so if i swith sesions or models this serves as were we left and wat was implemented and how was it done its pupose soo its easier to continue for other models.
4. Keep going until every feature in this document is implemented and verified. Do not stop early or hand back partly finished work. Test on the device I connect (I will mainly use my tablet). If something can't be verified on a connected device (overlay, usage access, widgets), build it anyway and list it for real-device testing. Voice assistant features are the exception: build them, but do not test them (see section 11).
5. When you have finished coding and building and are ready to install the app and test it on a real device, stop and ask me to connect my device. I will connect it, and then you can start installing and testing.
6. At the end, report what works, what is left to test on a real device, and give me a short checklist of voice commands to try on my newer phone.

## Definition of done (v1)
- Every "must have" feature works on a real device, including after reboot and with the app in the background.
- Widgets can be added multiple times, customized, resized, and stay accurate over time.
- Animated backgrounds run smoothly without noticeable battery drain.
- The island-style display and floating dock work when enabled, use the right fallback on unsupported phones, and clean up properly.
- Voice commands (open app, timer, countdown, reminder, Pomodoro screen, weekly stats) are implemented with shortcut fallbacks everywhere else. You do not test them; I will.
- The layout adapts cleanly from narrow phones to tablets, in portrait and landscape, with no clipped, stretched, or broken screens.
- Permissions are requested gracefully and the app still works if usage access is denied.
- Every feature has been built, run, and checked or test setup, and anything that can't be verified there is listed for real-device testing.
- Clean Gradle build with no critical warnings, and a README with features 
