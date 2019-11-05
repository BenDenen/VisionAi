#pragma once

#include "math.hpp"

#include "coordinate.hpp"

#include <iostream>
#include <utility>

namespace hlf {
    namespace util {

        template<typename T>
        class Point {
        public:
            typedef T value_type;

            T x, y;

            Point() {}

            Point(T x_, T y_) : x(x_), y(y_) {}

            template<typename U>
            Point(Point<U> const &u) : x(u.x), y(u.y) {}

            explicit Point(hlf::util::Coordinate const &c)
                    : x(static_cast<T>(hlf::util::toFloating(c.lon))),
                      y(static_cast<T>(hlf::util::toFloating(c.lat))) {
            }

            static Point<T> Zero() { return Point<T>(0, 0); }

            // TODO: temporary solution
            hlf::util::Coordinate toOsrm() const {
                return hlf::util::Coordinate(hlf::util::FloatLongitude{static_cast<double>(x)},
                                              hlf::util::FloatLatitude{static_cast<double>(y)});
            }

            bool EqualDxDy(Point<T> const &p, T eps) const {
                return ((fabs(x - p.x) < eps) && (fabs(y - p.y) < eps));
            }

            T SquaredLength(Point<T> const &p) const {
                return math::sq(x - p.x) + math::sq(y - p.y);
            }

            double Length(Point<T> const &p) const {
                return sqrt(SquaredLength(p));
            }

            bool IsAlmostZero() const {
                return math::AlmostEqualULPs(*this, Point<T>(0, 0));
            }

            Point<T> Move(T len, T ang) const {
                return Point<T>(x + len * cos(ang), y + len * sin(ang));
            }

            Point<T> Move(T len, T angSin, T angCos) const {
                return Point<T>(x + len * angCos, y + len * angSin);
            }

            Point<T> const &operator-=(Point<T> const &a) {
                x -= a.x;
                y -= a.y;
                return *this;
            }

            Point<T> const &operator+=(Point<T> const &a) {
                x += a.x;
                y += a.y;
                return *this;
            }

            template<typename U>
            Point<T> const &operator*=(U const &k) {
                x = static_cast<T>(x * k);
                y = static_cast<T>(y * k);
                return *this;
            }

            template<typename U>
            Point<T> const &operator=(Point<U> const &a) {
                x = static_cast<T>(a.x);
                y = static_cast<T>(a.y);
                return *this;
            }

            bool operator==(Point<T> const &p) const {
                return x == p.x && y == p.y;
            }

            bool operator!=(Point<T> const &p) const {
                return !(*this == p);
            }

            Point<T> operator+(Point<T> const &pt) const {
                return Point<T>(x + pt.x, y + pt.y);
            }

            Point<T> operator-(Point<T> const &pt) const {
                return Point<T>(x - pt.x, y - pt.y);
            }

            Point<T> operator-() const {
                return Point<T>(-x, -y);
            }

            Point<T> operator*(T scale) const {
                return Point<T>(x * scale, y * scale);
            }

            Point<T> operator/(T scale) const {
                return Point<T>(x / scale, y / scale);
            }

            Point<T> mid(Point<T> const &p) const {
                return Point<T>((x + p.x) * 0.5, (y + p.y) * 0.5);
            }

            /// @name VectorOperationsOnPoint
            // @{
            double Length() const {
                return sqrt(x * x + y * y);
            }

            Point<T> Normalize() const {
                assert(!IsAlmostZero());
                double const module = this->Length();
                return Point<T>(x / module, y / module);
            }

            std::pair<Point<T>, Point<T> > Normals(T prolongationFactor = 1) const {
                T const prolongatedX = prolongationFactor * x;
                T const prolongatedY = prolongationFactor * y;
                return std::pair<Point<T>, Point<T> >(
                        Point<T>(static_cast<T>(-prolongatedY), static_cast<T>(prolongatedX)),
                        Point<T>(static_cast<T>(prolongatedY), static_cast<T>(-prolongatedX)));
            }
            // @}

            void Rotate(double angle) {
                T cosAngle = cos(angle);
                T sinAngle = sin(angle);
                T oldX = x;
                x = cosAngle * oldX - sinAngle * y;
                y = sinAngle * oldX + cosAngle * y;
            }

            void Transform(Point<T> const &org,
                           Point<T> const &dx, Point<T> const &dy) {
                T oldX = x;
                x = org.x + oldX * dx.x + y * dy.x;
                y = org.y + oldX * dx.y + y * dy.y;
            }
        };

        template<typename T>
        inline Point<T> const operator-(Point<T> const &a, Point<T> const &b) {
            return Point<T>(a.x - b.x, a.y - b.y);
        }

        template<typename T>
        inline Point<T> const operator+(Point<T> const &a, Point<T> const &b) {
            return Point<T>(a.x + b.x, a.y + b.y);
        }

// Dot product of a and b, equals to |a|*|b|*cos(angle_between_a_and_b).
        template<typename T>
        T const DotProduct(Point<T> const &a, Point<T> const &b) {
            return a.x * b.x + a.y * b.y;
        }

// Value of cross product of a and b, equals to |a|*|b|*sin(angle_between_a_and_b).
        template<typename T>
        T const CrossProduct(Point<T> const &a, Point<T> const &b) {
            return a.x * b.y - a.y * b.x;
        }

        template<typename T>
        Point<T> const Rotate(Point<T> const &pt, T a) {
            Point<T> res(pt);
            res.Rotate(a);
            return res;
        }

        template<typename T, typename U>
        Point<T> const Shift(Point<T> const &pt, U const &dx, U const &dy) {
            return Point<T>(pt.x + dx, pt.y + dy);
        }

        template<typename T, typename U>
        Point<T> const Shift(Point<T> const &pt, Point<U> const &offset) {
            return Shift(pt, offset.x, offset.y);
        }

        template<typename T>
        Point<T> const Floor(Point<T> const &pt) {
            Point<T> res;
            res.x = floor(pt.x);
            res.y = floor(pt.y);
            return res;
        }

        template<typename T>
        bool AlmostEqualAbs(Point<T> const &a, Point<T> const &b, double const eps) {
            return math::AlmostEqualAbs(a.x, b.x, eps) && math::AlmostEqualAbs(a.y, b.y, eps);
        }

        template<typename T>
        bool AlmostEqualULPs(Point<T> const &a, Point<T> const &b, unsigned int maxULPs = 256) {
            return math::AlmostEqualULPs(a.x, b.x, maxULPs) &&
                   math::AlmostEqualULPs(a.y, b.y, maxULPs);
        }


/// Returns a point which is belonged to the segment p1, p2 with respet the indent shiftFromP1 from p1.
/// If shiftFromP1 is more the distance between (p1, p2) it returns p2.
/// If shiftFromP1 is less or equal zero it returns p1.
        template<typename T>
        Point<T> PointAtSegment(Point<T> const &p1, Point<T> const &p2, T shiftFromP1) {
            Point<T> p12 = p2 - p1;
            shiftFromP1 = math::clamp(shiftFromP1, 0.0, p12.Length());
            return p1 + p12.Normalize() * shiftFromP1;
        }

        template<typename T>
        bool operator<(Point<T> const &l, Point<T> const &r) {
            if (l.x != r.x)
                return l.x < r.x;
            return l.y < r.y;
        }

//typedef Point<float> PointF;
        typedef Point<double> PointD;
        typedef Point<uint32_t> PointU;
        typedef Point<uint64_t> PointU64;
        typedef Point<int32_t> PointI;
        typedef Point<int64_t> PointI64;

    } // namespace util
} // namespace hlf
