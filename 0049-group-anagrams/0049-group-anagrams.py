from collections import defaultdict
class Solution:
    def groupAnagrams(self, strs: List[str]) -> List[List[str]]:
        search_dict = defaultdict(list)
        for s in strs:
            search_dict[tuple(sorted(s))].append(s)
        
        result = []
        for key in search_dict.keys():
            result.append(search_dict[key])
        
        return result