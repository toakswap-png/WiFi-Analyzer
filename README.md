📡*** Wi-Fi Analyzer & Spectrum Monitor (Android)*** <br>

A modern, high-performance Android application built with Jetpack Compose and Kotlin to scan, analyze, 
and optimize Wi-Fi networks in real-time across 2.4 GHz, 5 GHz, and 6 GHz bands.<br>

🌟 Key Features<br>
📈 1. Interactive Spectrum & Channel Graph<br>
Smart Anti-Overlap Badge System: Dynamically computes non-overlapping badge positions for multiple overlapping SSIDs on co-channels.<br>
High-Contrast Visual Badges: Dark #0F172A cards with color-coded borders, dot indicators, and bold white typography for clear readability.<br>
BSSID-Hashed Color Palette: Every access point is automatically assigned a consistent, distinct color based on its MAC address.<br>
Multi-Band Support: Switch seamlessly between 2.4 GHz, 5 GHz, and 6 GHz frequency spectrums.<br>

📶 2. Access Points Inspector<br>
Comprehensive AP Details: Displays SSID, BSSID, RSSI (dBm), Channel, Frequency (MHz),<br>
Distance estimation, and Security encryption protocols (WPA/WPA2/WPA3).<br>
Instant Band & Text Filtering: Filter networks by frequency band or search by name in real-time.<br>
Smart Wi-Fi State Detection: Prompts the user with a quick settings shortcut if Wi-Fi is disabled on the device.<br>

🔗 3. Connected Network & IP Diagnostics<br>
Accurate Wi-Fi Standard Detection: Identifies Wi-Fi generation standards (Wi-Fi 4, Wi-Fi 5, Wi-Fi 6/6E, Wi-Fi 7).<br>
Network IP Configuration: Displays local device IP, calculated Subnet Mask, Gateway Router IP, DNS 1/DNS 2 servers, and MAC address.<br>
Real-Time Signal History Chart: Live RSSI tracking over time to monitor signal stability.<br>
Built-In Ping Latency Tool: Measures network round-trip time (RTT) to gateway or custom hosts.<br>

⭐ 4. Channel Rating & Recommendation<br>
Channel Scoring Engine: Analyzes channel congestion and ranks the best channels for optimal router configuration.<br>

🚀 Release Notes — v1.0.0<br>
Release Title: v1.0.0 — Initial Public Release & Spectrum UI Overhaul<br>

🆕 What's New & Highlights:<br>
Spectrum Tab Overlap Prevention: Implemented an automated 2D bounding-box algorithm so multiple co-channel SSIDs never obscure each other.<br>
Color & Contrast Enhancements: Added high-contrast badge background cards with matching signal curve border accents.<br>
Connected Tab Accuracy Updates:<br>
Corrected Subnet Mask calculation and Gateway Router IP lookup logic.<br>
Enhanced Wi-Fi Standard identification (Wi-Fi 4 / 5 / 6 / 6E / 7).<br>
Selectable text containers for copying IP addresses and DNS values.<br>
Edge-to-Edge Material 3 UI: Full Material Design 3 dark theme implementation using Jetpack Compose.<br>
