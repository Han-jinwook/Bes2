# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Hilt
-keep class com.bes2.app.Bes2Application { *; }
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep public class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class javax.inject.** { *; }

# Android Components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }

# Retrofit & Gson (만약 사용한다면)
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Data Classes (매우 중요: 데이터 모델이 깨지면 분석 결과가 0이 됨)
-keep class com.bes2.data.model.** { *; }
-keep class com.bes2.domain.model.** { *; }

# Worker (백그라운드 작업)
-keep class androidx.work.** { *; }
-keep class com.bes2.background.worker.** { *; }

# ML & Coil (이미지 처리)
-keep class coil.** { *; }
