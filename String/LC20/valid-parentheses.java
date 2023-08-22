package String.LC20;

class Solution {
    public boolean isValid(String s) {
        if (s.startsWith(")") || s.startsWith("}") || s.startsWith("]")) {
            return false;
        }
        int prelength = s.length();
        while (s.length() != 0) {
            s = s.replace("()", "");
            s = s.replace("{}", "");
            s = s.replace("[]", "");
            if (prelength == s.length()) {
                return false;
            } else {
                prelength = s.length();
            }
        }
        return true;
    }
}