package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Conversion {

	private ArrayList<File> liste_shapefile = new ArrayList<File>();
	private ArrayList<Triangle> liste_triangle = new ArrayList<Triangle>();
	//private ArrayList<Polygon> liste_polygon = new ArrayList<Polygon>();
	private Map<Geometry,Double> liste_polygon= new HashMap<Geometry,Double>();
	private GeometryToTriangle gtt;
	private int decoupe;
	private String hauteur;

	public Conversion(ArrayList<File> liste_shapefile,int coupe, String hauteur){
		this.liste_shapefile=liste_shapefile;
		this.decoupe=coupe;
		this.hauteur=hauteur;
		this.liste_triangle = new ArrayList<Triangle>();
		this.gtt = new GeometryToTriangle();
	}


	//Parcours les fichiers shapefiles et stock les Polygons et leur hauteur
	public void parcoursFichier() throws IOException{
		for(int i=0;i<liste_shapefile.size();i++){
			ShpFile file = new ShpFile(liste_shapefile.get(i));
			ArrayList<SimpleFeature> features = file.readFile();
			for(SimpleFeature feature:features){
				Geometry geom = (Geometry) feature.getAttribute("the_geom");
				if(geom instanceof Polygon){
					Polygon polys = (Polygon) geom;
					if(!hauteur.equals("Error")){
						liste_polygon.put(polys,(((Number)feature.getAttribute(hauteur)).doubleValue()));
					}
					else
						liste_polygon.put(polys,0.0);
				}
				if(geom instanceof MultiPolygon){
					MultiPolygon mp = (MultiPolygon) geom;
					ArrayList<Polygon> listepoly = new ArrayList<Polygon>();
					listepoly=gtt.decomposeMultiPolygon(mp,listepoly);
					if(!hauteur.equals("Error")){
					for(Polygon polys:listepoly){
							liste_polygon.put(polys,(((Number)feature.getAttribute(hauteur)).doubleValue()));
						}
					}
					else{
						for(Polygon polys:listepoly){
							liste_polygon.put(polys,0.0);
						}
				
					}
				}
			}
		}
		regroupePolygon();
	}


	//Regroupe tous les polygons dans un MultiPolygon puis le met en Geometry
	public void regroupePolygon() throws IOException{
		double haut=0;
		Polygon[] tab_polys = new Polygon[liste_polygon.keySet().size()];
		int ii = 0;
		for(Geometry p:liste_polygon.keySet())
		{
			tab_polys[ii] = (Polygon)p;
			ii++;
		}
			
		GeometryFactory factory = new GeometryFactory();
		Geometry geo = factory.createMultiPolygon(tab_polys);
		//Geometry geo = (Geometry) total;
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		ArrayList<Geometry> liste = quadrillage(coord[0],coord[2],decoupe);

		//Divise la Geometry avec le quadrillage
		for(int i=0;i<liste.size();i++){
			Geometry res = geo.intersection(liste.get(i));
			System.out.println("Geometry "+i );
			if(res instanceof Polygon){
				Polygon respoly = (Polygon) res;
				//haut = hauteurPolygon(respoly);
				gtt.polygonSTL(respoly,haut);
			}
			else if(res instanceof MultiPolygon){
				MultiPolygon resmul = (MultiPolygon) res;
				ArrayList<Polygon> listepolys = new ArrayList<Polygon>();
				listepolys = gtt.decomposeMultiPolygon(resmul,listepolys);
				for(int j=0;j<listepolys.size();j++){
					//haut = hauteurPolygon(listepolys.get(j));
					gtt.polygonSTL(listepolys.get(j),haut);
				}
			}
			else
				System.out.println(res);
			liste_triangle=gtt.getListeTriangle();

			//Ecrit fichier STL 
			ecrireSTL(i);
			gtt.videListe();
		}
	}


	//Retourne le quadrillage de la Geometry
	public ArrayList<Geometry> quadrillage(Coordinate min, Coordinate max, int coupe){
		ArrayList<Geometry> quadri = new ArrayList<Geometry>();
		double intervalx = (max.x - min.x ) / width;
		double intervaly =(max.y - min.y ) / height;
		GeometryFactory fact = new GeometryFactory();
		//Point p;
		//p.distance(g);
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				Coordinate coord1 = new Coordinate(min.x+intervalx*i,min.y+intervaly*j);
				Coordinate coord2 = new Coordinate(min.x+intervalx*(i+1),min.y+intervaly*j);
				Coordinate coord3 = new Coordinate(min.x+intervalx*(i+1),min.y+intervaly*(j+1));
				Coordinate coord4 = new Coordinate(min.x+intervalx*i,min.y+intervaly*(j+1));
				Coordinate[] cooord = {coord1,coord2,coord3,coord4,coord1};
				Polygon polys = fact.createPolygon(cooord);
				quadri.add(polys);
			}
		}
		return quadri;
	}


	//Ecritur du fichier STL
	public void ecrireSTL(int num) throws IOException{
		FileOutputStream fos = new FileOutputStream("STL"+num+".stl");
		DataOutputStream dos = new DataOutputStream(fos);
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();
		dos.close();
		fos.close();
	}


	//Parcours tous les polygons pour retrouver la hauteur du polygon donne
/*	public double hauteurPolygon(Polygon polys){
		for(int i=0;i<liste_polygon.size();i++){
			if(liste_polygon.get(i).overlaps(polys)){
				System.out.println(i+" - "+liste_hauteur);
				return liste_hauteur.get(i);
			}	
		}
		System.out.println(polys);
		System.out.println("zero");
		return 0;
	}*/
}

