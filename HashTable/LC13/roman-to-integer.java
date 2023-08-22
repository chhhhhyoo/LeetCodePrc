import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

class Solution {
    public static int romanToInt(String s) {
        Dictionary<String, Integer> rn = new Hashtable();
        rn.put("I", 1);
        rn.put("V", 5);
        rn.put("X", 10);
        rn.put("L", 50);
        rn.put("C", 100);
        rn.put("D", 500);
        rn.put("M", 1000);
        String[] strSplit = s.split("");

        ArrayList<String> ss = new ArrayList<String>(
                Arrays.asList(strSplit));
        int part = 0;
        int total = 0;
        String p = ss.get(0);
        for (String k : ss) {
            if (rn.get(p) == rn.get(k)) {
                part += rn.get(k);
                total += rn.get(k);
            } else if (rn.get(p) < rn.get(k)) {
                total -= 2 * part;
                total += rn.get(k);
                part = 0;
            }

            else if (rn.get(p) > rn.get(k)) {
                total += rn.get(k);
                part = rn.get(k);
            }
            p = k;
        }

        return total;
    }
}