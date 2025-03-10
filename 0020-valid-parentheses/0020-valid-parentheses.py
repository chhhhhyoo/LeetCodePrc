class Solution:
    def isValid(self, s: str) -> bool:
        parentheses_map = {'{':'}', '(':')', '[':']'}
        p_stack = []

        for c in s:
            if c in parentheses_map:
                p_stack.append(c)
            elif c in parentheses_map.values():
                if not p_stack or parentheses_map[p_stack[-1]] != c:
                    return False
                else :
                    p_stack.pop()
        return not p_stack
