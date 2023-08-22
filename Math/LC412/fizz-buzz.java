package Math.LC412;

import java.util.ArrayList;
import java.util.List;

class Solution {
    public List<String> fizzBuzz(int n) {
        List<String> FB = new ArrayList<String>(n);
        for (int i = 1; i <= n; i++) {
            if (i % 15 == 0) {
                FB.add("FizzBuzz");
            } else if (i % 3 == 0) {
                FB.add("Fizz");
            } else if (i % 5 == 0) {
                FB.add("Buzz");
            } else {
                FB.add(String.valueOf(i));
            }
        }
        return FB;
    }
}