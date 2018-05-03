package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;



public class AppSTL {
	public static ArrayList<Triangle> liste_triangle = new ArrayList<Triangle>();

	public static void main(String[] args) throws Exception {

		//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
		DataOutputStream dos = new DataOutputStream(fos);

		//Recuperation des donnees du fichier shp
		ShpFile file = new ShpFile("ne_50m_admin_0_sovereignty.shp");
		//ne_50m_admin_0_sovereignty
		ArrayList<SimpleFeature> features = file.readFile();
		
	

		//Parcours toute la structure
		for(SimpleFeature feature:features){
				// Verification de la figure geometrique
				String s = feature.getAttribute("the_geom").toString();
				
				//if(s.indexOf("GEOMETRYCOLLECTION")!=-1){}
				if(s.indexOf("POINT ZM")!=-1){
				}
				else if(s.indexOf("POINT M")!=-1){
				}else if(s.indexOf("POINT EMPTY")!=-1){
				}else if(s.indexOf("MULTIPOINT")!=-1){
				}else if(s.indexOf("POINT")!=-1){
				}else if(s.indexOf("MULTILINESTRING")!=-1){
				}else if(s.indexOf("LINESTRING")!=-1){
				}else if(s.indexOf("MULTIPOLYGON EMPTY")!=-1){
				}else if(s.indexOf("MULTIPOLYGON")!=-1){
					MultiPolygon mp = (MultiPolygon) feature.getAttribute("the_geom");
					decomposeMultiPolygon(mp);
				}else if(s.indexOf("POLYGON")!=-1){
					Polygon polys = (Polygon) feature.getAttribute("the_geom");
					polygonSTL(polys);
				}
		}
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();

	}
	
	
	//Divise le multipolygon en polygon
	public static void decomposeMultiPolygon(MultiPolygon mp){
		Polygon polys;
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			polys = ((Polygon)mp.getGeometryN(i));
			polygonSTL(polys);
		}
	}

	
	//Recupere tous les triangles du polygon et les convertie en Triangle
	public static void polygonSTL(Polygon polys){
		ArrayList<Polygon> triangles = trianglePolygon(polys);
		for(Polygon p:triangles){
			Point3D[] point= new Point3D[3];
			Coordinate[] coord_triangle=p.getCoordinates();
			for(int i=0;i<3;i++){
				point[i] = new Point3D((float) coord_triangle[i].x,0.0f,(float) coord_triangle[i].y);
			}
			Triangle tri = new Triangle(point);
			liste_triangle.add(tri);
		}
	}
	
	
	//Recupere tous les triangles de la geometrie
	public static ArrayList<Polygon> trianglePolygon(Polygon polys){
		ArrayList<Polygon> allTriangle = new ArrayList<Polygon>();
		return trianglePolygon(polys,allTriangle);
	}	
	
	//Trouve une oreille la stock dans liste des triangles et la soustrait au polygone du debut
	private static ArrayList<Polygon> trianglePolygon(Polygon polys,ArrayList<Polygon> allTriangle ){
		int longueur = polys.getNumPoints();
		if(longueur == 4 ){
			allTriangle.add(polys);
			return allTriangle;
		}
		Coordinate[] coord_polys=polys.getCoordinates();
		for(int i = 0 ; i< longueur;i++){
			Polygon triangle = generateTriangle(coord_polys[i],coord_polys[(i+1)%longueur],coord_polys[(i+2)%longueur]);
			if(isHear(polys,triangle)){
				allTriangle.add(triangle);
				polys = (Polygon)polys.difference(triangle);
				return trianglePolygon(polys, allTriangle);
				}
		}
		return allTriangle;
	}	
	
	//Verifie si le triangle est une oreille
	public static boolean isHear(Polygon polys,Polygon triangle ){
		if(!polys.contains(triangle))
			return false;
		Geometry geom = polys.difference(triangle);
		if(geom instanceof Polygon)
			return true;
		return false;
	}
	
	//Genere un triangle avec 3 coordonnees
	public static Polygon generateTriangle(Coordinate a, Coordinate b, Coordinate c){
		GeometryFactory fact = new GeometryFactory();
		Coordinate[] coords = {a,b,c,a};
		Polygon newpolys =fact.createPolygon(coords);
		return newpolys;
	}	
	
}