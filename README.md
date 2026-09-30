# Scraps to Savory

An Android app (Java) that helps a user reduce food waste by tracking the ingredients they actually have at home — their "pantry" — and suggesting recipes they can cook using strictly those ingredients, no shopping trip required. Built for Mobile App Development 700.

Recipes only appear as "suggested" if every single required ingredient is already in the pantry, in at least the required quantity. This is the app's core piece of logic, and will live in a class called `StrictMatcher`.

## Database choice: SQLite

This app uses **SQLite**, via `SQLiteOpenHelper` (`DatabaseHelper`), implemented fully on-device.

Why: the app's data (pantry items and a fixed recipe collection) is inherently local and single-user, so a cloud database like Firebase adds sync complexity with no real benefit here. SQLite also means the app runs fully offline, with no account setup or network dependency required to demo or mark it, and it's the persistence approach covered directly in the module's persistent-data chapter.

## Project status

Work in progress, built incrementally — see commit history. Currently implemented:

- Project scaffold (Gradle, manifest, resources, theme)
- Data models (`PantryItem`, `Recipe`, `RecipeIngredient`, plus `PantryCategory`, `DietTag`, `MealType`)
- `DatabaseHelper` with the full schema and 27 seeded South African home-cooking / quick-meal recipes
- Pantry List screen with a RecyclerView bound to the database (add, edit and delete all working)
- Add/Edit Ingredient form with input validation

Still to come: the strict-matching algorithm, Suggested Recipes screen, Recipe Detail screen, and Settings screen.

## Setup / run instructions

1. Clone this repository.
2. Open the project folder in Android Studio (a recent stable version — Iguana/2023.2 or newer recommended).
3. Let Gradle sync finish (it will download the AndroidX/Material dependencies listed in `app/build.gradle`).
4. Run on an emulator or physical device with Android 7.0 (API 24) or higher.
5. No API keys, accounts, or network access are required — the app seeds its own recipe data on first launch.
