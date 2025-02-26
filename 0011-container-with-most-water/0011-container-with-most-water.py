class Solution:
    def maxArea(self, height: List[int]) -> int:
        start_index = 0
        end_index = len(height) - 1
        max_area = 0

        while start_index < end_index:
            max_area = max(min(height[start_index], height[end_index]) * (end_index - start_index),max_area)
            if height[start_index] <= height[end_index]:
                start_index += 1
            else:
                end_index -= 1

        return max_area