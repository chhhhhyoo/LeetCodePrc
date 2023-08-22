package Array.LC1480;

class Solution {
    public int[] runningSum(int[] nums) {
        int[] result = new int[nums.length];
        int ip = 0;
        for (int i = 0; i < nums.length; i++) {
            ip += nums[i];
            result[i] = ip;
        }
        return result;
    }
}