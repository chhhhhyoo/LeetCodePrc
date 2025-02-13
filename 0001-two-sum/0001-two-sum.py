class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        hash_dict = {}
        for i, n in enumerate(nums):
            wanted = target - n
            if wanted in hash_dict :
                return [i, hash_dict[wanted]]
            
            hash_dict[n] = i
        
        return []
            
        
        


        