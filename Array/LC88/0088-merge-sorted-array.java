import java.util.Arrays;

class Solution {
    public static void merge(int[] nums1, int m, int[] nums2, int n) {
		int idx1 = m-1;
		int idx2 = n-1;
		int idx_merge = m+n-1;
		while(idx1 >= 0 && idx2 >= 0) {
			if(nums1[idx1] > nums2[idx2]) {
				nums1[idx_merge]=nums1[idx1];
				idx1--;
				}
			else {
				nums1[idx_merge]=nums2[idx2];
				idx2--;
			}
			idx_merge--;
		}
		while(idx2>=0) {
			nums1[idx_merge]= nums2[idx2];
			idx2--;
			idx_merge--;
		}
		System.out.println(Arrays.toString(nums1));		
	}
}