# 🛡️ SMS Spam Filter

An advanced Android application for filtering and blocking SMS spam messages using machine learning and intelligent pattern recognition, specifically designed for Hebrew content and Israeli spam patterns.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Machine Learning](https://img.shields.io/badge/ML-Custom%20Algorithm-orange.svg)
![API Level](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 🚀 Overview

SMS Spam Filter is a sophisticated Android application that automatically detects and blocks spam SMS messages using a custom machine learning algorithm. Built with privacy in mind, all processing is done locally on the device with no data transmitted to external servers.

### Key Features

- **🤖 Custom ML Algorithm** - Intelligent spam detection with continuous learning
- **🔒 Privacy-First Design** - 100% local processing, no data collection
- **🇮🇱 Hebrew Support** - Optimized for Israeli spam patterns and Hebrew content  
- **⚡ Real-time Blocking** - Automatic notification blocking for spam messages
- **📱 Modern UI** - Clean, intuitive Material Design interface
- **📊 Smart Analytics** - Track blocked messages and sender statistics

## 📱 Screenshots

*Screenshots will be added soon*

## 🛠️ Technical Stack

### Languages & Frameworks
- **Kotlin** - Primary development language
- **Android SDK** - Native Android development
- **Material Design 3** - Modern UI components

### Key Technologies
- **NotificationListenerService** - Real-time SMS monitoring
- **Content Providers** - SMS history access
- **Custom ML Algorithm** - Pattern recognition and classification
- **Local Storage** - SharedPreferences and JSON serialization

### Architecture Pattern
- **MVVM** - Model-View-ViewModel architecture
- **Repository Pattern** - Data access abstraction
- **Coroutines** - Asynchronous operations

## 🧠 Machine Learning Algorithm

The app uses a custom-built machine learning algorithm specifically designed for SMS spam detection:

### Feature Analysis
```kotlin
- Pattern Recognition: Identifies common spam patterns
- Keyword Analysis: Detects money, urgency, and gambling terms
- Sender Verification: Unknown number detection
- Content Structure: Message length and formatting analysis
- Link Detection: Suspicious URL identification
```

### Learning Capabilities
- **User Feedback Integration** - Learns from user corrections
- **Dynamic Weight Adjustment** - Adapts to new spam patterns
- **Hebrew Language Optimization** - Trained on Israeli spam samples
- **Continuous Improvement** - Updates classification model based on usage

## 📋 Features

### Smart Detection
- ✅ Real-time spam identification
- ✅ Automatic notification blocking
- ✅ Custom blacklist management
- ✅ Pattern-based recognition
- ✅ Hebrew content analysis

### User Management
- ✅ Message history review
- ✅ Manual spam/legitimate marking
- ✅ Blocked senders management
- ✅ Detailed message analytics
- ✅ One-click sender blocking

### Privacy & Security
- ✅ 100% local processing
- ✅ No internet permissions required
- ✅ Encrypted local storage
- ✅ No data collection
- ✅ Open source transparency

## 🏗️ Project Structure
```
app/src/main/java/com/nitzan/smsspamfilter/
├── MainActivity.kt                  # Main dashboard
├── MessagesActivity.kt             # Message management
├── MessageDetailActivity.kt        # Individual message view
├── BlockedSendersActivity.kt       # Blacklist management
├── SenderMessagesActivity.kt       # Sender-specific messages
│
├── SpamDetectorML.kt              # ML algorithm core
├── BlockedSendersManager.kt       # Blacklist operations
├── MessageStorage.kt              # Data access layer
├── SMSNotificationListener.kt     # Real-time monitoring
│
├── MessagesCompactAdapter.kt      # Message list UI
├── BlockedSendersAdapter.kt       # Blacklist UI
└── SMSMessage.kt                  # Data model
```

## 🚀 Getting Started

### Prerequisites
- Android 7.0 (API level 24) or higher
- ~20MB storage space
- SMS and Notification permissions

### Installation
1. Download the APK from [Releases](../../releases)
2. Enable "Install from Unknown Sources" in device settings
3. Install the application
4. Grant required permissions when prompted

### Initial Setup
1. Open the app and tap "Configure Permissions"
2. Enable SMS reading permissions
3. Go to: Settings → Apps → Special App Access → Notification Access
4. Enable "SMS Spam Filter"
5. The app is now ready to protect you from spam!

## 🎯 Algorithm Performance

The custom ML algorithm achieves high accuracy through:

- **Multi-layer Analysis** - Combines multiple detection methods
- **Context Awareness** - Considers sender history and patterns
- **Adaptive Learning** - Improves with user feedback
- **False Positive Minimization** - Conservative classification approach

### Spam Categories Detected
- 📞 **Telemarketing** - Unwanted promotional calls
- 🎰 **Gambling** - Poker, casino, and betting promotions  
- 💰 **Financial Scams** - Fake prizes and money offers
- 🔗 **Phishing** - Malicious links and fake surveys
- ⚡ **Urgent Scams** - Time-limited fake offers

## 📊 Development Insights

- **Development Time**: 2-3 weeks
- **Lines of Code**: ~2,000+ lines of Kotlin
- **Testing**: Emulator and real device testing
- **ML Training**: Custom dataset of Israeli spam patterns

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### How to Contribute
1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Areas for Contribution
- 🐛 Bug fixes and improvements
- ✨ New spam detection patterns
- 🎨 UI/UX enhancements
- 📝 Documentation improvements
- 🌍 Localization for other languages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

**Nitzan Wainshtein**
- 📧 Email: [Nitzanwa@gmail.com](mailto:Nitzanwa@gmail.com)
- 💼 LinkedIn: [linkedin.com/in/nitzwa](https://www.linkedin.com/in/nitzwa/)
- 🐙 GitHub: [@NitzanWainshtein](https://github.com/NitzanWainshtein)

## 🙏 Acknowledgments

- Android community for excellent documentation
- Open source libraries that made this project possible
- Beta testers who helped improve the spam detection accuracy

---

⭐ If you found this project helpful, please give it a star on GitHub!

**Made with ❤️ for a spam-free SMS experience**
