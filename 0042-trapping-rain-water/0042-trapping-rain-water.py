class Solution:
    def trap(self, height: List[int]) -> int:
        prefix = []
        suffix = []
        # getting prefix and suffix
        # prefix
        for i in range(len(height)):
            if not prefix:
                prefix.append(height[i])

            elif height[i] > prefix[-1]:
                prefix.append(height[i])
            else :
                prefix.append(prefix[-1])
        # suffix
        for i in reversed(range(len(height))):
            if not suffix:
                suffix.append(height[i])
            if height[i] > suffix[-1]:
                suffix.append(height[i])
            else :
                suffix.append(suffix[-1])
        
        suffix = suffix[::-1]

        trapped_water = 0

        for i in range(len(height)):
            min_height = min(prefix[i], suffix[i])
            trapped_water += min_height - height[i]
        
        return trapped_water