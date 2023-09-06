class Solution(object):
    def lengthOfLongestSubstring(self, s):
        long_sub = ''
        check_sub = ''

        for c in s:
            if c not in check_sub:
                check_sub += c
            else :
                if len(check_sub) > len(long_sub):
                    long_sub = check_sub
                dup_index = check_sub.find(c) + 1
                check_sub = check_sub[dup_index:] + c

        if len(check_sub) > len(long_sub):
            long_sub = check_sub

        return len(long_sub)

if __name__ == "__main__":
    solution = Solution()

    # s1 = "abcabcbb"
    # result1 = solution.lengthOfLongestSubstring(s1)
    # print(result1)

    # s2 = "bbbbb"
    # result2 = solution.lengthOfLongestSubstring(s2)
    # print(result2)

    # s3 = "pwwkew"
    # result3 = solution.lengthOfLongestSubstring(s3)
    # print(result3)
    
    # s4 = ""
    # result4 = solution.lengthOfLongestSubstring(s4)
    # print(result4)

    # s5 = "dvdf"
    # result5 = solution.lengthOfLongestSubstring(s5)
    # print(result5)

    # s6 = "jbpnbwwd"
    # result6 = solution.lengthOfLongestSubstring(s6)
    # print(result6)

    # s7 = " a"
    # result7 = solution.lengthOfLongestSubstring(s7)
    # print(result7)

    # s8 = "aabaab!bb"
    # result8 = solution.lengthOfLongestSubstring(s8)
    # print(result8)

    # s9 = "loddktdji"
    # result9 = solution.lengthOfLongestSubstring(s9)
    # print(result9)

    s10 = "jbpnbwwd"
    result10 = solution.lengthOfLongestSubstring(s10)
    print(result10)
