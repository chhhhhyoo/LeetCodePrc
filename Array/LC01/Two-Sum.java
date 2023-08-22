class Solution {
    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        for (int i = 0; i < nums.length - 1; i++) {
            int get = 0;
            get = target - nums[i];
            result[0] = i;
            for (int j = i + 1; j < nums.length; j++) {
                if (get == nums[j]) {
                    result[1] = j;
                    return result;
                }
            }
        }
        return null;
    }
}