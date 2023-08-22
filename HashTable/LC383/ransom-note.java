package HashTable.LC383;

class Solution {
    public boolean canConstruct(String ransomNote, String magazine) {
        while (!magazine.isEmpty()) {
            char b = magazine.charAt(0);
            String a = String.valueOf(b);
            if (ransomNote.indexOf(a) != -1) {
                ransomNote = ransomNote.replaceFirst(a, "");
                magazine = magazine.replaceFirst(a, "");
            } else {
                magazine = magazine.replaceFirst(a, "");
            }
        }
        return ransomNote.isEmpty();
    }
}