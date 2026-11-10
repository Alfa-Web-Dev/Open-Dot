# OpenDot Android

An independent Android adaptation of the open-source OpenDot project. It is not an official OpenDot product and is not affiliated with the upstream maintainers.

## Current implementation

- Native Kotlin + Jetpack Compose Android application
- Local SQLite storage for custom Dots and conversation history
- Create agents with a name, role description, and custom system instructions
- Chat with an agent through an OpenAI-compatible `/chat/completions` endpoint
- Provider URL, model name, and API key configuration
- API key encryption using Android Keystore (AES-GCM)
- GitHub Actions debug APK build workflow

## Current limitations

This is an early port, not feature parity with desktop OpenDot. The following upstream capabilities are not yet implemented on Android:

- Pi AgentSession runtime replacement / native agent tool loop
- llama.cpp Android inference integration and model download management
- Streaming responses, tool execution, MCP connections and approvals
- Long-term semantic memory and memory controls
- Dot-to-Dot links, SuperDot orchestration and Organisation projects
- Scheduled/background watchers, integrations, file tools and voice
- Full localization (English, Hindi, Urdu), accessibility and release hardening

The current chat client supports compatible chat-completion APIs only. A cloud provider may require a paid API key; using the app itself is free, but provider usage terms and costs still apply. A local endpoint can be used when a compatible server is running on the Android device at localhost. Native on-device inference is planned separately.

## Build

GitHub Actions builds a debug APK on pushes to `main` and pull requests. Open the repository's **Actions** tab and download the `opendot-android-debug` artifact from a successful run.

## Architecture direction

The desktop app uses Electron, Node.js and the pi agent harness. Those components cannot simply be embedded in a native Android APK. This project ports product concepts and behavior to Kotlin/Android, with Android-native storage, secret protection, networking, and lifecycle handling.

## Upstream attribution and license

Upstream project: https://github.com/athulsreekumar/opendot

The upstream OpenDot project is MIT licensed. Its license notice is preserved in this repository's `LICENSE`. Any reused upstream source must retain applicable copyright and license notices. This repository is an independent adaptation.

## Security

- Never commit API keys, passwords, or signing keys.
- Provider keys entered in the app are encrypted with Android Keystore.
- HTTPS is required for remote endpoints. Cleartext HTTP is restricted to loopback localhost for local development/model servers.
- Review permissions and privacy implications before adding tools or integrations.
