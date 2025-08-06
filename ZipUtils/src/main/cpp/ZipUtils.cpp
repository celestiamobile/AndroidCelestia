// ZipUtils.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include <android/log.h>

#include <jni.h>
#include <zip.h>

#include <filesystem>
#include <fstream>
#include <string>
#include <vector>

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_ziputils_ZipUtils_unzip(JNIEnv *env, jclass, jstring source_path,
                                            jstring destination_folder_path) {

    const jint ZIP_ERROR = 1;
    const jint CREATE_DIRECTORY_ERROR = 2;
    const jint OPEN_FILE_ERROR = 3;
    const jint WRITE_FILE_ERROR = 4;

    static jclass exceptionContextClass = nullptr;
    static jmethodID exceptionContextInitMethod = nullptr;

    if (exceptionContextClass == nullptr)
    {
        jclass clz = env->FindClass("space/celestia/ziputils/ZipExceptionContext");
        exceptionContextClass = static_cast<jclass>(env->NewGlobalRef(static_cast<jobject>(clz)));
        exceptionContextInitMethod = env->GetMethodID(exceptionContextClass, "<init>","(ILjava/lang/String;)V");
        env->DeleteLocalRef(clz);
    }

    const char *c_sourcePath = env->GetStringUTFChars(source_path, nullptr);
    const char *c_destPath = env->GetStringUTFChars(destination_folder_path, nullptr);
    std::string source = c_sourcePath;
    std::string destinationFolder = c_destPath;
    env->ReleaseStringUTFChars(source_path, c_sourcePath);
    env->ReleaseStringUTFChars(destination_folder_path, c_destPath);

    auto archive = zip_open(source.c_str(), 0, nullptr);
    if (!archive)
        return env->NewObject(exceptionContextClass, exceptionContextInitMethod, ZIP_ERROR, static_cast<jobject>(nullptr));

    zip_file_t *currentEntry = nullptr;
    jobject errorContext = nullptr;

    for (zip_int64_t i = 0; i < zip_get_num_entries(archive, 0); i += 1)
    {
        // TODO: Check cancellation
        struct zip_stat st;
        if (zip_stat_index(archive, i, 0, &st) != 0)
        {
            errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, ZIP_ERROR, static_cast<jobject>(nullptr));
            break;
        }

        std::filesystem::path name{ st.name };
        std::vector<std::string> components;

        for (auto const& component : name)
            components.push_back(component.string());

        bool isRegularFile = name.has_filename();
        std::filesystem::path currentDirectory = destinationFolder;

        for (int j = 0; j < components.size(); ++j)
        {
            const auto& component = components[j];
            if (component.empty())
                continue;

            // The last one is the filename for file entries, we don't create directory for it
            if (!isRegularFile || j != components.size() - 1)
            {
                currentDirectory = currentDirectory / component;
                std::error_code ec;
                bool exists = std::filesystem::exists(currentDirectory, ec);

                if (ec)
                {
                    jstring jpath = env->NewStringUTF(currentDirectory.string().c_str());
                    errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, CREATE_DIRECTORY_ERROR, jpath);
                    env->DeleteLocalRef(jpath);
                    break;
                }

                if (!exists)
                {
                    bool success = std::filesystem::create_directory(currentDirectory, ec);
                    if (!success || ec)
                    {
                        jstring jpath = env->NewStringUTF(currentDirectory.string().c_str());
                        errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, CREATE_DIRECTORY_ERROR, jpath);
                        env->DeleteLocalRef(jpath);
                        break;
                    }
                }
            }
        }

        if (errorContext != nullptr)
            break;

        if (!isRegularFile)
            continue;

        currentEntry = zip_fopen_index(archive, i, 0);
        if (!currentEntry)
        {
            errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, ZIP_ERROR, static_cast<jobject>(nullptr));
            break;
        }

        std::filesystem::path filePath = currentDirectory / name.filename();
        std::ofstream file(filePath, std::ios::binary);
        if (!file.good())
        {
            jstring jpath = env->NewStringUTF(filePath.string().c_str());
            errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, OPEN_FILE_ERROR, jpath);
            env->DeleteLocalRef(jpath);
            break;
        }

        zip_uint64_t bytesWritten = 0;
        const uint32_t bufferSize = 4096;
        char buffer[bufferSize];
        while (bytesWritten != st.size)
        {
            // TODO: Check cancellation
            auto bytesRead = zip_fread(currentEntry, buffer, bufferSize);
            if (bytesRead < 0)
            {
                errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, ZIP_ERROR, static_cast<jobject>(nullptr));
                break;
            }

            if (!file.write(buffer, bytesRead).good())
            {
                jstring jpath = env->NewStringUTF(filePath.string().c_str());
                errorContext = env->NewObject(exceptionContextClass, exceptionContextInitMethod, WRITE_FILE_ERROR, jpath);
                env->DeleteLocalRef(jpath);
                break;
            }

            bytesWritten += bytesRead;
        }
        zip_fclose(currentEntry);
        currentEntry = nullptr;

        if (errorContext != nullptr)
            break;
    }

    // Clean up
    if (currentEntry != nullptr)
        zip_fclose(currentEntry);
    zip_close(archive);

    return errorContext;
}