package Array.LC724;

class Solution {
    public int pivotIndex(int[] nums) {
        int lft = 0;
        int rgt = 0;
        for (int i = 1; i < nums.length; i++) {
            rgt += nums[i];
        }

        for (int i = 0; i < nums.length; i++) {
            if (lft == rgt) {
                return i;
            }
            lft += nums[i];
            if (i < nums.length - 1) {
                rgt -= nums[i + 1];
            } else {
                rgt -= 0;
            }
        }
        return -1;
    }
}