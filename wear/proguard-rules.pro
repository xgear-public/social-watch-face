#Common part
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes JavascriptInterface
-keepattributes *Annotation*

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
-keepclasseswithmembernames class * {
	public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
	public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  	public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-dontwarn android.support.**
-keep class android.support.v4.** {*;}


# Support for Crashlytics
-keepattributes SourceFile,LineNumberTable

#Support for ExceptionWear
-keep class pl.tajchert.exceptionwear.** {*;}
-keep public class * implements pl.tajchert.exceptionwear.ExceptionWearHandler


#Support for Timber
-keep class timber.log.** {*;}
-keep public class * implements timber.log.Timber.Tree
-keep public class * extends timber.log.Timber.HollowTree



# GOOGLE PLAY SERVICES
-dontwarn com.google.android.gms.**
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
# Keep SafeParcelable value, needed for reflection. This is required to support backwards
# compatibility of some classes.
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
# Keep the names of classes/members we need for client functionality.
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
# Needed for Parcelable/SafeParcelable Creators to not get stripped
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
# Support for Android Advertiser ID.
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }




## Support for Timber. To remove debug logs:
#-assumenosideeffects class android.util.Log {
#    public static *** d(...);
#    public static *** v(...);
#}
#-assumenosideeffects class timber.log.Timber* {
#    public static *** d(...);
#    public static *** v(...);
#}