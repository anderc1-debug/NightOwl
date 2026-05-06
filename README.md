# NightOwl

**Your campus. Anytime.**

NightOwl is a JavaFX desktop application that gives college students one place to find campus resources specific to their school. Mental health services, academic support, safety contacts, tutoring, and more, all in one app, available at any hour.

Built for CSC 311 at Farmingdale State College, Spring 2026.

---

## The Problem

Campus resources exist at every school. Students just cannot find them, especially late at night when offices are closed and information is scattered across a dozen different websites. NightOwl puts everything in one place with a search bar and data specific to your campus.

---

## Features

- **Multi-campus support** for Farmingdale State, Stony Brook, Old Westbury, Nassau CC, Hofstra, and Adelphi
- **Resource directory** with live search and category filtering (Mental Health, Academic, Safety, Health)
- **Bookmarks** to save frequently used resources, persisted to the local database
- **Wellness tracker** to log mood, sleep, stress, and study hours daily with a 14-day line chart
- **Tip submission** so students can flag outdated or missing resource info
- **Admin console** to manage users and process submitted tips
- **Full auth system** with account creation, login, logout, and session management
- **Onboarding flow** for school selection, major, class year, and resource preferences
- **Fully offline** with no internet required, no cloud, no external server

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| UI Framework | JavaFX 21 with FXML |
| Styling | CSS |
| Database | Apache Derby (embedded) |
| Build Tool | Maven |
| IDE | IntelliJ IDEA |
| Version Control | GitHub |

---

## Project Structure

```
src/main/java/com/nightowl/
├── Main.java               Entry point
├── MainApp.java            JavaFX Application, handles splash and auth flow
├── MainController.java     Primary controller for all UI panels
├── DatabaseManager.java    Singleton, handles all Derby DB operations
├── CampusResources.java    Utility class holding all campus data
├── UserProfile.java        User model
└── WellnessEntry.java      Record type for wellness log entries

src/main/resources/
├── fxml/                   FXML layout files
└── css/styles.css          Purple theme stylesheet
```

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- IntelliJ IDEA (recommended)

### Run the App

```bash
git clone https://github.com/anderc1-debug/NightOwl.git
cd NightOwl
mvn clean javafx:run
```

The Derby database creates itself automatically on first run. No setup or configuration needed.

### First Run Note

On first launch you will be prompted to create an account. To grant admin access, you will need to update the IS_ADMIN column directly in the Derby database for that user, or use the default admin credentials if you set them up beforehand.

### Derby Version Note

This project uses Derby 10.14.2.0 intentionally. Derby 10.15 and above introduced JPMS module declarations, which causes the engine module to hard-require commons internally regardless of plugin configuration. Once those jars hit the module path the app fails to launch with no clean workaround.

Derby 10.14 is pre-JPMS, ships as a single plain jar, gets placed on the classpath automatically, and works fine with JDK 21. Do not upgrade Derby without testing the full module path behavior first.

---

## Known Limitations

**Passwords are stored as plain text.** The production fix would be to add jBCrypt to the Maven dependencies and hash on account creation. This was a known tradeoff given project scope and time.

**No password reset flow.** Adding one would require an email field on the user table, a reset token table, and something like JavaMail to send the link.

**JavaFX chart styling is limited.** The wellness chart works and shows the data correctly but the colors do not fully match the purple theme. JavaFX chart CSS is poorly documented and inconsistent.

**Campus resource links can go stale.** Links were verified at build time but there is no automated check in place. The tip submission feature exists specifically to handle this but it still requires a human to review and update.

**No wellness data export.** You can view the chart but you cannot download the data.

---

## Architecture Notes

DatabaseManager is a singleton so there is one shared instance handling all database operations across the app. CampusResources uses Java records for immutable campus data. WellnessEntry is also a record type, used as a compact immutable data carrier between the database and the UI.

All resource panels are built dynamically in Java at runtime based on the logged-in user's campus. An earlier version had Farmingdale resources hardcoded in FXML, which had to be refactored when multi-campus support was added.

---

## Contributors

- **Rory Anderson** (anderc1-debug) - architecture, JavaFX implementation, database design, all features
- **Ana Garcia** (GarcaA100) - initial file setup

---

## Course

CSC 311 - Software Engineering
Farmingdale State College
Spring 2026
