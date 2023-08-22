package LinkedList.LC876;

/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode() {}
 * ListNode(int val) { this.val = val; }
 * ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public ListNode middleNode(ListNode head) {
        int rs;
        int count = 0;
        ListNode place = head;
        while (place != null) {
            count += 1;
            place = place.next;
        }
        if (count % 2 == 1) {
            for (int i = 0; i < (count - 1) / 2; i++) {
                head = head.next;
            }
        } else {
            for (int p = 0; p < count / 2; p++) {
                head = head.next;
            }
        }
        return head;
    }
}