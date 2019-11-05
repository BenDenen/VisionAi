//
// Created by Boris Denisenko on 2019-10-09.
//
#pragma once

#include "point2d.hpp"

#include <memory>
#include <vector>
#include <cstring>


namespace vsa {

    enum class ImageFormat : uint8_t {
        Unknown = 0,
        RGBA,
        BGRA,
        RGB,
        BGR,
        Grayscale8        // one 8-bit channel
    };

    inline bool IsImageFormatRGB(ImageFormat fmt) {
        return (fmt == ImageFormat::RGBA) || (fmt == ImageFormat::RGB);
    }

    inline bool IsImageFormatBGR(ImageFormat fmt) {
        return (fmt == ImageFormat::BGRA) || (fmt == ImageFormat::BGR);
    }

    inline uint8_t CalcChannelsNum(ImageFormat format) {
        switch (format) {
            case ImageFormat::BGRA:
            case ImageFormat::RGBA:
                return 4;

            case ImageFormat::BGR:
            case ImageFormat::RGB:
                return 3;

            case ImageFormat::Grayscale8:
                return 1;

            default:
                return 0;
        }
    }

    class Image {
    public:

        Image()
                : m_size(0, 0), m_format(ImageFormat::Unknown) {
        }

        Image(uint32_t width, uint32_t height, ImageFormat format)
                : m_size(width, height), m_format(format) {
            Resize(SizeInBytes());
        }

        Image(Point2U const &size, ImageFormat format)
                : m_size(size), m_format(format) {
            Resize(SizeInBytes());
        }

        uint8_t *Data() {
            return m_data.get();
        }

        uint8_t const *CData() const {
            return m_data.get();
        }

        void ClearPixels() {
            memset(m_data.get(), 0, SizeInBytes());
        }

        bool IsEmpty() const {
            return m_data.get() == nullptr;
        }

        void Set(uint32_t width, uint32_t height, ImageFormat format, uint8_t const *data) {
            Reset(width, height, format);
            memcpy(m_data.get(), data, SizeInBytes());
        }

        template<typename T>
        void Set(uint32_t width, uint32_t height, ImageFormat format, T const *data) {
            Reset(width, height, format);
            for (uint32_t i = 0; i < SizeInBytes(); ++i)
                m_data[i] = static_cast<uint8_t>(data[i]);
        }

        uint32_t Width() const {
            return m_size.x;
        }

        uint32_t Height() const {
            return m_size.y;
        }

        Point2U const &Size() const {
            return m_size;
        }

        uint32_t SizeInBytes() const {
            return GetChannelsNum() * m_size.x * m_size.y;
        }

        uint8_t GetChannelsNum() const {
            return CalcChannelsNum(m_format);
        }

        ImageFormat Format() const {
            return m_format;
        }

        void Reset(uint32_t width, uint32_t height, ImageFormat format) {
            m_size.x = width;
            m_size.y = height;
            m_format = format;
            Resize(SizeInBytes());
        }

        void Resize(uint32_t newSize) {
            m_data.reset(new uint8_t[newSize]);
        }

    private:
        std::unique_ptr<uint8_t[]> m_data;
        Point2U m_size;
        ImageFormat m_format; // image format
    };

    using ImagePtr = std::shared_ptr<Image>;

    inline ImagePtr CreateImage() {
        return std::make_shared<Image>();
    }

    inline ImagePtr CreateImage(uint32_t width, uint32_t height, ImageFormat format) {
        return std::make_shared<Image>(width, height, format);
    }

    inline ImagePtr CreateImage(Point2U const &size, ImageFormat format) {
        return std::make_shared<Image>(size, format);
    }

    inline ImagePtr CopyImage(ImagePtr img) {
        ImagePtr res = CreateImage(img->Size(), img->Format());

        assert(img->SizeInBytes() == res->SizeInBytes());
        memcpy(res->Data(), img->Data(), img->SizeInBytes());
        return res;
    }

} // namespace vsa


