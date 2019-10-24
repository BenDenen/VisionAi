#pragma once

#include "image_utils.hpp"

#include <arm_neon.h>

namespace vsa {
    namespace utils {

        inline std::vector<uint32_t>
        CalcResizeOffsets(uint32_t inpChannels, Point2U inpSize, Point2U resizedSize) {
            std::vector<uint32_t> offsets;

            uint32_t const count = resizedSize.x * resizedSize.y;

            offsets.resize(count);

            for (uint32_t i = 0; i < count; ++i) {
                uint32_t const x = i % resizedSize.x;
                uint32_t const y = i / resizedSize.x;

                uint32_t const px = uint32_t(float(inpSize.x) * x / resizedSize.x);
                uint32_t const py = uint32_t(float(inpSize.y) * y / resizedSize.y);

                offsets[i] = (py * inpSize.x + px) * inpChannels;
            }

            return offsets;
        }

        namespace detail {

            inline void
            MoveAxes(std::vector<float *> &axes, ImageFormat oldFormat, ImageFormat newFormat) {
                if (oldFormat == ImageFormat::BGR || oldFormat == ImageFormat::BGRA) {
                    if (newFormat == ImageFormat::RGB || newFormat == ImageFormat::RGBA)
                        std::swap(axes[0], axes[2]);
                } else if (oldFormat == ImageFormat::RGB || oldFormat == ImageFormat::RGBA) {
                    if (newFormat == ImageFormat::BGR || newFormat == ImageFormat::BGRA)
                        std::swap(axes[0], axes[2]);
                }
            }

            template<ImageFormat Format>
            struct ChannelsNum;

            template<>
            struct ChannelsNum<ImageFormat::Grayscale8> {
                static constexpr uint8_t Value = 1;
            };

            template<>
            struct ChannelsNum<ImageFormat::RGB> {
                static constexpr uint8_t Value = 3;
            };

            template<>
            struct ChannelsNum<ImageFormat::BGR> {
                static constexpr uint8_t Value = 3;
            };

            template<>
            struct ChannelsNum<ImageFormat::RGBA> {
                static constexpr uint8_t Value = 4;
            };

            template<>
            struct ChannelsNum<ImageFormat::BGRA> {
                static constexpr uint8_t Value = 4;
            };

            template<uint8_t Channels>
            inline void CopyPixelsSlices(vsa::ImagePtr const &image, float **slices,
                                         std::vector<uint32_t> const &offsets);

            template<>
            inline void CopyPixelsSlices<1>(vsa::ImagePtr const &image, float **slices,
                                            std::vector<uint32_t> const &offsets) {
                float *grayscale = slices[0];
                uint8_t const *src = image->CData();

                for (uint32_t offset : offsets) {
                    uint8_t const *pixel = src + offset;

                    *grayscale = *pixel / 255.f;
                    ++grayscale;
                }
            }

            template<>
            inline void CopyPixelsSlices<3>(vsa::ImagePtr const &image, float **slices,
                                            std::vector<uint32_t> const &offsets) {

                uint8_t *data = image->Data();
                float newData[4];

                float *rChannel = slices[0];
                float *gChannel = slices[1];
                float *bChannel = slices[2];

                float32x4_t const divValue = vdupq_n_f32(255.f);
                uint32_t *offset = const_cast<uint32_t *>(offsets.data());
                size_t const num = offsets.size();
                for (size_t i = 0; i < num; ++i) {
                    uint8x8_t const values8 = vget_low_u8(vld1q_u8(data + *offset));
                    uint16x4_t const values16 = vget_low_u16(vmovl_u8(values8));
                    uint32x4_t const values32 = vmovl_u16(values16);
                    float32x4_t const valuesFloat = vcvtq_f32_u32(values32);

                    float32x4_t const result = vdivq_f32(valuesFloat, divValue);

                    // store
                    vst1q_f32(newData, result);

                    rChannel[0] = newData[0];
                    ++rChannel;

                    gChannel[0] = newData[1];
                    ++gChannel;

                    bChannel[0] = newData[2];
                    ++bChannel;

                    ++offset;
                }
            }

            template<uint8_t Channels>
            inline void CopyPixels(vsa::ImagePtr const &image, float *output,
                                   std::vector<uint32_t> const &offsets);

            template<uint8_t Channels>
            inline void CopyPixelsBytes(vsa::ImagePtr const &image, uint8_t *output,
                                        std::vector<uint32_t> const &offsets);

            template<>
            inline void CopyPixels<1>(vsa::ImagePtr const &image, float *output,
                                      std::vector<uint32_t> const &offsets) {
                uint8_t const *src = image->CData();

                assert(image->GetChannelsNum() == 1);

                for (uint32_t offset : offsets)
                    *(output++) = *(src + offset) / 255.f;
            }

            template<>
            inline void CopyPixels<3>(vsa::ImagePtr const &image, float *output,
                                      std::vector<uint32_t> const &offsets) {

                uint8_t *data = image->Data();
                float newData[4];

                float32x4_t const divValue = vdupq_n_f32(255.f);
                uint32_t *offset = const_cast<uint32_t *>(offsets.data());
                size_t const num = offsets.size();
                for (size_t i = 0; i < num; ++i) {

                    uint8x8_t const values8 = vget_low_u8(vld1q_u8(data + *offset));
                    uint16x4_t const values16 = vget_low_u16(vmovl_u8(values8));
                    uint32x4_t const values32 = vmovl_u16(values16);

                    float32x4_t const valuesFloat = vcvtq_f32_u32(values32);

                    float32x4_t const result = vdivq_f32(valuesFloat, divValue);

                    // store
                    vst1q_f32(newData, result);

                    output[0] = newData[0];
                    output[1] = newData[1];
                    output[2] = newData[2];

                    output += 3;
                    ++offset;
                }
            }

            template<>
            inline void CopyPixelsBytes<3>(vsa::ImagePtr const &image, uint8_t *output,
                                           std::vector<uint32_t> const &offsets) {

                uint8_t const *src = image->CData();
                for (uint32_t offset : offsets) {
                    uint8_t const *pixel = src + offset;

                    *(output++) = *(pixel + 2);
                    *(output++) = *(pixel + 1);
                    *(output++) = *(pixel + 0);
                }
            }


        } // namespace detail

// Resize image with swap channels position in shape from [Height, Width, Channels] to [Channles, Height, Width] format
        template<vsa::ImageFormat Format>
        void ResizeImageSwapShape(vsa::ImagePtr const &image, std::vector<float> &result,
                                  vsa::Point2U newSize, std::vector<uint32_t> const &offsets) {
            uint32_t const bytesPerSlice = newSize.x * newSize.y;
            assert(offsets.size() == bytesPerSlice);

            uint32_t const channelsNum = detail::ChannelsNum<Format>::Value;

            assert((image->GetChannelsNum() == channelsNum) ||
                   (image->GetChannelsNum() == 4 && channelsNum == 3));

            std::vector<float *> outSlices(channelsNum);

            result.resize(channelsNum * bytesPerSlice);
            for (uint32_t i = 0; i < outSlices.size(); ++i)
                outSlices[i] = result.data() + i * bytesPerSlice;

            detail::MoveAxes(outSlices, image->Format(), Format);
            detail::CopyPixelsSlices<detail::ChannelsNum<Format>::Value>(image, outSlices.data(),
                                                                         offsets);
        }

        template<vsa::ImageFormat Format>
        inline void ResizeImageSwapShape(vsa::ImagePtr const &image, std::vector<float> &result,
                                         vsa::Point2U newSize) {
            auto offsets = CalcResizeOffsets(image->GetChannelsNum(), image->Size(), newSize);
            ResizeImageSwapShape<Format>(image, result, newSize, offsets);
        }

        template<vsa::ImageFormat Format>
        void
        ResizeImage(vsa::ImagePtr const &image, std::vector<float> &result, vsa::Point2U newSize,
                    std::vector<uint32_t> const &offsets) {
            uint32_t const bytesPerSlice = newSize.x * newSize.y;
            assert(offsets.size() == bytesPerSlice);

            uint32_t const channelsNum = detail::ChannelsNum<Format>::Value;

            assert((image->GetChannelsNum() == channelsNum) ||
                   (image->GetChannelsNum() == 4 && channelsNum == 3));

            result.resize(channelsNum * bytesPerSlice);
            detail::CopyPixels<detail::ChannelsNum<Format>::Value>(image, result.data(), offsets);
        }

        template<vsa::ImageFormat Format>
        void
        ResizeImage(vsa::ImagePtr const &image, float *result, vsa::Point2U newSize,
                    std::vector<uint32_t> const &offsets) {
            uint32_t const bytesPerSlice = newSize.x * newSize.y;
            assert(offsets.size() == bytesPerSlice);

            uint32_t const channelsNum = detail::ChannelsNum<Format>::Value;

            assert((image->GetChannelsNum() == channelsNum) ||
                   (image->GetChannelsNum() == 4 && channelsNum == 3));

            detail::CopyPixels<detail::ChannelsNum<Format>::Value>(image, result, offsets);
        }

        template<vsa::ImageFormat Format>
        void
        ResizeImage(vsa::ImagePtr const &image, uint8_t *result, vsa::Point2U newSize,
                    std::vector<uint32_t> const &offsets) {
            uint32_t const bytesPerSlice = newSize.x * newSize.y;
            assert(offsets.size() == bytesPerSlice);

            uint32_t const channelsNum = detail::ChannelsNum<Format>::Value;

            assert((image->GetChannelsNum() == channelsNum) ||
                   (image->GetChannelsNum() == 4 && channelsNum == 3));

            detail::CopyPixelsBytes<detail::ChannelsNum<Format>::Value>(image, result, offsets);
        }

        template<vsa::ImageFormat Format>
        inline void
        ResizeImage(vsa::ImagePtr const &image, std::vector<float> &result, vsa::Point2U newSize) {
            auto offsets = CalcResizeOffsets(image->GetChannelsNum(), image->Size(), newSize);
            ResizeImage<Format>(image, result, newSize, offsets);
        }

    } // namespace platform
} // namespace vsa
