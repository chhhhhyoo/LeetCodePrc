package String.LC14;

class Solution {
    public String longestCommonPrefix(String[] strs) {
        String res = strs[0];
        if (strs[0].length() == 0) {
            return "";
        }
        for (int i = 1; i < strs.length; i++) {
            while (strs[i].indexOf(res) != 0) {
                res = res.substring(0, res.length() - 1);
            }
            if (res.length() == 0) {
                return "";
            }
        }
        return res;
    }
}