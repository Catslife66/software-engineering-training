# 3591. Question: Check if Any Element Has Prime Frequency -> hash-lookup

You are given an integer array nums.
Return true if the frequency of any element of the array is prime, otherwise, return false.
The frequency of an element x is the number of times it occurs in the array.
A prime number is a natural number greater than 1 with only two factors, 1 and itself.

Example 1:

> Input: nums = [1,2,3,4,5,4]
> Output: true
> Explanation:
> 4 has a frequency of two, which is a prime number.

Example 2:

> Input: nums = [1,2,3,4,5]
> Output: false
> Explanation:
> All elements have a frequency of one.

Example 3:

> Input: nums = [2,2,2,4,4]
> Output: true
> Explanation:
> Both 2 and 4 have a prime frequency.

Constraints:

- 1 <= nums.length <= 100
- 0 <= nums[i] <= 100

## Solution

```
import math

class Solution:
    def isPrime(self, num: int) -> bool:
        if n < 2:
            return False
        //Only check up to the square root of n
        for i in range(2, int(math.sqrt(n)) + 1):
            if n % i == 0:
                return False
        return True

    def checkPrimeFrequency(self, nums: List[int]) -> bool:
        frequency = {}
        for i in nums:
            if i in frequency:
                frequency[i] += 1
            else:
                frequency[i] = 1

        for freq in frequency.values():
            if self.isPrime(freq):
                return True
        return False
```
