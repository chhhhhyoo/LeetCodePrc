class Solution {
    public int[] plusOne(int[] digits) {
        int d_len = digits.length;
		int carry = 1;
		for(int i = d_len -1; i>=0; i--) {
			int tmp = digits[i] + carry;
			carry = tmp/10;
			digits[i] = tmp%10;
			if(carry==0) {
				break;
			}
		}
		if(carry>0) {
			int[] newD = new int[d_len+1];
			newD[0] = carry;
			for(int j=0; j<d_len; j++) {
				newD[j+1] = digits[j];
			}
			return newD;
		}
		return digits;
    }
}