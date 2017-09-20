-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-dontwarn com.evernote.android.state.**
-keep class com.evernote.android.state.** { *; }
-keep class **$$StateSaver { *; }
-keepnames class * { @com.evernote.android.state.State *;}
-keepnames class * { @com.evernote.android.state.StateReflection *;}

-keep class uk.co.chrisjenx.calligraphy.* { *; }
-keep class uk.co.chrisjenx.calligraphy.*$* { *; }

-dontwarn org.mockito.**

-dontwarn org.jetbrains.anko.internals.AnkoInternals
-dontwarn com.squareup.okhttp.**