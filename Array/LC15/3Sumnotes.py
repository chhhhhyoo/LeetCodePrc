# 일단 숫자를 sort할 것이다. // python 의 sort 알고리즘은 Timsort 로 이것의 average 와 worst case time efficiency 는
# O(n log n) 이기 때문에 그냥 이 sort 를 사용하겠다
# two pointer 로 각 끝에서 하나씩 들어올 것이다 (이 방식 유지할까??)

# 다음 세번째 숫자를 더할때 만약 크면 더이상 iterate over 할 필요 없고 작다면 다음것 확인 같으면 같은
# 세번째 숫자가 또있는지 확인하고 없으면 다음 두숫자의 조합을 진행하자

# 첫번째 숫자가 0 초과일 때는 전체 iteration 스탑. (왜냐하면 나머지 두숫자는 뒤에 있는데 그러면 더해도 0이상이라 0을 만들수 없다)
# 첫 두 숫자의 합이 0 초과인 경우도 같이 적용
# 셋째도 같이 적용 (지금 이 세가지 경우의 조건이 비슷한데 이것을 묶을 수 있을까)
# 각 for loop 사이에 바로 다음 for loop 을 돌리지 말고 체크 하는 부분을 만들어서 break 하자

# 하나하나 iteration 해서 확인 하는 것도 contains 로 확인하는 것 중에 무엇이 time efficiency 가 좋을까?
# contains method 는 어떤 algorithm 을 통해 서칭 하는 것일까?

class Solution(object):
    def threeSum(self, nums):
        """
        :type nums: List[int]
        :rtype: List[List[int]]
        """
        result = []
        nums.sort()

        for idx in range(len(nums) - 2) :
            if idx > 0 & nums[idx] == nums[idx-1] : # same inspection already done in prev iteration
                continue
            i = nums[idx] # first value (smallest)
            if i > 0 :
                break
            start = idx + 1
            end = len(nums) - 1
            while start < end :
                j = nums[start] # second value (middle)
                if i + j > 0 :
                    break
                k = nums[end] # third value (last)
                sum = i + j + k
                if sum == 0 : # wanted pair
                    result.append([i, j, k])
                    if j == k :
                        break
                    while start < end and nums[start] == nums[start+1] :
                        start += 1
                    while start < end  and nums[end] == nums[end-1] :
                        end -= 1
                    start += 1
                    end -= 1
                    
                elif sum > 0 :
                    end -= 1
                else :
                    start += 1

        return result
    
                    
        # for idx_i in range(len(nums)-2) :
        #     if nums[idx_i] > 0 :
        #         break
        #     i = nums[idx_i]
        #     for idx_j in range(idx_i + 1, len(nums)-1) :
        #         if i + nums[idx_j] > 0 :
        #             break
        #         j = nums[idx_j]
        #         for idx_k in range(idx_j + 1, len(nums)) :
        #             if i + j + nums[idx_k] > 0 :
        #                 break
        #             k = nums[idx_k]
                    
        #             if i + j + k == 0 :
        #                 pair = [i, j, k]
        #                 if pair not in result :
        #                     result.append(pair)

        # return result


if __name__ == "__main__":
    solution = Solution()

    # nums = [-2,0,1,1,2]
    # nums = [0,1,1]
    # nums = [0,0,0]
    nums = [0, 0]
    print('nums : ', type(nums))
    result = solution.threeSum(nums)
    print(result)
