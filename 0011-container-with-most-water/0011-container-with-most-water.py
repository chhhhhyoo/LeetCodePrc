class Solution:
    def maxArea(self, height: List[int]) -> int:
        start_index = 0
        end_index = len(height) - 1
        max_area = min(height[start_index], height[end_index]) * (end_index - start_index)

        while start_index < end_index:
            if height[start_index] <= height[end_index]:
                start_index += 1
                max_area = max(min(height[start_index], height[end_index]) * (end_index - start_index), max_area)
            else:
                end_index -= 1
                max_area = max(min(height[start_index], height[end_index]) * (end_index - start_index), max_area)

        return max_area