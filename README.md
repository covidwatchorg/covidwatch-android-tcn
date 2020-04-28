![](https://github.com/covid19risk/covidwatch-android/workflows/Develop%20Branch%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=covid19risk_covidwatch-android&metric=alert_status)](https://sonarcloud.io/dashboard?id=covid19risk_covidwatch-android)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# CovidWatch Android POC
Android POC for www.covid-watch.org

## Who are we? What is this app?

This repository is focused on the implementation of the Android version of the Covid Watch app. Our goals are to:
- Allow users to anonymously record interactions with others using the [TCN Protocol](https://github.com/TCNCoalition/tcn-client-android)
- Notify users if someone they've interacted with in the past 2 weeks indicates they've tested positive for COVID-19 (again, anonymously!)
- Offer a seamless UX to complete all of the above!

The current version of the Figma we're working towards: https://www.figma.com/file/0uL6s79o21jwUFZz16Dr8b/Covid-Watch---App-v1.8?node-id=0%3A1

## Setup

Clone this repo from the `develop` branch:

```
git clone git@github.com:covid19risk/covidwatch-android.git
```

We are using tracking TCN's project as a submodule for now tracking the develop branch, so don't forget to init and fetch.

First time:

```
$ git submodule update --init --recursive --remote
```

To Update:

```
git submodule update --remote
```

Open the project in Android Studio. Install onto a phone of yours with the `app` configuration, and you're free to explore the app! Its optimal to install on 2 phones as much of the behavior of the app depends on 2 phones interacting.

**Note:** You cannot run this app on an emulator! We are dependent on Bluetooth being on and active, and most standard Android emulators do not have Bluetooth drivers.

## Looking to contribute?

- Run on your own device to explore the UX. Look at the [Figma](https://www.figma.com/file/0uL6s79o21jwUFZz16Dr8b/Covid-Watch---App-v1.8?node-id=0%3A1) for what the UX should look like. If you have any feedback/find any problems, create an issue!
- Look at https://github.com/orgs/covid19risk/projects/1 for existing issues. If you see something you want to work on, assign yourself to it, set it to in progress, and make a PR to the `develop` branch.

## FAQ

What is the anonymous protocol for communication between phones? How does it work and who designed it?

Covid Watch uses Temporary Contact Numbers, a decentralized, privacy-first contact tracing protocol developed by the [TCN Coalition](https://tcn-coalition.org/). This protocol is built to be extensible, with the goal of providing interoperability between contact tracing applications. You can read more about it on their [Github](https://github.com/TCNCoalition/TCN).

What's this repository vs the other repositories in the covid19risk Organization?

This is the repository for development of the front-facing Android mobile app for Covid Watch, including the UX and tie-ins to the TCN Bluetooth Protocol and backend services. Related repos:
- [Android Minimal:](https://github.com/covid19risk/covidwatch-android-minimal) Proof of concept pilot app for testing integrations with the bluetooth protocol.
- [TCN:](https://github.com/TCNCoalition/tcn-client-android) Implementation of bluetooth protocol.

## Contributors

- Madi Myrzabek (@madim)
- Milen Marinov (@BurningAXE)
- James Taylor (@jamesjmtaylor)
- Pavlo (@Apisov)
- Madhava (@madhavajay)
- Nitin Kumar (@nkumarcc, nkumarcc@gmail.com)
- Hayden Raddiford (@haydenridd)
- Enrico Grillo (@redbasset)

## Join the cause!

Interested in volunteering with Covid Watch? Check out our [get involved page](https://covid-watch.org/collaborate) and send us an email at contact@covid-watch.org!

