class Solution:
    def carFleet(self, target: int, position: List[int], speed: List[int]) -> int:
        pos_speed = sorted([[x,y] for x, y in zip(position, speed)], key= lambda x : x[0], reverse= True)
        time_needed = [(target - x[0])/x[1] for x in pos_speed]
        cnt = 0
        while time_needed:
            needed = time_needed[0]
            time_needed = [x - needed for x in time_needed if x - needed > 0]
            cnt += 1
        return  cnt