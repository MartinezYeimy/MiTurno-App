# Reglas de ProGuard/R8 (solo si activas isMinifyEnabled en release).
# Los modelos usados por Gson/Retrofit deben conservarse:
-keep class com.miturno.app.data.model.** { *; }
-keep class com.miturno.app.data.remote.** { *; }
