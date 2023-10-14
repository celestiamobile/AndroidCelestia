#include <CelestiaJNI.h>
#include <ft2build.h>
#include <fmt/format.h>
#include FT_FREETYPE_H
#include <string>
#include <vector>

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_space_celestia_celestia_Font_c_1getFontNames(JNIEnv *env, jclass, jstring path) {
    static FT_Library ftlib = nullptr;
    if (ftlib == nullptr && FT_Init_FreeType(&ftlib) != 0)
        return nullptr;

    FT_Face face;
    const char *c_path = env->GetStringUTFChars(path, nullptr);
    std::string cppPath = c_path;
    env->ReleaseStringUTFChars(path, c_path);

    // Get the number of faces by passing -1 as face_index
    if (FT_New_Face(ftlib, cppPath.c_str(), -1, &face) != 0)
        return nullptr;

    FT_Long faceNum = face->num_faces;
    FT_Done_Face(face);
    if (face->num_faces <= 0)
        return nullptr;

    std::vector<std::string> fontNames;
    for (FT_Long i = 0; i < faceNum; ++i)
    {
        // If any of the faces fail, fail all
        if (FT_New_Face(ftlib, cppPath.c_str(), i, &face) != 0)
            return nullptr;

        std::string name = fmt::format("{} ({})", face->family_name, face->style_name);
        FT_Done_Face(face);
        fontNames.push_back(name);
    }
    jobjectArray results = env->NewObjectArray(static_cast<jsize>(faceNum), stringClz, nullptr);
    for (FT_Long i = 0; i < faceNum; ++i)
    {
        jstring str = env->NewStringUTF(fontNames[i].c_str());
        env->SetObjectArrayElement(results, static_cast<jsize>(i), str);
        env->DeleteLocalRef(str);
    }
    return results;
}
