import java.util.HashSet;
import java.util.LinkedList;

public class Tree<V,E> extends Graph<V,E> {
    public Tree(){
        super();
    }

    public Node addRoot(V data){
        if (!getNode().isEmpty()){
            throw new IllegalStateException("Tree already has a root.");
        }
        return addNode(data);
    }
    public Node addChild(Node parent, V childData){
        Node childNode = addNode(childData);
        addEdge(null, parent, childNode);
        return childNode;
    }
    public boolean isAcyclic(){
        int rootCount = 0;
        Node root = null;

        // Simple case - check for single root
        for (Node node : getNode()){
            if (node.getEdgeIn().isEmpty()){
                rootCount++;
                root = node;
            }
            // If a node has more than one parent
            if (node.getEdgeIn().size() > 1){
                return false;
            }
        }

        if (rootCount != 1){
            return false;
        }

        // Complex case - cycle check using DFS
        HashSet<Node> visited = new HashSet<>();
        try{
            dfsHelper(root, null, visited)
        } catch (IllegalStateException e){
            return false;
        }

        return visited.size() == numNodes(); // Have traversed tree with no cycles
    }
    private void dfsHelper(Node current, Node parent, HashSet<Node> visited){
        if (visited.contains(current)){
            throw new IllegalStateException("Graph contains a cycle.");
        }

        visited.add(current); // Not a cycle so far
        try {
            for (Edge edge : current.getEdgeOut()){
                Node child = edge.getTail();
                if (child != parent) {
                    dfsHelper(child, current, visited);
                }
            }
        }
    }

    public int getNodeDepth(Node node){
        int depth = 0;
        Node current = node;

        while(!current.getEdgeIn().isEmpty()){// While not the root node
            depth++; // go to next level and increment depth
            current = current.getEdgeIn().get(0).getHead(); // Head I think, for going up towards the root
        }

        return depth;
    }

    public int getTreeHeight(){
        if (numNodes() == 0){
            return -1;
        }
        Node root = null;
        for (Node node : getNode()){
            if (node.getEdgeIn().isEmpty()){
                root = node;
                break;
            }
        }
        return findMaxDepth(root);
    }
    // Likely not correct
    private int findMaxDepth(Node node){
        if(node.getEdgeOut().isEmpty()){// No more children, base case
            return 0;
        }

        int maxChildDepth = 0;
        for (Edge edge : node.getEdgeOut()){
            int childDepth = findMaxDepth(edge.getTail());
            maxChildDepth = Math.max(maxChildDepth, childDepth);
        }
        return maxChildDepth + 1;
    }

    public LinkedList<Node> getLeafNodes() {
        LinkedList<Node> leaves = new LinkedList<>();
        for (Node node : getNode()) {
            if (node.getEdgeOut().isEmpty()) {
                leaves.add(node);
            }
        }
        return leaves;
    }


    public Node getParent(Node node) {
        if (node.getEdgeIn().isEmpty()) {
            return null;
        }
        return node.getEdgeIn().get(0).getHead();
    }

    public LinkedList<Node> getChildren(Node node) {
        LinkedList<Node> children = new LinkedList<>();
        for (Edge edge : node.getEdgeOut()) {
            children.add(edge.getTail());
        }
        return children;
    }
}
