# ExpenseMate — Android Expense Tracker

An original Android internship practice project with Kotlin, Jetpack Compose and local SQLite storage.

## Features
- Add daily expenses with title, positive rupee amount, and category.
- Categories: Food, Travel, Shopping, Bills, Other.
- Browse expenses and filter by category.
- View total spending, formatted in Indian rupees.
- Edit a saved expense without changing its original date.
- Delete a saved expense after confirmation.
- Expenses persist offline in a local SQLite database.
- Money stored as integer paise to prevent floating-point rounding errors.

## Run in Android Studio
1. Unzip the project.
2. Open the **ExpenseMate** folder in a recent Android Studio installation.
3. Use JDK 17 and ensure Android SDK Platform 36 is installed.
4. Allow Gradle to sync (requires internet to download dependencies).
5. Run the `app` configuration on an Android emulator or device (Android 8.0+).

The ZIP contains the Gradle wrapper **properties** but does not include gradle-wrapper.jar or gradlew scripts. Android Studio can import the project using its installed Gradle environment; if the IDE requires a Gradle wrapper, generate one using an installed Gradle 8.13 (`gradle wrapper --gradle-version 8.13`).

## Project layout
- `MainActivity.kt`: Compose screens, expense list, category filtering, editor, and deletion confirmation.
- `ExpenseDatabase.kt`: SQLiteOpenHelper table and CRUD operations.
- `app/build.gradle.kts`: Android app configuration and dependencies.

## Manual verification checklist
- [ ] Create a Food expense for ₹100.50; total increases by ₹100.50.
- [ ] Add another expense in Travel; both appear in history.
- [ ] Filter Food; only Food expenses appear.
- [ ] Edit the first expense to ₹150.00; total recalculates.
- [ ] Cancel deletion; item stays.
- [ ] Confirm deletion; item disappears and total decreases.
- [ ] Restart the app; saved expenses remain available.
- [ ] Verify zero/negative/invalid amounts cannot be saved.

## Internship submission guidance
Capture **real** screenshots after running the app: Home screen, Add Expense, Category Filter, Edit Expense, Delete Confirmation. Add a short screen recording showing all five CRUD/history steps. Do not claim completion or testing before verification.

## Notes
Uses SQLiteOpenHelper, which is one of the permitted database choices in the task. This demo performs small local SQLite operations directly for simplicity. For a production-scale app, move persistence to an IO dispatcher and introduce a ViewModel/repository layer.
