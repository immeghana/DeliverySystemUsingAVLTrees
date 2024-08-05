/**
 * Tree for managing ETA values
 * associated with orders in the Gator Delivery system.
 */
class ETATree {
    
    /**
     * Represents a node in the ETATree, containing an ETA value and a reference to
     * the corresponding node in the AVLTree.
     */
    static class Node {
        int eta;
        AVLTree.Node avlNode;
        Node left;
        Node right;
        int height;

        /**
         * Constructs a new Node with the specified ETA value and AVLNode reference.
         * @param eta The ETA value.
         * @param avlNode The corresponding AVLNode reference.
         */
        Node(int eta, AVLTree.Node avlNode) {
            this.eta = eta;
            this.avlNode = avlNode;
            this.height = 1;
        }
    }

    /** The root node of the ETATree. */
    Node root;

    /**
     * Calculates the height of a given node in the tree.
     * @param node The node whose height is to be calculated.
     * @return The height of the node.
     */
    int height(Node node) {
        if (node == null) return 0;
        return node.height;
    }

    /**
     * Calculates the balance factor at a given node in the tree.
     * @param node The node whose balance factor is to be calculated.
     * @return The balance factor of the node.
     */
    int balance(Node node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    /**
     * Performs a right rotation at the given node.
     * @param y The node around which the rotation is performed.
     * @return The new root node after rotation.
     */
    Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    /**
     * Performs a left rotation at the given node.
     * @param x The node around which the rotation is performed.
     * @return The new root node after rotation.
     */
    Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    /**
     * Inserts a new node with the given ETA and AVLNode references into the tree.
     */
    Node insert(Node node, int eta, AVLTree.Node avlNode) {
        if (node == null) return new Node(eta, avlNode);

        if (eta < node.eta)
            node.left = insert(node.left, eta, avlNode);
        else
            node.right = insert(node.right, eta, avlNode);

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = balance(node);

        if (balance > 1 && eta < node.left.eta)
            return rightRotate(node);

        if (balance < -1 && eta > node.right.eta)
            return leftRotate(node);

        if (balance > 1 && eta > node.left.eta) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        if (balance < -1 && eta < node.right.eta) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }
}
