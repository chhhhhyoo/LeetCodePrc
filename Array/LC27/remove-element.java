package Array.LC27;

class Solution {
    public int removeElement(int[] nums, int val) {
        int index = 0;
        for (int i : nums) {
            if (i != val) {
                nums[index] = i;
                index += 1;
            }
        }
        return index;
    }
}