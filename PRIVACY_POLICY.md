# Privacy Policy

**Last updated:** July 29, 2026

## Overview

DevForge ("we", "our", or "the app") is a developer toolkit application built with privacy in mind. This policy explains how we handle your data when you use the app.

## Data Collection & Usage

### Information We Collect

| Data Type | Collected | Purpose |
|-----------|-----------|---------|
| **Gemini API Key** | Yes (user-provided) | Required for AI Assistant chat functionality. Stored locally via `.env` file and never transmitted outside of Gemini API calls. |
| **API Requests** | No | All HTTP requests made through the API Tester are executed directly from your device. We do not log, store, or monitor them. |
| **GitHub Data** | No | GitHub searches and profile views go directly to GitHub's public API. We do not cache or store this data server-side. |
| **Saved Items** | Yes (local only) | API requests, regex patterns, and bookmarks you save are stored exclusively in your device's local Room database. |
| **Usage Analytics** | No | DevForge does not collect analytics, crash reports, or usage statistics. |
| **Personal Information** | No | We do not collect names, emails, device IDs, or any personally identifiable information. |

### Local Storage

The app uses **Room Database** (SQLite) for local persistence:

- **Saved API Requests** – URLs, headers, auth configs, and responses you choose to save
- **Regex Presets** – Custom regex patterns you create
- **Bookmarks** – Saved content from the Learning Hub
- **Preferences** – Theme selection (dark/light), onboarding completion status

All data stays on your device and can be cleared at any time via **Settings → Clear All Data**.

## Network Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | API Tester (making HTTP requests), GitHub Explorer (fetching repos), AI Assistant (Gemini API calls) |
| `ACCESS_NETWORK_STATE` | Checking network connectivity before requests |
| `POST_NOTIFICATIONS` | Download completion notifications (GitHub ZIP downloads) |
| `READ/WRITE_EXTERNAL_STORAGE` | Legacy support for older Android versions (API < 29) |

## Third-Party Services

### Google Gemini API
- When using the AI Assistant, your chat messages are sent to Google's Gemini API
- [Google's Privacy Policy](https://policies.google.com/privacy) applies to those communications
- We recommend not sharing sensitive information in AI chat prompts

### GitHub REST API
- Repository searches and user lookups go directly to GitHub's public API
- [GitHub's Privacy Policy](https://docs.github.com/en/site-policy/privacy-policies) applies

### Firebase App Check
- Used for security verification via reCAPTCHA
- [Google's Privacy Policy](https://policies.google.com/privacy) applies

## Data Security

- Your Gemini API key is stored locally and never shared with us
- No data is transmitted to our servers (we have no backend)
- All network requests originate from your device to the respective third-party APIs
- Room database is sandboxed within the app's private storage

## Children's Privacy

DevForge is a developer tool and is not intended for children under 13. We do not knowingly collect data from children.

## Your Rights & Control

- **Clear Data**: Settings → Clear All Data to delete all locally stored information
- **Uninstall**: Removing the app deletes all local data
- **API Key**: You can remove or change your Gemini API key at any time via the `.env` file

## Changes to This Policy

We may update this policy as the app evolves. Changes will be reflected in the "Last updated" date at the top.

## Contact

For questions about this privacy policy, please open an issue on the GitHub repository:

[https://github.com/ZainulabdeenOfficial/Devforge](https://github.com/ZainulabdeenOfficial/Devforge)
