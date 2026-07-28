📶 Wi-Fi Analyzer & Spectrum Monitor (Android) <br>
A modern, high-performance Android application built with Kotlin and Jetpack Compose to scan, analyze, visualize Wi-Fi spectrum, and diagnose network health in real-time.<br>
✨ Key Features<br>
📡 1. Real-Time Wi-Fi Spectrum Graph (Channel Graph)<br>
Parabolic Frequency Curves: Visualizes Wi-Fi channels across 2.4 GHz, 5 GHz, and 6 GHz bands.<br>
Overlap-Free SSID Badges: Smart spatial badge algorithm automatically adjusts badge positions and text background colors when multiple SSIDs overlap on the same channel.<br>
BSSID Color Mapping: Unique color coding per BSSID for clear identification of dual-band or multi-AP routers.<br>
🔍 2. Access Point Scanner<br>
Comprehensive Nearby Scanning: Detects nearby Wi-Fi access points with detailed metrics (RSSI signal strength in dBm, Frequency, Channel, Security type, Link speeds).<br>
Smart Off-State Detection: Prominently prompts "Please Turn On WiFi" with a direct quick-action button to open Android Wi-Fi settings when Wi-Fi is disabled.
Instant Filtering & Search: Filter by frequency bands (2.4 GHz, 5 GHz, 6 GHz) or search by network name (SSID/BSSID).<br>
Detailed AP Breakdown: Click on any network to inspect standard protocols, capabilities, and distance estimations.<br>
🔗 3. Connected Network & IP Diagnostics<br>
Accurate Standard Detection: Real-time detection of Wi-Fi 7 (802.11be), Wi-Fi 6E, Wi-Fi 6 (802.11ax), Wi-Fi 5, and Wi-Fi 4.<br>
Security Protocol Identification: Detects WPA3-Personal (SAE), WPA3/WPA2-Enterprise, Enhanced Open (OWE), and WPA2-PSK.<br>
Complete Network Specs: Displays Local Device IP, Gateway Router IP, Auto-calculated Subnet Mask, DNS 1 & DNS 2, MAC Address, Link Speeds, and Channel width.<br>
Selectable High-Contrast UI: High-readability card layouts with copyable text containers for IP/MAC addresses.<br>
Live Signal History Chart: Real-time graph tracking RSSI signal stability over time.<br>
Built-in Ping Latency Tool: Real-time ping test for local gateway and custom domain latency diagnostics.<br>
📊 4. Channel Rating & Recommendations<br>
Automatically evaluates channel congestion on 2.4 GHz and 5 GHz bands to recommend the best channels for configuring your router.<br>
🎨 5. Modern UI & Simulation Support<br>
Material Design 3 Dark Theme: Deep Navy canvas with Neon Cyan, Green, and Purple tech accents.<br>
Simulation Mode: Built-in toggle to simulate nearby networks when running on emulators without direct Wi-Fi hardware access.<br>
🛠️ Tech Stack & Architecture<br>
Language: 100% Kotlin<br>
UI Framework: Jetpack Compose (Material 3)<br>
Architecture: MVVM + Repository Pattern<br>
Concurrency: Kotlin Coroutines & Flow / StateFlow<br>
State Management: collectAsStateWithLifecycle()<br>
💡 Quick Summary in Hindi:<br>
Spectrum Tab: Multiple SSIDs hone par ab text overlap nahi karta. Unke high-contrast background badges aur BSSID-wise unique colors auto-adjust hote hain.<br>
Wi-Fi Off State: Jab Wi-Fi off hoga to screen par "Please Turn On WiFi" alert card aayega jisse ek-click me settings khul sake.<br>
Connected Tab: Accurate Wi-Fi Standard (Wi-Fi 6/6E/7), Security Type (WPA3/OWE/WPA2), Subnet Mask, Gateway, DNS, aur selectable copyable text.<br>
