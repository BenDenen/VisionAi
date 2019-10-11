#include <functional>
#include <iostream>
#include <type_traits>

namespace hlf
{

template <typename From, typename Tag> struct Alias;
template <typename From, typename Tag>
inline std::ostream &operator<<(std::ostream &stream, const Alias<From, Tag> &inst);

template <typename From, typename Tag> struct Alias final
{
    using value_type = From;
    static_assert(std::is_arithmetic<From>::value, "Needs to be based on an arithmetic type");

    From __value;
    friend std::ostream &operator<<<From, Tag>(std::ostream &stream, const Alias &inst);

    explicit operator From &() { return __value; }
    explicit operator From() const { return __value; }
    inline Alias operator+(const Alias rhs_) const
    {
        return Alias{__value + static_cast<const From>(rhs_)};
    }
    inline Alias operator-(const Alias rhs_) const
    {
        return Alias{__value - static_cast<const From>(rhs_)};
    }
    inline Alias operator*(const Alias rhs_) const
    {
        return Alias{__value * static_cast<const From>(rhs_)};
    }
    inline Alias operator*(const double rhs_) const { return Alias{__value * rhs_}; }
    inline Alias operator/(const Alias rhs_) const
    {
        return Alias{__value / static_cast<const From>(rhs_)};
    }
    inline Alias operator/(const double rhs_) const { return Alias{__value / rhs_}; }
    inline Alias operator|(const Alias rhs_) const
    {
        return Alias{__value | static_cast<const From>(rhs_)};
    }
    inline Alias operator&(const Alias rhs_) const
    {
        return Alias{__value & static_cast<const From>(rhs_)};
    }
    inline bool operator<(const Alias z_) const { return __value < static_cast<const From>(z_); }
    inline bool operator>(const Alias z_) const { return __value > static_cast<const From>(z_); }
    inline bool operator<=(const Alias z_) const { return __value <= static_cast<const From>(z_); }
    inline bool operator>=(const Alias z_) const { return __value >= static_cast<const From>(z_); }
    inline bool operator==(const Alias z_) const { return __value == static_cast<const From>(z_); }
    inline bool operator!=(const Alias z_) const { return __value != static_cast<const From>(z_); }

    inline Alias operator++()
    {
        __value++;
        return *this;
    }
    inline Alias operator--()
    {
        __value--;
        return *this;
    }

    inline Alias operator+=(const Alias z_)
    {
        __value += static_cast<const From>(z_);
        return *this;
    }
    inline Alias operator-=(const Alias z_)
    {
        __value -= static_cast<const From>(z_);
        return *this;
    }
    inline Alias operator/=(const Alias z_)
    {
        __value /= static_cast<const From>(z_);
        return *this;
    }
    inline Alias operator*=(const Alias z_)
    {
        __value *= static_cast<const From>(z_);
        return *this;
    }
    inline Alias operator|=(const Alias z_)
    {
        __value |= static_cast<const From>(z_);
        return *this;
    }
    inline Alias operator&=(const Alias z_)
    {
        __value &= static_cast<const From>(z_);
        return *this;
    }
};

template <typename From, typename Tag>
inline std::ostream &operator<<(std::ostream &stream, const Alias<From, Tag> &inst)
{
    return stream << inst.__value;
}
}

namespace std
{
template <typename From, typename Tag> struct hash<hlf::Alias<From, Tag>>
{
    typedef hlf::Alias<From, Tag> argument_type;
    typedef std::size_t result_type;
    result_type operator()(argument_type const &s) const
    {
        return std::hash<From>()(static_cast<const From>(s));
    }
};
}

