SpendWise: Personal Expenditure Tracker
SpendWise is a lightweight, aesthetic desktop application designed for students to manage their daily expenses. Built with Java Swing and a FlatLaf modern interface, it provides a clean, terminal-style experience using the Consolas font and a color-coded financial dashboard.

Key Features
Color-Coded Budgeting: The total balance changes color dynamically:

Leaf Green: Under budget (Safe).

Lemon Yellow: Approaching limit (Caution).

Blood Red: Budget exceeded (Warning).

Categorized Spending: Transactions are color-coded by category for instant recognition:

Maroon: Food 

Gold: Shopping 

Dark Blue: Travel 

Black: Bills 

Visual Analytics: Generate a Pie Chart to see exactly where your money is going.

Data Persistence: Uses SQLite to save your data locally—no internet required.

Export Feature: Save your logs as a .csv file to open in Excel or Google Sheets.

Preview
The application features a soft pink aesthetic with high-contrast, category-specific text colors and a custom-drawn analytics engine.

Tech Stack
Language: Java (JDK 17 or higher)

UI Framework: Java Swing + FlatLaf (Flat Light Theme)

Database: SQLite (JDBC)

Typography: Consolas (Monospaced)

How to Run
1. Prerequisites
Make sure you have the following installed:

Java JDK

An IDE (VS Code, IntelliJ, or Eclipse)

2. Dependencies
You will need to add these .jar files to your project's classpath:

FlatLaf (for the modern UI look)

SQLite JDBC Driver (to handle the database)

3. Execution
Clone this repository or copy the Main.java file.

Compile the project:

Bash
javac -cp "lib/*" Main.java
Run the application:

Bash
java -cp "lib/*;." Main
Project Structure
Plaintext
SpendWise/
├── src/
│   └── Main.java       # Core application logic and UI
├── lib/
│   ├── flatlaf.jar     # UI Theme dependency
│   └── sqlite-jdbc.jar # Database connector
├── spendwise.db        # Automatically generated local database
└── README.md           # Project documentation
Usage Tips
Set Budget: Click the BUDGET button in the sidebar to define your monthly limit.

Delete Entries: Simply select a row in the table and click the [X] DELETE button.

View Stats: Use the STATS button to visualize your spending distribution.

Developed by a student for students. Stay mindful of your spending! 🌸
