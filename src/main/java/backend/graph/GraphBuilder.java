package backend.graph;

import backend.rasterio.RasterDataset;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.*;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * This Class builds graph for RRN processing.
 */
public class GraphBuilder {

    private static Logger log = Logger.getLogger(GraphBuilder.class.getName());

    DefaultDirectedWeightedGraph<RasterDataset,DefaultWeightedEdge> graph;
    UndirectedGraph<RasterDataset,DefaultWeightedEdge> undirected_graph;

    public GraphBuilder() {
        graph = new DefaultDirectedWeightedGraph<RasterDataset, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        undirected_graph = new AsUndirectedGraph<>(graph);
    }

    public void add_file(RasterDataset ds1) {
        graph.addVertex( ds1 );

        for (RasterDataset ds2 : vertices()) {
            if (ds1.equals(ds2)) continue;
            double weight = ds1.adjacent_weight(ds2);
            if (weight > 0) {
                DefaultWeightedEdge e = graph.addEdge(ds1,ds2);
                graph.setEdgeWeight(e,weight);
            }
        }
    }

    /**
     * Get size of results arrays
     * @return
     */
    public int get_results_size() {
        return edge_count();
    }

    /**
     * Populate results array
     * @param source
     * @param target
     * @param weights
     */
    public void get_results( RasterDataset[] source, RasterDataset[] target, double[] weights ) {

        build_graph();

        int i = 0;
        for (DefaultWeightedEdge e : edges()) {
            source[i] = graph.getEdgeSource(e);
            target[i] = graph.getEdgeTarget(e);
            weights[i] = graph.getEdgeWeight(e);
            i += 1;
        }
    }

    public int vertices_count() {
        return graph.vertexSet().size();
    }

    public int edge_count() {
        return graph.edgeSet().size();
    }

    Collection<RasterDataset> vertices() {
        return graph.vertexSet();
    }

    Collection<DefaultWeightedEdge> edges() {
        return graph.edgeSet();
    }


    private DirectedGraph<RasterDataset,DefaultWeightedEdge> get_graph() {
        return graph;
    }

    private UndirectedGraph<RasterDataset,DefaultWeightedEdge> get_undirected_graph() {
        return undirected_graph;
    }

    /**
     * Finds adjacent images and record them to graph. build spanning tree.
     */
    private void build_graph() {
        log.info(String.format("Building graph for %d vertices, %d edges",
                vertices_count(),edge_count()));

        MinimumSpanningTree<RasterDataset,DefaultWeightedEdge> min_span_tree =
                new KruskalMinimumSpanningTree<>(graph);

        Set<DefaultWeightedEdge> min_edge_set = min_span_tree.getMinimumSpanningTreeEdgeSet();

        // Delete all unnecessary edges
        Set<DefaultWeightedEdge> edges_for_deletion = new HashSet<>();
        for (DefaultWeightedEdge e : graph.edgeSet()) {
            if (!min_edge_set.contains(e)) {
                edges_for_deletion.add(e);
            }
        }
        graph.removeAllEdges(edges_for_deletion);
    }

    public void save_graph_dot(String filename) {

        // Make sure graph is builded
        build_graph();

        IntegerNameProvider<RasterDataset> p1=new IntegerNameProvider<RasterDataset>();
        StringNameProvider<RasterDataset> p2=new StringNameProvider<RasterDataset>();

        DOTExporter graphviz_exporter = new DOTExporter(p1,p2,null);

        try {
            log.info(String.format("Exporing graph with %d vertices, %d edges to %s",
                    vertices_count(),edge_count(),filename));
            graphviz_exporter.export(new FileWriter(filename),graph);
        } catch (IOException ex) {
            log.severe(String.format("Error exporting graph to %s. Reason: %s",filename,ex.getMessage()) );
        }

    }

    public static GraphBuilder from_datasets( Collection<RasterDataset> datasets ) {
        GraphBuilder gbuilder = new GraphBuilder();
        for (RasterDataset ds : datasets) {
            gbuilder.add_file(ds);
        }
        return gbuilder;
    }

}
