# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


include_directories(src/main/jni)

add_library( # Sets the name of the library.
             LPR

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/com_LPR.cpp
             src/main/jni/tool.c
             src/main/jni/PLR_Main.c
             src/main/jni/PlateLocate.c
             src/main/jni/Array.c
             src/main/jni/PlateRecog.c
             src/main/jni/GetFeature.c
             src/main/jni/SChar.c
             src/main/jni/Simlarity.c
             src/main/jni/PreProc.c
             src/main/jni/PlateTemplate.c
             src/main/jni/svm.cpp
             src/main/jni/svm-predict.c)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       LPR

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

