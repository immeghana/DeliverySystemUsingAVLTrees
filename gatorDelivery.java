import java.util.*;
import java.io.*;

/**
 * The GatorDelivery class implements a delivery management system for handling orders, priorities, delivery times, and estimated time of arrival (ETA).
 * It utilizes AVL trees for order prioritization and ETA trees for efficient retrieval of orders within specified time ranges.
 */
public class gatorDelivery {
 static PrintWriter writer;
    static AVLTree priorityTree = new AVLTree(); // AVL tree for order prioritization
    static ETATree etaTree = new ETATree(); // ETA tree for efficient retrieval of orders within time ranges
    static int eta_track = 0; // Tracks the overall delivery time for ETA calculation
    static Set<Integer> deliveredOrderIds = new HashSet<>(); // Set to track delivered orders


/**
 * This is the entry point of the program and is called when the program starts. 
 * It's used to read input from a file, process commands, and write output to another file.
 */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GatorDelivery <input_filename>");
            return;
        }

        String inputFilename = args[0];
        String outputFilename = inputFilename.replace(".txt", "_output.txt");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFilename));
            PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFilename))
        ) {
            writer = fileWriter; // Assign the fileWriter to the global writer variable
            String command;
            while ((command = reader.readLine()) != null) {
                command = command.trim();
                if (command.equals("Quit()"))
                    break;
                processCommand(command);
            }
        } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
        }
    }

    //this method will process the commands from text file.
    static void processCommand(String command) {
        String[] tokens = command.split("\\(|,|\\)");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim(); // Trim each token
   	}
        switch (tokens[0]) {
            case "print":
                if (tokens.length == 2) {
                    int orderId = Integer.parseInt(tokens[1]);
                    print(orderId);
                } else if (tokens.length == 3) {
                    int time1 = Integer.parseInt(tokens[1].trim());
                    int time2 = Integer.parseInt(tokens[2].trim());
                    print(time1, time2);
                } else {
		    //This line will send the strings to the writeOutput method. Which is a method that will write to the output file. This line will be used throughout the code.
                    writeOutput("Invalid order command.");
                }
                break;
            case "getRankOfOrder":
                if (tokens.length == 2) {
                    int orderId = Integer.parseInt(tokens[1]);
                    getRankOfOrder(orderId);
                } else {
                    writeOutput("Invalid getRankOfOrder command.");
                }
                break;
            case "createOrder":
                if (tokens.length == 5) {
                    int orderId = Integer.parseInt(tokens[1]);
                    int currentSystemTime = Integer.parseInt(tokens[2]);
                    int orderValue = Integer.parseInt(tokens[3]);
                    int deliveryTime = Integer.parseInt(tokens[4]);
                    createOrder(orderId, currentSystemTime, orderValue, deliveryTime);
                } else {
                    writeOutput("Invalid createOrder command.");
                }
                break;
            case "cancelOrder":
                if (tokens.length == 3) {
                    int orderId = Integer.parseInt(tokens[1]);
                    int currentSystemTime = Integer.parseInt(tokens[2]);
                    cancelOrder(orderId, currentSystemTime);
                } else {
                    writeOutput("Invalid cancelOrder command.");
                }
                break;
            case "updateTime":
                if (tokens.length == 4) {
                    
		    int orderId = Integer.parseInt(tokens[1]);
                    int currentSystemTime = Integer.parseInt(tokens[2]);
                    int newDeliveryTime = Integer.parseInt(tokens[3]);
                    updateTime(orderId, currentSystemTime, newDeliveryTime);
                } else {
                    writeOutput("Invalid updateTime command.");
                }
                break;
            default:
                writeOutput("Invalid command: " + command);
                break;
        }
    }
   
    // This method prints the details of a specific order. It calculates the ETA and prints the order details if the order exists. 
    static void print(int orderId) {
        // Retrieve order from AVL tree
        Order order = searchOrder(orderId);

        if (order != null) {

            // Calculate ETA
            int eta = calculateETA(order);

            // Print order details
            writeOutput("[" + order.orderId + ", " + order.currentSystemTime + ", " + order.orderValue + ", "
                    + order.deliveryTime + ", " + eta + "]");
        } else {
            writeOutput("Order with orderId " + orderId + " not found.");
        }
    }

   // This method prints orders within a specified time range. It traverses the ETA tree to find orders within the range and prints them. This method is used.
   static void print(int time1, int time2) {
    // Retrieve orders within the given time range
    List<Integer> ordersWithinRange = new ArrayList<>();
    printOrdersWithinRange(etaTree.root, time1, time2, ordersWithinRange);

    if (!ordersWithinRange.isEmpty()) {
        // Convert the list of integers to a comma-separated string
        String ordersString = String.join(", ", ordersWithinRange.stream().map(Object::toString).toArray(String[]::new));
        writeOutput(ordersString);
    } else {
        writeOutput("There are no orders in that time period");
    }
}


    //This method is a helper method for the print(int time1, int time2) method. It recursively traverses the ETA tree to find orders within the specified time range
    static void printOrdersWithinRange(ETATree.Node node, int time1, int time2, List<Integer> ordersWithinRange) {
        if (node != null) {

            int etaWithDeliveryTime = node.eta;

            if (etaWithDeliveryTime >= time1 && node.eta <= time2 && !deliveredOrderIds.contains(node.avlNode.order.orderId)) {
                ordersWithinRange.add(node.avlNode.order.orderId);
            }

	    //traverse all the nodes
            printOrdersWithinRange(node.left, time1, time2, ordersWithinRange);
            printOrdersWithinRange(node.right, time1, time2, ordersWithinRange);
        }
    }


    //This method retrieves the rank of a specific order based on its priority in the AVL tree. It's called when the "getRankOfOrder" command is processed. 
    static void getRankOfOrder(int orderId) {
        // Retrieve rank of order from AVL tree
        int rank = getRankOfOrder(priorityTree.root, orderId);

        if (rank > 0) {
            writeOutput("Order " + orderId + " will be delivered after " + rank + " orders.");
        } else {
            writeOutput("Order " + orderId + " not found.");
        }
    }

    // This method is a helper method for getRankOfOrder(int orderId). It recursively traverses the AVL tree to find the rank of the order. 
    static int getRankOfOrder(AVLTree.Node node, int orderId) {
        if (node == null) return 0;
    
        if (node.order.orderId == orderId) return 1;
    
        if (node.order.orderId < orderId)
            return 1 + size(node.left) + getRankOfOrder(node.right, orderId);
        else
            return getRankOfOrder(node.left, orderId);
    }


    //This method calculates the size of the AVL tree rooted at the given node. It's called to determine the size of the AVL tree, but it's not directly used in the main functionality of the program. Used at debugging.
    static int size(AVLTree.Node node) {
        if (node == null) return 0;
        return 1 + size(node.left) + size(node.right);
    }
    
    //Another debugging method
    static void printAVLTreeStructure(AVLTree.Node node) {
        if (node != null) {
            printAVLTreeStructure(node.left);
            //System.out.println("Order ID: " + node.order.orderId + ", Priority: " + node.order.calculatePriority());
            printAVLTreeStructure(node.right);
        }
    }
    

    //This method creates a new order and inserts it into the AVL and ETA trees. It's called when the "createOrder" command is processed.
    static void createOrder(int orderId, int currentSystemTime, int orderValue, int deliveryTime) {
        
        Order order = new Order(orderId, currentSystemTime, orderValue, deliveryTime);
        priorityTree.root = priorityTree.insert(priorityTree.root, order);
        int eta = calculateETA(order);
        eta += eta_track; //eta_track maintains a track of ETAs of previous orders
    
        // Update eta_track
        eta_track += order.deliveryTime+currentSystemTime;
    
        // Insert ETA into the ETA tree
        etaTree.root = etaTree.insert(etaTree.root, eta, priorityTree.root);
    
        // Print the orderId values of nodes in the AVL tree
        //System.out.println("Debug: Printing AVL Tree structure after creating order " + orderId + ":");
       // printAVLTreeStructure(priorityTree.root);
    
        // Check if the newly created order has higher priority than existing orders
        AVLTree.Node highestPriorityNode = findHighestPriorityOrder(priorityTree.root, order);
    
        if (highestPriorityNode != null && highestPriorityNode.order.calculatePriority() >= order.calculatePriority() &&
                calculateETA(highestPriorityNode.order) <= currentSystemTime) {
            // Remove the highest priority order from the delivery queue
            priorityTree.root = deleteNode(priorityTree.root, highestPriorityNode.order.orderId);
    
            // Update ETA for orders affected by the change in delivery queue order
            updateETAForAffectedOrders(highestPriorityNode.order, currentSystemTime);
            
            // Print the change in delivery queue order
            writeOutput("Order " + highestPriorityNode.order.orderId + " has been delivered. Order " +
                               order.orderId + " is now under delivery.");
    
            // Print the updated ETAs
            printUpdatedETAs(currentSystemTime);
        } else {
            // Print the newly created order with its ETA
            if (currentSystemTime >= eta) {
                writeOutput("Order " + orderId + " has been delivered at time " + eta + ".");
            } else {
                writeOutput("Order " + orderId + " has been created - ETA: " + eta);
            }
    
            // Print delivered orders
            printDeliveredOrders(currentSystemTime);
        }
    }



    // This method finds the highest priority order in the AVL tree. It's called to determine if a newly created order has higher priority than existing orders, but it's not directly used in the main functionality of the program.
    static AVLTree.Node findHighestPriorityOrder(AVLTree.Node node, Order order) {
        if (node == null) return null;
    
        AVLTree.Node highestPriorityNode = null;
        if (order.calculatePriority() < node.order.calculatePriority()) {
            highestPriorityNode = findHighestPriorityOrder(node.left, order);
            if (highestPriorityNode == null) {
                highestPriorityNode = node;
            }
        } else {
            highestPriorityNode = findHighestPriorityOrder(node.right, order);
        }
    
        return highestPriorityNode;
    }

    //This method updates the ETAs of orders affected by the delivery of a specific order. It's called when an order is delivered to adjust the ETAs of other orders in the ETA tree.
    static void updateETAForAffectedOrders(Order deliveredOrder, int currentSystemTime) {
        // Traverse the ETA tree to update ETAs of orders affected by the delivery of the deliveredOrder
        etaTree.root = updateETAForAffectedOrders(etaTree.root, deliveredOrder, currentSystemTime);
    }
    

    // This method is a helper method for updateETAForAffectedOrders(Order deliveredOrder, int currentSystemTime). It recursively traverses the ETA tree to update ETAs. 
    static ETATree.Node updateETAForAffectedOrders(ETATree.Node node, Order deliveredOrder, int currentSystemTime) {
        if (node == null) return null;
    
        if (node.avlNode.order.orderId == deliveredOrder.orderId) {
            node.eta = Integer.MAX_VALUE; // Mark this node as delivered
        }
    
        if (node.avlNode.order.orderId < deliveredOrder.orderId) {
            // Update ETA if the order is not delivered yet
            if (node.eta > currentSystemTime) {
                node.eta = currentSystemTime + deliveredOrder.deliveryTime;
                writeOutput("Updated ETA for order " + node.avlNode.order.orderId + " to: " + node.eta);
            }
            node.left = updateETAForAffectedOrders(node.left, deliveredOrder, currentSystemTime);
        } else {
            node.right = updateETAForAffectedOrders(node.right, deliveredOrder, currentSystemTime);
        }
    
        return node;
    }


    //This method prints orders that have been delivered up to the current system time. It traverses the ETA tree to find delivered orders and prints them. 
    static void printDeliveredOrders(int currentSystemTime) {
        Map<Integer, Integer> deliveredOrders = new HashMap<>();
        printDeliveredOrders(etaTree.root, currentSystemTime, deliveredOrders);
    
        for (Map.Entry<Integer, Integer> entry : deliveredOrders.entrySet()) {
            int orderId = entry.getKey();
            int eta = entry.getValue();
            writeOutput("Order " + orderId + " has been delivered at time " + eta + ".");
        }
    }

    //This method is a helper method for printDeliveredOrders(int currentSystemTime). It recursively traverses the ETA tree to find delivered orders and adds them to a map.
    static void printDeliveredOrders(ETATree.Node node, int currentSystemTime, Map<Integer, Integer> deliveredOrders) {
        if (node != null) {
            if (node.eta <= currentSystemTime && !deliveredOrderIds.contains(node.avlNode.order.orderId)) {
                AVLTree.Node avlNode = node.avlNode;
    	    deliveredOrderIds.add(avlNode.order.orderId);
                deliveredOrders.put(avlNode.order.orderId, node.eta);
            }
            printDeliveredOrders(node.left, currentSystemTime, deliveredOrders);
            printDeliveredOrders(node.right, currentSystemTime, deliveredOrders);
        }
    }


    //This method removes delivered orders from the AVL and ETA trees.
    static void removeDeliveredOrders(Map<Integer, Integer> deliveredOrders) {
        for (Integer orderId : deliveredOrders.keySet()) {
            priorityTree.root = deleteNode(priorityTree.root, orderId);
            etaTree.root = deleteOrderFromETATree(etaTree.root, orderId);
        }
    }

    //This method deletes an order from the ETA tree. It's called when an order is canceled to remove it from the ETA tree.
    static ETATree.Node deleteOrderFromETATree(ETATree.Node node, int orderId) {
        if (node == null) return null;
    
        if (node.avlNode.order.orderId == orderId) {
            node.eta = Integer.MAX_VALUE; // Mark this node as canceled
        }
    
        if (node.avlNode.order.orderId < orderId) {
            node.left = deleteOrderFromETATree(node.left, orderId);
        } else {
            node.right = deleteOrderFromETATree(node.right, orderId);
        }
    
        return node;
    }


   //This method cancels an order and updates the ETA of affected orders. It's called when the "cancelOrder" command is processed.
   static void cancelOrder(int orderId, int currentSystemTime) {
        // Search for the order in the AVL tree
        Order orderToCancel = searchOrder(orderId);
    
        if (orderToCancel != null) {
            // Check if the order has already been delivered
            int eta = calculateETA(orderToCancel);
            if (eta <= currentSystemTime) {
                writeOutput("Cannot cancel. Order " + orderId + " has already been delivered.");
                return;
            }
    
            // Cancel the order
            priorityTree.root = deleteNode(priorityTree.root, orderId);
            writeOutput("Order " + orderId + " has been canceled.");
    
            // Update the ETAs of all orders with lower priority
            updateETAsWithLowerPriority(orderId, currentSystemTime);
    
            // Print the updated ETAs
            printUpdatedETAs(currentSystemTime);
        } else {
            writeOutput("Order " + orderId + " not found.");
        }
    }


    //This method deletes a node (order) from the AVL tree. It's called when an order is canceled or delivered. 
    static AVLTree.Node deleteNode(AVLTree.Node root, int orderId) {
        if (root == null) return root;

        if (orderId < root.order.orderId)
            root.left = deleteNode(root.left, orderId);
        else if (orderId > root.order.orderId)
            root.right = deleteNode(root.right, orderId);
        else {
            if (root.left == null || root.right == null) {
                AVLTree.Node temp = null;
                if (temp == root.left)
                    temp = root.right;
                else
                    temp = root.left;

                if (temp == null) {
                    temp = root;
                    root = null;
                } else
                    root = temp;
            } else {
                AVLTree.Node temp = minValueNode(root.right);
                root.order = temp.order;
                root.right = deleteNode(root.right, temp.order.orderId);
            }
        }

        if (root == null) return root;

        root.height = Math.max(priorityTree.height(root.left), priorityTree.height(root.right)) + 1;

        int balance = getBalance(root);

        if (balance > 1 && getBalance(root.left) >= 0)
            return rightRotate(root);

        if (balance > 1 && getBalance(root.left) < 0) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }

        if (balance < -1 && getBalance(root.right) <= 0)
            return leftRotate(root);

        if (balance < -1 && getBalance(root.right) > 0) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }

        return root;
    }


    
    static AVLTree.Node minValueNode(AVLTree.Node node) {
        AVLTree.Node current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }


    static int getBalance(AVLTree.Node node) {
        if (node == null)
            return 0;
        return priorityTree.height(node.left) - priorityTree.height(node.right);
    }

    static AVLTree.Node rightRotate(AVLTree.Node y) {
        AVLTree.Node x = y.left;
        AVLTree.Node T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(priorityTree.height(y.left), priorityTree.height(y.right)) + 1;
        x.height = Math.max(priorityTree.height(x.left), priorityTree.height(x.right)) + 1;

        return x;
    }

    static AVLTree.Node leftRotate(AVLTree.Node x) {
        AVLTree.Node y = x.right;
        AVLTree.Node T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(priorityTree.height(x.left), priorityTree.height(x.right)) + 1;
        y.height = Math.max(priorityTree.height(y.left), priorityTree.height(y.right)) + 1;

        return y;
    }


    // This method updates the ETAs of orders with lower priority after an order is canceled. It's called when an order is canceled to adjust the ETAs of other orders in the ETA tree. 
    static void updateETAsWithLowerPriority(int orderId, int currentSystemTime) {
        // Traverse the ETA tree and update ETAs of orders with lower priority
        etaTree.root = updateETAsWithLowerPriority(etaTree.root, orderId, currentSystemTime);
    }

    //This method is a helper method for updateETAsWithLowerPriority(int orderId, int currentSystemTime). It recursively traverses the ETA tree to update ETAs of orders with lower priority.
    static ETATree.Node updateETAsWithLowerPriority(ETATree.Node node, int orderId, int currentSystemTime) {
        if (node == null) return null;

        if (node.avlNode.order.orderId == orderId) {
            node.eta = Integer.MAX_VALUE; // Mark this node as canceled
        }

        if (node.avlNode.order.orderId < orderId) {
            // Update ETA if the order is not canceled
            if (node.eta > currentSystemTime) {
                node.eta = currentSystemTime;
            }
            node.left = updateETAsWithLowerPriority(node.left, orderId, currentSystemTime);
        } else {
            node.right = updateETAsWithLowerPriority(node.right, orderId, currentSystemTime);
        }

        return node;
    }

    // This method updates the delivery time of a specific order and adjusts its ETA. It's called when the "updateTime" command is processed.
    static void updateTime(int orderId, int currentSystemTime, int newDeliveryTime) {
        // Retrieve order from AVL tree
        Order order = searchOrder(orderId);
    
        if (order != null) {
            // Check if the order has already been delivered
            int eta = calculateETA(order);
            if (eta <= currentSystemTime) {
                writeOutput("Cannot update. Order " + orderId + " has already been delivered.");
                return;
            }
    
            // Update delivery time
            order.deliveryTime = newDeliveryTime;
    
            // Update ETA in the AVL tree
            priorityTree.root = updatePriorityTree(priorityTree.root, order);
    
            // Update ETA in the ETA tree
            int newETA = calculateETA(order);
            etaTree.root = updateETATree(etaTree.root, eta, newETA, priorityTree.root);
    
            // Print the updated ETAs
            printUpdatedETAs(currentSystemTime);
        } else {
            writeOutput("Order " + orderId + " not found.");
        }
    }

    // This method updates the AVL tree with the new delivery time of an order. It's called when the delivery time of an order is updated.
    static AVLTree.Node updatePriorityTree(AVLTree.Node node, Order order) {
        if (node == null) return null;

        if (node.order.orderId == order.orderId) {
            node.order = order;
        }

        if (order.calculatePriority() < node.order.calculatePriority())
            node.left = updatePriorityTree(node.left, order);
        else
            node.right = updatePriorityTree(node.right, order);

        return node;
    }

    //This method updates the ETA tree with the new ETA of an order. It's called when the delivery time of an order is updated. 
    static ETATree.Node updateETATree(ETATree.Node node, int oldETA, int newETA, AVLTree.Node avlNode) {
        if (node == null) return null;

        if (node.eta == oldETA && node.avlNode == avlNode) {
            node.eta = newETA;
        }

        node.left = updateETATree(node.left, oldETA, newETA, avlNode);
        node.right = updateETATree(node.right, oldETA, newETA, avlNode);

        return node;
    }

    //This method calculates the estimated time of arrival (ETA) for a given order. It's called to determine the ETA of a newly created order and to update the ETA of affected orders.
    static int calculateETA(Order order) {
        int eta = order.currentSystemTime + order.deliveryTime;
        ETATree.Node node = etaTree.root;
        while (node != null) {
            if (order.calculatePriority() < node.avlNode.order.calculatePriority()) {
                eta = Math.max(eta, node.eta + node.avlNode.order.deliveryTime);
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return eta;
    }

    //This method searches for an order with the given order ID in the AVL tree. It's called to retrieve an order when processing commands. 
    static Order searchOrder(int orderId) {
        return searchOrder(priorityTree.root, orderId);
    }

    //This method is a helper method for searchOrder(int orderId). It recursively searches for an order with the given order ID in the AVL tree. 
    static Order searchOrder(AVLTree.Node node, int orderId) {
        if (node == null || node.order.orderId == orderId) {
            if (node != null) {
                //System.out.println("Debug: Found node with orderId " + node.order.orderId);
            } else {
                //System.out.println("Debug: Node is null");
            }
            return node != null ? node.order : null;
        }
    
        if (node.order.orderId < orderId) {
            //System.out.println("Debug: Going right from node " + node.order.orderId);
            return searchOrder(node.right, orderId);
        }
    
        //System.out.println("Debug: Going left from node " + node.order.orderId);
        return searchOrder(node.left, orderId);
    }



    //This method prints the updated ETAs of orders after an operation (create, cancel, or update). It's called to print the updated ETAs.
    static void printUpdatedETAs(int currentSystemTime) {
        List<Integer> updatedOrders = new ArrayList<>();
        printUpdatedETAs(etaTree.root, currentSystemTime, updatedOrders);

        if (!updatedOrders.isEmpty()) {
            System.out.print("Updated ETAs: ");
            for (Integer orderId : updatedOrders) {
                int eta = calculateETA(searchOrder(orderId));
                writeOutput("[" + orderId + ": " + eta + "], ");
            }
            
        }
    }


   //This method is a helper method for printUpdatedETAs(int currentSystemTime). It recursively traverses the ETA tree to find orders with updated ETAs.
    static void printUpdatedETAs(ETATree.Node node, int currentSystemTime, List<Integer> updatedOrders) {
        if (node != null) {
            if (node.eta <= currentSystemTime) {
                AVLTree.Node avlNode = node.avlNode;
                updatedOrders.add(avlNode.order.orderId);
            }
            printUpdatedETAs(node.left, currentSystemTime, updatedOrders);
            printUpdatedETAs(node.right, currentSystemTime, updatedOrders);
        }
    }
    static ETATree.Node minValueNode(ETATree.Node node) {
        ETATree.Node current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }

    //This method helps in writing the output file.
    static void writeOutput(String output) {
        if (writer != null) {
            writer.println(output);
        }
    }


}

