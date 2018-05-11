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
	private int decoupex;
	private int decoupey;
	private String hauteur;

	public Conversion(ArrayList<File> liste_shapefile,int decoupex,int decoupey, String hauteur){
		this.liste_shapefile=liste_shapefile;
		this.decoupex=decoupex;
		this.decoupey=decoupey;
		this.hauteur=hauteur;
		this.liste_triangle = new ArrayList<Triangle>();
		gtt = new GeometryToTriangle(liste_triangle);
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
				String s = feature.getAttribute("the_geom").toString();
				//if(s.indexOf("GEOMETRYCOLLECTION")!=-1){}
				if(s.indexOf("POINT ZM")!=-1){}
				else if(s.indexOf("POINT M")!=-1){
				}else if(s.indexOf("POINT EMPTY")!=-1){
				}else if(s.indexOf("MULTIPOINT")!=-1){
				}else if(s.indexOf("POINT")!=-1){
				}else if(s.indexOf("MULTILINESTRING")!=-1){
				}else if(s.indexOf("LINESTRING")!=-1){
				}else if(s.indexOf("MULTIPOLYGON EMPTY")!=-1){
				}else if(s.indexOf("MULTIPOLYGON")!=-1){
					MultiPolygon mp = (MultiPolygon) feature.getAttribute("the_geom");
					liste_polygon=gtt.decomposeMultiPolygon(mp);
				}else if(s.indexOf("POLYGON")!=-1){
					Polygon polys = (Polygon) feature.getAttribute("the_geom");
					liste_polygon.add(polys);
				}
			}
		}
		Polygon[] tab_polys = new Polygon[liste_polygon.size()];
		for(int i=0;i<liste_polygon.size();i++){
			tab_polys[i]=liste_polygon.get(i);
		}
		GeometryFactory factory = new GeometryFactory();
		MultiPolygon total = factory.createMultiPolygon(tab_polys);
		Geometry geo = (Geometry) total;
		
		//Geometry test2 = test.intersection(geo);
		ArrayList<Geometry> liste = quadrillage(geo);
	}

	
	//Retourne le quadrillage de la Geometry
	public ArrayList<Geometry> quadrillage(Geometry geo){
		ArrayList<Geometry> quadri = new ArrayList<Geometry>();
		GeometryFactory fact = new GeometryFactory();
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		double intervalx = (coord[0].x+coord[2].x)/(double)decoupex;
		double intervaly = (coord[0].y+coord[2].y)/(double)decoupey;
		intervalx=Math.sqrt(Math.pow(intervalx-coord[0].x,2));
		intervaly=Math.sqrt(Math.pow(intervaly-coord[0].y,2));
		for(int i=0;i<decoupex+1;i++){
			System.out.println(i+" ------");
			for(int j=0;j<decoupey+1;j++){
				Coordinate coord1 = new Coordinate(coord[0].x+intervalx*i,coord[0].y+intervaly*j);
				Coordinate coord2 = new Coordinate(coord[0].x+intervalx*(i+1),coord[0].y+intervaly*j);
				Coordinate coord3 = new Coordinate(coord[0].x+intervalx*(i+1),coord[0].y+intervaly*(j+1));
				Coordinate coord4 = new Coordinate(coord[0].x+intervalx*i,coord[0].y+intervaly*(j+1));
				Coordinate[] cooord = {coord1,coord2,coord3,coord4,coord1};
				Polygon polys = fact.createPolygon(cooord);
				quadri.add((Geometry)polys);
			}
			System.out.println(" ------");
		}
		return quadri;
	}
	

	//Ecritur du fichier STL
	public void ecrireSTL() throws IOException{
		//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
		DataOutputStream dos = new DataOutputStream(fos);

		//Ecrit dans le fichier STL
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();
	}
}

