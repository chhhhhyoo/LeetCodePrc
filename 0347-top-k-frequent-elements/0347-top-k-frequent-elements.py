class Solution:
    def topKFrequent(self, nums: List[int], k: int) -> List[int]:
        count_num = {}
        for n in nums:
            if n in count_num:
                count_num[n] += 1
            else :
                count_num[n] = 1
        print(count_num)
        count_num = sorted(count_num.items(), key= lambda kv : (kv[1], kv[0]), reverse=True)
        return [num for num, _ in count_num[:k]]