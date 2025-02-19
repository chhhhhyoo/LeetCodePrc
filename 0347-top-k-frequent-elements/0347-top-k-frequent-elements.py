from collections import Counter
class Solution:
    def topKFrequent(self, nums: List[int], k: int) -> List[int]:
        common_k = Counter(nums).most_common(k)
        return [num for num, _ in common_k[:k]]