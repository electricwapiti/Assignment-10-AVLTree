package org.example;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringJoiner;

public class AVLTree {
    private static class Node {
        int val;
        Node left;
        Node right;
        int height;
        int size; // number of nodes in subtree rooted at this node

        Node(int v) {
            val = v;
            height = 1;
            size = 1;
        }
    }

    private Node root;

    // Public API
    public void insert(int value) {
        root = insert(root, value);
    }

    public void delete(int value) {
        root = delete(root, value);
    }

    // Preorder serialize with "nil" markers to preserve structure
    public String serialize() {
        StringJoiner sj = new StringJoiner(",");
        serializePre(root, sj);
        return sj.toString();
    }

    // Deserialize a tree created by serialize() and return a new AVLTree
    public static AVLTree deserialize(String s) {
        AVLTree tree = new AVLTree();
        if (s == null || s.isEmpty()) return tree;
        String[] parts = s.split(",");
        Deque<String> q = new ArrayDeque<>();
        for (String p : parts) q.addLast(p);
        tree.root = deserializePre(q);
        // fix heights bottom-up
        fixHeights(tree.root);
        return tree;
    }

    // ---------- Internal helpers ----------
    private static void serializePre(Node node, StringJoiner sj) {
        if (node == null) {
            sj.add("nil");
            return;
        }
        sj.add(Integer.toString(node.val));
        serializePre(node.left, sj);
        serializePre(node.right, sj);
    }

    private static Node deserializePre(Deque<String> q) {
        if (q.isEmpty()) return null;
        String t = q.removeFirst();
        if ("nil".equals(t)) return null;
        Node node = new Node(Integer.parseInt(t));
        node.left = deserializePre(q);
        node.right = deserializePre(q);
        return node;
    }

    private static int height(Node n) {
        return n == null ? 0 : n.height;
    }

    private static int size(Node n) {
        return n == null ? 0 : n.size;
    }

    private static void fixHeights(Node n) {
        if (n == null) return;
        fixHeights(n.left);
        fixHeights(n.right);
        // update both height and size
        n.height = 1 + Math.max(height(n.left), height(n.right));
        n.size = 1 + size(n.left) + size(n.right);
    }

    private Node insert(Node node, int value) {
        if (node == null) return new Node(value);
        if (value < node.val) node.left = insert(node.left, value);
        else if (value > node.val) node.right = insert(node.right, value);
        else return node; // duplicates ignored

        update(node);
        return balance(node);
    }

    private Node delete(Node node, int value) {
        if (node == null) return null;
        if (value < node.val) node.left = delete(node.left, value);
        else if (value > node.val) node.right = delete(node.right, value);
        else {
            // node to delete
            if (node.left == null || node.right == null) {
                Node temp = node.left != null ? node.left : node.right;
                node = temp; // may be null
            } else {
                // two children: get inorder successor (smallest in right)
                Node succ = minValueNode(node.right);
                node.val = succ.val;
                node.right = delete(node.right, succ.val);
            }
        }

        if (node == null) return null;

        update(node);
        return balance(node);
    }

    private static Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null) current = current.left;
        return current;
    }

    private Node balance(Node node) {
        int bf = getBalance(node);
        // Left heavy
        if (bf > 1) {
            if (getBalance(node.left) < 0) node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        // Right heavy
        if (bf < -1) {
            if (getBalance(node.right) > 0) node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        return node;
    }

    private int getBalance(Node node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        // rotation
        x.right = y;
        y.left = T2;

        // update heights and sizes
        y.height = 1 + Math.max(height(y.left), height(y.right));
        y.size = 1 + size(y.left) + size(y.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        x.size = 1 + size(x.left) + size(x.right);

        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        // rotation
        y.left = x;
        x.right = T2;

        // update heights and sizes
        x.height = 1 + Math.max(height(x.left), height(x.right));
        x.size = 1 + size(x.left) + size(x.right);
        y.height = 1 + Math.max(height(y.left), height(y.right));
        y.size = 1 + size(y.left) + size(y.right);

        return y;
    }

    private static void update(Node n) {
        if (n == null) return;
        n.height = 1 + Math.max(height(n.left), height(n.right));
        n.size = 1 + size(n.left) + size(n.right);
    }

    /**
     * Return number of nodes in the tree.
     */
    public int size() {
        return size(root);
    }

    /**
     * Select the k-th smallest element (0-based).
     * Throws IllegalArgumentException if k is out of bounds.
     */
    public int select(int k) {
        if (k < 0 || k >= size()) throw new IllegalArgumentException("k out of range");
        Node cur = root;
        while (cur != null) {
            int leftSize = size(cur.left);
            if (k < leftSize) cur = cur.left;
            else if (k == leftSize) return cur.val;
            else {
                k = k - leftSize - 1;
                cur = cur.right;
            }
        }
        throw new IllegalStateException("select failed");
    }

    /**
     * Return in-order index (0-based) of value if present, otherwise -1.
     */
    public int indexOf(int value) {
        Node cur = root;
        int idx = 0;
        while (cur != null) {
            if (value < cur.val) {
                cur = cur.left;
            } else if (value > cur.val) {
                idx += size(cur.left) + 1;
                cur = cur.right;
            } else {
                idx += size(cur.left);
                return idx;
            }
        }
        return -1;
    }
}
