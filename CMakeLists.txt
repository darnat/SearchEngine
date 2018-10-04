cmake_minimum_required(VERSION 3.9.5)
set(EXECUTABLE_NAME "searchEngine")
project(SearchEngine)

find_package(Java REQUIRED)
include(UseJava)

# enable_testing()

set(CMAKE_JAVA_COMPILE_FLAGS "-source" "1.8" "-target" "1.8" "-sourcepath" "src")
# set(MAIN_CLASS "edu.csulb.PositionalInvertedIndexer") # console mode
set(MAIN_CLASS "edu.csulb.gui") # gui mode
set(OUTPUT_DIR ${PROJECT_SOURCE_DIR}/bin)

include_directories(${PROJECT_SOURCE_DIR}/src)

file(GLOB_RECURSE SRC_FILES "src/*.java")
file(GLOB_RECURSE JAR_FILES "src/*.jar")

add_jar(${EXECUTABLE_NAME}
        SOURCES ${SRC_FILES}
        INCLUDE_JARS ${JAR_FILES}
        OUTPUT_DIR "${OUTPUT_DIR}"
        ENTRY_POINT "${MAIN_CLASS}")

add_custom_target(run COMMAND java "-cp" "${OUTPUT_DIR}/${EXECUTABLE_NAME}.jar:${JAR_FILES}" "${MAIN_CLASS}")