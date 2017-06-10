package server.utils;

/**
 * klasa odpowiada za operacje na ci�gach znak�w
 *
 * @author andrzej.salamon@biztech.pl
 *
 */
public class StringUtils {
    /**
     * ��czy zadane warto�ci, u�ywaj�c zadanego ci�gu znak�w
     *
     * @param arr
     *            parametry do z��czenia
     * @param glue
     *            ci�g znak�w, kt�rym b�d� po��czone warto�ci
     * @return po��czony ci�g warto�ci
     */
    public static String combine(Object[] arr, String glue) {
        int k = arr.length;
        if (k == 0)
            return null;
        StringBuilder out = new StringBuilder();
        out.append(arr[0]);
        for (int x = 1; x < k; ++x)
            out.append(glue).append(arr[x]);
        return out.toString();
    }
}
