import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 *  Implements a GUI for inputting Graph
 *
 *  @author Mulangma "Isabella" Zhu
 *  @version CSC 212, 28 April 2017
 */
public class GraphGUI {
    /** The graph to be displayed */
    private GraphCanvas canvas;

    /** Label for the instructions */
    private JLabel instr;

    /** Button for add node */
    private JButton addNodeButton;

    /** Button for remove node */
    private JButton rmvNodeButton;

    /** Button for change text */
    private JButton chgTextButton;

    /** Button for change distance */
    private JButton chgDistButton;


    private JButton centroidDecomposeButton;

    /** Button for Refresh */
    private JButton rfButton;

    /** The input mode */
    private InputMode mode = InputMode.ADD_NODES;

    /** Remembers node where last mousedown event occurred */
    private Tree<NodeData,EdgeData>.Node nodeUnderMouse;

    /**
     *  Schedules a job for the event dispatching thread
     *  creating and showing this application's GUI.
     */
    public static void main(String[] args) {
        final GraphGUI GUI = new GraphGUI();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUI.createAndShowGUI();
            }
        });
    }

    /** Sets up the GUI window */
    public void createAndShowGUI() {
        // Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        JFrame frame = new JFrame("Graph GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add components
        createComponents(frame);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /** Puts content in the GUI window */
    public void createComponents(JFrame frame) {
        // graph display
        Container pane = frame.getContentPane();
        pane.setLayout(new FlowLayout());
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        canvas = new GraphCanvas();
        GraphMouseListener gml = new GraphMouseListener();
        canvas.addMouseListener(gml);
        canvas.addMouseMotionListener(gml);
        panel1.add(canvas);
        instr = new JLabel("Click to add new nodes; drag to move.");
        panel1.add(instr,BorderLayout.NORTH);
        pane.add(panel1);

        // build graph buttons
        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(6,1));

        addNodeButton = new JButton("Add/Move Nodes");
        panel2.add(addNodeButton);
        addNodeButton.addActionListener(new AddNodeListener());

        rmvNodeButton = new JButton("Remove Nodes");
        panel2.add(rmvNodeButton);
        rmvNodeButton.addActionListener(new RmvNodeListener());

        chgTextButton = new JButton("Change Text");
        panel2.add(chgTextButton);
        chgTextButton.addActionListener(new ChgTextListener());

        chgDistButton = new JButton("Change Distance");
        panel2.add(chgDistButton);
        chgDistButton.addActionListener(new ChgDistListener());

        pane.add(panel2);

        // traversal buttons
        JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayout(1,1));

        centroidDecomposeButton = new JButton("Centroid Decomposition");
        panel3.add(centroidDecomposeButton);
        centroidDecomposeButton.addActionListener(new CentroidDecomposeListener());

        rfButton = new JButton("Refresh");
        panel3.add(rfButton);
        rfButton.addActionListener(new RFListener());
        pane.add(panel3);
    }

    /**
     * Returns a node found within the drawing radius of the given location,
     * or null if none
     *
     *  @param x  the x coordinate of the location
     *  @param y  the y coordinate of the location
     *  @return  a point from the canvas if there is one covering this location,
     *  or a null reference if not
     */
    @SuppressWarnings("unchecked")

    public Tree<NodeData,EdgeData>.Node findClosestNode(int x, int y) {
        Tree.Node nearbyNode = null;
        for (Tree<NodeData,EdgeData>.Node node : canvas.tree.getNodes()){
            Point p = node.getData().getPosition();
            if (p.distance(x,y)<=40){
                nearbyNode = node;
            }
        }
        return nearbyNode;
    }
    public Tree<NodeData,EdgeData>.Node findNearestParent(int x, int y) {
        Tree.Node closestParent = null;

        Point p = new Point(10000,10000);
        double minDistance = p.distance(x,y);

        for (Tree<NodeData,EdgeData>.Node node : canvas.tree.getNodes()){
            p = node.getData().getPosition() ;

            if (p.distance(x,y)<=minDistance){

                minDistance = p.distance(x,y);
                closestParent = node;
            }
        }
        return closestParent;
    }

    /** Constants for recording the input mode */
    enum InputMode {
        ADD_NODES, RMV_NODES, CHG_TEXT, CHG_DIST,
    }

    /** Listener for Add Node button */
    private class AddNodeListener implements ActionListener {
        /** Event handler for AddPoint button */
        public void actionPerformed(ActionEvent e) {
            mode = InputMode.ADD_NODES;
            instr.setText("Click to add new nodes or change their location.");
        }
    }

    /** Listener for Remove Node button */
    private class RmvNodeListener implements ActionListener {
        /** Event handler for RmvPoint button */
        public void actionPerformed(ActionEvent e) {
            mode = InputMode.RMV_NODES;
            instr.setText("Drag on nodes to remove them.");
        }
    }

    /** Listener for Change Text button */
    private class ChgTextListener implements ActionListener {
        /** Event handler for Chg button */
        public void actionPerformed(ActionEvent e) {
            mode = InputMode.CHG_TEXT;
            instr.setText("Click one node to change the text on the node.");
        }
    }

    /** Listener for Change Distance button */
    private class ChgDistListener implements ActionListener {
        /** Event handler for Chg button */
        public void actionPerformed(ActionEvent e) {
            mode = InputMode.CHG_DIST;
            instr.setText("Drag from one node to another to change the distance on the edge.");
        }
    }

    private class CentroidDecomposeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.startCentroidDecomposition();
            instr.setText("Visualizing centroid decomposition...");
        }
    }


    /** Listener for Refresh Path button */
    private class RFListener implements ActionListener {
        /** Event handler for Shortest Path button */
        public void actionPerformed(ActionEvent e) {
            canvas.refresh();
            addNodeButton.setEnabled(true);
            rmvNodeButton.setEnabled(true);

            chgTextButton.setEnabled(true);
            chgDistButton.setEnabled(true);

            instr.setText("Try functions by clicking buttons.");
        }
    }

    /** Mouse listener for GraphCanvas element */
    private class GraphMouseListener extends MouseAdapter
            implements MouseMotionListener {
        /** Responds to click event depending on mode */
        @SuppressWarnings("unchecked")
        public void mouseClicked(MouseEvent e) {
            Tree<NodeData,EdgeData>.Node closestNode = findClosestNode(e.getX(),e.getY());
            Tree<NodeData,EdgeData>.Node closestParent = findNearestParent(e.getX(),e.getY());
            boolean work = false;
            switch (mode) {
                case ADD_NODES:
                    if (closestNode==null){
                            char c = (char)(canvas.tree.numNodes()%26+65);
                            if (!canvas.tree.getNodes().isEmpty()){
                                canvas.tree.addNode((new NodeData(e.getPoint(),Character.toString(c))));
                                //canvas.repaint();
                                Tree<NodeData,EdgeData>.Node thisNode = findClosestNode(e.getX(),e.getY());
                                //Tree<NodeData,EdgeData>.Node nearbyNode = findClosestNode(e.getX(),e.getY());
                                canvas.tree.addEdge((new EdgeData(-1.0)),closestParent, thisNode);
                                //canvas.tree.addEdge((new EdgeData(-1.0)),thisNode, closestParent);
                                work = true;
                            } else{
                                canvas.tree.addNode(new NodeData(e.getPoint(),Character.toString(c)));
                                work = true;
                            }
                            canvas.repaint();
                        }
                    if (!work){
                        Toolkit.getDefaultToolkit().beep();
                    }
                    break;
                case RMV_NODES:
                    if (closestNode!=null){
                        canvas.tree.removeNode(closestNode);
                        canvas.repaint();
                        work = true;
                    }
                    if (!work){
                        Toolkit.getDefaultToolkit().beep();
                    }
                    break;
                case CHG_TEXT:
                    if (closestNode!=null) {
                        while (!work) {
                            try {
                                JFrame frame = new JFrame("Enter a text");
                                String text = JOptionPane.showInputDialog(frame, "Please enter the text on this node.");
                                if (text != null) {
                                    closestNode.getData().setText(text);
                                    canvas.repaint();
                                    work = true;
                                } else {
                                    Toolkit.getDefaultToolkit().beep();
                                }
                            } catch (Exception exception){
                                //do nothing
                            }
                        }
                    }
            }
        }

        /**
         * Records point under mousedown event in anticipation of possible drag
         * Record point under mouse, if any
         */
        public void mousePressed(MouseEvent e) {
            nodeUnderMouse = findClosestNode(e.getX(),e.getY());
        }

        /**
         * Responds to mouseup event
         * Clear record of point under mouse, if any
         */
        @SuppressWarnings("unchecked")
        public void mouseReleased(MouseEvent e) {
            Tree<NodeData,EdgeData>.Node closestNode = findClosestNode(e.getX(),e.getY());
            Tree<NodeData,EdgeData>.Node closestParent = findNearestParent(e.getX(),e.getY());
            boolean work = false;
            switch (mode) {

                case CHG_DIST:
                    //Tree<NodeData,EdgeData>.Node thisNode = findClosestNode(e.getX(),e.getY());
                    if (closestNode != null && nodeUnderMouse !=null){
                        System.out.println("Node under mouse: " + nodeUnderMouse.getData().getText());
                        System.out.println("Closest node: : " + closestNode.getData().getText());
                        Tree<NodeData,EdgeData>.Edge edge = nodeUnderMouse.edgeTo(closestNode);
                        System.out.println("Edge in quesion: " + edge.getData().getDistance());
                        if (edge != null) {
                            while (!work) {
                                try {
                                    JFrame frame = new JFrame("Enter a distance");
                                    String distance = JOptionPane.showInputDialog(frame, "Please enter the distance represented by this edge.");
                                    edge.getData().setDistance(Double.valueOf(distance));
                                    canvas.repaint();
                                    work = true;
                                } catch (Exception exception) {
                                    //do nothing
                                }
                            }
                        }
                    }
                    if (!work) {
                        Toolkit.getDefaultToolkit().beep();
                    }
            }
        }

        /** Responds to mouse drag event */
        @SuppressWarnings("unchecked")
        public void mouseDragged(MouseEvent e) {
            // test if the mouse drags on a node, and make sure the node is in the displaying area(r=40)
            if(mode == InputMode.ADD_NODES && nodeUnderMouse != null
                    && e.getX()>=40 && e.getY()>=40
                    && e.getX()<=1460 && e.getY()<=860) {
                nodeUnderMouse.getData().setPosition(e.getPoint());
                canvas.repaint();
            }
        }
        // Empty but necessary to comply with MouseMotionListener interface.
        public void mouseMoved(MouseEvent e) {
            nodeUnderMouse = null;
        }
    }

    /** Worker class for doing traversals */
    private class TraversalThread extends SwingWorker<Boolean, Object> {


        @Override
        public Boolean doInBackground() {
            addNodeButton.setEnabled(false);
            rmvNodeButton.setEnabled(false);
            chgTextButton.setEnabled(false);
            chgDistButton.setEnabled(false);
            centroidDecomposeButton.setEnabled(false);
            rfButton.setEnabled(false);
            //return canvas.paintTraversal(path);
            return false;
        }

        @Override
        protected void done() {
//            try {
//                if (path.isEmpty() && path != null) {  // test the result of doInBackground()
//                    instr.setText("There is no path. Please refresh.");
//                }
//                rfButton.setEnabled(true);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

}
