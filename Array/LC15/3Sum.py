class Solution(object):
    def threeSum(self, nums):
        """
        :type nums: List[int]
        :rtype: List[List[int]]
        """
        result = []
        nums.sort()

        for idx in range(len(nums) - 2) :
            if idx > 0 & nums[idx] == nums[idx-1] : # same inspection already done in prev iteration
                continue
            i = nums[idx] # first value (smallest)
            if i > 0 :
                break
            start = idx + 1
            end = len(nums) - 1
            while start < end :
                j = nums[start] # second value (middle)
                if i + j > 0 :
                    break
                k = nums[end] # third value (last)
                sum = i + j + k
                if sum == 0 : # wanted pair
                    result.append([i, j, k])
                    if j == k :
                        break
                    while start < end and nums[start] == nums[start+1] :
                        start += 1
                    while start < end  and nums[end] == nums[end-1] :
                        end -= 1
                    start += 1
                    end -= 1
                    
                elif sum > 0 :
                    end -= 1
                else :
                    start += 1

        return result