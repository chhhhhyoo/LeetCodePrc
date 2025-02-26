class Solution:
    def threeSum(self, nums: List[int]) -> List[List[int]]:
            sorted_nums = sorted(nums)
            result = []
            for i in range(len(nums)-2):
                if i > 0 and sorted_nums[i] == sorted_nums[i-1]:
                    continue
                start = i+1
                end = len(nums) - 1
                while start < end :
                    shot = sorted_nums[i] + sorted_nums[start] + sorted_nums[end]
                    if shot == 0:
                        result.append([sorted_nums[i], sorted_nums[start], sorted_nums[end]])
                        
                        while start < end and sorted_nums[start] == sorted_nums[start+1] :
                            start += 1

                        while start < end and sorted_nums[end] == sorted_nums[end-1] :
                            end -= 1

                        start += 1
                        end -= 1            

                    elif shot < 0 :
                        start += 1
                    else :
                        end -= 1

            return result