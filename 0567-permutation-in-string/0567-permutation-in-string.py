from collections import Counter
class Solution:
    def checkInclusion(self, s1: str, s2: str) -> bool:
        len1, len2 = len(s1), len(s2)
        if len1 > len2 :
            return False
        
        s1_counter = Counter(s1)
        window_counter = Counter(s2[:len1])

        if s1_counter == window_counter:
            return True
        
        for i in range(len1, len2) :
            incoming = s2[i]
            outgoing = s2[i-len1]

            window_counter[incoming] += 1
            window_counter[outgoing] -= 1
            if window_counter[outgoing] == 0:
                del window_counter[outgoing]

            if s1_counter == window_counter:
                return True
            
        return False