class Solution:
    def carFleet(self, target: int, position: List[int], speed: List[int]) -> int:
        pos_speed = sorted(zip(position, speed), key= lambda x : x[0], reverse= True)
        time_needed = [(target - x[0])/x[1] for x in pos_speed]
        stack = []
        for t in time_needed:
            if stack and stack[-1] >= t:
                continue
            stack.append(t)
        return len(stack)