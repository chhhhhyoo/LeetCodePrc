class Solution:
    def longestConsecutive(self, nums: List[int]) -> int:
        if not nums:
            return 0
        nums = set(nums)
        final_seq = 0

        for num in nums:
            if num -1 not in nums:
                seq = 1
                curr_num = num
            
                while curr_num + 1 in nums:
                    curr_num += 1
                    seq += 1

                final_seq = max(final_seq, seq)

        return final_seq