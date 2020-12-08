package bearmaps.Coordinate;

import java.util.List;

public class KDTree implements PointSet {

    private Node root;
    private int size;


    private static class Node {
        private Point p;      // the point
        private Node lb;        // the left/bottom subtree
        private Node rt;        // the right/top subtree
        private boolean vertical;

        Node(Point p,  boolean vertical) {
            this.p = p;
            this.vertical = vertical;
            lb = null;
            rt = null;
        }

        public boolean isVertical() {
            return vertical;
        }
    }

    public KDTree(List<Point> point2DS) {
        root = null;
        size = 0;

        for (Point p : point2DS) {
            insert(p);
        }
    }

    // is the set empty?
    public boolean isEmpty() {
        return size == 0;
    }

    // number of points in the set
    public int size() {
        return size;
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point p) {
        if (p == null) throw new IllegalArgumentException();

        if (contains(p)) return;

        if (size == 0) {
            root = new Node(p, true);
        } else {
            Node currNode = root;
            while (true) {
                int cmp = compare(currNode, p, currNode.isVertical());
                if (cmp > 0) {
                    if (currNode.rt == null) {
                        insert(currNode, p, true);
                        break;
                    } else {
                        currNode = currNode.rt;
                    }
                } else {
                    if (currNode.lb == null) {
                        insert(currNode, p, false);
                        break;
                    } else {
                        currNode = currNode.lb;
                    }
                }
            }
        }
        size++;
    }

    private void insert(Node insertNode, Point p, boolean insertRT) {
        boolean flag = insertNode.isVertical();
        if (flag) {
            if (insertRT) {
                insertNode.rt = new Node(p, false);
            } else {
                insertNode.lb = new Node(p,  false);
            }
        } else {
            if (insertRT) {
                insertNode.rt = new Node(p, true);
            } else {
                insertNode.lb = new Node(p,true);
            }

        }
    }


    // does the set contain point p?
    public boolean contains(Point p) {
        if (p == null) throw new IllegalArgumentException();

        boolean flag = false;
        Node currNode = root;
        if (size == 0) return false;
        while (true) {
            if (currNode.p.equals(p)) {
                flag = true;
                break;
            }
            int cmp = compare(currNode, p, currNode.isVertical());
            if (cmp > 0) {
                if (currNode.rt != null) {
                    currNode = currNode.rt;
                } else break;
            } else {
                if (currNode.lb != null) {
                    currNode = currNode.lb;
                } else break;
            }
        }
        return flag;
    }



    private int compare(Node curr, Point cmp, boolean cmpX) {
        if (cmpX) {
            if (curr.p.getX() >= cmp.getX()) {
                return -1;
            }
        } else {
            if (curr.p.getY() >= cmp.getY()) {
                return -1;
            }
        }
        return 1;
    }

    @Override
    public Point nearest(double x, double y) {
        Point p = new Point(x,y);
        return nearest(root, root.p, p);
    }


    private Point nearest(Node currnode, Point nearestP, Point p) {

        if (currnode == null) return nearestP;

        if (Point.distance(p,currnode.p)<Point.distance(p,nearestP)) nearestP = currnode.p;

        int cmp = compare(currnode, p, currnode.isVertical());
        Node goodSide,badSide;
        if(cmp>0){
            goodSide = currnode.rt;
            badSide = currnode.lb;
        }else{
            goodSide = currnode.lb;
            badSide = currnode.rt;
        }

        nearestP = nearest(goodSide,nearestP,p);
        if(badSide!=null && isWorthLooking(currnode,nearestP,p)){
            nearestP = nearest(badSide,nearestP,p);
        }


        return nearestP;
    }
    private boolean isWorthLooking(Node n, Point best, Point target) {
        double distToBest = Point.distance(best, target);
        double distToBad;
        if (n.isVertical()) {
            distToBad = Point.distance(new Point(n.p.getX(),target.getY()), target);
        } else {
            distToBad = Point.distance(new Point(target.getX(),n.p.getY()), target);
        }
        return distToBad < distToBest;
    }
}
