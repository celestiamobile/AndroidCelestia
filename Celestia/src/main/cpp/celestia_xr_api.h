#pragma once

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Render an eye (left or right) for OpenXR.
 * 
 * @param corePtr Pointer to the CelestiaCore instance.
 * @param width The width of the viewport/eye texture.
 * @param height The height of the viewport/eye texture.
 * @param left   The left frustum bound.
 * @param right  The right frustum bound.
 * @param bottom The bottom frustum bound.
 * @param top    The top frustum bound.
 * @param nearZ  The near clipping plane.
 * @param farZ   The far clipping plane.
 * @param viewMatrix A 16-element float array representing the column-major view matrix.
 * @param doTick Whether to call core->tick() before core->draw().
 */
void CelestiaXR_RenderEye(void* corePtr, int width, int height,
                          float left, float right, float bottom, float top,
                          float nearZ, float farZ, const float* viewMatrix, bool doTick);

#ifdef __cplusplus
}
#endif
