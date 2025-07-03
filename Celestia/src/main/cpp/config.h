/*
 * config.h
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#define HAVE_BYTESWAP_H
#define HAVE_CHARCONV
//#define HAVE_FLOAT_CHARCONV
//#define HAVE_WORDEXP
#define HAVE_MESHOPTIMIZER
#define HAVE_CONSTEXPR_CMATH
#define HAVE_SINCOS
//#define HAVE_APPLE_SINCOS
#define HAVE_CONSTEXPR_EMPTY_ARRAY

#ifdef HAVE_CONSTEXPR_CMATH
#define CELESTIA_CMATH_CONSTEXPR constexpr
#else
#define CELESTIA_CMATH_CONSTEXPR
#endif

#define VERSION "1.7.0"