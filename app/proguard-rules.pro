# Keep FocusLock entry points and OkHttp TLS/HTTP2 classes cleanly shrunk
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.focuslock.dns.model.** { *; }
