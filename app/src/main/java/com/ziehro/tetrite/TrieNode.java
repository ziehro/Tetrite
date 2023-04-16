package com.ziehro.tetrite;

public class TrieNode {
    private TrieNode[] children;
    public boolean isEndOfWord;

    public TrieNode() {
        children = new TrieNode[26]; // 26 letters in the alphabet
        isEndOfWord = false;
    }

    public void insert(String word) {
        TrieNode node = this;
        for (char c : word.toCharArray()) {
            c = Character.toLowerCase(c); // Convert the character to lowercase
            int index = c - 'a';
            if (index < 0 || index >= children.length) { // Check if the index is within the valid range
                continue;
            }
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isEndOfWord = true;
    }


    public boolean search(String word) {
        TrieNode node = this;
        for (char c : word.toCharArray()) {
            c = Character.toLowerCase(c); // Convert the character to lowercase
            int index = c - 'a';
            if (index < 0 || index >= children.length) { // Check if the index is within the valid range
                return false;
            }
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return node.isEndOfWord;
    }

}
