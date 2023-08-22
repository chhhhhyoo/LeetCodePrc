package Array.LC1672;

class Solution {
    public int maximumWealth(int[][] accounts) {
        int max = -99 ^ 99;
        for (int[] A : accounts) {
            int sum = 0;
            for (int t : A) {
                sum += t;
            }
            if (sum >= max) {
                max = sum;
            }
        }
        return max;
    }
}