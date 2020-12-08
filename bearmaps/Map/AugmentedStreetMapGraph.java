package bearmaps.Map;

import bearmaps.PathAlgorithm.streetmap.Node;
import bearmaps.PathAlgorithm.streetmap.StreetMapGraph;
import bearmaps.Coordinate.KDTree;
import bearmaps.Coordinate.Point;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 * @author  Michael Wang, Alan Yao, Josh Hug
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    private Map<Point, Node> pointNodeMap;
    private KDTree kdTree;
    private List<Node> nodes;
    private Map<String, List<Node>> nameToNodes;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);

        pointNodeMap = new HashMap<>();
        nameToNodes = new HashMap<>();

        nodes = this.getNodes();
        List<Point> points = nodeToPoint(nodes);

        kdTree = new KDTree(points);
    }

    // built a map to go between Nodes and Points
    // return a list of Points
    private List<Point> nodeToPoint(List<Node> nodes) {
        List<Point> result = new ArrayList<>();
        for (Node node : nodes) {
            if (name(node.id()) != null) {
                String lowercaseName = cleanString(name(node.id()));
                if (!nameToNodes.containsKey(lowercaseName)) {
                    nameToNodes.put(lowercaseName, new LinkedList<>());
                }
                nameToNodes.get(lowercaseName).add(node);
            }
            if (neighbors(node.id()).size() > 0) {
                Point point = new Point(node.lon(), node.lat());
                pointNodeMap.put(point, node);
                result.add(point);
            }
        }
        return result;
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Point targetPoint = kdTree.nearest(lon, lat);
        Node targetNode = pointNodeMap.get(targetPoint);
        return targetNode.id();
    }


    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        List<String> strings = new LinkedList<>();
        prefix = cleanString(prefix);
        for(String name:nameToNodes.keySet()){
            if(name.startsWith(prefix)){
                for(Node node:nameToNodes.get(name)){
                    String tmp = node.name();
                    if(!strings.contains(tmp)){
                        strings.add(tmp);
                    }
                }
            }
        }
        return strings;
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        List<Map<String, Object>> mapList = new LinkedList<>();
        String lowercaseName = cleanString(locationName);
        for (String name : nameToNodes.keySet()) {
            if (name.equals(lowercaseName)) {
                for (Node node : nameToNodes.get(name)) {
                    if (node.name() != null) {
                        if (cleanString(node.name()).contains(cleanString(lowercaseName))) {
                            Map<String, Object> locationInfo = new HashMap<>();
                            locationInfo.put("lon", node.lon());
                            locationInfo.put("lat", node.lat());
                            locationInfo.put("name", node.name());
                            locationInfo.put("id", node.id());
                            mapList.add(locationInfo);
                        }
                    }
                }
                break;
            }
        }
        return mapList;
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
