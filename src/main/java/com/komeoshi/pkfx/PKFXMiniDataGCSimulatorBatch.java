package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PKFXMiniDataGCSimulatorBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCSimulatorBatch.class);

    public static void main(String[] args) {
        PKFXMiniDataGCSimulatorBatch batch = new PKFXMiniDataGCSimulatorBatch();
        batch.run();
    }

    public void run() {

        double[] params = {

                0.00001,
                0.00002,
                0.00003,
                0.00004,
                0.00005,
                0.00006,
                0.00007,
                0.00008,
                0.00009,
                0.00010,
                0.00011,
                0.00021,
                0.00022,
                0.00023,
                0.00024,
                0.00025,
                0.00026,
                0.00027,
                0.00028,
                0.00029,
                0.00030,
                0.000310,
                0.00032,
                0.00033,
                0.000340,
                0.000341,
                0.000342,
                0.000343,
                0.000344,
                0.000345,
                0.000346,
                0.000347,
                0.000348,
                0.000349,
                0.000350,
                0.00036,
                0.00037,
                0.00038,
                0.00039,
                0.00040,
                0.00041,
                0.00051,
                0.00061,
                0.00062,
                0.00063,
                0.00064,
                0.00065,
                0.00066,
                0.00067,
                0.00068,
                0.00069,
                0.00070,
                0.00071,
                0.00072,
                0.00073,
                0.00074,
                0.00075,
                0.00076,
                0.00077,
                0.00078,
                0.00079,
                0.00080,
                0.00081,
                0.00091,
                0.001,
                0.002,
                0.003,
                0.004,
                0.005,
                0.006,
                0.007,
                0.008,
                0.009,
                0.010,
                0.011,
                0.012,
                0.013,
                0.014,
                0.015,
                0.016,
                0.017,
                0.018,
                0.019,
                0.020,
                0.021,
                0.022,
                0.023,
                0.024,
                0.0241,
                0.0242,
                0.0243,
                0.0244,
                0.0245,
                0.0246,
                0.0247,
                0.0248,
                0.0249,
                0.025,
                0.026,
                0.0261,
                0.0262,
                0.0263,
                0.0264,
                0.0265,
                0.0266,
                0.0267,
                0.0268,
                0.0269,
                0.027,
                0.0271,
                0.0272,
                0.0273,
                0.0274,
                0.0275,
                0.0276,
                0.0277,
                0.0278,
                0.0279,
                0.028,
                0.029,
                0.030,
                0.031,
                0.0311,
                0.0312,
                0.0313,
                0.0314,
                0.0315,
                0.0316,
                0.0317,
                0.0318,
                0.0319,
                0.032,
                0.033,
                0.034,
                0.035,
                0.036,
                0.037,
                0.038,
                0.039,
                0.040,
                0.041,
                0.042,
                0.043,
                0.044,
                0.045,
                0.046,
                0.047,
                0.048,
                0.049,
                0.050,
                0.051,
                0.052,
                0.053,
                0.054,
                0.055,
                0.056,
                0.057,
                0.058,
                0.059,
                0.060,
                0.061,
                0.062,
                0.063,
                0.064,
                0.065,
                0.066,
                0.067,
                0.068,
                0.069,
                0.070,
                0.071,
                0.072,
                0.073,
                0.074,
                0.075,
                0.076,
                0.077,
                0.078,
                0.079,
                0.080,
                0.081,
                0.082,
                0.083,
                0.084,
                0.085,
                0.086,
                0.087,
                0.0871,
                0.0872,
                0.0873,
                0.0874,
                0.0875,
                0.0876,
                0.0877,
                0.0878,
                0.0879,
                0.088,
                0.089,
                0.090,
                0.091,
                0.092,
                0.093,
                0.094,
                0.095,
                0.096,
                0.097,
                0.098,
                0.099,
                0.100,
                0.110,
                0.120,
                0.130,
                0.140,
                0.150,
                0.160,
                0.170,
                0.180,
                0.190,
                0.200,
                0.210,
                0.220,
                0.230,
                0.240,
                0.250,
                0.260,
                0.270,
                0.280,
                0.290,
                0.200,
                0.200,
                0.200,
                0.300,
                0.310,
                0.320,
                0.330,
                0.340,
                0.350,
                0.360,
                0.370,
                0.380,
                0.390,
                0.400,
                0.500,
                0.600,
                0.700,
                0.800,
                0.900,
                1.000,


        };

        List<Candle> candles = null;
        Map<String, Candle> longCandles = null;

        for (double param : params) {
            log.info("" + (param * 1000));

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.setCandles(candles);
            sim1.run();

            if (candles == null) {
                candles = sim1.getCandles();
            }
        }
    }
}
