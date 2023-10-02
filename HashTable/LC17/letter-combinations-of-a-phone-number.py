class Solution(object):
    def letterCombinations(self, digits):
        """
        :type digits: str
        :rtype: List[str]
        """
        if not digits :
            return []
        
        numpad = {
            '2': 'abc',
            '3': 'def',
            '4': 'ghi',
            '5': 'jkl',
            '6': 'mno',
            '7': 'pqrs',
            '8': 'tuv',
            '9': 'wxyz'
        }
        res = []

        def get_comb(combi, digits) :
            if not digits :
                # append each corresponding character in the digit
                res.append(combi)
                return
            num_chrs = numpad[digits[0]]
            for num_chr in num_chrs :
                get_comb(combi + num_chr, digits[1:])

        get_comb('', digits)
        return res