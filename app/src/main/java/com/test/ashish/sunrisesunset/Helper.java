package com.test.ashish.sunrisesunset;

public class Helper {

    public static int dayOfYear(int month, int day, int year){
        double N1 = Math.floor(275 * month / 9);
        double N2 = Math.floor((month + 9) / 12);
        double N3 = (1 + Math.floor((year - 4 * Math.floor(year / 4) + 2) / 3));
        double N = N1 - (N2 * N3) + day - 30;
        return Integer.parseInt(String.valueOf(N));
    }

    public static double computeSunrise(double latitude, double longitude, int day, boolean sunrise) {

    /*Sunrise/Sunset Algorithm taken from
        http://williams.best.vwh.net/sunrise_sunset_algorithm.htm
        inputs:
            day = day of the year
            sunrise = true for sunrise, false for sunset
        output:
            time of sunrise/sunset in hours */

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
        double L = M + (1.916 * Math.sin(M * D2R)) + (0.020 * Math.sin(2 * M * D2R)) + 282.634;
        if (L > 360) {
            L = L - 360;
        } else if (L < 0) {
            L = L + 360;
        };

        //calculate the Sun's right ascension
        double RA = R2D * Math.atan(0.91764 * Math.tan(L * D2R));
        if (RA > 360) {
            RA = RA - 360;
        } else if (RA < 0) {
            RA = RA + 360;
        };

        //right ascension value needs to be in the same qua
        double Lquadrant = (Math.floor(L / (90))) * 90;
        double RAquadrant = (Math.floor(RA / 90)) * 90;
        RA = RA + (Lquadrant - RAquadrant);

        //right ascension value needs to be converted into hours
        RA = RA / 15;

        //calculate the Sun's declination
        double sinDec = 0.39782 * Math.sin(L * D2R);
        double cosDec = Math.cos(Math.asin(sinDec));

        //calculate the Sun's local hour angle
        double cosH = (Math.cos(zenith * D2R) - (sinDec * Math.sin(latitude * D2R))) / (cosDec * Math.cos(latitude * D2R));
        double H;
        if (sunrise) {
            H = 360 - R2D * Math.acos(cosH);
        } else {
            H = R2D * Math.acos(cosH);
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
        double localT = UT + 1;

        //convert to Milliseconds
        return localT * 3600 * 1000;
    }
}
