class Solution:
    def characterReplacement(self, s: str, k: int) -> int:
        left = 0
        char_count = defaultdict(int)
        max_count = 0
        res = 0

        for right in range(len(s)):
            c = s[right]
            char_count[c] += 1
            max_count = max(max_count, char_count[c])

            if right - left + 1 - max_count > k :
                char_count[s[left]] -= 1
                left += 1

            res = max(res, right - left + 1)

        return res