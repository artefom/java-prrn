package backend;

import backend.rasterio.RasterDataset;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.*;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;

import java.awt.image.Raster;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class GraphBuilder implements TaskProvider {

    private static Logger log = Logger.getLogger(GraphBuilder.class.getName());

    TaskFactoryBase tf;

    DefaultDirectedWeightedGraph<RasterDataset,DefaultWeightedEdge> graph;
    UndirectedGraph<RasterDataset,DefaultWeightedEdge> undirected_graph;

    public GraphBuilder() {
        graph = new DefaultDirectedWeightedGraph<RasterDataset, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        undirected_graph = new AsUndirectedGraph<>(graph);
    }

    public void add_file(String filename) throws IOException {
        RasterDataset ds1 = RasterDataset.from_file(filename);
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

    /**
     * Creates tasks for execution in TaskSheduler based on built graph.
     * Each task contains info about 2 images and intended to be solved by RRNPorcess.
     * @return collection of tasks
     */
    public Collection<TaskBase> get_tasks() {
        build_graph();

        ArrayList<TaskBase> tasks = new ArrayList<>();

        for (DefaultWeightedEdge edge : edges()) {
            RasterDataset ds_target = graph.getEdgeTarget(edge);
            RasterDataset ds_source = graph.getEdgeSource(edge);
            tasks.add( tf.create_task(ds_target,ds_source) );
        }

        return tasks;
    }

    public void set_task_factory(TaskFactoryBase tf) {
        this.tf = tf;
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

}
