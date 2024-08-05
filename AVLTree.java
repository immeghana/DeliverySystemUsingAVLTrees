/**
 * Represents a AVL tree for managing orders
 * in the Gator Delivery system.
 */
class AVLTree {

    /**
     * Represents a node in the AVLTree, containing an Order and references to
     * left and right child nodes.
     */
    static class Node {
        Order order;
        Node left;
        Node right;
        int height;

        /**
         * Constructs a new Node with the specified Order.
         * @param order The Order to be stored in the node.
         */
        Node(Order order) {
            this.order = order;
            this.height = 1;
        }
    }

    Node root;

    /**
     * Calculates the height of a given node in the tree.
     */
    int height(Node node) {
        if (node == null) return 0;
        return node.height;
    }

    /**
     * Calculates the balance factor of a given node in the tree
     */
    int balance(Node node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    /**
     * Performs a right rotation on the given node.
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
     * Performs a left rotation on the given node.
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
     * Inserts a new node with the given Order into the tree.
     */
    Node insert(Node node, Order order) {
        if (node == null) {
            return new Node(order);
        }
    
        if (order.calculatePriority() < node.order.calculatePriority()) {
            node.left = insert(node.left, order);
        } else {
            node.right = insert(node.right, order);
        }
    
        node.height = 1 + Math.max(height(node.left), height(node.right));
    
        int balance = balance(node);
    
        if (balance > 1 && order.calculatePriority() < node.left.order.calculatePriority())
            return rightRotate(node);
    
        if (balance < -1 && order.calculatePriority() > node.right.order.calculatePriority())
            return leftRotate(node);
    
        if (balance > 1 && order.calculatePriority() > node.left.order.calculatePriority()) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
    
        if (balance < -1 && order.calculatePriority() < node.right.order.calculatePriority()) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
    
        return node;
    }
}
