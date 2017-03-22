package rjsv.circularview.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description
 *
 * @author <a href="mailto:ricardo.vieira@xpand-it.com">RJSV</a>
 * @version $Revision : 1 $
 */

public class GeneralUtils {

    // Mathematical
    public static double round(double value, int places) {
        if (places < 0) {
            return value;
        }
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static float round(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

    public static boolean fileExistsInAssets(Context context, String pathInAssets) {
        boolean result = false;
        if (context != null) {
            AssetManager mg = context.getResources().getAssets();
            InputStream is;
            try {
                is = mg.open(pathInAssets);
                result = true;
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                result = false;
            }
        }
        return result;
    }

    public static boolean isAClick(int threshold, float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX > threshold || differenceY > threshold) {
            return false;
        }
        return true;
    }
}

