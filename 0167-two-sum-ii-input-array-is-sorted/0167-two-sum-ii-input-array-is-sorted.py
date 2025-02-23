class Solution:
    def twoSum(self, numbers: List[int], target: int) -> List[int]:
        left = 0
        right = len(numbers)-1
        curr_sum = numbers[left] + numbers[right]
        while target != curr_sum:
            if target > curr_sum:
                left += 1
            else :
                right -= 1
            curr_sum = numbers[left] + numbers[right]

        return [left+1, right+1]