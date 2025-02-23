class Solution:
    def twoSum(self, numbers: List[int], target: int) -> List[int]:
        for i, n in enumerate(numbers):
            wanted = target - n
            if wanted in numbers[i+1:]:
                return [i+1, numbers[i+1:].index(wanted)+i+2]