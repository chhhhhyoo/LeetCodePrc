class Solution {
    public boolean isPalindrome(int x) {
        if(x < 0){
            return false;
        }
        
        String original = String.valueOf(x);
        StringBuffer sbr = new StringBuffer(original);
        sbr.reverse();
        String b = String.valueOf(sbr);
        return original.equals(b);
    }
}