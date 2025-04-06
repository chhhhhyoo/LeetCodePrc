from collections import Counter
class Solution:
    def checkInclusion(self, s1: str, s2: str) -> bool:
        len1, len2 = len(s1), len(s2)
        s1_counter = Counter(s1)
        for i in range(len2-len1+1):
            s2_window = Counter(s2[i:i+len1])
            if s2_window == s1_counter:
                return True
        return False