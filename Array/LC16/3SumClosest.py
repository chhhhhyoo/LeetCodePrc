class Solution(object):
    def threeSumClosest(self, nums, target):
        """
        :type nums: List[int]
        :type target: int
        :rtype: int
        """
        nums.sort()
        res_sum = float('inf')
        min_diff = float('inf')

        for idx in range(len(nums) - 2) :
            if idx > 0 and nums[idx] == nums[idx-1] : # same inspection already done in prev iteration
                continue
            i = nums[idx] # first value (smallest)
            start = idx + 1
            end = len(nums) - 1

            while start < end :
                j = nums[start] # second value (middle)
                k = nums[end] # third value (last)
                current_sum = i + j + k
                current_diff = abs(current_sum - target)

                if current_diff < min_diff :
                    min_diff = current_diff
                    res_sum = current_sum
                
                if current_sum == target : # wanted pairs
                    return current_sum
                
                elif current_sum < target :
                    while start < end and nums[start] == nums[start+1] :
                        start += 1
                    start += 1
                else :
                    while start < end  and nums[end] == nums[end-1] :
                        end -= 1
                    end -= 1
        return res_sum