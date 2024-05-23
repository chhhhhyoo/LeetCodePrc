def maxProfit(self, prices):
    if not prices:
        return 0

    n = len(prices)
    min_price = prices[0]
    max_profit = 0

    for i in range(1, n):
        min_price = min(min_price, prices[i])
        max_profit = max(max_profit, prices[i] - min_price)

    return max_profit
