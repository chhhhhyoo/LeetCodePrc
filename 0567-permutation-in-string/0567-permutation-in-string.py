class Solution:
    def checkInclusion(self, s1: str, s2: str) -> bool:
        n = len(s1)
        for i in range(len(s2)-n+1):
            partial_s2 = s2[i:i+n]
            if sorted(s1) == sorted(partial_s2):
                return True
        return False