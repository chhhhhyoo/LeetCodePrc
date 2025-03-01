class Solution:
    def trap(self, height: List[int]) -> int:
        prefix = []
        suffix = []
        curr_prefix = 0
        curr_suffix = 0
        # getting prefix and suffix
        # prefix
        for i in range(len(height)):
            if height[i] > curr_prefix:
                prefix.append(height[i])
                curr_prefix = height[i]
            else :
                prefix.append(curr_prefix)
        # suffix
        for i in reversed(range(len(height))):
            if height[i] > curr_suffix:
                suffix.append(height[i])
                curr_suffix = height[i]
            else :
                suffix.append(curr_suffix)
        
        suffix = list(reversed(suffix))

        trapped_water = 0

        for i in range(len(height)):
            min_height = min(prefix[i], suffix[i])
            trapped_water += min_height - height[i]
        
        return trapped_water