class Solution {
        public int searchInsert(int[] nums, int target) {
            if(nums[0] > target){
                return 0;
            }
            if(nums[nums.length-1] < target) {
                return nums.length;
            }
            return binarySearch(nums,0,nums.length,target);
        }
        
        public int binarySearch(int arr[], int start, int end, int target)
        {	
            if (end >= start) {
                int mid = start + (end - start) / 2;
                
                if (arr[mid] == target)
                    return mid;
                if (arr[mid] < target && target < arr[mid+1])
                    return mid+1;            
                if (arr[mid] > target)
                    return binarySearch(arr, start, mid - 1, target);

                return binarySearch(arr, mid + 1, end, target);
            }
            return -1;
        }
}