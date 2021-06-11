<!--
  Title: PLANet Scheduling Library
  Description: Preference-based automatic schedule planner library for maximum task completion
  Authors: Michal Balaban, Jason Elter, Nataly Milman
  -->

# PLANet 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

<sup>Authors: **Michal Balaban**, **Jason Elter**, **Nataly Milman**</sup>

## What is PLANet?
* PLANet is a time management library that automates the process of organizing your schedule.
* PLANet takes your current schedule, preferences and different events and tasks that you want to add to that schedule and gives you the optimal schedule for your preferences.
* PLANet lets you group together different tasks and time-slots according to category and importance so that your schedule suits your specific needs!
* PLANet can split up different tasks so you can use your time more efficiently.

## How to use the library?
The current upload format for this repository currently requires you to pull the repository into Android Studio and compile from there, though you are also welcome to take any file you want from the project and to use it in your code in any way you would like :)

Steps:
1. Install Android Studio - Follow the instructions in https://developer.android.com/studio/install<br/>
2. Clone the project to an empty folder using `git clone https://github.com/nataly-milman/os-planner.git`.
3. Run the library and copy the created `.aar` file from the generated folder.
4. Paste the `.aar` file into libs folder of your app.
5. Add the following lines to your module's gradle file:<br/>
   In dependencies:<br/>
   `implementation(name: 'library-debug', ext: 'aar')`<br/>
   In repositories:<br/>
   `flatDir { dirs 'libs' }`
6. Add the following permissions to your AndroidManifest:</br>
    `<uses-permission android:name="android.permission.READ_CALENDAR" />`</br>
    `<uses-permission android:name="android.permission.WRITE_CALENDAR" />`

To add a new calendar just use our preCreated PlannerManager, which can be synced with Google Calendar automatically, or insert events manually, as specified during creation.

To add a new task and watch our sub task creation algorithm at play, use PlannerManager's addTask,
and specify the task's deadline and expected duration it will take to complete in milliseconds.

## Regarding pull requests
In order to contribute to this project, please open a well documented pull request with the relevant changes.<br/>
**note -** Any pull requests to the library will be reviewed by all main authors of this library and the request will only be accepted if approved by all three.
