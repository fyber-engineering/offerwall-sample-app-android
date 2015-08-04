# Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Fyber Mediation

-keepattributes JavascriptInterface

-keep class android.webkit.JavascriptInterface

# Annotation for Mediation only

-dontwarn com.fyber.mediation.annotations.MediationAPI

# Google Advertising Id

-keep class com.google.android.gms.ads.identifier.** { *; }
