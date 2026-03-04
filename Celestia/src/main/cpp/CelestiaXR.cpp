#include "celestia_xr_api.h"

#include <cmath>

#include <celestia/celestiacore.h>
#include <celengine/perspectiveprojectionmode.h>
#include <celmath/frustum.h>

using namespace Eigen;

class CustomPerspectiveProjectionMode : public celestia::engine::PerspectiveProjectionMode
{
public:
    CustomPerspectiveProjectionMode(float left, float right, float top, float bottom, float nearZ, float farZ, float width, float height) :
        PerspectiveProjectionMode(width, height, 0, 0),
        left(left), right(right), top(top), bottom(bottom),
        nearZ(nearZ), farZ(std::isinf(farZ) ? maximumFarZ : std::min(farZ, maximumFarZ))
    {
    }

    CustomPerspectiveProjectionMode(const CustomPerspectiveProjectionMode &) = default;
    CustomPerspectiveProjectionMode(CustomPerspectiveProjectionMode &&) = default;
    CustomPerspectiveProjectionMode &operator=(const CustomPerspectiveProjectionMode &) = default;
    CustomPerspectiveProjectionMode &operator=(CustomPerspectiveProjectionMode &&) = default;
    ~CustomPerspectiveProjectionMode() = default;

    std::tuple<float, float> getDefaultDepthRange() const override
    {
        return std::make_tuple(nearZ, farZ);
    }

    Matrix4f getProjectionMatrix(float nz, float fz, float) const override
    {
        float ratio = nz / nearZ;

        float l = ratio * left;
        float r = ratio * right;
        float t = ratio * top;
        float b = ratio * bottom;

        // https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/glFrustum.xml
        float A = (r + l) / (r - l);
        float B = (t + b) / (t - b);
        float C = -(fz + nz) / (fz - nz);
        float D = -2.0f * fz * nz / (fz - nz);

        Matrix4f m;

        m << 2.0f * nz / (r - l),                0.0f,     A, 0.0f,
             0.0f,                2.0f * nz / (t - b),     B, 0.0f,
             0.0f,                               0.0f,     C,    D,
             0.0f,                               0.0f, -1.0f, 0.0f;

        return m;
    }

    float getMinimumFOV() const override { return getFOV(1.0f); }
    float getMaximumFOV() const override { return getFOV(1.0f); }
    float getFOV(float zoom) const override
    {
        float a = top * top + nearZ * nearZ;
        float b = bottom * bottom + nearZ * nearZ;
        float c = (top - bottom) * (top - bottom);
        return std::acos((a + b - c) / (2.0f * std::sqrt(a * b)));
    }
    float getZoom(float fov) const override { return 1.0f; }
    celestia::math::Frustum getFrustum(float _nearZ, float _farZ, float zoom) const override {
        float ratio = _nearZ / nearZ;
        return celestia::math::Frustum(left * ratio, right * ratio, top * ratio, bottom * ratio, _nearZ, _farZ);
    }
    double getViewConeAngleMax(float zoom) const override
    {
        float a = left * left + top * top;
        float b = right * right + top * top;
        float c = left * left + bottom * bottom;
        float d = right * right + bottom * bottom;
        float maxValue = std::max({a, b, c, d});
        return static_cast<double>(nearZ) / std::sqrt(static_cast<double>(nearZ * nearZ + maxValue));
    }

    static constexpr float maximumFarZ = 1.0e9f;

    Vector3f getPickRay(float x, float y, float zoom) const override
    {
        auto invProj = getProjectionMatrix(nearZ, maximumFarZ, 1.0f).inverse();
        float aspectRatio = width / height;
        Vector4f clip(x / aspectRatio * 2.0f, y * 2.0f, -1.0, 1.0);
        return (invProj * clip).head<3>().normalized();
    }

    Vector2f getRayIntersection(Vector3f pickRay, float zoom) const override
    {
        auto proj = getProjectionMatrix(nearZ, maximumFarZ, 1.0f);
        float coeff = -nearZ / pickRay.z();
        Vector4f point(coeff * pickRay.x(), coeff * pickRay.y(), -nearZ, 1.0f);
        Vector4f projected = proj * point;
        projected /= projected.w();
        float aspectRatio = width / height;
        return Vector2f(projected.x() * aspectRatio, projected.y());
    }

private:
    float left;
    float right;
    float bottom;
    float top;
    float nearZ;
    float farZ;
};

extern "C" {

void CelestiaXR_RenderEye(void* corePtr, int width, int height,
                          float left, float right, float bottom, float top,
                          float nearZ, float farZ, const float* viewMatrix, bool doTick) {
    if (!corePtr || !viewMatrix) return;

    auto* core = static_cast<CelestiaCore*>(corePtr);

    // Construct orientation matrix from the view matrix array
    // viewMatrix is expected to be a 16-element float array (column-major)
    Matrix4f viewMat = Map<const Matrix4f>(viewMatrix);

    // Extract the rotation part
    Matrix3f rotation = viewMat.block<3, 3>(0, 0);

    core->getRenderer()->setProjectionMode(std::make_shared<CustomPerspectiveProjectionMode>(
        left, right, top, bottom, nearZ, farZ, static_cast<float>(width), static_cast<float>(height)));
    core->getRenderer()->setCameraTransform(rotation.cast<double>());

    core->resize(width, height);

    if (doTick) {
        core->tick();
    }

    core->draw();
}

} // extern "C"
