# Fyber Mediation
-keep class com.fyber.mediation.MediationConfigProvider { *; }
-keep class com.fyber.mediation.MediationAdapterStarter { *; }

# ** Adapters **

# Vungle
-keep class com.vungle.** { public *; }
-keep class dagger.**
-keep class javax.inject.*
-keepattributes *Annotation*
-keepattributes Signature

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep class com.nineoldandroids.animation.** { *; }
-dontwarn com.nineoldandroids.animation.**

-keep class com.nineoldandroids.view.** { *; }
-dontwarn com.nineoldandroids.view.**

# Millennial
-keep class com.nuance.nmdp.** { *; }
-dontwarn com.nuance.nmdp.**


