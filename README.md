# Scraps to Savory

An Android app, written in Java, that helps a user reduce food waste by tracking the ingredients they actually have at home, their "pantry", and suggesting recipes they can cook using strictly those ingredients, with no shopping trip required.

Recipes only appear as "suggested" if every single required ingredient is already in the pantry, in at least the required quantity. This is the app's core piece of logic, and lives in a class called `StrictMatcher`.

## Database choice: SQLite

This app uses SQLite, through `SQLiteOpenHelper` (`DatabaseHelper`), implemented fully on-device.

Why: the app's data, pantry items and a fixed recipe collection, is inherently local and single-user, so a cloud database such as Firebase adds sync complexity with no real benefit here. SQLite also means the app runs fully offline, with no account setup or network dependency required to run it, and it is the persistence approach covered directly in the module's persistent data chapter.

## Setup / run instructions

1. Clone this repository.
2. Open the project folder in Android Studio, using a recent stable version.
3. Let Gradle sync finish. It will download the AndroidX and Material dependencies listed in `app/build.gradle`.
4. Run on an emulator or physical device with Android 7.0 (API 24) or higher.
5. No API keys, accounts, or network access are required. The app seeds its own recipe data on first launch.
