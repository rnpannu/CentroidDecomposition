import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;


 //Implements a graphical canvas that displays a graph.
public class GraphCanvas extends JComponent {
    /** The Graph */

    public Tree<NodeData,EdgeData> tree;

    /** Constructor */
    public GraphCanvas() {
        tree = new Tree<NodeData, EdgeData>();
    }

    /**
     *  Paints a blue circle thirty pixels in diameter at each node
     *  and a blue line for each edge.
     *  @param g The graphics object to draw with
     */


    public void paintComponent(Graphics g){
        if (tree != null) {
            for (Tree<NodeData, EdgeData>.Edge edge : tree.getEdges()) {
                g.setColor(edge.getData().getColor());
                Tree<NodeData, EdgeData>.Node sigma = edge.getNode();
                Point s = sigma.getData().getPosition();
                Point r = edge.getOtherNode(sigma).getData().getPosition();
                double dist = paintArrowLine(g, (int)s.getX(), (int)s.getY(), (int)r.getX(), (int)r.getY(), 20, 10, edge.getData().getDistance());
                if (edge.getData().getDistance()==-1) {
                    edge.getData().setDistance(dist);
                }
            }
            for (Tree<NodeData,EdgeData>.Node node : tree.getNodes()) {
                Point q = node.getData().getPosition();
                g.setColor(node.getData().getColor());
                g.fillOval((int) q.getX()-40, (int) q.getY()-40, 80, 80);
                //paint text
                g.setColor(Color.white);
                g.drawString(node.getData().getText(),(int)q.getX()-5,(int)q.getY()+5);
            }
        }

    }

    /**
     * Draw an arrow line between two points, revised from the code of @phibao37
     * http://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
     * @param x1 x-position of first point
     * @param y1 y-position of first point
     * @param x2 x-position of second point
     * @param y2 y-position of second point
     * @param d  the width of the arrow
     * @param h  the height of the arrow
     * @return the distance between two points
     */
    private double paintArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h,Double dist){
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double sin = dy/D, cos = dx/D;

        //change the end point of the line, r = 40
//        x2 = (int)(x2 - 40 * cos);
//        y2 = (int)(y2 - 40 * sin);
        x2 = (int)(x2 - 40 * cos);
        y2 = (int)(y2 - 40 * sin);
        D = D - 40; // change the length of the line
        double xm = D - d, xn = xm, ym = h, yn = -h, x;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
        // paint the distance
        if (dist == -1.0) {
            // the actual distance(D+40) between two points next to the tail (D=the length of the arrow)
            dist = Math.round((D + 40) * 100) / 100.00;
        }
        g.drawString(Double.toString(dist),(int) (xm - 30 * cos), (int) (ym - 30 * sin));
        return dist;
    }

    public Tree<NodeData,EdgeData> getCentroidDecomposition() {
        // Create a new tree to represent the centroid decomposition
        Tree<NodeData,EdgeData> centroidTree = new Tree<NodeData,EdgeData>();

        // If the tree is empty, return an empty centroid tree
        if (tree.getNodes().isEmpty()) {
            return centroidTree;
        }

        // Mark which nodes have been used as centroids
        ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked = new ArrayList<>();

        // Map to keep track of original nodes to centroid nodes
        Map<Tree<NodeData,EdgeData>.Node, Tree<NodeData,EdgeData>.Node> centroidMapping = new HashMap<>();


        // Start centroid decomposition from the first node
        getCentroidRecursive(tree.getNodes().get(0), centroidTree, centroidMarked, centroidMapping);

        return centroidTree;
    }
    private Tree<NodeData,EdgeData>.Node getCentroidRecursive(Tree<NodeData,EdgeData>.Node currentNode, Tree<NodeData,EdgeData> centroidTree,
                                           ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked,
                                           Map<Tree<NodeData,EdgeData>.Node, Tree<NodeData,EdgeData>.Node> centroidMapping){
        // Find the centroid of the current subtree
        System.out.println("Prospective node: " + currentNode.getData().getText());
        (currentNode.getData()).setColor(Color.ORANGE);
        repaint();
        long sleepTime = 500;
        long start = System.currentTimeMillis();
        while (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
                break;
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
                // Calculate remaining sleep time
                sleepTime -= (System.currentTimeMillis() - start);
            }
        }


        Tree<NodeData,EdgeData>.Node centroid = findCentroid(currentNode, centroidMarked);
        //(centroid.getData()).setColor(Color.GREEN);
        //Thread.sleep(500);
        //repaint();
        // Mark this node as a centroid
        centroidMarked.add(centroid);

        // Create a centroid node in the new tree

        Tree<NodeData,EdgeData>.Node centroidTreeNode = centroidTree.addNode(centroid.getData());
        centroidMapping.put(centroid, centroidTreeNode);

        // Find the connected components after removing the centroid
        List<Tree<NodeData,EdgeData>.Node> components = findComponents(centroid, centroidMarked);

        // Recursively decompose each component
        for (Tree.Node component : components) {
            if (!centroidMarked.contains(component)) {
                Tree.Node componentCentroid = getCentroidRecursive(component, centroidTree,
                        centroidMarked, centroidMapping);

                // Add an edge between the current centroid and the component's centroid
                if (componentCentroid != null) {
                    centroidTree.addEdge((new EdgeData(1.0)),
                            centroidMapping.get(centroid),
                            centroidMapping.get(componentCentroid));
                }
            }
        }

        return centroid;
    }

    private Tree<NodeData,EdgeData>.Node findCentroid(Tree<NodeData,EdgeData>.Node root, ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
        int[] totalSize = {0};
        Map<Tree<NodeData,EdgeData>.Node, Integer> subtreeSize = new HashMap<>();

        // First DFS to calculate subtree sizes
        System.out.println("Subtree rooted at: " + root.getData().getText());
        calculateSubtreeSizes(root, null, subtreeSize, totalSize, centroidMarked);

        // Second DFS to find the centroid
        return findCentroidHelper(root, null, subtreeSize, totalSize[0], centroidMarked);
    }

    private void calculateSubtreeSizes(Tree<NodeData,EdgeData>.Node current, Tree<NodeData,EdgeData>.Node parent,
                                       Map<Tree<NodeData,EdgeData>.Node, Integer> subtreeSize,
                                       int[] totalSize,
                                       ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
        if (centroidMarked.contains(current)) return;
        System.out.println("Marked centroids: ");
        for (Tree<NodeData,EdgeData>.Node node : centroidMarked) {
            System.out.print(node.getData().getText() + ", ");
        }
        System.out.println("");
        totalSize[0]++;
        subtreeSize.put(current, 1);
        System.out.println("Subtree size of tree: " + totalSize[0]);
        for (Tree<NodeData,EdgeData>.Node neighbor : current.getNeighbors()) {
            if (neighbor != parent && !centroidMarked.contains(neighbor)) {

                calculateSubtreeSizes(neighbor, current, subtreeSize, totalSize, centroidMarked);
                subtreeSize.put(current, subtreeSize.get(current) + subtreeSize.get(neighbor));
            }
        }
    }

    private Tree<NodeData,EdgeData>.Node findCentroidHelper(Tree<NodeData,EdgeData>.Node current, Tree<NodeData,EdgeData>.Node parent,
                                         Map<Tree<NodeData,EdgeData>.Node, Integer> subtreeSize,
                                         int totalSize,
                                         ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
        boolean isCentroid = true;
        Tree<NodeData,EdgeData>.Node heaviestChild = null;
        int maxChildSize = 0;

        for (Tree.Node neighbor : current.getNeighbors()) {
            if (neighbor != parent && !centroidMarked.contains(neighbor)) {
                // Check if any subtree is too large
                if (subtreeSize.get(neighbor) > totalSize / 2) {
                    isCentroid = false;
                }

                // Find the heaviest child
                if (heaviestChild == null || subtreeSize.get(neighbor) > maxChildSize) {
                    heaviestChild = neighbor;
                    maxChildSize = subtreeSize.get(neighbor);
                }
            }
        }

        // Check if current node is a valid centroid
        int remainingSize = totalSize - subtreeSize.get(current);
        if (isCentroid && remainingSize <= totalSize / 2) {
            return current;
        }

        // If not, recursively find centroid in the heaviest child
        return findCentroidHelper(heaviestChild, current, subtreeSize, totalSize, centroidMarked);
    }

    private List<Tree<NodeData,EdgeData>.Node> findComponents(Tree<NodeData,EdgeData>.Node centroid, ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
        List<Tree<NodeData,EdgeData>.Node> components = new ArrayList<>();
        System.out.println("Component rooted at centroid: " + centroid.getData().getText());
        for (Tree<NodeData,EdgeData>.Node neighbor : centroid.getNeighbors()) {
            if (!centroidMarked.contains(neighbor)) {
                System.out.println("Component Node : " + neighbor.getData().getText());
                components.add(neighbor);
            }
        }

        return components;
    }
    public void startCentroidDecomposition() {
        if (tree != null && !tree.getNodes().isEmpty()) {
            System.out.println("Starting CentroidDecomposition");
            new DecompositionWorker(getCentroidDecomposition()).execute();
        } else {
            System.out.println("Tree is empty");
            //instr.setText("Tree is empty or invalid.");
        }
    }

    private class DecompositionWorker extends SwingWorker<Void, Tree<NodeData, EdgeData>.Node> {
        private final Tree<NodeData, EdgeData> decompositionTree;

        public DecompositionWorker(Tree<NodeData, EdgeData> tree) {
            this.decompositionTree = tree;
            System.out.println("Got to worker");
        }

        @Override
        protected Void doInBackground() {
            for (Tree<NodeData, EdgeData>.Node centroid : decompositionTree.getNodes()) {
                publish(centroid);
                try {
                    Thread.sleep(1000); // Pause for animation effect
                } catch (InterruptedException ignored) {}
            }
            return null;
        }

        protected void process(ArrayList<Tree<NodeData, EdgeData>.Node> centroids) {
            Tree<NodeData, EdgeData>.Node lastCentroid = centroids.get(centroids.size() - 1);
            highlightNode(lastCentroid); // Custom method to highlight a node
            repaint();
        }

        @Override
        protected void done() {
            System.out.println("Complete");
        }
    }
    private void highlightNode(Tree<NodeData, EdgeData>.Node node) {
        for (Tree<NodeData, EdgeData>.Edge edge : node.getEdges()) {
            edge.getData().setColor(Color.RED); // Highlight subtree connections
        }
        node.getData().setColor(Color.GREEN); // Highlight centroid
    }

    /**
     * Paint the traversal path to red
     * @param path the list of edges in the traversal path
     * @return whether there is a traversal to paint or not

    public Boolean paintTraversal(LinkedList<Graph<NodeData,EdgeData>.Edge> path){
        boolean painting;
        if (path.isEmpty()){
            painting = false;
            return painting;
        } else {
            painting = true;
        }
        // set every thing to gray, and set the start point to orange
        for (Graph<NodeData,EdgeData>.Edge edge:graph.getEdge()){
            edge.getData().setColor(Color.lightGray);
        }
        for (Graph<NodeData,EdgeData>.Node node:graph.getNode()){
            node.getData().setColor(Color.lightGray);
        }
        path.get(0).getHead().getData().setColor(Color.ORANGE);
        repaint();

        for (Graph<NodeData,EdgeData>.Edge edge:path){
            try {
            Thread.sleep(500);
            } catch (InterruptedException ignore) {
            }
            // Color the current edge
            edge.getData().setColor(new Color(8,83,109));
            edge.getTail().getData().setColor(Color.ORANGE);
            repaint();
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignore) {
            }
            edge.getTail().getData().setColor(new Color(8,83,109));
            repaint();
        }
        return painting;
    }
     */
    /**
     * Repaint every thing to the default color
     */
    public void refresh(){
        if (tree != null) {
            for (Tree<NodeData,EdgeData>.Edge edge:tree.getEdges()){
                edge.getData().setColor(new Color(8,83,109));
            }
            for (Tree<NodeData,EdgeData>.Node node:tree.getNodes()){
                node.getData().setColor(new Color(8,83,109));
            }
        }

        repaint();
    }

    /**
     *  The component will look bad if it is sized smaller than this
     *
     *  @returns The minimum dimension
     */
    public Dimension getMinimumSize() {
        return new Dimension(1500,9000);
    }

    /**
     *  The component will look best at this size
     *
     *  @returns The preferred dimension
     */
    public Dimension getPreferredSize() {
        return new Dimension(1500,900);
    }
}
