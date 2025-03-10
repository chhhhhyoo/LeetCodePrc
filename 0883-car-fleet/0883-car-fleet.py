class Solution:
    def carFleet(self, target: int, position: List[int], speed: List[int]) -> int:
        sorted_position = sorted(enumerate(position), key= lambda x : x[1], reverse= True)
        sorted_speed = [speed[sorted_position[x][0]] for x in range(len(speed))]
        sorted_position = [x[1] for x in sorted_position]

        time_needed = [(target - sorted_position[x])/sorted_speed[x] for x in range(len(sorted_position))]
        cnt = 0
        while time_needed:
            needed = time_needed[0]
            time_needed = [x - needed for x in time_needed if x - needed > 0]
            cnt += 1
        return  cnt