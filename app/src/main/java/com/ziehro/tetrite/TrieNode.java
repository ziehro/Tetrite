package com.ziehro.tetrite;

import android.util.Log;

public class TrieNode {
    private final int ALPHABET_SIZE = 52;
    TrieNode[] children;
    boolean isWord;

    public TrieNode() {
        this.children = new TrieNode[ALPHABET_SIZE];
        this.isWord = false;
    }

    public void insert(String word) {
        TrieNode currentNode = this;
        for (char c : word.toCharArray()) {
            int index = Character.toLowerCase(c) - 'a'; // Adjust the index calculation to handle both lowercase and uppercase letters
            if (currentNode.children[index] == null) {
                currentNode.children[index] = new TrieNode();
            }
            currentNode = currentNode.children[index];
        }
        currentNode.isWord = true;
    }

    public boolean search(String word) {
        TrieNode currentNode = this;
        for (char c : word.toCharArray()) {

            int index = Character.toLowerCase(c) - 'a'; // Adjust the index calculation to handle both lowercase and uppercase letters

            if (currentNode.children[index] == null) {
                return false;
            }
            currentNode = currentNode.children[index];
        }
        return currentNode.isWord;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(this, "", sb);
        return sb.toString();
    }

    private void buildString(TrieNode node, String prefix, StringBuilder sb) {
        if (node.isWord) {
            sb.append(prefix);
            sb.append("\n");
        }

        for (int i = 0; i < node.children.length; i++) {
            TrieNode child = node.children[i];
            if (child != null) {
                char letter = (char) (i + 'a');
                buildString(child, prefix + letter, sb);
            }
        }
    }
}
