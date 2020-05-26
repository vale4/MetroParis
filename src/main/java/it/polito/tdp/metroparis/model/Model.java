package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge>graph;
	private List<Fermata>fermate;
	private Map<Integer, Fermata>fermateIdMap;
	
	public Model() {
		this.graph=new SimpleDirectedGraph<>(DefaultEdge.class);
		
		MetroDAO dao=new MetroDAO();
		
		//CREAZIONE DI VERTICI
		this.fermate=dao.getAllFermate();
		this.fermateIdMap=new HashMap<>();
		for(Fermata f : this.fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		Graphs.addAllVertices(this.graph, this.fermate);
		
		//System.out.println(this.graph);
		
		
		//CREAZIONE DI ARCHI
		//-metodo 1 (coppia di vertici)
	/*	for(Fermata fp : this.fermate) {
			for(Fermata fa : this.fermate) {
				if(dao.fermateConnesse(fp, fa)) {
					this.graph.addEdge(fp, fa);
				}
			}
		}
		
		//System.out.println(this.graph);*/
		
		
		//-metodo 2 (da un vertice, trova tutti i connessi)
		/*for(Fermata fp : this.fermate) {
			List<Fermata>connesse=dao.fermateSuccessive(fp, fermateIdMap);
			
			for(Fermata fa : connesse) {
				this.graph.addEdge(fp, fa);
			}
		}*/
		
				
		
		//-metodo 3 (chiedo al database l'elenco degli archi)
		List<CoppiaFermate>coppie=dao.coppieFermate(fermateIdMap);
		for(CoppiaFermate c : coppie) {
			this.graph.addEdge(c.getFp(), c.getFa());
		}
		
		System.out.format("Grafo caricato con con %d vertici e %d archi\n", this.graph.vertexSet().size(), this.graph.edgeSet().size());
	}
	
	/**
	 * Visita l'intero grafo con la stategia Breadth First 
	 * e ritorna l'insieme dei vertici incontrati
	 * @param source vertice di partenza della visita
	 * @return insieme dei vertici incontrati
	 */
	public List<Fermata> visitaAmpiezza(Fermata source) {
		List<Fermata>visita=new ArrayList<>();
		GraphIterator<Fermata, DefaultEdge>bfv=new BreadthFirstIterator<>(graph, source);
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		return visita;
	}
	
	
	public List<Fermata> visitaProfondita(Fermata source) {
		List<Fermata>visita=new ArrayList<>();
		GraphIterator<Fermata, DefaultEdge>dfv=new DepthFirstIterator<>(graph, source);
		while(dfv.hasNext()) {
			visita.add(dfv.next());
		}
		return visita;
	}
	
	public Map<Fermata, Fermata> alberoVisita(Fermata source) {
		Map<Fermata, Fermata>albero=new HashMap<>();
		albero.put(source, null);
		
		GraphIterator<Fermata, DefaultEdge>bfv=new BreadthFirstIterator<>(graph, source);
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>(){
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				// la visita sta considerando un nuovo arco
				//questo arco ha scoperto un nuovo vertice?
				//se sì, provenendo da dove?
				DefaultEdge edge=e.getEdge(); //(a,b) : ho scoperto 'a' partendo da 'b' oppure 'b' da 'a'
				Fermata a=graph.getEdgeSource(edge);
				Fermata b=graph.getEdgeTarget(edge);
				if(albero.containsKey(a)) {
					albero.put(b,a);
				}else {
					albero.put(a,b);
				}

			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {
				// TODO Auto-generated method stub

			}
			
		});
		
		while(bfv.hasNext()) {
			bfv.next(); //estrai l'elemento e ignoralo
		}
		
		return albero;
	}
	
	
	public List<Fermata>camminiMinimi(Fermata partenza, Fermata arrivo) {
		DijkstraShortestPath<Fermata, DefaultEdge> dij=new DijkstraShortestPath<>(graph);
		
		GraphPath<Fermata, DefaultEdge>cammino=dij.getPath(partenza, arrivo);
		
		return cammino.getVertexList();
	}
	
	
	public static void main(String args[]) {
		Model m=new Model();
		List<Fermata>visita1=m.visitaAmpiezza(m.fermate.get(0));
		//System.out.println(visita1);
		List<Fermata>visita2=m.visitaProfondita(m.fermate.get(0));
		//System.out.println(visita2);
		
		Map<Fermata, Fermata>albero=m.alberoVisita(m.fermate.get(0));
		for(Fermata f : albero.keySet()) {
			//System.out.format("%s <- %s\n", f, albero.get(f));
		}
		
		List<Fermata>cammino=m.camminiMinimi(m.fermate.get(0), m.fermate.get(1));
		System.out.println(cammino);
	}

}
