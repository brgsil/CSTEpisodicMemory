package CSTEpisodicMemory.util.visualization;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.representation.idea.Idea;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class GraphIdeaVisualizer extends JFrame {

    private static final double div = 50;
    protected static final double FORCE_CENTER = 1.1 / div;
    protected static final double REPEL_FORCE = 200 / div;
    protected static final double LINK_FORCE = 0.01 / div;
    protected static final double DRAG_COEF = 2 / div;
    protected static final double MAX_DIST = 100;
    protected static final double MAX_VEL = 5;

    private int width, heigth;
    private GraphIdea graphIdea;
    private JComponent display;
    private Graph gg = new Graph();
    private boolean physics = true;
    private Node selected = null;
    protected String[] types = new String[]{"Event", "Location", "Episode", "Context", "Property"};
    protected List<String> selections = new ArrayList<>(Arrays.asList(types));

    public GraphIdeaVisualizer(int width, int heigth, GraphIdea graph) {
        this.width = width;
        this.heigth = heigth;
        this.graphIdea = graph;

        initComponents();

        java.util.Timer t = new Timer();
        GraphIdeaVisualizer.mainTimerTask tt = new GraphIdeaVisualizer.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 500, 50);

        setVisible(true);
    }

    class mainTimerTask extends TimerTask {

        GraphIdeaVisualizer l;

        public mainTimerTask(GraphIdeaVisualizer ll) {
            l = ll;
        }

        public void run() {
            l.repaint();
        }
    }

    private void initComponents(){
        setSize(width,heigth);

        JToolBar toolBar = new JToolBar();
        JToggleButton button = new JToggleButton("Physics");
        button.setSelected(true);
        button.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                physics = itemEvent.getStateChange() == ItemEvent.SELECTED;
            }
        });
        toolBar.add(button);

        for (int i = 0; i<5;i++){
            JCheckBox cBox = new JCheckBox(types[i]);
            cBox.setSelected(true);
            int finalI = i;
            cBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                        selections.add(types[finalI]);
                    else
                        selections.remove(types[finalI]);
                }
            });
            toolBar.add(cBox);
        }

        getContentPane().add(toolBar, BorderLayout.NORTH);
        JComponent display = new JComponent() {


            @Override
            protected void paintComponent(Graphics g) {
                g.setFont(new Font("Roboto", Font.BOLD, 14));
                updtateGraph();
                g.translate(width/2, heigth/2);
                gg.draw((Graphics2D) g);
            }


            private void updtateGraph(){
                for (Idea node : graphIdea.getNodes()){
                    String nodeName = GraphIdea.getNodeContent(node).getName();
                        if (!gg.hasNode(nodeName)) {
                            gg.insertNode(nodeName, (String) node.get("Type").getValue());
                            return;
                        }
                        Map<String, List<Idea>> links = graphIdea.getSuccesors(node);
                        for (List<Idea> linkedNodes : links.values()) {
                            for (Idea nodeB : linkedNodes) {
                                    String nodeBName = GraphIdea.getNodeContent(nodeB).getName();
                                    if (!gg.hasNode(nodeBName)) {
                                        gg.insertNode(nodeBName, (String) nodeB.get("Type").getValue());
                                        return;
                                    }
                                    if (!gg.hasLink(nodeName, nodeBName)) {
                                        gg.insertLink(nodeName, nodeBName);
                                        return;
                                    }
                            }
                        }
                }
            }
        };

        display.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e){
                processPos(e.getX(), e.getY());
            }
        });

        display.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Release");
                if (selected != null) {
                    selected.selected = false;
                    selected = null;
                }
            }
        });

        add(display);
    }

    protected void processPos(double x, double y){
        ArrayRealVector test = new ArrayRealVector(new double[]{x-width/2,y-heigth/2});
        Node newSelected = gg.getSelectedNode(test);
        if (selected == null && newSelected != null){
            selected = newSelected;
            selected.pos = test;
            selected.selected = true;
        } else {
            selected.pos = test;
        }
    }

    public class Node{
        protected ArrayRealVector pos;
        protected String name;
        protected String type;
        protected ArrayRealVector force;
        protected ArrayRealVector vel;
        protected boolean selected = false;

        private static final Map<String, Color> COLORS = new HashMap<>(){
            {
                put("Event", Color.ORANGE);
                put("Location", Color.GREEN);
                put("Episode", Color.RED);
                put("Context", Color.MAGENTA);
                put("Property", Color.BLUE);
            }
        };

        public Node(String name, String type) {
            Random rnd = new Random();
            int mult = 1;
            this.pos = new ArrayRealVector(new double[]{rnd.nextDouble()*width*mult-width*mult/2f, rnd.nextDouble()*heigth*mult-heigth*mult/2f});
            this.name = name;
            this.type = type;
            this.force = new ArrayRealVector(new double[]{0,0});
            this.vel = new ArrayRealVector(new double[]{0,0});
        }

        protected void draw(Graphics2D g){
            //this.updatePos();
            g.setColor(COLORS.get(type));
            int size = 25;
            Ellipse2D.Double circle = new Ellipse2D.Double(pos.getEntry(0) - size/2f,
                    pos.getEntry(1)-size/2f,
                    size, size);
            g.fill(circle);

            FontMetrics metrics = g.getFontMetrics();
            int x = (int) this.pos.getEntry(0) - metrics.stringWidth(this.name) / 2;
            int y = (int) this.pos.getEntry(1) -  metrics.getHeight() / 2 + size / 2 + 20;

            g.setColor(Color.BLACK);
            g.drawString(this.name, x, y);
        }

        protected void updatePos(){
            if (!selected) {
                RealVector drag = this.vel.mapMultiply(DRAG_COEF);
                this.force = this.force.subtract(drag);
                this.vel = this.vel.add(this.force);
                if (this.vel.getNorm() > MAX_VEL) {
                    this.vel = (ArrayRealVector) this.vel.mapDivide(this.vel.getNorm()).mapMultiply(MAX_VEL);
                }
                this.pos = this.pos.add(this.vel);
            }
        }
    }

    protected class Link{
        protected Node a, b;

        public Link(Node a, Node b) {
            this.a = a;
            this.b = b;
        }

        protected void draw(Graphics2D g){
            Line2D.Double line = new Line2D.Double(a.pos.getEntry(0), a.pos.getEntry(1),
                    b.pos.getEntry(0), b.pos.getEntry(1));
            g.setColor(Color.BLACK);
            g.draw(line);
        }
    }

    protected class Graph{
        private Map<String, Node> nodes = new HashMap<>();
        private List<Link> links = new ArrayList<>();

        protected void insertNode(String name, String type){
            this.nodes.put(name, new Node(name, type));
        }

        protected void insertLink(String na, String nb){
            this.links.add(new Link(
                    this.nodes.get(na),
                    this.nodes.get(nb)
            ));
        }

        protected void draw(Graphics2D g){
            if (physics) {
                this.updateForces();
                this.nodes.forEach((n, node) -> {
                    if (selections.contains(node.type))
                        node.updatePos();
                });
            }
            this.links.forEach(l-> {
                if (selections.contains(l.a.type) && selections.contains(l.b.type))
                    l.draw(g);
            });
            this.nodes.forEach((n,node)-> {
                if (selections.contains(node.type))
                    node.draw(g);
            });
        }

        private void updateForces(){
            List<Node> otherNodes = new ArrayList<>(this.nodes.values());
            for (Node nodeA : this.nodes.values()){
                if (selections.contains(nodeA.type)) {
                    ArrayRealVector centerAttraction = (ArrayRealVector) nodeA.pos.copy().mapMultiply(-1).mapDivide(nodeA.pos.getNorm()).mapMultiply(FORCE_CENTER);
                    nodeA.force = centerAttraction;
                    otherNodes.remove(nodeA);

                    for (Node nodeB : otherNodes) {
                        if (selections.contains(nodeB.type)) {
                            ArrayRealVector dir = (ArrayRealVector) nodeB.pos.copy().subtract(nodeA.pos);
                            if (dir.getNorm() < 2 * MAX_DIST) {
                                ArrayRealVector force = (ArrayRealVector) dir.mapDivide(dir.getNorm() * dir.getNorm());
                                force = (ArrayRealVector) force.mapMultiply(REPEL_FORCE);
                                nodeA.force = nodeA.force.subtract(force);
                                nodeB.force = nodeB.force.add(force);
                            }
                        }
                    }

                    for (Link l : this.links) {
                        if (selections.contains(l.a.type) && selections.contains(l.b.type)) {
                            ArrayRealVector dis = l.a.pos.copy().subtract(l.b.pos);
                            double diff = dis.getNorm() - MAX_DIST;
                            dis = (ArrayRealVector) dis.mapDivide(dis.getNorm()).mapMultiply(diff).mapMultiply(LINK_FORCE);
                            l.a.force = l.a.force.subtract(dis);
                            l.b.force = l.b.force.add(dis);
                        }
                    }
                }
            }
        }

        protected boolean hasNode(String name){
            return this.nodes.containsKey(name);
        }

        protected boolean hasLink(String na, String nb){
            Node NA = this.nodes.get(na);
            Node NB = this.nodes.get(nb);
            for (Link l : links){
                if ((l.a == NA && l.b == NB) || (l.a == NB && l.b == NA))
                    return true;
            }
            return false;
        }

        protected Node getSelectedNode(ArrayRealVector probe){
            for (Node n : this.nodes.values()){
                if (n.pos.subtract(probe).getNorm() < 25/2f)
                    return n;
            }
            return null;
        }
    }
}