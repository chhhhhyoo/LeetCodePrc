package Array.LC26;

class Solution {
    public int removeDuplicates(int[] nums) {
        int index = 1;
        int prev = nums[0];
        for (int i : nums) {
            if (prev < i) {
                nums[index] = i;
                prev = i;
                index += 1;
            }
        }
        return index;
    }
}