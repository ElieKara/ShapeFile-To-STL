package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Coordinate;
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
		ArrayList<SimpleFeature> features = file.readFile();
		
	

		//Parcours toute la structure
		for(SimpleFeature feature:features){
			if(feature.getID().equals("ne_50m_admin_0_sovereignty.200")){
				
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
				}
			}
		}
		System.out.println(liste_triangle.size());

	}
	
	
	//Divise le multipolygon en polygon
	public static void decomposeMultiPolygon(MultiPolygon mp){
		Polygon polys;
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			polys = ((Polygon)mp.getGeometryN(i));
			polygonSTL(polys);
		}
	}

	
	//Recupere un triangle qui compose le polygone et stock les points du triangle
	public static void polygonSTL(Polygon polys){
		Polygon triangle;
		Point3D[] point= new Point3D[3];
		System.out.println("*****-------------------"+polys.getNumPoints());

		while(polys.getNumPoints()!=4){
			//System.out.println(polys);
			System.out.println(polys.getNumPoints());

			triangle = trianglePolygon(polys);
			//System.out.println("---"+triangle.getNumPoints());
			//System.out.println("---"+triangle);
			polys=enlevePointPolygon(polys,triangle);
			Coordinate[] coord_triangle=triangle.getCoordinates();
			for(int i=0;i<3;i++)
				point[i] = new Point3D((float) coord_triangle[i].x,0.0f,(float) coord_triangle[i].y);
			Triangle tri = new Triangle(point);
			liste_triangle.add(tri);
		}
		Coordinate[] coord_polys=polys.getCoordinates();
		for(int i=0;i<3;i++)
			point[i] = new Point3D((float) coord_polys[i].x,0.0f,(float) coord_polys[i].y);
		Triangle tri = new Triangle(point);
		liste_triangle.add(tri);
		System.out.println("-------------------*****"+polys.getNumPoints());
	}
	
	
	
	//Réalise la soustraction entre polygon 
	public static Polygon enlevePointPolygon(Polygon polys, Polygon triangle){
		if(polys.getNumPoints()-1!=polys.difference(triangle).getNumPoints()){
			MultiPolygon mp = (MultiPolygon)polys.difference(triangle);
			for (int i = 1; i < mp.getNumGeometries(); i++) {
				polys = ((Polygon)mp.getGeometryN(i));
				polygonSTL(polys);
			}
			return (Polygon)mp.getGeometryN(0);
		}
		//System.out.println("***"+polys.getNumPoints());
		//System.out.println("***"+polys.difference(triangle).getNumPoints());
		//System.out.println("***"+polys.difference(triangle));
		return (Polygon) polys.difference(triangle).getGeometryN(0);
	}
	
	
	//Recupere un triangle qui est a l'interieur du polygon
	public static Polygon trianglePolygon(Polygon polys){
		int longueur = polys.getNumPoints();
		if(longueur==4)
			return polys;
		for(int i = 0 ; i< longueur-1;i++)
		{
			for(int j = 2 ; j<longueur - 2 ;j++)
			{
				Polygon triangle;
				int point_debut = i;
				int point_fin = (i+j)%longueur;
				if(orientationPolygon(polys,point_debut,point_fin)){
					triangle=newPolygon(polys,point_debut,point_fin);
				}
				else{
					triangle=newPolygon(polys,point_fin,point_debut);
				}
				if(polys.contains(triangle))
					return trianglePolygon(triangle);	
			}
		}
		return null;
		
	}


	//Renvoie true si le petit polygon va de A vers B
	public static boolean orientationPolygon(Polygon polys,int pointA, int pointB){
		Coordinate[] coord_polys=polys.getCoordinates();
		int petit,grand,sens1,sens2;
		if(pointA<pointB){
			petit=pointA;
			grand=pointB;
			sens1=grand-petit;
			sens2=coord_polys.length-sens1;
			if(sens1<sens2)
				return true;
			else 
				return false;
		}
		else{
			petit=pointB;
			grand=pointA;
			sens1=grand-petit;
			sens2=coord_polys.length-sens1;
			if(sens1<sens2)
				return false;
			else 
				return true;
		}
	}

	//Divise le polygon en 2 polygon et renvoie le plus petit
	public static Polygon newPolygon(Polygon polys, int pointA, int pointB){
		Coordinate[] coord_polys=polys.getCoordinates();
		GeometryFactory fact = new GeometryFactory();
		int dist = pointB - pointA;
		if(dist<0)
			dist=dist*(-1);
		int mSize = coord_polys.length-dist;
		if(mSize>dist)
			mSize=dist;
		mSize=mSize+2;
		Coordinate[] coords = new Coordinate[mSize];
		int j = 0;
		for(int i = pointA;j<mSize;i++){
			
			if(i%coord_polys.length == coord_polys.length-1)
				i++;
			coords[j] = coord_polys[i%coord_polys.length];
			j++;
		}
		coords[mSize-1]=coord_polys[pointA];
		Polygon newpolys =fact.createPolygon(coords);
		return newpolys;
	}

	//Ecrire des float en little endian dans le fichier STL
	public static void writeFloatLE(DataOutputStream out, float value) throws IOException{
		writeIntLE(out,Float.floatToRawIntBits(value));
	}

	//Ecrire des int en little endian dans le fichier STL
	public static void writeIntLE(DataOutputStream out, int value) throws IOException{
		out.writeByte(value & 0xFF);
		out.writeByte((value >> 8) & 0xFF);
		out.writeByte((value >> 16) & 0xFF);
		out.writeByte((value >> 24) & 0xFF);

	}
}