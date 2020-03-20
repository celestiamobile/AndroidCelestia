#include "CelestiaJNI.h"
#include <celengine/universe.h>
#include <celutil/gettext.h>

#include <map>
#include <vector>
#include <string>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1getStarCatalog(JNIEnv *env,
                                                                           jobject thiz) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);
    return (jlong)u->getStarCatalog();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1getDSOCatalog(JNIEnv *env,
                                                                          jobject thiz) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);
    return (jlong)u->getDSOCatalog();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1findObject(JNIEnv *env, jobject thiz,
                                                                       jstring name) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);
    const char *str = env->GetStringUTFChars(name, nullptr);
    Selection *sel = new Selection(u->find(str));
    env->ReleaseStringUTFChars(name, str);
    return (jlong)sel;
}

using namespace std;

static jobject create_browser_item(JNIEnv *env, std::string name, map<string, jobject> mp) {
    jobject hashmap = env->NewObject(hmClz, hmiMethodID);
    map<string, jobject>::iterator iter;
    for(iter = mp.begin(); iter != mp.end(); iter++) {
        env->CallObjectMethod(hashmap, hmpMethodID, env->NewStringUTF(iter->first.c_str()), iter->second);
    }
    return env->NewObject(cbiClz, cbii2MethodID, env->NewStringUTF(name.c_str()), hashmap);
}

static void create_browser_item_and_add(JNIEnv *env, jobject parent, std::string name, map<string, jobject> mp) {
    env->CallObjectMethod(parent, hmpMethodID, env->NewStringUTF(name.c_str()), create_browser_item(env, name, mp));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1getChildrenForStar(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong pointer) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);
    SolarSystem *ss = u->getSolarSystem((Star *)pointer);

    PlanetarySystem* sys = NULL;
    if (ss) sys = ss->getPlanets();

    jobject hashmap = env->NewObject(hmClz, hmiMethodID);

    if (sys) {
        int sysSize = sys->getSystemSize();
        map<string, jobject> topLevel;
        map<string, jobject> planets;
        map<string, jobject> dwarfPlanets;
        map<string, jobject> minorMoons;
        map<string, jobject> asteroids;
        map<string, jobject> comets;
        map<string, jobject> spacecrafts;

        for (int i = 0; i < sysSize; i++) {
            Body* body = sys->getBody(i);
            if (body->getName().empty())
                continue;

            string name = body->getName(true).c_str();
            jobject jbody = env->NewObject(cbClz, cbiMethodID, (jlong)body);
            jobject jitem = env->NewObject(cbiClz, cbii1MethodID, env->NewStringUTF(name.c_str()), jbody, thiz);

            int bodyClass  = body->getClassification();

            switch (bodyClass)
            {
                case Body::Invisible:
                    continue;
                case Body::Planet:
                    planets[name] = jitem;
                    break;
                case Body::DwarfPlanet:
                    dwarfPlanets[name] = jitem;
                    break;
                case Body::Moon:
                case Body::MinorMoon:
                    if (body->getRadius() < 100.0f || Body::MinorMoon == bodyClass)
                        minorMoons[name] = jitem;
                    else
                        topLevel[name] = jitem;
                    break;
                case Body::Asteroid:
                    asteroids[name] = jitem;
                    break;
                case Body::Comet:
                    comets[name] = jitem;
                    break;
                case Body::Spacecraft:
                    spacecrafts[name] = jitem;
                    break;
                default:
                    topLevel[name] = jitem;
                    break;
            }
        }

        map<string, jobject>::iterator iter;
        for(iter = topLevel.begin(); iter != topLevel.end(); iter++)
            env->CallObjectMethod(hashmap, hmpMethodID, env->NewStringUTF(iter->first.c_str()), iter->second);

        if (!planets.empty())
            create_browser_item_and_add(env, hashmap, _("Planets"), planets);

        if (!dwarfPlanets.empty())
            create_browser_item_and_add(env, hashmap, _("Dwarf Planets"), dwarfPlanets);

        if (!minorMoons.empty())
            create_browser_item_and_add(env, hashmap, _("Minor Moons"), minorMoons);

        if (!asteroids.empty())
            create_browser_item_and_add(env, hashmap, _("Asteroids"), asteroids);

        if (!comets.empty())
            create_browser_item_and_add(env, hashmap, _("Comets"), comets);

        if (!spacecrafts.empty())
            create_browser_item_and_add(env, hashmap, _("Spacecrafts"), spacecrafts);
    }

    return hashmap;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1getChildrenForBody(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong pointer) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);

    Body *b = (Body *)pointer;
    PlanetarySystem* sys = b->getSatellites();

    jobject hashmap = env->NewObject(hmClz, hmiMethodID);
    map<string, jobject> topLevel;

    if (sys)
    {
        int sysSize = sys->getSystemSize();

        map<string, jobject> minorMoons;
        map<string, jobject> comets;
        map<string, jobject> spacecrafts;

        int i;
        for (i = 0; i < sysSize; i++)
        {
            Body* body = sys->getBody(i);
            if (body->getName().empty())
                continue;

            string name = body->getName(true).c_str();
            jobject jbody = env->NewObject(cbClz, cbiMethodID, (jlong)body);
            jobject jitem = env->NewObject(cbiClz, cbii1MethodID, env->NewStringUTF(name.c_str()), jbody, thiz);

            int bodyClass  = body->getClassification();

            if (bodyClass==Body::Asteroid) bodyClass = Body::Moon;

            switch (bodyClass)
            {
                case Body::Invisible:
                    continue;
                case Body::Moon:
                case Body::MinorMoon:
                    if (body->getRadius() < 100.0f || Body::MinorMoon == bodyClass)
                        minorMoons[name] = jitem;
                    else
                        topLevel[name] = jitem;
                    break;
                case Body::Comet:
                    comets[name] = jitem;
                    break;
                case Body::Spacecraft:
                    spacecrafts[name] = jitem;
                    break;
                default:
                    topLevel[name] = jitem;
                    break;
            }
        }

        if (!minorMoons.empty())
            create_browser_item_and_add(env, hashmap, _("Minor Moons"), minorMoons);

        if (!comets.empty())
            create_browser_item_and_add(env, hashmap, _("Comets"), comets);

        if (!spacecrafts.empty())
            create_browser_item_and_add(env, hashmap, _("Spacecrafts"), spacecrafts);
    }

    map<string, jobject>::iterator iter;
    for(iter = topLevel.begin(); iter != topLevel.end(); iter++)
        env->CallObjectMethod(hashmap, hmpMethodID, env->NewStringUTF(iter->first.c_str()), iter->second);

    vector<Location *>* locations = b->getLocations();
    if (locations != nullptr)
    {
        map<string, jobject> locationsMap;

        vector<Location *>::iterator iter;
        for (iter = locations->begin(); iter != locations->end(); iter++)
        {
            string name = (*iter)->getName(true).c_str();
            jobject jlocation = env->NewObject(clClz, cliMethodID, (jlong)*iter);
            jobject jitem = env->NewObject(cbiClz, cbii1MethodID, env->NewStringUTF(name.c_str()), jlocation, thiz);
            locationsMap[name] = jitem;
        }
        if (!locationsMap.empty())
            create_browser_item_and_add(env, hashmap, _("Locations"), locationsMap);
    }

    return hashmap;
}