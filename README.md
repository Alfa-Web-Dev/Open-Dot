# OpenDot Android

An independent, community-built Android adaptation inspired by the open-source OpenDot project.

> **Project status:** Initial setup. Features, architecture, and release readiness are still under development. This project is not an official OpenDot product and is not affiliated with the upstream maintainers.

## Goals

- Native Android application built with Kotlin and Jetpack Compose
- Local-first data and settings, with optional cloud AI providers
- Separate Dots with their own instructions, configuration, memory, and conversations
- Permission-aware tools and explicit approval for sensitive actions
- English, Hindi, and Urdu interface support
- Automated Android builds with GitHub Actions

## Planned architecture

Clean Architecture with MVVM, Hilt, Room, DataStore, Ktor, and WorkManager. Local model support will be evaluated through an Android-compatible llama.cpp integration.

## Build

The Android Gradle project and CI workflow will be added in upcoming commits. Until then, there is no installable APK in this repository.

## Upstream project and attribution

This project is an independent adaptation. The upstream OpenDot repository is:

https://github.com/athulsreekumar/opendot

The upstream project is licensed under the MIT License. Any upstream code reused in this adaptation will retain its applicable copyright and license notices. This repository will include its own license and attribution files as implementation proceeds.

## Security

- Do not commit API keys, signing keys, passwords, or other secrets.
- Use GitHub Actions secrets for release signing configuration.
- Review permissions and privacy implications before enabling tools or integrations.
