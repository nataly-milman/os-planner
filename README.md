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
* PLANet takes your current schedule, your preferences and the different events and tasks that you want to add to that schedule and gives you the optimal schedule for your preferences.
* PLANet lets you group together different tasks and time-slots according to category and importance so that your schedule suits your specific needs!
* PLANet can split up different tasks so you can use your time more efficiently.

## How to use the library?
The current upload format for this repository requires you to pull the repository into Android Studio and compile from there, though you are also welcome to take whichever files you want from this project and to add them to your code in any way you like :)

Steps:
1. Install Android Studio - Follow the instructions in https://developer.android.com/studio/install<br/>
2. Clone git repo.
3. Run 'Build' -> 'Make project'.
4. Copy the created aar file from the 'app/build/outputs/aar' folder.
5. Paste the aar file into libs folder of your app.
6. Add the following line to your module's build.gradle file:

   In dependencies :
   implementation(name: <Your_aar_file_name>, ext: 'aar')

   In repositories :
   flatDir {
                             dirs 'libs'
                         }
7. Add the following permissions to your AndroidManifest:

    `<uses-permission android:name="android.permission.READ_CALENDAR" />`

    `<uses-permission android:name="android.permission.WRITE_CALENDAR" />`

To add a new calendar just use our preCreated PlannerMediator, which can be synced with
google calendar automatically, or insert events manually, as specified during creation.

To add a new task and watch our sub task creation algorithm at play, use PlannerMediator's addTask,
and specify the task's deadline and expected duration it will take to complete in milliseconds.

## An example of using PlannerMediator can be seen in exampleapp.
You can run exampleapp, add the widget created to your screen, and press AddTask to create a task using PLANet library.


## Regarding pull requests
In order to contribute to this project, please open a well documented pull request with the relevant changes.<br/>
Any pull requests to the library will be reviewed by all main authors of this library and the request will only be accepted if approved unanimously by all of us.
