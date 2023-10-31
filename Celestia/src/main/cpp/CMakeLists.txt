cmake_minimum_required(VERSION 3.4.1)

set(CELESTIA_DIR ${CMAKE_SOURCE_DIR}/../../../../../Celestia)
set(CELESTIA_SRC_DIR "${CELESTIA_DIR}/src")
set(CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celestia/audiosession.cpp
        ${CELESTIA_SRC_DIR}/celestia/audiosession.h
        ${CELESTIA_SRC_DIR}/celestia/celestiacore.cpp
        ${CELESTIA_SRC_DIR}/celestia/celestiacore.h
        ${CELESTIA_SRC_DIR}/celestia/celestiastate.cpp
        ${CELESTIA_SRC_DIR}/celestia/celestiastate.h
        ${CELESTIA_SRC_DIR}/celestia/configfile.cpp
        ${CELESTIA_SRC_DIR}/celestia/configfile.h
        ${CELESTIA_SRC_DIR}/celestia/destination.cpp
        ${CELESTIA_SRC_DIR}/celestia/destination.h
        ${CELESTIA_SRC_DIR}/celestia/eclipsefinder.cpp
        ${CELESTIA_SRC_DIR}/celestia/eclipsefinder.h
        ${CELESTIA_SRC_DIR}/celestia/favorites.cpp
        ${CELESTIA_SRC_DIR}/celestia/favorites.h
        ${CELESTIA_SRC_DIR}/celestia/helper.cpp
        ${CELESTIA_SRC_DIR}/celestia/helper.h
        ${CELESTIA_SRC_DIR}/celestia/miniaudiosession.cpp
        ${CELESTIA_SRC_DIR}/celestia/miniaudiosession.h
        ${CELESTIA_SRC_DIR}/celestia/moviecapture.h
        ${CELESTIA_SRC_DIR}/celestia/scriptmenu.cpp
        ${CELESTIA_SRC_DIR}/celestia/scriptmenu.h
        ${CELESTIA_SRC_DIR}/celestia/textprintposition.cpp
        ${CELESTIA_SRC_DIR}/celestia/textprintposition.h
        ${CELESTIA_SRC_DIR}/celestia/url.cpp
        ${CELESTIA_SRC_DIR}/celestia/url.h
        ${CELESTIA_SRC_DIR}/celestia/view.cpp
        ${CELESTIA_SRC_DIR}/celestia/view.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celttf/truetypefont.cpp
        ${CELESTIA_SRC_DIR}/celttf/truetypefont.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celutil/arrayvector.h
        ${CELESTIA_SRC_DIR}/celutil/binaryread.h
        ${CELESTIA_SRC_DIR}/celutil/binarywrite.h
        ${CELESTIA_SRC_DIR}/celutil/blockarray.h
        ${CELESTIA_SRC_DIR}/celutil/bytes.h
        ${CELESTIA_SRC_DIR}/celutil/color.cpp
        ${CELESTIA_SRC_DIR}/celutil/color.h
        ${CELESTIA_SRC_DIR}/celutil/filetype.cpp
        ${CELESTIA_SRC_DIR}/celutil/filetype.h
        ${CELESTIA_SRC_DIR}/celutil/flag.h
        ${CELESTIA_SRC_DIR}/celutil/formatnum.cpp
        ${CELESTIA_SRC_DIR}/celutil/formatnum.h
        ${CELESTIA_SRC_DIR}/celutil/fsutils.cpp
        ${CELESTIA_SRC_DIR}/celutil/fsutils.h
        ${CELESTIA_SRC_DIR}/celutil/greek.cpp
        ${CELESTIA_SRC_DIR}/celutil/greek.h
        ${CELESTIA_SRC_DIR}/celutil/indexlist.h
        ${CELESTIA_SRC_DIR}/celutil/intrusiveptr.h
        ${CELESTIA_SRC_DIR}/celutil/logger.cpp
        ${CELESTIA_SRC_DIR}/celutil/logger.h
        ${CELESTIA_SRC_DIR}/celutil/ranges.h
        ${CELESTIA_SRC_DIR}/celutil/r128.h
        ${CELESTIA_SRC_DIR}/celutil/r128util.cpp
        ${CELESTIA_SRC_DIR}/celutil/r128util.h
        ${CELESTIA_SRC_DIR}/celutil/reshandle.h
        ${CELESTIA_SRC_DIR}/celutil/resmanager.h
        ${CELESTIA_SRC_DIR}/celutil/stringutils.h
        ${CELESTIA_SRC_DIR}/celutil/stringutils.cpp
        ${CELESTIA_SRC_DIR}/celutil/strnatcmp.h
        ${CELESTIA_SRC_DIR}/celutil/strnatcmp.cpp
        ${CELESTIA_SRC_DIR}/celutil/timer.cpp
        ${CELESTIA_SRC_DIR}/celutil/timer.h
        ${CELESTIA_SRC_DIR}/celutil/tokenizer.cpp
        ${CELESTIA_SRC_DIR}/celutil/tokenizer.h
        ${CELESTIA_SRC_DIR}/celutil/tzutil.cpp
        ${CELESTIA_SRC_DIR}/celutil/tzutil.h
        ${CELESTIA_SRC_DIR}/celutil/unicode.cpp
        ${CELESTIA_SRC_DIR}/celutil/unicode.h
        ${CELESTIA_SRC_DIR}/celutil/utf8.cpp
        ${CELESTIA_SRC_DIR}/celutil/utf8.h
        ${CELESTIA_SRC_DIR}/celutil/watcher.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celscript/common/script.cpp
        ${CELESTIA_SRC_DIR}/celscript/common/script.h
        ${CELESTIA_SRC_DIR}/celscript/common/scriptmaps.cpp
        ${CELESTIA_SRC_DIR}/celscript/common/scriptmaps.h
        ${CELESTIA_SRC_DIR}/celscript/legacy/cmdparser.cpp
        ${CELESTIA_SRC_DIR}/celscript/legacy/cmdparser.h
        ${CELESTIA_SRC_DIR}/celscript/legacy/command.cpp
        ${CELESTIA_SRC_DIR}/celscript/legacy/command.h
        ${CELESTIA_SRC_DIR}/celscript/legacy/execenv.h
        ${CELESTIA_SRC_DIR}/celscript/legacy/execution.cpp
        ${CELESTIA_SRC_DIR}/celscript/legacy/execution.h
        ${CELESTIA_SRC_DIR}/celscript/legacy/legacyscript.cpp
        ${CELESTIA_SRC_DIR}/celscript/legacy/legacyscript.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_category.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_category.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_celestia.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_celestia.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_frame.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_frame.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_gl.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_gl.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_internal.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_misc.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_misc.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_object.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_object.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_observer.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_observer.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_phase.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_phase.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_position.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_position.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_rotation.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_rotation.h
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_vector.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/celx_vector.h
        ${CELESTIA_SRC_DIR}/celscript/lua/glcompat.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/glcompat.h
        ${CELESTIA_SRC_DIR}/celscript/lua/luascript.cpp
        ${CELESTIA_SRC_DIR}/celscript/lua/luascript.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celcompat/charconv.h
        ${CELESTIA_SRC_DIR}/celcompat/charconv_impl.cpp
        ${CELESTIA_SRC_DIR}/celcompat/charconv_impl.h
        ${CELESTIA_SRC_DIR}/celcompat/numbers.h
        ${CELESTIA_SRC_DIR}/celcompat/numbers_impl.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celmodel/material.cpp
        ${CELESTIA_SRC_DIR}/celmodel/material.h
        ${CELESTIA_SRC_DIR}/celmodel/mesh.cpp
        ${CELESTIA_SRC_DIR}/celmodel/mesh.h
        ${CELESTIA_SRC_DIR}/celmodel/model.cpp
        ${CELESTIA_SRC_DIR}/celmodel/modelfile.cpp
        ${CELESTIA_SRC_DIR}/celmodel/modelfile.h
        ${CELESTIA_SRC_DIR}/celmodel/model.h
        ${CELESTIA_SRC_DIR}/celmodel/tangents.cpp
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celephem/customorbit.cpp
        ${CELESTIA_SRC_DIR}/celephem/customorbit.h
        ${CELESTIA_SRC_DIR}/celephem/customrotation.cpp
        ${CELESTIA_SRC_DIR}/celephem/customrotation.h
        ${CELESTIA_SRC_DIR}/celephem/jpleph.cpp
        ${CELESTIA_SRC_DIR}/celephem/jpleph.h
        ${CELESTIA_SRC_DIR}/celephem/nutation.cpp
        ${CELESTIA_SRC_DIR}/celephem/nutation.h
        ${CELESTIA_SRC_DIR}/celephem/orbit.cpp
        ${CELESTIA_SRC_DIR}/celephem/orbit.h
        ${CELESTIA_SRC_DIR}/celephem/precession.cpp
        ${CELESTIA_SRC_DIR}/celephem/precession.h
        ${CELESTIA_SRC_DIR}/celephem/rotation.cpp
        ${CELESTIA_SRC_DIR}/celephem/rotation.h
        ${CELESTIA_SRC_DIR}/celephem/sampfile.cpp
        ${CELESTIA_SRC_DIR}/celephem/sampfile.h
        ${CELESTIA_SRC_DIR}/celephem/samporbit.cpp
        ${CELESTIA_SRC_DIR}/celephem/samporbit.h
        ${CELESTIA_SRC_DIR}/celephem/samporient.cpp
        ${CELESTIA_SRC_DIR}/celephem/samporient.h
        ${CELESTIA_SRC_DIR}/celephem/vsop87.cpp
        ${CELESTIA_SRC_DIR}/celephem/vsop87.h
        ${CELESTIA_SRC_DIR}/celephem/scriptobject.cpp
        ${CELESTIA_SRC_DIR}/celephem/scriptobject.h
        ${CELESTIA_SRC_DIR}/celephem/scriptorbit.cpp
        ${CELESTIA_SRC_DIR}/celephem/scriptorbit.h
        ${CELESTIA_SRC_DIR}/celephem/scriptrotation.cpp
        ${CELESTIA_SRC_DIR}/celephem/scriptrotation.h
        ${CELESTIA_SRC_DIR}/celephem/spiceinterface.cpp
        ${CELESTIA_SRC_DIR}/celephem/spiceinterface.h
        ${CELESTIA_SRC_DIR}/celephem/spiceorbit.cpp
        ${CELESTIA_SRC_DIR}/celephem/spiceorbit.h
        ${CELESTIA_SRC_DIR}/celephem/spicerotation.cpp
        ${CELESTIA_SRC_DIR}/celephem/spicerotation.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celrender/asterismrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/asterismrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/atmosphererenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/atmosphererenderer.h
        ${CELESTIA_SRC_DIR}/celrender/boundariesrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/boundariesrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/cometrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/cometrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/eclipticlinerenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/eclipticlinerenderer.h
        ${CELESTIA_SRC_DIR}/celrender/galaxyrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/galaxyrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/globularrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/globularrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/largestarrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/largestarrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/linerenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/linerenderer.h
        ${CELESTIA_SRC_DIR}/celrender/nebularenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/nebularenderer.h
        ${CELESTIA_SRC_DIR}/celrender/openclusterrenderer.cpp
        ${CELESTIA_SRC_DIR}/celrender/openclusterrenderer.h
        ${CELESTIA_SRC_DIR}/celrender/rendererfwd.h
        ${CELESTIA_SRC_DIR}/celrender/gl/buffer.cpp
        ${CELESTIA_SRC_DIR}/celrender/gl/buffer.h
        ${CELESTIA_SRC_DIR}/celrender/gl/vertexobject.cpp
        ${CELESTIA_SRC_DIR}/celrender/gl/vertexobject.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celengine/asterism.cpp
        ${CELESTIA_SRC_DIR}/celengine/asterism.h
        ${CELESTIA_SRC_DIR}/celengine/astro.cpp
        ${CELESTIA_SRC_DIR}/celengine/astro.h
        ${CELESTIA_SRC_DIR}/celengine/astroobj.h
        ${CELESTIA_SRC_DIR}/celengine/atmosphere.h
        ${CELESTIA_SRC_DIR}/celengine/axisarrow.cpp
        ${CELESTIA_SRC_DIR}/celengine/axisarrow.h
        ${CELESTIA_SRC_DIR}/celengine/body.cpp
        ${CELESTIA_SRC_DIR}/celengine/body.h
        ${CELESTIA_SRC_DIR}/celengine/boundaries.cpp
        ${CELESTIA_SRC_DIR}/celengine/boundaries.h
        ${CELESTIA_SRC_DIR}/celengine/category.cpp
        ${CELESTIA_SRC_DIR}/celengine/category.h
        ${CELESTIA_SRC_DIR}/celengine/console.cpp
        ${CELESTIA_SRC_DIR}/celengine/console.h
        ${CELESTIA_SRC_DIR}/celengine/constellation.cpp
        ${CELESTIA_SRC_DIR}/celengine/constellation.h
        ${CELESTIA_SRC_DIR}/celengine/curveplot.cpp
        ${CELESTIA_SRC_DIR}/celengine/curveplot.h
        ${CELESTIA_SRC_DIR}/celengine/dateformatter.cpp
        ${CELESTIA_SRC_DIR}/celengine/dateformatter.h
        ${CELESTIA_SRC_DIR}/celengine/deepskyobj.cpp
        ${CELESTIA_SRC_DIR}/celengine/deepskyobj.h
        ${CELESTIA_SRC_DIR}/celengine/dsodb.cpp
        ${CELESTIA_SRC_DIR}/celengine/dsodb.h
        ${CELESTIA_SRC_DIR}/celengine/dsoname.cpp
        ${CELESTIA_SRC_DIR}/celengine/dsoname.h
        ${CELESTIA_SRC_DIR}/celengine/dsooctree.cpp
        ${CELESTIA_SRC_DIR}/celengine/dsooctree.h
        ${CELESTIA_SRC_DIR}/celengine/dsorenderer.cpp
        ${CELESTIA_SRC_DIR}/celengine/dsorenderer.h
        ${CELESTIA_SRC_DIR}/celengine/fisheyeprojectionmode.cpp
        ${CELESTIA_SRC_DIR}/celengine/fisheyeprojectionmode.h
        ${CELESTIA_SRC_DIR}/celengine/frame.cpp
        ${CELESTIA_SRC_DIR}/celengine/frame.h
        ${CELESTIA_SRC_DIR}/celengine/framebuffer.cpp
        ${CELESTIA_SRC_DIR}/celengine/framebuffer.h
        ${CELESTIA_SRC_DIR}/celengine/frametree.cpp
        ${CELESTIA_SRC_DIR}/celengine/frametree.h
        ${CELESTIA_SRC_DIR}/celengine/galaxy.cpp
        ${CELESTIA_SRC_DIR}/celengine/galaxy.h
        ${CELESTIA_SRC_DIR}/celengine/galaxyform.cpp
        ${CELESTIA_SRC_DIR}/celengine/galaxyform.h
        ${CELESTIA_SRC_DIR}/celengine/geometry.h
        ${CELESTIA_SRC_DIR}/celengine/glsupport.cpp
        ${CELESTIA_SRC_DIR}/celengine/glsupport.h
        ${CELESTIA_SRC_DIR}/celengine/glmarker.cpp
        ${CELESTIA_SRC_DIR}/celengine/globular.cpp
        ${CELESTIA_SRC_DIR}/celengine/globular.h
        ${CELESTIA_SRC_DIR}/celengine/glshader.cpp
        ${CELESTIA_SRC_DIR}/celengine/glshader.h
        ${CELESTIA_SRC_DIR}/celengine/hash.cpp
        ${CELESTIA_SRC_DIR}/celengine/hash.h
        ${CELESTIA_SRC_DIR}/celengine/lightenv.h
        ${CELESTIA_SRC_DIR}/celengine/location.cpp
        ${CELESTIA_SRC_DIR}/celengine/location.h
        ${CELESTIA_SRC_DIR}/celengine/lodspheremesh.cpp
        ${CELESTIA_SRC_DIR}/celengine/lodspheremesh.h
        ${CELESTIA_SRC_DIR}/celengine/marker.cpp
        ${CELESTIA_SRC_DIR}/celengine/marker.h
        ${CELESTIA_SRC_DIR}/celengine/meshmanager.cpp
        ${CELESTIA_SRC_DIR}/celengine/meshmanager.h
        ${CELESTIA_SRC_DIR}/celengine/mapmanager.cpp
        ${CELESTIA_SRC_DIR}/celengine/mapmanager.h
        ${CELESTIA_SRC_DIR}/celengine/modelgeometry.cpp
        ${CELESTIA_SRC_DIR}/celengine/modelgeometry.h
        ${CELESTIA_SRC_DIR}/celengine/multitexture.cpp
        ${CELESTIA_SRC_DIR}/celengine/multitexture.h
        ${CELESTIA_SRC_DIR}/celengine/name.cpp
        ${CELESTIA_SRC_DIR}/celengine/name.h
        ${CELESTIA_SRC_DIR}/celengine/nebula.cpp
        ${CELESTIA_SRC_DIR}/celengine/nebula.h
        ${CELESTIA_SRC_DIR}/celengine/objectrenderer.h
        ${CELESTIA_SRC_DIR}/celengine/observer.cpp
        ${CELESTIA_SRC_DIR}/celengine/observer.h
        ${CELESTIA_SRC_DIR}/celengine/octree.h
        ${CELESTIA_SRC_DIR}/celengine/opencluster.cpp
        ${CELESTIA_SRC_DIR}/celengine/opencluster.h
        ${CELESTIA_SRC_DIR}/celengine/orbitsampler.h
        ${CELESTIA_SRC_DIR}/celengine/overlay.cpp
        ${CELESTIA_SRC_DIR}/celengine/overlay.h
        ${CELESTIA_SRC_DIR}/celengine/overlayimage.cpp
        ${CELESTIA_SRC_DIR}/celengine/overlayimage.h
        ${CELESTIA_SRC_DIR}/celengine/parseobject.cpp
        ${CELESTIA_SRC_DIR}/celengine/parseobject.h
        ${CELESTIA_SRC_DIR}/celengine/parser.cpp
        ${CELESTIA_SRC_DIR}/celengine/parser.h
        ${CELESTIA_SRC_DIR}/celengine/perspectiveprojectionmode.cpp
        ${CELESTIA_SRC_DIR}/celengine/perspectiveprojectionmode.h
        ${CELESTIA_SRC_DIR}/celengine/planetgrid.cpp
        ${CELESTIA_SRC_DIR}/celengine/planetgrid.h
        ${CELESTIA_SRC_DIR}/celengine/pointstarrenderer.cpp
        ${CELESTIA_SRC_DIR}/celengine/pointstarrenderer.h
        ${CELESTIA_SRC_DIR}/celengine/pointstarvertexbuffer.cpp
        ${CELESTIA_SRC_DIR}/celengine/pointstarvertexbuffer.h
        ${CELESTIA_SRC_DIR}/celengine/projectionmode.cpp
        ${CELESTIA_SRC_DIR}/celengine/projectionmode.h
        ${CELESTIA_SRC_DIR}/celengine/rectangle.h
        ${CELESTIA_SRC_DIR}/celengine/referencemark.h
        ${CELESTIA_SRC_DIR}/celengine/rendcontext.cpp
        ${CELESTIA_SRC_DIR}/celengine/rendcontext.h
        ${CELESTIA_SRC_DIR}/celengine/render.cpp
        ${CELESTIA_SRC_DIR}/celengine/render.h
        ${CELESTIA_SRC_DIR}/celengine/renderglsl.cpp
        ${CELESTIA_SRC_DIR}/celengine/renderglsl.h
        ${CELESTIA_SRC_DIR}/celengine/renderinfo.h
        ${CELESTIA_SRC_DIR}/celengine/renderlistentry.h
        ${CELESTIA_SRC_DIR}/celengine/rotationmanager.cpp
        ${CELESTIA_SRC_DIR}/celengine/rotationmanager.h
        ${CELESTIA_SRC_DIR}/celengine/selection.cpp
        ${CELESTIA_SRC_DIR}/celengine/selection.h
        ${CELESTIA_SRC_DIR}/celengine/shadermanager.cpp
        ${CELESTIA_SRC_DIR}/celengine/shadermanager.h
        ${CELESTIA_SRC_DIR}/celengine/shared.h
        ${CELESTIA_SRC_DIR}/celengine/simulation.cpp
        ${CELESTIA_SRC_DIR}/celengine/simulation.h
        ${CELESTIA_SRC_DIR}/celengine/skygrid.cpp
        ${CELESTIA_SRC_DIR}/celengine/skygrid.h
        ${CELESTIA_SRC_DIR}/celengine/solarsys.cpp
        ${CELESTIA_SRC_DIR}/celengine/solarsys.h
        ${CELESTIA_SRC_DIR}/celengine/spheremesh.cpp
        ${CELESTIA_SRC_DIR}/celengine/spheremesh.h
        ${CELESTIA_SRC_DIR}/celengine/starbrowser.cpp
        ${CELESTIA_SRC_DIR}/celengine/starbrowser.h
        ${CELESTIA_SRC_DIR}/celengine/starcolors.cpp
        ${CELESTIA_SRC_DIR}/celengine/starcolors.h
        ${CELESTIA_SRC_DIR}/celengine/star.cpp
        ${CELESTIA_SRC_DIR}/celengine/star.h
        ${CELESTIA_SRC_DIR}/celengine/stardb.cpp
        ${CELESTIA_SRC_DIR}/celengine/stardb.h
        ${CELESTIA_SRC_DIR}/celengine/starname.cpp
        ${CELESTIA_SRC_DIR}/celengine/starname.h
        ${CELESTIA_SRC_DIR}/celengine/staroctree.cpp
        ${CELESTIA_SRC_DIR}/celengine/staroctree.h
        ${CELESTIA_SRC_DIR}/celengine/stellarclass.cpp
        ${CELESTIA_SRC_DIR}/celengine/stellarclass.h
        ${CELESTIA_SRC_DIR}/celengine/surface.h
        ${CELESTIA_SRC_DIR}/celengine/texmanager.cpp
        ${CELESTIA_SRC_DIR}/celengine/texmanager.h
        ${CELESTIA_SRC_DIR}/celengine/textlayout.cpp
        ${CELESTIA_SRC_DIR}/celengine/textlayout.h
        ${CELESTIA_SRC_DIR}/celengine/texture.cpp
        ${CELESTIA_SRC_DIR}/celengine/texture.h
        ${CELESTIA_SRC_DIR}/celengine/timeline.cpp
        ${CELESTIA_SRC_DIR}/celengine/timeline.h
        ${CELESTIA_SRC_DIR}/celengine/timelinephase.cpp
        ${CELESTIA_SRC_DIR}/celengine/timelinephase.h
        ${CELESTIA_SRC_DIR}/celengine/trajmanager.cpp
        ${CELESTIA_SRC_DIR}/celengine/trajmanager.h
        ${CELESTIA_SRC_DIR}/celengine/univcoord.h
        ${CELESTIA_SRC_DIR}/celengine/universe.cpp
        ${CELESTIA_SRC_DIR}/celengine/universe.h
        ${CELESTIA_SRC_DIR}/celengine/value.cpp
        ${CELESTIA_SRC_DIR}/celengine/value.h
        ${CELESTIA_SRC_DIR}/celengine/viewporteffect.cpp
        ${CELESTIA_SRC_DIR}/celengine/viewporteffect.h
        ${CELESTIA_SRC_DIR}/celengine/virtualtex.cpp
        ${CELESTIA_SRC_DIR}/celengine/virtualtex.h
        ${CELESTIA_SRC_DIR}/celengine/visibleregion.cpp
        ${CELESTIA_SRC_DIR}/celengine/visibleregion.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celimage/bmp.cpp
        ${CELESTIA_SRC_DIR}/celimage/dds.cpp
        ${CELESTIA_SRC_DIR}/celimage/dds_decompress.cpp
        ${CELESTIA_SRC_DIR}/celimage/dds_decompress.h
        ${CELESTIA_SRC_DIR}/celimage/image.cpp
        ${CELESTIA_SRC_DIR}/celimage/image.h
        ${CELESTIA_SRC_DIR}/celimage/imageformats.h
        ${CELESTIA_SRC_DIR}/celimage/jpeg.cpp
        ${CELESTIA_SRC_DIR}/celimage/pixelformat.h
        ${CELESTIA_SRC_DIR}/celimage/png.cpp
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/cel3ds/3dsmodel.cpp
        ${CELESTIA_SRC_DIR}/cel3ds/3dsmodel.h
        ${CELESTIA_SRC_DIR}/cel3ds/3dsread.cpp
        ${CELESTIA_SRC_DIR}/cel3ds/3dsread.h
        )

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_SRC_DIR}/celmath/distance.h
        ${CELESTIA_SRC_DIR}/celmath/ellipsoid.h
        ${CELESTIA_SRC_DIR}/celmath/frustum.cpp
        ${CELESTIA_SRC_DIR}/celmath/frustum.h
        ${CELESTIA_SRC_DIR}/celmath/geomutil.h
        ${CELESTIA_SRC_DIR}/celmath/intersect.h
        ${CELESTIA_SRC_DIR}/celmath/mathlib.h
        ${CELESTIA_SRC_DIR}/celmath/randutils.cpp
        ${CELESTIA_SRC_DIR}/celmath/randutils.h
        ${CELESTIA_SRC_DIR}/celmath/ray.h
        ${CELESTIA_SRC_DIR}/celmath/solve.h
        ${CELESTIA_SRC_DIR}/celmath/sphere.h
        ${CELESTIA_SRC_DIR}/celmath/vecgl.h
        )


set(CELESTIA_JNI_DIR ${CMAKE_SOURCE_DIR})

list(APPEND CELESTIA_SOURCES
        ${CELESTIA_JNI_DIR}/CelestiaAppCore.cpp
        ${CELESTIA_JNI_DIR}/CelestiaSimulation.cpp
        ${CELESTIA_JNI_DIR}/CelestiaSelection.cpp
        ${CELESTIA_JNI_DIR}/CelestiaAstroObject.cpp
        ${CELESTIA_JNI_DIR}/CelestiaUniverse.cpp
        ${CELESTIA_JNI_DIR}/CelestiaStarCatalog.cpp
        ${CELESTIA_JNI_DIR}/CelestiaDSOCatalog.cpp
        ${CELESTIA_JNI_DIR}/CelestiaDSO.cpp
        ${CELESTIA_JNI_DIR}/CelestiaStar.cpp
        ${CELESTIA_JNI_DIR}/CelestiaBody.cpp
        ${CELESTIA_JNI_DIR}/CelestiaLocation.cpp
        ${CELESTIA_JNI_DIR}/CelestiaStarBrowser.cpp
        ${CELESTIA_JNI_DIR}/CelestiaScript.cpp
        ${CELESTIA_JNI_DIR}/CelestiaUtils.cpp
        ${CELESTIA_JNI_DIR}/CelestiaOrbit.cpp
        ${CELESTIA_JNI_DIR}/CelestiaRotationModel.cpp
        ${CELESTIA_JNI_DIR}/CelestiaUniversalCoord.cpp
        ${CELESTIA_JNI_DIR}/CelestiaDMS.cpp
        ${CELESTIA_JNI_DIR}/CelestiaVector.cpp
        ${CELESTIA_JNI_DIR}/CelestiaEclipseFinder.cpp
        ${CELESTIA_JNI_DIR}/CelestiaPlanetarySystem.cpp
        ${CELESTIA_JNI_DIR}/CelestiaObserver.cpp
        ${CELESTIA_JNI_DIR}/CelestiaDestination.cpp
        ${CELESTIA_JNI_DIR}/CelestiaRenderer.cpp
        ${CELESTIA_JNI_DIR}/CelestiaFont.cpp
        )

set(THIRD_PARTY_DIR "${CMAKE_SOURCE_DIR}/dependency/android")
set(THIRD_PARTY_LIB_DIR "${THIRD_PARTY_DIR}/libs")
set(THIRD_PARTY_INCLUDE_DIR "${THIRD_PARTY_DIR}/include")

set(CMAKE_CXX_STANDARD 17)

add_definitions(-DENABLE_NLS)
add_definitions(-DHAVE_MESHOPTIMIZER)
add_definitions(-DUSE_SPICE)
add_definitions(-DGL_ES)
add_definitions(-DUSE_MINIAUDIO)
add_definitions(-DCELX)
add_definitions(-DUSE_ICU)

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    add_definitions(-DEIGEN_NO_DEBUG)
endif()

include_directories(
        ${CMAKE_SOURCE_DIR}
        ${CELESTIA_SRC_DIR}
        ${THIRD_PARTY_INCLUDE_DIR}
        ${THIRD_PARTY_INCLUDE_DIR}/cspice
        ${THIRD_PARTY_INCLUDE_DIR}/lua
        ${THIRD_PARTY_INCLUDE_DIR}/eigen3
        ${THIRD_PARTY_INCLUDE_DIR}/gettext
        ${THIRD_PARTY_INCLUDE_DIR}/jpeg
        ${THIRD_PARTY_INCLUDE_DIR}/libpng
        ${THIRD_PARTY_INCLUDE_DIR}/libepoxy
        ${THIRD_PARTY_INCLUDE_DIR}/json
        ${THIRD_PARTY_INCLUDE_DIR}/freetype/freetype2
        ${THIRD_PARTY_INCLUDE_DIR}/meshoptimizer
        ${THIRD_PARTY_INCLUDE_DIR}/miniaudio
        ${THIRD_PARTY_INCLUDE_DIR}/icu
)

add_library(cspice STATIC IMPORTED)
set_target_properties(cspice PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/cspice.a)

add_library(lua STATIC IMPORTED)
set_target_properties(lua PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/liblua.a)

add_library(fmt STATIC IMPORTED)
set_target_properties(fmt PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libfmt.a)

add_library(gettext STATIC IMPORTED)
set_target_properties(gettext PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libintl.a)

add_library(libepoxy STATIC IMPORTED)
set_target_properties(libepoxy PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libGL.a)

add_library(libpng STATIC IMPORTED)
set_target_properties(libpng PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libpng16.a)

add_library(jpeg STATIC IMPORTED)
set_target_properties(jpeg PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libjpeg.a)

add_library(freetype STATIC IMPORTED)
set_target_properties(freetype PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libfreetype.a)

add_library(meshoptimizer STATIC IMPORTED)
set_target_properties(meshoptimizer PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libmeshoptimizer.a)

add_library(icuuc STATIC IMPORTED)
set_target_properties(icuuc PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libicuuc.a)

add_library(icudata STATIC IMPORTED)
set_target_properties(icudata PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libicudata.a)

add_library(icui18n STATIC IMPORTED)
set_target_properties(icui18n PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libicui18n.a)

find_package(games-frame-pacing REQUIRED CONFIG)

add_library(
        celestia
        SHARED
        ${CELESTIA_SOURCES} )

find_library(
        log-lib
        log )

target_link_libraries(
        celestia
        z
        fmt
        libepoxy
        gettext
        jpeg
        freetype
        lua
        libpng
        cspice
        meshoptimizer
        icuuc
        icudata
        icui18n
        EGL
        android
        games-frame-pacing::swappy_static
        ${log-lib} )
