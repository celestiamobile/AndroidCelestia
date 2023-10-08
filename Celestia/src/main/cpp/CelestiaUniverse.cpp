/*
 * CelestiaUniverse.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaSelection.h"
#include <celengine/body.h>
#include <celengine/universe.h>
#include <celutil/gettext.h>

#include <map>
#include <vector>
#include <string>

#include <json.hpp>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Universe_c_1getStarCatalog(JNIEnv *env, jclass clazz, jlong pointer) {
    auto u = (Universe *)pointer;
    return (jlong)u->getStarCatalog();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Universe_c_1getDSOCatalog(JNIEnv *env, jclass clazz, jlong pointer) {
    auto u = (Universe *)pointer;
    return (jlong)u->getDSOCatalog();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Universe_c_1findObject(JNIEnv *env, jclass clazz, jlong pointer, jstring name) {
    auto u = (Universe *)pointer;
    const char *str = env->GetStringUTFChars(name, nullptr);
    auto sel = u->find(str, {});
    env->ReleaseStringUTFChars(name, str);
    return selectionAsJavaSelection(env, sel);
}

using namespace std;

using json = nlohmann::json;

const static std::string BROWSER_ITEM_NAME_KEY = "name";
const static std::string BROWSER_ITEM_TYPE_KEY = "type";
const static jint BROWSER_ITEM_TYPE_BODY = 0;
const static jint BROWSER_ITEM_TYPE_LOCATION = 1;
const static std::string BROWSER_ITEM_POINTER_KEY = "pointer";
const static std::string BROWSER_ITEM_CHILDREN_KEY = "children";

static json create_browser_item(std::string name, jint key, map<string, pair<jlong, string>> mp) {
    json j;
    j[BROWSER_ITEM_NAME_KEY] = name;
    json items;
    map<string, pair<jlong, string>>::iterator iter;
    for(iter = mp.begin(); iter != mp.end(); iter++) {
        json item;
        item[BROWSER_ITEM_POINTER_KEY] = iter->second.first;
        item[BROWSER_ITEM_NAME_KEY] = iter->second.second;
        item[BROWSER_ITEM_TYPE_KEY] = key;
        items[iter->first] = item;
    }
    j[BROWSER_ITEM_CHILDREN_KEY] = items;
    return j;
}

static void create_browser_item_and_add(json &parent, std::string name, int key,  map<string, pair<jlong, string>> mp) {
    parent[name] = create_browser_item(name, key, mp);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Universe_c_1getChildrenForStar(JNIEnv *env, jclass clazz, jlong ptr, jlong pointer) {
    auto u = (Universe *)ptr;
    SolarSystem *ss = u->getSolarSystem((Star *)pointer);

    PlanetarySystem* sys = NULL;
    if (ss) sys = ss->getPlanets();

    json j;

    if (sys) {
        int sysSize = sys->getSystemSize();
        map<string, pair<jlong, string>> topLevel;
        map<string, pair<jlong, string>> planets;
        map<string, pair<jlong, string>> dwarfPlanets;
        map<string, pair<jlong, string>> minorMoons;
        map<string, pair<jlong, string>> asteroids;
        map<string, pair<jlong, string>> comets;
        map<string, pair<jlong, string>> spacecrafts;

        for (int i = 0; i < sysSize; i++) {
            Body* body = sys->getBody(i);
            if (body->getName().empty())
                continue;

            string name = body->getName(true).c_str();
            auto jitem = make_pair((jlong)body, name);

            int bodyClass  = body->getClassification();

            switch (bodyClass)
            {
                case Body::Invisible:
                case Body::Diffuse:
                case Body::Component:
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

        map<string, pair<jlong, string>>::iterator iter;
        for(iter = topLevel.begin(); iter != topLevel.end(); iter++) {
            json item;
            item[BROWSER_ITEM_POINTER_KEY] = iter->second.first;
            item[BROWSER_ITEM_NAME_KEY] = iter->second.second;
            item[BROWSER_ITEM_TYPE_KEY] = BROWSER_ITEM_TYPE_BODY;
            j[iter->first] = item;
        }

        if (!planets.empty())
            create_browser_item_and_add(j, _("Planets"), BROWSER_ITEM_TYPE_BODY, planets);

        if (!dwarfPlanets.empty())
            create_browser_item_and_add(j, _("Dwarf Planets"), BROWSER_ITEM_TYPE_BODY, dwarfPlanets);

        if (!minorMoons.empty())
            create_browser_item_and_add(j, _("Minor Moons"), BROWSER_ITEM_TYPE_BODY, minorMoons);

        if (!asteroids.empty())
            create_browser_item_and_add(j, _("Asteroids"), BROWSER_ITEM_TYPE_BODY, asteroids);

        if (!comets.empty())
            create_browser_item_and_add(j, _("Comets"), BROWSER_ITEM_TYPE_BODY, comets);

        if (!spacecrafts.empty())
            create_browser_item_and_add(j, _("Spacecraft"), BROWSER_ITEM_TYPE_BODY, spacecrafts);
    }

    return env->NewStringUTF(j.dump(-1, ' ', false, nlohmann::detail::error_handler_t::replace).c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Universe_c_1getChildrenForBody(JNIEnv *env, jclass clazz, jlong ptr, jlong pointer) {
    auto u = (Universe *)ptr;

    Body *b = (Body *)pointer;
    PlanetarySystem* sys = b->getSatellites();

    json j;

    if (sys)
    {
        int sysSize = sys->getSystemSize();

        map<string, pair<jlong, string>> topLevel;
        map<string, pair<jlong, string>> minorMoons;
        map<string, pair<jlong, string>> comets;
        map<string, pair<jlong, string>> spacecrafts;

        int i;
        for (i = 0; i < sysSize; i++)
        {
            Body* body = sys->getBody(i);
            if (body->getName().empty())
                continue;

            string name = body->getName(true).c_str();
            auto jitem = make_pair((jlong)body, name);

            int bodyClass  = body->getClassification();

            if (bodyClass==Body::Asteroid) bodyClass = Body::Moon;

            switch (bodyClass)
            {
                case Body::Invisible:
                case Body::Diffuse:
                case Body::Component:
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

        map<string, pair<jlong, string>>::iterator iter;
        for(iter = topLevel.begin(); iter != topLevel.end(); iter++) {
            json item;
            item[BROWSER_ITEM_POINTER_KEY] = iter->second.first;
            item[BROWSER_ITEM_NAME_KEY] = iter->second.second;
            item[BROWSER_ITEM_TYPE_KEY] = BROWSER_ITEM_TYPE_BODY;
            j[iter->first] = item;
        }

        if (!minorMoons.empty())
            create_browser_item_and_add(j, _("Minor Moons"), BROWSER_ITEM_TYPE_BODY, minorMoons);

        if (!comets.empty())
            create_browser_item_and_add(j, _("Comets"), BROWSER_ITEM_TYPE_BODY, comets);

        if (!spacecrafts.empty())
            create_browser_item_and_add(j, _("Spacecraft"), BROWSER_ITEM_TYPE_BODY, spacecrafts);
    }

    auto locations = b->getLocations();
    if (locations.has_value() && !locations->empty())
    {
        std::map<string, pair<jlong, string>> locationsMap;
        for (const auto loc : *locations)
        {
            auto name = loc->getName(true);
            if (name.empty())
                continue;

            locationsMap[name] = make_pair(reinterpret_cast<jlong>(loc), name);
        }
        if (!locationsMap.empty())
            create_browser_item_and_add(j, _("Locations"), BROWSER_ITEM_TYPE_LOCATION, locationsMap);
    }

    return env->NewStringUTF(j.dump(-1, ' ', false, nlohmann::detail::error_handler_t::replace).c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Universe_c_1mark(JNIEnv *env, jclass clazz,
                                                         jlong ptr, jobject selection,
                                                         jint marker) {
    auto u = (Universe *)ptr;
    u->markObject(javaSelectionAsSelection(env, selection), celestia::MarkerRepresentation(celestia::MarkerRepresentation::Symbol(marker), 10.0f, Color(0.0f, 1.0f, 0.0f, 0.9f)), 1);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Universe_c_1unmark(JNIEnv *env, jclass clazz,
                                                           jlong ptr, jobject selection) {
    auto u = (Universe *)ptr;
    u->unmarkObject(javaSelectionAsSelection(env, selection), 1);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Universe_c_1unmarkAll(JNIEnv *env, jclass clazz,
                                                              jlong ptr) {
    auto u = (Universe *)ptr;
    u->unmarkAll();
}