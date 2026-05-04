# Semester 6 Grades Calculator

A sleek, high-performance desktop application built with Java Swing designed to track and project academic grades across various courses. The application features a modern dark-themed interface and real-time calculation logic to help students monitor their academic progress.

## Features

*   **Dark Mode Interface:** A custom-styled UI using a sophisticated dark palette and Segoe UI typography for maximum readability.
*   **Persistent Data:** Utilizes the Java Preferences API to automatically save your input data, ensuring your grades are preserved between sessions without manual saving.
*   **Smart Projections:** Dynamically calculates the exact percentage required on final exams to achieve specific letter grades (A+ through D) based on weighted course components.
*   **Comprehensive Course Support:** Pre-configured for several core subjects including Computer Architecture, Operating Systems, Management, Networks, Database Systems II, and Software Engineering.
*   **Visual Notifications:** Features an animated "Saved!" notification system and input validation to prevent invalid grade entries.

## Course Components Tracked

The application handles complex grading formulas for each course, such as:
*   **Best-of-N Logic:** Automatically selects the best N-1 assignments where applicable.
*   **Weighted Categories:** Balances quizzes, labs, milestones, projects, and midterms against a final exam weight.

## Installation & Usage

This repository provides pre-compiled installers for **Windows** and **macOS**. To get started:

1.  Navigate to the **Releases** section of this GitHub repository.
2.  Download the installer specific to your operating system.
3.  Run the installer and launch the application—no manual Java setup or command-line execution is required.

## Project Structure

*   `GradeCalculator.java`: The primary source file containing the GUI implementation, theme configuration, and grade calculation logic.

## How it Works

1.  **Input:** Enter your current marks for each component in the course tabs.
2.  **Auto-Save:** The app triggers a save and recalculation whenever you press **Enter** or click the **Save** button that appears on focus.
3.  **Analysis:** The summary table at the bottom updates instantly. If a grade is already reached, it displays "Achieved"; otherwise, it shows the percentage needed on the final exam. If a grade is mathematically impossible, it displays "X".

---
*Built with Java Swing.*