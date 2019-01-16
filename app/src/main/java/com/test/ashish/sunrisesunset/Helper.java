package com.test.ashish.sunrisesunset;

import android.util.Log;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

public class Helper {


    public static long computeSunrise(double latitude, double longitude, int day, boolean sunrise) {

        double zenith = 90.83333333333333;
        double D2R = Math.PI / 180;
        double R2D = 180 / Math.PI;

        // convert the longitude to hour value and calculate an approximate time
        double lnHour = longitude / 15;
        double t;
        if (sunrise) {
            t = day + ((6 - lnHour) / 24);
        } else {
            t = day + ((18 - lnHour) / 24);
        };

        //calculate the Sun's mean anomaly
        double M = (0.9856 * t) - 3.289;

        //calculate the Sun's true longitude
        double L = M + (1.916 * sin(M * D2R)) + (0.020 * sin(2 * M * D2R)) + 282.634;
        if (L > 360) {
            L = L - 360;
        } else if (L < 0) {
            L = L + 360;
        };

        //calculate the Sun's right ascension
        double RA = R2D * atan(0.91764 * tan(L * D2R));
        if (RA > 360) {
            RA = RA - 360;
        } else if (RA < 0) {
            RA = RA + 360;
        };

        //right ascension value needs to be in the same qua
        double Lquadrant = (floor(L / (90))) * 90;
        double RAquadrant = (floor(RA / 90)) * 90;
        RA = RA + (Lquadrant - RAquadrant);

        //right ascension value needs to be converted into hours
        RA = RA / 15;

        //calculate the Sun's declination
        double sinDec = 0.39782 * sin(L * D2R);
        double cosDec = cos(asin(sinDec));

        //calculate the Sun's local hour angle
        double cosH = (cos(zenith * D2R) - (sinDec * sin(latitude * D2R))) / (cosDec * cos(latitude * D2R));
        double H;
        if (sunrise) {
            H = 360 - R2D * acos(cosH);
        } else {
            H = R2D * acos(cosH);
        };
        H = H / 15;

        //calculate local mean time of rising/setting
        double T = H + RA - (0.06571 * t) - 6.622;

        //adjust back to UTC
        double UT = T - lnHour;
        if (UT > 24) {
            UT = UT - 24;
        } else if (UT < 0) {
            UT = UT + 24;
        }

        //convert UT value to local time zone of latitude/longitude
        double localT = UT;

        //convert to Milliseconds
        return (long) (localT * 3600 * 1000);
    }

    public static long calculateSunriseExact(double latitude, double longitude, int N, boolean sunrise){

//        Source:
//        Almanac for Computers, 1990
//        published by Nautical Almanac Office
//        United States Naval Observatory
//        Washington, DC 20392
//
//        Inputs:
//        day, month, year:      date of sunrise/sunset
//        latitude, longitude:   location for sunrise/sunset
//        zenith:                Sun's zenith for sunrise/sunset
//        offical      = 90 degrees 50'
//        civil        = 96 degrees
//                nautical     = 102 degrees
//                astronomical = 108 degrees
//
//        NOTE: longitude is positive for East and negative for West
//        NOTE: the algorithm assumes the use of a calculator with the
//        trig functions in "degree" (rather than "radian") mode. Most
//        programming languages assume radian arguments, requiring back
//        and forth convertions. The factor is 180/pi. So, for instance,
//                the equation RA = atan(0.91764 * tan(L)) would be coded as RA
//                = (180/pi)*atan(0.91764 * tan((pi/180)*L)) to give a degree
//        answer with a degree input for L.
//
//
//        1. first calculate the day of the year
//
//                N1 = floor(275 * month / 9)
//        N2 = floor((month + 9) / 12)
//        N3 = (1 + floor((year - 4 * floor(year / 4) + 2) / 3))
//        N = N1 - (N2 * N3) + day - 30

//        2. convert the longitude to hour value and calculate an approximate time
        double zenith = 90.83333333333333;
        double lngHour = longitude / 15;

//        if rising time is desired:
        double t;
        if (sunrise) {
            t = N + ((6 - lngHour) / 24);
        }else {
            t = N + ((18 - lngHour) / 24);
        }

//        3. calculate the Sun's mean anomaly

        double M = (0.9856 * t) - 3.289;

//        4. calculate the Sun's true longitude

        double L = M + (1.916 * sin(M)) + (0.020 * sin(2 * M)) + 282.634;
//        NOTE: L potentially needs to be adjusted into the range [0,360) by adding/subtracting 360
        if (L > 360) {
            L = L - 360;
        } else if (L < 0) {
            L = L + 360;
        };

//        5a. calculate the Sun's right ascension

        double RA = atan(0.91764 * tan(L));
//        NOTE: RA potentially needs to be adjusted into the range [0,360) by adding/subtracting 360
        if (RA > 360) {
            RA = RA - 360;
        } else if (RA < 0) {
            RA = RA + 360;
        }

//        5b. right ascension value needs to be in the same samequadrant as L

        double Lquadrant  = (floor( L/90)) * 90;
        double RAquadrant = (floor(RA/90)) * 90;
        RA = RA + (Lquadrant - RAquadrant);

//        5c. right ascension value needs to be converted into hours

        RA = RA / 15;

//        6. calculate the Sun's declination

        double sinDec = 0.39782 * sin(L);
        double cosDec = cos(asin(sinDec));

//        7a. calculate the Sun's local hour angle

        double cosH = (cos(zenith) - (sinDec * sin(latitude))) / (cosDec * cos(latitude));

//        if (cosH >  1)
//            the sun never rises on this location (on the specified date)
//        if (cosH < -1)
//            the sun never sets on this location (on the specified date)

//        7b. finish calculating H and convert into hours

//        if if rising time is desired:
        double H;
        if (sunrise) {
            H = 360 - acos(cosH);
        }else {
//        if setting time is desired:
            H = acos(cosH);
        }

        H = H / 15;

//        8. calculate local mean time of rising/setting

        double T = H + RA - (0.06571 * t) - 6.622;

//        9. adjust back to UTC
//        NOTE: UT potentially needs to be adjusted into the range [0,24) by adding/subtracting 24

        double UT = T - lngHour;
        if (UT > 24) {
            UT = UT - 24;
        } else if (UT < 0) {
            UT = UT + 24;
        }

//        10. convert UT value to local time zone of latitude/longitude

        //convert UT value to local time zone of latitude/longitude
        double localT = UT;

        //convert to Milliseconds
        return (long) (localT * 3600 * 1000);

    }
}
