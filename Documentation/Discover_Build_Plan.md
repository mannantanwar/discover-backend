# Discover — Solo Build Plan

*A practical, phase-by-phase plan for building Discover alone, tailored to a Java/Spring Boot backend developer. Optimized so the project is useful, demoable, and resume-worthy at every stage — even if you stop halfway.*

---

## 0. Read this part first — the two ideas that decide whether you finish

Almost every ambitious solo app dies for one of two reasons. Internalize these before you write a line of code.

### Idea 1: Build single-player first. Always.

Your spec's most exciting features — Taste Network, "people like you," similar-user reviews, match scores, leaderboards — are **worthless until you have thousands of active users.** With 1 user (you), "people who like what you like" is an empty screen. This is the *cold-start problem*, and it kills apps like this.

**The rule:** every feature you build must be genuinely useful with exactly one user and zero social graph. Social and network features are added *on top* of an already-useful single-player app — never as a dependency for basic value.

This single principle reorders your entire build. It's why Dish Intelligence (useful solo) comes before Taste Network (useless solo), even though your spec lists them close together.

### Idea 2: Your spec is a *vision*, not an MVP.

You wrote a brilliant vision doc — you even labeled it "not a technical document" and "the master checklist so nothing is forgotten." Perfect. Keep it as your north star. But you cannot *build* it in order. An MVP is the smallest thing that delivers the core magic. For Discover, the magic is:

> **"I walked into this restaurant and the app told me exactly what to order — and it was right."**

That's Dish Intelligence + a Taste Profile. Everything else is scaffolding around that moment. Build toward that moment first.

---

## 1. Scope discipline — what "MVP" actually means here

Your spec has ~25 modules. Here's the honest triage:

| Build in MVP (single-player useful) | Defer to V2 (needs users) | Defer to V3 (Future/hard) |
|---|---|---|
| Auth + onboarding | Social feed / posts | AI chat assistant |
| Place browse / detail / map / search | Follow / followers | OCR menu scanner |
| **Dish Intelligence (flagship)** | Taste Network / "people like you" | Live crowd tracking |
| Taste Profile | Similar-user reviews | Reservations / waitlists |
| Reviews & ratings | Creators / leaderboards | AR / voice search |
| Context intelligence (time/weather) | Collaborative recommendations | Wearable integration |
| Save / collections | Trails (curated, social) | Group planning (do late) |
| Content-based recommendations + match score | Badges (mostly) | |

**Your first shippable target isn't the MVP column above — it's even smaller:** one city, one category (restaurants), Dish Intelligence + Taste Profile. Prove the magic, then expand.

---

## 2. Tech stack (chosen for a solo Java dev)

You're strongest in Spring Boot. Lean into it hard on the backend; make pragmatic, low-effort choices everywhere else.

### Backend — your home turf
- **Spring Boot 3 / Java 21** — your strength; excellent for this.
- **PostgreSQL 16 + PostGIS** — one database does relational data *and* geospatial ("places within 5 km" is a single PostGIS `ST_DWithin` query). Don't add a separate geo service.
- **Flyway** — versioned DB migrations from day one.
- **Redis** — feed caching, rate limiting, hot lists. Add when you need it, not before.
- **Search: start with Postgres** `pg_trgm` + full-text. It's genuinely good enough for one city. Add OpenSearch/Elasticsearch only when it hurts.
- **Auth:** Spring Security + JWT. Add Google Sign-In via OAuth2 early (users hate passwords).
- **Object storage:** Cloudflare R2 or Backblaze B2 for photos/videos (cheap, no egress fees). Never store media in Postgres.

### Mobile — your weak spot, so minimize the pain
This is a location-first, on-the-go product, so it must be mobile eventually. But native iOS + Android solo is too much. Pick **one cross-platform framework:**

- **Flutter (recommended for you).** Single language (Dart), one codebase → iOS + Android, strong typing that'll feel familiar coming from Java, excellent maps/location/camera plugins, great docs. Structured and predictable.
- **React Native / Expo** — also fine, larger community, but the JS ecosystem churn is more chaotic for a backend dev.

> **Faster-validation option:** build a **mobile-web PWA (Next.js)** first if you want to test the concept before committing to a mobile app. It's quicker to iterate solo and you can put it in front of friends via a URL. Trade-off: worse maps/location/camera UX, no app-store presence. Reasonable for phases 0–2, then move to Flutter once the concept is proven. Your call — I'd lean Flutter from the start since the product's soul is mobile, but PWA-first is a legitimate shortcut.

- **Maps:** **Mapbox** (generous free tier, great custom styling) or Google Maps SDK. Mapbox is usually more cost-friendly for a places app.

### Infrastructure — keep it near-free until you have users
- **Deploy:** Railway, Render, or Fly.io — cheap, Git-push deploys, managed Postgres. Don't touch Kubernetes.
- **Containerize** with a simple Dockerfile early; it makes moving hosts painless later.
- **CI:** GitHub Actions (free) — build + test on every push.
- **Monitoring:** Sentry (free tier) for crash/error tracking.

**Monthly cost through the single-player phase: roughly $0–20.** Don't over-provision.

---

## 3. The two hard problems (neither is code)

### Problem A: Where does place & dish data come from?

- **Places** are easy: seed from Google Places API, Foursquare, or OpenStreetMap/Overpass. For MVP you only need one city/one area.
- **Dishes are the hard part — and dishes are your flagship.** No API cleanly sells dish-level menus with taste tags and photos. Your options:
  1. **Manual seeding (do this for MVP).** Pick ~30–50 restaurants in one neighborhood. Enter their dishes, prices, and taste tags by hand. Tedious, but it *unblocks the entire flagship feature* and guarantees data quality where it matters most.
  2. **Crowdsource later.** Once you have users, let them add dishes/tags. Reward with badges.
  3. **Menu scraping / OCR** — a V3 automation, not a starting point.

**Do not start feature code until you've decided how dish data enters the system.** Manual seeding of one neighborhood is the right answer for now.

### Problem B: Log every interaction from day one

The single most important backend decision: **capture an event log from the very first version, even before you use it.**

Every view, save, rating, search, and dwell-time becomes a row in an `interaction_events` table (`user_id`, `entity_type`, `entity_id`, `event_type`, `context`, `timestamp`). You won't need it in phase 1 — but your Taste Profile, recommendations, and eventual collaborative filtering are *all* built from this log. If you start logging in month 8, your intelligence starts from zero in month 8. If you start in month 1, you'll have rich data ready when you need it. This is the highest-leverage, lowest-effort thing you can do early.

---

## 4. How the "AI" actually works (you don't need ML to start)

The recommendation engine sounds like it needs deep learning. It doesn't — not for a long time. Start with transparent math:

- **Taste Profile = a preference vector.** A weighted score across dimensions: cuisines, flavors (sweet/spicy/…), ambience, budget, activity types, dietary. Seeded at onboarding, then nudged by the event log (save `+`, positive rating `++`, negative `--`, long view `+`).
- **Every place/dish is tagged on the same dimensions.**
- **Match Score = weighted cosine similarity** between the user vector and the item vector, blended with a popularity prior and context.
- **Context intelligence v1 is cheap and high-magic:** time of day (trivial), weather (OpenWeather free API), day of week, a hardcoded India festival calendar. "Perfect weather for chai and pakoras" is a `if raining` rule — and it *feels* magical.
- **"Why am I seeing this?" comes free.** Because the model is linear/similarity-based, you can read off the top-contributing dimensions and generate the explanation your spec asks for. (A neural net can't hand you that as easily — another reason to start simple.)
- **Collaborative filtering ("people like you") comes later,** once the event log has real interaction data across many users: user–user or item–item cosine similarity. Meaningful only at scale — which is exactly why it's a V2 feature.

Start heuristic. Earn the right to add ML later with data you've been collecting all along.

---

## 5. The phase-by-phase build plan

Each phase ends in a **demoable milestone** — something you can show a friend or screenshot for your portfolio. Ship in this order. Resist jumping ahead to the exciting network features; they'll be empty and demoralizing.

At ~15–20 focused hours/week alongside DTU, expect **~1–2.5 months per phase.**

---

### Phase 0 — Walking skeleton *(foundations)*
**Goal:** a deployed app where you can register, log in, and land on an empty home screen. End to end.

Build:
- Spring Boot project, Postgres + PostGIS, Flyway, Dockerfile, deploy to Railway/Fly.
- JWT auth + Google Sign-In. User + session model.
- `interaction_events` table (start logging immediately).
- Mobile shell (Flutter): splash → login → empty home. Talks to the live backend.
- GitHub repo + Actions CI + Sentry.

**Done when:** you install the app on your phone, sign in with Google, and it's talking to your deployed backend. *This milestone feels small but is psychologically huge — the whole pipeline works.*

---

### Phase 1 — Places core *(single-player useful)*
**Goal:** a genuinely useful "places near me" browser, useful with one user.

Build:
- Place data model; seed one neighborhood (~30–50 places) from Google Places/OSM.
- Place list, place detail (info, amenities, photos, hours, map), map view (Mapbox).
- Search (Postgres `pg_trgm` + full-text) and basic filters (category, distance, budget, open-now).
- "Near me" via PostGIS. Save places + Collections.

**Done when:** you can open the app in your area, browse real nearby places, search, filter, save some. *This is already a usable app.*

---

### Phase 2 — Dish Intelligence v1 *(the flagship — but "dumb")*
**Goal:** walk into a restaurant, instantly see what to order. The magic moment, single-player.

Build:
- Dish entity under places; dish detail (photo, price, taste tags, description).
- **Manually seed dishes** for your ~30–50 restaurants (the grunt work that unlocks everything).
- Dish reviews / ratings / photos / taste tags.
- Dish analytics (most ordered, trending, hidden gem) — simple counts at first.
- "Recommended dishes" — **rules-based**: match dish taste tags to the user's stated onboarding preferences + popularity. No ML yet.

**Done when:** you open a seeded restaurant and see a ranked "Recommended for you" dish list with reasons. Show this to a friend — *this is the "I wish every app had this" moment.* If it lands, you're onto something real.

---

### Phase 3 — Taste Profile + Recommendations v1 *(still single-player)*
**Goal:** a personalized feed that visibly improves the more you use it.

Build:
- Onboarding taste capture → seed the preference vector.
- Taste Profile that learns from the event log (saves, views, ratings).
- Content-based place + dish recommendations via cosine similarity → **Match Score**.
- **"Why am I seeing this?"** explanations from top-contributing dimensions.
- **Context intelligence v1:** time of day, weather (OpenWeather), festival calendar → contextual home-feed banners.
- Personalized Home Feed assembled from the above.

**Done when:** two different onboarding answers produce visibly different feeds, and the feed shifts as you interact. *Now it's personalized — the core promise is delivered, still with a single user.*

> **This is your first true milestone as a startup or a portfolio centerpiece.** If you stop here, you have a genuinely impressive, complete single-player app. Everything after this adds network effects.

---

### Phase 4 — Reviews & the social layer *(now you need users)*
**Goal:** turn a single-player app into a social one.

Build:
- Full reviews (place / dish / activity) with photos, tags, occasion, helpful-votes.
- User profiles (stats, activity, favorites) + privacy controls.
- Follow / followers / following.
- Posts + social feed, likes, comments.
- Notifications (in-app first; push later).

**Done when:** you and a handful of testers can follow each other, post, review, and see each other's activity. **Recruit 20–50 real testers now** (friends, DTU peers) — you need real data before the network features mean anything.

---

### Phase 5 — Network effects layer *(needs a user base)*
**Goal:** the differentiators that only work at scale.

Build:
- **Taste Network:** user–user similarity from the event log → "people like you," similar-user recommendations, Taste Match %.
- **Collaborative filtering** blended into recommendations (now you have data).
- **Similar-user & friend reviews** surfaced first.
- **Personal Trails** (auto visit history + privacy) and **Curated Trails**.
- **Creators, leaderboards, badges.**

**Done when:** a new user with a filled taste profile sees non-empty "people like you" sections. *Only attempt this once you have enough active users to compute meaningful similarity — otherwise it'll be empty and discouraging.*

---

### Phase 6 — Planning, hardening, launch
**Goal:** production-ready and in the stores.

Build:
- Group taste match / group recommendations.
- Content moderation, abuse reporting, full privacy settings.
- Performance (Redis caching, feed optimization), crash triage.
- Push notifications, onboarding polish, App Store / Play submission.

**Done when:** a stranger can install it from a store, onboard, and get value without you in the room.

---

## 6. What you'll need to learn (and in what order)

You've got the backend. Budget learning time for:

1. **One mobile framework (Flutter).** Your biggest gap. Start learning it during Phase 0. ~4–6 weeks to competence alongside building.
2. **Geospatial basics (PostGIS).** A weekend. Pays off immediately in Phase 1.
3. **Recommender fundamentals** — content-based filtering, cosine similarity, then collaborative filtering. You do *not* need deep learning to start. A week of reading gets you through Phase 3.
4. **Basic mobile design / UX.** You don't need to be a designer, but a clean, consistent UI is what makes it feel like a product vs. a class project. Use a component kit; copy good apps' patterns.

---

## 7. Staying alive — the solo survival guide

Solo, long-haul projects fail from attrition, not difficulty. Protect against it:

- **Ship a demoable slice every phase.** Momentum comes from visible progress, not perfect code. A working ugly screen beats a beautiful plan.
- **One city, one category, to start.** Every time you feel the urge to add a module, ask: "does this help the 'what should I order here' moment?" If not, it goes in the backlog.
- **Keep the vision doc as backlog, not roadmap.** You already wrote the perfect north-star doc. Don't build in its order.
- **Don't build the network features early.** They'll be empty and will make the app feel dead. This is the #1 solo trap for social apps.
- **Time-box perfectionism.** Refactor when something hurts twice, not preemptively.
- **Get it in front of real people at Phase 2.** Their reaction to Dish Intelligence tells you whether to keep going. Build → show → learn.

---

## 8. Resume/portfolio value at each checkpoint

Even if it never becomes a company, each phase is a real credential:

- **After Phase 1:** "Built and deployed a full-stack geospatial mobile app (Spring Boot, PostGIS, Flutter)." Already strong.
- **After Phase 3:** "Designed a content-based recommendation engine with explainable match scoring and context-awareness." This is *senior-level* portfolio material and rare for a student.
- **After Phase 5:** "Built a taste-based social graph with collaborative filtering." Now you're describing systems that real companies pay for.

You genuinely win either way. Now go build Phase 0.

---

*Companion to your "Discover — Product Feature Specification v1.0." That doc is the destination; this is the route.*
