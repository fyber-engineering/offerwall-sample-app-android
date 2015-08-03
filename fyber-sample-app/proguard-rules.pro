
# Butter knife

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

#-keep class com.sponsorpay.mediation.** { *;}

-keepattributes JavascriptInterface

-keep class android.webkit.JavascriptInterface

# Google Advertising Id

-keep class com.google.android.gms.ads.identifier.** { *; }

#Annotation for Mediation only
-dontwarn com.fyber.mediation.annotations.MediationAPI