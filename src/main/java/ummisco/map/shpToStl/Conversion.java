package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Conversion {

	private ArrayList<File> liste_shapefile = new ArrayList<File>();
	private GeometryToTriangle gtt;
	private ArrayList<Triangle> liste_triangle = new ArrayList<Triangle>();
	private ArrayList<Polygon> liste_polygon = new ArrayList<Polygon>();
	private ArrayList<Double> liste_hauteur= new ArrayList<Double>();
	private int decoupex;
	private int decoupey;
	private String hauteur;

	public Conversion(ArrayList<File> liste_shapefile,int decoupex,int decoupey, String hauteur){
		this.liste_shapefile=liste_shapefile;
		this.decoupex=decoupex;
		this.decoupey=decoupey;
		this.hauteur=hauteur;
		this.liste_triangle = new ArrayList<Triangle>();
		this.gtt = new GeometryToTriangle();
	}


	//Parcours les fichiers shapefiles et regroupe dans les Geometry en un
	public void parcoursFichier() throws IOException{
		for(int i=0;i<liste_shapefile.size();i++){

			//Recuperation des donnees du fichier shp
			ShpFile file = new ShpFile(liste_shapefile.get(i));
			ArrayList<SimpleFeature> features = file.readFile();

			//Parcours toutes la structure
			for(SimpleFeature feature:features){
				
				//Verification de forme geometrique
				Geometry geom = (Geometry) feature.getAttribute("the_geom");
				if(geom instanceof Polygon){
					Polygon polys = (Polygon) geom;
					liste_polygon.add(polys);
					if(!hauteur.equals("Error"));
						liste_hauteur.add((Double) feature.getAttribute(hauteur));
				}
				if(geom instanceof MultiPolygon){
					MultiPolygon mp = (MultiPolygon) geom;
					liste_polygon=gtt.decomposeMultiPolygon(mp);
				}
			}
		}
		
		//Regroupe tous les polygons dans un MultiPolygon puis le met en Geometry
		Polygon[] tab_polys = new Polygon[liste_polygon.size()];
		for(int i=0;i<liste_polygon.size();i++){
			tab_polys[i]=liste_polygon.get(i);
		}
		GeometryFactory factory = new GeometryFactory();
		MultiPolygon total = factory.createMultiPolygon(tab_polys);
		Geometry geo = (Geometry) total;
		
		//Creer quadrillage pour la Geometry
		ArrayList<Geometry> liste = quadrillage(geo);
		
		//Divise la Geometry avec le quadrillage
		for(int i=0;i<liste.size();i++){
			Geometry res = geo.intersection(liste.get(i));
			if(res instanceof Polygon){
				Polygon respoly = (Polygon) res;
				gtt.polygonSTL(respoly);
			}
			else if(res instanceof MultiPolygon){
				MultiPolygon resmul = (MultiPolygon) res;
				ArrayList<Polygon> listepolys = gtt.decomposeMultiPolygonQuadra(resmul);
				for(int j=0;j<listepolys.size();j++){
					gtt.polygonSTL(listepolys.get(j));
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
	public ArrayList<Geometry> quadrillage(Geometry geo){
		ArrayList<Geometry> quadri = new ArrayList<Geometry>();
		double intervalx;
		double intervaly;
		GeometryFactory fact = new GeometryFactory();
		Geometry limite = geo.getEnvelope();	
		Coordinate[] coord = limite.getCoordinates();
		if(coord[0].x<0 && coord[2].x<0)
			intervalx = Math.abs((Math.abs(coord[2].x)+coord[0].x)/(double)(decoupex+1));
		else
			intervalx = (Math.abs(coord[2].x)+Math.abs(coord[0].x))/(double)(decoupex+1);
		if(coord[0].y<0 && coord[2].y<0)
			intervaly = Math.abs((Math.abs(coord[2].y)+coord[0].y)/(double)(decoupey+1));
		else
			intervaly = (Math.abs(coord[2].y)+Math.abs(coord[0].y))/(double)(decoupey+1);
		for(int i=0;i<decoupex+1;i++){
			for(int j=0;j<decoupey+1;j++){
				Coordinate coord1 = new Coordinate(coord[0].x+intervalx*i,coord[0].y+intervaly*j);
				Coordinate coord2 = new Coordinate(coord[0].x+intervalx*(i+1),coord[0].y+intervaly*j);
				Coordinate coord3 = new Coordinate(coord[0].x+intervalx*(i+1),coord[0].y+intervaly*(j+1));
				Coordinate coord4 = new Coordinate(coord[0].x+intervalx*i,coord[0].y+intervaly*(j+1));
				Coordinate[] cooord = {coord1,coord2,coord3,coord4,coord1};
				Polygon polys = fact.createPolygon(cooord);
				quadri.add((Geometry)polys);
			}
		}
		return quadri;
	}
	

	//Ecritur du fichier STL
	public void ecrireSTL(int num) throws IOException{
		
		//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("STL"+num+".stl");
		DataOutputStream dos = new DataOutputStream(fos);

		//Ecrit dans le fichier STL
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();
		dos.close();
		fos.close();
	}
	
	
	//Parcours tous les polygons pour retrouver la hauteur du polygon donne
	public double hauteurPolygon(Geometry geo){
		
		return 2;
	}
}

